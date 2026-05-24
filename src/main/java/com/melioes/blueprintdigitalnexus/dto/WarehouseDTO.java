package com.melioes.blueprintdigitalnexus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 仓库DTO
 */
@Data
public class WarehouseDTO {

    /**
     * 仓库ID（修改时必填）
     */
    private Long warehouseId;

    /**
     * 仓库编码（新增时后端生成）
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 50, message = "仓库名称长度不能超过50个字符")
    private String warehouseName;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 仓库地址
     */
    private String location;
}
