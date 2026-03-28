package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.modules.vrs.domain.entity.VrsUser;
import cn.dong.nexus.modules.vrs.mapper.VrsUserMapper;
import cn.dong.nexus.modules.vrs.service.IVrsUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VrsUserServiceImpl extends ServiceImpl<VrsUserMapper, VrsUser> implements IVrsUserService {
}
