package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.modules.vrs.domain.bo.EkpReviewBO;
import cn.dong.nexus.modules.vrs.domain.bo.EkpUserBO;
import cn.dong.nexus.modules.vrs.domain.bo.VrsLoginUser;
import cn.dong.nexus.modules.vrs.domain.dto.VrsBookingDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionDTO;
import cn.dong.nexus.modules.vrs.mapper.EkpCommonMapper;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@DS(GlobalConstants.DataSource.EKP_SQLSERVER)
@RequiredArgsConstructor
public class EkpCommonService {
    private final EkpCommonMapper ekpCommonMapper;
    private final IAuthContext authContext;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    public EkpUserBO getEkpUserByPhone(String phone) {
        return ekpCommonMapper.selectEkpUserByPhone(phone);
    }

    public EkpUserBO getEkpUserById(String id) {
        return ekpCommonMapper.selectEkpUserById(id);
    }

    public EkpUserBO getEkpUserInfoByPhone(String phone) {
        EkpUserBO ekpUser = this.getEkpUserByPhone(phone);
        if (Objects.isNull(ekpUser)) {
            return null;
        }
        EkpUserBO ekpUserInfo = this.getEkpUserById(ekpUser.getId());
        ekpUserInfo.setLoginName(ekpUser.getLoginName());
        ekpUserInfo.setPhone(ekpUser.getPhone());
        return ekpUserInfo;
    }


