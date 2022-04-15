package com.example.my_seckill.utils;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MD5Utils {
    private static final String SALT = "1a2b3c4d";

    public static String md5(String src) { // md5 值
        return DigestUtils.md5Hex(src);
    }

    // 前端加密
    public static String inputPassToFormPass(String inputPass) {
        String str = "" + SALT.charAt(0) + SALT.charAt(2) + inputPass + SALT.charAt(5) + SALT.charAt(4);
        return md5(str);
    }

    // 后端加密，随机salt
    public static String formPassToDbPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    // 前端到数据库的密码
    public static String inputPassToDbPass(String inputPass, String salt) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDbPass(formPass, salt);
        return dbPass;
    }

    public static void main(String[] args) {
        String formPass = inputPassToFormPass("123456");
        String dbPass = formPassToDbPass(formPass, SALT);
        String dbPass1 = inputPassToDbPass("123456", SALT);

        System.out.println("formPass = " + formPass);
        System.out.println("dbPass = " + dbPass);
        System.out.println("dbPass1 = " + dbPass1);
//        String s = inputPassToDbPass("123456", SALT);
    }


}
