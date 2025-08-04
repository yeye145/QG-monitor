package com.qg.service.impl;

import com.qg.domain.Result;
import com.qg.service.UsersService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UsersServiceImpl implements UsersService {

    @Override
    public Map<String,Object> loginByPassword(String email, String password) {
        return null;
    }
}
