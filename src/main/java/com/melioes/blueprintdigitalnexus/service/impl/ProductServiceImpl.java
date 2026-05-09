package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.ProductConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.dto.ProductDTO;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.mapper.ProductMapper;
import com.melioes.blueprintdigitalnexus.query.ProductQuery;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    /**
     * 商品列表
     *
     * @param query 查询条件（关键词、分类ID、分页参数）
     * @return 商品分页数据
     */
    @Override
    public IPage<ProductVO> getProductPage(ProductQuery query) {
        // 使用 PageQuery 中定义的 page 和 size
        Page<Product> pageParams = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 模糊搜索关键词
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, query.getKeyword())
                    .or()
                    .like(Product::getSkuCode, query.getKeyword()));
        }

        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }

        IPage<Product> result = this.page(pageParams, wrapper);

        return result.convert(entity -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }
    /**
     * 添加商品
     *
      * @param dto 商品信息
     */
    @Override
    public void addProduct(ProductDTO dto) {
        // 1. 校验SKU是否重复
        checkSkuUnique(dto.getSkuCode(), null);
        // 2. 转换并保存
        Product product = new Product();
        // 转换
        BeanUtils.copyProperties(dto, product);
        // 3. 状态自动补充：如果没传状态，默认设为“已上架” (使用常量)
        if (product.getPublishStatus() == null) {
            product.setPublishStatus(ProductConstant.PUBLISH);
        }

        this.save(product);
        // 2. 关键：记录落库后的真实 ID，方便回溯
        log.info("OK: 商品已持久化, 数据库分配ID: {}, SKU: {}", product.getProductId(), product.getSkuCode());
    }


    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @Override
    public void deleteById(Long id) {
        //  获取商品是否存在
        Product product = getAndCheckProduct(id);
        //  删除商品
        this.removeById(product);
        log.info("OK: 商品删除完成, ID: {}", id);

    }
    /**
     * 修改商品
     *
     * @param productDto 商品信息
     */
    @Override
    public void updateProduct(ProductDTO productDto) {
        // 1. 获取商品是否存在
        Product product = getAndCheckProduct(productDto.getProductId());
        checkSkuUnique(productDto.getSkuCode(), productDto.getProductId());
        // 3. 增强校验：如果修改了分类，检查新分类是否存在 (调用分类 Service 或 Mapper)
        if (productDto.getCategoryId() != null) {
            // 这里可以调用 categoryService.getById() 检查
            log.info("检查关联分类是否存在: {}", productDto.getCategoryId());
        }
        // 转换
        BeanUtils.copyProperties(productDto,product);
        // 保存
        this.updateById(product);
        log.info("OK: 商品信息更新成功, ID: {}", productDto.getProductId());
    }
    /**
     * 校验SKU是否重复
     *
     * @param skuCode  SKU编码
     * @param productId 商品ID
     */
    private void checkSkuUnique(String skuCode, Long productId) {
        // 查询SKU是否重复 动态拼接查询条件productId != null
        Long count = this.lambdaQuery().eq(Product::getSkuCode, skuCode).ne(productId != null, Product::getProductId, productId).count();
        // 重复则抛出异常
        if (count > 0) {
            log.warn("FAIL: SKU 查重未通过 -> 试图占用的编号: {}, 当前商品ID: {}", skuCode, productId);
            throw new BusinessException(ProductConstant.SKU_ALREADY_EXISTS);
        }
    }


    /**
     * 这是一个“守卫”方法：确保商品存在，否则直接报错
     */
    private Product getAndCheckProduct(Long productId) {
        // 1. 获取商品
        Product product = this.getById(productId);
        // 2. 判断商品是否存在
        if (product == null) {
            // 记录异常尝试
            log.warn("FAIL: 业务检查失败，目标商品不存在, ID: {}", productId);
            throw new BusinessException(ProductConstant.PRODUCT_NOT_FOUND);
        }
        return product; // 顺便把查出来的对象还给人家
    }
}