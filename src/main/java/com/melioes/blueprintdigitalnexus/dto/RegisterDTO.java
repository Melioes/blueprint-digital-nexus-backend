package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册参数
 */
@Data
@NoArgsConstructor
public class RegisterDTO {

    private String username;
    private String password;
    private String realName;
}