package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.query.RoleQuery;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    /**
     * 获取角色列表
     * @return
     */
    IPage<RoleVO> getRolePage(RoleQuery query);
    /**
     * 新增角色
     * @param role
     */
    void addRole(RoleDTO role);
    /**
     * 删除角色
     * @param id
     */
    void deleteRole(Long id);
    /**
     * 修改角色
     * @param role
     */
    void updateRole(RoleDTO  role);

    /**
     * 根据ID查询角色详情
     */
    RoleVO getRoleById(Long id);

    /**
     * 查询角色列表（无分页）
     */
    List<RoleVO> getRoleList(RoleQuery query);
}
