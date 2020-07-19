package com.luckynineapps.stickersmaker.imagecropperfragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.luckynineapps.stickersmaker.Action;
import com.luckynineapps.stickersmaker.FileItem;
import com.luckynineapps.stickersmaker.ImageCropperActivity;
import com.luckynineapps.stickersmaker.OnBackPressedListener;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.Tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class ManualCroppingFragment extends Fragment {
    private final int MANUAL = 1;
    private final int MAGIC = 2;
    View view;
    ImageCropperActivity activity;
    ImageView img;
    public int imageX = 0;
    public int imageY = 0;
    public Bitmap imageBitmap;
    public boolean edited = false;
    Canvas cvs;
    Paint p;
    boolean processed = false;
    int mode = MANUAL;
    /*RelativeLayout manual, magic;
    ImageView manualCheck, magicCheck;*/
    int radius = 32;
    ImageView indicator, indicator2;
    ImageView placeholder;
    SeekBar size;
    Bitmap mainBitmap;
    int resizeWidth = 0;
    int resizeHeight = 0;
    RelativeLayout undo, redo;
    ArrayList<Action> actions;
    ArrayList<Action> redoActions;
    Bitmap placeHolderBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cropper_manual, container, false);
        return view;
    }

    @SuppressWarnings("all")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (ImageCropperActivity) getActivity();
        img = view.findViewById(R.id.img);
        indicator = view.findViewById(R.id.indicator);
        indicator2 = view.findViewById(R.id.indicator2);
        placeholder = view.findViewById(R.id.placeholder);
        size = view.findViewById(R.id.size);
        undo = view.findViewById(R.id.undo);
        redo = view.findViewById(R.id.redo);
        /*manual = view.findViewById(R.id.manual);
        manualCheck = view.findViewById(R.id.manual_check);
        magic = view.findViewById(R.id.magic);
        magicCheck = view.findViewById(R.id.magic_check);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = MANUAL;
                manual.setBackgroundColor(ContextCompat.getColor(activity, R.color.semi_black));
                manualCheck.setVisibility(View.VISIBLE);
                magic.setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
                magicCheck.setVisibility(View.GONE);
            }
        });
        magic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = MAGIC;
                magic.setBackgroundColor(ContextCompat.getColor(activity, R.color.semi_black));
                magicCheck.setVisibility(View.VISIBLE);
                manual.setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
                manualCheck.setVisibility(View.GONE);
            }
        });*/
        actions = new ArrayList<>();
        redoActions = new ArrayList<>();
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redo();
            }
        });
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                radius = value;
                indicator2.setScaleX(radius);
                indicator2.setScaleY(radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        imageBitmap = activity.imageBitmap;
        if (!processed) {
            processed = true;
            img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final ViewTreeObserver.OnGlobalLayoutListener layoutListener = this;
                    img.post(new Runnable() {
                        @Override
                        public void run() {
                            final int width = img.getWidth();
                            final int height = img.getHeight();
                            if (width > 0 && height > 0) {
                                if (Build.VERSION.SDK_INT >= 16) {
                                    img.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                                } else {
                                    img.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                                }
                                int imageWidth = imageBitmap.getWidth();
                                int imageHeight = imageBitmap.getHeight();
                                imageX = 0;
                                imageY = 0;
                                resizeWidth = 0;
                                resizeHeight = 0;
                                if (imageWidth > imageHeight) {
                                    resizeWidth = width;
                                    resizeHeight = (int) ((float) imageHeight * (float) resizeWidth / (float) imageWidth);
                                    imageX = (width / 2) - (resizeWidth / 2);
                                    imageY = (height / 2) - (resizeHeight / 2);
                                } else if (imageWidth < imageHeight) {
                                    resizeHeight = height;
                                    resizeWidth = (int) ((float) resizeHeight * (float) imageWidth / (float) imageHeight);
                                    imageX = (width / 2) - (resizeWidth / 2);
                                    imageY = (height / 2) - (resizeHeight / 2);
                                } else {
                                    resizeWidth = width;
                                    resizeHeight = resizeWidth;
                                    //imageX = (width/2)-(resizeWidth/2);
                                    imageX = 0;
                                    imageY = (height / 2) - (resizeHeight / 2);
                                }
                                imageBitmap = imageBitmap.createScaledBitmap(imageBitmap, resizeWidth, resizeHeight, true);
                                mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                cvs = new Canvas(mainBitmap);
                                p = new Paint();
                                p.setAntiAlias(true);
                                placeHolderBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.placeholder);
                                Bitmap a = placeHolderBitmap;
                                if (width > height) {
                                    int w2 = width;
                                    int h2 = (int)((float)placeHolderBitmap.getHeight()*((float)w2/(float)placeHolderBitmap.getWidth()));
                                    placeHolderBitmap = Bitmap.createScaledBitmap(placeHolderBitmap, w2, h2, true);
                                } else {
                                    int h2 = height;
                                    int w2 = (int)((float)placeHolderBitmap.getWidth()*((float)h2/(float)placeHolderBitmap.getHeight()));
                                    placeHolderBitmap = Bitmap.createScaledBitmap(placeHolderBitmap, w2, h2, true);
                                }
                                a.recycle();
                                cvs.drawBitmap(placeHolderBitmap, 0, 0, p);
                                cvs.drawBitmap(imageBitmap, imageX, imageY, p);
                                img.setImageBitmap(mainBitmap);
                                Bitmap b = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                Action action = new Action(b, imageX, imageY, resizeWidth, resizeHeight);
                                action.setElementType(Action.ROOT);
                                actions.add(action);
                                indicator.setTranslationX(width/2);
                                indicator.setTranslationY(height/2);
                                indicator2.setTranslationX(width/2);
                                indicator2.setTranslationY(height/2-150);
                                placeholder.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        float x = event.getX();
                                        float y = event.getY();
                                        int touchAction = event.getAction();
                                        if (touchAction == MotionEvent.ACTION_UP) {
                                            if (actions.size() > 0) {
                                                if (actions.get(0).getElementType() != Action.ROOT) {
                                                    Bitmap b = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                    Action action = new Action(b, imageX, imageY, resizeWidth, resizeHeight);
                                                    action.setPreviousBitmap(imageBitmap);
                                                    action.setElementType(Action.ROOT);
                                                    actions.add(0, action);
                                                }
                                            } else {
                                                Bitmap b = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                Action action = new Action(b, imageX, imageY, resizeWidth, resizeHeight);
                                                action.setPreviousBitmap(imageBitmap);
                                                action.setElementType(Action.ROOT);
                                                actions.add(action);
                                            }
                                            Bitmap b = mainBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                            Action action = new Action(b, 0, 0, width, height);
                                            actions.add(action);
                                            /*try {
                                                b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream("/sdcard/a.png"));
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }*/
                                        }
                                        if (mode == MANUAL) {
                                            erasePictureManually(x, y);
                                        } else if (mode == MAGIC) {
                                            indicator.setTranslationX(x - radius);
                                            indicator.setTranslationY(y - radius);
                                            indicator2.setTranslationX(x - radius);
                                            indicator2.setTranslationY(y - 150 - radius);
                                            if (touchAction == MotionEvent.ACTION_UP) {
                                                p.setStyle(Paint.Style.FILL);
                                                p.setColor(Color.RED);
                                                int currentColor = mainBitmap.getPixel((int) x, (int) y)&0xFFFFFF;
                                                Tool.floodFill(mainBitmap, new Point((int)x, (int)y), currentColor, 0xff0000);
                                                /*p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                                                float[] hsv = new float[3];
                                                Color.RGBToHSV((currentColor>>16)&0xFF, (currentColor>>8)&0xFF, currentColor&0xFF, hsv);
                                                //cvs.drawCircle(x, y, radius, p);
                                                int currentHue = (int)hsv[0];
                                                //Tool.log("Current color: "+hsv[0]);
                                                radius = 100;
                                                for (int j = 0; j < radius * 2; j++) {
                                                    for (int i = 0; i < radius * 2; i++) {
                                                        if ((x+i) >= 0 && (x+i) < mainBitmap.getWidth() && (y+j) >= 0 && (y+j) < mainBitmap.getHeight()) {
                                                            int color = mainBitmap.getPixel((int) x + i, (int) y + j);
                                                            Color.RGBToHSV((color>>16)&0xFF, (color>>8)&0xFF, color&0xFF, hsv);
                                                            //Tool.log("Color: "+hsv[0]);
                                                            int hue = (int)hsv[0];
                                                            if (currentHue == hue) {
                                                                //mainBitmap.setPixel((int)x+i, (int)y+j, Color.RED);
                                                                cvs.drawPoint(x + i, y + j, p);
                                                            }
                                                        }
                                                    }
                                                }*/
                                                img.invalidate();
                                            }
                                        }
                                        return true;
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
        activity.addOnBackPressedListener(new OnBackPressedListener() {
            @Override
            public void onBackPressed() {
                if (edited) {
                    AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setMessage(R.string.text30)
                            .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    processed = false;
                                    activity.finish();
                                }
                            })
                            .setNegativeButton(R.string.text_no, null)
                            .create();
                    dialog.show();
                } else {
                    processed = false;
                    activity.finish();
                }
            }
        });
    }

    public void erasePictureManually(float x, float y) {
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        cvs.drawCircle(x, y-150, radius, p);
        indicator.setTranslationX(x-radius);
        indicator.setTranslationY(y-radius);
        indicator2.setTranslationX(x-radius);
        indicator2.setTranslationY(y-150-radius);
        img.invalidate();
    }

    public String save() {
        Bitmap b = Bitmap.createBitmap(mainBitmap, imageX, imageY, resizeWidth, resizeHeight);
        File folder = new File(Environment.getExternalStorageDirectory(), "NewStickers4Chatting");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File savedFile = new File(activity.getFilesDir(), UUID.randomUUID().toString()+".png");
        try {
            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedFile.getAbsolutePath();
    }

    public void undo() {
        if (actions.size() > 0) {
            Action lastAction = actions.get(actions.size()-1);
            cvs.drawColor(Color.BLACK);
            p = new Paint();
            p.setAntiAlias(true);
            cvs.drawBitmap(placeHolderBitmap, 0, 0, p);
            cvs.drawBitmap(lastAction.getBitmap(), lastAction.getX(), lastAction.getY(), p);
            if (lastAction.getPreviousBitmap() != null) {
                if (!lastAction.getPreviousBitmap().isRecycled()) {
                    lastAction.getPreviousBitmap().recycle();
                }
            }
            img.invalidate();
            redoActions.add(lastAction);
            actions.remove(actions.size()-1);
        }
    }

    public void redo() {
        if (redoActions.size() > 0) {
            Action lastAction = redoActions.get(redoActions.size()-1);
            cvs.drawColor(Color.BLACK);
            p = new Paint();
            p.setAntiAlias(true);
            cvs.drawBitmap(placeHolderBitmap, 0, 0, p);
            cvs.drawBitmap(lastAction.getBitmap(), lastAction.getX(), lastAction.getY(), p);
            if (lastAction.getPreviousBitmap() != null) {
                if (!lastAction.getPreviousBitmap().isRecycled()) {
                    lastAction.getPreviousBitmap().recycle();
                }
            }
            img.invalidate();
            actions.add(lastAction);
            redoActions.remove(redoActions.size()-1);
        }
    }
}
