package com.roc.admin.backend.utils.crypt;

import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SM4Util {


    public static void main(String[] args) {
        String ori = "123========================================================================456";
//        SecretKey key = KeyUtil.generateKey(SM4.ALGORITHM_NAME,128);
        String key = "jkl;POIU1234+=d5";
        String encrypt = SmUtil.sm4(key.getBytes(StandardCharsets.UTF_8)).encryptBase64(ori);
        System.out.println(encrypt);
        String decrypt = SmUtil.sm4(key.getBytes(StandardCharsets.UTF_8)).decryptStr(encrypt, StandardCharsets.UTF_8);
        System.out.println(decrypt);
    }
}
