package com.melioes.blueprintdigitalnexus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.query.UserQuery;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户分页查询
     */
//    @RequiresRole({"ADMIN"})
    @GetMapping("/page")
    public Result<IPage<EmployeeVO>> page(UserQuery query) {
        log.info("分页查询用户: page={}, size={}, keyword={}",
                query.getPage(),
                query.getSize(),
                query.getKeyword()
        );

        // 查询用户分页（包含 page / size / keyword）
        return Result.success(sysUserService.getUserPage(query));
    }
    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> add(@RequestBody EmployeeDTO dto) {
        log.info("新增用户请求: {}", dto);
        sysUserService.addUser(dto);
        return Result.success();
    }

    /**
     * 删除用户（MP）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户id:{}", id);
        sysUserService.removeById(id);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> update(@RequestBody EmployeeDTO dto) {
        log.info("修改用户请求: {}", dto);
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 查询单个用户（MP）
     */
    @GetMapping("/{id}")
    public Result<EmployeeVO> detail(@PathVariable Long id) {
        log.info("查询用户详情, id={}", id);
        return Result.success(sysUserService.getUserDetail(id));
    }
}