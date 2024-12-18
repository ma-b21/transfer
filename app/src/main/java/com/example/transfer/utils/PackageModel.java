package com.example.transfer.utils;

public class PackageModel {
    public static final byte START_FLAG = 0x2;
    public static final byte END_FLAG = 0x3;

    private final Header header;
    private final byte[] data;
    private final byte tail = END_FLAG;
    private static class Header {
        private byte startFlag;
        private byte length;
        private byte number;
        private PackageType type;
        private byte checkSum;
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
        this.header.startFlag = START_FLAG;
        this.header.length = (byte) data.length;
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
        this.header.startFlag = packageData[0];
        this.header.length = packageData[1];
        this.header.type = packageData[2] == 0 ? PackageType.DATA : PackageType.END;
        this.header.number = packageData[3];
        this.header.checkSum = packageData[4];
        this.data = new byte[packageData.length - 6];
        System.arraycopy(packageData, 5, data, 0, data.length);
    }

    /**
     * 构造函数，解析一个数据包
     * @param binaryString 数据包的二进制字符串
     */
    public PackageModel(String binaryString) {
        this(StringProcessor.decode_binary_to_bytes(binaryString));
    }

    /**
     * 计算校验和
     * @return 校验和
     */
    private byte calculateCheckSum() {
        byte checkSum = 0;
        checkSum ^= header.startFlag;
        checkSum ^= header.length;
        checkSum ^= header.type.type;
        checkSum ^= header.number;
        for (byte b : data) {
            checkSum ^= b;
        }
        checkSum ^= tail;
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
        packageData[0] = header.startFlag;
        packageData[1] = header.length;
        packageData[2] = header.type.type;
        packageData[3] = header.number;
        packageData[4] = header.checkSum;
        System.arraycopy(data, 0, packageData, 5, data.length);
        packageData[packageData.length - 1] = tail;
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
