package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

public class ZoomableImageView extends View {

    private Drawable image;
    ImageButton img,img1;
    private int zoomControler=200;

    public ZoomableImageView(Context context){
        super(context);

        image=context.getResources().getDrawable(R.drawable.wallpaper);
        //image=context.getResources().getDrawable(R.drawable.icon);

        setFocusable(true);
    }

    public ZoomableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        image=context.getResources().getDrawable(R.drawable.wallpaper);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //here u can control the width and height of the images........ this line is very important
        image.setBounds((getWidth()/2)-zoomControler, (getHeight()/2)-zoomControler, (getWidth()/2)+zoomControler, (getHeight()/2)+zoomControler);
        image.draw(canvas);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
            // zoom in
            zoomControler+=10;
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
            // zoom out
            zoomControler-=10;
        }
        if(zoomControler<10){
            zoomControler=10;
        }

        invalidate();
        return true;
    }
}