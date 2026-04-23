package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.constant.auth.PasswordConstant;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.properties.JwtProperties;
import com.melioes.blueprintdigitalnexus.common.utils.JwtUtil;
import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(LoginDTO dto) {

        // 1. 查询用户
        SysUser user = getUserByUsername(dto.getUsername());

        if (user == null) {
            throw new BusinessException(AuthMessageConstant.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(AuthMessageConstant.ACCOUNT_DISABLED);
        }

        // 3. 密码校验（BCrypt匹配）  对比数据库
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthMessageConstant.PASSWORD_ERROR);
        }

        // 2. 查角色
        List<String> roleKeys = userMapper.selectRoleKeyList(user.getUserId());

        // 3. JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("roleKeys", roleKeys);

        return JwtUtil.generateToken(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims
        );
    }

    @Override
    public void register(RegisterDTO dto) {

        // 1. 用户是否存在
        SysUser exist = getUserByUsername(dto.getUsername());

        if (exist != null) {
            throw new BusinessException(AuthMessageConstant.USER_ALREADY_EXISTS);
        }

        // 2. 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        //  密码处理
        String rawPassword = (dto.getPassword() == null)
                ? PasswordConstant.DEFAULT_PASSWORD
                : dto.getPassword();
        //加密存数据库
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        user.setRealName(dto.getRealName());
        user.setStatus(1);

        userMapper.insert(user);

        // 3. 分配默认角色（USER）
        SysRole role = sysRoleMapper.selectOne(
                //创建一个“查询条件容器” 用来装 SQL 条件的盒子
                new LambdaQueryWrapper<SysRole>()
                        //key value
                        .eq(SysRole::getRoleKey, RoleConstant.USER)
        );

        if (role == null) {
            throw new BusinessException("默认角色不存在");
        }

        // 创建用户-角色关联对象（对应 sys_user_role 表）
                SysUserRole ur = new SysUserRole();

        // 设置用户ID（刚注册成功的用户）
                ur.setUserId(user.getUserId());

        // 设置角色ID（从 sys_role 表查出来的 USER 角色）
                ur.setRoleId(role.getRoleId());

        // 插入到中间表 sys_user_role
        // 作用：建立 用户 ↔ 角色 的绑定关系
        log.info("准备插入用户角色关系 userId={}, roleId={}", user.getUserId(), role.getRoleId());
        //userRoleMapper.insert(ur);
        int rows = userRoleMapper.insert(ur);
        log.info("插入 sys_user_role 成功 rows={}", rows);
    }

    /**
     * 根据用户名查询用户
     * 复用：登录 / 注册检查
     */
    private SysUser getUserByUsername(String username) {
        //查数据库之前检查用户名
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(AuthMessageConstant.USERNAME_EMPTY);
        }
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username.trim())
        );
    }
}