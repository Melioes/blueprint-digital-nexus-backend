package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.mapper.ProductCategoryMapper;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    /**
     * 获取分类树形结构
     *
     * @return
     */
    @Override
    public List<ProductCategoryVO> getCategoryTree() {
        // 1. 查询所有分类（MyBatis-Plus 自动过滤已删除的）
        List<ProductCategory> allList = this.list();

        // 2. 将 Entity 转换为树形 VO[cite: 1]
        return allList.stream()
                .filter(item -> item.getParentId() == 0) // 找出顶级分类
                .map(item -> convertToVO(item, allList))
                .sorted(Comparator.comparing(ProductCategoryVO::getSort))
                .collect(Collectors.toList());
    }
    /**
     * 递归转换
     *
     * @param entity
     * @param all
     * @return
     */
    private ProductCategoryVO convertToVO(ProductCategory entity, List<ProductCategory> all) {
        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(entity, vo);

        // 递归找子类
        List<ProductCategoryVO> children = all.stream()
                .filter(c -> c.getParentId().equals(entity.getCategoryId()))
                .map(c -> convertToVO(c, all))
                .sorted(Comparator.comparing(ProductCategoryVO::getSort))
                .collect(Collectors.toList());

        vo.setChildren(children);
        return vo;
    }
}
