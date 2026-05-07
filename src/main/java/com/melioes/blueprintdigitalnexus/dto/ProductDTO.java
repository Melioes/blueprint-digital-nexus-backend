package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long productId;
    private String skuCode;     // SKU编码，唯一
    private String productName;
    private Long categoryId;    // 分类ID
    private String unit;        // 单位（件、盒、kg）
    private String spec;        // 规格
    private BigDecimal price;   // 价格
    private Integer publishStatus; // 0下架，1上架
    private String remark;      // 备注
}