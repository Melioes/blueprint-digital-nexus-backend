package com.melioes.blueprintdigitalnexus.vo;

import lombok.Data;

@Data
public class InventoryVO {
    private Long inventoryId;
    private Long warehouseId;
    private Long productId;
    private Integer totalStock;
    private Integer lockedStock;
    private Integer version;
    /**
     * 仓库名称(从Warehouse查询)
     */
    private String warehouseName;

    /**
     * 商品名称 (从 Product 查询)
     */
    private String productName;

    /**
     * 商品SKU（从wms_product表查）
     */
    private String skuCode;
}
