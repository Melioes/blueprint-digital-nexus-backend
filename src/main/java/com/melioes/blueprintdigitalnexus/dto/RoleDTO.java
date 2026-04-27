package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status;



}