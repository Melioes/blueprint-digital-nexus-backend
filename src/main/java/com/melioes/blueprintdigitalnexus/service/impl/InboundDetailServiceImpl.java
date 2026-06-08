package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.wms.InboundConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.dto.InboundDetailDTO;
import com.melioes.blueprintdigitalnexus.entity.InboundDetail;
import com.melioes.blueprintdigitalnexus.entity.Product;
import com.melioes.blueprintdigitalnexus.mapper.InboundDetailMapper;
import com.melioes.blueprintdigitalnexus.service.InboundDetailService;
import com.melioes.blueprintdigitalnexus.service.ProductService;
import com.melioes.blueprintdigitalnexus.vo.InboundDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 入库单明细业务实现类
 * <p>
 * 职责：纯数据操作层，不参与业务决策。
 * 提供明细的增、删、查能力，由上层 InboundOrderServiceImpl 调用。
 * </p>
 */
@Slf4j
@Service
public class InboundDetailServiceImpl extends ServiceImpl<InboundDetailMapper, InboundDetail>
        implements InboundDetailService {

    @Autowired
    private ProductService productService;

    /**
     * 根据入库单ID查询明细（包含商品信息）
     * <p>
     * 实现思路：先查明细列表，再批量查询关联商品（用 Map 避免 N+1），最后组装 VO。
     * 为空时返回空 List 而非 null，方便调用方直接遍历。
     * </p>
     *
     * @param orderId 入库单ID（必填）
     * @return 入库单明细列表（含商品名称、SKU编码）
     */
    @Override
    public List<InboundDetailVO> listByOrderId(Long orderId) {
        log.debug("查询入库单明细: orderId={}", orderId);

        // 1. 查询明细列表
        List<InboundDetail> detailList = this.lambdaQuery()
                .eq(InboundDetail::getInboundOrderId, orderId)
                .list();

        if (CollectionUtils.isEmpty(detailList)) {
            return new ArrayList<>();
        }

        // 2. 批量查询商品信息（一次查询代替 N 次）
        Map<Long, Product> productMap = fetchProductMap(detailList);

        // 3. 转换为 VO 并关联商品信息
        return detailList.stream()
                .map(detail -> convertToVO(detail, productMap))
                .collect(Collectors.toList());
    }

    /**
     * 批量新增明细
     * <p>
     * 实现思路：先校验数据合法性（明细非空、商品存在、数量>0），再转换为实体批量写入。
     * 加 @Transactional 保证全部写入或全部回滚。
     * </p>
     *
     * @param orderId 入库单ID（必填）
     * @param details 明细列表（必填，不可为空）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(Long orderId, List<InboundDetailDTO> details) {
        log.debug("批量新增入库单明细: orderId={}, detailCount={}", orderId, details.size());

        // 1. 校验明细列表非空
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException(InboundConstant.DETAILS_REQUIRED);
        }

        // 2. 校验所有商品是否存在（利用 MP 的 count 轻量查询）
        validateProductsExist(details);

        // 3. 转换为实体并批量保存（同时校验数量>0）
        List<InboundDetail> detailList = details.stream()
                .map(dto -> toEntity(orderId, dto))
                .collect(Collectors.toList());

        this.saveBatch(detailList);
    }

    /**
     * 根据入库单ID删除明细
     * <p>
     * 使用 lambdaUpdate().remove() 直接按条件删除，避免先查再删的两次 DB 交互。
     * </p>
     *
     * @param orderId 入库单ID（必填）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByOrderId(Long orderId) {
        log.debug("删除入库单明细: orderId={}", orderId);
        if (orderId == null) {
            throw new BusinessException(InboundConstant.ORDER_ID_REQUIRED);
        }
        // 使用 lambdaUpdate 直接条件删除，比 lambdaQuery + removeById 效率更高
        this.lambdaUpdate()
                .eq(InboundDetail::getInboundOrderId, orderId)
                .remove();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 批量查询商品，构建 productId -> Product 映射
     * <p>
     * 设计目的：将"从明细列表提取 productIds → 批量查询 → 构建 Map"这个重复模式封装起来，
     * 避免每次调用方手动写 stream().map().collect(toSet()) 的样板代码。
     * </p>
     *
     * @param detailList 明细实体列表
     * @return productId 到 Product 的映射，key 为商品ID，value 为商品实体
     */
    private Map<Long, Product> fetchProductMap(List<InboundDetail> detailList) {
        Set<Long> productIds = detailList.stream()
                .map(InboundDetail::getProductId)
                .collect(Collectors.toSet());

        List<Product> productList = productService.lambdaQuery()
                .in(Product::getProductId, productIds)
                .list();

        return productList.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));
    }

    /**
     * 校验明细中的商品是否全部存在
     * <p>
     * 用 MP 的 count + in 查询一次确认所有商品ID有效性，
     * count 结果与传入 ID 总数不一致即判定存在无效商品。
     * </p>
     *
     * @param details 明细DTO列表
     */
    private void validateProductsExist(List<InboundDetailDTO> details) {
        Set<Long> productIds = details.stream()
                .map(InboundDetailDTO::getProductId)
                .collect(Collectors.toSet());

        long productCount = productService.lambdaQuery()
                .in(Product::getProductId, productIds)
                .count();

        if (productCount != productIds.size()) {
            throw new BusinessException(InboundConstant.PRODUCT_PARTIAL_NOT_FOUND);
        }
    }

    /**
     * DTO 转 Entity，同时校验数量合法性
     *
     * @param orderId 入库单ID
     * @param dto     明细DTO
     * @return 明细实体
     */
    private InboundDetail toEntity(Long orderId, InboundDetailDTO dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException(InboundConstant.INVALID_QUANTITY);
        }
        InboundDetail detail = new InboundDetail();
        detail.setInboundOrderId(orderId);
        detail.setProductId(dto.getProductId());
        detail.setQuantity(dto.getQuantity());
        return detail;
    }

    /**
     * 将明细实体转换为视图对象，并关联商品名称和SKU编码
     * <p>
     * 设计思路：接收预查询好的 productMap 而非在方法内查询，
     * 这样批量转换时只需要一次 DB 查询，避免 N+1 问题。
     * </p>
     *
     * @param detail     明细实体
     * @param productMap 商品ID到商品实体的映射
     * @return 明细视图对象（含商品名称、SKU编码）
     */
    private InboundDetailVO convertToVO(InboundDetail detail, Map<Long, Product> productMap) {
        InboundDetailVO vo = new InboundDetailVO();
        BeanUtils.copyProperties(detail, vo);

        // 从预查询的 Map 中获取商品信息，避免额外 DB 查询
        Product product = productMap.get(detail.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setSkuCode(product.getSkuCode());
        }

        return vo;
    }
}