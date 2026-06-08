package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * 角色视图对象
 * 用于返回给前端展示的角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
}