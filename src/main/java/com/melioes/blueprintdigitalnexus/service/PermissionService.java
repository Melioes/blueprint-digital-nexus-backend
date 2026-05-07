package com.melioes.blueprintdigitalnexus.service;

import java.util.List;

public interface PermissionService {

    List<String> getUserPermissions(Long userId);

}