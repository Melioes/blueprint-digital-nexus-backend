package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;

@Data
public class MenuDTO {
    //编辑需要
    private Long menuId;
    private String menuName;
    private Long parentId;
    private String path;
    private String component;
    private Integer type;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer status;
}