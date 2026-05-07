package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long productId;
    private String skuCode;
    private String productName;
    private Long categoryId;
    private String categoryName; // 额外增加：分类名称，方便前端显示
    private String unit;
    private String spec;
    private BigDecimal price;
    private Integer publishStatus;
    private LocalDateTime createTime;
}