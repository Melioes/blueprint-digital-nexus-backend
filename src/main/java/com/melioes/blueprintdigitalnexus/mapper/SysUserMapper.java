package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<String> selectRoleKeyList(Long userId);
}