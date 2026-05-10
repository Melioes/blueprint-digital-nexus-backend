package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.utils.CodeGenerator;
import com.melioes.blueprintdigitalnexus.dto.ProductDTO;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import com.melioes.blueprintdigitalnexus.mapper.ProductMapper;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.service.ProductCategoryService;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品业务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    @Lazy // 延迟加载，防止循环依赖
    private ProductCategoryService productCategoryService;

    /**
     * SKU 前缀常量
     */
    private static final String SKU_PREFIX = "SKU";

    /**
     * 分页查询商品列表
     *
     * @param query 查询条件（关键词、分类ID、分页参数）
     * @return 商品分页数据
     */
    @Override
    public IPage<ProductVO> getProductPage(ProductQuery query) {
        // 处理分页参数默认值
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();

        Page<Product> pageParams = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, query.getKeyword())
                    .or()
                    .like(Product::getSkuCode, query.getKeyword())
                    .or()
                    .like(Product::getBarcode, query.getKeyword()));
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }

        if (query.getPublishStatus() != null) {
            wrapper.eq(Product::getPublishStatus, query.getPublishStatus());
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> result = this.page(pageParams, wrapper);
        return result.convert(this::convertToProductVO);
    }

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情VO
     */
    @Override
    public ProductVO getProductById(Long productId) {
        Product product = getAndCheckProduct(productId);
        return convertToProductVO(product);
    }

    /**
     * 查询商品列表（无分页）
     *
     * @param query 查询条件
     * @return 商品列表
     */
    @Override
    public List<ProductVO> getProductList(ProductQuery query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, query.getKeyword())
                    .or()
                    .like(Product::getSkuCode, query.getKeyword())
                    .or()
                    .like(Product::getBarcode, query.getKeyword()));
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }

        if (query.getPublishStatus() != null) {
            wrapper.eq(Product::getPublishStatus, query.getPublishStatus());
        }

        wrapper.orderByDesc(Product::getCreateTime);

        return this.list(wrapper).stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    /**
     * 新增商品
     *
     * @param dto 商品信息DTO
     */
    @Override
    public void addProduct(ProductDTO dto) {
        log.info("新增商品: productName={}, categoryId={}", dto.getProductName(), dto.getCategoryId());
        // 生成SKU码
        // 获取今日已创建的商品数量
        int todayCount = countTodayProducts();
        // 生成SKU码，格式为：SKU-20230405-001
        String skuCode = CodeGenerator.generate(SKU_PREFIX, todayCount);
        dto.setSkuCode(skuCode);

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);

        if (product.getPublishStatus() == null) {
            product.setPublishStatus(ProductConstant.PUBLISH);
        }

        this.save(product);
        log.info("OK: 商品新增成功, productId={}, skuCode={}", product.getProductId(), product.getSkuCode());
    }

    /**
     * 统计今日已创建的商品数量
     *
     * @return 今日商品数量
     */
    private int countTodayProducts() {
        String todayDateStr = CodeGenerator.getTodayDateStr();
        String likePattern = SKU_PREFIX + "-" + todayDateStr + "%";
        return Math.toIntExact(this.lambdaQuery()
                .likeRight(Product::getSkuCode, likePattern)
                .count());
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @Override
    public void deleteById(Long id) {
        log.info("删除商品: id={}", id);

        Product product = getAndCheckProduct(id);
        this.removeById(product);
        log.info("OK: 商品删除成功, id={}", id);
    }

    /**
     * 修改商品
     *
     * @param productDto 商品信息DTO
     */
    @Override
    public void updateProduct(ProductDTO productDto) {
        log.info("修改商品: productId={}", productDto.getProductId());

        Product product = getAndCheckProduct(productDto.getProductId());
        checkSkuUnique(productDto.getSkuCode(), productDto.getProductId());

        if (productDto.getCategoryId() != null) {
            productCategoryService.getById(productDto.getCategoryId());
        }

        BeanUtils.copyProperties(productDto, product);
        this.updateById(product);
        log.info("OK: 商品修改成功, productId={}", productDto.getProductId());
    }

    /**
     * 校验SKU是否重复
     *
     * @param skuCode   SKU编码
     * @param productId 商品ID
     */
    private void checkSkuUnique(String skuCode, Long productId) {
        if (!StringUtils.hasText(skuCode)) {
            return;
        }

        Long count = this.lambdaQuery()
                .eq(Product::getSkuCode, skuCode)
                .ne(productId != null, Product::getProductId, productId)
                .count();

        if (count > 0) {
            log.warn("FAIL: SKU重复校验失败 -> skuCode={}, productId={}", skuCode, productId);
            throw new BusinessException(String.format(ProductConstant.SKU_ALREADY_EXISTS, skuCode));
        }
    }

    /**
     * 守卫方法：确保商品存在，否则直接报错
     *
     * @param productId 商品ID
     * @return 商品实体
     */
    private Product getAndCheckProduct(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            log.warn("FAIL: 商品不存在, productId={}", productId);
            throw new BusinessException(ProductConstant.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /**
     * 将商品实体转换为视图对象
     * 包含：关联查询分类名称、转换状态名称
     *
     * @param entity 商品实体
     * @return 商品视图对象
     */
    private ProductVO convertToProductVO(Product entity) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getCategoryId() != null) {
            ProductCategory category = productCategoryService.getById(entity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        if (ProductConstant.PUBLISH.equals(entity.getPublishStatus())) {
            vo.setPublishStatusName(ProductConstant.PUBLISH_NAME);
        } else if (ProductConstant.UNPUBLISH.equals(entity.getPublishStatus())) {
            vo.setPublishStatusName(ProductConstant.UNPUBLISH_NAME);
        }

        return vo;
    }
}
