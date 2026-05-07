package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    // 继承 BaseMapper 后，基本的增删改查都已经有了
}