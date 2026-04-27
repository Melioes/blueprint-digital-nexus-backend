package com.melioes.blueprintdigitalnexus.convert;

import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

// 标记这是 MapStruct 的转换接口，交给 Spring 管理
@Mapper(componentModel = "spring")
public interface UserConvert {
//    @Mapping(target = "password", ignore = true)
        // 实体转 VO
    EmployeeVO toVO(SysUser user);

    /**
     * 更新用户（只更新非 null 字段） DTO → Entity（更新用）null 的字段，不会覆盖原值
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(EmployeeDTO dto, @MappingTarget SysUser user);
}
