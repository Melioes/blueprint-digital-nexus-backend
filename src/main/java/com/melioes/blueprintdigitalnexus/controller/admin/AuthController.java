package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AuthController {
    @Autowired
    private SysUserService sysUserService;
    /**
     * 登录
     * @param dto 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO dto) {
        String token = sysUserService.login(dto);
        log.info("用户登录成功，username={}", dto.getUsername());
        return Result.success(token, AuthMessageConstant.LOGIN_SUCCESS);
    }

    /**
     * 注册
     * @param dto 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        log.info("收到注册请求 username={}", dto.getUsername());
        sysUserService.register(dto);
        log.info("用户注册成功，username={}", dto.getUsername());
        return Result.success(null, AuthMessageConstant.REGISTER_SUCCESS);
    }

}
