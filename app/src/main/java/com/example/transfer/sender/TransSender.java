package com.example.transfer.sender;

import static android.os.SystemClock.sleep;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.TextView;

import com.example.transfer.receiver.TransReceiver;
import com.example.transfer.utils.PackageModel;
import com.example.transfer.utils.PackageProcessor;
import com.example.transfer.utils.QPSKModem;
import com.example.transfer.utils.StringProcessor;

import java.util.Arrays;

public class TransSender {
    public void send(String message, TextView binaryTxt){
//        TODO： 实现发送逻辑
        Log.i("TRANS_APP", "send: " + message);

        PackageModel[] packages = PackageProcessor.generatePackages(message);
        String[] binarySignals = new String[packages.length];
        for (PackageModel p : packages) {
            binarySignals[p.getNumber()] = p.toBinaryString();
        }
        for (String binarySignal : binarySignals) {
            Log.i("TRANS_APP", "send: " + binarySignal + "  " + binarySignal.length());
            short[] signal = QPSKModem.modulate("11100010" + binarySignal);
            Log.i("TRANS_APP", "length: " + signal.length);
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    QPSKModem.SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    signal.length,
                    AudioTrack.MODE_STREAM
            );
            audioTrack.play();
            audioTrack.write(signal, 0, signal.length);
            while (audioTrack.getPlaybackHeadPosition() < signal.length) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
            sleep(300);
            audioTrack.stop();
            audioTrack.release();
        }
        String binary = StringProcessor.encode_bytes_to_binary(StringProcessor.encode_string_to_bytes(message));
        binaryTxt.setText(binary);
    }

}
