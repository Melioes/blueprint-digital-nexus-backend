package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import com.melioes.blueprintdigitalnexus.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.util.Base64;

/**
 * 认证控制器
 *
 * 负责登录、注册、获取 RSA 公钥等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@Tag(name = "认证管理", description = "登录注册接口")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 注入 RSA 密钥对（由 RsaConfig 在启动时生成）
     */
    @Autowired
    private KeyPair rsaKeyPair;

    /**
     * 获取 RSA 公钥
     *
     * 前端登录前先调此接口获取公钥，用公钥加密密码后再提交登录
     * 这样密码在网络传输中不会以明文形式暴露
     *
     * @return Base64 编码的 RSA 公钥字符串
     */
    @GetMapping("/pubkey")
    @Operation(summary = "获取RSA公钥", description = "前端用公钥加密密码后再提交登录，防止密码明文传输")
    public Result<String> getPublicKey() {
        // 将公钥编码为 Base64 字符串返回给前端
        String publicKeyStr = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
        log.info("[RSA] 返回公钥");
        return Result.success(publicKeyStr);
    }

    /**
     * 登录
     *
     * 密码字段支持两种格式：
     * 1. RSA 加密后的密文（前端加密提交）→ 后端用私钥解密得到真实密码
     * 2. 明文密码（兼容 APIFox 等工具直接测试）
     *
     * @param dto 登录参数（username + password）
     * @return 登录结果（token + 用户信息）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统（密码支持RSA加密）")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        // 尝试 RSA 解密密码（如果前端传的是加密后的密文）
        String decryptedPassword = tryRsaDecrypt(dto.getPassword());
        dto.setPassword(decryptedPassword);

        LoginVO vo = sysUserService.login(dto);
        log.info("用户登录成功，username={}", dto.getUsername());
        return Result.success(vo, AuthMessageConstant.LOGIN_SUCCESS);
    }

    /**
     * 注册
     *
     * 注册时密码也是明文传输（注册页面没有加密，后续可以加）
     *
     * @param dto 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        log.info("收到注册请求 username={}", dto.getUsername());
        sysUserService.register(dto);
        log.info("用户注册成功，username={}", dto.getUsername());
        return Result.success(null, AuthMessageConstant.REGISTER_SUCCESS);
    }

    /**
     * 尝试用 RSA 私钥解密密码
     *
     * 如果解密成功 → 返回解密后的真实密码
     * 如果解密失败（不是 RSA 密文，比如 APIFox 直接传的明文）→ 原样返回
     *
     * 这样做是为了兼容：前端加密提交和 APIFox 明文测试都能用
     *
     * @param password 前端传来的密码（可能是 RSA 密文，也可能是明文）
     * @return 解密后的真实密码
     */
    private String tryRsaDecrypt(String password) {
        try {
            // 用私钥解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(password));
            String result = new String(decrypted);
            log.info("[RSA] 密码解密成功");
            return result;
        } catch (Exception e) {
            // 解密失败，说明不是 RSA 密文，当作明文密码处理（兼容 APIFox 测试）
            log.info("[RSA] 密码非RSA密文，按明文处理");
            return password;
        }
    }
}
