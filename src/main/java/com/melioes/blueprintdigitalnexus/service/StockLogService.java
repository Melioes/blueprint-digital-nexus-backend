package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.entity.StockLog;
import com.melioes.blueprintdigitalnexus.query.StockLogQuery;
import com.melioes.blueprintdigitalnexus.vo.StockLogVO;

/**
 * 库存变动日志 Service 接口
 */
public interface StockLogService extends IService<StockLog> {

    /**
     * 分页查询库存变动日志（包含仓库名称、商品名称、单号等关联信息）
     *
     * @param query 查询条件（仓库ID、商品ID、变动类型、分页参数）
     * @return 分页结果（VO，已关联仓库名、商品名）
     */
    IPage<StockLogVO> getStockLogPage(StockLogQuery query);
}
