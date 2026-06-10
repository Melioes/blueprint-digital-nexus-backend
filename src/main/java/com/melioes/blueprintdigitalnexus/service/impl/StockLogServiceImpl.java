package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.entity.StockLog;
import com.melioes.blueprintdigitalnexus.entity.Warehouse;
import com.melioes.blueprintdigitalnexus.mapper.StockLogMapper;
import com.melioes.blueprintdigitalnexus.query.StockLogQuery;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.service.StockLogService;
import com.melioes.blueprintdigitalnexus.service.WarehouseService;
import com.melioes.blueprintdigitalnexus.vo.StockLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存变动日志 Service 实现
 *
 * 设计说明：
 * - 日志表是自包含快照，orderNo 在写入时已存好，查询时不需要关联业务表
 * - 只需关联 warehouse（仓库名）和 product（商品名），用批量预加载避免 N+1
 * - 不注入 InboundOrderService / OutboundOrderService，避免循环依赖
 */
@Slf4j
@Service
public class StockLogServiceImpl extends ServiceImpl<StockLogMapper, StockLog> implements StockLogService {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ProductService productService;

    /**
     * 分页查询库存变动日志
     */
    @Override
    public IPage<StockLogVO> getStockLogPage(StockLogQuery query) {
        log.info("[库存变动日志] 分页查询: pageNum={}, pageSize={}, warehouseId={}, productId={}, type={}",
                query.fetchPage(), query.fetchSize(), query.getWarehouseId(), query.getProductId(), query.getType());

        // 1. 构建查询条件
        LambdaQueryWrapper<StockLog> wrapper = buildQueryWrapper(query);

        // 2. 执行分页查询（按时间倒序）
        Page<StockLog> pageParam = new Page<>(query.fetchPage(), query.fetchSize());
        wrapper.orderByDesc(StockLog::getCreateTime);
        IPage<StockLog> pageResult = this.page(pageParam, wrapper);

        // 3. 无数据直接返回
        if (pageResult.getRecords() == null || pageResult.getRecords().isEmpty()) {
            return pageResult.convert(this::convertToBasicVO);
        }

        // 4. 批量预加载仓库和商品（两次 SQL，避免 N+1）
        Set<Long> warehouseIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();

        for (StockLog logEntry : pageResult.getRecords()) {
            if (logEntry.getWarehouseId() != null) warehouseIds.add(logEntry.getWarehouseId());
            if (logEntry.getProductId() != null) productIds.add(logEntry.getProductId());
        }

        Map<Long, Warehouse> warehouseMap = warehouseService.listByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, w -> w));

        Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // 5. 转换为 VO（orderNo 直接从实体取，不需要再查业务表）
        return pageResult.convert(logEntry -> convertToVO(logEntry, warehouseMap, productMap));
    }

    // ==================== 私有方法 ====================

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<StockLog> buildQueryWrapper(StockLogQuery query) {
        LambdaQueryWrapper<StockLog> wrapper = new LambdaQueryWrapper<>();

        if (query.getWarehouseId() != null) {
            wrapper.eq(StockLog::getWarehouseId, query.getWarehouseId());
        }
        if (query.getProductId() != null) {
            wrapper.eq(StockLog::getProductId, query.getProductId());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(StockLog::getType, query.getType());
        }

        return wrapper;
    }

    /**
     * 基础转换：仅复制自身字段（无数据时的兜底）
     */
    private StockLogVO convertToBasicVO(StockLog logEntry) {
        StockLogVO vo = new StockLogVO();
        BeanUtils.copyProperties(logEntry, vo);
        return vo;
    }

    /**
     * 完整转换：关联仓库名、商品名
     * orderNo 直接从实体取（写入时已存储的快照）
     */
    private StockLogVO convertToVO(StockLog logEntry,
                                   Map<Long, Warehouse> warehouseMap,
                                   Map<Long, Product> productMap) {
        StockLogVO vo = new StockLogVO();
        BeanUtils.copyProperties(logEntry, vo);

        // 关联仓库名称
        Warehouse warehouse = warehouseMap.get(logEntry.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        // 关联商品名称 + SKU
        Product product = productMap.get(logEntry.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setSkuCode(product.getSkuCode());
        }

        // orderNo 已经在 BeanUtils.copyProperties 时复制过来了，不需要额外处理

        return vo;
    }
}
