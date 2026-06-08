package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;


    private EmployeeInfoVO userInfo;
}