package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.MenuDTO;
import com.melioes.blueprintdigitalnexus.entity.SysMenu;
import com.melioes.blueprintdigitalnexus.vo.MenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单树
     */
    List<MenuVO> getMenuTree();

    /**
     * 获取菜单详情
     */
    SysMenu getMenuById(Long menuId);

    /**
     * 新增菜单
     */
    void addMenu(MenuDTO dto);

    /**
     * 修改菜单
     */
    void updateMenu(MenuDTO dto);

    /**
     * 删除菜单
     */
    void deleteMenu(Long menuId);

    /**
     * 获取用户菜单树
     */
    List<MenuVO> getUserMenuTree(Long userId);
}
