package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.OutboundDetailDTO;
import com.melioes.blueprintdigitalnexus.entity.OutboundDetail;
import com.melioes.blueprintdigitalnexus.vo.OutboundDetailVO;

import java.util.List;

/**
 * 出库单明细服务接口
 */

public interface OutboundDetailService extends IService<OutboundDetail> {

    /**
     * 根据出库单ID查询明细（包含商品信息）
     */
    List<OutboundDetailVO> listByOrderId(Long orderId);

    /**
     * 批量新增明细
     */
    void saveBatch(Long orderId, List<OutboundDetailDTO> details);

    /**
     * 根据出库单ID删除明细
     */
    void removeByOrderId(Long orderId);
}