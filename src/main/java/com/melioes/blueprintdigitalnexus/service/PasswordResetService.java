package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 重置用户密码
     */
    public void resetPassword(String username, String newPassword) {

        // 1. 查用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(AuthMessageConstant.USER_NOT_FOUND);
        }

        // 2. 加密密码（核心）
        String encoded = passwordEncoder.encode(newPassword);

        // 3. 写回数据库
        user.setPassword(encoded);
        userMapper.updateById(user);
    }
}