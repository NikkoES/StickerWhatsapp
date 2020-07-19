package com.luckynineapps.stickersmaker.imagecropperfragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luckynineapps.stickersmaker.ImageCropperActivity;
import com.luckynineapps.stickersmaker.OnBackPressedListener;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.Tool;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.luckynineapps.stickersmaker.ImageCropperActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AutoCroppingFragment extends Fragment {
    View view;
    ImageCropperActivity activity;
    ImageView img;
    ProgressBar progress;
    TextView text01;
    RelativeLayout container01;
    boolean processed = false;
    Bitmap imageBitmap;
    public int imageX = 0;
    public int imageY = 0;
    Canvas cvs;
    Paint p;
    ArrayList<Point> points = null;
    int faceCount = 0;
    RelativeLayout container02;
    Button tryAgain;
    boolean imageLoaded = false;
    int width = 0, height = 0;
    int resizeWidth = 0, resizeHeight = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cropper_auto, container, false);
        return view;
    }

    @SuppressWarnings("all")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (ImageCropperActivity) getActivity();
        img = view.findViewById(R.id.img);
        progress = view.findViewById(R.id.progress);
        text01 = view.findViewById(R.id.text01);
        container01 = view.findViewById(R.id.container01);
        container02 = view.findViewById(R.id.container02);
        tryAgain = view.findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tool.isNetworkConnected(activity)) {
                    progress.setVisibility(View.VISIBLE);
                    text01.setVisibility(View.VISIBLE);
                    container02.setVisibility(View.GONE);
                    tryAgain.setVisibility(View.GONE);
                    processImage();
                }
            }
        });
        if (!processed) {
            processed = true;
            imageBitmap = activity.imageBitmap;
            boolean firstTimeAutoCrop = Tool.read(activity, "first_time_auto_crop", true);
            if (firstTimeAutoCrop && !Tool.isNetworkConnected(activity)) {
                progress.setVisibility(View.GONE);
                text01.setVisibility(View.GONE);
                container02.setVisibility(View.VISIBLE);
                tryAgain.setVisibility(View.VISIBLE);
                return;
            }
            processImage();
        }
        activity.addOnBackPressedListener(new OnBackPressedListener() {
            @Override
            public void onBackPressed() {
                processed = false;
                activity.finish();
            }
        });
    }

    @SuppressWarnings("all")
    public void processImage() {
        Tool.log("Detecting size changes...");
        img.setOnTouchListener(new View.OnTouchListener() {
            float lastX = 0;
            float lastY = 0;
            boolean startMoving = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (imageLoaded && points != null) {
                        int minX = Collections.min(points, new Comparator<Point>() {
                            @Override
                            public int compare(Point point1, Point point2) {
                                return Integer.valueOf(point1.x).compareTo(Integer.valueOf(point2.x));
                            }
                        }).x + imageX;
                        int minY = Collections.min(points, new Comparator<Point>() {
                            @Override
                            public int compare(Point point1, Point point2) {
                                return Integer.valueOf(point1.y).compareTo(Integer.valueOf(point2.y));
                            }
                        }).y + imageY;
                        int maxX = Collections.max(points, new Comparator<Point>() {
                            @Override
                            public int compare(Point point1, Point point2) {
                                return Integer.valueOf(point1.x).compareTo(Integer.valueOf(point2.x));
                            }
                        }).x + imageX;
                        int maxY = Collections.max(points, new Comparator<Point>() {
                            @Override
                            public int compare(Point point1, Point point2) {
                                return Integer.valueOf(point1.y).compareTo(Integer.valueOf(point2.y));
                            }
                        }).y + imageY;
                        if (x >= minX && y >= minY && x < maxX && y < maxY) {
                            startMoving = true;
                            lastX = x;
                            lastY = y;
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    startMoving = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (startMoving) {
                        float deltaX = x - lastX;
                        float deltaY = y - lastY;
                        for (Point p : points) {
                            p.x += deltaX;
                            p.y += deltaY;
                        }
                        p.setStyle(Paint.Style.FILL);
                        p.setColor(Color.BLACK);
                        cvs.drawRect(0, 0, width, imageY, p);
                        cvs.drawRect(0, imageY+resizeHeight, width, imageY+resizeHeight+imageY, p);
                        cvs.drawRect(0, imageY, imageX, imageY+resizeHeight, p);
                        cvs.drawRect(imageX+resizeWidth, imageY, imageX+resizeWidth+imageX, imageY+resizeHeight, p);
                        cvs.drawBitmap(imageBitmap, imageX, imageY, p);
                        drawPoints();
                        img.invalidate();
                        lastX = x;
                        lastY = y;
                    }
                }
                return true;
            }
        });
        img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver.OnGlobalLayoutListener layoutListener = this;
                img.post(new Runnable() {
                    @Override
                    public void run() {
                        Tool.log("Size changed");
                        width = img.getWidth();
                        height = img.getHeight();
                        Tool.log("ImageView width: " + width);
                        Tool.log("ImageView height: " + height);
                        if (width > 0 && height > 0) {
                            if (Build.VERSION.SDK_INT >= 16) {
                                img.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                            } else {
                                img.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                            }
                            Tool.log("Width: " + width + ", Height: " + height);
                            Tool.log("Image width: " + imageBitmap.getWidth() + ", Image height: " + imageBitmap.getHeight());
                            int imageWidth = imageBitmap.getWidth();
                            int imageHeight = imageBitmap.getHeight();
                            imageX = 0;
                            imageY = 0;
                            int resizeWidth = 0;
                            int resizeHeight = 0;
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
                            final Bitmap mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            cvs = new Canvas(mainBitmap);
                            p = new Paint();
                            p.setAntiAlias(true);
                            cvs.drawBitmap(imageBitmap, imageX, imageY, p);
                            img.setImageBitmap(mainBitmap);
                            FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                                    .build();
                            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
                            Tool.log("Detecting faces...");
                            detector.detectInImage(image)
                                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            Tool.save(activity, "first_time_auto_crop", false);
                                            Tool.log("Success detecting faces");
                                            faceCount = faces.size();
                                            Tool.log("Total faces detected: " + faceCount);
                                            if (faceCount == 0) {
                                                progress.setVisibility(View.GONE);
                                                text01.setVisibility(View.GONE);
                                                Tool.show(activity, R.string.text40);
                                                return;
                                            }
                                            for (FirebaseVisionFace face : faces) {
                                                Tool.log("Detecting face outlines...");
                                                List<FirebaseVisionPoint> edges = face.getContour(FirebaseVisionFaceContour.FACE).getPoints();
                                                if (edges != null) {
                                                    Tool.log("Total edges: " + edges.size());
                                                    points = new ArrayList<>();
                                                    for (int i = 0; i < edges.size(); i++) {
                                                        points.add(new Point(imageX+edges.get(i).getX().intValue(), imageY+edges.get(i).getY().intValue()));
                                                    }
                                                    drawPoints();
                                                    progress.setVisibility(View.GONE);
                                                    text01.setVisibility(View.GONE);
                                                    img.invalidate();
                                                    imageLoaded = true;
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Tool.log("Failed to detect faces");
                                            e.printStackTrace();
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    public void drawPoints() {
        Point firstPoint = points.get(0);
        Point lastPoint = points.get(points.size() - 1);
        Path path = new Path();
        path.moveTo(firstPoint.x, firstPoint.y);
        for (int i = 0; i < points.size() - 1; i++) {
            Point currentPoint = points.get(i);
            Point nextPoint = points.get(i + 1);
            if (currentPoint != null && nextPoint != null) {
                path.lineTo(nextPoint.x, nextPoint.y);
            }
        }
        path.lineTo(firstPoint.x, firstPoint.y);
        path.close();
        cvs.drawLine(lastPoint.x, lastPoint.y, firstPoint.x, firstPoint.y, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55ff0000);
        cvs.drawPath(path, p);
    }

    public String save() {
        ProgressDialog saveDialog = new ProgressDialog(activity);
        saveDialog.setMessage(getResources().getString(R.string.text31));
        saveDialog.setCancelable(false);
        saveDialog.show();
        Point nearestXPoint = Collections.min(points, new Comparator<Point>() {
            @Override
            public int compare(Point point1, Point point2) {
                return Integer.valueOf(point1.x).compareTo(Integer.valueOf(point2.x));
            }
        });
        Point nearestYPoint = Collections.min(points, new Comparator<Point>() {
            @Override
            public int compare(Point point1, Point point2) {
                return Integer.valueOf(point1.y).compareTo(Integer.valueOf(point2.y));
            }
        });
        Point farthestXPoint = Collections.max(points, new Comparator<Point>() {
            @Override
            public int compare(Point point1, Point point2) {
                return Integer.valueOf(point1.x).compareTo(Integer.valueOf(point2.x));
            }
        });
        Point farthestYPoint = Collections.max(points, new Comparator<Point>() {
            @Override
            public int compare(Point point1, Point point2) {
                return Integer.valueOf(point1.y).compareTo(Integer.valueOf(point2.y));
            }
        });
        int nearestX = getRealX(nearestXPoint.x - imageX);
        int nearestY = getRealY(nearestYPoint.y - imageY);
        int width = getRealX(farthestXPoint.x - imageX) - getRealX(nearestXPoint.x - imageX);
        int height = getRealY(farthestYPoint.y - imageY) - getRealY(nearestYPoint.y - imageY);
        Tool.log("Selection width: " + width + ", Selection height: " + height);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(result);
        Paint p = new Paint();
        p.setAntiAlias(true);
        cvs.drawARGB(0, 0, 0, 0);
        p.setColor(0xff424242);
        Path path = new Path();
        path.moveTo(getRealX(points.get(0).x - imageX) - nearestX, getRealY(points.get(0).y - imageY) - nearestY);
        for (int i = 1; i < points.size(); i++) {
            path.lineTo(getRealX(points.get(i).x - imageX) - nearestX, getRealY(points.get(i).y - imageY) - nearestY);
        }
        path.close();
        cvs.drawPath(path, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cvs.drawBitmap(activity.imageBitmap, -(nearestX), -(nearestY), p);
        String imagePath = new File(activity.getFilesDir(), UUID.randomUUID().toString()+".png").getAbsolutePath();
        if (imagePath.endsWith("/")) {
            imagePath = imagePath.substring(0, imagePath.length() - 1);
        }
        String imageFileName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
        imagePath = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
        if (imageFileName.contains(".")) {
            imageFileName = imageFileName.substring(0, imageFileName.lastIndexOf("."));
        }
        imageFileName += "_copy.png";
        imagePath += imageFileName;
        Tool.log("Image file path:" + imagePath);
        try {
            result.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(imagePath));
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        saveDialog.dismiss();
        return imagePath;
    }

    private int getRealX(int x) {
        return activity.imageBitmap.getWidth()*x/imageBitmap.getWidth();
    }

    private int getRealY(int y) {
        return activity.imageBitmap.getHeight()*y/imageBitmap.getHeight();
    }
}
