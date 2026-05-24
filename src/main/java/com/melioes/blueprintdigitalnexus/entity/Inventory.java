package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 库存实体类
 * 对应数据库表：wms_inventory
 * 核心约束：(warehouse_id, product_id) 联合唯一
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wms_inventory")
public class Inventory {
    /**
     * 库存记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long inventoryId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品ID（关联wms_product表）
     */
    private Integer totalStock;

    /**
     * 锁定库存数量
     */
    private Integer lockedStock;


    /**
     * 乐观锁版本号
     * &#064;Version：MyBatis-Plus乐观锁注解，用于并发更新控制
     */
    @Version
    private  Integer version;

}
