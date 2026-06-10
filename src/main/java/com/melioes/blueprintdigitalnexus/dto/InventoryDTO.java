package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存调整DTO
 * 前端调用调整接口时传递的参数
 */
@Data
@NoArgsConstructor
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

    /**
     * 业务单据ID（入库单ID 或 出库单ID）
     * 用于库存变动日志追溯
     */
    private Long bizId;

    /**
     * 单据编号（入库单号 或 出库单号）
     * 用于库存变动日志快照
     */
    private String orderNo;
}
