package com.melioes.blueprintdigitalnexus.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresPermission;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.query.ProductCategoryQuery;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Tag(name = "商品分类管理", description = "商品分类管理相关接口")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService categoryService;

    /**
     * 获取分类树形结构
     *
     * @return 分类树形数据
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    @RequiresPermission("product:category:view")
    public Result<List<ProductCategoryVO>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 新增分类
     *
     * @param categoryDto 分类信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增分类")
    //@RequiresPermission("product:category:add")
    public Result<Void> add(@RequestBody ProductCategoryDTO categoryDto) {
        categoryService.addCategory(categoryDto);
        return Result.success();
    }

    /**
     * 修改分类
     *
     * @param categoryDto 分类信息
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改分类")
    //@RequiresPermission("product:category:edit")
    public Result<Void> update(@RequestBody ProductCategoryDTO categoryDto) {
        categoryService.updateCategory(categoryDto);
        return Result.success();
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    //@RequiresPermission("product:category:delete")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 获取分类下拉列表
     *
     * @param query 查询条件
     * @return 分类列表
     */
    @GetMapping("/dropdown")
    @Operation(summary = "获取分类下拉列表")
    public Result<List<ProductCategoryVO>> getCategoryDropdown(ProductCategoryQuery query) {
        return Result.success(categoryService.getCategoryDropdown(query));
    }

    /**
     * 分页查询分类列表（平铺结构，用于后台管理表格）
     *
     * @param query 查询条件
     * @return 分类分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询分类")
    //@RequiresPermission("product:category:view")
    public Result<IPage<ProductCategoryVO>> getPage(ProductCategoryQuery query) {
        return Result.success(categoryService.getCategoryPage(query));
    }
}
