package cn.dong.nexus.common.constants;

public interface ApiConstants {

    /**
     * 发起蓝凌流程 API
     */
    String INITIATE_EKP_REVIEW = "/api/km-review/kmReviewRestService/addReview";

    /**
     * 审批蓝凌流程 API
     */
    String APPROVE_EKP_REVIEW = "/api/km-review/kmReviewRestService/updateReviewInfo";

    interface Wx {

        String GET_SESSION_INFO = "https://api.weixin.qq.com/sns/jscode2session";

        String GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";

        String GET_USER_PHONE_NUMBER = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

        String SEND_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    }
}
