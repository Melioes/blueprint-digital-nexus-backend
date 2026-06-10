package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.service.DashboardService;
import com.melioes.blueprintdigitalnexus.vo.DashboardTrendVO;
import com.melioes.blueprintdigitalnexus.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 首页看板控制器
 *
 * 两个接口：
 * 1. GET /dashboard          → 5个卡片 + 最近单据（缓存10秒）
 * 2. GET /dashboard/trend    → 趋势图数据（支持任意日期范围）
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "首页看板", description = "看板数据聚合接口")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取看板数据（5个卡片 + 最近单据）
     *
     * 缓存策略：Redis TTL 10秒，靠 TTL 自动过期，不需要 @CacheEvict
     */
    @GetMapping
    @Operation(summary = "获取看板数据", description = "返回5个卡片数据（含同比）+ 最近出入库单")
    @Cacheable(value = "WMS:DASHBOARD", key = "'all'")
    public Result<DashboardVO> getDashboard() {
        log.info("[接口] 获取看板数据");
        return Result.success(dashboardService.getDashboard());
    }

    /**
     * 获取趋势图数据（支持任意日期范围）
     *
     * 前端日期选择器选好日期后调用这个接口
     * 默认值：近7天
     *
     * @param startDate 开始日期（格式：2026-06-01）
     * @param endDate   结束日期（格式：2026-06-10）
     * @return 入库/出库趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据", description = "支持任意日期范围的出入库趋势查询")
    public Result<DashboardTrendVO> getTrend(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 默认值：近7天
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        // 参数校验
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        // 限制最大范围为 90 天，防止查询过慢
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > 90) {
            throw new BusinessException("查询范围不能超过90天");
        }

        log.info("[接口] 获取趋势数据: {} ~ {}", startDate, endDate);
        return Result.success(dashboardService.getTrend(startDate, endDate));
    }
}
