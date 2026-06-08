package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundDetailVO {
    /**
     * 入库单明细ID
     */
    private Long inboundDetailId;
    /**
     * 入库单ID
     */
    private Long inboundOrderId;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 商品名称
     */
    private String productName; // 关联字段：商品名称
    /**
     * 商品编码
     */
    private String skuCode; // 关联字段：商品编码
    /**
     * 入库数量
     */
    private Integer quantity;
}