package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
/**
 * 注册参数
 */
@Data
public class RegisterDTO {

    private String username;
    private String password;
    private String realName;
}