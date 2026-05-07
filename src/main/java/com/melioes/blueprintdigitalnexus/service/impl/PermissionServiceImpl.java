package com.melioes.blueprintdigitalnexus.service.impl;

import com.melioes.blueprintdigitalnexus.mapper.SysMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMenuMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 权限服务实现类
 *
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

     @Autowired
     private SysRoleMenuMapper roleMenuMapper;

     @Autowired
     private SysMenuMapper menuMapper;

//    @Override
//    public List<String> getUserPermissions(Long userId) {
//        //
//        // 1. 查角色
//         List<String> roleKeys = userRoleMapper.selectRoleKeys(userId);
//
//        // 2. 查角色对应菜单
//         List<String> permissions = menuMapper.selectPermissionsByRoleKeys(roleKeys);
//
//        // 3. 去重返回
//         return permissions.stream().distinct().toList();
//
//        // 暂时返回空列表
//        return Collections.emptyList();
//    }

    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }
        // 1. 查角色
        List<String> roleKeys = userRoleMapper.selectRoleKeys(userId);
        //
        if (roleKeys.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> permissions = menuMapper.selectPermissionsByRoleKeys(roleKeys);
        // 3. 去重返回
        return permissions.stream()
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .distinct()
                .toList();
    }
}
