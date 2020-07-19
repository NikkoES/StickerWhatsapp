package com.luckynineapps.stickersmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ViewAllWebStickersActivity extends AppCompatActivity {
    String stickerPackID;
    String stickerPackName;
    String folderPath = "";
    RecyclerView stickerList;
    ArrayList<FileItem> files;
    AllWebStickerAdapter adapter;
    ProgressBar progress;
    TextView text01;
    ArrayList<String> stickerPaths;
    ArrayList<String> stickerURLs;
    boolean infoCanBeSeen = false;
    ImageView icon;
    TextView nameView, sizeView;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_stickers);
        setTitle(R.string.text12);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        stickerPackID = getIntent().getStringExtra("id");
        stickerPackName = getIntent().getStringExtra("name");
        folderPath = getIntent().getStringExtra("folder_path");
        icon = findViewById(R.id.icon);
        nameView = findViewById(R.id.name);
        sizeView = findViewById(R.id.stickers_size);
        stickerList = findViewById(R.id.stickers);
        progress = findViewById(R.id.progress);
        text01 = findViewById(R.id.text01);
        Tool.log("Sticker pack ID: "+stickerPackID);
        stickerPaths = Tool.read(this, stickerPackID+"_sticker_paths");
        if (stickerPaths == null) {
            stickerPaths = new ArrayList<>();
        }
        stickerURLs = Tool.read(this, stickerPackID+"_sticker_urls");
        if (stickerURLs == null) {
            stickerURLs = new ArrayList<>();
        }
        Tool.log("Total sticker paths: "+stickerPaths.size());
        Tool.log("Total sticker urls: "+stickerURLs.size());
        stickerList.setLayoutManager(new GridLayoutManager(this, 4));
        stickerList.setItemAnimator(new DefaultItemAnimator());
        files = new ArrayList<>();
        adapter = new AllWebStickerAdapter(this, files);
        stickerList.setAdapter(adapter);
        final AdView ad = findViewById(R.id.ad);
        ad.loadAd(new AdRequest.Builder()
                .build());
        ad.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                ad.setVisibility(View.VISIBLE);
            }
        });
        checkStickers();
    }

    @SuppressWarnings("all")
    public void checkStickers() {
        // Get amount of files which need to be downloaded
        int amount = 0;
        for (String path:stickerPaths) {
            if (!new File(path).exists()) {
                amount++;
            }
        }
        if (amount == 0) {
            progress.setVisibility(View.GONE);
            text01.setVisibility(View.GONE);
            stickerList.setVisibility(View.VISIBLE);
        }
        for (int i=0; i<stickerPaths.size(); i++) {
            FileItem item = new FileItem();
            files.add(item);
        }
        for (int i=0; i<stickerPaths.size(); i++) {
            final String path = stickerPaths.get(i);
            if (!new File(path).exists()) {
                final int i0 = i;
                String url = stickerURLs.get(i);
                Tool.downloadFile(url, new File(path), new OnCompletionListener() {
                    @Override
                    public void onComplete() {
                        FileItem item = files.get(i0);
                        item.setType(FileItem.TYPE_IMAGE);
                        item.setPath(path);
                        item.setTime(new File(path).lastModified());
                        item.setFolderPath(folderPath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setVisibility(View.GONE);
                                text01.setVisibility(View.GONE);
                                stickerList.setVisibility(View.VISIBLE);
                                adapter.notifyItemChanged(i0);
                            }
                        });
                    }
                });
            } else {
                FileItem item = files.get(i);
                item.setType(FileItem.TYPE_IMAGE);
                item.setPath(path);
                item.setTime(new File(path).lastModified());
                item.setFolderPath(folderPath);
                adapter.notifyItemChanged(i);
            }
        }
        infoCanBeSeen = true;
        Picasso.get().load(new File(files.get(0).getPath())).into(icon);
        nameView.setText(stickerPackName);
        long totalSize = 0;
        for (FileItem item:files) {
            totalSize += new File(item.getPath()).length();
        }
        Tool.log("Total size: "+totalSize);
        String sizeText = "";
        if (totalSize >= 1024*1024) {
            sizeText += (totalSize/1024/1024);
            sizeText += "MB";
        } else if (totalSize >= 1024) {
            sizeText += (totalSize/1024);
            sizeText += "KB";
        } else if (totalSize < 1024) {
            sizeText += totalSize;
            sizeText += "B";
        }
        Tool.log("Size text: "+sizeText);
        sizeView.setText(sizeText);
    }

    public void addToWhatsApp(View view) {
        StickerPack pack = new StickerPack();
        pack.getFiles().addAll(files);
        for (FileItem item:files) {
            pack.setTotalSize(pack.getTotalSize()+new File(item.getPath()).length());
        }
        pack.setType(StickerPack.TYPE_STICKER_PACK);
        pack.setAndroidPlayStoreLink("https://play.google.com/store/apps/details?id=com.luckynineapps.stickersmaker");
        pack.setIdentifier(stickerPackID);
        pack.setName(stickerPackName);
        pack.setPublisher("Lucky Nine Apps");
        pack.setPath(folderPath);
        pack.setPublisherEmail("cs@luckynineapps.com");
        Tool.addStickersToWhatsApp2(this, pack);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_all_web_stickers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }/* else if (id == R.id.info) {
            if (infoCanBeSeen) {
                View view = LayoutInflater.from(this).inflate(R.layout.show_sticker_pack_info, null);
                ImageView icon = view.findViewById(R.id.icon);
                TextView name = view.findViewById(R.id.name);
                ImageView link = view.findViewById(R.id.link);
                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tool.openURL(ViewAllWebStickersActivity.this, "http://www.luckynineapps.com");
                    }
                });
                Picasso.get().load(new File(files.get(0).getPath())).into(icon);
                name.setText(stickerPackName);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(view)
                        .create();
                dialog.show();
            }
        }*/ else if (id == R.id.privacy_policy) {
            Intent i = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(i);
        } else if (id == R.id.terms) {
            Intent i = new Intent(this, TermsActivity.class);
            startActivity(i);
        }
        return false;
    }
}
