package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
/**
 * 库存 Mapper 接口
 *
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
