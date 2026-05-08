package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.ProductDTO;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/admin/product")
@Tag(name = "商品管理", description = "商品管理相关接口")
public class ProductController {
    @Autowired
    private ProductService productService;
    /**
     * 商品列表
     *
     * @param query 查询条件（关键词、分类ID、分页参数）
     *  @return 商品分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "商品列表", description = "根据条件分页查询商品列表")
    //@RequiresPermission("product:list")
    public Result <IPage<ProductVO>> getProductPage(ProductQuery query) {
        log.info("分页查询商品列表: page={}, size={}, keyword={}, categoryId={}, publishStatus={}",
                query.getPage(), query.getSize(), query.getKeyword(), query.getCategoryId(), query.getPublishStatus());
        return Result.success(productService.getProductPage(query));
    }

    /**
     * 添加商品
     *
     * @param productDTO 商品信息DTO
     * @return 无返回值
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品", description = "添加商品")
    //@RequiresPermission("product:add")
    public Result<Void> addProduct(@RequestBody ProductDTO productDTO) {
        log.info("商品信息 {}", productDTO);
        productService.addProduct(productDTO);
        return Result.success();
    }
    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 无返回值
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除商品")
    //@RequiresPermission("product:delete")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除商品 {}", id);
        productService.deleteById(id);
        return Result.success();
    }
    /**
     * 修改商品
     *
     * @param productDto 商品信息DTO
     * @return 无返回值
     */
    @PutMapping("/update")
    @Operation(summary = "修改商品", description = "修改商品")
    //@RequiresPermission("product:update")
    public Result<Void>  updateProduct(@RequestBody ProductDTO productDto) {
        log.info("修改商品 {}", productDto.getProductId());
        productService.updateProduct(productDto);
        return Result.success();
    }
}
