package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.InventoryConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.WarehouseConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.utils.BizValidateUtil;
import com.melioes.blueprintdigitalnexus.dto.InventoryDTO;
import com.melioes.blueprintdigitalnexus.entity.Inventory;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.entity.StockLog;
import com.melioes.blueprintdigitalnexus.entity.Warehouse;
import com.melioes.blueprintdigitalnexus.mapper.InventoryMapper;
import com.melioes.blueprintdigitalnexus.query.InventoryQuery;
import com.melioes.blueprintdigitalnexus.service.InventoryService;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.service.StockLogService;
import com.melioes.blueprintdigitalnexus.service.WarehouseService;
import com.melioes.blueprintdigitalnexus.vo.InventoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
/**
 * 库存服务实现类
 */
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private ProductService productService;
    @Autowired
    private StockLogService stockLogService;

    /**
     * 分页查询库存列表（包含商品与仓库明细）
     * 
     * @param query 库存分页查询条件传输对象
     * @return 包含完整商品名、仓库名等信息的库存 VO 分页数据
     */
    @Override
    public IPage<InventoryVO> getInventoryPage(InventoryQuery query) {
        log.info("[分页查询] 开始执行: pageNum={}, pageSize={}", query.fetchPage(), query.fetchSize());

        // 步骤1：构建查询条件
        LambdaQueryWrapper<Inventory> wrapper = buildQueryWrapper(query);

        // 步骤2：执行分页查询
        Page<Inventory> page = new Page<>(query.fetchPage(), query.fetchSize());
        IPage<Inventory> inventoryPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(inventoryPage.getRecords())) {
            log.info("[分页查询] 未查询到数据: total={}", inventoryPage.getTotal());
            return new Page<>();
        }

        // 步骤3：批量查询关联表并转换 VO
        List<InventoryVO> voList = convertToVOList(inventoryPage.getRecords());

        // 步骤4：组装返回结果
        IPage<InventoryVO> voPage = new Page<>(inventoryPage.getCurrent(), inventoryPage.getSize(),
                inventoryPage.getTotal());
        voPage.setRecords(voList);

        log.info("[分页查询] 执行成功: total={}", inventoryPage.getTotal());
        return voPage;
    }

    /**
     * 根据ID查询库存详情
     * 
     * @param id 库存ID
     * @return 库存详情 VO
     */
    @Override
    public InventoryVO getInventoryById(Long id) {
        log.info("[详情查询] 开始执行: id={}", id);

        // 步骤1：检查库存是否存在
        Inventory inventory = getAndCheckInventory(id);

        // 步骤2：转换为 VO 并填充关联字段
        InventoryVO vo = convertToInventoryVO(inventory);

        log.info("[详情查询] 执行成功: id={}", id);
        return vo;
    }

    /**
     * 根据仓库ID和商品ID查询库存
     * 
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存 VO，不存在则返回 null
     */
    @Override
    public InventoryVO getByWarehouseAndProduct(Long warehouseId, Long productId) {
        log.info("[仓库+商品查询] 开始执行: warehouseId={}, productId={}", warehouseId, productId);

        // 步骤1：构建查询条件
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Inventory::getWarehouseId, warehouseId);
        wrapper.eq(productId != null, Inventory::getProductId, productId);

        // 步骤2：执行查询
        Inventory inventory = this.getOne(wrapper);

        // 步骤3：如果没查到，返回 null（业务允许不存在）
        if (inventory == null) {
            log.info("[仓库+商品查询] 未找到记录: warehouseId={}, productId={}", warehouseId, productId);
            return null;
        }

        // 步骤4：转换为 VO
        InventoryVO vo = convertToInventoryVO(inventory);
        log.info("[仓库+商品查询] 执行成功: inventoryId={}", inventory.getInventoryId());
        return vo;
    }

    /**
     * 调整库存（核心方法，支持增量和覆盖两种模式）
     * <p>
     * 1. 增量模式（adjustType=IN/OUT）：在现有库存基础上加/减
     * 2. 覆盖模式（adjustType为空）：直接设置库存值（如果不存在则创建）
     * </p>
     * 
     * @param dto 调整库存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(InventoryDTO dto) {
        log.info("[库存调整] 开始执行: warehouseId={}, productId={}, totalStock={}, adjustType={}",
                dto.getWarehouseId(), dto.getProductId(), dto.getTotalStock(), dto.getAdjustType());

        // 防线1：校验 warehouseId 和 productId 不能为空
        if (dto.getWarehouseId() == null || dto.getProductId() == null) {
            log.warn("[库存调整] 仓库ID或商品ID为空: warehouseId={}, productId={}", dto.getWarehouseId(), dto.getProductId());
            throw new BusinessException(InventoryConstant.WAREHOUSE_AND_PRODUCT_ID_REQUIRED);
        }

        // 防线2：校验仓库是否存在
        BizValidateUtil.checkIdExist(
                warehouseService.getBaseMapper(),
                Warehouse::getWarehouseId,
                dto.getWarehouseId(),
                WarehouseConstant.WAREHOUSE_NOT_FOUND);

        // 防线3：校验商品是否存在
        BizValidateUtil.checkIdExist(
                productService.getBaseMapper(),
                Product::getProductId,
                dto.getProductId(),
                ProductConstant.PRODUCT_NOT_FOUND);

        // 防线4：校验调整数量
        if (dto.getTotalStock() == null) {
            log.warn("[库存调整] 调整数量为空");
            throw new BusinessException(InventoryConstant.ADJUST_QUANTITY_REQUIRED);
        }

        // 步骤1：查询该仓库+商品是否已有库存 查当前库存（拿到 version）
        Inventory inventory = this.lambdaQuery()
                .eq(Inventory::getWarehouseId, dto.getWarehouseId())
                .eq(Inventory::getProductId, dto.getProductId())
                .one();

        Integer newStock;

        if (inventory != null) {
            // 场景A：已有库存 → 更新
            Integer oldStock = inventory.getTotalStock() == null ? 0 : inventory.getTotalStock();
            log.info("[库存调整] 找到现有库存: inventoryId={}, currentStock={}",
                    inventory.getInventoryId(), oldStock);

            // 根据 adjustType 计算新库存
            if (InventoryConstant.ADJUST_TYPE_IN.equals(dto.getAdjustType())) {
                // 模式1：入库（增加库存）
                newStock = oldStock + dto.getTotalStock();
            } else if (InventoryConstant.ADJUST_TYPE_OUT.equals(dto.getAdjustType())) {
                // 模式2：出库（减少库存）
                if (oldStock < dto.getTotalStock()) {
                    log.warn("[库存调整] 库存不足: current={}, need={}", oldStock, dto.getTotalStock());
                    throw new BusinessException(InventoryConstant.STOCK_NOT_ENOUGH);
                }
                newStock = oldStock - dto.getTotalStock();
            } else {
                // 模式3：覆盖调整（直接设置）
                newStock = dto.getTotalStock();
            }

            // 校验新库存不能为负数
            if (newStock < 0) {
                log.warn("[库存调整] 调整后库存不能为负数: newStock={}", newStock);
                throw new BusinessException(InventoryConstant.STOCK_NOT_POSITIVE);
            }

            // 更新库存（乐观锁自动生效）
            inventory.setTotalStock(newStock);

            this.updateById(inventory);
            // 记录库存变动日志
            saveStockLog(dto, newStock);


            log.info("[库存调整] 更新库存成功: inventoryId={}, oldStock={}, newStock={}",
                    inventory.getInventoryId(), oldStock, newStock);
        } else {
            // 场景B：没有库存 → 新增
            log.info("[库存调整] 未找到现有库存，准备新增");

            // 如果是出库模式，但没有库存，直接报错
            if (InventoryConstant.ADJUST_TYPE_OUT.equals(dto.getAdjustType())) {
                log.warn("[库存调整] 出库模式但无库存记录");
                throw new BusinessException(InventoryConstant.STOCK_NOT_ENOUGH);
            }

            // 计算新库存（如果是 IN 模式或者覆盖模式）
            newStock = dto.getTotalStock();

            // 校验新库存不能为负数
            if (newStock < 0) {
                log.warn("[库存调整] 新增库存不能为负数: newStock={}", newStock);
                throw new BusinessException(InventoryConstant.STOCK_NOT_POSITIVE);
            }

            // 新增库存
            inventory = new Inventory();
            inventory.setWarehouseId(dto.getWarehouseId());
            inventory.setProductId(dto.getProductId());
            inventory.setTotalStock(newStock);
            inventory.setLockedStock(InventoryConstant.DEFAULT_LOCKED_STOCK);
            inventory.setVersion(InventoryConstant.DEFAULT_VERSION);
            this.save(inventory);

            log.info("[库存调整] 新增库存成功: inventoryId={}, warehouseId={}, productId={}, totalStock={}",
                    inventory.getInventoryId(), dto.getWarehouseId(), dto.getProductId(), newStock);
        }

        log.info("[库存调整] 执行成功");
    }

    /**
     * 查询库存列表（无分页，用于下拉）
     * 
     * @param query 库存查询条件
     * @return 库存列表
     */
    @Override
    public List<InventoryVO> getInventoryList(InventoryQuery query) {
        log.info("[列表查询] 开始执行: warehouseId={}, productId={}", query.getWarehouseId(), query.getProductId());

        // 步骤1：构建查询条件（复用分页逻辑）
        LambdaQueryWrapper<Inventory> wrapper = buildQueryWrapper(query);

        // 步骤2：执行列表查询（与分页的区别在于用 list 而非 page）
        List<Inventory> inventoryList = this.list(wrapper);

        // 步骤3：批量查询关联表并转换 VO
        List<InventoryVO> voList = convertToVOList(inventoryList);

        log.info("[列表查询] 执行成功: count={}", voList.size());
        return voList;
    }

    /**
     * 检查库存是否存在并返回（供其他模块调用）
     * 
     * @param id 库存ID
     * @return 库存实体
     */
    @Override
    public Inventory getAndCheckInventory(Long id) {
        // 复用工具类：自动检查 ID 合法性、查询数据库、不存在则抛异常
        return BizValidateUtil.checkIdExistAndGet(
                baseMapper,
                Inventory::getInventoryId,
                id,
                InventoryConstant.INVENTORY_NOT_FOUND);
    }

    /**
     * 新增库存
     * 
     * @param dto 新增库存参数
     */
    @Override
    public void addInventory(InventoryDTO dto) {
        log.info("[新增库存] 开始执行: warehouseId={}, productId={}, totalStock={}",
                dto.getWarehouseId(), dto.getProductId(), dto.getTotalStock());

        // 防线1：校验仓库是否存在
        BizValidateUtil.checkIdExist(
                warehouseService.getBaseMapper(),
                Warehouse::getWarehouseId,
                dto.getWarehouseId(),
                WarehouseConstant.WAREHOUSE_NOT_FOUND);

        // 防线2：校验商品是否存在
        BizValidateUtil.checkIdExist(
                productService.getBaseMapper(),
                Product::getProductId,
                dto.getProductId(),
                ProductConstant.PRODUCT_NOT_FOUND);

        // 防线3：校验库存数量不能小于0
        if (dto.getTotalStock() != null && dto.getTotalStock() < 0) {
            log.warn("[新增库存] 库存数量不能为负数: totalStock={}", dto.getTotalStock());
            throw new BusinessException(InventoryConstant.STOCK_NOT_POSITIVE);
        }

        // 防线4：检查该仓库+商品是否已存在库存
        Long count = this.lambdaQuery()
                .eq(Inventory::getWarehouseId, dto.getWarehouseId())
                .eq(Inventory::getProductId, dto.getProductId())
                .count();

        if (count != null && count > 0) {
            log.warn("[新增库存] 库存已存在: warehouseId={}, productId={}", dto.getWarehouseId(), dto.getProductId());
            throw new BusinessException(InventoryConstant.INVENTORY_ALREADY_EXISTS);
        }

        // 步骤5：保存到数据库
        Inventory inventory = new Inventory();
        BeanUtils.copyProperties(dto, inventory);
        inventory.setLockedStock(InventoryConstant.DEFAULT_LOCKED_STOCK);
        inventory.setVersion(InventoryConstant.DEFAULT_VERSION);
        this.save(inventory);

        log.info("[新增库存] 执行成功: inventoryId={}, warehouseId={}, productId={}",
                inventory.getInventoryId(), dto.getWarehouseId(), dto.getProductId());
    }

    // ==================== 私有方法：转换逻辑 ====================

    /**
     * 单个库存实体转换为 VO（批量查询场景，使用预加载的 Map）
     * 
     * @param inventory    库存实体
     * @param productMap   商品 Map（预加载）
     * @param warehouseMap 仓库 Map（预加载）
     * @return 库存 VO
     */
    private InventoryVO convertToInventoryVO(Inventory inventory,
            Map<Long, Product> productMap,
            Map<Long, Warehouse> warehouseMap) {
        InventoryVO inventoryVO = new InventoryVO();
        BeanUtils.copyProperties(inventory, inventoryVO);

        // 填充仓库信息
        Warehouse warehouse = warehouseMap.get(inventory.getWarehouseId());
        if (warehouse != null) {
            inventoryVO.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充商品信息
        Product product = productMap.get(inventory.getProductId());
        if (product != null) {
            inventoryVO.setProductName(product.getProductName());
            inventoryVO.setSkuCode(product.getSkuCode());
        }

        return inventoryVO;
    }

    /**
     * 单个库存实体转换为 VO（单个查询场景，直接查库）
     * 
     * @param inventory 库存实体
     * @return 库存 VO
     */
    private InventoryVO convertToInventoryVO(Inventory inventory) {
        InventoryVO vo = new InventoryVO();
        BeanUtils.copyProperties(inventory, vo);

        // 填充仓库信息
        if (inventory.getWarehouseId() != null) {
            Warehouse warehouse = warehouseService.getById(inventory.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 填充商品信息
        if (inventory.getProductId() != null) {
            Product product = productService.getById(inventory.getProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
                vo.setSkuCode(product.getSkuCode());
            }
        }

        return vo;
    }

    /**
     * 批量库存实体转换为 VO（性能优化：一次性加载关联表）
     * 
     * @param inventoryList 库存实体列表
     * @return VO 列表
     */
    private List<InventoryVO> convertToVOList(List<Inventory> inventoryList) {
        if (CollectionUtils.isEmpty(inventoryList)) {
            return List.of();
        }

        // 步骤1：收集需要查询的关联表 ID（去重）
        Set<Long> productIds = inventoryList.stream().map(Inventory::getProductId).collect(Collectors.toSet());
        Set<Long> warehouseIds = inventoryList.stream().map(Inventory::getWarehouseId).collect(Collectors.toSet());

        log.info("[批量转换] 开始预加载关联表: warehouseIdCount={}, productIdCount={}", warehouseIds.size(), productIds.size());

        // 步骤2：批量预加载关联表（性能优化：两次查询而非 N 次）
        Map<Long, Warehouse> warehouseMap = warehouseService.listByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, w -> w));
        Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // 步骤3：转换为 VO（使用预加载的 Map）
        return inventoryList.stream()
                .map(inventory -> convertToInventoryVO(inventory, productMap, warehouseMap))
                .toList();
    }

    // ==================== 私有方法：查询条件构建 ====================

    /**
     * 构建库存查询条件（复用给分页和列表用）
     * 
     * @param query 查询参数
     * @return 查询条件 wrapper
     */
    private LambdaQueryWrapper<Inventory> buildQueryWrapper(InventoryQuery query) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        // 场景1：有模糊搜索关键字
        if (StringUtils.hasText(query.getKeyWord())) {
            String keyWord = query.getKeyWord();
            log.info("[构建条件] 触发关键字搜索: keyWord='{}'", keyWord);

            // 先模糊搜索商品表（按名称或 SKU）
            List<Product> productList = productService.lambdaQuery()
                    .like(Product::getProductName, keyWord)
                    .or()
                    .like(Product::getSkuCode, keyWord)
                    .list();

            // 再模糊搜索仓库表（按名称）
            List<Warehouse> warehouseList = warehouseService.lambdaQuery()
                    .select(Warehouse::getWarehouseId)
                    .like(Warehouse::getWarehouseName, keyWord)
                    .list();

            // 如果都没搜到，加一个不可能的条件（1=0），确保查不到数据
            if (CollectionUtils.isEmpty(productList) && CollectionUtils.isEmpty(warehouseList)) {
                log.warn("[构建条件] 关键字未匹配到任何商品或仓库: keyWord='{}'", keyWord);
                wrapper.apply("1=0");
                return wrapper;
            }

            // 构建 OR 条件：(商品ID IN 商品列表) OR (仓库ID IN 仓库列表)
            wrapper.and(w -> {
                if (!CollectionUtils.isEmpty(productList)) {
                    List<Long> pIds = productList.stream().map(Product::getProductId).toList();
                    w.in(Inventory::getProductId, pIds);
                }
                if (!CollectionUtils.isEmpty(warehouseList)) {
                    List<Long> wIds = warehouseList.stream().map(Warehouse::getWarehouseId).toList();
                    if (!CollectionUtils.isEmpty(productList)) {
                        w.or(); // 如果前面有商品条件，用 OR 连接
                    }
                    w.in(Inventory::getWarehouseId, wIds);
                }
            });
        }

        // 场景2：普通条件筛选
        wrapper.eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId());
        wrapper.eq(query.getProductId() != null, Inventory::getProductId, query.getProductId());

        return wrapper;
    }

    /**
     * 记录库存变动日志
     *
     * @param dto      库存调整参数
     * @param newStock 调整后的库存值
     */
    private void saveStockLog(InventoryDTO dto, Integer newStock) {
        StockLog log = new StockLog();
        log.setWarehouseId(dto.getWarehouseId());
        log.setProductId(dto.getProductId());

        // 计算变动数量：正数=入库增加，负数=出库减少
        if (InventoryConstant.ADJUST_TYPE_IN.equals(dto.getAdjustType())) {
            log.setChangeQty(dto.getTotalStock());    // 入库：正数
            log.setType("IN");
        } else if (InventoryConstant.ADJUST_TYPE_OUT.equals(dto.getAdjustType())) {
            log.setChangeQty(-dto.getTotalStock());   // 出库：负数
            log.setType("OUT");
        } else {
            // 覆盖模式：变动量 = 新值 - 旧值（这里简化为记录新值）
            log.setChangeQty(newStock);
            log.setType("ADJUST");
        }

        // bizId 和 orderNo 通过 dto 传入（手动调整时为空）
        log.setBizId(dto.getBizId());
        log.setOrderNo(dto.getOrderNo());

        stockLogService.save(log);
    }
}
