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
    }

    @Data
    public static class Vrs {
        private String appId;
        private String appSecret;
        private String wxappMsgTemplateId;
    }
}