package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "测试用接口")
public class TestController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/role/{userId}")
    @Operation(summary = "测试查询用户角色", description = "根据用户ID查询角色标识列表")
    public List<String> testRole(@PathVariable Long userId) {
        return sysUserMapper.selectRoleKeyList(userId);
    }
}