package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;

import java.util.List;

public interface ProductCategoryService  extends IService<ProductCategory> {

    /**
     * 获取分类的树形结构
     */
    List<ProductCategoryVO> getCategoryTree();

    /**
     * 添加分类
     */
    void addCategory(ProductCategoryDTO dto);
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    /**
     * 修改分类
     */
    void updateCategory(ProductCategoryDTO dto);
}
