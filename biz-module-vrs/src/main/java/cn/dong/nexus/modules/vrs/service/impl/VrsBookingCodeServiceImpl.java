package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.modules.vrs.domain.entity.VrsBookingCode;
import cn.dong.nexus.modules.vrs.mapper.VrsBookingCodeMapper;
import cn.dong.nexus.modules.vrs.service.IVrsBookingCodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VrsBookingCodeServiceImpl extends ServiceImpl<VrsBookingCodeMapper, VrsBookingCode> implements IVrsBookingCodeService {
}
