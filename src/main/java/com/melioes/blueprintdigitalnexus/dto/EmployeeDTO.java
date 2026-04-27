package com.melioes.blueprintdigitalnexus.dto;

import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeDTO {

    private Long userId; // 更新用

    @NotBlank(message = AuthMessageConstant.USERNAME_EMPTY)
    private String username;

    @NotBlank(message = AuthMessageConstant.PASSWORD_EMPTY)
    private String password;
//    @NotBlank(message = AuthMessageConstant.USERNAME_EMPTY)
    private String realName;

    // 可选字段
    private String avatar;

    private Integer status;

    private List<Long> roleIds;
}