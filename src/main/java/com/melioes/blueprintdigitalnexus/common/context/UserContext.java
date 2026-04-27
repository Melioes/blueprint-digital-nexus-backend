package com.melioes.blueprintdigitalnexus.common.context;

import java.util.List;

/**
 * 用户上下文（线程级）
 * 用于存储登录用户信息
 */
public class UserContext {


    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();


    // 角色信息

    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long get() {
        return USER_ID.get();
    }

    /**
     * 设置角色（新增）
     */
    public static void setRoles(List<String> roles) {
        ROLES.set(roles);
    }

    /**
     * 获取角色（新增）
     */
    public static List<String> getRoles() {
        return ROLES.get();
    }

    /**
     * 清理线程数据
     */
    public static void remove() {
        USER_ID.remove();
        ROLES.remove();
    }
}