package com.example.transfer.ui.receive;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.transfer.R;
import com.example.transfer.receiver.TransReceiver;

public class ReceiveFragment extends Fragment {

    private final TransReceiver receiver;
    public ReceiveFragment() {
        // Required empty public constructor
        receiver = new TransReceiver();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_receive, container, false);
        ToggleButton btn = root.findViewById(R.id.receive_button);
        TextView txtView = root.findViewById(R.id.receive_text);
        TextView binary = root.findViewById(R.id.receive_text_binary);

//        初始btn的背景设置为灰色， 字体为白色
        btn.setTextOff("点击开始接收");
        btn.setTextOn("点击停止接收");
        btn.setBackgroundColor(ContextCompat.getColor(this.requireContext(), R.color.gray));
        btn.setTextColor(ContextCompat.getColor(this.requireContext(), R.color.white));
        btn.setChecked(false);

        btn.setOnClickListener(
                v -> {
                    if (btn.isChecked()) {
                        btn.setBackgroundColor(ContextCompat.getColor(this.requireContext(), R.color.purple_500));
                        btn.setTextColor(ContextCompat.getColor(this.requireContext(), R.color.black));
                        receiver.receive(txtView, binary);
                    } else {
                        btn.setBackgroundColor(ContextCompat.getColor(this.requireContext(), R.color.gray));
                        btn.setTextColor(ContextCompat.getColor(this.requireContext(), R.color.white));
                        receiver.stop();
                    }
                }
        );
        return root;
    }
}