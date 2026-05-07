package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {

    private Long menuId;
    private String menuName;
    private Long parentId;
    private String path;
    private String component;
    private Integer type;
    private String permission;
    private String icon;
    private Integer sort;

    /**
     * 子菜单（核心）
     */
    private List<MenuVO> children;
}