package com.luckynineapps.stickersmaker.emoticonselectorfragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luckynineapps.stickersmaker.Emoticon;
import com.luckynineapps.stickersmaker.EmoticonAdapter;
import com.luckynineapps.stickersmaker.EmoticonSelectorActivity;
import com.luckynineapps.stickersmaker.R;

import java.io.File;
import java.util.ArrayList;

public class DefaultFragment extends Fragment {
    View view;
    EmoticonSelectorActivity activity;
    RecyclerView emoticonList;
    ArrayList<Emoticon> emoticons;
    EmoticonAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_emoticon_selector_default, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (EmoticonSelectorActivity) getActivity();
        emoticonList = view.findViewById(R.id.emoticons);
        emoticonList.setLayoutManager(new GridLayoutManager(activity, 5));
        emoticonList.setItemAnimator(new DefaultItemAnimator());
        emoticons = new ArrayList<>();
        adapter = new EmoticonAdapter(activity, emoticons, this);
        emoticonList.setAdapter(adapter);
        File emoticonsFolder = new File(activity.getFilesDir(), "emoticons");
        for (File emoticonFile : emoticonsFolder.listFiles()) {
            Emoticon emoticon = new Emoticon();
            emoticon.setPath(emoticonFile.getAbsolutePath());
            emoticons.add(emoticon);
            adapter.notifyDataSetChanged();
        }
    }

    public void selectEmoticon(Emoticon emoticon) {
        Intent i = new Intent();
        i.putExtra("emoticon_path", emoticon.getPath());
        activity.setResult(Activity.RESULT_OK, i);
        activity.finish();
    }
}
