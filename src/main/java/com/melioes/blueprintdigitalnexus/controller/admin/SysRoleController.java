package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresPermission;
import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresRole;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.melioes.blueprintdigitalnexus.common.utils.JsonLogUtil;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.query.RoleQuery;
import com.melioes.blueprintdigitalnexus.service.SysRoleService;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@Tag(name = "角色管理", description = "角色管理接口")
@RequestMapping("/admin/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 新增角色
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:add
     */
    @PostMapping
    @OperLog(module = "角色管理", operation = "新增角色")
    @Operation(summary = "新增角色", description = "创建新角色")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:add")
    public Result<Void> add(@RequestBody RoleDTO role) {
        log.info("[接口] 新增角色：\n{}", JsonLogUtil.toPrettyJson(role));
        sysRoleService.addRole(role);
        return Result.success();
    }

    /**
     * 删除角色（逻辑校验）
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:delete
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "角色管理", operation = "删除角色")
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除角色请求, id={}", id);
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 修改角色
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:edit
     */
    @PutMapping
    @OperLog(module = "角色管理", operation = "修改角色")
    @Operation(summary = "修改角色", description = "更新角色信息")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:edit")
    public Result<Void> update(@RequestBody RoleDTO role) {
        log.info("[接口] 修改角色：\n{}", JsonLogUtil.toPrettyJson(role));
        sysRoleService.updateRole(role);
        return Result.success();
    }

    /**
     * 查询角色分页列表
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:view
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询角色", description = "根据条件分页查询角色列表")
    public Result<IPage<RoleVO>> page(RoleQuery query) {
        log.info("分页查询角色: page={}, size={}, keyword={}, status={}",
                query.getPage(),
                query.getSize(),
                query.getKeyword(),
                query.getStatus());

        return Result.success(sysRoleService.getRolePage(query));
    }

    /**
     * 根据ID查询角色详情
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:view
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询角色详情", description = "根据ID查询角色详情")
    @RequiresRole({ RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN })
    @RequiresPermission("system:role:view")
    public Result<RoleVO> getById(@PathVariable Long id) {
        log.info("查询角色详情, id={}", id);
        return Result.success(sysRoleService.getRoleById(id));
    }

    /**
     * 查询角色列表（无分页，用于下拉选择）
     * 需要角色：SUPER_ADMIN, ADMIN
     * 需要权限：system:role:view
     */
    @GetMapping("/list")
    @Operation(summary = "查询角色列表", description = "查询所有角色列表（用于下拉选择）")
    public Result<List<RoleVO>> list(RoleQuery query) {
        log.info("查询角色下拉列表: status={}, keyword={}, dropdown={}",
                query.getStatus(),
                query.getKeyword(),
                query.getDropdown());
        return Result.success(sysRoleService.getRoleList(query));
    }
}
