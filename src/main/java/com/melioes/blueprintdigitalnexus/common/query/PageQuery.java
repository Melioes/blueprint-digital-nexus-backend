package com.melioes.blueprintdigitalnexus.common.query;

import lombok.Data;

@Data
public class PageQuery {
    private int page = 1;
    private int size = 10;
}