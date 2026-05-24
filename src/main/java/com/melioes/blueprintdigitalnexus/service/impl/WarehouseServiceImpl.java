package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.DateConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.WarehouseConstant;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import com.melioes.blueprintdigitalnexus.common.utils.BizValidateUtil;
import com.melioes.blueprintdigitalnexus.common.utils.CodeGenerator;
import com.melioes.blueprintdigitalnexus.common.utils.RedisIdGenerator;
import com.melioes.blueprintdigitalnexus.dto.WarehouseDTO;
import com.melioes.blueprintdigitalnexus.entity.Warehouse;
import com.melioes.blueprintdigitalnexus.mapper.WarehouseMapper;
import com.melioes.blueprintdigitalnexus.query.WarehouseQuery;
import com.melioes.blueprintdigitalnexus.service.WarehouseService;
import com.melioes.blueprintdigitalnexus.vo.WarehouseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库业务实现类
 * 实现 SequenceSyncService 接口，提供序号同步能力
 */
@Slf4j
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse>
        implements WarehouseService, SequenceSyncService {

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    /**
     * 分页查询仓库列表
     *
     * @param query 查询条件（关键词、状态、分页参数）
     * @return 仓库分页数据
     */
    @Override
    public IPage<WarehouseVO> getWarehousePage(WarehouseQuery query) {
        // int pageNum = query.getPage() == null ? 1 : query.getPage();
        // int pageSize = query.getSize() == null ? 10 : query.getSize();
        //
        // Page<Warehouse> pageParam = new Page<>(pageNum, pageSize);
        Page<Warehouse> pageParams = new Page<>(query.fetchPage(), query.fetchSize());
        LambdaQueryWrapper<Warehouse> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(Warehouse::getCreateTime);

        IPage<Warehouse> pageResult = this.page(pageParams, wrapper);
        return pageResult.convert(this::convertToVO);
    }

    /**
     * 查询仓库列表（无分页）
     *
     * @param query 查询条件
     * @return 仓库列表
     */
    @Override
    public List<WarehouseVO> getWarehouseList(WarehouseQuery query) {
        LambdaQueryWrapper<Warehouse> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(Warehouse::getCreateTime);

        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询仓库详情
     *
     * @param warehouseId 仓库ID
     * @return 仓库详情VO
     */
    @Override
    public WarehouseVO getWarehouseById(Long warehouseId) {
        // 使用通用校验工具：校验ID是否存在并获取实体
        Warehouse warehouse = BizValidateUtil.checkIdExistAndGet(
                baseMapper,
                Warehouse::getWarehouseId,
                warehouseId,
                WarehouseConstant.WAREHOUSE_NOT_FOUND);
        return convertToVO(warehouse);
    }

    /**
     * 新增仓库
     *
     * @param dto 仓库信息DTO
     */
    @Override
    public void addWarehouse(WarehouseDTO dto) {
        log.info("新增仓库: warehouseName={}", dto.getWarehouseName());

        // 1. 校验仓库名称唯一性
        BizValidateUtil.checkUniqueForAdd(
                baseMapper,
                Warehouse::getWarehouseName,
                dto.getWarehouseName(),
                WarehouseConstant.WAREHOUSE_NAME_EXISTS);

        // 2. 生成编码，最多重试 3 次防止冲突
        String warehouseCode = CodeGenerator.generateWithRetry(
                WarehouseConstant.WAREHOUSE_CODE_PREFIX,
                redisIdGenerator,
                code -> this.lambdaQuery().eq(Warehouse::getWarehouseCode, code).count() > 0);
        dto.setWarehouseCode(warehouseCode);

        // 3. 设置默认状态（启用）
        if (dto.getStatus() == null) {
            dto.setStatus(WarehouseConstant.STATUS_ENABLE);
        }

        // 4. 保存
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(dto, warehouse);
        this.save(warehouse);

        log.info("OK: 仓库新增成功: warehouseId={}, warehouseCode={}",
                warehouse.getWarehouseId(), warehouse.getWarehouseCode());
    }

    /**
     * 修改仓库
     *
     * @param dto 仓库信息DTO
     */
    @Override
    public void updateWarehouse(WarehouseDTO dto) {
        log.info("修改仓库: warehouseId={}", dto.getWarehouseId());

        // 1. 校验ID是否存在
        BizValidateUtil.checkIdExist(
                baseMapper,
                Warehouse::getWarehouseId,
                dto.getWarehouseId(),
                WarehouseConstant.WAREHOUSE_NOT_FOUND);

        Warehouse warehouse = this.getById(dto.getWarehouseId());

        // 2. 校验仓库名称唯一性（排除当前ID，防止编辑时误判自己重名）
        if (StringUtils.hasText(dto.getWarehouseName())
                && !dto.getWarehouseName().equals(warehouse.getWarehouseName())) {
            BizValidateUtil.checkUniqueForEdit(
                    baseMapper,
                    Warehouse::getWarehouseName,
                    dto.getWarehouseName(),
                    Warehouse::getWarehouseId,
                    dto.getWarehouseId(),
                    WarehouseConstant.WAREHOUSE_NAME_EXISTS);
        }

        // 3. 不允许修改仓库编码（编码一旦生成就固定，保持唯一性）
        // 把原来的key打回去
        dto.setWarehouseCode(warehouse.getWarehouseCode());

        // 4. 更新
        BeanUtils.copyProperties(dto, warehouse);
        this.updateById(warehouse);

        log.info("OK: 仓库修改成功: warehouseId={}", dto.getWarehouseId());
    }

    /**
     * 删除仓库
     *
     * @param warehouseId 仓库ID
     */
    @Override
    public void deleteWarehouse(Long warehouseId) {
        log.info("删除仓库: warehouseId={}", warehouseId);

        // 1. 校验ID是否存在
        BizValidateUtil.checkIdExist(
                baseMapper,
                Warehouse::getWarehouseId,
                warehouseId,
                WarehouseConstant.WAREHOUSE_NOT_FOUND);

        // 2. 检查是否有库存关联（TODO: 待库存模块完成后实现，防止有库存的仓库被误删）
        // Long inventoryCount = inventoryService.lambdaQuery()
        // .eq(Inventory::getWarehouseId, warehouseId)
        // .count();
        // if (inventoryCount > 0) {
        // log.warn("FAIL: 仓库下有库存，无法删除: warehouseId={}", warehouseId);
        // throw new BusinessException(WarehouseConstant.WAREHOUSE_HAS_INVENTORY);
        // }

        // 3. 删除
        this.removeById(warehouseId);
        log.info("OK: 仓库删除成功: warehouseId={}", warehouseId);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Warehouse> buildQueryWrapper(WarehouseQuery query) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();

        // 下拉列表：仅显示启用状态
        if (Boolean.TRUE.equals(query.getDropdown())) {
            wrapper.eq(Warehouse::getStatus, WarehouseConstant.STATUS_ENABLE);
        } else {
            // 状态筛选
            wrapper.eq(query.getStatus() != null, Warehouse::getStatus, query.getStatus());
        }

        // 关键词筛选（名称或编码）
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Warehouse::getWarehouseName, query.getKeyword())
                    .or()
                    .like(Warehouse::getWarehouseCode, query.getKeyword()));
        }

        return wrapper;
    }

    /**
     * 将仓库实体转换为视图对象
     * 包含：转换状态名称
     *
     * @param warehouse 仓库实体
     * @return 仓库视图对象
     */
    private WarehouseVO convertToVO(Warehouse warehouse) {
        WarehouseVO vo = new WarehouseVO();
        BeanUtils.copyProperties(warehouse, vo);

        // 状态名称转换
        if (WarehouseConstant.STATUS_ENABLE.equals(warehouse.getStatus())) {
            vo.setStatusName("启用");
        } else if (WarehouseConstant.STATUS_DISABLE.equals(warehouse.getStatus())) {
            vo.setStatusName("禁用");
        }

        return vo;
    }

    // ==================== SequenceSyncService 接口实现 ====================

    @Override
    public String getBusinessPrefix() {
        return WarehouseConstant.WAREHOUSE_CODE_PREFIX;
    }

    @Override
    public int getTodayMaxSequence(String dateStr) {
        log.debug("[WarehouseServiceImpl] 统计今日最大序号: date={}", dateStr);

        // 1. 计算当天的开始和结束时间
        LocalDateTime startOfDay = LocalDate.parse(dateStr, DateConstant.FORMATTER_YYYYMMDD).atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 2. 查询当天创建的所有仓库
        List<Warehouse> warehouses = this.lambdaQuery()
                .ge(Warehouse::getCreateTime, startOfDay)
                .lt(Warehouse::getCreateTime, endOfDay)
                .list();

        // 3. 使用通用工具类计算最大序号
        return CodeGenerator.getTodayMaxSequence(
                dateStr,
                WarehouseConstant.WAREHOUSE_CODE_PREFIX,
                warehouses,
                Warehouse::getWarehouseCode);
    }
}
