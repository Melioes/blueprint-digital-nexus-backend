package com.melioes.blueprintdigitalnexus.dto;

import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录参数
 */
@Data
@NoArgsConstructor
public class LoginDTO {

    /**
     * 用户名
     */
    @NotBlank(message = AuthMessageConstant.USERNAME_EMPTY)
    @Size(min = 2, max = 30, message = "用户名长度2-30个字符")
    private String username;

    /**
     * 密码（前端 RSA 加密后提交，密文长度约 344 字符）
     * 未加密时原始密码 6-50 字符
     */
    @NotBlank(message = AuthMessageConstant.PASSWORD_EMPTY)
    @Size(min = 6, max = 512, message = "密码长度6-512个字符")
    private String password;
}
