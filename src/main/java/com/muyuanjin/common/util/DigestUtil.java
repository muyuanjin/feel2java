package com.muyuanjin.common.util;

import com.muyuanjin.compiler.util.Throws;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author muyuanjin
 */
public class DigestUtil {
    public static String sha256Hex(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str == null ? null : str.getBytes(StandardCharsets.UTF_8));
            return new String(encodeHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw Throws.sneakyThrows(e);
        }
    }

    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static char[] encodeHex(byte[] bytes) {
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }
}
