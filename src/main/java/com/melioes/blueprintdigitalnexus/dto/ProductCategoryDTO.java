package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;

@Data
public class ProductCategoryDTO {
    private Long categoryId;   // 修改时需要
    private String categoryName;
    private String categoryCode;
    private Long parentId;
    private Integer sort;
    private Integer status;
}