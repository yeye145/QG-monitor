package com.qg.controller;

import cn.hutool.core.bean.BeanUtil;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.dto.UsersDto;
import com.qg.service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.qg.domain.Code.*;

@Tag(name ="用户个人信息")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UsersService usersService;

    /**
     * 用户通过邮箱登录
     * @param email
     * @param password
     * @return
     */

    @GetMapping("/password")
    public Result loginByPassword(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> map = usersService.loginByPassword(email, password);
        if (map == null) {
            return new Result(NOT_FOUND, "用户未注册");
        }
        Users user = (Users) map.get("user");
        if (user == null) {
            return new Result(BAD_REQUEST, "未注册");
        }
        Long id = user.getId();

        map.put("user", BeanUtil.copyProperties(user, UsersDto.class));
        return new Result(SUCCESS, map, "登录成功");
    }
}
