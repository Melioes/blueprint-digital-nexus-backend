package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存变动日志 Mapper
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}