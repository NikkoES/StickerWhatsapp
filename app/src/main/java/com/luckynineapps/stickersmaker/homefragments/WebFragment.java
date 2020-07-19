package com.luckynineapps.stickersmaker.homefragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.luckynineapps.stickersmaker.FileItem;
import com.luckynineapps.stickersmaker.HomeActivity;
import com.luckynineapps.stickersmaker.ObjectSerializer;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.StickerPack;
import com.luckynineapps.stickersmaker.Tool;
import com.luckynineapps.stickersmaker.WebStickerPackAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WebFragment extends Fragment {
    public static WebFragment instance;
    View v;
    HomeActivity activity;
    RecyclerView stickerPackList;
    ArrayList<StickerPack> stickerPacks;
    WebStickerPackAdapter adapter;
    ProgressBar progress;
    TextView text01;
    LinearLayoutManager lm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home_web, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        instance = this;
        activity = (HomeActivity) getActivity();
        stickerPackList = v.findViewById(R.id.sticker_packs);
        progress = v.findViewById(R.id.progress);
        text01 = v.findViewById(R.id.text01);
        lm = new LinearLayoutManager(activity);
        stickerPackList.setLayoutManager(lm);
        stickerPackList.setItemAnimator(new DefaultItemAnimator());
        stickerPacks = new ArrayList<>();
        adapter = new WebStickerPackAdapter(activity, stickerPacks);
        stickerPackList.setAdapter(adapter);
        boolean loaded = Tool.read(activity, "stickers_loaded", false);
        Tool.log("Loaded: " + loaded);
        loaded = false;
        if (!loaded) {
            collectStickerPacks();
        } else {
            collectStickersFromCache();
        }
        //collectStickersFromCache();
    }

    public void collectStickerPacks() {
        stickerPacks.clear();
        adapter.notifyDataSetChanged();
        final File stickersFolder = new File(activity.getFilesDir(), "stickers");
        if (!stickersFolder.exists()) {
            stickersFolder.mkdirs();
        }
        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest request = new StringRequest(Request.Method.GET, "http://156.67.216.106/apps/sticker/get_stickers.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Tool.log("Response: " + response);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final JSONArray folders = new JSONArray(response);
                                    for (int i = 0; i < folders.length(); i++) {
                                        final StickerPack pack = new StickerPack();
                                        stickerPacks.add(pack);
                                        final JSONObject folder = folders.getJSONObject(i);
                                        String names = folder.getString("name");
                                        final String id = names.split("_")[0];
                                        final String name = names.split("_")[1];
                                        final File stickerFolder = new File(stickersFolder, id + "_" + name);
                                        if (!stickerFolder.exists()) {
                                            stickerFolder.mkdirs();
                                        }
                                        final int stickerPackOrder = stickerPacks.size();
                                        final ArrayList<String> stickerURLs = new ArrayList<>();
                                        final ArrayList<String> stickerPaths = new ArrayList<>();
                                        final int i0 = i;
                                        pack.setName(name);
                                        Tool.log("Sticker pack: "+name);
                                        pack.setPath(stickerFolder.getAbsolutePath());
                                        pack.setIdentifier(id);
                                        final JSONArray stickers = folder.getJSONArray("items");
                                        for (int j = 0; j < stickers.length(); j++) {
                                            String stickerName = null;
                                            try {
                                                stickerName = stickers.getString(j);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            final File stickerFile = new File(stickerFolder, stickerName);
                                            FileItem item = new FileItem();
                                            item.setType(FileItem.TYPE_IMAGE);
                                            item.setTime(System.currentTimeMillis());
                                            item.setFolderPath(stickerFolder.getAbsolutePath());
                                            item.setPath(stickerFile.getAbsolutePath());
                                            item.setStickerPackID(name);
                                            pack.setTotalSize(pack.getTotalSize() + (int) stickerFile.length());
                                            stickerPaths.add(stickerFile.getAbsolutePath());
                                            String stickerURL = "http://156.67.216.106/apps/sticker/stickers/" + id + "_" + name + "/" + stickerName;
                                            stickerURLs.add(stickerURL);
                                            if (pack.getFiles().size() < 5) {
                                                Tool.log("Sticker URL: "+stickerURL);
                                                final int j0 = j;
                                                try {
                                                    URL url = new URL(stickerURL);
                                                    URLConnection c = url.openConnection();
                                                    c.connect();
                                                    InputStream stream = c.getInputStream();
                                                    FileOutputStream fos = new FileOutputStream(stickerFile);
                                                    int read;
                                                    byte[] buffer = new byte[8192];
                                                    while ((read = stream.read(buffer)) != -1) {
                                                        fos.write(buffer, 0, read);
                                                    }
                                                    fos.flush();
                                                    fos.close();
                                                    stream.close();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                pack.getFiles().add(item);
                                            }
                                        }
                                        Tool.save(activity, id + "_sticker_paths", stickerPaths);
                                        Tool.save(activity, id + "_sticker_urls", stickerURLs);
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pack.setCanBeAddedToWhatsApp(true);
                                                adapter.notifyItemChanged(stickerPackOrder);
                                                if (pack.getAdapter() != null) {
                                                    pack.getAdapter().notifyDataSetChanged();
                                                }
                                                progress.setVisibility(View.GONE);
                                                text01.setVisibility(View.GONE);
                                                stickerPackList.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                    Tool.save(activity, "web_sticker_packs", ObjectSerializer.serialize(stickerPacks));
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Collections.sort(stickerPacks, new Comparator<StickerPack>() {
                                                @Override
                                                public int compare(StickerPack o1, StickerPack o2) {
                                                    try {
                                                        return o1.getName().compareTo(o2.getName());
                                                    } catch (Exception e) {
                                                        return 0;
                                                    }
                                                }
                                            });
                                            for (int i = 0; i < stickerPacks.size(); i++) {
                                                if (i > 0 && ((i + 1) % 4) == 0) {
                                                    StickerPack pack = new StickerPack();
                                                    pack.setType(StickerPack.TYPE_AD);
                                                    stickerPacks.add(i, pack);
                                                }
                                            }
                                            adapter.notifyDataSetChanged();
                                            //Tool.save(activity, "stickers_loaded", true);
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(request);
    }

    public void collectStickersFromCache() {
        final File stickersFolder = new File(activity.getFilesDir(), "stickers");
        if (!stickersFolder.exists()) {
            stickersFolder.mkdirs();
        }
        File[] stickerFolders = stickersFolder.listFiles();

        /*StickerPack pack = new StickerPack();
        pack.setIdentifier("sticker01");
        pack.setName("Sticker 1");
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        stickerPacks.add(pack);

        pack = new StickerPack();
        pack.setIdentifier("sticker01");
        pack.setName("Sticker 1");
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        stickerPacks.add(pack);

        pack = new StickerPack();
        pack.setType(StickerPack.TYPE_AD);
        stickerPacks.add(pack);

        pack = new StickerPack();
        pack.setIdentifier("sticker01");
        pack.setName("Sticker 1");
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        pack.getFiles().add(new FileItem("/sdcard/a.png"));
        stickerPacks.add(pack);
        adapter.notifyDataSetChanged();*/

        Tool.log("Sticker pack count: " + stickerFolders.length);
        for (int i = 0; i < stickerFolders.length; i++) {
            File stickerFolder = stickerFolders[i];
            if (stickerFolder.isDirectory()) {
                Tool.log("Sticker folder: " + stickerFolder.getAbsolutePath());
                String path = stickerFolder.getAbsolutePath();
                String folderName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] names = folderName.split("_");
                // Splitted in two parts. First part is ID, and second part is name.
                String id = names[0];
                String name = names[1];
                StickerPack pack = new StickerPack();
                ArrayList<String> stickerPaths = new ArrayList<>();
                ArrayList<String> stickerURLs = new ArrayList<>();
                pack.setIdentifier(id);
                pack.setName(name);
                pack.setProgressEnabled(false);
                pack.setPath(stickerFolder.getAbsolutePath());
                for (File stickerFile : stickerFolder.listFiles()) {
                    FileItem item = new FileItem();
                    pack.setTotalSize(pack.getTotalSize()+stickerFile.length());
                    item.setFolderPath(stickerFolder.getAbsolutePath());
                    item.setPath(stickerFile.getAbsolutePath());
                    item.setStickerPackID(id);
                    stickerPaths.add(stickerFile.getAbsolutePath());
                    stickerURLs.add("");
                    item.setTime(stickerFile.lastModified());
                    item.setType(FileItem.TYPE_IMAGE);
                    pack.getFiles().add(item);
                }
                stickerPacks.add(pack);
                Tool.save(activity, id + "_sticker_paths", stickerPaths);
                Tool.save(activity, id + "_sticker_urls", stickerURLs);
            }
        }
        Tool.save(activity, "sticker_packs", ObjectSerializer.serialize(stickerPacks));
        Collections.sort(stickerPacks, new Comparator<StickerPack>() {
            @Override
            public int compare(StickerPack o1, StickerPack o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i = 0; i < stickerPacks.size(); i++) {
            if (i > 0 && ((i + 1) % 4) == 0) {
                StickerPack pack = new StickerPack();
                pack.setType(StickerPack.TYPE_AD);
                stickerPacks.add(i, pack);
            }
        }
        adapter.notifyDataSetChanged();
        progress.setVisibility(View.GONE);
        text01.setVisibility(View.GONE);
        stickerPackList.setVisibility(View.VISIBLE);
    }
}
