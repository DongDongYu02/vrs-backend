package cn.dong.nexus.modules.vrs.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vrs_booking")
public class VrsBooking extends BaseEntity {

    private String intervieweeId;

    private String receptionArea;

    private String receptionDept;

    private String receptionistName;

    private String receptionistContact;

    private String visitorName;

    private String visitorContact;

    private LocalDateTime visitingTime;

    private String visitingReason;

    private Integer status;

    private Integer isShare;

    private String inviterId;

    private String licensePlate;

    private String creator;

    private Integer vrsType;

    private String ekpReviewId;

    private String photoUrl;

    private String creatorOpenid;

    private String visitorCompany;

    private LocalDateTime actualVisitTime;

}
