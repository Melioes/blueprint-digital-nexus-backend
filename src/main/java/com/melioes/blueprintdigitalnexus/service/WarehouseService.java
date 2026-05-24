package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.WarehouseDTO;
import com.melioes.blueprintdigitalnexus.entity.Warehouse;
import com.melioes.blueprintdigitalnexus.query.WarehouseQuery;
import com.melioes.blueprintdigitalnexus.vo.WarehouseVO;

import java.util.List;

/**
 * 仓库Service接口
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询仓库列表
     *
     * @param query 查询条件
     * @return 仓库分页数据
     */
    IPage<WarehouseVO> getWarehousePage(WarehouseQuery query);

    /**
     * 查询仓库列表（无分页）
     *
     * @param query 查询条件
     * @return 仓库列表
     */
    List<WarehouseVO> getWarehouseList(WarehouseQuery query);

    /**
     * 根据ID查询仓库详情
     *
     * @param warehouseId 仓库ID
     * @return 仓库详情
     */
    WarehouseVO getWarehouseById(Long warehouseId);

    /**
     * 新增仓库
     *
     * @param dto 仓库信息
     */
    void addWarehouse(WarehouseDTO dto);

    /**
     * 修改仓库
     *
     * @param dto 仓库信息
     */
    void updateWarehouse(WarehouseDTO dto);

    /**
     * 删除仓库
     *
     * @param warehouseId 仓库ID
     */
    void deleteWarehouse(Long warehouseId);
}
