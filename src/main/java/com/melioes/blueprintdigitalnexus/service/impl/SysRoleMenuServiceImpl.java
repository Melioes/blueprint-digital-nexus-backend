package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.entity.SysMenu;
import com.melioes.blueprintdigitalnexus.entity.SysRoleMenu;
import com.melioes.blueprintdigitalnexus.mapper.SysMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMenuMapper;
import com.melioes.blueprintdigitalnexus.service.SysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色菜单关联服务实现类
 */
@Slf4j
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;

    /**
     * 绑定角色和菜单（覆盖模式）
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRoleMenus(Long roleId, List<Long> menuIds) {
        log.info("绑定角色菜单 roleId={}, menuIds={}", roleId, menuIds);

        if (roleId == null || roleId <= 0) {
            throw new BusinessException(RoleConstant.ROLE_ID_INVALID);
        }

        if (menuIds == null) {
            throw new BusinessException(RoleConstant.MENU_LIST_EMPTY);
        }

        // 检查所有菜单ID是否存在
        if (!menuIds.isEmpty()) {
            List<SysMenu> existingMenus = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .in(SysMenu::getMenuId, menuIds)
            );

            Set<Long> existingMenuIds = existingMenus.stream()
                    .map(SysMenu::getMenuId)
                    .collect(Collectors.toSet());

            List<Long> invalidMenuIds = menuIds.stream()
                    .filter(id -> !existingMenuIds.contains(id))
                    .toList();

            if (!invalidMenuIds.isEmpty()) {
                log.warn("绑定菜单包含无效ID: {}", invalidMenuIds);
                throw new BusinessException(RoleConstant.INVALID_MENU_IDS + ": " + invalidMenuIds);
            }
        }

        // 删除旧的绑定
        this.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));

        // 如果为空，只删除不新增
        if (menuIds.isEmpty()) {
            log.info("角色菜单绑定已清空 roleId={}", roleId);
            return;
        }

        // 插入新的绑定
        List<SysRoleMenu> list = menuIds.stream().map(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            return rm;
        }).toList();

        this.saveBatch(list);
        log.info("角色菜单绑定成功 roleId={}, menuCount={}", roleId, list.size());
    }

    /**
     * 解绑角色菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindRoleMenus(Long roleId, List<Long> menuIds) {
        log.info("解绑角色菜单 roleId={}, menuIds={}", roleId, menuIds);

        // 判断角色ID是否为空或无效
        if (roleId == null || roleId <= 0) {
            throw new BusinessException(RoleConstant.ROLE_ID_INVALID);
        }

        // 判断菜单ID列表是否为空
        if (menuIds == null || menuIds.isEmpty()) {
            throw new BusinessException(RoleConstant.MENU_LIST_EMPTY);
        }

        // 去重
        Set<Long> uniqueMenuIds = new HashSet<>(menuIds);

        // 查询现有的绑定关系
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId)
                .in(SysRoleMenu::getMenuId, uniqueMenuIds);
        List<SysRoleMenu> existingBindings = sysRoleMenuMapper.selectList(queryWrapper);

        // 如果没有找到，也提示
        if (existingBindings.isEmpty()) {
            log.warn("角色菜单不存在绑定关系 roleId={}, menuIds={}", roleId, menuIds);
            throw new BusinessException(RoleConstant.MENU_NOT_FOUND);
        }

        // 删除指定的绑定
        List<Long> existingMenuIds = existingBindings.stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .in(SysRoleMenu::getMenuId, existingMenuIds));

        log.info("角色菜单解绑成功 roleId={}, menuCount={}", roleId, existingMenuIds.size());
    }

    /**
     * 根据角色ID获取菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        log.info("获取角色菜单ID roleId={}", roleId);

        if (roleId == null || roleId <= 0) {
            throw new BusinessException(RoleConstant.ROLE_ID_INVALID);
        }

        List<Long> menuIds = this.list(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .select(SysRoleMenu::getMenuId)
                                .eq(SysRoleMenu::getRoleId, roleId)
                ).stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

        log.info("获取角色菜单ID成功 roleId={}, menuCount={}", roleId, menuIds.size());
        return menuIds;
    }
}
