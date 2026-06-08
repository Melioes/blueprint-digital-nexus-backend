package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.OutboundDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单明细Mapper
 */
@Mapper
public interface OutboundDetailMapper extends BaseMapper<OutboundDetail> {
}
