package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luckynineapps.stickersmaker.homefragments.LocalFragment;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.startapp.android.publish.ads.nativead.NativeAdDetails;
import com.startapp.android.publish.ads.nativead.NativeAdPreferences;
import com.startapp.android.publish.ads.nativead.StartAppNativeAd;
import com.startapp.android.publish.adsCommon.Ad;
import com.startapp.android.publish.adsCommon.adListeners.AdEventListener;

import java.io.File;
import java.util.ArrayList;

public class LocalStickerAdapter extends RecyclerView.Adapter<LocalStickerAdapter.ViewHolder> {
    Context context;
    public ArrayList<FileItem> files;

    public LocalStickerAdapter(Context context, ArrayList<FileItem> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_sticker, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, int position) {
        final FileItem item = files.get(position);
        if (item.getType() == FileItem.TYPE_AD) {
            vh.container01.setVisibility(View.GONE);
            vh.viewMore.setVisibility(View.GONE);
            vh.ad.setVisibility(View.VISIBLE);
            final NativeAdPreferences pref = new NativeAdPreferences()
                    .setPrimaryImageSize(2)
                    .setAdsNumber(1)
                    .setAutoBitmapDownload(false);
            final StartAppNativeAd ad = new StartAppNativeAd(context);
            ad.loadAd(pref, new AdEventListener() {
                @Override
                public void onReceiveAd(Ad ad0) {
                    Tool.log("onReceiveAd()");
                    try {
                        final NativeAdDetails detail = ad.getNativeAds().get(0);
                        vh.adTitle.setText(detail.getTitle());
                        vh.adDesc.setText(detail.getDescription());
                        detail.sendImpression(context);
                        vh.ad.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Tool.log("Clicking ad...");
                                detail.sendClick(context);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailedToReceiveAd(Ad ad) {
                    Tool.log("Ad failed to load");
                    Tool.log("Error message: "+ad.getErrorMessage());
                }
            });
        } else {
            vh.ad.setVisibility(View.GONE);
            if (item.getType() == FileItem.TYPE_IMAGE) {
                vh.viewMore.setVisibility(View.GONE);
                vh.container01.setVisibility(View.VISIBLE);
            } else if (item.getType() == FileItem.TYPE_VIEW_MORE) {
                vh.container01.setVisibility(View.GONE);
                vh.viewMore.setVisibility(View.VISIBLE);
            }
            if (position == 0) {
                vh.tray.setVisibility(View.VISIBLE);
            } else {
                vh.tray.setVisibility(View.GONE);
            }
            Picasso.get().load(new File(item.getPath())).resize(128, 128).into(vh.img);
            String path = item.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (item.getType() == FileItem.TYPE_IMAGE) {
                vh.container01.setVisibility(View.VISIBLE);
                vh.viewMore.setVisibility(View.GONE);
            } else {
                vh.container01.setVisibility(View.GONE);
                vh.viewMore.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= 21) {
                    vh.viewMore.setBackgroundResource(R.drawable.ripple01);
                }
            }
            vh.viewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ViewAllStickersActivity.class);
                    i.putExtra("folder_path", item.getPath());
                    context.startActivity(i);
                }
            });
            String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            vh.name.setText(fileName);
            vh.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showStickerDialog(item);
                }
            });
            vh.container02.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showStickerDialog(item);
                }
            });
        }
    }

    public void showStickerDialog(final FileItem item) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_sticker, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(v)
                .create();
        ImageView img = v.findViewById(R.id.img);
        RelativeLayout add = v.findViewById(R.id.add);
        RelativeLayout edit = v.findViewById(R.id.edit);
        RelativeLayout delete = v.findViewById(R.id.delete);
        Picasso.get().load(new File(item.getPath())).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(img);
        Picasso.get().invalidate(new File(item.getPath()));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                View v = LayoutInflater.from(context).inflate(R.layout.add_to_pack, null);
                final EditText packNameField = v.findViewById(R.id.pack_name);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.text16)
                        .setView(v)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String packName = packNameField.getText().toString();
                                if (packName.equals("")) {
                                    Tool.show(context, R.string.text13);
                                    return;
                                }
                                File folder = new File(context.getFilesDir(), "customstickers/"+packName);
                                if (!folder.exists()) {
                                    folder.mkdirs();
                                }
                                // Check is package name exists
                                if (new File(folder, packName).exists()) {
                                    Tool.show(context, R.string.text15);
                                    return;
                                }
                                String path = item.getPath();
                                if (path.endsWith("/")) {
                                    path = path.substring(0, path.length() - 1);
                                }
                                String name = path.substring(path.lastIndexOf("/") + 1, path.length());
                                File newStickerFile = new File(folder, name);
                                Tool.copyFile(item.getPath(), newStickerFile.getAbsolutePath());
                                Tool.show(context, R.string.text14);
                            }
                        })
                        .setNegativeButton(R.string.text_cancel, null)
                        .create();
                dialog.show();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                LocalFragment.getInstance().editImage(LocalStickerAdapter.this, item);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage(R.string.text11)
                        .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                files.remove(item);
                                Tool.log("Deleting file: " + item.getPath());
                                new File(item.getPath()).delete();
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.text_no, null)
                        .create();
                dialog.show();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        dialog.getWindow().setLayout(Tool.dpToPx(context, 350), window.getAttributes().height);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView container01;
        public RelativeLayout container02;
        public RelativeLayout viewMore;
        public ImageView img;
        public ImageView tray;
        public TextView name;
        public CardView ad;
        public TextView adTitle, adDesc;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            container02 = view.findViewById(R.id.container02);
            viewMore = view.findViewById(R.id.view_more);
            img = view.findViewById(R.id.img);
            tray = view.findViewById(R.id.tray);
            name = view.findViewById(R.id.name);
            ad = view.findViewById(R.id.ad);
            adTitle = view.findViewById(R.id.ad_title);
            adDesc = view.findViewById(R.id.ad_desc);
        }
    }
}
