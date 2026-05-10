package com.melioes.blueprintdigitalnexus.common.query;

import lombok.Data;

@Data
public class PageQuery {
//    private int page = 1;
//    private int size = 10;

    /** 页码，默认1 */
    private Integer page = 1;

    /** 每页条数，默认10 */
    private Integer size = 10;
}