package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.util.List;
/**
 * 菜单VO
 */
@Data
public class ProductCategoryVO {
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private Long parentId;
    private Integer sort;
    private Integer status;
    // 关键：存放子分类的列表
    private List<ProductCategoryVO> children;
}
