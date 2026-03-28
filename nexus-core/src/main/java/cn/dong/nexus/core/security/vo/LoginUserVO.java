package cn.dong.nexus.core.security.vo;

import cn.dong.nexus.core.security.context.LoginUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserVO {

    @Schema(description = "Token")
    private String token;

    @Schema(description = "用户信息")
    private LoginUser userInfo;

//    @Data
//    public static class LoginUser {
//
//        @Schema(description = "用户 ID")
//        private String id;
//
//        @Schema(description = "用户名")
//        private String username;
//
//        @Schema(description = "头像")
//        private String avatar;
//
//        @Schema(description = "手机号")
//        private String phone;
//
//        @Schema(description = "昵称")
//        private String nickname;
//
//        @Schema(description = "客户端")
//        private String client;
//
//        @Schema(description = "身份")
//        private Integer identity;
//
//        @Schema(description = "拓展信息")
//        private Map<String, Object> extInfo;
//
//    }
}
