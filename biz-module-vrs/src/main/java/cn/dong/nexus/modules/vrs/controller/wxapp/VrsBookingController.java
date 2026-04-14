package cn.dong.nexus.modules.vrs.controller.wxapp;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.modules.vrs.domain.dto.VrsBookingDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsUpdateBookingStatusDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.dong.nexus.modules.vrs.domain.query.VrsBookingQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingVO;
import cn.dong.nexus.modules.vrs.service.IVrsBookingService;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController("VrsBookingController_App")
@RequestMapping("/vrs/wxapp/booking")
@Tag(name = "预约管理")
@RequiredArgsConstructor
public class VrsBookingController {
    private final IVrsBookingService vrsBookingService;

    @GetMapping("/get-share-info")
    @Operation(summary = "获取邀请信息")
    public Result<JSONObject> getShareInfo(@RequestParam("shareId") String shareId) {
        JSONObject result = vrsBookingService.getShareInfo(shareId);
        return Result.success(result);
    }

    @PostMapping("/create-share-info")
    @Operation(summary = "创建邀请信息")
    public Result<String> getShareInfo(@RequestBody JSONObject body) {
        String uuid = vrsBookingService.createShareInfo(body);
        return Result.success(uuid, ApiMessage.SUCCESS.getMessage());
    }

    @PostMapping
    @Operation(summary = "新增预约")
    public Result<Void> create(@RequestBody @Validated VrsBookingDTO dto) {
        vrsBookingService.create(dto);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "预约列表")
    public Result<List<VrsBookingVO>> list(@ParameterObject VrsBookingQuery query) {
        List<VrsBookingVO> records = vrsBookingService.getList(query);
        return Result.success(records);
    }

    @GetMapping("{id}")
    @Operation(summary = "预约详情")
    public Result<VrsBookingDetailVO> detail(@PathVariable String id) {
        VrsBookingDetailVO detail = vrsBookingService.getDetailById(id);
        return Result.success(detail);
    }

    @GetMapping("/{id}/code")
    @Operation(summary = "获取访客码")
    public Result<VrsBookingCodeVO> getBookingCode(@PathVariable String id) {
        VrsBookingCodeVO bookingCode = vrsBookingService.getBookingCode(id);
        return Result.success(bookingCode);
    }

    @PostMapping("/update-status")
    @Operation(summary = "更新预约状态")
    public Result<Void> updateStatus(@RequestBody @Validated VrsUpdateBookingStatusDTO dto) {
        VrsBooking vrsBooking = vrsBookingService.lambdaQuery().eq(VrsBooking::getId, dto.getId()).one();
        if (Objects.isNull(vrsBooking)) {
            return Result.error(ApiMessage.NOT_FOUND);
        }
        vrsBooking.setStatus(dto.getStatus());
        vrsBookingService.updateStatus(vrsBooking);
        return Result.success();
    }

    @GetMapping("/code-used")
    @Operation(summary = "获取访客码使用状态")
    public Result<Integer> getCodeUsed(@RequestParam("codeId") String codeId) {
        Integer used = vrsBookingService.getCodeUsed(codeId);
        return Result.success(used);
    }

    @PostMapping("/code/{codeId}/write-off")
    @Operation(summary = "访客码核销")
    public Result<Integer> codeWriteOff(@PathVariable String codeId) {
        vrsBookingService.codeWriteOff(codeId);
        return Result.success();
    }

    @GetMapping("/latest-code")
    @Operation(summary = "获取最新可用访客码")
    public Result<VrsBookingCodeDetailVO> getLatestCode() {
        VrsBookingCodeDetailVO vo = vrsBookingService.getLatestCode();
        return Result.success(vo);
    }


}
