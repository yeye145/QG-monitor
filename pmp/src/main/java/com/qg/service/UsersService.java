package com.qg.service;


import com.qg.domain.Result;
import com.qg.domain.Users;

import java.util.Map;

public interface UsersService {
    Map<String,Object> loginByPassword(String email, String password);

    Result register(Users user, String code);

    Result sendCodeByEmail(String email);
}
