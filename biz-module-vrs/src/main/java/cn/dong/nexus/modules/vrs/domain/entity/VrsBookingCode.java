package cn.dong.nexus.modules.vrs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vrs_booking_code")
public class VrsBookingCode {
    private String id;

    private String url;

    private Integer used;

    private String vrsBookingId;

    private LocalDateTime usedTime;
}
