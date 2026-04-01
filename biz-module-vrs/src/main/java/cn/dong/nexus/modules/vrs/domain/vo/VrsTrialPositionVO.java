package cn.dong.nexus.modules.vrs.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "试岗申请列表 VO")
public class VrsTrialPositionVO {

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "人员手机号")
    private String personPhone;

    @Schema(description = "试岗部门")
    private String dept;

    @Schema(description = "试岗岗位")
    private String position;

    @Schema(description = "试岗开始日期")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate beginTime;

    @Schema(description = "试岗结束日期")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate endTime;

}
