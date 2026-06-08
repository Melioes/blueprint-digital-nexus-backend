package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 用户角色关联 Mapper 接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    List<String> selectRoleKeys(Long userId);
}