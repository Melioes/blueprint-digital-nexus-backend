package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数 继承 PageQuery 以获得 page 和 size 属性
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {
    private String keyword;
}