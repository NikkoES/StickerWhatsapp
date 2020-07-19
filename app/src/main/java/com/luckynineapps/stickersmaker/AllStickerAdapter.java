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

public class AllStickerAdapter extends RecyclerView.Adapter<AllStickerAdapter.ViewHolder> {
    Context context;
    public ArrayList<FileItem> files;

    public AllStickerAdapter(Context context, ArrayList<FileItem> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_all_stickers_item, viewGroup, false);
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
        String fileName = path.substring(path.lastIndexOf("/")+1, path.length());
        vh.name.setText(fileName);
        vh.container02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStickerDialog(item);
            }
        });
        vh.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStickerDialog(item);
            }
        });
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
        Picasso.get().load(new File(item.getPath())).into(img);
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
                                File folder = new File(Tool.getDataDir(), "stickers/"+packName);
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
                LocalFragment.getInstance().editImage(AllStickerAdapter.this, item);
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
        public RelativeLayout container02;
        public ImageView img;
        public TextView name;

        public ViewHolder(View view) {
            super(view);
            container02 = view.findViewById(R.id.container02);
            img = view.findViewById(R.id.img);
            name = view.findViewById(R.id.name);
        }
    }
}
