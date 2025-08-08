package com.qg.controller;

import cn.hutool.core.bean.BeanUtil;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.dto.RegisterDTO;
import com.qg.dto.UsersDTO;
import com.qg.service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.qg.domain.Code.*;
@Slf4j
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
            return new Result(NOT_FOUND, "用户未注册或密码错误");
        }
        Users user = (Users) map.get("user");
        if (user == null) {
            return new Result(BAD_REQUEST, "未注册");
        }
        Long id = user.getId();

        map.put("user", BeanUtil.copyProperties(user, UsersDTO.class));
        return new Result(SUCCESS, map, "登录成功");
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        // 参数校验
        if (registerDTO == null) {
            log.warn("注册失败，请求参数为空");
            return new Result(BAD_REQUEST, "注册信息不能为空");
        }

        if (registerDTO.getUsers() == null) {
            log.warn("注册失败，用户信息为空");
            return new Result(BAD_REQUEST, "用户信息不能为空");
        }

        log.info("开始注册用户，邮箱: {}", registerDTO.getUsers().getEmail());

        try {
            String code = registerDTO.getCode() != null ? registerDTO.getCode().trim() : "";
            if (code.isEmpty()) {
                log.warn("注册失败，验证码为空，邮箱: {}", registerDTO.getUsers().getEmail());
                return new Result(BAD_REQUEST, "验证码不能为空");
            }

            Result result = usersService.register(registerDTO.getUsers(), code);
            log.info("用户注册处理完成，邮箱: {}, 结果: {}",
                    registerDTO.getUsers().getEmail(), result.getCode());
            return result;
        } catch (Exception e) {
            log.error("用户注册异常，邮箱: {}", registerDTO.getUsers().getEmail(), e);
            return new Result(INTERNAL_ERROR, "注册过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 发送验证码到邮箱
     * @param email
     * @return
     */
    @GetMapping("/sendCodeByEmail")
    public Result sendCodeByEmail(@RequestParam("email") String email) {
        System.out.println(email);
        // 发送验证码到邮箱
        return usersService.sendCodeByEmail(email);
    }

    /**
     * 获取用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getUser(@PathVariable Long id) {
        return usersService.getUser(id);
    }


    /**
     * 找回密码
     * @param user
     * @param code
     * @return
     */
    @PutMapping("/findPassword/{code}")
    public Result findPassword(@RequestBody Users user, @PathVariable String code) {
        return usersService.findPassword(user, code);
    }
}
