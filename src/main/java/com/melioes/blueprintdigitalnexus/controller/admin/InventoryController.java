package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.melioes.blueprintdigitalnexus.common.utils.JsonLogUtil;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.InventoryDTO;
import com.melioes.blueprintdigitalnexus.query.InventoryQuery;
import com.melioes.blueprintdigitalnexus.service.InventoryService;
import com.melioes.blueprintdigitalnexus.vo.InventoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理
 */
@Slf4j
@Tag(name = "库存管理", description = "库存管理相关接口")
@RestController
@RequestMapping("/admin/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    /**
     * 新增库存
     */
    @PostMapping("/add")
    @OperLog(module = "库存管理", operation = "新增库存")
    @Operation(summary = "新增库存", description = "新增库存接口")
    public Result<Void> add(@RequestBody InventoryDTO dto) {
        log.info("[接口] 新增库存：\n{}", JsonLogUtil.toPrettyJson(dto));
        inventoryService.addInventory(dto);
        return Result.success();
    }

    /**
     * 分页查询库存
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询库存", description = "分页查询库存接口")
    public Result<IPage<InventoryVO>> getPage(InventoryQuery query) {
        log.info("[接口] 分页查询库存被调用: pageNum={}, pageSize={}, warehouseId={}, productId={}, keyWord={}",
                query.getPage(), query.getSize(), query.getWarehouseId(),
                query.getProductId(), query.getKeyWord());
        return Result.success(inventoryService.getInventoryPage(query));
    }

    /**
     * 查询库存列表（无分页，用于下拉）
     */
    @GetMapping("/list")
    @Operation(summary = "查询库存列表（无分页）", description = "查询库存列表，下拉用")
    public Result<List<InventoryVO>> getList(InventoryQuery query) {
        log.info("[接口] 查询库存列表被调用: warehouseId={}, productId={}",
                query.getWarehouseId(), query.getProductId());
        return Result.success(inventoryService.getInventoryList(query));
    }

    /**
     * 根据ID查询库存详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询库存详情", description = "根据库存ID查询详情")
    public Result<InventoryVO> getById(@PathVariable Long id) {
        log.info("[接口] 根据ID查询库存详情被调用: id={}", id);
        return Result.success(inventoryService.getInventoryById(id));
    }

    /**
     * 根据仓库+商品查询库存
     */
    @GetMapping("/by-warehouse-product")
    @Operation(summary = "根据仓库+商品查询库存", description = "根据仓库ID和商品ID查询库存，供其他模块调用")
    public Result<InventoryVO> getByWarehouseAndProduct(@RequestParam Long warehouseId, @RequestParam Long productId) {
        log.info("[接口] 根据仓库+商品查询库存被调用: warehouseId={}, productId={}", warehouseId, productId);
        return Result.success(inventoryService.getByWarehouseAndProduct(warehouseId, productId));
    }

    /**
     * 调整库存（核心接口）
     */
    @PostMapping("/adjust")
    @OperLog(module = "库存管理", operation = "调整库存")
    @Operation(summary = "调整库存", description = "调整库存，支持入库、出库、覆盖三种模式")
    public Result<Void> adjust(@RequestBody InventoryDTO dto) {
        log.info("[接口] 调整库存：\n{}", JsonLogUtil.toPrettyJson(dto));
        inventoryService.adjustInventory(dto);
        return Result.success();
    }
}
