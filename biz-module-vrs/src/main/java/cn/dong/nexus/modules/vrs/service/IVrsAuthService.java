package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsLoginDTO;

public interface IVrsAuthService {
    LoginUserVO login(VrsLoginDTO dto);
}
