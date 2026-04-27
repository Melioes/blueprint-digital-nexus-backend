package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;

@Data
public class UserQuery extends PageQuery {
    private String keyword;
}