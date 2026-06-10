package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库存变动日志实体
 * 对应数据库表 wms_stock_log
 *
 * 每次库存发生变化时自动记录：
 * - 入库确认 → change_qty 为正数，type = IN
 * - 出库确认 → change_qty 为负数，type = OUT
 */
@Data
@NoArgsConstructor
@TableName("wms_stock_log")
public class StockLog {
    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 变动数量（正数=入库增加，负数=出库减少）
     */
    private Integer changeQty;

    /**
     * 变动类型：IN=入库，OUT=出库
     */
    private String type;

    /**
     * 业务单据ID（入库单ID 或 出库单ID）
     */
    private Long bizId;

    /**
     * 单据编号快照（如 OUT-20260610-001）
     * 写入时直接存储，不依赖业务表
     */
    private String orderNo;

    /**
     * 变动时间
     */
    private LocalDateTime createTime;
}