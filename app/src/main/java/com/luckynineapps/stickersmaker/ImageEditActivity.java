package com.luckynineapps.stickersmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class ImageEditActivity extends AppCompatActivity {
    public static ImageEditActivity instance;
    private final int SELECT_EMOTICON = 1;
    private final int SELECT_CLIP_ART = 2;
    private final int ROTATE_IMAGE = 3;
    private final int SELECT_CUSTOM_FONT = 4;
    private final int SELECT_IMAGE = 5;
    private final int CROP_IMAGE = 6;
    private final int PLAIN = 1;
    private final int BOLD = 2;
    private final int ITALIC = 3;
    private final int UNDERSCORE = 4;
    private final int STRIKETHROUGH = 5;
    PhotoView img;
    Canvas cvs;
    Paint p;
    int width = 0;
    int height = 0;
    Bitmap mainBitmap;
    ArrayList<Action> actions;
    ArrayList<Action> undoActions;
    ArrayList<Action> redoActions;
    int fingersCount = 0;
    boolean touching = true;
    Action selectedAction; //The Action which your finger touches into
    int lastX = -1;
    int lastY = -1;
    int distanceX = 0;
    int distanceY = 0;
    long touchStartTime = 0;
    AlertDialog fontDialog = null;
    TextView fontPreview = null;
    String selectedFontPath = "";
    Typeface currentTypeface;
    int currentColor = 0;
    boolean edited = false;
    LinearLayout actionToolbar;
    RelativeLayout rotate;
    RelativeLayout remove;
    RelativeLayout crop;
    Bitmap scaleXIndicator;
    Bitmap scaleYIndicator;
    boolean scalingX1 = false;
    boolean scalingY1 = false;
    boolean scalingX2 = false;
    boolean scalingY2 = false;
    Bitmap currentBitmap;
    int selectedStyle = PLAIN;
    File croppedBitmapFile;
    // Used when users want to rotate their images
    Bitmap bitmap0;
    int x0 = 0;
    int y0 = 0;
    int w0 = 0;
    int h0 = 0;
    Canvas c0;
    Paint p0;
    ImageView img0;
    int rotation0 = 0;

    @SuppressWarnings("all")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_image_edit);
        setTitle(R.string.text46);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        final String imagePath = getIntent().getStringExtra("image_path");
        img = findViewById(R.id.img);
        actionToolbar = findViewById(R.id.action_toolbar);
        rotate = findViewById(R.id.rotate);
        remove = findViewById(R.id.remove);
        crop = findViewById(R.id.crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ImageEditActivity.this, ImageCropperActivity.class);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedAction.getImage().compress(Bitmap.CompressFormat.PNG, 100, baos);
                try {
                    baos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i.putExtra("image", baos.toByteArray());
                startActivityForResult(i, CROP_IMAGE);
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Action a : actions) {
                    if (selectedAction != null) {
                        if (selectedAction.getId() == a.getId()) {
                            undoActions.add(a);
                            actions.remove(a);
                            rotate.setVisibility(View.GONE);
                            crop.setVisibility(View.GONE);
                            remove.setVisibility(View.GONE);
                            selectedAction = null;
                            redrawAll();
                            break;
                        }
                    }
                }
            }
        });
        actions = new ArrayList<>();
        undoActions = new ArrayList<>();
        redoActions = new ArrayList<>();
        img.setZoomable(false);
        scaleXIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.scale_x);
        scaleYIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.scale_y);
        scaleXIndicator = Bitmap.createScaledBitmap(scaleXIndicator, 70, 50, true);
        scaleYIndicator = Bitmap.createScaledBitmap(scaleYIndicator, 50, 70, true);
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_DOWN) {
                    touchStartTime = System.currentTimeMillis();
                    touching = true;
                    selectedAction = null;
                    boolean touched = false;
                    for (int i = actions.size() - 1; i >= 0; i--) {
                        Action a = actions.get(i);
                        if (x >= a.getX() - 50
                                && y >= a.getY() + a.getHeight() / 2 - 50
                                && x < a.getX() + 50
                                && y < a.getY() + a.getHeight() / 2 + 50) {
                            touched = true;
                            scalingX1 = true;
                            selectedAction = a;
                            break;
                        }
                        if (x >= a.getX() + a.getWidth() / 2 - 50
                                && y >= a.getY() - 50
                                && x < a.getX() + a.getWidth() / 2 + 50
                                && y < a.getY() + 50) {
                            touched = true;
                            scalingY1 = true;
                            selectedAction = a;
                            break;
                        }
                        if (x >= a.getX() + a.getWidth() - 50
                                && y >= a.getY() + a.getHeight() / 2 - 50
                                && x < a.getX() + a.getWidth() + 50
                                && y < a.getY() + a.getHeight() / 2 + 50) {
                            touched = true;
                            scalingX2 = true;
                            selectedAction = a;
                            break;
                        }
                        if (x >= a.getX() + a.getWidth() / 2 - 50
                                && y >= a.getY() + a.getHeight() - 50
                                && x < a.getX() + a.getWidth() / 2 + 50
                                && y < a.getY() + a.getHeight() + 50) {
                            touched = true;
                            scalingY2 = true;
                            selectedAction = a;
                            break;
                        }
                        if (x >= a.getX() - 50 && y >= a.getY() - 50 && x < a.getX() + 50 && y < a.getY() + 50) {
                            touched = true;
                            selectedAction = a;
                            break;
                        }
                        if (x >= a.getX() && y >= a.getY() && x < a.getX() + a.getWidth() && y < a.getY() + a.getHeight()) {
                            touched = true;
                            selectedAction = a;
                            break;
                        }
                    }
                    redrawAll();
                    if (touched) {
                        remove.setVisibility(View.VISIBLE);
                        rotate.setVisibility(View.VISIBLE);
                        crop.setVisibility(View.VISIBLE);
                        drawIndicators();
                        img.invalidate();
                    } else {
                        remove.setVisibility(View.GONE);
                        rotate.setVisibility(View.GONE);
                        crop.setVisibility(View.GONE);
                    }
                    img.getAttacher().onTouch(view, event);
                    fingersCount = 1;
                } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    img.getAttacher().onTouch(view, event);
                    fingersCount = 2;
                } else if (action == MotionEvent.ACTION_POINTER_UP) {
                    img.getAttacher().onTouch(view, event);
                    fingersCount = 1;
                } else if (action == MotionEvent.ACTION_UP) {
                    img.getAttacher().onTouch(view, event);
                    if (selectedAction != null && currentBitmap != null) {
                        selectedAction.setImage(currentBitmap);
                        currentBitmap = null;
                    }
                    touching = false;
                    scalingX1 = false;
                    scalingY1 = false;
                    scalingX2 = false;
                    scalingY2 = false;
                    lastX = -1;
                    lastY = -1;
                    fingersCount = 0;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    edited = true;
                    if (fingersCount == 1) {
                        if (touching && selectedAction != null) {
                            if (scalingX1) {
                                for (Action a : actions) {
                                    if (selectedAction.getId() == a.getId()) {
                                        if (selectedAction.getType() == Action.ADD_IMAGE
                                                || selectedAction.getType() == Action.ADD_CLIP_ART) {
                                            int width = (int) x - a.getX();
                                            if ((a.getWidth() - width) > 0) {
                                                a.setX((int) x);
                                                boolean negative = Tool.isNegative(a.getWidth() - width);
                                                a.setWidth(Tool.abs(a.getWidth() - width));
                                                Bitmap scaledBitmap = a.getImage().createScaledBitmap(a.getImage(), a.getWidth(), a.getHeight(), true);
                                                if (negative) {
                                                    Matrix matrix = new Matrix();
                                                    matrix.postScale(-1, 1, a.getWidth() / 2, a.getHeight() / 2);
                                                    Bitmap scaledBitmap0 = scaledBitmap;
                                                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, a.getWidth(), a.getHeight(), matrix, true);
                                                    scaledBitmap0.recycle();
                                                }
                                                currentBitmap = scaledBitmap;
                                                redrawAll(a, scaledBitmap);
                                            }
                                        }
                                        drawIndicators();
                                        break;
                                    }
                                }
                            } else if (scalingY1) {
                                for (Action a : actions) {
                                    if (selectedAction.getId() == a.getId()) {
                                        if (selectedAction.getType() == Action.ADD_IMAGE
                                                || selectedAction.getType() == Action.ADD_CLIP_ART) {
                                            int height = (int) y - a.getY();
                                            if ((a.getHeight() - height) > 0) {
                                                a.setY((int) y);
                                                boolean negative = Tool.isNegative(a.getHeight() - height);
                                                a.setHeight(Tool.abs(a.getHeight() - height));
                                                Bitmap scaledBitmap = a.getImage().createScaledBitmap(a.getImage(), a.getWidth(), a.getHeight(), true);
                                                if (negative) {
                                                    Matrix matrix = new Matrix();
                                                    matrix.postScale(1, -1, a.getWidth() / 2, a.getHeight() / 2);
                                                    Bitmap scaledBitmap0 = scaledBitmap;
                                                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, a.getWidth(), a.getHeight(), matrix, true);
                                                    scaledBitmap0.recycle();
                                                }
                                                currentBitmap = scaledBitmap;
                                                redrawAll(a, scaledBitmap);
                                            }
                                        }
                                        drawIndicators();
                                        break;
                                    }
                                }
                            } else if (scalingX2) {
                                for (Action a : actions) {
                                    if (selectedAction.getId() == a.getId()) {
                                        if (selectedAction.getType() == Action.ADD_IMAGE
                                                || selectedAction.getType() == Action.ADD_CLIP_ART) {
                                            if (((int) x - a.getX()) > 0) {
                                                boolean negative = Tool.isNegative((int) x - a.getX());
                                                a.setWidth(Tool.abs((int) x - a.getX()));
                                                Bitmap scaledBitmap = a.getImage().createScaledBitmap(a.getImage(), a.getWidth(), a.getHeight(), true);
                                                if (negative) {
                                                    Matrix matrix = new Matrix();
                                                    matrix.postScale(-1, 1, a.getWidth(), a.getHeight());
                                                    Bitmap scaledBitmap0 = scaledBitmap;
                                                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, a.getWidth(), a.getHeight(), matrix, true);
                                                    scaledBitmap0.recycle();
                                                }
                                                currentBitmap = scaledBitmap;
                                                redrawAll(a, scaledBitmap);
                                            }
                                        }
                                        drawIndicators();
                                        break;
                                    }
                                }
                            } else if (scalingY2) {
                                for (Action a : actions) {
                                    if (selectedAction.getId() == a.getId()) {
                                        if (selectedAction.getType() == Action.ADD_IMAGE
                                                || selectedAction.getType() == Action.ADD_CLIP_ART) {
                                            if (((int) y - a.getY()) > 0) {
                                                boolean negative = Tool.isNegative((int) y - a.getY());
                                                a.setHeight((int) y - a.getY());
                                                Bitmap scaledBitmap = a.getImage().createScaledBitmap(a.getImage(), a.getWidth(), a.getHeight(), true);
                                                if (negative) {
                                                    Matrix matrix = new Matrix();
                                                    matrix.postScale(1, -1, a.getWidth() / 2, a.getHeight() / 2);
                                                    Bitmap scaledBitmap0 = scaledBitmap;
                                                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, a.getWidth(), a.getHeight(), matrix, true);
                                                    scaledBitmap0.recycle();
                                                }
                                                currentBitmap = scaledBitmap;
                                                redrawAll(a, scaledBitmap);
                                            }
                                        }
                                        drawIndicators();
                                        break;
                                    }
                                }
                            } else {
                                if (lastX == -1 && lastY == -1) {
                                    distanceX = (int) x - selectedAction.getX();
                                    distanceY = (int) y - selectedAction.getY();
                                } else {
                                    for (Action a : actions) {
                                        if (selectedAction.getId() == a.getId()) {
                                            a.setX(selectedAction.getX());
                                            a.setY(selectedAction.getY());
                                            break;
                                        }
                                    }
                                    long currentTime = System.currentTimeMillis();
                                    if ((currentTime - touchStartTime) >= 200) {
                                        selectedAction.setX((int) x - distanceX);
                                        selectedAction.setY((int) y - distanceY);
                                        redrawAll();
                                        drawIndicators();
                                    }
                                }
                            }
                            lastX = (int) x;
                            lastY = (int) y;
                        }
                    } else if (fingersCount == 2) {
                        img.getAttacher().onTouch(view, event);
                    }
                }
                return true;
            }
        });
        img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = img.getWidth();
                height = img.getHeight();
                if (width > 0 && height > 0) {
                    img.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    img.setImageBitmap(mainBitmap);
                    cvs = new Canvas(mainBitmap);
                    p = new Paint();
                    p.setAntiAlias(true);
                    if (imagePath != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false;
                        Bitmap b = BitmapFactory.decodeFile(imagePath, options);
                        int bW = 0;
                        int bH = 0;
                        if (b.getWidth() > b.getHeight()) {
                            bW = (int) ((float) width / 1.3f);
                            bH = b.getHeight() * bW / b.getWidth();
                        } else {
                            bH = height / 2;
                            bW = width * (bH) / height;
                        }
                        b = b.createScaledBitmap(b, bW, bH, true);
                        actions.add(new Action(actions.size() + 1, Action.ADD_IMAGE, b, (width / 2) - (bW / 2), (height / 2) - (bH / 2), bW, bH));
                        edited = true;
                        cvs.drawBitmap(b, (width / 2) - (bW / 2), (height / 2) - (bH / 2), p);
                        img.invalidate();
                    }
                }
            }
        });
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
    }

    public void undo(View view) {
        if (undoActions.size() > 0) {
            Action action = undoActions.get(undoActions.size() - 1);
            actions.add(action);
            redoActions.add(new Action(redoActions.size() + 1, Action.DELETE, action));
            undoActions.remove(action);
            redrawAll();
        }
    }

    public void redo(View view) {
        if (redoActions.size() > 0) {
            Action action = redoActions.get(redoActions.size() - 1);
            if (action.getType() == Action.DELETE) {
                for (Action a : actions) {
                    if (a.getId() == action.getAction().getId()) {
                        actions.remove(a);
                        undoActions.add(action.getAction());
                        redoActions.remove(action);
                        break;
                    }
                }
                redrawAll();
            }
        }
    }

    public void rotate(View view0) {
        View view = LayoutInflater.from(this).inflate(R.layout.rotate_image, null);
        final ImageView img = view.findViewById(R.id.img);
        img0 = img;
        final SeekBar rotation = view.findViewById(R.id.rotation);
        img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = img.getWidth();
                int height = img.getHeight();
                if (width > 0 && height > 0) {
                    img.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    Bitmap mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    img.setImageBitmap(mainBitmap);
                    Canvas c = new Canvas(mainBitmap);
                    c0 = c;
                    Paint p = new Paint();
                    p.setAntiAlias(true);
                    p0 = p;
                    Bitmap b = selectedAction.getImage();
                    int bW = 0;
                    int bH = 0;
                    if (b.getWidth() > b.getHeight()) {
                        bW = width;
                        bH = b.getHeight() * bW / b.getWidth();
                    } else {
                        bH = height;
                        bW = width * (bH) / height;
                    }
                    int bX = (width / 2) - (bW / 2);
                    int bY = (height / 2) - (bH / 2);
                    x0 = bX;
                    y0 = bY;
                    w0 = bW;
                    h0 = bH;
                    b = Bitmap.createScaledBitmap(b, bW, bH, true);
                    bitmap0 = b;
                    c0.translate(x0, y0);
                    c.drawBitmap(b, 0, 0, p);
                    img.invalidate();
                }
            }
        });
        rotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    rotation0 = value;
                    Matrix matrix = new Matrix();
                    matrix.setRotate(value, bitmap0.getWidth()/2, bitmap0.getHeight()/2);
                    c0.drawColor(0xff555555);
                    c0.drawBitmap(bitmap0, matrix, p0);
                    img0.invalidate();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation0);
                        Bitmap b = Bitmap.createBitmap(selectedAction.getImage(), 0, 0, selectedAction.getImage().getWidth(), selectedAction.getImage().getHeight(), matrix, true);
                        selectedAction.setImage(b);
                        selectedAction.setWidth(b.getWidth());
                        selectedAction.setHeight(b.getHeight());
                        redrawAll();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (bitmap0 != null && !bitmap0.isRecycled()) {
                            bitmap0.recycle();
                        }
                    }
                })
                .create();
        dialog.show();
        /*Bitmap b = selectedAction.getImage();
        File savedBitmapFile = new File(Tool.getCacheDir(), UUID.randomUUID().toString()+".png");
        croppedBitmapFile = new File(Tool.getCacheDir(), UUID.randomUUID().toString()+".png");
        try {
            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedBitmapFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        UCrop.of(Uri.fromFile(savedBitmapFile), Uri.fromFile(croppedBitmapFile))
                .useSourceImageAspectRatio()
                .start(this, ROTATE_IMAGE);*/
    }

    public void clearCanvas() {
        cvs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    public void redrawAll() {
        clearCanvas();
        for (Action a : actions) {
            if (a.getType() == Action.ADD_IMAGE
                    || a.getType() == Action.ADD_CLIP_ART) {
                cvs.drawBitmap(a.image, a.getX(), a.getY(), p);
            } else if (a.getType() == Action.ADD_TEXT) {
                if (currentTypeface != null) {
                    p.setColor(a.getTextColor());
                    p.setStyle(Paint.Style.FILL);
                    p.setTypeface(currentTypeface);
                    p.setTextSize(a.getTextSize());
                    int boundWidth = width;
                    TextPaint p0 = new TextPaint(p);
                    StaticLayout layout = new StaticLayout(a.getText(), p0, boundWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    int height = layout.getHeight();
                    cvs.drawText(a.getText(), a.getX(), a.getY() + height, p);
                }
            }
        }
        img.invalidate();
    }

    public void redrawAll(Action action, Bitmap img) {
        // Redraws all actions with the exception: some action must use bitmap above to draw
        clearCanvas();
        for (Action a : actions) {
            if (a.getType() == Action.ADD_IMAGE
                    || a.getType() == Action.ADD_CLIP_ART) {
                if (a.getId() == action.getId()) {
                    cvs.drawBitmap(img, a.getX(), a.getY(), p);
                } else {
                    cvs.drawBitmap(a.image, a.getX(), a.getY(), p);
                }
            } else if (a.getType() == Action.ADD_TEXT) {
                if (currentTypeface != null) {
                    p.setColor(a.getTextColor());
                    p.setStyle(Paint.Style.FILL);
                    p.setTypeface(currentTypeface);
                    p.setTextSize(a.getTextSize());
                    Rect bounds = new Rect();
                    p.getTextBounds(a.getText(), 0, a.getText().length(), bounds);
                    int height = bounds.height();
                    cvs.drawText(a.getText(), a.getX(), a.getY() + height, p);
                }
            }
        }
        ImageEditActivity.this.img.invalidate();
    }

    public void drawIndicators() {
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.GREEN);
        p.setStrokeWidth(4);
        cvs.drawRect(selectedAction.x, selectedAction.y, selectedAction.x+selectedAction.width, selectedAction.y+selectedAction.height, p);
        p.setStyle(Paint.Style.FILL);
        cvs.drawCircle(selectedAction.x, selectedAction.y, 10, p);
        cvs.drawCircle(selectedAction.x+selectedAction.width, selectedAction.y, 10, p);
        cvs.drawCircle(selectedAction.x+selectedAction.width, selectedAction.y+selectedAction.height, 10, p);
        cvs.drawCircle(selectedAction.x, selectedAction.y+selectedAction.height, 10, p);
        cvs.drawBitmap(scaleXIndicator, selectedAction.getX() - scaleXIndicator.getWidth() / 2, selectedAction.getY() + ((selectedAction.getHeight() / 2) - (scaleXIndicator.getHeight() / 2)), p);
        cvs.drawBitmap(scaleYIndicator, selectedAction.getX() + ((selectedAction.getWidth() / 2) - (scaleYIndicator.getWidth() / 2)), selectedAction.getY() - scaleYIndicator.getHeight() / 2, p);
        cvs.drawBitmap(scaleXIndicator, selectedAction.getX() + selectedAction.getWidth() - scaleXIndicator.getWidth() / 2, selectedAction.getY() + ((selectedAction.getHeight() / 2) - (scaleXIndicator.getHeight() / 2)), p);
        cvs.drawBitmap(scaleYIndicator, selectedAction.getX() + ((selectedAction.getWidth() / 2) - (scaleYIndicator.getWidth() / 2)), selectedAction.getY() + selectedAction.getHeight() - scaleYIndicator.getHeight() / 2, p);
        img.invalidate();
    }

    public void addImage(View view) {
        Tool.selectImage(this, R.string.text55, SELECT_IMAGE);
    }

    public void addEmoticon(View view) {
        Intent i = new Intent(this, EmoticonSelectorActivity.class);
        startActivityForResult(i, SELECT_EMOTICON);
    }

    public void addClipArt(View view) {
        Intent i = new Intent(this, ClipArtSelectorActivity.class);
        startActivityForResult(i, SELECT_CLIP_ART);
    }

    public void addText(View view0) {
        View view = LayoutInflater.from(this).inflate(R.layout.add_text, null);
        final RelativeLayout plain = view.findViewById(R.id.plain);
        final RelativeLayout bold = view.findViewById(R.id.bold);
        final RelativeLayout italic = view.findViewById(R.id.italic);
        final RelativeLayout underscore = view.findViewById(R.id.underscore);
        final RelativeLayout strikethrough = view.findViewById(R.id.strikethrough);
        plain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStyle = PLAIN;
                plain.setBackgroundColor(0xffdddddd);
                bold.setBackgroundColor(0xffffffff);
                italic.setBackgroundColor(0xffffffff);
                underscore.setBackgroundColor(0xffffffff);
                strikethrough.setBackgroundColor(0xffffffff);
            }
        });
        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStyle = BOLD;
                plain.setBackgroundColor(0xffffffff);
                bold.setBackgroundColor(0xffdddddd);
                italic.setBackgroundColor(0xffffffff);
                underscore.setBackgroundColor(0xffffffff);
                strikethrough.setBackgroundColor(0xffffffff);
            }
        });
        italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStyle = ITALIC;
                plain.setBackgroundColor(0xffffffff);
                bold.setBackgroundColor(0xffffffff);
                italic.setBackgroundColor(0xffdddddd);
                underscore.setBackgroundColor(0xffffffff);
                strikethrough.setBackgroundColor(0xffffffff);
            }
        });
        underscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStyle = UNDERSCORE;
                plain.setBackgroundColor(0xffffffff);
                bold.setBackgroundColor(0xffffffff);
                italic.setBackgroundColor(0xffffffff);
                underscore.setBackgroundColor(0xffdddddd);
                strikethrough.setBackgroundColor(0xffffffff);
            }
        });
        strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedStyle = STRIKETHROUGH;
                plain.setBackgroundColor(0xffffffff);
                bold.setBackgroundColor(0xffffffff);
                italic.setBackgroundColor(0xffffffff);
                underscore.setBackgroundColor(0xffffffff);
                strikethrough.setBackgroundColor(0xffdddddd);
            }
        });
        final EditText textField = view.findViewById(R.id.text);
        RelativeLayout fontSelector = view.findViewById(R.id.font_selector);
        final Spinner textSize = view.findViewById(R.id.text_size);
        final RelativeLayout textColor = view.findViewById(R.id.text_color);
        currentColor = 0xffff0000;
        ArrayList<String> textSizes = new ArrayList<>();
        for (int i = 80; i <= 500; i += 5) {
            textSizes.add(Integer.toString(i));
        }
        ArrayAdapter<String> textSizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, textSizes);
        textSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textSize.setAdapter(textSizeAdapter);
        textColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(ImageEditActivity.this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                builder.setTitle(R.string.text65);
                builder.setPreferenceName("MyColorPickerDialog");
                builder.setPositiveButton("OK", new ColorListener() {
                    @Override
                    public void onColorSelected(ColorEnvelope colorEnvelope) {
                        currentColor = 0xff000000 | colorEnvelope.getColor();
                        textColor.setBackgroundColor(currentColor);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });
        fontSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView fontList = new RecyclerView(ImageEditActivity.this);
                fontList.setItemAnimator(new DefaultItemAnimator());
                fontList.setLayoutManager(new LinearLayoutManager(ImageEditActivity.this));
                ArrayList<String> fontPaths = new ArrayList<>();
                File fontsFolder = new File(getFilesDir(), "fonts");
                for (File fontFile : fontsFolder.listFiles()) {
                    fontPaths.add(fontFile.getAbsolutePath());
                }
                fontPaths.add("custom");
                FontAdapter adapter = new FontAdapter(ImageEditActivity.this, fontPaths);
                fontList.setAdapter(adapter);
                fontDialog = new AlertDialog.Builder(ImageEditActivity.this)
                        .setView(fontList)
                        .create();
                fontDialog.show();
            }
        });
        fontPreview = view.findViewById(R.id.font_preview);
        File fontsFolder = new File(getFilesDir(), "fonts");
        fontPreview.setTypeface(Typeface.createFromFile(fontsFolder.listFiles()[0]));
        selectedFontPath = fontsFolder.listFiles()[0].getAbsolutePath();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.text62)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String text = textField.getText().toString();
                        if (text.equals("")) {
                            Tool.show(ImageEditActivity.this, R.string.text61);
                            return;
                        }
                        currentTypeface = Typeface.createFromFile(selectedFontPath);
                        Paint textPaint = fontPreview.getPaint();
                        textPaint.setTextSize(120);
                        textPaint.setTypeface(currentTypeface);
                        int fontSize = Integer.parseInt(textSize.getSelectedItem().toString());
                        if (selectedStyle == BOLD) {
                            currentTypeface = Typeface.create(currentTypeface, Typeface.BOLD);
                        } else if (selectedStyle == ITALIC) {
                            currentTypeface = Typeface.create(currentTypeface, Typeface.ITALIC);
                        }
                        Rect bounds = new Rect();
                        p.setTextSize(fontSize);
                        p.setTypeface(currentTypeface);
                        p.getTextBounds(text, 0, text.length(), bounds);
                        int fontWidth = bounds.width();
                        int fontHeight = bounds.height();
                        if (selectedStyle == UNDERSCORE) {
                            fontHeight += 8;
                        }
                        Bitmap b = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_8888);
                        Canvas c = new Canvas(b);
                        Paint p = new Paint();
                        p.setAntiAlias(true);
                        p.setStyle(Paint.Style.FILL);
                        p.setColor(currentColor);
                        p.setTypeface(currentTypeface);
                        p.setTextSize(fontSize);
                        if (selectedStyle == UNDERSCORE) {
                            c.drawText(text, 0, fontHeight-8, p);
                        } else {
                            c.drawText(text, 0, fontHeight, p);
                        }
                        if (selectedStyle == UNDERSCORE) {
                            c.drawRect(0, fontHeight-5, fontWidth, fontHeight, p);
                        } else if (selectedStyle == STRIKETHROUGH) {
                            c.drawRect(0, fontHeight/2, fontWidth, fontHeight/2+3, p);
                        }
                        actions.add(new Action(actions.size()+1, Action.ADD_IMAGE, b, 20, 100, fontWidth, fontHeight));
                        edited = true;
                        redrawAll();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedFontPath = "";
                    }
                })
                .create();
        dialog.show();
    }

    public void selectCustomFont() {
        Tool.selectFile(this, getResources().getString(R.string.text67), SELECT_CUSTOM_FONT, "application/x-font-ttf", "application/x-font-truetype", "application/x-font-opentype");
    }

    public void selectFont(String fontPath) {
        selectedFontPath = fontPath;
        fontDialog.dismiss();
        Typeface tf = Typeface.createFromFile(fontPath);
        fontPreview.setTypeface(tf);
    }

    public void recyclerImages() {
        for (Action a : actions) {
            if (a.getType() == Action.ADD_IMAGE
                    || a.getType() == Action.ADD_CLIP_ART) {
                if (a.getImage() != null && !a.getImage().isRecycled()) {
                    a.getImage().recycle();
                }
            }
        }
    }

    @SuppressWarnings("all")
    public void save() {
        final InterstitialAd ad = new InterstitialAd(this);
        ad.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        ad.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                ad.show();
            }
        });
        ad.loadAd(new AdRequest.Builder().build());
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        if (actions.size() == 0) {
            Tool.show(this, R.string.text84);
            return;
        }
        redrawAll();
        if (actions.size() > 1) {
            Action nXAction = Collections.min(actions, new Comparator<Action>() {
                @Override
                public int compare(Action action1, Action action2) {
                    return Integer.valueOf(action1.getX()).compareTo(Integer.valueOf(action2.getX()));
                }
            });
            Action nYAction = Collections.min(actions, new Comparator<Action>() {
                @Override
                public int compare(Action action1, Action action2) {
                    return Integer.valueOf(action1.getY()).compareTo(Integer.valueOf(action2.getY()));
                }
            });
            Action fXAction = Collections.max(actions, new Comparator<Action>() {
                @Override
                public int compare(Action action1, Action action2) {
                    return Integer.valueOf(action1.getX()+action1.getWidth()).compareTo(Integer.valueOf(action2.getX()+action2.getWidth()));
                }
            });
            Action fYAction = Collections.max(actions, new Comparator<Action>() {
                @Override
                public int compare(Action action1, Action action2) {
                    return Integer.valueOf(action1.getY()+action1.getHeight()).compareTo(Integer.valueOf(action2.getY()+action2.getHeight()));
                }
            });
            x = nXAction.getX();
            y = nYAction.getY();
            width = (fXAction.getX()+fXAction.getWidth()) - nXAction.getX();
            height = (fYAction.getY()+fYAction.getHeight()) - nYAction.getY();
        } else if (actions.size() == 1) {
            x = actions.get(0).getX();
            y = actions.get(0).getY();
            width = actions.get(0).getWidth();
            height = actions.get(0).getHeight();
        }
        File myEditsFolder = new File(getFilesDir(), "customstickers/myedits");
        if (!myEditsFolder.exists()) {
            myEditsFolder.mkdirs();
        }
        String fileName = "edits";
        int filesCount = myEditsFolder.listFiles().length+1;
        if (filesCount < 10) {
            fileName += "000"+filesCount;
        } else if (filesCount < 100) {
            fileName += "00"+filesCount;
        } else if (filesCount < 1000) {
            fileName += "0"+filesCount;
        } else {
            fileName += ""+filesCount;
        }
        fileName += ".png";
        Tool.log("Saved file name: "+fileName);
        File savedFile = new File(myEditsFolder, fileName);
        int w = width;
        int h = height;
        if (x+width > mainBitmap.getWidth()) {
            w = mainBitmap.getWidth()-x;
        }
        if (y+height > mainBitmap.getHeight()) {
            h = mainBitmap.getHeight()-y;
        }
        if (x < 0) {
            w += x;
            x = 0;
        }
        if (y < 0) {
            h += y;
            y = 0;
        }
        Bitmap b = Bitmap.createBitmap(mainBitmap, x, y, w, h);
        try {
            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(savedFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Intent i = new Intent();
        i.putExtra("image_path", savedFile.getAbsolutePath());
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            recyclerImages();
            finish();
        } else if (id == R.id.save) {
            save();
        }
        return false;
    }

    @SuppressWarnings("all")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_EMOTICON) {
                String emoticonPath = data.getStringExtra("emoticon_path");
                Bitmap emoticon = BitmapFactory.decodeFile(emoticonPath);
                Action action = new Action(actions.size() + 1, Action.ADD_IMAGE, emoticon, 100, 100, emoticon.getWidth(), emoticon.getHeight());
                actions.add(action);
                edited = true;
                redrawAll();
            } else if (requestCode == SELECT_CLIP_ART) {
                String clipArtPath = data.getStringExtra("clip_art_path");
                Bitmap clipArt = BitmapFactory.decodeFile(clipArtPath);
                Action action = new Action(actions.size() + 1, Action.ADD_IMAGE, clipArt, 100, 100, clipArt.getWidth(), clipArt.getHeight());
                actions.add(action);
                edited = true;
                redrawAll();
            } else if (requestCode == ROTATE_IMAGE) {
                Bitmap b = BitmapFactory.decodeFile(croppedBitmapFile.getAbsolutePath());
                if (!selectedAction.getImage().isRecycled()) {
                    selectedAction.getImage().recycle();
                }
                selectedAction.setImage(b);
                selectedAction.setWidth(b.getWidth());
                selectedAction.setHeight(b.getHeight());
                redrawAll();
                selectedAction = null;
            } else if (requestCode == SELECT_CUSTOM_FONT) {
                fontDialog.dismiss();
                selectedFontPath = data.getData().getPath();
                if (!selectedFontPath.toLowerCase().endsWith(".ttf")
                        && !selectedFontPath.toLowerCase().endsWith(".otf")) {
                    Tool.show(this, R.string.text68);
                    return;
                }
                Typeface tf = Typeface.createFromFile(selectedFontPath);
                fontPreview.setTypeface(tf);
            } else if (requestCode == SELECT_IMAGE) {
                File openedImageFile = new File(getFilesDir(), UUID.randomUUID().toString()+".png");
                Tool.extractURIToFile(this, data.getData(), openedImageFile);
                Bitmap b = BitmapFactory.decodeFile(openedImageFile.getAbsolutePath());
                int bW = 0;
                int bH = 0;
                if (b.getWidth() > b.getHeight()) {
                    bW = (int) ((float) width / 1.3f);
                    bH = b.getHeight() * bW / b.getWidth();
                } else {
                    bH = height / 2;
                    bW = width * (bH) / height;
                }
                Bitmap newBitmap = b.createScaledBitmap(b, bW, bH, true);
                if (!b.isRecycled()) {
                    b.recycle();
                }
                actions.add(new Action(actions.size() + 1, Action.ADD_IMAGE, newBitmap, (width / 2) - (bW / 2), (height / 2) - (bH / 2), bW, bH));
                redrawAll();
            } else if (requestCode == CROP_IMAGE) {
                Bitmap b = BitmapFactory.decodeFile(data.getStringExtra("image_path"));
                selectedAction.setImage(b);
                selectedAction.setWidth(b.getWidth());
                selectedAction.setHeight(b.getHeight());
                redrawAll();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (edited) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.text30)
                    .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            recyclerImages();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.text_no, null)
                    .create();
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }
}
