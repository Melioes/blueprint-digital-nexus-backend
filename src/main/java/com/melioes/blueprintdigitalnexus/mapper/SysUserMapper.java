package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.SysUser;

import java.util.List;

;


public interface SysUserMapper extends BaseMapper<SysUser> {
    List<String> selectRoleKeyList(Long userId);
}