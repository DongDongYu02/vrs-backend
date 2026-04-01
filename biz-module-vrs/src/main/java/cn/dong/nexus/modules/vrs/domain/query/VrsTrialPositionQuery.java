package cn.dong.nexus.modules.vrs.domain.query;

import cn.dong.nexus.core.base.BaseQuery;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.bo.VrsLoginUser;
import cn.dong.nexus.modules.vrs.domain.entity.VrsTrialPosition;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "试岗申请列表查询对象")
public class VrsTrialPositionQuery extends BaseQuery<VrsTrialPosition> {

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "人员手机号")
    private String personPhone;

    @Schema(description = "员工负责人")
    private String respPerson;

    @Schema(description = "关键字")
    private String keyword;


    public QueryWrapper<VrsTrialPosition> toWxappQueryWrapper() {
        VrsLoginUser loginUser = (VrsLoginUser) SpringUtil.getBean(IAuthContext.class).getLoginUser();
        QueryWrapper<VrsTrialPosition> queryWrapper = new QueryWrapper<>();
        // 查看范围：保安和管理员查看所有，访客和职员只能查看与自己相关
        if (!VrsConstants.VrsType.SECURITY_PERSON.equals(loginUser.getVrsType()) &&
            !VrsConstants.VrsType.ADMIN.equals(loginUser.getVrsType())) {
            queryWrapper.lambda().and(w -> w
                    .eq(VrsTrialPosition::getCreateBy, loginUser.getId()).or()
                    .eq(VrsTrialPosition::getPersonPhone, loginUser.getPhone()).or()
                    .eq(VrsTrialPosition::getRespPerson, loginUser.getUsername()).or());
        }
        // 关键字搜索
        if (StrUtil.isNotBlank(this.keyword)) {
            queryWrapper.lambda().and(w -> w
                    .like(VrsTrialPosition::getPersonName, this.keyword).or()
                    .like(VrsTrialPosition::getPersonPhone, this.keyword).or()
                    .like(VrsTrialPosition::getRespPerson, this.keyword).or());
        }
        queryWrapper.lambda().orderByDesc(VrsTrialPosition::getCreateTime);
        return queryWrapper;
    }


}
