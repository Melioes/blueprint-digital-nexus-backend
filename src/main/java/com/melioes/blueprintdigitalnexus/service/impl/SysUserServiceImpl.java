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
import com.melioes.blueprintdigitalnexus.common.utils.RoleUtils;
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
//
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

        createUser(buildSysUser(dto), null);
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
// 修改 addUser 方法
    @Override
    @Transactional
    public void addUser(EmployeeDTO dto) {
        // 1. 校验用户名是否存在
        SysUser exist = getUserByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException(AuthMessageConstant.USER_ALREADY_EXISTS);
        }

        // 2. 构建用户对象
        SysUser user = buildSysUser(dto);

        // 3. 保存用户
        this.save(user);

        // 4. 角色绑定（修改：只在没有指定角色时绑定默认角色）
        List<Long> roleIds = dto.getRoleIds();
        if (roleIds != null && !roleIds.isEmpty()) {
            // 绑定指定角色
            for (Long roleId : roleIds) {
                bindRole(user.getUserId(), roleId);
            }
            log.info("绑定指定角色 roleIds={}", roleIds);
        } else {
            // 绑定默认角色
            bindDefaultRole(user.getUserId());
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
     * 构建用户对象（注册场景）
     */
    private SysUser buildSysUser(RegisterDTO dto) {
        SysUser user = new SysUser();

        // 必填字段
        user.setUsername(dto.getUsername());

        // 密码处理
        String rawPassword = StringUtils.hasText(dto.getPassword())
                ? dto.getPassword()
                : PasswordConstant.DEFAULT_PASSWORD;
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setRealName(dto.getRealName());
        user.setStatus(1);  // 注册默认启用

        return user;
    }


    /**
     * 构建用户对象（注册 / 后台新增通用）
     */
    private SysUser buildSysUser(EmployeeDTO dto) {

        SysUser user = new SysUser();

        // 用户名
        user.setUsername(dto.getUsername());

        // 密码处理（为空则给默认密码）
        String rawPassword = StringUtils.hasText(dto.getPassword())
                ? dto.getPassword()
                : PasswordConstant.DEFAULT_PASSWORD;

        // 加密存数据库
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // 真实姓名
        user.setRealName(dto.getRealName());

        // 默认启用账号
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        return user;
    }

    /**
     * 创建用户
     */
    private void createUser(SysUser user, String roleKey) {
//        userMapper.insert(user);
        this.save(user);
        // 绑定默认角色
        // 绑定角色（如果没有传递角色信息，自动分配默认角色）
        bindDefaultRole(user.getUserId(), roleKey);
    }

    /**
     * 绑定默认角色（USER）
     */
//    private void bindDefaultRole(Long userId) {
//
//        // 查询 USER 角色
//        SysRole role = sysRoleMapper.selectOne(
//                new LambdaQueryWrapper<SysRole>()
//                        .eq(SysRole::getRoleKey, RoleConstant.USER)
//        );
//
//        if (role == null) {
//            throw new BusinessException("默认角色不存在");
//        }
//
//        // 创建用户-角色关联对象
//        SysUserRole ur = new SysUserRole();
//
//        // 设置用户ID
//        ur.setUserId(userId);
//
//        // 设置角色ID
//        ur.setRoleId(role.getRoleId());
//
//        // 插入关系
//        log.info("准备绑定默认角色 userId={}, roleId={}", userId, role.getRoleId());
//
//        int rows = userRoleMapper.insert(ur);
//
//        log.info("绑定角色完成 rows={}", rows);
//    }




    /**
     * 绑定默认角色
     * @param userId 用户ID
     */
    private void bindDefaultRole(Long userId) {
        bindDefaultRole(userId, null);
    }

    /**
     * 绑定角色（支持自定义角色或默认角色）
     * @param userId 用户ID
     * @param roleKey 角色标识（可为null，为null时使用默认角色）
     */
    private void bindDefaultRole(Long userId, String roleKey) {
        SysRole role;

        if (roleKey == null || roleKey.trim().isEmpty()) {
            // 没有传递角色，使用默认角色 USER
            role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleKey, RoleConstant.USER)
            );

            // 如果默认角色不存在，自动创建
            if (role == null) {
                log.warn("默认角色 USER 不存在，自动创建");
                role = createDefaultRole();
            }
        } else {
            // 使用传递的角色
            role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleKey, roleKey)
            );

            if (role == null) {
                throw new BusinessException("角色 " + roleKey + " 不存在");
            }
        }

        // 创建用户-角色关联
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getRoleId());
        userRoleMapper.insert(ur);

        log.info("绑定角色成功 userId={}, roleId={}, roleKey={}",
                userId, role.getRoleId(), role.getRoleKey());
    }

    /**
     * 创建默认角色
     * 如果数据库中没有 '普通用户' 角色，就自动创建该角色
     */
    private SysRole createDefaultRole() {
        // 创建一个新的角色实例
        SysRole role = new SysRole();
        role.setRoleName("普通用户");  // 使用角色名称常量
        role.setRoleKey(RoleConstant.USER);
        role.setDescription("普通用户角色（系统默认）");
        role.setStatus(1);  // 默认启用
        role.setIsDeleted(0); // 默认未删除

        // 保存到数据库
        sysRoleMapper.insert(role);
        log.info("创建默认角色: {}", role);

        return role;
    }

    /**
     * 绑定指定角色（通过角色ID）
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    private void bindRole(Long userId, Long roleId) {
        // 1. 校验角色是否存在
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色ID不存在: " + roleId);
        }

        // 2. 校验角色是否已启用
        if (role.getStatus() != 1) {
            throw new BusinessException("角色已禁用: " + role.getRoleName());
        }

        // 3. 校验是否已绑定该角色
        Long count = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, roleId)
        );

        if (count > 0) {
            log.warn("用户已绑定该角色 userId={}, roleId={}", userId, roleId);
            return;  // 已绑定则跳过
        }

        // 4. 创建用户-角色关联
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);

        log.info("绑定角色成功 userId={}, roleId={}, roleName={}",
                userId, roleId, role.getRoleName());
    }


}


