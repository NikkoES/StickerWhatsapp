package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.luckynineapps.stickersmaker.emoticonselectorfragments.DefaultFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class EmoticonAdapter extends RecyclerView.Adapter<EmoticonAdapter.ViewHolder> {
    Context context;
    ArrayList<Emoticon> emoticons;
    Fragment fr;

    public EmoticonAdapter(Context context, ArrayList<Emoticon> emoticons, Fragment fr) {
        this.context = context;
        this.emoticons = emoticons;
        this.fr = fr;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.emoticon, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final Emoticon emoticon = emoticons.get(position);
        Picasso.get().load(new File(emoticon.getPath())).into(vh.emoticon);
        vh.container01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fr instanceof DefaultFragment) {
                    ((DefaultFragment)fr).selectEmoticon(emoticon);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return emoticons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container01;
        public ImageView emoticon;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            emoticon = view.findViewById(R.id.emoticon);
        }
    }
}
