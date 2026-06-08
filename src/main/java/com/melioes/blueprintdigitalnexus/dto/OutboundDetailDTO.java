package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 出库单明细参数
 */
@Data
@NoArgsConstructor
public class OutboundDetailDTO {
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 出库数量
     */
    private Integer quantity;
}