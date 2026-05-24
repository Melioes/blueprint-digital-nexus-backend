package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WarehouseQuery extends PageQuery {

    /**
     * 关键词（仓库名称/编码）
     */
    private String keyword;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 是否下拉列表（仅启用状态）
     */
    private Boolean dropdown;
}
