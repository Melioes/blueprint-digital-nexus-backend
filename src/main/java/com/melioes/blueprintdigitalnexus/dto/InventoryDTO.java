package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
/**
 * 库存调整DTO
 * 前端调用调整接口时传递的参数
 */
@Data
public class InventoryDTO {

    /**
     * 仓库ID 修改时使用，新增时忽略
     */
    private Long warehouseId;

    /**
     * 商品ID 添加时使用，修改时忽略
     */
    private Long productId;

    /**
     * 库存数量
     */
    private Integer totalStock;

    /**
     * 调整类型  \可选：IN=入库，OUT=出库）
     */
    private String adjustType;

    /**
     * 调整原因 (可选)
     */
    private String adjustReason;
}
