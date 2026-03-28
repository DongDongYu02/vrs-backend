package cn.dong.nexus.core.security.enums;

import lombok.Getter;

@Getter
public enum Client {

    /**
     * 管理后台
     */
    ADMIN("admin"),
    /**
     * 移动端
     */
    APP("app"),
    /**
     * VRS 访客预约系统
     */
    VRS("VRS");

    private final String code;

    Client(String code) {
        this.code = code;
    }
}
