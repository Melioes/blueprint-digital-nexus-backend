package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.dto.MenuDTO;
import com.melioes.blueprintdigitalnexus.entity.SysMenu;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.entity.SysRoleMenu;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import com.melioes.blueprintdigitalnexus.mapper.SysMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.service.SysMenuService;
import com.melioes.blueprintdigitalnexus.service.SysRoleMenuService;
import com.melioes.blueprintdigitalnexus.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 菜单服务实现类
 */
@Slf4j
@Service
public class SysMenuServiceImpl
        extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 获取菜单树
     */
    @Override
    public List<MenuVO> getMenuTree() {
        log.info("获取完整菜单树");

        List<SysMenu> menuList = this.lambdaQuery()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort)
                .list();

        List<MenuVO> voList = menuList.stream().map(menu -> {
            MenuVO vo = new MenuVO();
            BeanUtils.copyProperties(menu, vo);
            return vo;
        }).toList();

        return buildTree(voList, 0L);
    }

    /**
     * 获取菜单详情
     */
    @Override
    public SysMenu getMenuById(Long menuId) {
        log.info("获取菜单详情 menuId={}", menuId);

        if (menuId == null || menuId <= 0) {
            throw new BusinessException(RoleConstant.MENU_ID_INVALID);
        }

        SysMenu menu = this.getById(menuId);
        if (menu == null) {
            throw new BusinessException(RoleConstant.MENU_NOT_FOUND);
        }

        return menu;
    }

    /**
     * 新增菜单
     */
    @Override
    public void addMenu(MenuDTO dto) {
        log.info("新增菜单请求: {}", dto);

        // 1. 唯一性校验
        checkMenuUnique(dto, false);

        // 2. 保存菜单
        SysMenu menu = new SysMenu();
        // 属性复制
        BeanUtils.copyProperties(dto, menu);
        this.save(menu);

        log.info("新增菜单成功 menuId={}", menu.getMenuId());
    }

    /**
     * 修改菜单
     */
    @Override
    public void updateMenu(MenuDTO dto) {
        log.info("修改菜单请求 menuId={}", dto.getMenuId());

        if (dto.getMenuId() == null || dto.getMenuId() <= 0) {
            throw new BusinessException(RoleConstant.MENU_ID_EMPTY);
        }

        SysMenu menu = this.getById(dto.getMenuId());
        if (menu == null) {
            throw new BusinessException(RoleConstant.MENU_NOT_FOUND);
        }

        // 2. 唯一性校验
        checkMenuUnique(dto, true);

        BeanUtils.copyProperties(dto, menu);

        this.updateById(menu);
        log.info("修改菜单成功 menuId={}", dto.getMenuId());
    }

    /**
     * 删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        log.info("删除菜单请求 menuId={}", menuId);

        if (menuId == null || menuId <= 0) {
            throw new BusinessException(RoleConstant.MENU_ID_INVALID);
        }

        // 检查是否有子菜单
        long childCount = this.count(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, menuId));

        if (childCount > 0) {
            throw new BusinessException(RoleConstant.MENU_HAS_CHILDREN);
        }

        // 删除菜单
        this.removeById(menuId);

        // 删除角色-菜单绑定
        sysRoleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, menuId));

        log.info("删除菜单成功 menuId={}", menuId);
    }

    /**
     * 获取用户菜单树（根据用户角色过滤，只返回用户有权限的菜单）
     *
     * 查询逻辑：
     * 1. 通过 userId 查 sys_user_role 拿到 role_id 列表
     * 2. 过滤掉禁用的角色（status != 1）
     * 3. 通过 role_id 查 sys_role_menu 拿到 menu_id 列表
     * 4. 通过 menu_id 查 sys_menu 拿到菜单详情
     * 5. 构建树形结构返回
     *
     * @param userId 用户ID
     * @return 菜单树（只包含用户有权限的菜单）
     */
    @Override
    public List<MenuVO> getUserMenuTree(Long userId) {
        log.info("获取用户菜单树 userId={}", userId);

        if (userId == null || userId <= 0) {
            throw new BusinessException(RoleConstant.USER_ID_INVALID);
        }

        // 1. 查用户绑定的所有角色ID（包括禁用的）
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();

        if (roleIds.isEmpty()) {
            log.warn("用户 {} 无角色信息", userId);
            return Collections.emptyList();
        }

        // 2. 过滤掉禁用的角色（status != 1）
        // 例如：用户绑定了 [SUPER_ADMIN(启用), TEMP_TEST(禁用)]，只保留 SUPER_ADMIN
        roleIds = sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(r -> r.getStatus() == 1)
                .map(SysRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            log.warn("用户 {} 所有角色均已禁用", userId);
            return Collections.emptyList();
        }

        // 3. 通过启用的角色ID，查出所有绑定的菜单ID（去重）
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().toList();

        log.info("用户有权限的菜单ID：{}", menuIds);

        if (menuIds.isEmpty()) {
            log.warn("用户 {} 无菜单权限", userId);
            return Collections.emptyList();
        }

        // 4. 查菜单详情（只查启用的菜单）
        List<SysMenu> menuList = this.lambdaQuery()
                .in(SysMenu::getMenuId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort)
                .list();

        List<MenuVO> voList = menuList.stream().map(menu -> {
            MenuVO vo = new MenuVO();
            BeanUtils.copyProperties(menu, vo);
            return vo;
        }).toList();

        log.info("用户菜单树查询完成 userId={}, count={}", userId, voList.size());
        return buildTree(voList, 0L);
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildTree(List<MenuVO> list, Long parentId) {
        return list.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    List<MenuVO> children = buildTree(list, menu.getMenuId());
                    menu.setChildren(children);
                    return menu;
                })
                .toList();
    }

    /**
     * 校验菜单唯一性（菜单名称、路径、权限标识）
     * 
     * @param dto      菜单DTO
     * @param isUpdate 是否为更新操作
     */
    private void checkMenuUnique(MenuDTO dto, boolean isUpdate) {
        // 查询条件
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        // 菜单名称、路径、权限标识
//        wrapper.and(w -> w
//                .eq(SysMenu::getMenuName, dto.getMenuName())
//                .or()
//                .eq(SysMenu::getPath, dto.getPath())
//                .or()
//                .eq(SysMenu::getPermission, dto.getPermission()));
        wrapper.and(w -> {
            w.eq(SysMenu::getMenuName, dto.getMenuName());
            if (StringUtils.hasText(dto.getPath())) {
                w.or().eq(SysMenu::getPath, dto.getPath());
            }
            if (StringUtils.hasText(dto.getPermission())) {
                w.or().eq(SysMenu::getPermission, dto.getPermission());
            }
        });
        // 更新操作
        if (isUpdate) {
            wrapper.ne(SysMenu::getMenuId, dto.getMenuId());
        }
        // 查询
        long count = this.count(wrapper);

        if (count > 0) {
            // 已存在，判断是哪个字段重复
            List<SysMenu> existing = this.list(wrapper);
            for (SysMenu existingMenu : existing) {
                // 名称重复
                if (existingMenu.getMenuName().equals(dto.getMenuName())) {
                    throw new BusinessException(RoleConstant.MENU_NAME_EXIST);
                }
                // 路径重复
                if (existingMenu.getPath().equals(dto.getPath())) {
                    throw new BusinessException(RoleConstant.MENU_PATH_EXIST);
                }
                // 权限标识重复
                if (existingMenu.getPermission() != null && existingMenu.getPermission().equals(dto.getPermission())) {
                    throw new BusinessException(RoleConstant.MENU_PERMISSION_EXIST);
                }
            }
            // 名称重复
            throw new BusinessException(RoleConstant.MENU_NAME_EXIST);
        }

        // 检查父菜单是否存在 只有【子菜单】才需要校验父菜单是否存在
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            // 查询父菜单
            SysMenu parentMenu = this.getById(dto.getParentId());
            if (parentMenu == null) {
                throw new BusinessException(RoleConstant.PARENT_MENU_NOT_FOUND);
            }
        }
    }
}
