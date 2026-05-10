package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.ProductDTO;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {
    /**
     * 分页查询商品列表
     *
     * @param query 查询条件
     * @return 商品分页数据
     */
    IPage<ProductVO> getProductPage(ProductQuery query);

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情VO
     */
    ProductVO getProductById(Long productId);

    /**
     * 查询商品列表（无分页）
     *
     * @param query 查询条件
     * @return 商品列表
     */
    List<ProductVO> getProductList(ProductQuery query);

    /**
     * 添加商品
     *
     * @param productDTO 商品信息DTO
     */
    void addProduct(ProductDTO productDTO);

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteById(Long id);

    /**
     * 修改商品
     *
     * @param productDto 商品信息DTO
     */
    void updateProduct(ProductDTO productDto);
}