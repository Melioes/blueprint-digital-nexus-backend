package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.InboundDetailDTO;
import com.melioes.blueprintdigitalnexus.entity.InboundDetail;
import com.melioes.blueprintdigitalnexus.vo.InboundDetailVO;

import java.util.List;

/**
 * 入库单明细服务接口
 */
public interface InboundDetailService extends IService<InboundDetail> {

    /**
     * 根据入库单ID查询明细（包含商品信息）
     */
    List<InboundDetailVO> listByOrderId(Long orderId);

    /**
     * 批量新增明细
     */
    void saveBatch(Long orderId, List<InboundDetailDTO> details);

    /**
     * 根据入库单ID删除明细
     */
    void removeByOrderId(Long orderId);
}