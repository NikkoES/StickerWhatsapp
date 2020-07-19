package com.luckynineapps.stickersmaker.clipartselectorfragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luckynineapps.stickersmaker.ClipArt;
import com.luckynineapps.stickersmaker.ClipArtAdapter;
import com.luckynineapps.stickersmaker.ClipArtSelectorActivity;
import com.luckynineapps.stickersmaker.R;

import java.util.ArrayList;

public class DefaultFragment extends Fragment {
    View view;
    ClipArtSelectorActivity activity;
    RecyclerView clipArtList;
    ArrayList<ClipArt> clipArts;
    ClipArtAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_clipart_selector_default, container, false);
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (ClipArtSelectorActivity)getActivity();
        Bundle args = getArguments();
        if (args != null) {
            clipArts = (ArrayList<ClipArt>)args.getSerializable("clip_arts");
        } else {
            clipArts = new ArrayList<>();
        }
        clipArtList = view.findViewById(R.id.cliparts);
        clipArtList.setLayoutManager(new GridLayoutManager(activity, 5));
        clipArtList.setItemAnimator(new DefaultItemAnimator());
        adapter = new ClipArtAdapter(activity, clipArts, this);
        clipArtList.setAdapter(adapter);
    }

    public void selectClipArt(ClipArt clipArt) {
        Intent i = new Intent();
        i.putExtra("clip_art_path", clipArt.getPath());
        activity.setResult(Activity.RESULT_OK, i);
        activity.finish();
    }
}
