package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPositionCode;
import cn.dong.nexus.modules.vrs.mapper.VrsTrialPositionCodeMapper;
import cn.dong.nexus.modules.vrs.service.IVrsTrialPositionCodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VrsTrialPositionCodeServiceImpl extends ServiceImpl<VrsTrialPositionCodeMapper, VrsTrialPositionCode> implements IVrsTrialPositionCodeService {

}
