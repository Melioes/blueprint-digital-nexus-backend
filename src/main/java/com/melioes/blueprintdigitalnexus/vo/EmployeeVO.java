package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeVO {

    private Long userId;

    private String username;

    private String realName;

    private String avatar;

    private Integer status;

    private LocalDateTime createTime;
}