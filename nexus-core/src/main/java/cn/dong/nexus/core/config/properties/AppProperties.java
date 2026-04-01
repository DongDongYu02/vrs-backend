package cn.dong.nexus.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Ekp ekp;
    private Vrs vrs;
    private String FileUploadPath;
    private String aesKey;

    @Data
    public static class Ekp {
        private String serverUrl;
        private String vrsReviewTemplateId;
        private String vrsTrialPositionTemplateId;
        private Review review;

        @Data
        public static class Review {

            private BookingField bookingField;

            @Data
            public static class BookingField {
                private String interviewee;
                private String visitorName;
                private String visitorContact;
                private String visitorCompany;
                private String receptionArea;
                private String receptionDept;
                private String receptionistName;
                private String receptionistContact;
                private String visitingTime;
                private String visitingReason;
                private String loginName;
                private String actualVisitTime;
            }

        }
    }

    @Data
    public static class Vrs {
        private String appId;
        private String appSecret;
        private String wxappMsgTemplateId;
    }


}