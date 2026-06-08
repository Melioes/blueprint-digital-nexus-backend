package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入库单明细参数
 */
@Data
@NoArgsConstructor
public class InboundDetailDTO {
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 入库数量
     */
    private Integer quantity;
}