package com.example.transfer.sender;

import android.util.Log;

import com.example.transfer.utils.PackageModel;
import com.example.transfer.utils.PackageProcessor;
import com.example.transfer.utils.QPSKModem;
import com.example.transfer.utils.StringProcessor;

public class TransSender {
    public void send(String message){
//        TODO： 实现发送逻辑
        Log.i("TRANS", "send");
        PackageModel[] packages = PackageProcessor.generatePackages(message);
        for (PackageModel p : packages) {
            // 用audioTrack播放信号


        }
    }
}
