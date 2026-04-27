package com.melioes.blueprintdigitalnexus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.melioes.blueprintdigitalnexus.dto.EmployeeDTO;
import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.query.UserQuery;
import com.melioes.blueprintdigitalnexus.vo.EmployeeVO;
import com.melioes.blueprintdigitalnexus.vo.LoginVO;

//@Service
public interface SysUserService extends IService<SysUser>  {
    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);


//    IPage<EmployeeVO> getUserPage(int page, int size, String keyword);


    /**
     *
     * @param query
     * @return
     */
    IPage<EmployeeVO> getUserPage(UserQuery query);

    /**
     * 新增用户
     * @param dto
     */
    void addUser(EmployeeDTO dto);


    /**
     * 修改用户
     * @param dto
     */
    void updateUser(EmployeeDTO dto);


    /**
     * 用户详情
     * @param id
     * @return EmployeeVO
     */
    EmployeeVO getUserDetail(Long id);
}
