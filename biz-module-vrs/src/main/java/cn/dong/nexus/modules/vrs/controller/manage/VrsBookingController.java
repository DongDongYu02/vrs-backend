package cn.dong.nexus.modules.vrs.controller.manage;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.dong.nexus.modules.vrs.domain.dto.VrsUpdateBookingStatusDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.dong.nexus.modules.vrs.service.IVrsBookingService;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/vrs/booking")
@Tag(name = "预约管理")
@RequiredArgsConstructor
public class VrsBookingController {
    private final IVrsBookingService vrsBookingService;

    @PostMapping("/update-status")
    @Operation(summary = "更新预约状态")
    public Result<Void> updateStatus(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        VrsUpdateBookingStatusDTO dto = JSONUtil.toBean(body, VrsUpdateBookingStatusDTO.class);
        VrsBooking vrsBooking = vrsBookingService.lambdaQuery().eq(VrsBooking::getEkpReviewId, dto.getId()).one();
        if (Objects.isNull(vrsBooking)) {
            return Result.error(ApiMessage.NOT_FOUND);
        }
        vrsBooking.setStatus(dto.getStatus());
        vrsBookingService.updateStatus(vrsBooking);
        return Result.success();
    }

}
