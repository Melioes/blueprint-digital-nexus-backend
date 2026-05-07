package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
/**
 * 角色查询参数 继承 PageQuery 以获得 page 和 size 属性
 */
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