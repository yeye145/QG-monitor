package com.qg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.dto.UsersDTO;
import com.qg.mapper.UsersMapper;
import com.qg.service.UsersService;
import com.qg.utils.EmailService;
import com.qg.utils.HashSaltUtil;
import com.qg.utils.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.qg.domain.Code.*;
import static com.qg.utils.RedisConstants.*;
import static com.qg.utils.RedisConstants.LOGIN_CODE_KEY;

@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    EmailService emailService;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Map<String, Object> loginByPassword(String email, String password) {

        LambdaQueryWrapper<Users> lqw = new LambdaQueryWrapper<>();

        lqw.eq(Users::getEmail, email);
        System.out.println("登录邮箱：" + email);

        Users loginUser = usersMapper.selectOne(lqw);

        System.out.println(loginUser);


        if (loginUser == null || !HashSaltUtil.verifyHashPassword(password, loginUser.getPassword())) {
            return null;
        }
        //token验证
        String token = UUID.randomUUID().toString();
        UsersDTO userDto = BeanUtil.copyProperties(loginUser, UsersDTO.class);
        System.out.println(userDto);
        Map<String, Object> usermap = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fileName, fileValue) -> fileValue.toString()));


        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, usermap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", loginUser);
        return map;
    }

    @Override
    public Result register(Users user, String code) {

        // 判断参数非空
        if (user == null || code == null) {
            System.out.println("存在空参");
            return new Result(BAD_REQUEST, "存在空参");

        }
        // 获取用户邮箱，并做正则验证
        String email = user.getEmail().trim();
        if (RegexUtils.isEmailInvalid(email)) {
            System.out.println("邮箱格式错误");
            return new Result(BAD_REQUEST, "邮箱格式错误");
        }

        // 再查看验证码是否正确
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + email);
        if (cacheCode == null || !cacheCode.equals(code)) {
            System.out.println(cacheCode);
            System.out.println("用户输入的验证码：" + code);
            System.out.println("验证码错误");
            return new Result(NOT_FOUND, "验证码错误");
        }

        LambdaQueryWrapper<Users> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Users::getEmail, email);

        // 判断邮箱是否已经被注册
        Users one = usersMapper.selectOne(lqw);
        if (one != null) {
            System.out.println("该邮箱已被注册");
            return new Result(CONFLICT, "该邮箱已被注册！");
        }

        // 对密码进行加密处理
        user.setPassword(HashSaltUtil.creatHashPassword(user.getPassword()));

        // 自动生成一个初始姓名
        if(user.getUsername() == null || user.getUsername().trim().equals("")){
            user.setUsername("用户：" + RandomUtil.randomString(6));
        }

        // 向数据库中添加数据
        if (usersMapper.insert(user) != 1) {
            System.out.println("注册失败，请稍后重试");
            return new Result(INTERNAL_ERROR, "注册失败，请稍后重试");
        }
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getEmail, email);
        user= usersMapper.selectOne(wrapper);
        // 注册成功后删除验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + user.getEmail());
        // 随机生成token，作为的登录令牌
        String token = cn.hutool.core.lang.UUID.randomUUID().toString(true);
        // 7.2.将User对象转换为HashMap存储
        UsersDTO userDTO = BeanUtil.copyProperties(user, UsersDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fileName, fileValue) -> fileValue.toString()));
        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        Map<String, Object> map = new HashMap<>();
        map.put("user", userDTO);
        map.put("token", token);

        return new Result(CREATED, map, "恭喜你，注册成功！");
    }

    @Override
    public Result sendCodeByEmail(String email) {
        // 判断是否是无效邮箱地址
        if (RegexUtils.isEmailInvalid(email)) {
            return new Result(BAD_REQUEST, "邮箱格式错误");
        }
        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        System.out.println("验证码：" + code);

        // 发送验证码到邮箱
        // 3. 调用邮件工具类发送验证码
        boolean sendSuccess = emailService.sendSimpleEmail(
                email,
                "你的验证码",
                "尊敬的用户，你的验证码是：" + code + "，有效期2分钟。"
        );
        if (sendSuccess) {
            System.out.println("已发送验证码到邮箱到 " + email);
            // 保存验证码到 redis 中
            stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + email, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
            return new Result(SUCCESS, "验证码已发送至邮箱，请注意查收");
        } else {
            // 发送失败（可能是邮箱不存在或其他原因）
            return new Result(BAD_REQUEST, "验证码发送失败，请检查邮箱地址是否正确");
        }
    }

    //用户获取个人信息
    @Override
    public Result getUser(Long id) {
        if(id == null) {
            return new Result(BAD_REQUEST, "用户ID不能为空");
        }
        Users user = usersMapper.selectById(id);
        UsersDTO userDTO = BeanUtil.copyProperties(user, UsersDTO.class);
        if(user == null) {
            return new Result(NOT_FOUND, "用户不存在");
        }
        return new Result(SUCCESS, userDTO, "获取用户信息成功");
    }
}
