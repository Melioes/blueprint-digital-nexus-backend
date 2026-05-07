package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;

public interface ProductService extends IService<Product> {
    /**
     * 分页查询商品列表
     */
    IPage<ProductVO> getProductPage(ProductQuery query);
}