package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.bo.VrsLoginUser;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionStatusUpdateDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPosition;
import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPositionCode;
import cn.dong.nexus.modules.vrs.domain.query.VrsTrialPositionQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsTrialPositionVO;
import cn.dong.nexus.modules.vrs.mapper.VrsTrialPositionMapper;
import cn.dong.nexus.modules.vrs.service.EkpCommonService;
import cn.dong.nexus.modules.vrs.service.IVrsTrialPositionCodeService;
import cn.dong.nexus.modules.vrs.service.IVrsTrialPositionService;
import cn.dong.nexus.modules.vrs.util.VrsQrCodeUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VrsTrialPositionServiceImpl extends ServiceImpl<VrsTrialPositionMapper, VrsTrialPosition> implements IVrsTrialPositionService {
    private final IAuthContext authContext;
    private final EkpCommonService ekpCommonService;
    private final IVrsTrialPositionCodeService trialPositionCodeService;

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void create(VrsTrialPositionDTO dto) {
        dto.doValidate();
        VrsLoginUser loginUser = ((VrsLoginUser) authContext.getLoginUser());
        VrsTrialPosition entity = dto.toEntity();
        entity.setCreator(loginUser.getUsername());
        this.save(entity);

        // 发起蓝凌审批
        String ekpReviewId = ekpCommonService.startTrialPositionReview(dto);

        // 这里先报保存再发审批后回写ID 是为了数据一致性
        entity.setEkpReviewId(ekpReviewId);
        this.lambdaUpdate().set(VrsTrialPosition::getEkpReviewId, ekpReviewId)
                .eq(VrsTrialPosition::getId, entity.getId())
                .update();
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateStatus(VrsTrialPositionStatusUpdateDTO dto) {
        if (ObjectUtil.hasEmpty(dto.getStatus(), dto.getId())) {
            log.error("EKP 回调试岗申请状态失败，参数缺失，param={}", dto);
        }
        VrsTrialPosition entity = this.lambdaQuery().eq(VrsTrialPosition::getEkpReviewId, dto.getId()).one();
        if (ObjectUtil.isEmpty(entity)) {
            log.error("EKP 回调试岗申请状态失败，未找到对应记录，param={}", dto);
            return;
        }
        this.lambdaUpdate().set(VrsTrialPosition::getStatus, dto.getStatus())
                .eq(VrsTrialPosition::getId, entity.getId())
                .update();

        switch (dto.getStatus()) {
            // 审批通过
            case VrsConstants.TRIAL_POSITION_STATUS.APPROVED -> {
                // 生成访客码
                this.generateQrCode(entity.getId());
                // TODO 通知访客
            }
            // 审批驳回
            case VrsConstants.TRIAL_POSITION_STATUS.REJECTED -> {
                // 删除 EKP流程
                ekpCommonService.abandonEkpReview(entity.getEkpReviewId());
                // 通知访客
            }
            // 取消申请
            case VrsConstants.TRIAL_POSITION_STATUS.CANCELED -> {
                // 删除 EKP流程
                ekpCommonService.abandonEkpReview(entity.getEkpReviewId());
            }
        }
    }

    @Override
    public List<VrsTrialPositionVO> getList(VrsTrialPositionQuery query) {
        List<VrsTrialPosition> records = this.list(query.toWxappQueryWrapper());
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, VrsTrialPositionVO.class);

    }

    private void generateQrCode(String id) {
        JSONObject content = new JSONObject()
                .set("id", id)
                .set("type", VrsConstants.QrCodeType.TRIAL_POSITION);
        String url = VrsQrCodeUtil.generate(JSONUtil.toJsonStr(content));
        VrsTrialPositionCode code = new VrsTrialPositionCode();
        code.setTrialPositionId(id);
        code.setUrl(url);
        trialPositionCodeService.save(code);
    }
}
