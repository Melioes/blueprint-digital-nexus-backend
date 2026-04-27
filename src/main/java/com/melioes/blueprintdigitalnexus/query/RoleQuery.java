package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;

@Data
public class RoleQuery extends PageQuery {
    /**
     * 搜索关键字（角色名/标识）
     */
    private String keyword;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 是否用于下拉框
     * true = 只查启用 + 精简字段
     */
    private Boolean dropdown;
}