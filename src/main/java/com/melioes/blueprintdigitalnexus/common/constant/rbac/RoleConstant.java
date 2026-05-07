package com.melioes.blueprintdigitalnexus.common.constant.rbac;

/**
 * 角色常量
 * 用于系统角色标识（RBAC权限控制）
 */
// | role_id | role_name | role_key |
// | ------- | --------- | ----------- |
// | 1 | 超级管理员 | SUPER_ADMIN |
// | 2 | 管理员 | ADMIN |
// | 3 | 运营人员 | OPERATOR |
// | 4 | 普通用户 | USER |
// | 4 | 测试角色 | TEST_ROLE |

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

    // 菜单相关错误
    /** 菜单ID不能为空 */
    public static final String MENU_ID_EMPTY = "菜单ID不能为空";

    /** 菜单名称不能为空 */
    public static final String MENU_NAME_EMPTY = "菜单名称不能为空";

    /** 菜单ID无效 */
    public static final String MENU_ID_INVALID = "无效的菜单ID";

    /** 菜单未找到 */
    public static final String MENU_NOT_FOUND = "菜单未找到";

    /** 菜单与角色绑定失败 */
    public static final String ROLE_MENU_BIND_FAILED = "角色与菜单绑定失败";

    /** 菜单列表不能为空 */
    public static final String MENU_LIST_EMPTY = "菜单列表不能为空";

    /** 菜单ID无效，无法解绑 */
    public static final String MENU_ID_UNBIND_INVALID = "菜单ID无效，无法解绑";

    /** 菜单存在子菜单，无法删除 */
    public static final String MENU_HAS_CHILDREN = "该菜单下有子菜单，请先删除子菜单";

    // 角色相关错误
    /** 角色ID无效 */
    public static final String ROLE_ID_INVALID = "无效的角色ID";

    /** 角色未找到 */
    public static final String ROLE_NOT_FOUND = "角色未找到";

    /** 角色与菜单解绑失败 */
    public static final String ROLE_MENU_UNBIND_FAILED = "角色与菜单解绑失败";

    /** 用户ID不合法 */
    public static final String USER_ID_INVALID = "用户ID不合法";

    // ==================== 权限相关常量 ====================
    /** 未获取到用户角色信息 */
    public static final String NO_USER_ROLE_INFO = "未获取到用户角色信息";

    /** 无权限访问该接口 */
    public static final String NO_PERMISSION_ACCESS = "无权限访问该接口";

    /** 未获取到用户权限信息 */
    public static final String NO_USER_PERMISSION_INFO = "未获取到用户权限信息";

    // ==================== 菜单相关错误补充 ====================
    /** 菜单名称已存在 */
    public static final String MENU_NAME_EXIST = "菜单名称已存在";

    /** 菜单路径已存在 */
    public static final String MENU_PATH_EXIST = "菜单路径已存在";

    /** 菜单权限标识已存在 */
    public static final String MENU_PERMISSION_EXIST = "菜单权限标识已存在";

    /** 父菜单不存在 */
    public static final String PARENT_MENU_NOT_FOUND = "父菜单不存在";

    /** 包含无效的菜单ID */
    public static final String INVALID_MENU_IDS = "包含无效的菜单ID";

    // ==================== 用户相关错误补充 ====================
    /** 用户不存在 */
    public static final String USER_NOT_FOUND = "用户不存在";

    /** 不能删除当前登录用户 */
    public static final String CANNOT_DELETE_CURRENT_USER = "不能删除当前登录用户";
}
