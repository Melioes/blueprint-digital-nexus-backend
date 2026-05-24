package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;
/**
 * 登录返回数据
 */
@Data
public class LoginVO {

    private String token;


    private EmployeeInfoVO userInfo;
}