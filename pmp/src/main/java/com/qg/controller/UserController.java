package com.qg.controller;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.dto.EncryptedRequestDTO;
import com.qg.dto.EncryptionResultDTO;
import com.qg.dto.RegisterDTO;
import com.qg.dto.UsersDTO;
import com.qg.service.UsersService;
import com.qg.utils.CryptoUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
            String jsonData = new ObjectMapper().writeValueAsString(map);//将map类型的数据转化为json字符串
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
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {

        log.info("开始注册用户");
        log.info("RegisterDTO: {}", registerDTO);
        return usersService.register(registerDTO.getUsers(), registerDTO.getCode().trim());
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



//    void testGenerateRSAKeyPair() {
//        try {
//            // 生成密钥对
//            KeyPair keyPair = generateRSAKeyPair(2048);
//
//            // 获取公钥和私钥
//            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//
//            // 输出Base64编码的密钥
//            System.out.println("公钥: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
//            System.out.println("私钥: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
//
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 生成RSA密钥对
//     * @param keySize 密钥长度(通常为1024, 2048, 4096等)
//     * @return KeyPair对象
//     * @throws NoSuchAlgorithmException
//     */
//    public static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(keySize);
//        return keyPairGenerator.generateKeyPair();
//    }
}
