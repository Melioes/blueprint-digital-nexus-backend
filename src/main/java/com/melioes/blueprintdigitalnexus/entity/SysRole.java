package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long roleId;

    // 权限标识（核心字段）
    private String roleKey;

    // 角色名称（展示）
    private String roleName;

    // 描述
    private String description;

    // 状态：1正常 0禁用
    private Integer status;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) // 自动填充更新时间
    private LocalDateTime updateTime;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;
}