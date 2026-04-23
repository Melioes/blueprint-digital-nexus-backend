package com.melioes.blueprintdigitalnexus.controller.admin;

import com.melioes.blueprintdigitalnexus.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/role/{userId}")
    public List<String> testRole(@PathVariable Long userId) {
        return sysUserMapper.selectRoleKeyList(userId);
    }
}