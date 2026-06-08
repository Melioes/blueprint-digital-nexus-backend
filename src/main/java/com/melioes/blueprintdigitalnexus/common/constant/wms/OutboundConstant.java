package com.melioes.blueprintdigitalnexus.common.constant.wms;
/**
 * 出库模块常量
 */
public class OutboundConstant {
    // ==================== 状态常量 ====================
    // 和入库完全一样：0=草稿 1=已确认 2=已取消
    public static final Integer STATUS_DRAFT = 0;
    public static final Integer STATUS_CONFIRMED = 1;
    public static final Integer STATUS_CANCELLED = 2;

    // ==================== 状态名称 ====================
    public static final String STATUS_NAME_DRAFT = "草稿";
    public static final String STATUS_NAME_CONFIRMED = "已确认";
    public static final String STATUS_NAME_CANCELLED = "已取消";

    // ==================== 编码前缀 ====================
    // 入库单是 "IN"，出库单用 "OUT"
    public static final String ORDER_NO_PREFIX = "OUT";

    // ==================== 错误信息 ====================
    public static final String OUTBOUND_NOT_FOUND = "出库单不存在";
    public static final String INVALID_STATUS_FOR_CONFIRM = "只有草稿状态可以确认";
    public static final String INVALID_STATUS_FOR_UPDATE = "只有草稿状态可以修改";
    public static final String INVALID_STATUS_FOR_DELETE = "只有草稿状态可以删除";
    public static final String INVALID_STATUS_FOR_CANCEL = "只有已确认状态可以取消";
    public static final String INVALID_QUANTITY = "出库数量必须大于0";
    public static final String DETAILS_REQUIRED = "出库单明细不能为空";
    public static final String ORDER_ID_REQUIRED = "出库单ID不能为空";
    public static final String PRODUCT_PARTIAL_NOT_FOUND = "部分商品不存在";
    public static final String WAREHOUSE_ID_REQUIRED = "仓库ID不能为空";

    // ==================== 库存调整原因模板 ====================
    public static final String OUTBOUND_CONFIRM_REASON = "出库单确认: ";
    public static final String OUTBOUND_CANCEL_REASON = "出库单取消: ";
}
