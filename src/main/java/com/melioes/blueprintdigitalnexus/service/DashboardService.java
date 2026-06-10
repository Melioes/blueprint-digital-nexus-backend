package com.melioes.blueprintdigitalnexus.service;

import com.melioes.blueprintdigitalnexus.vo.DashboardTrendVO;
import com.melioes.blueprintdigitalnexus.vo.DashboardVO;

import java.time.LocalDate;

/**
 * 首页看板 Service 接口
 */
public interface DashboardService {

    /**
     * 获取看板数据（5个卡片 + 最近单据）
     *
     * @return 看板数据
     */
    DashboardVO getDashboard();

    /**
     * 获取趋势数据（支持任意日期范围）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 入库/出库趋势数据
     */
    DashboardTrendVO getTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取库存预警阈值
     *
     * @return 阈值（库存低于此值视为低库存）
     */
    int getLowStockThreshold();
}
