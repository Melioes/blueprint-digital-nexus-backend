package com.melioes.blueprintdigitalnexus.common.query;

import lombok.Data;

@Data
public class PageQuery  {
//    private int page = 1;
//    private int size = 10;

    /** 页码，默认1 */
    private Integer page = 1;

    /** 每页条数，默认10 */
    private Integer size = 10;


    /**
     * 获取受保护的页码：确保不为 null 且不小于 1
     */
    public int fetchPage() {
        return (page == null || page <= 0) ? 1 : page;
    }

    /**
     * 获取受保护的每页条数：确保不为 null 且在合理范围内
     */
    public int fetchSize() {
        return (size == null || size <= 0) ? 10 : size;
    }
}