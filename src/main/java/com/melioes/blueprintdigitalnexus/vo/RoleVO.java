package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RoleVO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
}