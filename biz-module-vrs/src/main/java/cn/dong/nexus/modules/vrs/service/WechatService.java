package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.dong.nexus.modules.vrs.constants.VrsConstants;
import cn.dong.nexus.modules.vrs.domain.bo.WxBookingMessageBO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WechatService {
    private final AppProperties appProperties;

    /**
     * 发送预约状态消息
     */
    @Async
    public void sendBooingStatusMessage(VrsBooking booking) {
        log.info("发送审批通知");

        String statusText = "未知";
        if (VrsConstants.VrsBookingStatus.APPROVED == booking.getStatus()) {
            statusText = "审批通过";
        } else if (VrsConstants.VrsBookingStatus.REJECTED == booking.getStatus()) {
            statusText = "审批驳回";
        }
        WxBookingMessageBO message = new WxBookingMessageBO();
        message.setTouser(booking.getCreatorOpenid());
        message.setTemplate_id(appProperties.getVrs().getWxappMsgTemplateId());
        WxBookingMessageBO.Data data = new WxBookingMessageBO.Data();
        data.setName5(new WxBookingMessageBO.Data.Value(booking.getVisitorName()));
        data.setThing29(new WxBookingMessageBO.Data.Value(booking.getReceptionistName()));
        data.setPhone_number18(new WxBookingMessageBO.Data.Value(booking.getReceptionistContact()));
        data.setTime10(new WxBookingMessageBO.Data.Value(LocalDateTimeUtil.format(booking.getVisitingTime(), GlobalConstants.DatePattern.Y_M_D_H_M)));
        data.setPhrase32(new WxBookingMessageBO.Data.Value(statusText));
        message.setData(data);
        String accessToken = this.getAccessToken();
        String url = StrUtil.format(ApiConstants.Wx.SEND_MESSAGE + "?access_token={}", accessToken);
        try {
            String jsonStr = JSONUtil.toJsonStr(message);
            String resp = HttpUtil.post(url, jsonStr);
            JSONObject json = JSONUtil.parseObj(resp);
            if (json.getInt("errcode") != 0) {
                log.error("访客预约审批微信消息发送失败：{}", resp);
            }
        } catch (Exception e) {
            log.error("访客预约审批微信消息发送失败：{}", e.getMessage());
        }
    }


    public String getAccessToken() {
        String cache = RedisUtil.getStr(GlobalConstants.CacheKey.WX_ACCESS_TOKEN);
        if (StrUtil.isNotBlank(cache)) {
            return cache;
        }
        JSONObject param = new JSONObject();
        param.set("grant_type", "client_credential");
        param.set("appid", appProperties.getVrs().getAppId());
        param.set("secret", appProperties.getVrs().getAppSecret());
        try {
            String resp = HttpUtil.get(ApiConstants.Wx.GET_ACCESS_TOKEN, param);
            JSONObject json = JSONUtil.parseObj(resp);
            String accessToken = json.getStr("access_token");
            RedisUtil.set(GlobalConstants.CacheKey.WX_ACCESS_TOKEN, accessToken, 7000, TimeUnit.SECONDS);
            return accessToken;
        } catch (Exception e) {
            log.error("获取微信AccessToken：{}", e.getMessage());
            throw new BizException("授权登录失败，请稍后重试！");
        }
    }
}
