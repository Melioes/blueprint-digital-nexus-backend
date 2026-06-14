package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.melioes.blueprintdigitalnexus.common.utils.JsonLogUtil;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.query.UserQuery;
import com.melioes.blueprintdigitalnexus.service.PermissionService;
import com.melioes.blueprintdigitalnexus.service.SysMenuService;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
import com.melioes.blueprintdigitalnexus.vo.MenuVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "用户管理", description = "用户管理接口")
@RequestMapping("/admin/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 用户分页查询
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:user:view
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
//    @RequiresRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
//    @RequiresPermission("system:user:view")
    public Result<IPage<EmployeeVO>> page(UserQuery query) {
        log.info("分页查询用户: page={}, size={}, keyword={}",
                query.getPage(),
                query.getSize(),
                query.getKeyword()
        );

        return Result.success(sysUserService.getUserPage(query));
    }

    /**
     * 新增用户
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:user:add
     */
    @PostMapping
    @OperLog(module = "用户管理", operation = "新增用户")
    @Operation(summary = "新增用户", description = "创建新用户")
//    @RequiresPermission("system:user:add")
//    @RequiresRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})

    public Result<Void> add(@RequestBody EmployeeDTO dto) {
        log.info("[接口] 新增用户：\n{}", JsonLogUtil.toPrettyJson(dto));
        sysUserService.addUser(dto);
        return Result.success();
    }

    /**
     * 删除用户
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:user:delete
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "用户管理", operation = "删除用户")
    @Operation(summary = "删除用户", description = "根据ID删除用户")
//    @RequiresRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
//    @RequiresPermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户id:{}", id);
        sysUserService.removeById(id);
        return Result.success();
    }

    /**
     * 修改用户
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:user:edit
     */
    @PutMapping
    @OperLog(module = "用户管理", operation = "修改用户")
    @Operation(summary = "修改用户", description = "更新用户信息")
//    @RequiresRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
//    @RequiresPermission("system:user:edit")
    public Result<Void> update(@RequestBody EmployeeDTO dto) {
        log.info("[接口] 修改用户：\n{}", JsonLogUtil.toPrettyJson(dto));
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 查询单个用户详情
     * 需要角色：SUPER_ADMIN, ADMIN, DEV
     * 需要权限：system:user:view
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详情")
//    @RequiresRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
//    @RequiresPermission("system:user:view")
    public Result<EmployeeVO> detail(@PathVariable Long id) {
        log.info("查询用户详情, id={}", id);
        return Result.success(sysUserService.getUserDetail(id));
    }

    /**
     * 获取当前登录用户的权限信息（供前端渲染菜单和按钮）
     *
     * 前端登录后调用此接口，拿到：
     * - roles：角色列表（如 ["SUPER_ADMIN"]）
     * - permissions：权限标识列表（如 ["dashboard:view", "product:list:view"]）
     * - menus：菜单树（用于渲染侧边栏）
     *
     * 管理员修改权限后，用户下次调用此接口自动拿到最新数据（Redis缓存2小时，改权限时主动清除）
     */
    @GetMapping("/permissions")
    @Operation(summary = "获取当前用户权限", description = "返回角色、权限标识、菜单树，供前端渲染侧边栏和控制按钮显隐")
    public Result<Map<String, Object>> getPermissions() {
        Long userId = UserContext.get();
        log.info("[接口] 获取当前用户权限: userId={}", userId);

        // 1. 查角色列表
        List<String> roles = permissionService.getUserRoles(userId);

        // 2. 查权限标识列表
        List<String> permissions = permissionService.getUserPermissions(userId);

        // 3. 查菜单树（只返回用户有权限的菜单，用于前端渲染侧边栏）
        List<MenuVO> menus = sysMenuService.getUserMenuTree(userId);

        // 4. 组装返回
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("roles", roles);
        data.put("permissions", permissions);
        data.put("menus", menus);

        return Result.success(data);
    }
}
