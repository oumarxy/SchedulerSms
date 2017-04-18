package com.ngocsang.smscode;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyleszombathy.sms_scheduler.R;


public class AddMessageFragment extends Fragment {

    public AddMessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_add_message, container, false);
    }

}
