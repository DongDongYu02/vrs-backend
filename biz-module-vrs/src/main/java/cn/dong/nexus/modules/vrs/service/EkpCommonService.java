package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.vrs.domain.bo.EkpReviewBO;
import cn.dong.nexus.modules.vrs.domain.bo.EkpUserBO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsBookingDTO;
import cn.dong.nexus.modules.vrs.mapper.EkpCommonMapper;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        // 被访单位
        content.set("fd_3eb701f7efa0f8", "浙江可得电子科技有限公司");
        // 接待区域
        content.set("fd_3eb7020398c5c4", dto.getReceptionArea());
        // 接待部门
        content.set("fd_3eb7020d462c06", dto.getReceptionDept());
        // 接待人
        content.set("fd_3eb702434791f6", dto.getReceptionistName());
        // 接待人联系方式
        content.set("fd_3eb702683c9f64", dto.getReceptionistContact());
        // 来访人姓名
        content.set("fd_3eb7027c43ad7a", dto.getVisitorName());
        // 来访人联系方式
        content.set("fd_3eb702ac0b1fc6", dto.getVisitorContact());
        // 来访时间
        content.set("fd_3eb702c2168aac", dto.getVisitingTime());
        // 来访事由
        content.set("fd_3ceda385caa300", dto.getVisitingReason());
        // 访客系统预约 ID
        content.set("fd_3eb8824010abf4", dto.getId());

        // 获取接待人
        EkpUserBO receptionist = ekpCommonMapper.selectEkpUserByPhone(dto.getReceptionistContact());
        if (Objects.nonNull(receptionist)) {
            content.set("fd_3f0bb3c94d2948", receptionist.getLoginName());
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
     * 删除蓝凌访客预约审批流
     *
     * @param ekpReviewId ekp 审批流ID
     */
    @DSTransactional(rollbackFor = Exception.class)
    public void abandonBookingEkpReview(String ekpReviewId) {
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
        reviewContent.put("fd_3ecd905233d8e4", LocalDateTimeUtil.format(now, GlobalConstants.DatePattern.Y_M_D_H_M));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos, "UTF-8", true, 0)) {
            encoder.writeObject(reviewContent instanceof HashMap
                    ? reviewContent
                    : new HashMap<>(reviewContent));
        }
        String newXmlStr = baos.toString(StandardCharsets.UTF_8);
        ekpCommonMapper.updateReviewContent(ekpReviewId, newXmlStr);
    }
}
