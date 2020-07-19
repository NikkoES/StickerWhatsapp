package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {
    Context context;
    ArrayList<String> fontPaths;

    public FontAdapter(Context context, ArrayList<String> fontPaths) {
        this.context = context;
        this.fontPaths = fontPaths;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.font, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int i) {
        final String fontPath = fontPaths.get(i);
        if (fontPath.equals("custom")) {
            vh.text.setVisibility(View.GONE);
            vh.customFont.setVisibility(View.VISIBLE);
        } else {
            vh.customFont.setVisibility(View.GONE);
            vh.text.setVisibility(View.VISIBLE);
            Typeface tf = Typeface.createFromFile(fontPath);
            vh.text.setTypeface(tf);
        }
        vh.container01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fontPath.equals("custom")) {
                    ImageEditActivity.instance.selectCustomFont();
                } else {
                    ImageEditActivity.instance.selectFont(fontPath);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fontPaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container01;
        public TextView text;
        public TextView customFont;

        public ViewHolder(View view) {
            super(view);
            container01 = view.findViewById(R.id.container01);
            text = view.findViewById(R.id.text);
            customFont = view.findViewById(R.id.custom_font);
        }
    }
}
