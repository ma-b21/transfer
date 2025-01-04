package com.example.transfer.utils;

import android.util.Log;


import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.ArrayList;
import java.util.List;

public class QPSKModem {
    public static final int SAMPLE_RATE = 48000;  // 采样频率

    public static final double SIGNAL_FREQ = 2000;  // 信号频率
    public static final double AMPLITUDE = 1.0;  // 振幅
    public static final double SYMBOL_DURATION = 0.025;  // 每个符号的持续时间
    public static final int SAMPLES_PER_SYMBOL = (int)(SAMPLE_RATE * SYMBOL_DURATION);

    // 禁止实例化
    private QPSKModem() {
        throw new UnsupportedOperationException("此类不能被实例化");
    }

    // QPSK 调制方法
    public static short[] modulate(String binaryString) {
        if (binaryString.length() % 2 != 0) {
            throw new IllegalArgumentException("输入数据长度必须为偶数");
        }
        Python py = Python.getInstance();
        PyObject qpsk = py.getModule("QPSK");
        PyObject modulate = qpsk.callAttr("qpsk_modulate", binaryString, SYMBOL_DURATION, SAMPLE_RATE, SIGNAL_FREQ, AMPLITUDE);
        return modulate.toJava(short[].class);
    }

    /**
     * 解调方法
     * @param receivedSignal 接收到的信号
     * @return 解调后的二进制字符串
     */
    public static String demodulate(short[] receivedSignal) {
         Python py = Python.getInstance();
         PyObject qpsk = py.getModule("QPSK");
         PyObject demodulate = qpsk.callAttr("handle_receive", receivedSignal, SYMBOL_DURATION, SAMPLE_RATE, SIGNAL_FREQ);
         return demodulate.toJava(String.class);
    }

    public static int getSignalLength(String binarySignal) {
        return binarySignal.length() * SAMPLES_PER_SYMBOL / 2;
    }
}
