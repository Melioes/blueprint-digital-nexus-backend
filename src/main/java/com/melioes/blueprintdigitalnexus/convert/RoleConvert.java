package com.melioes.blueprintdigitalnexus.convert;

import com.melioes.blueprintdigitalnexus.dto.RoleDTO;
import com.melioes.blueprintdigitalnexus.entity.SysRole;
import com.melioes.blueprintdigitalnexus.vo.RoleVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoleConvert {

    // DTO → Entity（新增用）
    SysRole toEntity(RoleDTO dto);

    // Entity → VO（返回前端）
    RoleVO toVO(SysRole role);

    // 修改用（只更新非 null 字段 ）
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromDto(RoleDTO dto, @MappingTarget SysRole role);
}