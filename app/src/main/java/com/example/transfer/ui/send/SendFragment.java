package com.example.transfer.ui.send;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.transfer.R;
import com.example.transfer.sender.TransSender;

public class SendFragment extends Fragment {
    private final TransSender sender;
    public SendFragment() {
        sender = new TransSender();
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_send, container, false);
        ImageButton btn = root.findViewById(R.id.send_button);
        EditText txt = root.findViewById(R.id.send_text);
        btn.setOnClickListener(
                v -> {
                    String message = txt.getText().toString();
                    sender.send(message);
                }
        );
        return root;
    }
}