package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.utils.CodeGenerator;
import com.melioes.blueprintdigitalnexus.dto.ProductCategoryDTO;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.mapper.ProductCategoryMapper;
import com.melioes.blueprintdigitalnexus.query.ProductCategoryQuery;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类业务实现类
 */
@Slf4j
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    @Autowired
    @Lazy
    private ProductService productService;

    private static final Long TOP_PARENT_ID = 0L;
    private static final Integer DEFAULT_SORT = 0;
    private static final String CATEGORY_PREFIX = "CATE";

    /**
     * 获取分类树形结构
     *
     * @return 分类树形数据
     */
    @Override
    public List<ProductCategoryVO> getCategoryTree() {
        List<ProductCategory> allList = this.list();
        if (allList == null || allList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<ProductCategory>> parentMap = allList.stream()
                .collect(
                        Collectors.groupingBy(item -> item.getParentId() == null ? TOP_PARENT_ID : item.getParentId()));

        return allList.stream()
                .filter(item -> TOP_PARENT_ID.equals(item.getParentId()) || item.getParentId() == null)
                .map(item -> convertToVO(item, parentMap))
                .sorted(Comparator.comparing(ProductCategoryVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    /**
     * 新增分类
     *
     * @param productCategoryDTO 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCategory(ProductCategoryDTO productCategoryDTO) {
        log.info("新增分类: categoryName={}, parentId={}", productCategoryDTO.getCategoryName(), productCategoryDTO.getParentId());

        if (productCategoryDTO.getParentId() == null) {
            productCategoryDTO.setParentId(TOP_PARENT_ID);
        }
        if (productCategoryDTO.getSort() == null) {
            productCategoryDTO.setSort(DEFAULT_SORT);
        }

        if (productCategoryDTO.getParentId() > 0) {
            this.getAndCheckCategory(productCategoryDTO.getParentId());
        }

        int todayCount = countTodayCategories();
        String categoryCode = CodeGenerator.generate(CATEGORY_PREFIX, todayCount);
        productCategoryDTO.setCategoryCode(categoryCode);

        if (productCategoryDTO.getStatus() == null) {
            productCategoryDTO.setStatus(ProductConstant.PUBLISH);
        }

        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryDTO, productCategory);
        this.save(productCategory);

        log.info("OK: 分类新增成功, ID: {}, 名称: {}, 编码: {}", productCategory.getCategoryId(),
                productCategory.getCategoryName(), productCategory.getCategoryCode());
    }

    /**
     * 统计今日已创建的分类数量
     *
     * @return 今日分类数量
     */
    private int countTodayCategories() {
        String todayDateStr = CodeGenerator.getTodayDateStr();
        String likePattern = CATEGORY_PREFIX + "-" + todayDateStr + "%";
        return Math.toIntExact(this.lambdaQuery()
                .likeRight(ProductCategory::getCategoryCode, likePattern)
                .count());
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCategory(Long id) {
        log.info("删除分类: id={}", id);

        this.getAndCheckCategory(id);

        Long count = this.lambdaQuery().eq(ProductCategory::getParentId, id).count();
        if (count > 0) {
            log.warn("FAIL: 删除拦截，分类下有子节点, ID: {}", id);
            throw new BusinessException(ProductConstant.CATEGORY_HAS_CHILDREN);
        }

        Long productCount = productService.lambdaQuery().eq(Product::getCategoryId, id).count();
        if (productCount > 0) {
            log.warn("FAIL: 删除拦截，分类下有商品, ID: {}, 数量: {}", id, productCount);
            throw new BusinessException(ProductConstant.CATEGORY_HAS_PRODUCTS);
        }

        this.removeById(id);
        log.info("OK: 分类删除成功, ID: {}", id);
    }

    /**
     * 修改分类
     *
     * @param dto 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCategory(ProductCategoryDTO dto) {
        log.info("修改分类: categoryId={}", dto.getCategoryId());

        ProductCategory entity = this.getAndCheckCategory(dto.getCategoryId());

        if (dto.getParentId() == null) {
            dto.setParentId(TOP_PARENT_ID);
        }

        if (dto.getCategoryId().equals(dto.getParentId())) {
            throw new BusinessException(ProductConstant.CATEGORY_SELF_REFERENCING);
        }

        if (dto.getParentId() > 0) {
            this.getAndCheckCategory(dto.getParentId());
        }

        checkCategoryCodeUnique(dto.getCategoryCode(), dto.getCategoryId());

        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
        log.info("OK: 分类更新成功, ID: {}", dto.getCategoryId());
    }

    /**
     * 获取分类下拉列表
     *
     * @param query 查询条件
     * @return 分类列表
     */
    @Override
    public List<ProductCategoryVO> getCategoryDropdown(ProductCategoryQuery query) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();

        if (Boolean.TRUE.equals(query.getDropdown())) {
            wrapper.eq(ProductCategory::getStatus, ProductConstant.PUBLISH);
        } else {
            wrapper.eq(query.getStatus() != null, ProductCategory::getStatus, query.getStatus());
        }

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(ProductCategory::getCategoryName, query.getKeyword())
                    .or()
                    .like(ProductCategory::getCategoryCode, query.getKeyword()));
        }

        wrapper.orderByAsc(ProductCategory::getSort);

        return this.list(wrapper).stream().map(entity -> {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询分类列表
     *
     * @param query 查询条件
     * @return 分类分页数据
     */
    @Override
    public IPage<ProductCategoryVO> getCategoryPage(ProductCategoryQuery query) {
        // 处理分页参数默认值
        int pageNum = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getSize() == null ? 10 : query.getSize();

        IPage<ProductCategory> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(ProductCategory::getCategoryName, query.getKeyword())
                    .or()
                    .like(ProductCategory::getCategoryCode, query.getKeyword()));
        }

        wrapper.eq(query.getStatus() != null, ProductCategory::getStatus, query.getStatus());
        wrapper.eq(query.getParentId() != null, ProductCategory::getParentId, query.getParentId());

        wrapper.orderByAsc(ProductCategory::getSort)
                .orderByDesc(ProductCategory::getCreateTime);

        return this.page(page, wrapper).convert(entity -> {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    /**
     * 守卫方法：确保分类存在，否则直接报错
     *
     * @param id 分类ID
     * @return 分类实体
     */
    private ProductCategory getAndCheckCategory(Long id) {
        ProductCategory category = this.getById(id);
        if (category == null) {
            log.warn("FAIL: 业务检查失败，分类不存在, ID: {}", id);
            throw new BusinessException(ProductConstant.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    /**
     * 将分类实体转换为视图对象（含子节点）
     *
     * @param entity    分类实体
     * @param parentMap 父子关系映射
     * @return 分类视图对象
     */
    private ProductCategoryVO convertToVO(ProductCategory entity, Map<Long, List<ProductCategory>> parentMap) {
        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(entity, vo);

        List<ProductCategory> childrenEntities = parentMap.getOrDefault(entity.getCategoryId(), new ArrayList<>());

        List<ProductCategoryVO> childrenVOs = childrenEntities.stream()
                .map(c -> convertToVO(c, parentMap))
                .sorted(Comparator.comparing(ProductCategoryVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        vo.setChildren(childrenVOs);
        return vo;
    }

    /**
     * 校验分类编码是否重复
     *
     * @param categoryCode 分类编码
     * @param categoryId   分类ID
     */
    private void checkCategoryCodeUnique(String categoryCode, Long categoryId) {
        if (!StringUtils.hasText(categoryCode)) {
            return;
        }

        Long count = this.lambdaQuery()
                .eq(ProductCategory::getCategoryCode, categoryCode)
                .ne(categoryId != null, ProductCategory::getCategoryId, categoryId)
                .count();

        if (count > 0) {
            log.warn("FAIL: 分类编码查重未通过 -> 试图占用的编码: {}, 当前分类ID: {}", categoryCode, categoryId);
            throw new BusinessException(String.format(ProductConstant.CATEGORY_CODE_ALREADY_EXISTS, categoryCode));
        }
    }
}
