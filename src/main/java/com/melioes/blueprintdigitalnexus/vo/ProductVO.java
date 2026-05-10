package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象
 * 用于返回给前端展示的商品信息
 */
@Data
public class ProductVO {
    private Long productId;
    private String skuCode;
    private String productName;
    private Long categoryId;
    private String categoryName; // 分类名称，方便前端显示
    private String unit;
    private String spec;
    private String barcode;     // 条码
    private BigDecimal price;
    private BigDecimal weight;  // 重量(kg)
    private BigDecimal volume;  // 体积(m³)
    private Integer publishStatus;
    private String publishStatusName; // 状态名称（已上架/已下架）
    private String remark;      // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}