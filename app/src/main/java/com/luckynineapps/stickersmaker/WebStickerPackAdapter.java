package com.luckynineapps.stickersmaker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class WebStickerPackAdapter extends RecyclerView.Adapter<WebStickerPackAdapter.ViewHolder> {
    Context context;
    ArrayList<StickerPack> stickerPacks;
    int totalAdsLoaded = 0;

    public WebStickerPackAdapter(Context context, ArrayList<StickerPack> stickerPacks) {
        this.context = context;
        this.stickerPacks = stickerPacks;
    }

    @Override
    public int getItemViewType(int i) {
        if (i > 0 && ((i + 1) % 4) == 0) {
            //Tool.log("Item at position " + i + " is of type ad.");
            return StickerPack.TYPE_AD;
        } else {
            //Tool.log("Item at position " + i + " is of type sticker pack.");
        }
        return StickerPack.TYPE_STICKER_PACK;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == StickerPack.TYPE_AD) {
            view = LayoutInflater.from(context).inflate(R.layout.web_sticker_pack_ad, viewGroup, false);
            AdView ad = view.findViewById(R.id.ad);
            ad.loadAd(new AdRequest.Builder()
                    .build());
        } else if (viewType == StickerPack.TYPE_STICKER_PACK) {
            view = LayoutInflater.from(context).inflate(R.layout.web_sticker_pack, viewGroup, false);
        }
        //View view = LayoutInflater.from(context).inflate(R.layout.web_sticker_pack, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @SuppressWarnings("all")
    @Override
    public void onBindViewHolder(final ViewHolder vh, int position) {
        final StickerPack stickerPack = stickerPacks.get(position);
        if (stickerPack.getType() == StickerPack.TYPE_AD) {
        } else if (stickerPack.getType() == StickerPack.TYPE_STICKER_PACK) {
            try {
                vh.name.setText(stickerPack.getName());
                long size = stickerPack.getTotalSize();
                String sizeString = "";
                if (size >= 1024*1024) {
                    sizeString += Long.toString(size/1024/1024) + "MB";
                } else if (size >= 1024) {
                    sizeString += Long.toString(size/1024) + "KB";
                } else if (size < 1024) {
                    sizeString += Long.toString(size) + "B";
                }
                vh.size.setText(sizeString);
                if (!stickerPack.canBeAddedToWhatsApp) {
                    vh.add.setClickable(false);
                } else {
                    vh.add.setClickable(true);
                }
                if (stickerPack.getFiles().size() > 0) {
                    Picasso.get().load(new File(stickerPack.getFiles().get(0).getPath())).into(vh.sticker01Img);
                }
                if (stickerPack.getFiles().size() > 1) {
                    Picasso.get().load(new File(stickerPack.getFiles().get(1).getPath())).into(vh.sticker02Img);
                }
                if (stickerPack.getFiles().size() > 2) {
                    Picasso.get().load(new File(stickerPack.getFiles().get(2).getPath())).into(vh.sticker03Img);
                }
                if (stickerPack.getFiles().size() > 3) {
                    Picasso.get().load(new File(stickerPack.getFiles().get(3).getPath())).into(vh.sticker04Img);
                }
                vh.mainContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, ViewAllWebStickersActivity.class);
                        i.putExtra("id", stickerPack.getIdentifier());
                        i.putExtra("folder_path", stickerPack.getPath());
                        i.putExtra("name", stickerPack.getName());
                        context.startActivity(i);
                    }
                });
                vh.viewMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, ViewAllWebStickersActivity.class);
                        i.putExtra("id", stickerPack.getIdentifier());
                        i.putExtra("folder_path", stickerPack.getPath());
                        i.putExtra("name", stickerPack.getName());
                        context.startActivity(i);
                    }
                });
                vh.add.setOnClickListener(new View.OnClickListener() {

                    @SuppressWarnings("all")
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
                        Tool.addStickersToWhatsApp2(context, stickerPack);
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    @SuppressWarnings({"all", "unchecked"})
    public void addStickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyURL, String licenseAgreementURL, ArrayList<Sticker> stickers) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String stickerPacksString = sp.getString("sticker_packs", "");
        ArrayList<StickerPack> stickerPacks;
        if (stickerPacksString.equals("")) {
            stickerPacks = new ArrayList<>();
        } else {
            stickerPacks = (ArrayList<StickerPack>) ObjectSerializer.deserialize(stickerPacksString);
        }
        StickerPack pack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyURL, licenseAgreementURL);
        pack.setStickers(stickers);
        boolean packAlreadyAdded = false;
        int previousPackIndex = 0;
        for (int i = 0; i < stickerPacks.size(); i++) {
            StickerPack stickerPack = stickerPacks.get(i);
            if (stickerPack.identifier.equals(identifier)) {
                previousPackIndex = i;
                packAlreadyAdded = true;
                break;
            }
        }
        if (packAlreadyAdded) {
            stickerPacks.set(previousPackIndex, pack);
        } else {
            stickerPacks.add(pack);
        }
        e.putString("sticker_packs", ObjectSerializer.serialize(stickerPacks));
        e.commit();
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView size;
        /*public RecyclerView stickerList;
        public ArrayList<FileItem> files;
        public WebStickerAdapter adapter;*/
        public RelativeLayout mainContainer;
        public RelativeLayout sticker01;
        public ImageView sticker01Img;
        public RelativeLayout sticker02;
        public ImageView sticker02Img;
        public RelativeLayout sticker03;
        public ImageView sticker03Img;
        public RelativeLayout sticker04;
        public ImageView sticker04Img;
        public RelativeLayout viewMore;
        public RelativeLayout add;
        //public RelativeLayout viewMore;
        //public ProgressBar progress;
        public View line01;
        public AdView ad;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            size = view.findViewById(R.id.size);
            //progress = view.findViewById(R.id.progress);
            line01 = view.findViewById(R.id.line01);
            add = view.findViewById(R.id.add);
            ad = view.findViewById(R.id.ad);
            mainContainer = view.findViewById(R.id.main_container);
            sticker01 = view.findViewById(R.id.sticker01);
            sticker02 = view.findViewById(R.id.sticker02);
            sticker03 = view.findViewById(R.id.sticker03);
            sticker04 = view.findViewById(R.id.sticker04);
            //viewMore = view.findViewById(R.id.view_more);
            sticker01Img = view.findViewById(R.id.sticker01_img);
            sticker02Img = view.findViewById(R.id.sticker02_img);
            sticker03Img = view.findViewById(R.id.sticker03_img);
            sticker04Img = view.findViewById(R.id.sticker04_img);
            viewMore = view.findViewById(R.id.view_more);
            /*stickerList = view.findViewById(R.id.stickers);
            stickerList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            stickerList.setItemAnimator(new DefaultItemAnimator());
            files = new ArrayList<>();
            adapter = new WebStickerAdapter(context, files);
            stickerList.setAdapter(adapter);*/
        }
    }
}
