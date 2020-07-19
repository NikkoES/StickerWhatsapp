package com.luckynineapps.stickersmaker.emoticonselectorfragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luckynineapps.stickersmaker.EmoticonSelectorActivity;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.Tool;

import java.io.File;
import java.util.UUID;

public class CustomFragment extends Fragment {
    private final int SELECT_EMOTICON = 1;
    View view;
    EmoticonSelectorActivity activity;
    Button select;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_emoticon_selector_custom, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (EmoticonSelectorActivity)getActivity();
        select = view.findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tool.selectImage(CustomFragment.this, activity, R.string.text55, SELECT_EMOTICON);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_EMOTICON) {
                File emoticonFile = new File(activity.getFilesDir(), UUID.randomUUID().toString()+".png");
                Tool.extractURIToFile(activity, data.getData(), emoticonFile);
                Intent i = new Intent();
                i.putExtra("emoticon_path", emoticonFile.getAbsolutePath());
                activity.setResult(Activity.RESULT_OK, i);
                activity.finish();
            }
        }
    }
}
