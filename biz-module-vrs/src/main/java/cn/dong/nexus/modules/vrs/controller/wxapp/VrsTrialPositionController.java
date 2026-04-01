package cn.dong.nexus.modules.vrs.controller.wxapp;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.modules.vrs.domain.dto.VrsTrialPositionDTO;
import cn.dong.nexus.modules.vrs.domain.query.VrsTrialPositionQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsTrialPositionVO;
import cn.dong.nexus.modules.vrs.service.IVrsTrialPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("VrsTrialPositionController_App")
@RequestMapping("/vrs/wxapp/trial")
@Tag(name = "试岗管理")
@RequiredArgsConstructor
public class VrsTrialPositionController {
    private final IVrsTrialPositionService trialPositionService;


    @PostMapping
    @Operation(summary = "新增申请")
    public Result<Void> create(@RequestBody @Validated VrsTrialPositionDTO dto) {
        trialPositionService.create(dto);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "申请列表")
    public Result<List<VrsTrialPositionVO>> list(@ParameterObject VrsTrialPositionQuery query) {
        List<VrsTrialPositionVO> result = trialPositionService.getList(query);
        return Result.success(result);
    }


}
