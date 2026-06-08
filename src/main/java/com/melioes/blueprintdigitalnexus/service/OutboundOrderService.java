package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import com.melioes.blueprintdigitalnexus.dto.OutboundOrderDTO;
import com.melioes.blueprintdigitalnexus.entity.OutboundOrder;
import com.melioes.blueprintdigitalnexus.query.OutboundOrderQuery;
import com.melioes.blueprintdigitalnexus.vo.OutboundOrderVO;

/**
 * 出库单服务接口
 */
public interface OutboundOrderService extends IService<OutboundOrder>, SequenceSyncService {

    /**
     * 分页查询出库单
     */
    IPage<OutboundOrderVO> getOutboundPage(OutboundOrderQuery query);

    /**
     * 查询出库单详情
     */
    OutboundOrderVO getOutboundById(Long id);

    /**
     * 创建出库单
     */
    void addOutbound(OutboundOrderDTO dto);

    /**
     * 修改出库单
     */
    void updateOutbound(OutboundOrderDTO dto);

    /**
     * 删除出库单
     */
    void deleteOutbound(Long id);

    /**
     * 确认出库
     */
    void confirmOutbound(Long id);

    /**
     * 取消出库
     */
    void cancelOutbound(Long id);

    /**
     * 检查出库单是否存在
     */
    OutboundOrder getAndCheckOutbound(Long id);
}