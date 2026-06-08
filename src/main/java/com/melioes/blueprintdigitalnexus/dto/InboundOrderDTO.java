package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 新增/修改入库单参数
 */
@Data
@NoArgsConstructor
public class InboundOrderDTO {
    /**
     * 入库单ID  修改时用
     */
    private Long inboundOrderId;
    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 备注
     */
    private String remark;
    /**
     * 入库单明细列表
     */
    private List<InboundDetailDTO> details; // 商品明细列表
}