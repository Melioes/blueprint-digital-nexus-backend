package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
