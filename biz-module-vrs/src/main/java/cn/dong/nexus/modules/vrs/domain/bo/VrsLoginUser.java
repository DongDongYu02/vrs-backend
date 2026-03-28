package cn.dong.nexus.modules.vrs.domain.bo;

import cn.dong.nexus.core.security.context.LoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VrsLoginUser extends LoginUser {
    private Integer vrsType;
    private String openid;
}
