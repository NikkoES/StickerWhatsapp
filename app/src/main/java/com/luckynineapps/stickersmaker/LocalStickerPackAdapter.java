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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.luckynineapps.stickersmaker.homefragments.LocalFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class LocalStickerPackAdapter extends RecyclerView.Adapter<LocalStickerPackAdapter.ViewHolder> {
    Context context;
    ArrayList<Folder> folders;

    public LocalStickerPackAdapter(Context context, ArrayList<Folder> folders) {
        this.context = context;
        this.folders = folders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_sticker_pack, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @SuppressWarnings("all")
    @Override
    public void onBindViewHolder(final ViewHolder vh, final int position) {
        final Folder folder = folders.get(position);
        if (folder.getFiles().size() == 0) {
            vh.empty.setVisibility(View.VISIBLE);
            vh.text01.setVisibility(View.VISIBLE);
            vh.stickerList.setVisibility(View.GONE);
        } else {
            vh.empty.setVisibility(View.GONE);
            vh.text01.setVisibility(View.GONE);
            vh.stickerList.setVisibility(View.VISIBLE);
        }
        String path = folder.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String folderName = path.substring(path.lastIndexOf("/") + 1, path.length());
        vh.name.setText(folderName);
        vh.add.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("all")
            @Override
            public void onClick(View view) {
                try {
                    if (!Tool.isPackageExists(context, "com.whatsapp")) {
                        Tool.show(context, R.string.text90);
                        return;
                    }
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
                    final ProgressDialog loadingDialog = new ProgressDialog(context);
                    loadingDialog.setMessage(context.getResources().getString(R.string.text26));
                    loadingDialog.setCancelable(false);
                    loadingDialog.show();
                    new AsyncTask<String, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(String... values) {
                            try {
                                if (folder.getFiles().size() < 4) {
                                    return 1;
                                }
                                String path = folder.getPath();
                                if (path.endsWith("/")) {
                                    path = path.substring(0, path.length() - 1);
                                }
                                String name = path.substring(path.lastIndexOf("/") + 1, path.length());
                                // Copy all stickers to data folder
                                File stickerFolder = new File(Tool.getDataDir(), "stickers/" + name);
                                if (!stickerFolder.exists()) {
                                    stickerFolder.mkdirs();
                                }
                                ArrayList<Sticker> stickers = new ArrayList<>();
                                //String trayImageName = "";
                                int looper = 0;
                                for (FileItem item : folder.getFiles()) {
                                    if (item.getType() == FileItem.TYPE_IMAGE) {
                                        if (new File(item.getPath()).isFile()) {
                                            String stickerPath = item.getPath();
                                            if (stickerPath.endsWith("/")) {
                                                stickerPath = stickerPath.substring(0, stickerPath.length() - 1);
                                            }
                                            // Convert local_sticker to WebP image
                                            Bitmap emptyPicture = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                                            Canvas cvs = new Canvas(emptyPicture);
                                            Paint p = new Paint();
                                            p.setAntiAlias(true);
                                            Bitmap picture = BitmapFactory.decodeFile(stickerPath);
                                            Bitmap newPicture = picture.createScaledBitmap(picture, 480, 480, true);
                                            picture.recycle();
                                            cvs.drawBitmap(newPicture, 16, 16, p);
                                            String stickerFileName = stickerPath.substring(stickerPath.lastIndexOf("/") + 1, stickerPath.length());
                                            stickerFileName = stickerFileName.substring(0, stickerFileName.lastIndexOf(".")) + ".webp";
                                            stickerFileName = stickerFileName.replace("%", "");
                                            stickerFileName = stickerFileName.replace(")", "");
                                            stickerFileName = stickerFileName.replace("(", "");
                                            File newStickerFile = new File(stickerFolder, stickerFileName);
                                            try {
                                                emptyPicture.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(newStickerFile));
                                                if (newStickerFile.length() >= 102400) {
                                                    emptyPicture.compress(Bitmap.CompressFormat.WEBP, 50, new FileOutputStream(newStickerFile));
                                                }
                                            } catch (Exception exp) {
                                                exp.printStackTrace();
                                            }
                                            if (looper == 0) {
                                                //trayImageName = stickerFileName;
                                                Bitmap trayBitmap = Bitmap.createScaledBitmap(emptyPicture, 96, 96, true);
                                                try {
                                                    trayBitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(new File(stickerFolder, "tray.webp")));
                                                } catch (Exception exp) {
                                                    exp.printStackTrace();
                                                }
                                                trayBitmap.recycle();
                                            }
                                            emptyPicture.recycle();
                                            newPicture.recycle();
                                            if (looper != 0) {
                                                Sticker sticker = new Sticker(stickerFileName, Arrays.asList(new String[]{
                                                        "☕", "☕"
                                                }));
                                                sticker.setPath(newStickerFile.getAbsolutePath());
                                                stickers.add(sticker);
                                            }
                                        }
                                        looper++;
                                    }
                                }
                                String id = name.replace("%", "");
                                addStickerPack(id, name, name, "tray.webp", "", "", "", "", stickers);
                                //addStickerPack("1", "DanaOS Stickers 1", "Dana", "/sdcard/tray.png", "", "", "", "", stickers);
                                Intent i = new Intent();
                                i.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                                i.putExtra("sticker_pack_id", id);
                                i.putExtra("sticker_pack_authority", "com.luckynineapps.stickersmaker.stickercontentprovider");
                                i.putExtra("sticker_pack_name", name);
                                context.startActivity(i);
                                int totalAdds = Tool.read(context, "total_adds", 0);
                                Tool.log("Total adds: " + totalAdds);
                                totalAdds++;
                                Tool.save(context, "total_adds", totalAdds);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Tool.show(context, "Error: " + e.getMessage());
                            }
                            return 0;
                        }

                        @Override
                        protected void onPostExecute(Integer value) {
                            super.onPostExecute(value);
                            loadingDialog.dismiss();
                            if (value == 1) { //Total stickers is less than 3
                                AlertDialog dialog = new AlertDialog.Builder(context)
                                        .setMessage(R.string.text17)
                                        .setPositiveButton("OK", null)
                                        .create();
                                dialog.show();
                            }
                        }
                    }.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    Tool.show(context, "Error: " + e.getMessage());
                }
            }
        });
        vh.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(context, vh.menu);
                menu.getMenuInflater().inflate(R.menu.local_sticker_pack, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.add_sticker) {
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
                            LocalFragment.getInstance().addSticker(folder, vh.adapter);
                        } else if (id == R.id.change_tray) {
                            LocalFragment.getInstance().changeTray(folder, vh.adapter);
                        } else if (id == R.id.delete) {
                            LocalFragment.getInstance().deleteStickerPack(folder, position);
                        }
                        return false;
                    }
                });
                menu.show();
            }
        });
        vh.files.clear();
        vh.files.addAll(folder.getFiles());
        Tool.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                vh.adapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings({"all", "unchecked"})
    public void addStickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyURL, String licenseAgreementURL, ArrayList<Sticker> stickers) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Tool.show(context, "Error: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container01;
        public TextView name;
        public RecyclerView stickerList;
        public ArrayList<FileItem> files;
        public LocalStickerAdapter adapter;
        public RelativeLayout add;
        public RelativeLayout menu;
        public ImageView empty;
        public TextView text01;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            name = view.findViewById(R.id.name);
            add = view.findViewById(R.id.add);
            menu = view.findViewById(R.id.menu);
            empty = view.findViewById(R.id.empty);
            text01 = view.findViewById(R.id.text01);
            stickerList = view.findViewById(R.id.stickers);
            stickerList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            stickerList.setItemAnimator(new DefaultItemAnimator());
            files = new ArrayList<>();
            adapter = new LocalStickerAdapter(context, files);
            stickerList.setAdapter(adapter);
        }
    }
}
