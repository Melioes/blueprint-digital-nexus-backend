package com.melioes.blueprintdigitalnexus;

import com.melioes.blueprintdigitalnexus.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PasswordResetTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Test
    public void initPassword() {
        passwordResetService.resetPassword("admin", "123456");
        passwordResetService.resetPassword("test1", "123456");
    }
}