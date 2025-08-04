package com.qg.service;


import java.util.Map;

public interface UsersService {
    Map<String,Object> loginByPassword(String email, String password);
}
