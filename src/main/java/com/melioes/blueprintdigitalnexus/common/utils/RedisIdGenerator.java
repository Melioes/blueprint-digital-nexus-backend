package com.melioes.blueprintdigitalnexus.common.utils;

import com.melioes.blueprintdigitalnexus.common.constant.DateConstant;
import com.melioes.blueprintdigitalnexus.common.constant.RedisKeyConstant;
import com.melioes.blueprintdigitalnexus.common.service.SequenceSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 业务 ID 生成器
 * 增强版：启动同步 + 数据库兜底 + 双重校验
 */
@Component
public class RedisIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(RedisIdGenerator.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 存储所有业务模块的同步服务
     */
    private final Map<String, SequenceSyncService> syncServiceMap = new ConcurrentHashMap<>();

    /**
     * 注册同步服务
     */
    public void registerSyncService(SequenceSyncService service) {
        syncServiceMap.put(service.getBusinessPrefix(), service);
        log.info("注册序号同步服务: prefix={}", service.getBusinessPrefix());
    }

    /**
     * 根据业务前缀生成每日递增的唯一编码（数字）
     */
    public long generateId(String businessPrefix) {
        return generateId(businessPrefix, 24);
    }

    /**
     * 根据业务前缀生成每日递增的唯一编码（数字）
     * 增强版：Redis 优先，失败时数据库兜底
     *
     * @param businessPrefix 业务前缀
     * @param expireHours    过期时间（小时）
     * @return 自增序号
     */
    public long generateId(String businessPrefix, int expireHours) {
        String dateStr = LocalDate.now().format(DateConstant.FORMATTER_YYYYMMDD);
        String key = RedisKeyConstant.KEY_PREFIX_CODE + businessPrefix + ":" + dateStr;

        try {
            Long increment = redisTemplate.opsForValue().increment(key);

            if (increment != null && increment == 1) {
                redisTemplate.expire(key, expireHours, TimeUnit.HOURS);
            }

            return increment != null ? increment : getSequenceFromDatabase(businessPrefix, dateStr);

        } catch (Exception e) {
            log.warn("Redis 生成序号失败，切换到数据库模式. key={}, error={}", key, e.getMessage());
            return getSequenceFromDatabase(businessPrefix, dateStr);
        }
    }

    /**
     * 生成完整编码（带补零）
     * 格式：Prefix-yyyyMMdd-001
     *
     * @param businessPrefix 业务前缀
     * @param zeroPadding    补零位数（如3位→001）
     * @return 完整编码
     */
    public String generateCode(String businessPrefix, int zeroPadding) {
        return generateCode(businessPrefix, zeroPadding, 24);
    }

    /**
     * 生成完整编码（带补零）
     *
     * @param businessPrefix 业务前缀
     * @param zeroPadding    补零位数
     * @param expireHours    过期时间（小时）
     * @return 完整编码
     */
    public String generateCode(String businessPrefix, int zeroPadding, int expireHours) {
        long id = generateId(businessPrefix, expireHours);
        String date = LocalDate.now().format(DateConstant.FORMATTER_YYYYMMDD);
        String sequence = String.format("%0" + zeroPadding + "d", id);
        return String.format("%s-%s-%s", businessPrefix, date, sequence);
    }

    /**
     * 从数据库获取今日序号
     * 兜底方案：查询今日数据库中最大序号 + 1
     */
    private long getSequenceFromDatabase(String businessPrefix, String dateStr) {
        // 获取序号同步服务
        SequenceSyncService syncService = syncServiceMap.get(businessPrefix);
        // 同步服务存在
        if (syncService != null) {
            // 获取今日最大序号
            int maxSequence = syncService.getTodayMaxSequence(dateStr);
            // 序号 + 1
            long nextSequence = maxSequence + 1;
            log.info("从数据库获取序号: prefix={}, date={}, max={}, next={}",
                    businessPrefix, dateStr, maxSequence, nextSequence);
            return nextSequence;
        }
        log.warn("未找到同步服务，使用时间戳: prefix={}", businessPrefix);
        return System.currentTimeMillis();
    }

    /**
     * 应用启动时同步：将数据库今日最大序号同步到 Redis
     * 防止 Redis 数据丢失导致序号重复
     */
    public void syncFromDatabase() {
        String dateStr = LocalDate.now().format(DateConstant.FORMATTER_YYYYMMDD);
        log.info("开始从数据库同步序号: date={}", dateStr);
        // 遍历所有业务模块
        for (Map.Entry<String, SequenceSyncService> entry : syncServiceMap.entrySet()) {
            // 获取业务前缀
            String prefix = entry.getKey();
            // 获取序号同步服务
            SequenceSyncService service = entry.getValue();

            // 尝试从数据库获取今日最大序号
            try {
                int dbMaxSequence = service.getTodayMaxSequence(dateStr);
                String key = RedisKeyConstant.KEY_PREFIX_CODE + prefix + ":" + dateStr;

                // 从 Redis 获取当前序号
                String currentRedisValue = redisTemplate.opsForValue().get(key);
                Long redisSequence = currentRedisValue != null ? Long.parseLong(currentRedisValue) : 0L;

                // 如果数据库序号大于 Redis 序号，则更新 Redis 无需拿数据库的最大值同步
                if (dbMaxSequence > redisSequence) {
                    redisTemplate.opsForValue().set(key, String.valueOf(dbMaxSequence), 24, TimeUnit.HOURS);
                    log.info("同步序号成功: prefix={}, db={}, redis={} -> {}",
                            prefix, dbMaxSequence, redisSequence, dbMaxSequence);
                } else {
                    log.info("序号无需同步: prefix={}, db={}, redis={}", prefix, dbMaxSequence, redisSequence);
                }
            } catch (Exception e) {
                log.error("同步序号失败: prefix={}", prefix, e);
            }
        }
    }

    /**
     * 获取回退ID（时间戳）- 保留兼容
     */
    @Deprecated
    private long getFallbackId() {
        return System.currentTimeMillis();
    }
}
