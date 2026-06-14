package com.melioes.blueprintdigitalnexus.service;

import java.util.List;

/**
 * 权限服务接口
 *
 * 提供用户角色和权限的查询、缓存、清除功能
 */
public interface PermissionService {

    /**
     * 根据用户ID获取角色列表（带缓存）
     *
     * @param userId 用户ID
     * @return 角色 key 列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 根据用户ID获取权限列表（带缓存）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 清除指定用户的角色缓存（同时清除权限缓存）
     *
     * @param userId 用户ID
     */
    void evictUserRolesCache(Long userId);

    /**
     * 清除指定用户的权限缓存
     *
     * @param userId 用户ID
     */
    void evictUserPermissionsCache(Long userId);

    /**
     * 清除所有绑定了指定角色的用户的缓存
     *
     * @param roleId 角色ID
     */
    void evictCacheByRoleId(Long roleId);
}
