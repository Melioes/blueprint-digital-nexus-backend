package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;

import java.util.List;

public interface ProductCategoryService  extends IService<ProductCategory> {

    /**
     * 获取分类的树形结构
     */
    List<ProductCategoryVO> getCategoryTree();
}
