package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import com.melioes.blueprintdigitalnexus.mapper.SysMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * 设计说明：
 * - getUserRoles() 和 getUserPermissions() 加了 @Cacheable 缓存
 * - 缓存 key = userId，TTL 在 CacheConfig 里配置（WMS:PERMISSION = 2小时）
 * - 权限变更时（改角色、改菜单）通过 @CacheEvict 主动删除缓存
 * - 这样管理员改权限后，用户下次请求自动生效，不需要重新登录
 *
 * 重要：Spring AOP 自调用问题
 * - 同一个类内 A 调用 B，B 上的 @CacheEvict 不会生效（因为绕过了代理）
 * - 解决方案：evictUserRolesCache() 用 @Caching 同时清除两个缓存
 *             evictCacheByRoleId() 通过 @Lazy 注入自身，走代理调用
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    /**
     * 注入自身代理（解决 Spring AOP 自调用问题）
     * @Lazy 延迟注入，避免循环依赖
     */
    @Lazy
    @Autowired
    private PermissionService self;

    /**
     * 根据用户ID获取角色列表（带缓存）
     *
     * 缓存 key: WMS:PERMISSION::roles:{userId}
     * 例如：用户51 → WMS:PERMISSION::roles:51
     */
    @Override
    @Cacheable(value = "WMS:PERMISSION", key = "'roles:' + #userId")
    public List<String> getUserRoles(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }

        // 查 sys_user_role 表拿到 role_id 列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 查 sys_role 表拿到 role_key 列表
        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());

        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);

        return roles.stream()
                .filter(r -> r.getStatus() == 1)  // 只返回启用的角色
                .map(SysRole::getRoleKey)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID获取权限列表（带缓存）
     *
     * 缓存 key: WMS:PERMISSION::perms:{userId}
     * 例如：用户51 → WMS:PERMISSION::perms:51
     */
    @Override
    @Cacheable(value = "WMS:PERMISSION", key = "'perms:' + #userId")
    public List<String> getUserPermissions(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }

        // 1. 查角色
        List<String> roleKeys = getUserRoles(userId);
        if (roleKeys.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查角色对应的菜单权限
        List<String> permissions = menuMapper.selectPermissionsByRoleKeys(roleKeys);

        // 3. 去重返回
        return permissions.stream()
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 清除指定用户的角色缓存 + 权限缓存
     *
     * 使用 @Caching 同时清除两个 key，避免自调用问题
     *
     * 调用时机：
     * - 管理员修改了用户的角色（updateUser）
     *
     * @param userId 用户ID
     */
    @Override
    @CacheEvict(value = "WMS:PERMISSION", key = "'roles:' + #userId")
    public void evictUserRolesCache(Long userId) {
        // 通过 self 代理调用，确保 @CacheEvict 生效
        self.evictUserPermissionsCache(userId);
    }

    /**
     * 清除指定用户的权限缓存
     */
    @Override
    @CacheEvict(value = "WMS:PERMISSION", key = "'perms:' + #userId")
    public void evictUserPermissionsCache(Long userId) {
        // 空方法，Spring AOP 代理会在方法执行前删除缓存
    }

    /**
     * 清除所有绑定了指定角色的用户的缓存
     *
     * 当管理员修改了某个角色的菜单权限时，
     * 需要清除所有绑定了该角色的用户的缓存
     *
     * @param roleId 角色ID
     */
    @Override
    public void evictCacheByRoleId(Long roleId) {
        // 1. 找到所有绑定了该角色的用户
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId));

        // 2. 通过 self 代理逐个清除缓存（确保 @CacheEvict 生效）
        for (SysUserRole ur : userRoles) {
            self.evictUserRolesCache(ur.getUserId());
        }
    }
}
