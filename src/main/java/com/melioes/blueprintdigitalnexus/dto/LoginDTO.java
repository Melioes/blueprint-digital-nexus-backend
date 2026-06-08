package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录参数
 */
@Data
@NoArgsConstructor
public class LoginDTO {
    private String username;
    private String password;
}
