package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.WarehouseDTO;
import com.melioes.blueprintdigitalnexus.query.WarehouseQuery;
import com.melioes.blueprintdigitalnexus.service.WarehouseService;
import com.melioes.blueprintdigitalnexus.vo.WarehouseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理控制器
 */
@RestController
@RequestMapping("/admin/warehouse")
@Tag(name = "仓库管理", description = "仓库管理接口")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 分页查询仓库列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询仓库", description = "根据条件分页查询仓库列表")
    public Result<IPage<WarehouseVO>> page(WarehouseQuery query) {
        return Result.success(warehouseService.getWarehousePage(query));
    }

    /**
     * 查询仓库列表（无分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询仓库列表", description = "查询所有仓库列表（用于下拉选择）")
    public Result<List<WarehouseVO>> list(WarehouseQuery query) {
        return Result.success(warehouseService.getWarehouseList(query));
    }

    /**
     * 查询仓库详情
     */
    @GetMapping("/{warehouseId}")
    @Operation(summary = "查询仓库详情", description = "根据ID查询仓库详情")
    public Result<WarehouseVO> getById(@PathVariable Long warehouseId) {
        return Result.success(warehouseService.getWarehouseById(warehouseId));
    }

    /**
     * 新增仓库
     */
    @PostMapping
    @Operation(summary = "新增仓库", description = "创建新仓库")
    public Result<Void> add(@RequestBody @Valid WarehouseDTO dto) {
        warehouseService.addWarehouse(dto);
        return Result.success();
    }

    /**
     * 修改仓库
     */
    @PutMapping
    @Operation(summary = "修改仓库", description = "更新仓库信息")
    public Result<Void> update(@RequestBody WarehouseDTO dto) {
        warehouseService.updateWarehouse(dto);
        return Result.success();
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/{warehouseId}")
    @Operation(summary = "删除仓库", description = "根据ID删除仓库")
    public Result<Void> delete(@PathVariable Long warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        return Result.success();
    }
}
