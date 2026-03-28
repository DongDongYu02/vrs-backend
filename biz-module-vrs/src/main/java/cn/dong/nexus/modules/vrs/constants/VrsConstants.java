package cn.dong.nexus.modules.vrs.constants;

public interface VrsConstants {
    interface VrsType {
        Integer EMPLOYEE = 1;
        Integer VISITOR = 2;
        Integer SECURITY_PERSON = 3;
        Integer ADMIN = 4;
    }

    interface VrsTypeText {
        String SECURITY_PERSON = "保安";
        String VISITOR = "访客";
        String ADMIN = "管理员";
    }

    interface VrsBookingStatus {
        int PENDING = 0;
        int APPROVED = 1;
        int REJECTED = 2;
        int CANCELED = 3;
        int VISITED = 4;
    }

    interface VrsBookingCodeStatus {
        int NOT_YET = 0;
        int NORMAL = 1;
        int EXPIRED = 2;
    }
}
