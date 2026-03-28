package cn.dong.nexus.modules.vrs.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.base.BaseQuery;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "访客预约 查询对象")
public class VrsBookingQuery extends BaseQuery<VrsBooking> {

    @Schema(description = "来访人姓名")
    @Query(SqlKeyword.LIKE)
    private String visitorName;

    @Schema(description = "来访人联系方式")
    @Query(SqlKeyword.LIKE)
    private String visitorContact;

    @Schema(description = "接待人联系方式")
    @Query(SqlKeyword.LIKE)
    private String receptionistContact;

    @Schema(description = "来访时间区间 'start,end'")
    private String visitingTime;

    @Schema(description = "是否为分享邀请预约 0否1是")
    @Query(SqlKeyword.EQ)
    private Integer isShare;

    @Schema(description = "创建人")
    @Query(SqlKeyword.EQ)
    private String createBy;

    @Schema(description = "邀请人ID")
    private String inviterId;

    @Schema(description = "访客系统用户类型")
    private Integer vrsType;

    @Schema(description = "关键字")
    private String searchValue;

    public QueryWrapper<VrsBooking> toWxappQueryWrapper() {
        QueryWrapper<VrsBooking> queryWrapper = new QueryWrapper<>();
        // 查看范围：保安和管理员查看所有，访客和职员只能查看与自己相关
        if (!VrsConstants.VrsType.SECURITY_PERSON.equals(vrsType) &&
            !VrsConstants.VrsType.ADMIN.equals(vrsType)) {
            queryWrapper.lambda().and(w -> w
                    .eq(VrsBooking::getCreateBy, this.createBy).or()
                    .eq(VrsBooking::getInviterId, this.createBy).or()
                    .eq(VrsBooking::getReceptionistContact, this.receptionistContact).or()
                    .eq(VrsBooking::getVisitorContact, this.visitorContact));
        }
        // 关键字搜索
        if (StrUtil.isNotBlank(this.searchValue)) {
            queryWrapper.lambda().and(w -> w
                    .like(VrsBooking::getVisitorContact, this.searchValue).or()
                    .like(VrsBooking::getVisitorName, this.searchValue).or()
                    .like(VrsBooking::getReceptionistContact, this.searchValue).or()
                    .like(VrsBooking::getReceptionistName, this.searchValue));
        }
        queryWrapper.lambda().orderByDesc(BaseEntity::getCreateTime);
        return queryWrapper;
    }
}
