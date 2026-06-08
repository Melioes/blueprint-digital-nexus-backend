package com.melioes.blueprintdigitalnexus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品数据传输对象
 * 用于接收前端新增/修改商品的请求参数
 */
@Data
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
    private String skuCode; // SKU编码，唯一
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    private String productName; // 商品名称
    private Long categoryId; // 分类ID
    private String unit; // 单位（件、盒、kg）
    private String spec; // 规格
    private String barcode; // 条码
    private BigDecimal price; // 价格
    private BigDecimal weight; // 重量(kg)
    private BigDecimal volume; // 体积(m³)
    private Integer publishStatus; // 0下架，1上架
    private String remark; // 备注
}