package com.melioes.blueprintdigitalnexus.common.constant.wms;

/**
 * 出入库单状态常量
 * 用于入库单 / 出库单流程控制
 */
public class OrderStatusConstant {

    /** 草稿状态 */
    public static final Integer DRAFT = 0;

    /** 处理中 */
    public static final Integer PROCESSING = 1;

    /** 已完成 */
    public static final Integer FINISHED = 2;

    /** 已取消 */
    public static final Integer CANCELED = 3;
}