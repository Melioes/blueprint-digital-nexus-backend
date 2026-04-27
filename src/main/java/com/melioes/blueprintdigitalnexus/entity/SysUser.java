package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;
    //真实名称
    private String realName;
    //头像
    private String avatar;
    //账号启动禁用
    private Integer status;
//    防止误删 + 保留历史数据
    @TableLogic // 逻辑删除注解
    private Integer isDeleted;

//    @TableField(fill = FieldFill.INSERT) // 自动填充创建时间
    private LocalDateTime createTime;

//    @TableField(fill = FieldFill.INSERT_UPDATE) // 自动填充更新时间
    private LocalDateTime updateTime;
}