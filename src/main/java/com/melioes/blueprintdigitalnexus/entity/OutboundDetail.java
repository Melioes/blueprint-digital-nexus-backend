package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 出库单明细实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wms_outbound_detail")
public class OutboundDetail {
    /**
     * 出库单明细ID
     */
    @TableId(value = "outbound_detail_id", type = IdType.AUTO)
    private Long outboundDetailId;
    /**
     * 出库单ID
     */
    private Long outboundOrderId;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 数量
     */
    private Integer quantity;
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