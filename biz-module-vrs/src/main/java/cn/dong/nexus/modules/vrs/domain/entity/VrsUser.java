package cn.dong.nexus.modules.vrs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("vrs_user")
@NoArgsConstructor
@AllArgsConstructor
public class VrsUser {
    private String id;

    private String phone;

    private Integer vrsType;

    public VrsUser(String phone, Integer vrsType) {
        this.phone = phone;
        this.vrsType = vrsType;
    }
}
