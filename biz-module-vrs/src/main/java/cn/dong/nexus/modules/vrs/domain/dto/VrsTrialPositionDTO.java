package cn.dong.nexus.modules.vrs.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPosition;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "试岗申请 DTO")
public class VrsTrialPositionDTO extends BaseDTO<VrsTrialPosition> {

    @NotBlank
    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "人员手机号")
    @NotBlank
    private String personPhone;

    @Schema(description = "试岗部门")
    private String dept;

    @Schema(description = "试岗岗位")
    @NotBlank
    private String position;

    @Schema(description = "员工负责人")
    private String respPerson;

    @Schema(description = "是否住宿 0否1是")
    @NotNull
    private Integer isAccommodation;

    @Schema(description = "出生日期")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate birthday;

    @Schema(description = "试岗开始日期")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    @NotNull
    private LocalDate beginTime;

    @Schema(description = "试岗结束日期")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    @NotNull
    private LocalDate endTime;

    @Schema(description = "是否老带新 0否1是")
    @NotNull
    private Integer isOlderLead;

    @Schema(description = "带领员工")
    private String leadEmployee;

    @Schema(description = "人员照片 URL")
    private String personPhotoUrl;

    @Schema(description = "备注")
    private String remark;


    @Override
    public void doValidate() {
        super.doValidate();
        if (endTime.isBefore(beginTime)) {
            throw new BizException("试岗结束日期不能早于开始日期");
        }
        // 判断时间段是与提交过的重叠
        boolean exists = Db.lambdaQuery(VrsTrialPosition.class)
                .ne(isUpdate(), VrsTrialPosition::getId, getId())
                .le(VrsTrialPosition::getBeginTime, endTime)
                .ge(VrsTrialPosition::getEndTime, beginTime)
                .exists();
        if (exists) {
            throw new BizException("试岗时间段内已提交过申请！");
        }

    }
}
