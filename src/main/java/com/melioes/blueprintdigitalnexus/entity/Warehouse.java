package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库实体
 */
@Data
@TableName("wms_warehouse")
public class Warehouse {

    /**
     * 仓库ID
     */
    @TableId(type = IdType.AUTO)
    private Long warehouseId;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 仓库地址
     */
    private String location;

    /**
     * 逻辑删除（0未删除 1已删除）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
