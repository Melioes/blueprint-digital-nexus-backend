package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 登录返回 / 当前用户信息 / JWT解析后返回
 */
@Data
public class EmployeeVO {

    private Long userId;

    private String username;

    private String realName;

    private String avatar;

    private Integer status;

    private LocalDateTime createTime;
}