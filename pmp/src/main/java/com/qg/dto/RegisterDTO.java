package com.qg.dto;

import com.qg.domain.Users;

public class RegisterDTO {
    private Users users;
    private String code;


    public RegisterDTO() {
    }

    public RegisterDTO(Users users, String code) {
        this.users = users;
        this.code = code;
    }

    /**
     * 获取
     * @return user
     */
    public Users getUsers() {
        return users;
    }

    /**
     * 设置
     * @param users
     */
    public void setUser(Users users) {
        this.users = users;
    }

    /**
     * 获取
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    public String toString() {
        return "RegisterDTO{users = " + users + ", code = " + code + "}";
    }
}
