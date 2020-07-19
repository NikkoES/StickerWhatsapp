package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class WebStickerAdapter extends RecyclerView.Adapter<WebStickerAdapter.ViewHolder> {
    Context context;
    public ArrayList<FileItem> files;

    public WebStickerAdapter(Context context, ArrayList<FileItem> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.web_sticker, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final FileItem item = files.get(position);
        Picasso.get().load(new File(item.getPath())).resize(128, 128).into(vh.img);
        if (item.getType() == FileItem.TYPE_IMAGE) {
            vh.container01.setVisibility(View.VISIBLE);
            vh.viewMore.setVisibility(View.GONE);
        } else if (item.getType() == FileItem.TYPE_VIEW_MORE) {
            vh.container01.setVisibility(View.GONE);
            vh.viewMore.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= 21) {
                vh.viewMore.setBackgroundResource(R.drawable.ripple01);
            }
        }
        vh.viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final InterstitialAd ad = new InterstitialAd(context);
                ad.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                ad.setAdListener(new AdListener() {

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        ad.show();
                    }
                });
                ad.loadAd(new AdRequest.Builder().build());
                Intent i = new Intent(context, ViewAllStickersActivity.class);
                i.putExtra("folder_path", item.getPath());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container01;
        public RelativeLayout viewMore;
        public ImageView img;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            viewMore = view.findViewById(R.id.view_more);
            img = view.findViewById(R.id.img);
        }
    }
}
