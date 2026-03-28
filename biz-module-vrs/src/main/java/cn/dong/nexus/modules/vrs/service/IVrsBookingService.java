package cn.dong.nexus.modules.vrs.service;

import cn.dong.nexus.modules.vrs.domain.dto.VrsBookingDTO;
import cn.dong.nexus.modules.vrs.domain.dto.VrsUpdateBookingStatusDTO;
import cn.dong.nexus.modules.vrs.domain.entity.VrsBooking;
import cn.dong.nexus.modules.vrs.domain.query.VrsBookingQuery;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingCodeVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingDetailVO;
import cn.dong.nexus.modules.vrs.domain.vo.VrsBookingVO;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IVrsBookingService extends IService<VrsBooking> {
    /**
     * 创建邀请信息
     */
    String createShareInfo(JSONObject body);

    /**
     * 获取邀请信息
     */
    JSONObject getShareInfo(String shareId);

    /**
     * 新增预约
     */
    void create(VrsBookingDTO dto);

    /**
     * 获取预约列表
     */
    List<VrsBookingVO> getList(VrsBookingQuery query);

    /**
     * 获取预约详情
     */
    VrsBookingDetailVO getDetailById(String id);

    /**
     * 更新预约状态
     */
    void updateStatus(VrsUpdateBookingStatusDTO dto);

    /**
     * 获取访客码
     */
    VrsBookingCodeVO getBookingCode(String id);

    /**
     * 获取访客码是否使用
     */
    Integer getCodeUsed(String codeId);

    /**
     * 访客码核销
     */
    void codeWriteOff(String codeId);

    /**
     * 获取最新可以访客码
     */
    VrsBookingCodeDetailVO getLatestCode();

}
