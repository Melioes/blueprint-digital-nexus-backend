package com.melioes.blueprintdigitalnexus.controller.admin;


import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresPermission;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/admin/category")
@Tag(name = "商品分类管理", description = "商品分类管理相关接口")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService categoryService;
    /**
     * 获取分类树形结构
     *
     * @return
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    @RequiresPermission("product:category:view") // 对应你SQL中的权限标识[cite: 5]
    public Result<List<ProductCategoryVO>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }


    @PostMapping("/add")
    @Operation(summary = "新增分类")
    //@RequiresPermission("product:category:add")
    public Result<Void> add(@RequestBody ProductCategoryDTO categoryDto) {
        log.info("==> [API] 准备新增分类: {}, 父级ID: {}", categoryDto.getCategoryName(), categoryDto.getParentId());
        categoryService.addCategory(categoryDto);
        return Result.success();
    }

    @PutMapping("/update")
    @Operation(summary = "修改分类")
    //@RequiresPermission("product:category:edit")
    public Result<Void> update(@RequestBody ProductCategoryDTO categoryDto) {
        log.info("==> [API] 准备修改分类, ID: {}, 新名称: {}", categoryDto.getCategoryId(), categoryDto.getCategoryName());
        categoryService.updateCategory(categoryDto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    //@RequiresPermission("product:category:delete")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("==> [API] 准备删除分类, 目标ID: {}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
