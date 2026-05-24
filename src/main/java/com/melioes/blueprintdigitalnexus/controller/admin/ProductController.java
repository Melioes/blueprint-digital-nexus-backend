package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.ProductDTO;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@Tag(name = "商品管理", description = "商品管理相关接口")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     *
     * @param query 查询条件
     * @return 商品分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询商品列表")
    public Result<IPage<ProductVO>> getProductPage(ProductQuery query) {
        return Result.success(productService.getProductPage(query));
    }

    /**
     * 查询商品列表（无分页）
     *
     * @param query 查询条件
     * @return 商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询商品列表（无分页）")
    public Result<List<ProductVO>> getList(ProductQuery query) {
        return Result.success(productService.getProductList(query));
    }

    /**
     * 查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "查询商品详情")
    public Result<ProductVO> getById(@PathVariable Long productId) {
        return Result.success(productService.getProductById(productId));
    }

    /**
     * 新增商品
     *
     * @param productDTO 商品信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增商品")
    public Result<Void> addProduct(@RequestBody @Valid ProductDTO productDTO) {
        productService.addProduct(productDTO);
        return Result.success();
    }

    /**
     * 修改商品
     *
     * @param productDto 商品信息
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改商品")
    public Result<Void> updateProduct(@RequestBody ProductDTO productDto) {
        productService.updateProduct(productDto);
        return Result.success();
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return Result.success();
    }
}
