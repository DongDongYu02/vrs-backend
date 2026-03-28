package cn.dong.nexus.modules.vrs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "小程序授权登录")
public class VrsLoginDTO {

    @Schema(description = "code")
    @NotBlank
    private String code;

    @Schema(description = "js_code")
    @NotBlank
    private String jsCode;
}
