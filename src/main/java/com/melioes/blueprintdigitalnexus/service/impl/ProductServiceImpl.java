package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import com.melioes.blueprintdigitalnexus.common.utils.RedisIdGenerator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 商品业务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService, SequenceSyncService {

    @Autowired
    @Lazy // 延迟加载，防止循环依赖
    private ProductCategoryService productCategoryService;

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    /**
     * SKU 前缀常量
     */
    private static final String SKU_PREFIX = "SKU";
    private static final Pattern SKU_PATTERN = Pattern.compile("^SKU-(\\d{8})-(\\d+)$");
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("yyyyMMdd");

    /**
     * 分页查询商品列表
     *
     * @param query 查询条件（关键词、分类ID、分页参数）
     * @return 商品分页数据
     */
    @Override
    public IPage<ProductVO> getProductPage(ProductQuery query) {
        // 处理分页参数默认值
        // int page = query.getPage() == null ? 1 : query.getPage();
        // int size = query.getSize() == null ? 10 : query.getSize();
        //
        // Page<Product> pageParams = new Page<>(page, size);
        // 优化成公共方法 手动在
        Page<Product> pageParams = new Page<>(query.fetchPage(), query.fetchSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, query.getKeyword())
                    .or()
                    .like(Product::getSkuCode, query.getKeyword())
                    .or()
                    .like(Product::getBarcode, query.getKeyword()));
        }
        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }
        // 上架状态筛选
        if (query.getPublishStatus() != null) {
            wrapper.eq(Product::getPublishStatus, query.getPublishStatus());
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);
        // 执行分页查询
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

        // 校验分类是否存在
        if (dto.getCategoryId() != null) {
            productCategoryService.getAndCheckCategory(dto.getCategoryId());
        }

        // 生成SKU码，最多重试 3 次防止冲突
        String skuCode = generateSkuCodeWithRetry(3);
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
     * 生成SKU码，带重试机制
     *
     * @param maxRetries 最大重试次数
     * @return SKU码
     */
    private String generateSkuCodeWithRetry(int maxRetries) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            String code = generateSkuCode();

            // 双重校验：检查数据库中是否已存在此编码
            Long count = this.lambdaQuery()
                    .eq(Product::getSkuCode, code)
                    .count();

            if (count == 0) {
                log.info("生成编码成功: code={}, retry={}", code, retryCount);
                return code;
            }

            log.warn("编码冲突，准备重试: code={}, retry={}", code, retryCount);
            retryCount++;
        }

        // 重试多次仍然失败，使用数据库兜底模式强制生成
        log.error("重试多次仍然失败，使用数据库兜底模式");
        String dateStr = java.time.LocalDate.now().format(DATE_FORMATTER);
        int dbMaxSequence = getTodayMaxSequence(dateStr);
        String fallbackCode = String.format("%s-%s-%03d", SKU_PREFIX, dateStr, dbMaxSequence + 1);
        log.info("兜底编码: code={}", fallbackCode);
        return fallbackCode;
    }

    /**
     * 生成SKU码（单次）
     */
    private String generateSkuCode() {
        long sequence = redisIdGenerator.generateId(SKU_PREFIX);
        String dateStr = java.time.LocalDate.now().format(DATE_FORMATTER);
        return String.format("%s-%s-%03d", SKU_PREFIX, dateStr, sequence);
    }

    // ==================== SequenceSyncService 接口实现 ====================

    @Override
    public String getBusinessPrefix() {
        return SKU_PREFIX;
    }

    @Override
    public int getTodayMaxSequence(String dateStr) {
        // 查询今日创建的所有商品
        java.time.LocalDateTime startOfDay = java.time.LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Product> products = this.lambdaQuery()
                .ge(Product::getCreateTime, startOfDay)
                .lt(Product::getCreateTime, endOfDay)
                .list();

        // 从编码中提取最大序号
        int maxSequence = 0;
        for (Product product : products) {
            String code = product.getSkuCode();
            if (code != null) {
                Matcher matcher = SKU_PATTERN.matcher(code);
                if (matcher.matches()) {
                    String seqStr = matcher.group(2);
                    try {
                        int seq = Integer.parseInt(seqStr);
                        if (seq > maxSequence) {
                            maxSequence = seq;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析编码序号失败: code={}", code);
                    }
                }
            }
        }

        log.debug("统计今日最大序号: date={}, max={}", dateStr, maxSequence);
        return maxSequence;
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

        // 校验分类是否存在
        if (productDto.getCategoryId() != null) {
            productCategoryService.getAndCheckCategory(productDto.getCategoryId());
        }

        // 保存原有的 skuCode，不允许修改
        String originalSkuCode = product.getSkuCode();

        BeanUtils.copyProperties(productDto, product);

        // 恢复原有的 skuCode
        product.setSkuCode(originalSkuCode);

        this.updateById(product);
        log.info("OK: 商品修改成功, productId={}, skuCode={}", productDto.getProductId(), originalSkuCode);
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
