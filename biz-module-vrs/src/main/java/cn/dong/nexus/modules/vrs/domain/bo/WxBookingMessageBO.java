package cn.dong.nexus.modules.vrs.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WxBookingMessageBO {

    private String touser;
    private String template_id;
    private String page = "pages/records/records";
    private Data data;


    @lombok.Data
    public static class Data {
        private Value name5;
        private Value thing29;
        private Value phone_number18;
        private Value time10;
        private Value phrase32;


        @lombok.Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Value {
            private String value;
        }
    }

}
