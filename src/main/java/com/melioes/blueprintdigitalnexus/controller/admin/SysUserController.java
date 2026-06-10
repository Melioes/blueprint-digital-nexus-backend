package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.alibaba.fastjson.JSON;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.query.UserQuery;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
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
        log.info("[接口] 新增用户：\n{}", JSON.toJSONString(dto, true));
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
        log.info("[接口] 修改用户：\n{}", JSON.toJSONString(dto, true));
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
}
