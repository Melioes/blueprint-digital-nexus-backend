package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.entity.SysRoleMenu;

import java.util.List;

public interface SysRoleMenuService
        extends IService<SysRoleMenu> {

    /**
     * 绑定角色菜单
     * @param roleId
     * @param menuIds
     */
    void bindRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 解绑角色菜单
     * @param roleId
     */
    void unbindRoleMenus(Long roleId,List<Long> menuIds);

    /**
     * 根据角色ID查询菜单ID列表
     * @param roleId
     * @return
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
