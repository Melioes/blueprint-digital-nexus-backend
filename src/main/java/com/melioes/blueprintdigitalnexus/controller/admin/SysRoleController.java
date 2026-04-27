package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.query.RoleQuery;
import com.melioes.blueprintdigitalnexus.service.SysRoleService;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> add(@RequestBody RoleDTO role) {
        log.info("新增角色请求: {}", role);
        sysRoleService.addRole(role);
        return Result.success();
    }

    /**
     * 删除角色（逻辑校验）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除角色请求, id={}", id);
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @PutMapping
    public Result<Void> update(@RequestBody RoleDTO  role) {
        log.info("修改角色请求: {}", role);
        sysRoleService.updateRole(role);
        return Result.success();
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/page")
    public Result<IPage<RoleVO>> page(RoleQuery query) {
        log.info("分页查询角色: page={}, size={}, keyword={}, status={}",
                query.getPage(),
                query.getSize(),
                query.getKeyword(),
                query.getStatus()
        );

        // 直接使用 Query 对象（包含 page / size / keyword / status）
        return Result.success(sysRoleService.getRolePage(query));
    }

    /**
     * 根据 ID 查询角色 前端点击编辑按钮回显ID
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        log.info("查询角色详情, id={}", id);
        return Result.success(sysRoleService.getRoleById(id));
    }


    /**
     * 查询角色列表（无分页）
     */
    @GetMapping("/list")
    public Result<List<RoleVO>> list(RoleQuery query) {
        log.info("查询角色下拉列表: status={}, keyword={}, dropdown={}",
                query.getStatus(),
                query.getKeyword(),
                query.getDropdown()
        );
        return Result.success(sysRoleService.getRoleList(query));
    }
}
