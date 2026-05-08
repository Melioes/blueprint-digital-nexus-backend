package com.melioes.blueprintdigitalnexus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("==> 开始执行 MyBatis-Plus 自动插入填充...");
        // strictInsertFill(元对象, 属性名, 属性类型, 填充值)
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("==> 开始执行 MyBatis-Plus 自动更新填充...");
//        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 使用 setFieldValByName 可以强制填充，不受“是否为 null”的限制
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }


}