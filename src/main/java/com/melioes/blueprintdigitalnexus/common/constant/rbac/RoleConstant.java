package com.melioes.blueprintdigitalnexus.common.constant.rbac;
/**
 * 角色常量
 * 用于系统角色标识（RBAC权限控制）
 */
//        | role_id | role_name | role_key    |
//        | ------- | --------- | ----------- |
//        | 1       | 超级管理员  | SUPER_ADMIN |
//        | 2       | 管理员     | ADMIN       |
//        | 3       | 运营人员    | OPERATOR    |
//        | 4       | 普通用户   | USER        |

public class RoleConstant {
    /** 超级管理员（系统最高权限） */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** 系统管理员 */
    public static final String ADMIN = "ADMIN";

    /** 运营人员（业务操作） */
    public static final String OPERATOR = "OPERATOR";

    /** 普通用户 */
    public static final String USER = "USER";
}