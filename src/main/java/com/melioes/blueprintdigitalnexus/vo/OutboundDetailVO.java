package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 出库单明细视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDetailVO {
    /**
     * 出库单明细ID
     */
    private Long outboundDetailId;
    /**
     * 出库单ID
     */
    private Long outboundOrderId;
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
     * 出库数量
     */
    private Integer quantity;
}