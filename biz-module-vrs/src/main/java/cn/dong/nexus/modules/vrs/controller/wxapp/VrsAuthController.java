package cn.dong.nexus.modules.vrs.controller.wxapp;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.dong.nexus.modules.vrs.domain.dto.VrsLoginDTO;
import cn.dong.nexus.modules.vrs.service.IVrsAuthService;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/vrs/auth")
@Tag(name = "授权登录")
@RequiredArgsConstructor
public class VrsAuthController {

    private final IVrsAuthService vrsAuthService;

    @PostMapping("/login")
    @Operation(summary = "小程序授权登录")
    public Result<LoginUserVO> login(@RequestBody @Validated VrsLoginDTO dto) {
        LoginUserVO result = vrsAuthService.login(dto);
        return Result.success(result);
    }

    @PostMapping("/provide/ekp/accessToken")
    @Operation(summary = "供EKP调用本服务接口的 token")
    public JSONObject provideEkpAccessToken() {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.nonNull(token)) {
            return new JSONObject().set("access_token", token);
        }
        String accessToken = RandomUtil.randomString(16);
        RedisUtil.set(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN, accessToken, 60 * 60 * 24);
        return new JSONObject().set("access_token", accessToken);
    }


}
