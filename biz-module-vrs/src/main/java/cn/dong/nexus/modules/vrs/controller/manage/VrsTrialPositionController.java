package cn.dong.nexus.modules.vrs.controller.manage;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionStatusUpdateDTO;
import cn.dong.nexus.modules.vrs.service.IVrsTrialPositionService;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/vrs/trial")
@Tag(name = "试岗管理")
@RequiredArgsConstructor
public class VrsTrialPositionController {
    private final IVrsTrialPositionService trialPositionService;

    @PostMapping("/ekp/status/callback")
    @Operation(summary = "EKP 试岗审批状态回调")
    public Result<Void> ekpStatusCallback(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        VrsTrialPositionStatusUpdateDTO dto = JSONUtil.toBean(body, VrsTrialPositionStatusUpdateDTO.class);
        trialPositionService.updateStatus(dto);
        return Result.success();
    }
}
