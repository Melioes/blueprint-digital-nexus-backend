package com.melioes.blueprintdigitalnexus.controller.admin;


import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresPermission;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @return
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    @RequiresPermission("product:category:view") // 对应你SQL中的权限标识[cite: 5]
    public Result<List<ProductCategoryVO>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
