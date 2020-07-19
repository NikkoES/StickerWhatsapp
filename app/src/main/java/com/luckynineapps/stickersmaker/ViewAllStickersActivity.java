package com.luckynineapps.stickersmaker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ViewAllStickersActivity extends AppCompatActivity {
    String folderPath = "";
    RecyclerView stickerList;
    ArrayList<FileItem> files;
    AllStickerAdapter adapter;
    ProgressBar progress;
    TextView text01;

    @SuppressWarnings({"all", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_stickers);
        setTitle(R.string.text12);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        folderPath = getIntent().getStringExtra("folder_path");
        stickerList = findViewById(R.id.stickers);
        progress = findViewById(R.id.progress);
        text01 = findViewById(R.id.text01);
        stickerList.setLayoutManager(new GridLayoutManager(this, 2));
        stickerList.setItemAnimator(new DefaultItemAnimator());
        files = new ArrayList<>();
        adapter = new AllStickerAdapter(this, files);
        stickerList.setAdapter(adapter);
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                collectStickers();
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                stickerList.setVisibility(View.VISIBLE);
                text01.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }
        }.execute();
    }

    public void collectStickers() {
        files.addAll(getAllFiles(new File(folderPath)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private ArrayList<FileItem> getAllFiles(File folder) {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")) {
                            FileItem item = new FileItem();
                            item.setType(FileItem.TYPE_IMAGE);
                            item.setPath(f.getAbsolutePath());
                            fileItems.add(item);
                        }
                    }
                } else {
                    if (!f.getName().equals("WhatsApp")) {
                        ArrayList<FileItem> folderFileItems = getAllFiles(f);
                        for (FileItem item : folderFileItems) {
                            fileItems.add(item);
                        }
                    }
                }
            }
        }
        return fileItems;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return false;
    }
}
