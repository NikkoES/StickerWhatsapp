package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class AllWebStickerAdapter extends RecyclerView.Adapter<AllWebStickerAdapter.ViewHolder> {
    Context context;
    public ArrayList<FileItem> files;

    public AllWebStickerAdapter(Context context, ArrayList<FileItem> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_all_web_stickers_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final FileItem item = files.get(position);
        Picasso.get().load(new File(item.getPath())).resize(128, 128).into(vh.img);
        String path = item.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.img);
        }
    }
}
