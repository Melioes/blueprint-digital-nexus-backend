package com.melioes.blueprintdigitalnexus.query;

import com.melioes.blueprintdigitalnexus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

// InboundOrderQuery - 出库单分页查询
@Data
@EqualsAndHashCode(callSuper = true)
public class OutboundOrderQuery extends PageQuery {
    private String orderNo; // 单号（模糊）
    private Long warehouseId; // 仓库ID
    private Integer status; // 状态
    private LocalDateTime startTime; // 创建时间范围-开始
    private LocalDateTime endTime; // 创建时间范围-结束
}