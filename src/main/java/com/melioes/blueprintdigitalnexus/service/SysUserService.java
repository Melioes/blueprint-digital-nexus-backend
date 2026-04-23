package com.melioes.blueprintdigitalnexus.service;

import com.melioes.blueprintdigitalnexus.dto.LoginDTO;
import com.melioes.blueprintdigitalnexus.dto.RegisterDTO;
import org.springframework.stereotype.Service;

@Service
public interface SysUserService {
    String login(LoginDTO dto);

    void register(RegisterDTO dto);
}
