package com.example.transfer.utils;

public class QPSKModem {
    // 采样率和符号周期
    private static final int SAMPLE_RATE = 44100;
//    private static final double SYMBOL_TIME = 0.05; // 20个符号每秒
    private static final int SYMBOL_RATE = 20;
    private static final int SAMPLES_PER_SYMBOL = SAMPLE_RATE / SYMBOL_RATE;
    private static final double PI = Math.PI;

    // QPSK星座点
    private static final double[] QPSK_PHASES = { PI / 4, 3 * PI / 4, 5 * PI / 4, 7 * PI / 4 };
    private QPSKModem() {
        throw new UnsupportedOperationException("此类不能被实例化");
    }

    // 将二进制字符串转换为比特数组
    private static int[] binaryStringToBitArray(String binaryString) {
        int[] bitArray = new int[binaryString.length()];
        for (int i = 0; i < binaryString.length(); i++) {
            bitArray[i] = binaryString.charAt(i) == '1' ? 1 : 0;
        }
        return bitArray;
    }

    // 将比特数组转换回二进制字符串
    private static String bitArrayToBinaryString(int[] bitArray) {
        StringBuilder sb = new StringBuilder();
        for (int bit : bitArray) {
            sb.append(bit);
        }
        return sb.toString();
    }

    // QPSK 调制方法
    public static short[] modulate(String binaryString) {
        int[] bitStream = binaryStringToBitArray(binaryString);
        int numSymbols = bitStream.length / 2;
        double[] signal = new double[numSymbols * SAMPLES_PER_SYMBOL];

        for (int i = 0; i < numSymbols; i++) {
            // 每两位比特组成一个符号
            int bit1 = bitStream[2 * i];
            int bit2 = bitStream[2 * i + 1];

            int symbolIndex = (bit1 << 1) | bit2;
            double phase = QPSK_PHASES[symbolIndex];

            // 生成该符号对应的正弦波
            for (int j = 0; j < SAMPLES_PER_SYMBOL; j++) {
                signal[i * SAMPLES_PER_SYMBOL + j] = Math.cos(2 * PI * SYMBOL_RATE * j / SAMPLE_RATE + phase);
            }
        }

        // 将信号归一化到 16 位
        short[] normalizedSignal = new short[signal.length];
        for (int i = 0; i < signal.length; i++) {
            normalizedSignal[i] = (short) (signal[i] * Short.MAX_VALUE);
        }

        return normalizedSignal;
    }

    // QPSK解调方法
    public static String demodulate(short[] receivedSignal) {
        double[] receivedSignalDouble = new double[receivedSignal.length];
        for (int i = 0; i < receivedSignal.length; i++) {
            receivedSignalDouble[i] = receivedSignal[i] / (double) Short.MAX_VALUE;
        }
        int numSymbols = receivedSignal.length / SAMPLES_PER_SYMBOL;
        int[] demodulatedBits = new int[numSymbols * 2];

        for (int i = 0; i < numSymbols; i++) {
            double sumI = 0;
            double sumQ = 0;

            // 提取 I 和 Q 分量
            for (int j = 0; j < SAMPLES_PER_SYMBOL; j++) {
                double sample = receivedSignalDouble[i * SAMPLES_PER_SYMBOL + j];
                sumI += sample * Math.cos(2 * PI * SYMBOL_RATE * j / SAMPLE_RATE);
                sumQ += -sample * Math.sin(2 * PI * SYMBOL_RATE * j / SAMPLE_RATE);
            }

            // 确定相位象限
            int bit1 = sumI > 0 ? 0 : 1;
            int bit2 = sumQ > 0 ? 0 : 1;

            demodulatedBits[2 * i] = bit1;
            demodulatedBits[2 * i + 1] = bit2;
        }

        return bitArrayToBinaryString(demodulatedBits);
    }
}
