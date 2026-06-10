package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页看板数据
 *
 * 设计说明：
 * - 5个卡片带同比数据（较昨日变化量）
 * - 趋势图拆到独立接口 /dashboard/trend（支持任意日期范围）
 * - 最近出入库单表格（最近5条）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    // ==================== 顶部5个卡片（带同比） ====================

    /**
     * 商品总数
     */
    private Integer productCount;

    /**
     * 库存总数（所有仓库所有商品的库存之和）
     */
    private Integer totalStock;

    /**
     * 今日入库数量（已确认的入库单明细数量之和）
     */
    private Integer todayInbound;

    /**
     * 今日入库较昨日变化量（正数=增长，负数=下降）
     */
    private Integer todayInboundChange;

    /**
     * 今日出库数量（已确认的出库单明细数量之和）
     */
    private Integer todayOutbound;

    /**
     * 今日出库较昨日变化量
     */
    private Integer todayOutboundChange;

    /**
     * 库存预警数量（库存低于阈值的商品数）
     */
    private Integer lowStockCount;

    // ==================== 最近出入库单 ====================

    /**
     * 最近入库单（最近5条）
     */
    private List<RecentOrderVO> recentInboundOrders;

    /**
     * 最近出库单（最近5条）
     */
    private List<RecentOrderVO> recentOutboundOrders;

    // ==================== 内部类：最近单据 ====================

    /**
     * 最近单据（出入库通用）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderVO {
        /** 单据ID */
        private Long orderId;
        /** 单号 */
        private String orderNo;
        /** 仓库名称 */
        private String warehouseName;
        /** 状态：0=草稿，1=已确认，2=已取消 */
        private Integer status;
        /** 状态名称 */
        private String statusName;
        /** 创建时间（格式：2026-06-10 16:30） */
        private String createTime;
    }
}
