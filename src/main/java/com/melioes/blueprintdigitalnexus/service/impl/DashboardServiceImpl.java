package com.melioes.blueprintdigitalnexus.service.impl;

import com.melioes.blueprintdigitalnexus.entity.*;
import com.melioes.blueprintdigitalnexus.service.*;
import com.melioes.blueprintdigitalnexus.vo.DashboardTrendVO;
import com.melioes.blueprintdigitalnexus.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页看板 Service 实现
 *
 * 功能清单：
 * 1. 5个卡片（商品总数、库存总数、今日入库、今日出库、库存预警）
 * 2. 卡片同比数据（较昨日变化量）
 * 3. 库存预警阈值从 sys_config 表读取（可配置）
 * 4. 最近出入库单（最近5条）
 * 5. 趋势图（支持任意日期范围，独立接口）
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InboundOrderService inboundOrderService;
    @Autowired
    private InboundDetailService inboundDetailService;
    @Autowired
    private OutboundOrderService outboundOrderService;
    @Autowired
    private OutboundDetailService outboundDetailService;
    @Autowired
    private StockLogService stockLogService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private SysConfigService sysConfigService;

    /** 配置 key：库存预警阈值 */
    private static final String CONFIG_LOW_STOCK_THRESHOLD = "low_stock_threshold";
    /** 配置默认值：库存 < 10 视为低库存 */
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /** 最近单据数量 */
    private static final int RECENT_ORDER_LIMIT = 5;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ==================== 看板主接口（5个卡片 + 最近单据） ====================

    @Override
    public DashboardVO getDashboard() {
        log.info("[看板] 开始聚合查询");
        long start = System.currentTimeMillis();

        DashboardVO vo = new DashboardVO();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 1. 商品总数（无同比，商品数量不会每日大幅变化）
        vo.setProductCount(getProductCount());

        // 2. 库存总数
        vo.setTotalStock(getTotalStock());

        // 3. 今日入库 + 同比
        int todayInbound = getOrderDetailSum(today, today, true);
        int yesterdayInbound = getOrderDetailSum(yesterday, yesterday, true);
        vo.setTodayInbound(todayInbound);
        vo.setTodayInboundChange(todayInbound - yesterdayInbound);

        // 4. 今日出库 + 同比
        int todayOutbound = getOrderDetailSum(today, today, false);
        int yesterdayOutbound = getOrderDetailSum(yesterday, yesterday, false);
        vo.setTodayOutbound(todayOutbound);
        vo.setTodayOutboundChange(todayOutbound - yesterdayOutbound);

        // 5. 库存预警（阈值从配置表读取）
        int threshold = getLowStockThreshold();
        vo.setLowStockCount(getLowStockCount(threshold));

        // 6. 最近入库单
        vo.setRecentInboundOrders(getRecentOrders(true));

        // 7. 最近出库单
        vo.setRecentOutboundOrders(getRecentOrders(false));

        long cost = System.currentTimeMillis() - start;
        log.info("[看板] 聚合查询完成，耗时{}ms", cost);

        return vo;
    }

    // ==================== 趋势图接口（支持任意日期范围） ====================

    @Override
    public DashboardTrendVO getTrend(LocalDate startDate, LocalDate endDate) {
        log.info("[看板] 趋势查询: {} ~ {}", startDate, endDate);

        // 计算天数
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 入库趋势
        List<DashboardTrendVO.TrendItem> inboundTrend = getTrendItems("IN", startDate, (int) days);

        // 出库趋势
        List<DashboardTrendVO.TrendItem> outboundTrend = getTrendItems("OUT", startDate, (int) days);

        return new DashboardTrendVO(inboundTrend, outboundTrend);
    }

    @Override
    public int getLowStockThreshold() {
        return sysConfigService.getIntValue(CONFIG_LOW_STOCK_THRESHOLD, DEFAULT_LOW_STOCK_THRESHOLD);
    }

    // ==================== 私有方法：卡片数据 ====================

    /** 商品总数 */
    private Integer getProductCount() {
        return Math.toIntExact(productService.count());
    }

    /** 库存总数 */
    private Integer getTotalStock() {
        return inventoryService.list().stream()
                .mapToInt(inv -> inv.getTotalStock() == null ? 0 : inv.getTotalStock())
                .sum();
    }

    /**
     * 查询指定日期范围的出入库明细数量之和
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param isInbound true=入库，false=出库
     * @return 数量之和
     */
    private int getOrderDetailSum(LocalDate startDate, LocalDate endDate, boolean isInbound) {
        if (isInbound) {
            List<InboundOrder> orders = inboundOrderService.lambdaQuery()
                    .eq(InboundOrder::getStatus, 1)
                    .ge(InboundOrder::getCreateTime, startDate.atStartOfDay())
                    .lt(InboundOrder::getCreateTime, endDate.plusDays(1).atStartOfDay())
                    .list();
            if (orders.isEmpty()) return 0;

            Set<Long> orderIds = orders.stream()
                    .map(InboundOrder::getInboundOrderId)
                    .collect(Collectors.toSet());

            return inboundDetailService.lambdaQuery()
                    .in(InboundDetail::getInboundOrderId, orderIds)
                    .list().stream()
                    .mapToInt(d -> d.getQuantity() == null ? 0 : d.getQuantity())
                    .sum();
        } else {
            List<OutboundOrder> orders = outboundOrderService.lambdaQuery()
                    .eq(OutboundOrder::getStatus, 1)
                    .ge(OutboundOrder::getCreateTime, startDate.atStartOfDay())
                    .lt(OutboundOrder::getCreateTime, endDate.plusDays(1).atStartOfDay())
                    .list();
            if (orders.isEmpty()) return 0;

            Set<Long> orderIds = orders.stream()
                    .map(OutboundOrder::getOutboundOrderId)
                    .collect(Collectors.toSet());

            return outboundDetailService.lambdaQuery()
                    .in(OutboundDetail::getOutboundOrderId, orderIds)
                    .list().stream()
                    .mapToInt(d -> d.getQuantity() == null ? 0 : d.getQuantity())
                    .sum();
        }
    }

    /** 库存预警数量 */
    private Integer getLowStockCount(int threshold) {
        return Math.toIntExact(inventoryService.lambdaQuery()
                .lt(Inventory::getTotalStock, threshold)
                .count());
    }

    // ==================== 私有方法：最近单据 ====================

    /**
     * 获取最近5条出入库单
     *
     * @param isInbound true=入库单，false=出库单
     * @return 最近单据列表
     */
    private List<DashboardVO.RecentOrderVO> getRecentOrders(boolean isInbound) {
        if (isInbound) {
            List<InboundOrder> orders = inboundOrderService.lambdaQuery()
                    .orderByDesc(InboundOrder::getCreateTime)
                    .last("LIMIT " + RECENT_ORDER_LIMIT)
                    .list();

            return orders.stream().map(order -> {
                DashboardVO.RecentOrderVO vo = new DashboardVO.RecentOrderVO();
                vo.setOrderId(order.getInboundOrderId());
                vo.setOrderNo(order.getOrderNo());
                vo.setStatus(order.getStatus());
                vo.setStatusName(getStatusName(order.getStatus()));
                vo.setCreateTime(order.getCreateTime() != null
                        ? order.getCreateTime().format(DATETIME_FORMATTER) : "");
                // 关联仓库名称
                Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
                vo.setWarehouseName(warehouse != null ? warehouse.getWarehouseName() : "");
                return vo;
            }).collect(Collectors.toList());
        } else {
            List<OutboundOrder> orders = outboundOrderService.lambdaQuery()
                    .orderByDesc(OutboundOrder::getCreateTime)
                    .last("LIMIT " + RECENT_ORDER_LIMIT)
                    .list();

            return orders.stream().map(order -> {
                DashboardVO.RecentOrderVO vo = new DashboardVO.RecentOrderVO();
                vo.setOrderId(order.getOutboundOrderId());
                vo.setOrderNo(order.getOrderNo());
                vo.setStatus(order.getStatus());
                vo.setStatusName(getStatusName(order.getStatus()));
                vo.setCreateTime(order.getCreateTime() != null
                        ? order.getCreateTime().format(DATETIME_FORMATTER) : "");
                // 关联仓库名称
                Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
                vo.setWarehouseName(warehouse != null ? warehouse.getWarehouseName() : "");
                return vo;
            }).collect(Collectors.toList());
        }
    }

    /** 状态码 → 状态名称 */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已确认";
            case 2 -> "已取消";
            default -> "未知";
        };
    }

    // ==================== 私有方法：趋势图 ====================

    /**
     * 获取指定日期范围的趋势数据
     *
     * @param type      变动类型：IN / OUT
     * @param startDate 开始日期
     * @param days      天数
     * @return 趋势数据列表
     */
    private List<DashboardTrendVO.TrendItem> getTrendItems(String type, LocalDate startDate, int days) {
        LocalDate endDate = startDate.plusDays(days - 1);

        // 查询变动日志
        List<StockLog> logs = stockLogService.lambdaQuery()
                .eq(StockLog::getType, type)
                .ge(StockLog::getCreateTime, startDate.atStartOfDay())
                .lt(StockLog::getCreateTime, endDate.plusDays(1).atStartOfDay())
                .list();

        // 按日期分组求和
        Map<String, Integer> dailyMap = new LinkedHashMap<>();
        for (StockLog logEntry : logs) {
            if (logEntry.getCreateTime() == null || logEntry.getChangeQty() == null) continue;
            String dateKey = logEntry.getCreateTime().toLocalDate().format(DATE_FORMATTER);
            dailyMap.merge(dateKey, Math.abs(logEntry.getChangeQty()), Integer::sum);
        }

        // 补齐没有数据的日期（填0）
        List<DashboardTrendVO.TrendItem> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String dateKey = startDate.plusDays(i).format(DATE_FORMATTER);
            int quantity = dailyMap.getOrDefault(dateKey, 0);
            result.add(new DashboardTrendVO.TrendItem(dateKey, quantity));
        }

        return result;
    }
}
