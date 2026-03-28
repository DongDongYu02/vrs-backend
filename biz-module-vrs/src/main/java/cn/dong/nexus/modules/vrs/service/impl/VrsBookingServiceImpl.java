package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.bo.VrsLoginUser;
import cn.dong.nexus.modules.vrs.domain.dto.VrsBookingDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsUpdateBookingStatusDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBookingCode;
import cn.dong.nexus.modules.vrs.domain.query.VrsBookingQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingVO;
import cn.dong.nexus.modules.vrs.mapper.VrsBookingMapper;
import cn.dong.nexus.modules.vrs.service.EkpCommonService;
import cn.dong.nexus.modules.vrs.service.IVrsBookingCodeService;
import cn.dong.nexus.modules.vrs.service.IVrsBookingService;
import cn.dong.nexus.modules.vrs.service.WechatService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VrsBookingServiceImpl extends ServiceImpl<VrsBookingMapper, VrsBooking> implements IVrsBookingService {
    private final IAuthContext authContext;
    private final EkpCommonService ekpCommonService;
    private final AppProperties appProperties;
    private final IVrsBookingCodeService vrsBookingCodeService;
    private final WechatService wechatService;

    @Override
    public String createShareInfo(JSONObject body) {
        String uuid = UUID.randomUUID().toString();
        // 链接30分钟过期
        RedisUtil.set(GlobalConstants.CacheKey.VRS_SHARE_INFO_PREFIX + uuid, body, 30, TimeUnit.MINUTES);
        return uuid;
    }

    @Override
    public JSONObject getShareInfo(String shareId) {
        JSONObject result = RedisUtil.get(GlobalConstants.CacheKey.VRS_SHARE_INFO_PREFIX + shareId, JSONObject.class);
        if (Objects.isNull(result)) {
            throw new BizException("此邀请链接已过期！");
        }
        return result;
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void create(VrsBookingDTO dto) {
        dto.checkContactEqual();
        VrsBooking booking = dto.toEntity();
        VrsLoginUser loginUser = (VrsLoginUser) authContext.getLoginUser();
        booking.setCreator(loginUser.getUsername());
        booking.setCreatorOpenid(loginUser.getOpenid());
        booking.setVrsType(loginUser.getVrsType());
        this.save(booking);
        dto.setId(booking.getId());
        dto.setSubmitPhone(authContext.getLoginUser().getPhone());
        // 发起 EKP 流程，会返回流程ID
        String ekpReviewId = ekpCommonService.startBookingEkpReview(dto);
        this.lambdaUpdate().eq(VrsBooking::getId, booking.getId())
                .set(VrsBooking::getEkpReviewId, ekpReviewId)
                .update();

    }

    @Override
    public List<VrsBookingVO> getList(VrsBookingQuery query) {
        List<VrsBooking> records = this.list(query.toWxappQueryWrapper());
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, VrsBookingVO.class);
    }

    @Override
    public VrsBookingDetailVO getDetailById(String id) {
        VrsBooking booking = this.getById(id);
        if (Objects.isNull(booking)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        return BeanUtil.copyProperties(booking, VrsBookingDetailVO.class);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateStatus(VrsUpdateBookingStatusDTO dto) {
        VrsBooking vrsBooking = this.lambdaQuery().eq(VrsBooking::getId, dto.getId()).one();
        if (Objects.isNull(vrsBooking)) {
            throw new BizException("访客预约记录不存在！");
        }
        // 更新状态
        this.lambdaUpdate().eq(VrsBooking::getId, vrsBooking.getId())
                .set(VrsBooking::getStatus, dto.getStatus())
                .update();
        switch (dto.getStatus()) {
            // 审批通过
            case VrsConstants.VrsBookingStatus.APPROVED -> {
                // 生成访客码
                this.generateVrsBookingCode(vrsBooking.getId());
                // TODO 通知访客
                vrsBooking.setStatus(VrsConstants.VrsBookingStatus.APPROVED);
                wechatService.sendBooingStatusMessage(vrsBooking);
            }
            // 审批驳回
            case VrsConstants.VrsBookingStatus.REJECTED -> {
                // 删除 EKP流程
                ekpCommonService.abandonBookingEkpReview(vrsBooking.getEkpReviewId());
                // 通知访客
                vrsBooking.setStatus(VrsConstants.VrsBookingStatus.REJECTED);
                wechatService.sendBooingStatusMessage(vrsBooking);
            }
            // 取消预约
            case VrsConstants.VrsBookingStatus.CANCELED -> {
                // 删除 EKP流程
                ekpCommonService.abandonBookingEkpReview(vrsBooking.getEkpReviewId());
            }
        }


    }

    @Override
    public VrsBookingCodeVO getBookingCode(String id) {
        VrsBooking booking = this.getById(id);
        if (Objects.isNull(booking)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        VrsBookingCode code = vrsBookingCodeService.lambdaQuery().eq(VrsBookingCode::getVrsBookingId, id).one();
        if (Objects.isNull(code)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        VrsBookingCodeVO vo = BeanUtil.copyProperties(code, VrsBookingCodeVO.class);
        LocalDate visitingDate = booking.getVisitingTime().toLocalDate();
        LocalDate now = LocalDate.now();
        if (visitingDate.isEqual(now)) {
            vo.setStatus(VrsConstants.VrsBookingCodeStatus.NORMAL);
        } else if (visitingDate.isBefore(now)) {
            vo.setStatus(VrsConstants.VrsBookingCodeStatus.EXPIRED);
        } else {
            vo.setStatus(VrsConstants.VrsBookingCodeStatus.NOT_YET);
        }
        return vo;
    }

    @Override
    public Integer getCodeUsed(String codeId) {
        VrsBookingCode code = vrsBookingCodeService.lambdaQuery()
                .select(VrsBookingCode::getUsed)
                .eq(VrsBookingCode::getId, codeId)
                .one();
        if (Objects.isNull(code)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        return code.getUsed();
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void codeWriteOff(String codeId) {
        VrsBookingCode code = vrsBookingCodeService.getById(codeId);
        if (Objects.isNull(code)) {
            throw new BizException("无效的访客码！");
        }
        VrsBooking vrsBooking = this.getById(code.getVrsBookingId());
        if (Objects.isNull(vrsBooking)) {
            throw new BizException("无效的访客码！");
        }
        // 预约有效期判断
        LocalDate visitingTime = vrsBooking.getVisitingTime().toLocalDate();
        LocalDate today = LocalDate.now();
        if (visitingTime.isBefore(today)) {
            throw new BizException("该访客码已失效，请确认来访日期！");
        }
        if (visitingTime.isAfter(today)) {
            throw new BizException("该访客码尚未生效，请确认来访日期！");
        }
        if (GlobalConstants.INT_YES.equals(code.getUsed())) {
            throw new BizException("该访客码已核销，请勿重复使用！");
        }
        // 核销
        LocalDateTime now = LocalDateTime.now();
        vrsBookingCodeService.lambdaUpdate()
                .set(VrsBookingCode::getUsed, GlobalConstants.INT_YES)
                .set(VrsBookingCode::getUsedTime, now)
                .eq(VrsBookingCode::getId, codeId)
                .update();
        this.lambdaUpdate()
                .set(VrsBooking::getActualVisitTime, now)
                .set(VrsBooking::getStatus, VrsConstants.VrsBookingStatus.VISITED)
                .eq(BaseEntity::getId, vrsBooking.getId()).update();
        // 把实际到访时间更新到 EKP
        ekpCommonService.updateBookingActualVisitTimeToReview(vrsBooking.getEkpReviewId(), now);

    }

    @Override
    public VrsBookingCodeDetailVO getLatestCode() {
        LoginUser loginUser = authContext.getLoginUser();
        List<VrsBooking> bookings = this.lambdaQuery()
                .eq(VrsBooking::getVisitorContact, loginUser.getPhone())
                .ge(VrsBooking::getVisitingTime, LocalDate.now())
                .list();
        if (bookings.isEmpty()) {
            throw new BizException("无最新可用访客码，请预约申请通过后再试！！");
        }
        List<String> bookingIds = bookings.stream().map(BaseEntity::getId).toList();
        VrsBookingCode code = vrsBookingCodeService.lambdaQuery()
                .in(VrsBookingCode::getVrsBookingId, bookingIds)
                .eq(VrsBookingCode::getUsed, GlobalConstants.INT_NO)
                .orderByDesc(VrsBookingCode::getId)
                .last("LIMIT 1")
                .one();
        if (Objects.isNull(code)) {
            throw new BizException("无最新可用访客码，请预约申请通过后再试！！");
        }
        // 查询出该访客码的预约记录
        VrsBooking booking = this.getById(code.getVrsBookingId());
        if (Objects.isNull(booking)) {
            throw new BizException("无最新可用访客码，请预约申请通过后再试！！");
        }

        VrsBookingCodeVO codeVO = BeanUtil.copyProperties(code, VrsBookingCodeVO.class);
        LocalDate visitingDate = booking.getVisitingTime().toLocalDate();
        LocalDate now = LocalDate.now();
        if (visitingDate.isEqual(now)) {
            codeVO.setStatus(VrsConstants.VrsBookingCodeStatus.NORMAL);
        } else if (visitingDate.isBefore(now)) {
            codeVO.setStatus(VrsConstants.VrsBookingCodeStatus.EXPIRED);
        } else {
            codeVO.setStatus(VrsConstants.VrsBookingCodeStatus.NOT_YET);
        }
        VrsBookingVO bookingVO = BeanUtil.copyProperties(booking, VrsBookingVO.class);
        return new VrsBookingCodeDetailVO(bookingVO, codeVO);

    }

    private void generateVrsBookingCode(String id) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = appProperties.getFileUploadPath() + today;
        FileUtil.mkdir(dir);

        String fileName = SecureUtil.md5(id) + ".png";
        String path = dir + "/" + fileName;
        try {
            File tempFile = File.createTempFile("logoTemp", ".png");
            ClassPathResource resource =
                    new ClassPathResource("static/kede-logo.png");
            FileCopyUtils.copy(resource.getInputStream(),
                    new FileOutputStream(tempFile));
            QrConfig qrConfig = QrConfig.create().setImg(tempFile)
                    .setHeight(500)
                    .setWidth(500);
            QrCodeUtil.generate(id, qrConfig, FileUtil.file(path));
            String codePath = today + "/" + fileName;
            VrsBookingCode vrsBookingCode = new VrsBookingCode();
            vrsBookingCode.setVrsBookingId(id);
            vrsBookingCode.setUrl(codePath);
            vrsBookingCodeService.save(vrsBookingCode);
        } catch (Exception e) {
            log.error("访客码生成失败：{}", e.getMessage());
        }
    }


}
