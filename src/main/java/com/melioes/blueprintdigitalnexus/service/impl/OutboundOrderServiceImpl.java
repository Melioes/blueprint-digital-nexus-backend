package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.DateConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.InboundConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.InventoryConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.OutboundConstant;
import com.melioes.blueprintdigitalnexus.common.constant.wms.WarehouseConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import com.melioes.blueprintdigitalnexus.common.utils.BizValidateUtil;
import com.melioes.blueprintdigitalnexus.common.utils.CodeGenerator;
import com.melioes.blueprintdigitalnexus.common.utils.RedisIdGenerator;
import com.melioes.blueprintdigitalnexus.dto.InventoryDTO;
import com.melioes.blueprintdigitalnexus.dto.OutboundDetailDTO;
import com.melioes.blueprintdigitalnexus.dto.OutboundOrderDTO;
import com.melioes.blueprintdigitalnexus.entity.*;
import com.melioes.blueprintdigitalnexus.mapper.OutboundOrderMapper;
import com.melioes.blueprintdigitalnexus.query.OutboundOrderQuery;
import com.melioes.blueprintdigitalnexus.service.*;
import com.melioes.blueprintdigitalnexus.vo.OutboundDetailVO;
import com.melioes.blueprintdigitalnexus.vo.OutboundOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * 出库单业务实现类
 *
 * 和入库对称，但有一个关键区别：
 * 入库确认 = 增加库存（adjustType=IN）
 * 出库确认 = 减少库存（adjustType=OUT）★
 * 入库取消 = 减少库存
 * 出库取消 = 增加库存（恢复）★
 */
