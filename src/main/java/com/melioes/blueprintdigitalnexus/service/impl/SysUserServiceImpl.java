package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.auth.AuthMessageConstant;
import com.melioes.blueprintdigitalnexus.common.constant.auth.PasswordConstant;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.common.properties.JwtProperties;
import com.melioes.blueprintdigitalnexus.common.utils.JwtUtil;
import com.melioes.blueprintdigitalnexus.convert.UserConvert;
import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.query.UserQuery;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import com.melioes.blueprintdigitalnexus.vo.EmployeeInfoVO;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
import com.melioes.blueprintdigitalnexus.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService  {

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
    @Autowired
    private UserConvert userConvert;


    @Override
    public LoginVO login(LoginDTO dto) {

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

        // 2. 查角色 sys_role 表
        List<String> roleKeys = userMapper.selectRoleKeyList(user.getUserId());

        // 3. JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("roleKeys", roleKeys);

        String token = JwtUtil.generateToken(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims
        );

        // =========================
        // 组装返回 VO
        // =========================
        EmployeeInfoVO userInfo = new EmployeeInfoVO();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRoles(roleKeys);
        userInfo.setStatus(user.getStatus());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(userInfo);

        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {

        // 1. 用户是否存在
        SysUser exist = getUserByUsername(dto.getUsername());

        if (exist != null) {
            throw new BusinessException(AuthMessageConstant.USER_ALREADY_EXISTS);
        }

        // 2. 创建用户（复用方法）
        SysUser user = buildSysUser(dto);

        // 3. 插入用户
        createUser(user);

        // 4. 分配默认角色（USER）
        bindDefaultRole(user.getUserId());
    }




    /**
     * 获取用户列表
     * @param query
     * @return
     */
    @Override
    public IPage<EmployeeVO> getUserPage(UserQuery query) {

        // 1. 分页对象（统一从 PageQuery 来）
        Page<SysUser> page = new Page<>(
                query.getPage(),
                query.getSize()
        );

        // 2. 查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        wrapper.and(StringUtils.hasText(query.getKeyword()), w ->
                w.like(SysUser::getUsername, query.getKeyword())
                        .or()
                        .like(SysUser::getRealName, query.getKeyword())
        );

        wrapper.orderByDesc(SysUser::getUserId);

        // 3. 查询
        IPage<SysUser> pageResult = this.page(page, wrapper);

        log.info("user total = {}", pageResult.getTotal());

        // 4. VO 转换（统一 converter）
        return pageResult.convert(userConvert::toVO);
    }

    /**
     * 新增用户
     * @param dto
     */
    @Override
    @Transactional
    public void addUser(EmployeeDTO dto) {

        // 1. 校验用户名是否存在
        SysUser exist = getUserByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException(AuthMessageConstant.USER_ALREADY_EXISTS);
        }

        // 2. 构建用户对象
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());

        // 密码处理
        String rawPassword = StringUtils.hasText(dto.getPassword())
                ? dto.getPassword()
                : PasswordConstant.DEFAULT_PASSWORD;

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(dto.getRealName());

        // 可选字段
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }

        // 状态（默认启用）
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // 3. 插入用户
        this.save(user);

        // 获取 userId
        Long userId = user.getUserId();

        // =========================
        // 4. 角色绑定（核心优化部分）
        // =========================
        List<Long> roleIds = dto.getRoleIds();

        if (roleIds != null && !roleIds.isEmpty()) {

            // 批量构建 user_role 关系
            List<SysUserRole> list = roleIds.stream()
                    .map(roleId -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(userId);
                        ur.setRoleId(roleId);
                        return ur;
                    })
                    .toList();

            for (SysUserRole ur : list) {
                userRoleMapper.insert(ur);
            }

            log.info("使用前端角色 roleIds={}", roleIds);

        } else {
            // 默认角色兜底
            bindDefaultRole(userId);
        }
    }

    /**
     * 修改用户
     */
    @Override
    public void updateUser(EmployeeDTO dto) {

        if (dto.getUserId() == null) {
            throw new BusinessException(AuthMessageConstant.ID_EMPTY);
        }

        SysUser user = getUserById(dto.getUserId());

        // 用户名单独处理（因为要校验唯一性）
        if (StringUtils.hasText(dto.getUsername())) {
            SysUser exist = getUserByUsername(dto.getUsername());
            if (exist != null && !exist.getUserId().equals(user.getUserId())) {
                throw new BusinessException(AuthMessageConstant.USER_ALREADY_EXISTS);
            }
        }
        //  所有字段更新 避免手动赋值
        userConvert.updateUserFromDto(dto, user);
        // 密码特殊处理
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        this.updateById(user);
    }

    /**
     * 获取用户详情
     */
    @Override
    public EmployeeVO getUserDetail(Long id) {
        SysUser user = getUserById(id);

        if (user == null) {
            throw new BusinessException(AuthMessageConstant.USER_NOT_FOUND);
        }
        //        MP 的 getById() 只返回 Entity（SysUser）
        //       它不会自动变 VO
        return userConvert.toVO(user);
    }

    /**
     * 检查用户ID 用户名称
     */
    private SysUser getUserById(Long id) {
        if (id == null) {
            throw new BusinessException(AuthMessageConstant.ID_EMPTY);
        }

        SysUser user = this.getById(id);

        if (user == null) {
            throw new BusinessException(AuthMessageConstant.USER_NOT_FOUND);
        }

        return user;
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
        //查数据库，获取用户
        return this.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username.trim())
        );
    }


    /**
     * 构建用户对象（注册 / 后台新增通用）
     */
    private SysUser buildSysUser(RegisterDTO dto) {

        SysUser user = new SysUser();

        // 用户名
        user.setUsername(dto.getUsername());

        // 密码处理（为空则给默认密码）
        String rawPassword = (dto.getPassword() == null)
                ? PasswordConstant.DEFAULT_PASSWORD
                : dto.getPassword();

        // 加密存数据库
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // 真实姓名
        user.setRealName(dto.getRealName());

        // 默认启用账号
        user.setStatus(1);

        return user;
    }

    /**
     * 创建用户
     */
    private void createUser(SysUser user) {
        userMapper.insert(user);
    }

    /**
     * 绑定默认角色（USER）
     */
    private void bindDefaultRole(Long userId) {

        // 查询 USER 角色
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleKey, RoleConstant.USER)
        );

        if (role == null) {
            throw new BusinessException("默认角色不存在");
        }

        // 创建用户-角色关联对象
        SysUserRole ur = new SysUserRole();

        // 设置用户ID
        ur.setUserId(userId);

        // 设置角色ID
        ur.setRoleId(role.getRoleId());

        // 插入关系
        log.info("准备绑定默认角色 userId={}, roleId={}", userId, role.getRoleId());

        int rows = userRoleMapper.insert(ur);

        log.info("绑定角色完成 rows={}", rows);
    }
}


