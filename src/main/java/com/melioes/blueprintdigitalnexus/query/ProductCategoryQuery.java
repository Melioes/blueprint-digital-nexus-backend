package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategoryQuery extends PageQuery {
    /**
     * 搜索关键词（模糊匹配名称或编码）
     */
    private String keyword;
    /**
     * 状态过滤
     */
    private Integer status;


    /** 检查这个字段是否存在 */
    private Long parentId;


    /**
     * 下拉框标识 true 开启业务守卫
     */
    private Boolean dropdown;
}