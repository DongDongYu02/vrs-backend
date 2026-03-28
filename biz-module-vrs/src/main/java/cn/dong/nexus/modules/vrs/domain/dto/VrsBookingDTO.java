package cn.dong.nexus.modules.vrs.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "访客预约 DTO")
public class VrsBookingDTO extends BaseDTO<VrsBooking> {

    @Schema(description = "被访单位")
    @NotBlank
    private String intervieweeId;

    @Schema(description = "接待区域")
    private String receptionArea;

    @Schema(description = "接待部门")
    @NotBlank
    private String receptionDept;

    @Schema(description = "接待人")
    @NotBlank
    private String receptionistName;

    @Schema(description = "接待人联系方式")
    @NotBlank
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "请输入正确的接待人联系方式！"
    )
    private String receptionistContact;

    @Schema(description = "来访人")
    @NotBlank
    private String visitorName;

    @Schema(description = "来访单位")
    private String visitorCompany;

    @Schema(description = "来访人联系方式")
    @NotBlank
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "请输入正确的来访人联系方式！"
    )
    private String visitorContact;

    @Schema(description = "来访时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = GlobalConstants.ZoneTime.GMT8)
    @NotNull
    private LocalDateTime visitingTime;

    @Schema(description = "来访事由")
    private String visitingReason;

    @Schema(description = "车牌号")
    private String licensePlate;

    @Schema(description = "是否为邀请预约 0否1是")
    private Integer isShare;

    @Schema(description = "邀请人 EKP ID")
    private String inviterId;

    @Schema(description = "来访人照片")
    private String photoUrl;

    @Schema(description = "提交人的手机号")
    private String submitPhone;

    public void checkContactEqual() {
        if (receptionistContact.equals(visitorContact)) {
            throw new BizException("接待人联系方式和来访人联系方式不能相同！");
        }
    }
}
