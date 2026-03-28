package cn.dong.nexus.modules.vrs.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "访客预约 VO")
public class VrsBookingVO {
    @Schema(description = "id")
    private String id;

    @Schema(description = "接待部门")
    private String receptionDept;

    @Schema(description = "接待人")
    private String receptionistName;

    @Schema(description = "来访人")
    private String visitorName;

    @Schema(description = "来访单位")
    private String visitorCompany;

    @Schema(description = "来访人联系方式")
    private String visitorContact;

    @Schema(description = "来访时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime visitingTime;

    @Schema(description = "来访事由")
    private String visitingReason;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;


}
