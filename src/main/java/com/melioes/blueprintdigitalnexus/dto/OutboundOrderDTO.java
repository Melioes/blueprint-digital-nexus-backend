package com.melioes.blueprintdigitalnexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 新增/修改出库单参数
 */
@Data
@NoArgsConstructor
public class OutboundOrderDTO {
    /**
     * 出库单ID  修改时用
     */
    private Long outboundOrderId;
    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 备注
     */
    private String remark;
    /**
     * 出库单明细列表
     */
    private List<OutboundDetailDTO> details; // 出库单明细列表
}