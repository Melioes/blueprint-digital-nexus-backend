package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.InboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单 Mapper 映射接口
 */
@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrder> {
}