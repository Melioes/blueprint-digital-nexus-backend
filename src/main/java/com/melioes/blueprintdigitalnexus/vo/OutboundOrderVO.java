package com.melioes.blueprintdigitalnexus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 出库单视图对象
 */
public class OutboundOrderVO {
    /**
     * 出库单ID
     */
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
     * 仓库名称
     */
    private String warehouseName; // 关联字段：仓库名称
    /**
     * 状态
     */
    private Integer status;
    /**
     * 状态名称
     */
    private String statusName; // 关联字段：状态名称
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
    private LocalDateTime createTime;
    /**
     * 出库明细列表
     */
    private List<OutboundDetailVO> details; // 出库明细列表
}