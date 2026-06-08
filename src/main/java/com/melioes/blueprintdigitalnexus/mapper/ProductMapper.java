package com.melioes.blueprintdigitalnexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melioes.blueprintdigitalnexus.entity.Product;
import org.apache.ibatis.annotations.Mapper;
/**
 * 商品 Mapper 继承 BaseMapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

}
