package cn.dong.nexus.modules.vrs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "试岗申请审批状态 DTO")
public class VrsTrialPositionStatusUpdateDTO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "状态")
    private Integer status;


}
