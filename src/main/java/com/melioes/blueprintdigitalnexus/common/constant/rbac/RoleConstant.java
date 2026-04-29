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
//        | 4       | 测试角色   | TEST_ROLE        |

public class RoleConstant {
    /** 超级管理员（系统最高权限） */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** 系统管理员 */
    public static final String ADMIN = "ADMIN";

    /** 运营人员（业务操作） */
    public static final String OPERATOR = "OPERATOR";

    /** 普通用户 */
    public static final String USER = "USER";

    /** 测试角色 */
    public static final String TEST_USER = "TEST_ROLE";

    /**
     * 角色名称已存在
     */
    public static final String ROLE_NAME_EXIST = "角色名称已存在";

    /**
     * 角色标识已存在
     */
    public static final String ROLE_CODE_EXIST = "角色标识已存在";

    /**
     * 该角色正在被使用，无法删除
     */
    public static final String ROLE_IN_USE_CANNOT_DELETE = "该角色正在被使用，无法删除";

    /**
     * 角色名称不能为空
     */
    public static final String ROLE_NAME_EMPTY = "角色名称不能为空";
}