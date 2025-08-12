package com.qg.controller;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.dto.EncryptedRequestDTO;
import com.qg.dto.EncryptionResultDTO;
import com.qg.dto.RegisterDTO;
import com.qg.dto.UsersDTO;
import com.qg.service.UsersService;
import com.qg.utils.CryptoUtils;
import com.qg.utils.FileUploadHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.qg.domain.Code.*;
import static com.qg.utils.FileUploadHandler.IMAGE_DIR;

@Slf4j
@Tag(name ="用户个人信息")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired  // Spring会自动注入配置好的ObjectMapper
    private ObjectMapper objectMapper;

    @Autowired
    private UsersService usersService;

    @Value("${rsa.key-pairs.pair2.private-key}") // 从配置读取RSA私钥
    private String rsaPrivateKey;

    @Value("${rsa.key-pairs.pair1.public-key}")
    private String rsaPublicKey;

    /**
     * 用户通过邮箱登录
     * @param
     * @return
     */

    @PostMapping("/password")
    public Result loginByPassword(@RequestBody EncryptedRequestDTO request) {
        try {
            log.info("接收到的参数: {}",request);
            // 1. 使用CryptoUtils解密请求
            String decryptedJson = CryptoUtils.decryptWithAESAndRSA(
                    request.getEncryptedData(),
                    request.getEncryptedKey(),
                    rsaPrivateKey
            );
            log.info("解密后的JSON: {}", decryptedJson);
            // 2. 解析JSON获取邮箱密码
            Map<String, String> params = new ObjectMapper()
                    .readValue(decryptedJson, new TypeReference<Map<String, String>>() {});
            String email = params.get("email");
            String password = params.get("password");
            log.info("用户邮箱: {}, 密码: {}", email, password);

            // 3. 业务处理
            Map<String, Object> map = usersService.loginByPassword(email, password);
            if (map == null) {
                return new Result(NOT_FOUND, "用户未注册");
            }
            Users user = (Users) map.get("user");
            if (user == null) {
                return new Result(BAD_REQUEST, "未注册");
            }

            // 4. 返回加密的结果
            map.put("user", BeanUtil.copyProperties(user, UsersDTO.class));

            // 5. 使用指定方法加密（关键修改点）

            String jsonData =objectMapper.writeValueAsString(map);//将map类型的数据转化为json字符串

            log.info("加密前的JSON: {}", jsonData);
            EncryptionResultDTO encryptionResultDTO = CryptoUtils.encryptWithAESAndRSA(jsonData, rsaPublicKey);
            log.info("加密后的JSON: {}", encryptionResultDTO.getEncryptedData());
            return new Result(SUCCESS, encryptionResultDTO , "登录成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用户注册
     * @param request 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody EncryptedRequestDTO request) {
        RegisterDTO registerDTO = new RegisterDTO();
        try {
        log.info("开始注册用户");
        String decryptedJson  = CryptoUtils.decryptWithAESAndRSA(
                request.getEncryptedData(),
                request.getEncryptedKey(),
                rsaPrivateKey
        );
        log.info("解密后的JSON: {}", decryptedJson);
        registerDTO = new ObjectMapper().readValue(decryptedJson, RegisterDTO.class);
        log.info("RegisterDTO: {}", registerDTO);

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
            log.error("用户注册异常，个人信息: {}", registerDTO, e);
            return new Result(INTERNAL_ERROR, "注册过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 发送验证码到邮箱
     * @param encryptedData
     * @param encryptedKey
     * @return
     */
    @GetMapping("/sendCodeByEmail")
    public Result sendCodeByEmail(@RequestParam("encryptedData") String encryptedData, @RequestParam("encryptedKey") String encryptedKey) {
        try {
            String decryptedJson  = CryptoUtils.decryptWithAESAndRSA(
                    encryptedData,
                    encryptedKey,
                    rsaPrivateKey
            );
            Map<String, String> params = new ObjectMapper()
                    .readValue(decryptedJson, new TypeReference<Map<String, String>>() {});
            String email = params.get("email");
            log.info("发送验证码到邮箱，邮箱: {}", email);
            // 发送验证码到邮箱
            return usersService.sendCodeByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
     * @param request
     * @return
     */
    @PutMapping("/findPassword")
    public Result findPassword(@RequestBody EncryptedRequestDTO request) {
        try {
            log.info("接收到的参数: {}",request);
            // 1. 使用CryptoUtils解密请求
            String decryptedJson = CryptoUtils.decryptWithAESAndRSA(
                    request.getEncryptedData(),
                    request.getEncryptedKey(),
                    rsaPrivateKey
            );
            log.info("解密后的JSON: {}", decryptedJson);
            // 2. 解析JSON获取邮箱密码
            Map<String, Object> params = new ObjectMapper()
                    .readValue(decryptedJson, new TypeReference<Map<String, Object>>() {});

            // 获取user对象并转换为Users类
            Map<String, Object> userMap = (Map<String, Object>) params.get("users");
            Users users = new ObjectMapper().convertValue(userMap, Users.class);
            log.info("用户信息: {}", users);

            // 获取code
            String code = (String) params.get("code");
            log.info("验证码: {}", code);
            return usersService.findPassword(users, code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新头像
     * @param file
     * @param userId
     * @return
     */
    @PostMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam("avatar") MultipartFile file,
                               @RequestParam("userId") Long userId) {
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return new Result(BAD_REQUEST, "请选择有效的头像文件");
            }

            // 判断是否为图片
            if (!FileUploadHandler.isValidImageFile(file)) {
                return new Result(Code.BAD_REQUEST, "上传的不是图片");
            }

            // 文件大小限制
            if (file.getSize() > 2 * 1024 * 1024) {
                return new Result(BAD_REQUEST, "图片大小不能超过2MB");
            }

            log.info("file ==> {}", file.getOriginalFilename());
            String avatarUrl = FileUploadHandler.saveFile(file, IMAGE_DIR);

            // 判断头像是否上传成功返回相应的结果
            if (usersService.updateAvatar(userId, avatarUrl)) {
                log.info("上传头像成功，url: {}", avatarUrl);
                return new Result(SUCCESS, avatarUrl, "头像上传成功");
            } else {
                return new Result(NOT_FOUND, "用户不存在");
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Result(INTERNAL_ERROR, "头像上传失败");
        }
    }

    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody EncryptedRequestDTO request) {
        log.info("接收到的参数: {}",request);
        // 1. 使用CryptoUtils解密请求
        String decryptedJson = null;
        try {
            decryptedJson = CryptoUtils.decryptWithAESAndRSA(
                    request.getEncryptedData(),
                    request.getEncryptedKey(),
                    rsaPrivateKey
            );
            log.info("解密后的JSON: {}", decryptedJson);
            Map<String, Object> params = new ObjectMapper()
                    .readValue(decryptedJson, new TypeReference<Map<String, Object>>() {});
            Users users = new ObjectMapper().convertValue(params.get("users"), Users.class);
            log.info("用户信息: {}", users);
            return usersService.updateUser(users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @PostMapping("/password1")
    public Result loginByPassword(@RequestBody Users user) {
        log.info("用户登录，邮箱: {}, 密码: {}", user.getEmail(), user.getPassword());
        return new Result(200,"登录成功");
    }
}
