package cn.dong.nexus.modules.vrs.controller;

import cn.dong.nexus.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/auth")
@Tag(name = "访客预约")
@RequiredArgsConstructor
public class VrsController {
    @GetMapping
    @Operation(summary = "访客预约")
    public Result<Void> login() {
        return Result.success();
    }
}
