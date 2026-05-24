package com.melioes.blueprintdigitalnexus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
/**
 * 角色菜单数据传输对象
 */
@Data
public class RoleMenuDTO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;  // 角色 ID
    // ❗允许 []（表示清空权限）
    @NotNull(message = "菜单列表不能为空")
    private List<Long> menuIds;  // 菜单 ID 列表
}