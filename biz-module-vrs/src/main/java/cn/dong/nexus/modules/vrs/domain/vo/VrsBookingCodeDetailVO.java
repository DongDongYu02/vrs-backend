package cn.dong.nexus.modules.vrs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "访客码详情")
@NoArgsConstructor
@AllArgsConstructor
public class VrsBookingCodeDetailVO {

    private VrsBookingVO booking;

    private VrsBookingCodeVO code;
}
