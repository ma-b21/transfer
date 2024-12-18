package com.example.transfer.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StringProcessor {
    // 私有构造函数，防止实例化
    private StringProcessor() {
        throw new UnsupportedOperationException("此类不能被实例化");
    }

    // 编码方法，将字符串编码为字节序列
    public static byte[] encode_string_to_bytes(String input) {
        return input.getBytes(StandardCharsets.ISO_8859_1);
    }

    // 解码方法，将字节序列解码为字符串
    public static String decode_bytes_to_string(byte[] bytesInput) {
        return new String(bytesInput, StandardCharsets.ISO_8859_1);
    }

    public static String encode_bytes_to_binary(byte[] byteData) {
        // 构建二进制字符串
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteData) {
            // 将每个字节转换为二进制格式，并填充到 8
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryString.toString();
    }

    public static byte[] decode_binary_to_bytes(String binaryString) {
        // 创建字符列表以存储二进制字符串
        ArrayList<String> binaryChars = new ArrayList<>();
        for (int i = 0; i < binaryString.length(); i += 8) {
            // 将二进制字符串分割为 8 位字符
            binaryChars.add(binaryString.substring(i, i + 8));
        }

        // 创建字节数组以存储解码后的字节
        byte[] byteData = new byte[binaryChars.size()];

        for (int i = 0; i < binaryChars.size(); i++) {
            // 将二进制字符串转换为字节
            byteData[i] = (byte) Integer.parseInt(binaryChars.get(i), 2);
        }
        return byteData;
    }
}
