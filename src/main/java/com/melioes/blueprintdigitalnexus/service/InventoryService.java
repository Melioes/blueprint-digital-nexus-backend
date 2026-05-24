package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.InventoryDTO;
import com.melioes.blueprintdigitalnexus.entity.Inventory;
import com.melioes.blueprintdigitalnexus.query.InventoryQuery;
import com.melioes.blueprintdigitalnexus.vo.InventoryVO;

import java.util.List;

public interface InventoryService extends IService<Inventory> {
    /**
     * 获取库存分页列表
     * 
     * @param query 查询参数
     * @return 库存 VO 列表
     */
    IPage<InventoryVO> getInventoryPage(InventoryQuery query);

    /**
     * 根据ID获取库存信息
     * 
     * @param id 库存ID
     * @return 库存信息
     */
    InventoryVO getInventoryById(Long id);

    /**
     * 根据仓库ID和商品ID获取库存信息
     * 
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存信息
     */
    InventoryVO getByWarehouseAndProduct(Long warehouseId, Long productId);

    /**
     * 调整库存
     * 
     * @param dto 调整库存参数
     */
    void adjustInventory(InventoryDTO dto);

    /**
     * 获取库存列表 无分页下拉
     * 
     * @param query 查询参数
     * @return 库存列表
     */
    List<InventoryVO> getInventoryList(InventoryQuery query);

    /**
     * 根据ID获取库存信息并检查是否存在 public，供其他模块调用）
     * 
     * @param id 库存ID
     * @return 库存信息
     */
    Inventory getAndCheckInventory(Long id);

    /**
     * 新增库存
     * 
     * @param dto 新增库存参数
     */
    void addInventory(InventoryDTO dto);
}
