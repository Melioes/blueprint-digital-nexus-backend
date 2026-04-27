package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.util.List;
//用于：
//登录返回 / 当前用户信息 / JWT解析后返回
@Data
public class EmployeeInfoVO {

    private Long userId;

    private String username;

    private String realName;

    private String avatar;
    private Integer status;
    private List<String> roles;
}