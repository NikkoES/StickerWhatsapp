package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.luckynineapps.stickersmaker.clipartselectorfragment.DefaultFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ClipArtAdapter extends RecyclerView.Adapter<ClipArtAdapter.ViewHolder> {
    Context context;
    ArrayList<ClipArt> clipArts;
    Fragment fr;

    public ClipArtAdapter(Context context, ArrayList<ClipArt> clipArts, Fragment fr) {
        this.context = context;
        this.clipArts = clipArts;
        this.fr = fr;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.clip_art, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int i) {
        final ClipArt clipArt = clipArts.get(i);
        Picasso.get().load(new File(clipArt.getPath())).into(vh.clipArt);
        vh.container01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fr instanceof DefaultFragment) {
                    ((DefaultFragment)fr).selectClipArt(clipArt);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return clipArts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container01;
        public ImageView clipArt;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            clipArt = view.findViewById(R.id.clip_art);
        }
    }
}
