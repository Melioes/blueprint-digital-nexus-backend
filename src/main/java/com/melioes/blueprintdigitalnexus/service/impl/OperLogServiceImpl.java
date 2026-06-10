package com.melioes.blueprintdigitalnexus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melioes.blueprintdigitalnexus.entity.SysOperLog;
import com.melioes.blueprintdigitalnexus.mapper.OperLogMapper;
import com.melioes.blueprintdigitalnexus.service.OperLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志 Service 实现
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, SysOperLog> implements OperLogService {
}
