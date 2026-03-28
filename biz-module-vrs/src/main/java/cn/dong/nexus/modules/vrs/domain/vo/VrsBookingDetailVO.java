package cn.dong.nexus.modules.vrs.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "访客预约详情 VO")
public class VrsBookingDetailVO {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "创建人 ID 来自EKP")
    private String createBy;

    @Schema(description = "创建人名称")
    private String creator;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;

    @Schema(description = "被访单位")
    private String intervieweeId;

    @Schema(description = "接待区域")
    private String receptionArea;

    @Schema(description = "接待部门")
    private String receptionDept;

    @Schema(description = "接待人")
    private String receptionistName;

    @Schema(description = "接待人联系方式")
    private String receptionistContact;

    @Schema(description = "来访人")
    private String visitorName;

    @Schema(description = "来访人联系方式")
    private String visitorContact;

    @Schema(description = "来访时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime visitingTime;

    @Schema(description = "来访事由")
    private String visitingReason;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否为邀请预约 0否1是")
    private Integer isShare;

    @Schema(description = "邀请人 EKP ID")
    private String inviterId;

    @Schema(description = "车牌号")
    private String licensePlate;

    @Schema(description = "申请人的系统用户类型")
    private Integer vrsType;

    @Schema(description = "EKP 流程ID")
    private String ekpReviewId;

    @Schema(description = "照片 url")
    private String photoUrl;

    @Schema(description = "创建人 openid")
    private String creatorOpenid;

    @Schema(description = "来访公司")
    private String visitorCompany;

    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    @Schema(description = "实际到访时间")
    private LocalDateTime actualVisitTime;
}
