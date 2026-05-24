package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 角色视图对象
 * 用于返回给前端展示的角色信息
 */
@Data
public class RoleVO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
}