package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionStatusUpdateDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPosition;
import cn.dong.nexus.modules.vrs.domain.query.VrsTrialPositionQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsTrialPositionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IVrsTrialPositionService extends IService<VrsTrialPosition> {
    /**
     * 新增申请
     */
    void create(VrsTrialPositionDTO dto);

    /**
     * 更新申请审批状态
     */
    void updateStatus(VrsTrialPositionStatusUpdateDTO dto);

    /**
     * 获取申请记录列表
     */
    List<VrsTrialPositionVO> getList(VrsTrialPositionQuery query);
}
