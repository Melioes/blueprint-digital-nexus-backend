package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;


    private EmployeeInfoVO userInfo;
}