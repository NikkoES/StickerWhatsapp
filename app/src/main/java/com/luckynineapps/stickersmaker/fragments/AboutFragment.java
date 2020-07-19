package com.luckynineapps.stickersmaker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luckynineapps.stickersmaker.HomeActivity;
import com.luckynineapps.stickersmaker.R;

public class AboutFragment extends Fragment {
    View view;
    HomeActivity activity;
    TextView text02, text04;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (HomeActivity)getActivity();
        text02 = view.findViewById(R.id.text02);
        text04 = view.findViewById(R.id.text04);
        text04.setMovementMethod(new LinkMovementMethod());
        text02.setText(Html.fromHtml("<b>WhatsApp Sticker Maker</b> "+activity.getResources().getString(R.string.text34)));
        text04.setText(Html.fromHtml("Icon made by <a href=\"https://www.flaticon.com/authors/smashicons\">Smashicons</a> and <a href=\"https://www.flaticon.com/authors/freepik\">Freepik</a> from <a href=\"www.flaticon.com\">www.flaticon.com</a>"));
    }
}
