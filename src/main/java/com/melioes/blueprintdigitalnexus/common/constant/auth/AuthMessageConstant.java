package com.melioes.blueprintdigitalnexus.common.constant.auth;
/**
 * 认证模块提示信息常量
 * 用于登录 / 注册 / token 校验等场景的统一返回提示
 */
public class AuthMessageConstant {

    // 登录
    public static final String USERNAME_EMPTY = "用户名不能为空";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_DISABLED = "账号已被禁用";
    public static final String LOGIN_SUCCESS = "登录成功";

    // 注册
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String REGISTER_SUCCESS = "注册成功";
    public static final String REGISTER_FAIL = "注册失败";

    // token / 会话
    /** token失效或非法 */
    public static final String NOT_LOGIN = "未登录";
    public static final String TOKEN_INVALID = "登录已失效，请重新登录";
    public static final String NO_PERMISSION = "无权限访问";
}