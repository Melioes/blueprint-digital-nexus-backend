package com.melioes.blueprintdigitalnexus.common.constant.wms;

/**
 * 商品状态常量
 * 控制商品是否上架/下架
 */
public class ProductConstant {

    /** 已上架 */
    public static final Integer PUBLISH = 1;

    /** 已下架 */
    public static final Integer UNPUBLISH = 0;

    /** 业务提示语：SKU已存在 */
    public static final String SKU_ALREADY_EXISTS = "操作失败：商品SKU编码 [%s] 已存在，请检查";

    /** 业务提示语：商品不存在 */
    public static final String PRODUCT_NOT_FOUND = "错误：未找到指定商品信息";

    // --- 分类相关提示语
    public static final String CATEGORY_NOT_FOUND = "错误：分类不存在";
    public static final String CATEGORY_PARENT_NOT_FOUND = "错误：父级分类不存在";
    public static final String CATEGORY_SELF_REFERENCING = "非法操作：父分类不能是自己";
    public static final String CATEGORY_HAS_CHILDREN = "操作拦截：该分类下还存在子分类，请先删除子分类";
    public static final String CATEGORY_HAS_PRODUCTS = "操作拦截：该分类下已关联商品，无法删除";
}