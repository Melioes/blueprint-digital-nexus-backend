package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.common.constant.auth.ErrorMessageConstant;
import com.melioes.blueprintdigitalnexus.common.constant.rbac.RoleConstant;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import com.melioes.blueprintdigitalnexus.convert.RoleConvert;
import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import com.melioes.blueprintdigitalnexus.mapper.SysRoleMapper;
import com.melioes.blueprintdigitalnexus.mapper.SysUserRoleMapper;
import com.melioes.blueprintdigitalnexus.query.RoleQuery;
import com.melioes.blueprintdigitalnexus.service.SysRoleService;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private RoleConvert roleConvert;

    /**
     * 查询角色列表
     */
    @Override
    public IPage<RoleVO> getRolePage(RoleQuery query) {

        // 1. 分页对象
        Page<SysRole> page = new Page<>(
                query.getPage(),
                query.getSize()
        );

        // 2. 查询条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getKeyword()),
                SysRole::getRoleName,
                query.getKeyword());

        wrapper.eq(query.getStatus() != null,
                SysRole::getStatus,
                query.getStatus());

        wrapper.orderByAsc(SysRole::getRoleId);

        // 3. 查询
        IPage<SysRole> rolePage = this.page(page, wrapper);
        // 👇 1. 先打印数据库原始数据
//        System.out.println("DB data: " + rolePage.getRecords());
//
//        SysRole role = rolePage.getRecords().get(0);
//
//        System.out.println(role.getRoleName());
//        System.out.println(role.getRoleKey());
        // 4. VO转换（重点）
        return rolePage.convert(roleConvert::toVO);
    }
    /**
     * 新增角色
     */
    @Override
    public void addRole(RoleDTO dto) {

        // 1. 唯一性校验
        checkRoleUnique(dto, false);

        // 2. DTO → Entity
        SysRole role = roleConvert.toEntity(dto);

        // 3. 保存
        this.save(role);
    }

    /**
     * 修改角色
     */
    @Override
    public void updateRole(RoleDTO dto) {

        // 1. 查询原数据
        SysRole role = this.getById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException(ErrorMessageConstant.DATA_NOT_EXIST);
        }

        // 2. 唯一性校验（复用）
        checkRoleUnique(dto, true);

        // 3. DTO → Entity（只更新非 null 字段 ）
        roleConvert.updateRoleFromDto(dto, role);

        // 4. 更新
        this.updateById(role);
    }

    /**
     * 删除角色
     */
    @Override
    public void deleteRole(Long id) {

        // 1. 是否存在
        if (this.getById(id) == null) {
            throw new BusinessException(ErrorMessageConstant.DATA_NOT_EXIST);
        }

        // 2. 是否被用户使用
        long count = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, id)
        );

        if (count > 0) {

            throw new BusinessException(RoleConstant.ROLE_IN_USE_CANNOT_DELETE);
        }

        // 3. 逻辑删除
        this.removeById(id);
    }


    /**
     * 根据 ID 查询角色
     */
    @Override
    public RoleVO getRoleById(Long id) {

        // 1. 查询数据
        SysRole role = this.getById(id);

        // 2. 判空（统一用常量）
        if (role == null) {
            throw new BusinessException(ErrorMessageConstant.DATA_NOT_EXIST);
        }

        // 3. 转 VO 返回
        return roleConvert.toVO(role);
    }

    /**
     * 获取角色列表 下拉框展示 无分页
     */
    @Override
    public List<RoleVO> getRoleList(RoleQuery query) {

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        // 1. 关键字（可选）
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysRole::getRoleName, query.getKeyword())
                    .or()
                    .like(SysRole::getRoleKey, query.getKeyword())
            );
        }

        // 2. 状态过滤（关键点：预留）
        wrapper.eq(query.getStatus() != null,
                SysRole::getStatus,
                query.getStatus()
        );

        // 3. 下拉框优化：只查启用（默认行为）
        if (Boolean.TRUE.equals(query.getDropdown())) {
            wrapper.eq(SysRole::getStatus, 1);
        }

        // 4. 排序（稳定输出）
        wrapper.orderByAsc(SysRole::getRoleId);

        return this.list(wrapper)
                .stream()
                .map(roleConvert::toVO)
                .toList();
    }

    /**
     * 校验角色名称、角色标识唯一性
     */
    private void checkRoleUnique(RoleDTO dto, boolean isUpdate) {

        // 1. 角色名称
        LambdaQueryWrapper<SysRole> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(SysRole::getRoleName, dto.getRoleName());
        //  更新时排除自身
        if (isUpdate) {
            nameWrapper.ne(SysRole::getRoleId, dto.getRoleId());
        }

        if (this.count(nameWrapper) > 0) {
            throw new BusinessException(RoleConstant.ROLE_NAME_EXIST);
        }

        // 2. 角色标识
        LambdaQueryWrapper<SysRole> keyWrapper = new LambdaQueryWrapper<>();
        keyWrapper.eq(SysRole::getRoleKey, dto.getRoleKey());

        if (isUpdate) {
            keyWrapper.ne(SysRole::getRoleId, dto.getRoleId());
        }

        if (this.count(keyWrapper) > 0) {
            throw new BusinessException(RoleConstant.ROLE_CODE_EXIST);
        }
    }
}