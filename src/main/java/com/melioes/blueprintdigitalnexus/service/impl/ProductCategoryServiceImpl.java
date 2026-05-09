package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.mapper.ProductCategoryMapper;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    @Autowired
    @Lazy
    private ProductService productService;
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
     * 添加分类
     *
     * @param productCategoryDTO
     */
    @Override
    public void addCategory(ProductCategoryDTO productCategoryDTO) {
        // 1. 获取父级分类
        Long parentId = productCategoryDTO.getParentId();
        // 判断父级分类是否存在
        if(parentId!= null && parentId > 0 ) {
            // 查询父级分类
            this.getAndCheckCategory(parentId);
        }
        if(productCategoryDTO.getStatus() == null) {
            productCategoryDTO.setStatus(ProductConstant.PUBLISH);
        }
        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryDTO, productCategory);
        this.save(productCategory);
        log.info("OK: 分类新增成功, ID: {}", productCategory.getCategoryId());
    }

    /**
     * 删除分类
     *
     * @param id
     */
    @Override
    public  void deleteCategory(Long id) {
        this.getAndCheckCategory(id);
        // 1. 获取分类id（已存在检查）
        Long count = this.lambdaQuery().eq(ProductCategory::getParentId, id).count();
        // 判断分类下是否存在子分类
        if (count > 0) {
            log.warn("FAIL: 删除失败，分类下存在子分类, ID: {}", id);
            throw new BusinessException(ProductConstant.CATEGORY_HAS_CHILDREN);
        }
        //判断是否有商品关联
        Long productCount = productService.lambdaQuery().eq(Product::getCategoryId, id).count();
        if (productCount > 0) {
            log.warn("FAIL: 删除失败，分类下有关联商品, ID: {}, 商品数: {}", id, productCount);
            throw new BusinessException(ProductConstant.CATEGORY_HAS_PRODUCTS);
        }

        this.removeById(id);
        log.info("OK: 分类删除完成, ID: {}", id);

    }

    /**
     * 修改分类
     *
     * @param dto
     */
    @Override
    public void updateCategory(ProductCategoryDTO dto) {
        // 1. 获取分类id
        ProductCategory andCheckCategory = this.getAndCheckCategory(dto.getCategoryId());
        // 2. 防止循环引用：不能把自己设为父节点
        if (dto.getCategoryId().equals(dto.getParentId())) {
            throw new BusinessException(ProductConstant.CATEGORY_SELF_REFERENCING);
        }
        // 3. 增强校验：如果修改了父分类，确保新父类是真实的
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            this.getAndCheckCategory(dto.getParentId());
        }
        BeanUtils.copyProperties(dto, andCheckCategory);

        this.updateById(andCheckCategory);
        log.info("OK: 分类修改成功, ID: {}", dto.getCategoryId());
    }

    /**
     *
     * 校验分类id是否存在
     * @param id
     * @return ProductCategory
     */
    private ProductCategory getAndCheckCategory(Long id) {
        ProductCategory productCategory = this.getById(id);
        if(productCategory == null) {
            log.warn("FAIL: 业务检查失败，分类不存在, ID: {}", id);
            throw new BusinessException(ProductConstant.CATEGORY_NOT_FOUND);
        }
        return productCategory;
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
