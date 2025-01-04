package com.example.transfer.receiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.transfer.utils.QPSKModem;
import com.example.transfer.utils.RealtimeAudioRecorder;
import com.example.transfer.utils.StringProcessor;

import org.w3c.dom.Text;


public class TransReceiver {
    private final RealtimeAudioRecorder recorder = new RealtimeAudioRecorder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView txtView;
    private  TextView binaryView;
    public void receive(TextView txtView, TextView binaryView){
        this.txtView = txtView;
        this.binaryView = binaryView;
        txtView.setText("正在接收数据...");
        recorder.startRecording();
    }

    public void extract_data(){
        Log.i("TRANS_APP", "start");
        Python py = Python.getInstance();
        PyObject pyHandler = py.getModule("QPSK1").get("handler");
        while (recorder.has_next()){
            assert pyHandler != null;
            pyHandler.callAttr("add_signal", (Object) recorder.next());
        }
        assert pyHandler != null;
        PyObject res = pyHandler.callAttr("handle_receive", QPSKModem.SYMBOL_DURATION, QPSKModem.SAMPLE_RATE, QPSKModem.SIGNAL_FREQ);
        String message = res.toJava(String.class);
        handler.post(() -> txtView.setText(""));
        handler.post(() -> txtView.setText(message));
        String binary = StringProcessor.encode_bytes_to_binary(StringProcessor.encode_string_to_bytes(message));
        handler.post(() -> binaryView.setText(binary));
        pyHandler.callAttr("clear_signal");
    }

    public void stop(){
        recorder.stopRecording();
        txtView.setText("正在解析数据...");
        new Thread(this::extract_data).start();
////        播放录音
//        short[] buffer = new short[0];
//        while(recorder.has_next()){
//            short[] signal = recorder.next();
//            short[] temp = new short[buffer.length + signal.length];
//            System.arraycopy(buffer, 0, temp, 0, buffer.length);
//            System.arraycopy(signal, 0, temp, buffer.length, signal.length);
//            buffer = temp;
//        }
//        AudioTrack audioTrack = new AudioTrack(
//                AudioManager.STREAM_MUSIC,
//                QPSKModem.SAMPLE_RATE,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                buffer.length,
//                AudioTrack.MODE_STREAM
//        );
//        audioTrack.play();
//        audioTrack.write(buffer, 0, buffer.length);
//        while (audioTrack.getPlaybackHeadPosition() < buffer.length) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
//        audioTrack.stop();
        }
}
