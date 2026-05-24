package com.melioes.blueprintdigitalnexus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 商品分类DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDTO {
    private Long categoryId;   // 修改时需要
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String categoryName;
    private String categoryCode;
    private Long parentId;
    private Integer sort;
    private Integer status;
}