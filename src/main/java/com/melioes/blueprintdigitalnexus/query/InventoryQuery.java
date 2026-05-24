package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存查询参数 继承PageQuery获得pageNum和pageSize字段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryQuery extends PageQuery {
    /**
     * 按仓库ID筛选
     */
    private Long warehouseId;
    /**
     * 按商品ID筛选
     */
    private Long productId;
    /**
     * 关键词搜索（搜商品名称/SKU）
     */
    private String keyWord;
}
