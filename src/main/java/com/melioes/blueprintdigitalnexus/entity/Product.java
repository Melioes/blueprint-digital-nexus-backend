package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类[cite: 1, 4]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wms_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long productId;

    private String skuCode;     // SKU编码
    private String productName; // 商品名称
    private Long categoryId;    // 分类ID
    private String unit;        // 单位
    private String spec;        // 规格
    private String barcode;     // 条码
    private BigDecimal price;   // 价格
    private BigDecimal weight;  // 重量
    private BigDecimal volume;  // 体积
    private Integer publishStatus; // 0下架，1上架
    private String remark;      // 备注


    @TableLogic
    private Integer isDeleted;  // 逻辑删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 商品图片路径
     */
    private String imageUrl;


}