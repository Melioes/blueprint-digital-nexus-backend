package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresPermission;
import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresRole;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.melioes.blueprintdigitalnexus.common.utils.JsonLogUtil;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.MenuDTO;
import com.melioes.blueprintdigitalnexus.dto.RoleMenuDTO;
import com.melioes.blueprintdigitalnexus.entity.SysMenu;
import com.melioes.blueprintdigitalnexus.service.SysMenuService;
import com.melioes.blueprintdigitalnexus.service.SysRoleMenuService;
import com.melioes.blueprintdigitalnexus.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/menu")
@Tag(name = "菜单管理", description = "菜单管理接口")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 新增菜单
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:menu:add
     */
    @PostMapping
    @OperLog(module = "菜单管理", operation = "新增菜单")
    @Operation(summary = "新增菜单", description = "创建新菜单")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:menu:add")
    public Result<Void> add(@RequestBody MenuDTO dto) {
        log.info("[接口] 新增菜单：\n{}", JsonLogUtil.toPrettyJson(dto));
        menuService.addMenu(dto);
        return Result.success();
    }

    /**
     * 菜单树 - 所有菜单返回
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:menu:view
     */
    @GetMapping("/tree")
    @Operation(summary = "查询菜单树", description = "查询完整的菜单树")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:menu:view")
    public Result<List<MenuVO>> tree() {
        return Result.success(menuService.getMenuTree());
    }

    /**
     * 绑定角色和菜单
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:assign
     */
    @PostMapping("/bindMenu")
    @OperLog(module = "菜单管理", operation = "分配菜单权限")
    @Operation(summary = "绑定角色菜单", description = "给角色绑定菜单权限")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:assign")
    public Result<Void> bindMenu(@RequestBody @Valid RoleMenuDTO dto) {
        log.info("绑定角色与菜单请求: roleId={}, menuIds={}", dto.getRoleId(), dto.getMenuIds());
        sysRoleMenuService.bindRoleMenus(dto.getRoleId(), dto.getMenuIds());
        log.info("角色与菜单绑定成功: roleId={}, menuIds={}", dto.getRoleId(), dto.getMenuIds());
        return Result.success();
    }

    /**
     * 删除角色和菜单的绑定
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:assign
     */
    @DeleteMapping("/unbindMenu")
    @OperLog(module = "菜单管理", operation = "解绑菜单权限")
    @Operation(summary = "解绑角色菜单", description = "移除角色绑定的菜单")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:assign")
    public Result<Void> unbindMenu(@RequestBody RoleMenuDTO dto) {
        log.info("删除角色与菜单绑定请求: roleId={}, menuIds={}", dto.getRoleId(), dto.getMenuIds());
        sysRoleMenuService.unbindRoleMenus(dto.getRoleId(), dto.getMenuIds());
        log.info("角色与菜单解绑成功: roleId={}, menuIds={}", dto.getRoleId(), dto.getMenuIds());
        return Result.success();
    }

    /**
     * 根据角色ID获取菜单ID列表
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:view
     */
    @GetMapping("/role/menus")
    @Operation(summary = "查询角色菜单ID", description = "根据角色ID查询菜单ID列表")
    public Result<List<Long>> getRoleMenuIds(@RequestParam Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new BusinessException(RoleConstant.ROLE_ID_INVALID);
        }
        return Result.success(sysRoleMenuService.getMenuIdsByRoleId(roleId));
    }

    /**
     * 菜单详情
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:menu:view
     */
    @GetMapping("/{menuId}")
    @Operation(summary = "查询菜单详情", description = "根据ID查询菜单详情")
    public Result<SysMenu> getById(@PathVariable Long menuId) {
        return Result.success(menuService.getMenuById(menuId));
    }

    /**
     * 菜单修改
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:menu:edit
     */
    @PutMapping
    @OperLog(module = "菜单管理", operation = "修改菜单")
    @Operation(summary = "修改菜单", description = "更新菜单信息")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:menu:edit")
    public Result<Void> update(@RequestBody MenuDTO dto) {
        log.info("[接口] 修改菜单：\n{}", JsonLogUtil.toPrettyJson(dto));
        menuService.updateMenu(dto);
        return Result.success();
    }

    /**
     * 菜单删除
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:menu:delete
     */
    @DeleteMapping("/{menuId}")
    @OperLog(module = "菜单管理", operation = "删除菜单")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:menu:delete")
    public Result<Void> delete(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return Result.success();
    }

    /**
     * 获取当前登录用户的菜单树
     */
    @GetMapping("/user/tree")
    @Operation(summary = "查询用户菜单树", description = "获取当前登录用户的菜单树")
    public Result<List<MenuVO>> getUserTree() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(AuthMessageConstant.NOT_LOGIN);
        }
        log.info("获取用户菜单树 userId={}", userId);
        return Result.success(menuService.getUserMenuTree(userId));
    }
}
