package com.luckynineapps.stickersmaker;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class CustomPhotoView extends PhotoViewAttacher {

    public CustomPhotoView(ImageView imageView) {
        super(imageView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        Tool.log("OnTouch()...");
        return super.onTouch(v, ev);
    }
}
