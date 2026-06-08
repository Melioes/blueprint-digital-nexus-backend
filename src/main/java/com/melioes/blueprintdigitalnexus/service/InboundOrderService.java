package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import com.melioes.blueprintdigitalnexus.dto.InboundOrderDTO;
import com.melioes.blueprintdigitalnexus.entity.InboundOrder;
import com.melioes.blueprintdigitalnexus.query.InboundOrderQuery;
import com.melioes.blueprintdigitalnexus.vo.InboundOrderVO;
/**
 * 入库单服务接口
 */
public interface InboundOrderService extends IService<InboundOrder>, SequenceSyncService {

    /**
     * 分页查询入库单
     */
    IPage<InboundOrderVO> getInboundPage(InboundOrderQuery query);

    /**
     * 查询入库单详情
     */
    InboundOrderVO getInboundById(Long id);

    /**
     * 创建入库单
     */
    void addInbound(InboundOrderDTO dto);

    /**
     * 修改入库单
     */
    void updateInbound(InboundOrderDTO dto);

    /**
     * 删除入库单
     */
    void deleteInbound(Long id);

    /**
     * 确认入库
     */
    void confirmInbound(Long id);

    /**
     * 取消入库
     */
    void cancelInbound(Long id);

    /**
     * 检查入库单是否存在
     */
    InboundOrder getAndCheckInbound(Long id);
}