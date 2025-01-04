package com.example.transfer.utils;

import android.util.Log;

public class PackageModel {
    public static final byte START_FLAG1 = (byte) 0x5F;
    public static final byte START_FLAG2 = (byte) 0x35;

    private final Header header;
    private final byte[] data;
    public static class Header {
        private byte startFlag1;
        private byte startFlag2;
        private byte length;
        private byte number;
        private PackageType type;
        private byte checkSum;

        /**
         * 构造函数，构建一个新的数据包头
         */
        public Header() {
        }
        /**
         * 解析二进制数据包头
         * @param binaryHeader 二进制数据包头
         */
        public Header(String binaryHeader){
            byte[] headerData = StringProcessor.decode_binary_to_bytes(binaryHeader);
            this.startFlag1 = headerData[0];
            this.startFlag2 = headerData[1];
            this.length = headerData[2];
            this.type = headerData[3] == 0 ? PackageType.DATA : PackageType.END;
            this.number = headerData[4];
            this.checkSum = headerData[5];
        }
        public int getLength() {
            return length;
        }
        public int getNumber() {
            return number;
        }
    }

    public enum PackageType {
        DATA((byte) 0x0),
        END((byte) 0x1);

        private final byte type;
        PackageType(byte type) {
            this.type = type;
        }
    }

    /**
     * 构造函数，构建一个新的数据包
     * @param data 数据
     * @param type 数据包类型
     */
    public PackageModel(byte[] data, PackageType type, byte number) {
        this.data = data;
        this.header = new Header();
        this.header.startFlag1 = START_FLAG1;
        this.header.startFlag2 = START_FLAG2;
        this.header.length = (byte) data.length;
        Log.i("TRANS", "package_len" + data.length);
        this.header.number = number;
        this.header.type = type;
        this.header.checkSum = calculateCheckSum();
    }

    /**
     * 构造函数，解析一个数据包
     * @param packageData 数据包
     */
    public PackageModel(byte[] packageData) {
        this.header = new Header();
        this.header.startFlag1 = packageData[0];
        this.header.startFlag2 = packageData[1];
        this.header.length = packageData[2];
        this.header.type = packageData[3] == 0 ? PackageType.DATA : PackageType.END;
        this.header.number = packageData[4];
        this.header.checkSum = packageData[5];
        this.data = new byte[packageData.length - 6];
        System.arraycopy(packageData, 6, data, 0, data.length);
    }

    /**
     * 构造函数，解析一个数据包
     * @param binaryString 数据包的二进制字符串
     */
    public PackageModel(String binaryString) {
        this(StringProcessor.decode_binary_to_bytes(binaryString));
//        Log.i("TRANS_APP", "binaryString: " + binaryString);
    }

    /**
     * 计算校验和
     * @return 校验和
     */
    private byte calculateCheckSum() {
        byte checkSum = 0;
        checkSum ^= header.startFlag1;
        checkSum ^= header.startFlag2;
        checkSum ^= header.length;
        checkSum ^= header.type.type;
        checkSum ^= header.number;
        for (byte b : data) {
            checkSum ^= b;
        }
        return checkSum;
    }
    /**
     * 验证校验和
     * @return 是否校验成功
     */
    private boolean verifyCheckSum() {
        return header.checkSum == calculateCheckSum();
    }

    public byte[] toByteArray() {
        byte[] packageData = new byte[data.length + 6];
        packageData[0] = header.startFlag1;
        packageData[1] = header.startFlag2;
        packageData[2] = header.length;
        packageData[3] = header.type.type;
        packageData[4] = header.number;
        packageData[5] = header.checkSum;
        System.arraycopy(data, 0, packageData, 6, data.length);
        return packageData;
    }

    /**
     * 将数据包转换为二进制字符串
     * @return 二进制字符串
     */
    public String toBinaryString() {
        byte[] packageData = toByteArray();
        return StringProcessor.encode_bytes_to_binary(packageData);
    }

    /**
     * 获取数据
     * @return 数据
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 获取数据包序号
     * @return 数据包序号
     */
    public byte getNumber() {
        return header.number;
    }

    /**
     * 获取数据包类型
     * @return 数据包类型
     */
    public PackageType getType() {
        return header.type;
    }
}
