package com.example.transfer.utils;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RealtimeAudioRecorder {
    private final int bufferSize; // 缓冲区大小

    private final BlockingQueue<short[]> audioQueue = new LinkedBlockingQueue<>();
    private boolean isRecording = false;

    private final short[] buffer;
    private AudioRecord audioRecord;

    public RealtimeAudioRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(
                QPSKModem.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        ) * 4;
        Log.i("TRANS_APP", "bufferSize: " + bufferSize);
        buffer = new short[bufferSize];
    }

    // 启动录音并实时处理数据
    @SuppressLint("MissingPermission")
    public void startRecording() {
        isRecording = true;
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // 麦克风输入
                QPSKModem.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException("AudioRecord 初始化失败！");
        }

        audioRecord.startRecording();
        new Thread(() -> {
            while (true) {
                int read = audioRecord.read(buffer, 0, bufferSize);
                if (read < 0) {
                    Log.e("RealtimeAudioRecorder", "录音结束！");
                    break;
                }
                short[] data = Arrays.copyOf(buffer, read);
                try {
                    audioQueue.put(data);
                } catch (InterruptedException e) {
                    Log.e("RealtimeAudioRecorder", "录音数据入队失败！");
                    break;
                }
            }
        }).start();
    }

    // 停止录音
    public void stopRecording() {
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
    }

    public boolean has_next() {
        return !audioQueue.isEmpty();
    }

    public short[] next() {
        try {
            return audioQueue.take();
        } catch (InterruptedException e) {
            Log.e("RealtimeAudioRecorder", "获取录音数据失败！");
            return new short[0];
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }
}