    /**
     * 启动蓝凌访客预约审批流
     */
    public String startBookingEkpReview(VrsBookingDTO dto) {
        String url = appProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        String docSubject = StrUtil.format("访客{}的来访预约申请", dto.getVisitorName());
        String docCreator = """
                {"LoginName":"vrs"}
                """;
        String fdTemplateId = appProperties.getEkp().getVrsReviewTemplateId();
        // 判断预约人是否为蓝凌用户
        EkpUserBO ekpUser = ekpCommonMapper.selectEkpUserByPhone(dto.getSubmitPhone());
        if (Objects.nonNull(ekpUser)) {
            docCreator = StrUtil.format("""
                    {"LoginName":"{}"}
                    """, ekpUser.getLoginName());
        }
        JSONObject content = new JSONObject();
        AppProperties.Ekp.Review.BookingField bookingField = appProperties.getEkp().getReview().getBookingField();
        // 被访单位
        content.set(bookingField.getInterviewee(), "德赛集团有限公司");
        // 接待区域
        content.set(bookingField.getReceptionArea(), dto.getReceptionArea());
        // 接待部门
        content.set(bookingField.getReceptionDept(), dto.getReceptionDept());
        // 接待人
        content.set(bookingField.getReceptionistName(), dto.getReceptionistName());
        // 接待人联系方式
        content.set(bookingField.getReceptionistContact(), dto.getReceptionistContact());
        // 来访人姓名
        content.set(bookingField.getVisitorName(), dto.getVisitorName());
        // 来访人联系方式
        content.set(bookingField.getVisitorContact(), dto.getVisitorContact());
        // 来访单位
        content.set(bookingField.getVisitorContact(), dto.getVisitorCompany());
        // 来访时间
        content.set(bookingField.getVisitingTime(), dto.getVisitingTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
        // 来访事由
        content.set(bookingField.getVisitingReason(), dto.getVisitingReason());
//        // 访客系统预约 ID
//        content.set("fd_3eb8824010abf4", dto.getId());

        // 获取接待人
        EkpUserBO receptionist = ekpCommonMapper.selectEkpUserByPhone(dto.getReceptionistContact());
        if (Objects.nonNull(receptionist)) {
            content.set(bookingField.getLoginName(), receptionist.getLoginName());
        }

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", fdTemplateId);
        wholeForm.add("formValues", content.toJSONString(1));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP访客预约流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP访客预约流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);

        }
        return body;
    }

    /**
     * 删除蓝凌审批流
     *
     * @param ekpReviewId ekp 审批流ID
     */
    @DSTransactional(rollbackFor = Exception.class)
    public void abandonEkpReview(String ekpReviewId) {
        ekpCommonMapper.deleteReviewAreader(ekpReviewId);
        ekpCommonMapper.deleteReviewOreader(ekpReviewId);
        ekpCommonMapper.deleteBookingReview(ekpReviewId);
        ekpCommonMapper.deleteReviewTodo(ekpReviewId);
    }

    public void updateBookingActualVisitTimeToReview(String ekpReviewId, LocalDateTime now) {
        EkpReviewBO review = ekpCommonMapper.selectReviewById(ekpReviewId);
        Map<String, Object> reviewContent;
        try (XMLDecoder decoder = new XMLDecoder(
                new ByteArrayInputStream(review.getExtendDataXml().getBytes(StandardCharsets.UTF_8)))) {
            reviewContent = (Map<String, Object>) decoder.readObject();
        }
        reviewContent.put(appProperties.getEkp().getReview().getBookingField().getActualVisitTime(),
                LocalDateTimeUtil.format(now, GlobalConstants.DatePattern.Y_M_D_H_M));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos, "UTF-8", true, 0)) {
            encoder.writeObject(reviewContent instanceof HashMap
                    ? reviewContent
                    : new HashMap<>(reviewContent));
        }
        String newXmlStr = baos.toString(StandardCharsets.UTF_8);
        ekpCommonMapper.updateReviewContent(ekpReviewId, newXmlStr);
    }

    public String startTrialPositionReview(VrsTrialPositionDTO dto) {
        VrsLoginUser loginUser = (VrsLoginUser) authContext.getLoginUser();
        String docSubject = StrUtil.format("人员{}的试岗申请", dto.getPersonName());
        String docCreator = """
                {"LoginName":"vrs"}
                """;
        EkpUserBO ekpUser = ekpCommonMapper.selectEkpUserByPhone(loginUser.getPhone());
        if (Objects.nonNull(ekpUser)) {
            docCreator = StrUtil.format("""
                    {"LoginName":"{}"}
                    """, ekpUser.getLoginName());
        }
        MultiValueMap<String, Object> form = this.buildTrialPositionReviewContent(dto);

        form.add("docSubject", docSubject);
        form.add("docCreator", docCreator);
        form.add("docStatus", 20);
        form.add("fdTemplateId", appProperties.getEkp().getVrsTrialPositionTemplateId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        String url = appProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (Objects.isNull(resp.getBody()) || StrUtil.isBlank(resp.getBody())) {
            log.error("发起试岗申请审批流失败，EKP接口返回异常，url={}, body={}", url, resp.getBody());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(resp.getBody())) {
            log.error("发起试岗申请审批流失败，EKP接口返回异常，url={}, body={}", url, resp.getBody());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return resp.getBody();


    }

    private MultiValueMap<String, Object> buildTrialPositionReviewContent(VrsTrialPositionDTO dto) {
        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();

        JSONObject content = new JSONObject();
        // 员工姓名
        content.set("fd_3f0ed32c089896", dto.getPersonName());
        // 员工手机号
        content.set("fd_3f0ed35662cc0c", dto.getPersonPhone());
        // 试岗部门
        content.set("fd_3f0ed361748b10", dto.getDept());
        // 试岗岗位
        content.set("fd_3f0ed374137180", dto.getPosition());
        // 是否住宿
        content.set("fd_3f0ee0096ef68c", dto.getIsAccommodation());
        // 员工负责人
        content.set("fd_3f0ed387ac7252", dto.getRespPerson());
        // 出生日期
        content.set("fd_3f0eeff3f58cee", dto.getBirthday().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 试岗开始日期
        content.set("fd_3f0eefec46b624", dto.getBeginTime().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 试岗结束日期
        content.set("fd_3f0eeff774b0da", dto.getEndTime().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 是否老带新
        content.set("fd_3f0ed3d26fb45e", dto.getIsOlderLead());
        // 带领员工
        content.set("fd_3f0ed3edef6b3a", dto.getLeadEmployee());
        // 备注
        content.set("fd_3ceda385caa300", dto.getRemark());
        // 本人照片
        String attForm = "attachmentForms[0]";
        wholeForm.add(attForm + ".fdKey", "fd_3f0ee006f4587e");
        String extName = FileUtil.extName(dto.getPersonPhotoUrl());
        wholeForm.add(attForm + ".fdFileName", StrUtil.format("{}.{}", RandomUtil.randomString(5), extName));
        String imagePath = appProperties.getFileUploadPath() + dto.getPersonPhotoUrl();
        wholeForm.add(attForm + ".fdAttachment", new FileSystemResource(new File(imagePath)));

        wholeForm.add("formValues", content.toJSONString(1));
        return wholeForm;
    }
}
