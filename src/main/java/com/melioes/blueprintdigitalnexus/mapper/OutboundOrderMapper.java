package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单 Mapper 映射接口
 */
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {
}