package com.melioes.blueprintdigitalnexus.common.constant.wms;

/**
 * 库存常量类
 */
public class InventoryConstant {
    // 错误信息
    public static final String INVENTORY_NOT_FOUND = "库存记录不存在";
    public static final String INVENTORY_ALREADY_EXISTS = "该商品在该仓库已有库存记录";
    public static final String STOCK_NOT_ENOUGH = "库存不足";
    public static final String STOCK_NOT_POSITIVE = "库存数量不能为负数";
    public static final String WAREHOUSE_AND_PRODUCT_ID_REQUIRED = "仓库ID和商品ID不能为空";
    public static final String ADJUST_QUANTITY_REQUIRED = "调整数量不能为空";

    // 调整类型
    public static final String ADJUST_TYPE_IN = "IN";
    public static final String ADJUST_TYPE_OUT = "OUT";

    // 业务默认值
    public static final Integer DEFAULT_LOCKED_STOCK = 0;
    public static final Integer DEFAULT_VERSION = 0;
}
