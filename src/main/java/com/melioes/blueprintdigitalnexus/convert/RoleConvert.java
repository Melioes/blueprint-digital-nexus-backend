package com.melioes.blueprintdigitalnexus.convert;

import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
//编译：执行mvn compile生成实现类 避免手写BeanUtils set错字段或者重复手写 注入使用：在 Service 里注入转换器，直接调用方法
@Mapper(componentModel = "spring")
public interface RoleConvert {

    // DTO → Entity（新增用）
    SysRole toEntity(RoleDTO dto);

    // Entity → VO（返回前端）
    RoleVO toVO(SysRole role);

    // 修改用（只更新非 null 字段 ）
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    //直接修改了你传进去的对象
    void updateRoleFromDto(RoleDTO dto, @MappingTarget SysRole role);
}