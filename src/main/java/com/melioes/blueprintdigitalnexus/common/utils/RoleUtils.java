package com.melioes.blueprintdigitalnexus.common.utils;

import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;

public class RoleUtils {

    /**
     * 生成角色标识
     * 规则：ROLE_ + 角色名称（去空格并大写）
     * @param roleName 角色名称
     * @return 生成的角色标识
     */
    public static String generateRoleKey(String roleName) {
        /**
         * 角色名称不能为空
         */
        if (roleName == null) {
            throw new IllegalArgumentException(RoleConstant.ROLE_NAME_EMPTY);
        }
        return "ROLE_" + roleName.replaceAll("\\s+", "_").toUpperCase();
    }
}