package com.luckynineapps.stickersmaker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public class Tool {
    public static final String adminEmail = "cs@luckynineapps.com";
    public static int NEXT_NOTIFICATION_ID = 1;
    public static String REPORTER_EMAIL = "noreply@luckynineapps.com"; // Sender
    public static String REPORTER_PASSWORD = "jakarta123";
    public static String REPORT_EMAIL = "cs@luckynineapps.com"; // Recepient
    public static String myUid = "";
    public static long NEW_RULES_EXPIRY = 30 * 86400000L; // Rentang hari semenjak peraturan baru diupload ke Firebase
    // Setelah melewati rentang hari, maka peraturan tersebut tidak lagi dianggap baru
    // Artinya, tidak akan ditampilkan kepada user

    public static File getDataDir() {
        File dataDir = new File(Environment.getExternalStorageDirectory(), "Android/data/com.luckynineapps.stickersmaker");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    public static void log(String message) {
        Log.e("Log", message);
    }

    public static void show(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context ctx, int resourceId) {
        Toast.makeText(ctx, resourceId, Toast.LENGTH_SHORT).show();
    }

    public static void save(Context ctx, String name, String value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(name, value);
        e.commit();
    }

    public static void save(Context ctx, String name, boolean value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putBoolean(name, value);
        e.commit();
    }

    public static void save(Context ctx, String name, int value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt(name, value);
        e.commit();
    }

    public static void save(Context ctx, String name, long value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putLong(name, value);
        e.commit();
    }

    public static void save(Context ctx, String name, Set<String> value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putStringSet(name, value);
        e.commit();
    }

    public static void save(Context ctx, String name, ArrayList<String> value) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String serializedValue = ObjectSerializer.serialize(value);
        e.putString(name, serializedValue);
        e.commit();
    }

    public static String read(Context ctx, String name, String defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getString(name, defaultValue);
    }

    public static boolean read(Context ctx, String name, boolean defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getBoolean(name, defaultValue);
    }

    public static int read(Context ctx, String name, int defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getInt(name, defaultValue);
    }

    public static long read(Context ctx, String name, long defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getLong(name, defaultValue);
    }

    public static Set<String> readSet(Context ctx, String name, Set<String> defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getStringSet(name, defaultValue);
    }

    @SuppressWarnings("all")
    public static ArrayList<String> read(Context ctx, String name) {
        SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
        String serializedValue = sp.getString(name, null);
        if (serializedValue != null) {
            ArrayList<String> value = (ArrayList<String>) ObjectSerializer.deserialize(serializedValue);
            return value;
        }
        return null;
    }

    public static int dpToPx(Context ctx, int dp) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context ctx, int px) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getWidth());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getWidth() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static void copyFile(Context ctx, Uri uri, File file) {
        try {
            InputStream fis = ctx.getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void copyFile(String path1, String path2) {
        try {
            FileInputStream fis = new FileInputStream(path1);
            FileOutputStream fos = new FileOutputStream(path2);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void extractURIToFile(Context ctx, Uri uri, File dst) {
        try {
            InputStream stream = ctx.getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(dst);
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
    }

    public static int getClosestNumber(ArrayList<Integer> numbers, int number) {
        boolean haveNumberBelowIt = false;
        for (int n : numbers) {
            if (n < number) {
                haveNumberBelowIt = true;
                break;
            }
        }
        if (!haveNumberBelowIt) {
            return -1;
        }
        int distance = Math.abs(numbers.get(0) - number);
        int idx = 0;
        for (int c = 1; c < numbers.size(); c++) {
            int n2 = numbers.get(c);
            if (n2 > number) {
                continue;
            }
            int cdistance = Math.abs(n2 - number);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return numbers.get(idx);
    }

    public static ArrayList<Integer> getLowerNumbers(ArrayList<Integer> numbers, int number) {
        ArrayList<Integer> lowerNumbers = new ArrayList<>();
        for (int n : numbers) {
            if (n <= number) {
                lowerNumbers.add(n);
            }
        }
        return lowerNumbers;
    }

    public static boolean isGPSEnabled(Context ctx) {
        LocationManager locMgr = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static int daysBetween(java.util.Date d1, java.util.Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    @SuppressWarnings("unchecked")
    public static void openPlayStore(Context context) {
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @SuppressWarnings("unchecked")
    public static void openPlayStore(Context context, String packageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static void openURL(Context ctx, String url) {
        ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String replaceChar(String text, int index, char ch) {
        return text.substring(0, index) + ch + text.substring(index + 1, text.length());
    }

    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @SuppressLint("NewApi")
    public static String getDataPath(Context context, Uri uri) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void openFile(Context ctx, String filePath) {
        Uri uri = Uri.fromFile(new File(filePath));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (filePath.toLowerCase().endsWith(".doc") || filePath.contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (filePath.toLowerCase().endsWith(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (filePath.toLowerCase().endsWith(".ppt") || filePath.toLowerCase().endsWith(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (filePath.toLowerCase().endsWith(".xls") || filePath.toLowerCase().endsWith(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (filePath.toLowerCase().endsWith(".zip") || filePath.toLowerCase().endsWith(".rar")) {
            intent.setDataAndType(uri, "application/x-wav");
        } else if (filePath.toLowerCase().endsWith(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if (filePath.toLowerCase().endsWith(".wav") || filePath.toLowerCase().endsWith(".mp3")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (filePath.toLowerCase().endsWith(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg") || filePath.toLowerCase().endsWith(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (filePath.toLowerCase().endsWith(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (filePath.toLowerCase().endsWith(".3gp")
                || filePath.toLowerCase().endsWith(".mpg")
                || filePath.toLowerCase().endsWith(".mpeg")
                || filePath.toLowerCase().endsWith(".mkv")
                || filePath.toLowerCase().endsWith(".mp4")
                || filePath.toLowerCase().endsWith(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static String getDateString(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String timeString = "";
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            timeString += "0";
        }
        timeString += Integer.toString(day);
        timeString += "/";
        int month = c.get(Calendar.MONTH) + 1;
        if (month < 10) {
            timeString += "0";
        }
        timeString += Integer.toString(month);
        timeString += "/";
        int year = c.get(Calendar.YEAR);
        timeString += Integer.toString(year);
        return timeString;
    }

    public static void initiateMyUid(Context ctx) {
        myUid = Tool.read(ctx, "uid", "");
    }

    public static void setMyUid(Context ctx, String uid) {
        myUid = uid;
    }

    public static String getMyUid() {
        return myUid;
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        return height;
    }

    @SuppressWarnings({"all", "deprecation"})
    public static boolean isActivityRunning(Context ctx, Class cls) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (task.baseActivity.getPackageName().equalsIgnoreCase(cls.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void copyAsset(Context ctx, String assetName, String dstPath) {
        try {
            InputStream stream = ctx.getAssets().open(assetName);
            FileOutputStream fos = new FileOutputStream(dstPath);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            fos.close();
            stream.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static String getCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean isMinimumWAVersion(Context ctx) {
        try {
            String versionName = ctx.getPackageManager().getPackageInfo("com.whatsapp", 0).versionName;
            //TODO get WA minimum version
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void selectImage(Activity activity, String title, int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, title), code);
    }

    public static void selectImage(Activity activity, int titleId, int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(titleId)), code);
    }

    public static void selectImage(Fragment fr, String title, int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fr.startActivityForResult(Intent.createChooser(intent, title), code);
    }

    public static void selectImage(Fragment fr, Activity activity, int titleId, int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fr.startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(titleId)), code);
    }

    public static void selectFile(Activity activity, String title, int code, String... mimeTypes) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, title), code);
    }

    public static void clearCache(Context ctx) {
        File cacheFolder = new File(ctx.getFilesDir(), "cache");
        if (cacheFolder.exists()) {
            for (File cache : cacheFolder.listFiles()) {
                cache.delete();
            }
        }
    }

    public static Bitmap getRotatedBitmap(Bitmap bitmap, float rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    public static int abs(int a) {
        if (a < 0) {
            return -a;
        }
        return a;
    }

    public static boolean isNegative(int a) {
        return a < 0;
    }

    public static boolean isPackageExists(Context ctx, String packageName) {
        try {
            return ctx.getPackageManager().getApplicationInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getNumberOfGridColumns(Context context, int itemWidth) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / itemWidth);
        return noOfColumns;
    }

    public static void addStickersToWhatsApp2(final Context context, final StickerPack stickerPack) {
        try {
            if (!Tool.isPackageExists(context, "com.whatsapp")) {
                Tool.show(context, R.string.text90);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Tool.show(context, e.getMessage());
        }
        final ProgressDialog loadingDialog = new ProgressDialog(context);
        loadingDialog.setMessage(context.getResources().getString(R.string.text26));
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... values) {
                try {
                    if (stickerPack.getFiles().size() < 3) {
                        return 1;
                    }
                    String path = stickerPack.getPath();
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    String names = path.substring(path.lastIndexOf("/") + 1, path.length());
                    String id = names.split("_")[0];
                    String name = names.split("_")[1];
                    boolean allFilesDownloaded = true;
                    ArrayList<String> stickerPaths = Tool.read(context, id + "_sticker_paths");
                    ArrayList<String> stickerURLs = Tool.read(context, id + "_sticker_urls");
                    for (String url : stickerURLs) {
                        Tool.log("Sticker URL: " + url);
                    }
                    for (String p : stickerPaths) {
                        Tool.log("Sticker path: " + p);
                    }
                    Tool.log("=========================");
                    for (String p : stickerPaths) {
                        if (!new File(p).exists()) {
                            allFilesDownloaded = false;
                            break;
                        }
                    }
                    if (!allFilesDownloaded) {
                        stickerPack.getFiles().clear();
                        for (int i = 0; i < stickerPaths.size(); i++) {
                            String p = stickerPaths.get(i);
                            File stickerFile = new File(p);
                            FileItem item = new FileItem(p, stickerPack.getPath(), FileItem.TYPE_IMAGE, stickerFile.lastModified(), id);
                            stickerPack.getFiles().add(item);
                            if (!stickerFile.exists()) {
                                String stickerURL = stickerURLs.get(i);
                                try {
                                    URL url = new URL(stickerURL);
                                    URLConnection c = url.openConnection();
                                    c.connect();
                                    InputStream stream = c.getInputStream();
                                    FileOutputStream fos = new FileOutputStream(stickerFile);
                                    int read;
                                    byte[] buffer = new byte[1024 * 1024];
                                    while ((read = stream.read(buffer)) != -1) {
                                        fos.write(buffer, 0, read);
                                    }
                                    fos.flush();
                                    fos.close();
                                    stream.close();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    // Copy all stickers to data folder
                    File stickerFolder = new File(Tool.getDataDir(), "stickers/" + name);
                    if (!stickerFolder.exists()) {
                        stickerFolder.mkdirs();
                    }
                    ArrayList<Sticker> stickers = new ArrayList<>();
                    //String trayImageName = "";
                    int looper = 0;
                    for (FileItem item : stickerPack.getFiles()) {
                        if (new File(item.getPath()).isFile()) {
                            String stickerPath = item.getPath();
                            if (stickerPath.endsWith("/")) {
                                stickerPath = stickerPath.substring(0, stickerPath.length() - 1);
                            }
                            // Convert local_sticker to WebP image
                            Bitmap emptyPicture = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                            Canvas cvs = new Canvas(emptyPicture);
                            Paint p = new Paint();
                            p.setAntiAlias(true);
                            Tool.log("Sticker path: " + stickerPath);
                            Bitmap picture = BitmapFactory.decodeFile(stickerPath);
                            Bitmap newPicture = picture.createScaledBitmap(picture, 480, 480, true);
                            picture.recycle();
                            cvs.drawBitmap(newPicture, 16, 16, p);
                            String stickerFileName = stickerPath.substring(stickerPath.lastIndexOf("/") + 1, stickerPath.length());
                            stickerFileName = stickerFileName.substring(0, stickerFileName.lastIndexOf(".")) + ".webp";
                            stickerFileName = stickerFileName.replace("%", "");
                            stickerFileName = stickerFileName.replace(")", "");
                            stickerFileName = stickerFileName.replace("(", "");
                            File newStickerFile = new File(stickerFolder, stickerFileName);
                            try {
                                emptyPicture.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(newStickerFile));
                                if (newStickerFile.length() >= 102400) {
                                    emptyPicture.compress(Bitmap.CompressFormat.WEBP, 50, new FileOutputStream(newStickerFile));
                                }
                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                            if (looper == 0) {
                                //trayImageName = stickerFileName;
                                Bitmap trayBitmap = Bitmap.createScaledBitmap(emptyPicture, 96, 96, true);
                                try {
                                    trayBitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(new File(stickerFolder, "tray.webp")));
                                } catch (Exception exp) {
                                    exp.printStackTrace();
                                }
                                trayBitmap.recycle();
                            }
                            emptyPicture.recycle();
                            newPicture.recycle();
                            Sticker sticker = new Sticker(stickerFileName, Arrays.asList(new String[]{
                                    "☕", "☕"
                            }));
                            sticker.setPath(newStickerFile.getAbsolutePath());
                            stickers.add(sticker);
                        }
                        looper++;
                    }
                    addStickerPack2(context, id, name, name, "tray.webp", "", "", "", "", stickers);
                    //addStickerPack("1", "DanaOS Stickers 1", "Dana", "/sdcard/tray.png", "", "", "", "", stickers);
                    Intent i = new Intent();
                    i.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                    i.putExtra("sticker_pack_id", id);
                    i.putExtra("sticker_pack_authority", "com.luckynineapps.stickersmaker.stickercontentprovider");
                    i.putExtra("sticker_pack_name", name);
                    context.startActivity(i);
                    int totalAdds = Tool.read(context, "total_adds", 0);
                    Tool.log("Total adds: " + totalAdds);
                    totalAdds++;
                    Tool.save(context, "total_adds", totalAdds);
                } catch (final Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Tool.show(context, e.getMessage());
                        }
                    });
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer value) {
                super.onPostExecute(value);
                loadingDialog.dismiss();
                if (value == 1) { //Total stickers is less than 3
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setMessage(R.string.text17)
                            .setPositiveButton("OK", null)
                            .create();
                    dialog.show();
                }
            }
        }.execute();
    }

    @SuppressWarnings({"all", "unchecked"})
    public static void addStickerPack2(final Context context, String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyURL, String licenseAgreementURL, ArrayList<Sticker> stickers) {
        try {
            SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            String stickerPacksString = sp.getString("sticker_packs", "");
            ArrayList<StickerPack> stickerPacks;
            if (stickerPacksString.equals("")) {
                stickerPacks = new ArrayList<>();
            } else {
                stickerPacks = (ArrayList<StickerPack>) ObjectSerializer.deserialize(stickerPacksString);
            }
            StickerPack pack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyURL, licenseAgreementURL);
            pack.setStickers(stickers);
            boolean packAlreadyAdded = false;
            int previousPackIndex = 0;
            for (int i = 0; i < stickerPacks.size(); i++) {
                StickerPack stickerPack = stickerPacks.get(i);
                if (stickerPack.identifier.equals(identifier)) {
                    previousPackIndex = i;
                    packAlreadyAdded = true;
                    break;
                }
            }
            if (packAlreadyAdded) {
                stickerPacks.set(previousPackIndex, pack);
            } else {
                stickerPacks.add(pack);
            }
            e.putString("sticker_packs", ObjectSerializer.serialize(stickerPacks));
            e.commit();
        } catch (final Exception e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Tool.show(context, e.getMessage());
                        }
                    });
        }
    }

    @SuppressWarnings("all")
    public static void addStickersToWhatsApp(final Context context, final Folder folder) {
        if (!Tool.isPackageInstalled(context, "com.whatsapp")) {
            Tool.show(context, R.string.text90);
            return;
        }
        final ProgressDialog loadingDialog = new ProgressDialog(context);
        loadingDialog.setMessage(context.getResources().getString(R.string.text26));
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... values) {
                if (folder.getFiles().size() < 4) {
                    return 1;
                }
                String path = folder.getPath();
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                String name = path.substring(path.lastIndexOf("/") + 1, path.length());
                // Copy all stickers to data folder
                File stickerFolder = new File(context.getFilesDir(), "localstickers/" + name);
                if (!stickerFolder.exists()) {
                    stickerFolder.mkdirs();
                }
                ArrayList<Sticker> stickers = new ArrayList<>();
                //String trayImageName = "";
                int looper = 0;
                for (FileItem item : folder.getFiles()) {
                    if (new File(item.getPath()).isFile()) {
                        String stickerPath = item.getPath();
                        if (stickerPath.endsWith("/")) {
                            stickerPath = stickerPath.substring(0, stickerPath.length() - 1);
                        }
                        // Convert local_sticker to WebP image
                        Bitmap emptyPicture = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                        Canvas cvs = new Canvas(emptyPicture);
                        Paint p = new Paint();
                        p.setAntiAlias(true);
                        Bitmap picture = BitmapFactory.decodeFile(stickerPath);
                        Bitmap newPicture = picture.createScaledBitmap(picture, 480, 480, true);
                        picture.recycle();
                        cvs.drawBitmap(newPicture, 16, 16, p);
                        String stickerFileName = stickerPath.substring(stickerPath.lastIndexOf("/") + 1, stickerPath.length());
                        stickerFileName = stickerFileName.substring(0, stickerFileName.lastIndexOf(".")) + ".webp";
                        stickerFileName = stickerFileName.replace("%", "");
                        stickerFileName = stickerFileName.replace(")", "");
                        stickerFileName = stickerFileName.replace("(", "");
                        File newStickerFile = new File(stickerFolder, stickerFileName);
                        try {
                            emptyPicture.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(newStickerFile));
                            if (newStickerFile.length() >= 102400) {
                                emptyPicture.compress(Bitmap.CompressFormat.WEBP, 50, new FileOutputStream(newStickerFile));
                            }
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                        if (looper == 0) {
                            //trayImageName = stickerFileName;
                            Bitmap trayBitmap = Bitmap.createScaledBitmap(emptyPicture, 96, 96, true);
                            try {
                                trayBitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(new File(stickerFolder, "tray.webp")));
                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                            trayBitmap.recycle();
                        }
                        emptyPicture.recycle();
                        newPicture.recycle();
                        if (looper != 0) {
                            Sticker sticker = new Sticker(stickerFileName, Arrays.asList(new String[]{
                                    "☕", "☕"
                            }));
                            sticker.setPath(newStickerFile.getAbsolutePath());
                            stickers.add(sticker);
                        }
                    }
                    looper++;
                }
                /*if (trayImageName.equals("")) {
                    trayImageName = UUID.randomUUID()+".png";
                }
                //Tool.log("Tray file name: "+trayImageName);*/
                String id = name.replace("%", "");
                addStickerPack(context, id, name, name, "tray.webp", "", "", "", "", stickers);
                //addStickerPack("1", "DanaOS Stickers 1", "Dana", "/sdcard/tray.png", "", "", "", "", stickers);
                Intent i = new Intent();
                i.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                i.putExtra("sticker_pack_id", id);
                i.putExtra("sticker_pack_authority", "com.dn.whatsappsticker.stickercontentprovider");
                i.putExtra("sticker_pack_name", name);
                context.startActivity(i);
                int totalAdds = Tool.read(context, "total_adds", 0);
                Tool.log("Total adds: " + totalAdds);
                totalAdds++;
                Tool.save(context, "total_adds", totalAdds);
                return 0;
            }

            @Override
            protected void onPostExecute(Integer value) {
                super.onPostExecute(value);
                loadingDialog.dismiss();
                if (value == 1) { //Total stickers is less than 3
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setMessage(R.string.text17)
                            .setPositiveButton("OK", null)
                            .create();
                    dialog.show();
                }
            }
        }.execute();
    }

    @SuppressWarnings({"all", "unchecked"})
    private static void addStickerPack(Context context, String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyURL, String licenseAgreementURL, ArrayList<Sticker> stickers) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String stickerPacksString = sp.getString("sticker_packs", "");
        ArrayList<StickerPack> stickerPacks;
        if (stickerPacksString.equals("")) {
            stickerPacks = new ArrayList<>();
        } else {
            stickerPacks = (ArrayList<StickerPack>) ObjectSerializer.deserialize(stickerPacksString);
        }
        StickerPack pack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyURL, licenseAgreementURL);
        pack.setStickers(stickers);
        boolean packAlreadyAdded = false;
        int previousPackIndex = 0;
        for (int i = 0; i < stickerPacks.size(); i++) {
            StickerPack stickerPack = stickerPacks.get(i);
            if (stickerPack.identifier.equals(identifier)) {
                previousPackIndex = i;
                packAlreadyAdded = true;
                break;
            }
        }
        if (packAlreadyAdded) {
            stickerPacks.set(previousPackIndex, pack);
        } else {
            stickerPacks.add(pack);
        }
        e.putString("sticker_packs", ObjectSerializer.serialize(stickerPacks));
        e.commit();
    }

    public static boolean isPackageInstalled(Context ctx, String packageName) {
        try {
            return ctx.getPackageManager().getApplicationInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void downloadFile(final String fileURL, final File targetFile, final OnCompletionListener completeListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(fileURL);
                    URLConnection c = url.openConnection();
                    c.connect();
                    InputStream stream = c.getInputStream();
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int read;
                    byte[] buffer = new byte[1024 * 1024];
                    while ((read = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                    fos.close();
                    stream.close();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (completeListener != null) {
                                completeListener.onComplete();
                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void floodFill(Bitmap image, Point node, int targetColor, int replacementColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        int target = targetColor;
        int replacement = replacementColor;
        if (target != replacement) {
            Queue<Point> queue = new LinkedList<Point>();
            do {
                int x = node.x;
                int y = node.y;
                while (x > 0 && image.getPixel(x - 1, y) == target) {
                    x--;
                }
                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width && image.getPixel(x, y) == target) {
                    image.setPixel(x, y, replacement);
                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target) {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    } else if (spanUp && y > 0
                            && image.getPixel(x, y - 1) != target) {
                        spanUp = false;
                    }
                    if (!spanDown && y < height - 1
                            && image.getPixel(x, y + 1) == target) {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    } else if (spanDown && y < height - 1
                            && image.getPixel(x, y + 1) != target) {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((node = queue.poll()) != null);
        }
    }

    public static void sendMessage(Context ctx, String phoneNumber, String message) {
        if (isPackageInstalled(ctx, "com.whatsapp")) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.setPackage("com.whatsapp");
            i.putExtra(Intent.EXTRA_TEXT, message);
            ctx.startActivity(Intent.createChooser(i, "Error reporting"));
        }
    }
}