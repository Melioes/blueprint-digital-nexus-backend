package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.query.ProductCategoryQuery;
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

    /**
     * 获取分类下拉列表（平铺结构）
     * @param query 查询参数
     * @return 分类 VO 列表
     */
    List<ProductCategoryVO> getCategoryDropdown(ProductCategoryQuery query);

    /**
     * 获取分类分页列表
     * @param query 查询参数
     * @return 分类 VO 列表
     */
    IPage<ProductCategoryVO> getCategoryPage(ProductCategoryQuery query);
}
