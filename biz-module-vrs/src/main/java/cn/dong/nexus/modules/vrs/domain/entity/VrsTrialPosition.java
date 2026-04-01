package cn.dong.nexus.modules.vrs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vrs_trial_position")
public class VrsTrialPosition {
    /**
     * 主键
     */
    private String id;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员手机号
     */
    private String personPhone;

    /**
     * 试岗部门
     */
    private String dept;

    /**
     * 试岗岗位
     */
    private String position;

    /**
     * 员工负责人
     */
    private String respPerson;


    /**
     * 是否住宿 0否1是
     */
    private Integer isAccommodation;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 试岗开始日期
     */
    private LocalDate beginTime;

    /**
     * 试岗结束日期
     */
    private LocalDate endTime;

    /**
     * 是否老带新 0否1是
     */
    private Integer isOlderLead;

    /**
     * 带领员工
     */
    private String leadEmployee;

    /**
     * 人员照片 URL
     */
    private String personPhotoUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人（职员姓名或访客）
     */
    private String creator;

    private String createBy;

    /**
     * 蓝凌流程 ID
     */
    private String ekpReviewId;

    private Integer status;
}