@Slf4j
@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService, SequenceSyncService {
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private OutboundDetailService outboundDetailService;
    @Autowired
    private ProductService productService;

    @Autowired
    private RedisIdGenerator redisIdGenerator;// 生成出库单号

    @Autowired
    private InventoryService inventoryService;
    /**
     * 分页查询出库单
     * <p>
     * 实现思路：先构建条件分页查询主表 → 批量查仓库（一次查询代替 N 次）→ 组装 VO。
     * 无数据时直接返回，避免多余的仓库查询。
     * </p>
     *
     * @param query 查询条件（单号模糊、仓库ID、状态、时间范围、分页参数）
     * @return 出库单分页数据
     */
    @Override
    public IPage<OutboundOrderVO> getOutboundPage(OutboundOrderQuery query) {

        // 1. 构建条件 + 分页查询
        Page<OutboundOrder> pageParam = new Page<>(query.fetchPage(), query.fetchSize());
        LambdaQueryWrapper<OutboundOrder> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(OutboundOrder::getCreateTime);

        IPage<OutboundOrder> pageResult = this.page(pageParam, wrapper);

        // 2. 无数据直接返回
        if (CollectionUtils.isEmpty(pageResult.getRecords())) {
            log.info("无出库单数据");
            return pageResult.convert(this::convertToBasicVO);
        }

        // 3. 批量查询关联仓库（一次查询，用 Map 做 O(1) 查找）
        Map<Long, Warehouse> warehouseMap = fetchWarehouseMap(pageResult.getRecords());

        // 4. 转换为 VO（关联仓库名称 + 状态名称）
        return pageResult.convert(order -> convertToVO(order, warehouseMap));
    }

    /**
     * 查询出库单详情
     * <p>
     * 实现思路：校验存在 → 单查仓库 → 查明细（含商品信息）→ 组装完整 VO。
     * </p>
     *
     * @param id 出库单ID
     * @return 出库单详情（含明细列表）
     */
    @Override
    public OutboundOrderVO getOutboundById(Long id) {
        // 1. 检查出库单是否存在
        OutboundOrder order = getAndCheckOutbound(id);


        // 2. 查询仓库信息
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());

        // 3. 查询明细（包含商品）
        List<OutboundDetailVO> detailVOList = outboundDetailService.listByOrderId(id);
        // 4. 转换明细为VO
        OutboundOrderVO vo = convertToVO(order, warehouse);
        vo.setDetails(detailVOList);

        return vo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOutbound(OutboundOrderDTO dto) {
        log.info("创建出库单: warehouseId={}", dto.getWarehouseId());

        // 第1步：校验参数（仓库存在？商品存在？数量>0？）
        validateOutboundParams(dto);

        // 2. 生成出库单号（格式: OUT-yyyyMMdd-xxx，自动去重）
        String orderNo = CodeGenerator.generateWithRetry(
                OutboundConstant.ORDER_NO_PREFIX,
                redisIdGenerator,
                code -> this.lambdaQuery().eq(OutboundOrder::getOrderNo, code).count() > 0);

        // 3. 保存主表（默认草稿状态） 0 草稿
        OutboundOrder order = new OutboundOrder();
        BeanUtils.copyProperties(dto, order);
        order.setOrderNo(orderNo);
        order.setStatus(OutboundConstant.STATUS_DRAFT);
        this.save(order);

        // 4. 保存明细表
        outboundDetailService.saveBatch(order.getOutboundOrderId(), dto.getDetails());

        log.info("OK: 出库单创建成功: orderId={}, orderNo={}", order.getOutboundOrderId(), orderNo);

    }


    /**
     * 修改出库单
     * <p>
     * 完整流程：
     * 1. 校验出库单存在且状态为草稿
     * 2. 校验入参（明细非空、仓库存在、商品存在、数量>0）
     * 3. 删除旧明细 → 保存新明细
     * 4. 更新主表（不更新订单号、状态、创建时间）
     * 全程在一个事务中，保证旧明细删除和新明细写入的原子性。
     * </p>
     *
     * @param dto 出库单信息
     */
    @Override
    //删明细和存主表必须在同一事务里，失败就全部回滚
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(OutboundOrderDTO dto) {
        log.info("修改出库单: orderId={}", dto.getOutboundOrderId());
        // 1. 校验出库单是否存在
        OutboundOrder order = getAndCheckOutbound(dto.getOutboundOrderId());
        // 2. 校验状态：只有草稿可以修改
        checkStatusDraft(order, OutboundConstant.INVALID_STATUS_FOR_UPDATE);
        // 3．校验参数（仓库、商品、数量）
        validateOutboundParams(dto);
        // 4。删旧明细+存新明细
        outboundDetailService.removeByOrderId(order.getOutboundOrderId());
        outboundDetailService.saveBatch(order.getOutboundOrderId(), dto.getDetails());
        // 5. 更新主表（仅更新允许修改的字段） 业务只允许修改仓库，备注
        order.setWarehouseId(dto.getWarehouseId());
        order.setRemark(dto.getRemark());
        this.updateById(order);
        log.info("OK: 出库单修改成功: orderId={}", order.getOutboundOrderId());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutbound(Long id) {
        log.info("删除出库单: id={}", id);
        // 1.查出库单是否存在
        OutboundOrder order = getAndCheckOutbound(id);
        // 2．检查状态是否为草稿
        checkStatusDraft(order, OutboundConstant.INVALID_STATUS_FOR_DELETE);
        // 3.删明细+删主表
        outboundDetailService.removeByOrderId(id);
        this.removeById(id);
        log.info("OK: 出库单删除成功: id={}", id);
    }

    /**
     * 确认出库（核心业务方法）
     * <p>
     * 完整流程：
     * 1. 校验出库单存在且状态为草稿
     * 2. 查询明细列表
     * 3. 逐条调用库存模块减少库存（adjustType=OUT）
     * 4. 更新出库单状态为已确认
     * <p>
     * 设计要点：
     * - 逐条调用而非批量调用：每个商品的库存调整涉及乐观锁，必须独立处理
     * - 调整原因预计算：orderNo 在循环中不变，提前拼好避免循环内重复拼接字符串
     * - 全程 @Transactional：任一条明细调整失败即全部回滚
     * </p>
     *
     * @param id 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutbound(Long id) {
        log.info("确认出库: id={}", id);
        // 1.查出库单是否存在
        OutboundOrder order = getAndCheckOutbound(id);
        // 2．校验状态为草稿
        checkStatusDraft(order, OutboundConstant.INVALID_STATUS_FOR_CONFIRM);
        // 3.查出明细列表
        List<OutboundDetail> detailList = outboundDetailService.lambdaQuery().eq(OutboundDetail::getOutboundOrderId, id).list();
        // 4. 逐条写入库存（乐观锁保证并发安全）
        // 预计算调整原因，避免循环内重复拼接字符串
        String reason = OutboundConstant.OUTBOUND_CONFIRM_REASON + order.getOrderNo();
        for (OutboundDetail detail : detailList) {
            // 调用库存模块 循环扣减每个商品的库存
            inventoryService.adjustInventory(
                    buildInventoryAdjustDTO(order, detail, InventoryConstant.ADJUST_TYPE_OUT, reason));
        }

        // 5.更新状态为已确认
        order.setStatus(OutboundConstant.STATUS_CONFIRMED);
        this.updateById(order);
        log.info("OK: 出库确认成功: id={}, orderNo={}", id, order.getOrderNo());

    }

    /**
     * 取消出库（核心业务方法）
     * <p>
     * 完整流程：
     * 1. 校验出库单存在且状态为已确认
     * 2. 查询明细列表
     * 3. 逐条调用库存模块减少库存（用 ADJUST_TYPE_IN）
     * 4. 更新出库单状态为已取消
     * <p>
     * 设计要点：与确认出库对称，仅 adjustType 方向和状态校验目标不同。
     * </p>
     *
     * @param id 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutbound(Long id) {
        log.info("取消出库: id={}", id);

        // 1. 校验出库单存在
        OutboundOrder order = getAndCheckOutbound(id);

        // 2. 校验状态：只有已确认可以取消
        if (!OutboundConstant.STATUS_CONFIRMED.equals(order.getStatus())) {
            throw new BusinessException(OutboundConstant.INVALID_STATUS_FOR_CANCEL);
        }

        // 3. 查询明细
        List<OutboundDetail> detailList = outboundDetailService.lambdaQuery()
                .eq(OutboundDetail::getOutboundOrderId, id).list();

        // 4. 逐条恢复库存（和确认相反，用 ADJUST_TYPE_IN）
        String reason = OutboundConstant.OUTBOUND_CANCEL_REASON + order.getOrderNo();
        for (OutboundDetail detail : detailList) {
            inventoryService.adjustInventory(
                    buildInventoryAdjustDTO(order, detail, InventoryConstant.ADJUST_TYPE_IN, reason));
        }

        // 5. 更新状态为已取消
        order.setStatus(OutboundConstant.STATUS_CANCELLED);
        this.updateById(order);
        log.info("OK: 出库取消成功: id={}, orderNo={}", id, order.getOrderNo());
    }

    /**
     * 校验出库单是否存在，存在则返回实体
     *
     * @param id 出库单ID
     * @return 出库单实体
     */
    @Override
    public OutboundOrder getAndCheckOutbound(Long id) {
        return BizValidateUtil.checkIdExistAndGet(
                baseMapper,
                OutboundOrder::getOutboundOrderId,
                id,
                OutboundConstant.OUTBOUND_NOT_FOUND
        );
    }

    /**
     * 基础转换：仅复制自身字段，不关联仓库/状态名称
     * 用于分页结果为空时的兜底转换。
     */
    private OutboundOrderVO convertToBasicVO(OutboundOrder order) {
        OutboundOrderVO vo = new OutboundOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    // ==================== SequenceSyncService 接口实现 ====================

    /**
     * 业务编码前缀
     */
    @Override
    public String getBusinessPrefix() {
        return InboundConstant.ORDER_NO_PREFIX;
    }

    /**
     * 获取今日最大序号
     * <p>
     * 查询当天创建的所有出库单，解析订单号中的序号部分，返回最大值。
     * 供 CodeGenerator 在生成新单号时计算下一个序号。
     * </p>
     *
     * @param dateStr 日期字符串（yyyyMMdd 格式）
     * @return 今日最大序号，无数据返回 0
     */
    @Override
    public int getTodayMaxSequence(String dateStr) {
        log.debug("[OutboundOrderServiceImpl] 统计今日最大序号: date={}", dateStr);

        // 1. 计算当天时间范围
        LocalDateTime startOfDay = LocalDate.parse(dateStr, DateConstant.FORMATTER_YYYYMMDD).atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 2. 查询当天创建的所有出库单
        List<OutboundOrder> orderList = this.lambdaQuery()
                .ge(OutboundOrder::getCreateTime, startOfDay)
                .lt(OutboundOrder::getCreateTime, endOfDay)
                .list();

        // 3. 从订单号中解析最大序号
        return CodeGenerator.getTodayMaxSequence(
                dateStr,
                OutboundConstant.ORDER_NO_PREFIX,
                orderList,
                OutboundOrder::getOrderNo);
    }


    /**
     * 完整转换（单条场景）：直接传入 Warehouse 对象关联仓库名称
     * 用于详情查询等单条场景，避免构建 Map 的开销。
     *
     * @param order     出库单实体
     * @param warehouse 仓库实体
     * @return 出库单视图对象（含仓库名称、状态名称）
     */
    private OutboundOrderVO convertToVO(OutboundOrder order, Warehouse warehouse) {
        OutboundOrderVO vo = new OutboundOrderVO();
        BeanUtils.copyProperties(order, vo);

        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        setStatusName(vo, order.getStatus());

        return vo;
    }

    /**
     * 完整转换（批量场景）：从预查询的 Map 中关联仓库名称
     * <p>
     * 设计思路：接收预查询好的 warehouseMap 而非在方法内查询，
     * 这样分页批量转换时只需要一次 DB 查询，用 Map 做 O(1) 查找。
     * </p>
     *
     * @param order        出库单实体
     * @param warehouseMap 仓库ID到仓库实体的映射
     * @return 出库单视图对象（含仓库名称、状态名称）
     */
    private OutboundOrderVO convertToVO(OutboundOrder order, Map<Long, Warehouse> warehouseMap) {
        // 2. 转换为VO
        OutboundOrderVO vo = new OutboundOrderVO();
        BeanUtils.copyProperties(order, vo);
        Warehouse warehouse = warehouseMap.get(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }
        setStatusName(vo, order.getStatus());
        return vo;
    }

    /**
     * 设置状态名称（数字 → 中文）
     */
    private void setStatusName(OutboundOrderVO vo, Integer status) {
        if (OutboundConstant.STATUS_DRAFT.equals(status)) {
            vo.setStatusName(OutboundConstant.STATUS_NAME_DRAFT);
        } else if (OutboundConstant.STATUS_CONFIRMED.equals(status)) {
            vo.setStatusName(OutboundConstant.STATUS_NAME_CONFIRMED);
        } else if (OutboundConstant.STATUS_CANCELLED.equals(status)) {
            vo.setStatusName(OutboundConstant.STATUS_NAME_CANCELLED);
        }
    }


    /**
     * 构建分页查询条件
     * <p>
     * 设计说明：使用独立的 if 判断而非链式 .eq(condition, ...)，
     * 因为多个条件之间是 AND 关系且各自独立，if 写法可读性更好，
     * 且当条件为 null/空时完全跳过，不产生多余的 SQL 片段。
     * </p>
     */
    private LambdaQueryWrapper<OutboundOrder> buildQueryWrapper(OutboundOrderQuery query) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();

        // 单号模糊查询
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(OutboundOrder::getOrderNo, query.getOrderNo());
        }
        // 仓库ID精确筛选
        if (query.getWarehouseId() != null) {
            wrapper.eq(OutboundOrder::getWarehouseId, query.getWarehouseId());
        }
        // 状态精确筛选
        if (query.getStatus() != null) {
            wrapper.eq(OutboundOrder::getStatus, query.getStatus());
        }
        // 时间范围筛选（左闭右闭）
        if (query.getStartTime() != null) {
            wrapper.ge(OutboundOrder::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OutboundOrder::getCreateTime, query.getEndTime());
        }

        return wrapper;
    }

    // ==================== 私有辅助方法：批量查询 ====================

    /**
     * 批量查询仓库，构建 warehouseId → Warehouse 映射
     * <p>
     * 设计目的：封装"提取 warehouseIds → 批量查询 → 构建 Map"的重复模式，
     * 避免调用方每次手写 stream().map().collect(toSet()) 的样板代码。
     * </p>
     *
     * @param orderList 出库单列表
     * @return warehouseId 到 Warehouse 的映射
     */
    private Map<Long, Warehouse> fetchWarehouseMap(List<OutboundOrder> orderList) {
        Set<Long> warehouseIds = orderList.stream()
                .map(OutboundOrder::getWarehouseId)
                .collect(Collectors.toSet());

        List<Warehouse> warehouseList = warehouseService.lambdaQuery()
                .in(Warehouse::getWarehouseId, warehouseIds)
                .list();

        return warehouseList.stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, w -> w));
    }


    /**
     * 统一校验入库单入参（用于新增和修改）
     * <p>
     * 校验项：明细非空、仓库存在、商品存在、数量>0。
     * 将 addInbound 和 updateInbound 中的重复校验逻辑提取到此方法。
     * </p>
     */
    private void validateOutboundParams(OutboundOrderDTO dto) {
        // 仓库ID必填
        if (dto.getWarehouseId() == null) {
            throw new BusinessException(OutboundConstant.WAREHOUSE_ID_REQUIRED);
        }

        // 明细列表非空
        if (CollectionUtils.isEmpty(dto.getDetails())) {
            throw new BusinessException(OutboundConstant.DETAILS_REQUIRED);
        }

        // 校验仓库是否存在
        BizValidateUtil.checkIdExist(
                warehouseService.getBaseMapper(),
                Warehouse::getWarehouseId,
                dto.getWarehouseId(),
                WarehouseConstant.WAREHOUSE_NOT_FOUND);

        // 校验所有商品是否存在（一次 count 查询）
        validateDetailProductsExist(dto.getDetails());

        // 校验所有明细数量 > 0
        validateQuantities(dto.getDetails());
    }

    /**
     * 校验明细中的所有商品是否存在
     * <p>
     * 用 MP count + in 一次查询确认，count 与 ID 数量不一致即存在无效商品。
     * </p>
     */
    private void validateDetailProductsExist(List<OutboundDetailDTO> details) {
        Set<Long> productIds = details.stream()
                .map(OutboundDetailDTO::getProductId)
                .collect(Collectors.toSet());

        long productCount = productService.lambdaQuery()
                .in(Product::getProductId, productIds)
                .count();

        if (productCount != productIds.size()) {
            throw new BusinessException(OutboundConstant.PRODUCT_PARTIAL_NOT_FOUND);
        }
    }

    /**
     * 校验所有明细数量 > 0
     * <p>
     * 使用 Stream.allMatch 替代 for 循环，更简洁。
     * </p>
     */
    private void validateQuantities(List<OutboundDetailDTO> details) {
        boolean allValid = details.stream()
                .allMatch(d -> d.getQuantity() != null && d.getQuantity() > 0);
        if (!allValid) {
            throw new BusinessException(OutboundConstant.INVALID_QUANTITY);
        }
    }


    /**
     * 校验出库单状态为草稿 0
     *
     * @param order    出库单
     * @param errorMsg 状态不符合时的错误信息
     */
    private void checkStatusDraft(OutboundOrder order, String errorMsg) {
        if (!OutboundConstant.STATUS_DRAFT.equals(order.getStatus())) {
            throw new BusinessException(errorMsg);
        }
    }

    /**
     * 构建库存调整参数
     */
    private InventoryDTO buildInventoryAdjustDTO(OutboundOrder order, OutboundDetail detail,
                                                 String adjustType, String reason) {
        InventoryDTO dto = new InventoryDTO();
        dto.setWarehouseId(order.getWarehouseId());
        dto.setProductId(detail.getProductId());
        dto.setTotalStock(detail.getQuantity());
        dto.setAdjustType(adjustType);
        dto.setAdjustReason(reason);
        return dto;
    }

}

