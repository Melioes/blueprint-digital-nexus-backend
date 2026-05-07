package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper 接口 先只做基础CRUD，后面再加树结构查询
 *
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    //
     List<String> selectPermissionsByRoleKeys(List<String> roleKeys);
}
