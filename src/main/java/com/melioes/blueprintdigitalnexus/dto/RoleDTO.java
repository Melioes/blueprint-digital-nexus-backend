package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status;


    //修改方案
//    private Long roleId;
//
//    private String roleName;
//
//    private String description;
//
//    private Integer status;

}