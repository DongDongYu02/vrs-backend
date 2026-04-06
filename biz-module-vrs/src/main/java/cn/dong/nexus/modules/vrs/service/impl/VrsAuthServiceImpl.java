package cn.dong.nexus.modules.vrs.service.impl;

import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.enums.Client;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.bo.EkpUserBO;
import cn.dong.nexus.modules.vrs.domain.bo.VrsLoginUser;
import cn.dong.nexus.modules.vrs.domain.dto.VrsLoginDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsUser;
import cn.dong.nexus.modules.vrs.service.EkpCommonService;
import cn.dong.nexus.modules.vrs.service.IVrsAuthService;
import cn.dong.nexus.modules.vrs.service.IVrsUserService;
import cn.dong.nexus.modules.vrs.service.WechatService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VrsAuthServiceImpl implements IVrsAuthService {
    private final AppProperties appProperties;
    private final EkpCommonService ekpCommonService;
    private final IAuthContext authContext;
    private final IVrsUserService vrsUserService;
    private final WechatService wechatService;

    @Override
    public LoginUserVO login(VrsLoginDTO dto) {
        String openid = this.getWxOpenId(dto.getJsCode());
        String phone = this.getUserPhoneNumber(dto.getCode());
        if (StrUtil.hasBlank(phone, openid)) {
            log.error("获取微信用户信息失败，openid：{}，ophone：{}", openid, phone);
            throw new BizException("授权登录失败，请稍后重试！");
        }
        // 从 EKP 查询用户
        EkpUserBO ekpUser = ekpCommonService.getEkpUserInfoByPhone(phone);
        // 职员用户
        if (Objects.nonNull(ekpUser)) {
            VrsLoginUser loginUser = new VrsLoginUser();
            loginUser.setId(ekpUser.getId());
            loginUser.setVrsType(VrsConstants.VrsType.EMPLOYEE);
            loginUser.setUsername(ekpUser.getName());
            loginUser.setPhone(phone);
            loginUser.setOpenid(openid);
            // 如果本系统存在用户 则覆盖权限
            VrsUser vrsUser = vrsUserService.lambdaQuery().eq(VrsUser::getPhone, phone).one();
            if (Objects.nonNull(vrsUser)) {
                loginUser.setVrsType(vrsUser.getVrsType());
            }
            authContext.login(loginUser, Client.VRS);
            return new LoginUserVO(authContext.getToken(), loginUser);
        }
        // 访客用户
        VrsUser vrsUser = vrsUserService.lambdaQuery().eq(VrsUser::getPhone, phone).one();
        if (Objects.isNull(vrsUser)) {
            // 首次登录，记录用户
            vrsUser = new VrsUser(phone, VrsConstants.VrsType.VISITOR);
            vrsUserService.save(vrsUser);
        }
        // 获取匿名 保安还是访客
        String username = VrsConstants.VrsType.SECURITY_PERSON.equals(vrsUser.getVrsType()) ?
                VrsConstants.VrsTypeText.SECURITY_PERSON : VrsConstants.VrsTypeText.VISITOR;
        VrsLoginUser loginUser = new VrsLoginUser();
        loginUser.setId(vrsUser.getId());
        loginUser.setVrsType(vrsUser.getVrsType());
        loginUser.setUsername(username);
        loginUser.setPhone(phone);
        loginUser.setOpenid(openid);
        authContext.login(loginUser, Client.VRS);
        return new LoginUserVO(authContext.getToken(), loginUser);
    }

    private String getWxOpenId(String jsCode) {
        JSONObject params = new JSONObject();
        params.set("grant_type", "client_credential");
        params.set("appid", appProperties.getVrs().getAppId());
        params.set("secret", appProperties.getVrs().getAppSecret());
        params.set("js_code", jsCode);
        try {
            String resp = HttpUtil.get(ApiConstants.Wx.GET_SESSION_INFO, params);
            JSONObject json = JSONUtil.parseObj(resp);
            return json.getStr("openid");
        } catch (Exception e) {
            log.error("获取微信用户Session失败：{}", e.getMessage());
            throw new BizException("授权登录失败，请稍后重试！");
        }
    }


    private String getUserPhoneNumber(String code) {
        String accessToken = wechatService.getAccessToken();
        String url = StrUtil.format(ApiConstants.Wx.GET_USER_PHONE_NUMBER + "?access_token={}", accessToken);
        JSONObject body = new JSONObject();
        body.set("code", code);
        String resp = null;
        try {
            resp = HttpUtil.post(url, JSONUtil.toJsonStr(body));
            JSONObject json = JSONUtil.parseObj(resp);
            return json.getJSONObject("phone_info").getStr("purePhoneNumber");
        } catch (Exception e) {
            log.error("获取微信用户手机号失败：{}，resp:{}", e.getMessage(), resp);
            throw new BizException("授权登录失败，请稍后重试！");
        }
    }


}
