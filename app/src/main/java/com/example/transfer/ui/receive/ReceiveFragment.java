package com.example.transfer.ui.receive;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
        ImageButton btn = root.findViewById(R.id.receive_button);
        TextView txtView = root.findViewById(R.id.receive_text);

        btn.setOnClickListener(
                yv -> receiver.receive(txtView)
        );
        return root;
    }
}