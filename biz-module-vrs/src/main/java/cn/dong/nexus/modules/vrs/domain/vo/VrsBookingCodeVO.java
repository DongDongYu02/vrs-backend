package cn.dong.nexus.modules.vrs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "访客码 VO")
public class VrsBookingCodeVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "访客码地址")
    private String url;

    @Schema(description = "预约 ID")
    private String vrsBookingId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否已使用 0否1是")
    private Integer used;
}
