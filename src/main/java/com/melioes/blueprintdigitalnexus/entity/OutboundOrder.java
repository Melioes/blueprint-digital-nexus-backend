package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 入库单实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wms_outbound_order")
public class OutboundOrder {
    /**
     * 出库单ID
     */
    @TableId(value = "outbound_order_id", type = IdType.AUTO)
    private Long outboundOrderId;
    /**
     * 订单编号
     */
    private String orderNo;
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 状态 0 草稿 1 确认 2 取消
     */
    private Integer status;
    /**
     * 操作员
     */
    private String operator;
    /**
     * 备注
     */
    private String remark;

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

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}