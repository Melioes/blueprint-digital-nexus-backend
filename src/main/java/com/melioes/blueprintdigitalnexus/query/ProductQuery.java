package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;

/**
 * 商品查询参数对象
 * 继承 PageQuery 以获得 page 和 size 属性
 */
@Data
public class ProductQuery extends PageQuery {

    /**
     * 搜索关键词（模糊匹配商品名称或SKU编码）
     */
    private String keyword;

    /**
     * 分类ID（精确筛选）
     */
    private Long categoryId;

    /**
     * 发布状态（0:下架, 1:上架）
     */
    private Integer publishStatus;
}