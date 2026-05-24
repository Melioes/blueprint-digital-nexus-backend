package com.melioes.blueprintdigitalnexus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 商品分类实体类
 */
@Data
@TableName("wms_category")
public class ProductCategory {

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private String categoryName;
    // 商品分类编码
    private String categoryCode;
    // 父级分类ID
    private Long parentId; // 注意：对应数据库 parent_id
    private Integer sort;
    private Integer status;

    @TableLogic // MyBatis-Plus 逻辑删除注解[cite: 1]
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) // 自动填充创建时间
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 自动填充更新时间
    private LocalDateTime updateTime;
}
