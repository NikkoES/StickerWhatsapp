package com.luckynineapps.stickersmaker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS = 1;
    String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    BillingClient c;
    ProgressBar progress;
    TextView output;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);*/

        //setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable e) {
                String DOUBLE_LINE_SEP = "\n\n";
                String SINGLE_LINE_SEP = "\n";
                final StringBuffer report = new StringBuffer(e.toString());
                Throwable cause = e.getCause();
                if (cause != null) {
                    report.append(cause.toString());
                    report.append(SINGLE_LINE_SEP);
                    StackTraceElement[] traces = cause.getStackTrace();
                    for (int i = 0; i < traces.length; i++) {
                        if (i < 2) {
                            report.append("    ");
                            report.append(traces[i].toString());
                            report.append(SINGLE_LINE_SEP);
                        }
                    }
                }
                // Getting the Device brand,model and sdk verion details.
                report.append("\n\n");
                report.append("--------- Device ---------\n");
                report.append("Brand: ");
                report.append(Build.BRAND);
                report.append(SINGLE_LINE_SEP);
                report.append("Device: ");
                report.append(Build.DEVICE);
                report.append(SINGLE_LINE_SEP);
                report.append("Model: ");
                report.append(Build.MODEL);
                report.append(SINGLE_LINE_SEP);
                report.append("Id: ");
                report.append(Build.ID);
                report.append(SINGLE_LINE_SEP);
                report.append("Product: ");
                report.append(Build.PRODUCT);
                report.append(SINGLE_LINE_SEP);
                report.append("\n\n");
                report.append("--------- Firmware ---------\n");
                report.append("SDK: ");
                report.append(Build.VERSION.SDK);
                report.append(SINGLE_LINE_SEP);
                report.append("Release: ");
                report.append(Build.VERSION.RELEASE);
                report.append(SINGLE_LINE_SEP);
                report.append("Incremental: ");
                report.append(Build.VERSION.INCREMENTAL);
                report.append(SINGLE_LINE_SEP);
                report.append("\n");

                Log.e("Report ::", report.toString());
                /*Intent crashedIntent = new Intent(MainActivity.this, ShowCrashActivity.class);
                crashedIntent.putExtra("message",  report.toString());
                crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(crashedIntent);*/
                try {
                    FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "error.log"));
                    fos.write(report.toString().getBytes());
                    fos.flush();
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                System.exit(0);
            }
        });
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(permissions[2]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            } else {
                init();
            }
        } else {
            init();
        }
    }

    public void forceCrash(View view) {
        throw new NullPointerException("This is a null ptr");
    }


    @SuppressWarnings("all")
    public void init() {
        createNotificationChannel();
        StartAppSDK.init(this, "210600301", false);
        StartAppAd.disableSplash();
        StartAppAd.disableAutoInterstitial();
        setContentView(R.layout.activity_main);
        progress = findViewById(R.id.progress);
        output = findViewById(R.id.output);
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, Constants.APP_ID);
        c = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

            }
        }).build();
        c.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                Purchase.PurchasesResult result = c.queryPurchases(BillingClient.SkuType.INAPP);
                if (result != null) {
                    List<Purchase> purchaseList = result.getPurchasesList();
                    if (purchaseList != null) {
                        for (Purchase purchase : result.getPurchasesList()) {
                            if (purchase.getSku().equals("remove_ads")) {
                                Tool.save(MainActivity.this, "ads_removed", true);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
        // Clear cache everytime app starts
        Tool.clearCache(this);
        File defaultStickerFolder = new File(getFilesDir(), "stickers");
        if (!defaultStickerFolder.exists()) {
            defaultStickerFolder.mkdirs();
        }
        File defaultSticker = new File(defaultStickerFolder, "diamond.webp");
        if (!defaultSticker.exists()) {
            Tool.copyAsset(this, "diamond.webp", defaultSticker.getAbsolutePath());
        }
        File defaultTrayIcon = new File(defaultStickerFolder, "diamond.png");
        if (!defaultTrayIcon.exists()) {
            Tool.copyAsset(this, "diamond.png", defaultTrayIcon.getAbsolutePath());
        }
        final int version = Tool.read(this, "version", 0);
        if (version == 0) {
            output.append(getResources().getString(R.string.text95)+"\n");
            // Download necessary data
            downloadEmoticons();
        } else {
            output.append("Loading...\n");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long lastCheckTime = Tool.read(MainActivity.this, "last_check_time", 0L);
                    if (lastCheckTime != 0) {
                        if ((System.currentTimeMillis()-lastCheckTime) > 1*24*60*60*1000) { //7 day
                            checkVersionUpdate();
                        } else {
                            startActivity();
                        }
                    } else {
                        checkVersionUpdate();
                    }
                }
            });
        }
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NewStickers";
            String description = "NewStickers app notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("com.luckynineapps.stickersmaker", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void checkVersionUpdate() {
        try {
            URL url = new URL("http://156.67.216.106/apps/sticker/version");
            URLConnection c = url.openConnection();
            c.connect();
            String response = "";
            InputStream stream = c.getInputStream();
            int read;
            byte[] buffer = new byte[8192];
            while ((read = stream.read(buffer)) != -1) {
                response += new String(buffer, 0, read);
            }
            stream.close();
            response = response.trim();
            final int version = Tool.read(this, "version", 0);
            int versionCode = Integer.parseInt(response);
            if (version < versionCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        output.append("There is new update...\n");
                    }
                });
                // There are new stickers
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                        builder.setContentTitle("New stickers available!");
                        builder.setContentText("There are new stickers for you available to use");
                        builder.setSmallIcon(R.drawable.ic_launcher);
                        Notification not = builder.build();
                        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        mgr.notify(1, not);
                    }
                });
                // Download necessary data
                downloadEmoticons();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        output.append("Starting HomeActivity...\n");
                    }
                });
                startActivity();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    output.append(e.getMessage()+"\n");
                }
            });
        }
    }

    public void startActivity() {
        Intent i = new Intent(this, HomeActivity.class);
        //Intent i = new Intent(this, TestActivity.class);
        startActivity(i);
        finish();
    }

    public void downloadEmoticons() {
        try {
            Tool.log("Downloading emoticons...");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    output.append("Downloading emoticons...\n");
                }
            });
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET,
                    "http://156.67.216.106/apps/sticker/get_emoticons.php", new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    Tool.log("Response: "+response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            output.append("Response: "+response+"\n");
                        }
                    });
                    final File emoticonsFolder = new File(getFilesDir(), "emoticons");
                    if (!emoticonsFolder.exists()) {
                        emoticonsFolder.mkdirs();
                    }
                    try {
                        final JSONArray emoticons = new JSONArray(response);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < emoticons.length(); i++) {
                                    try {
                                        final String name = emoticons.getString(i);
                                        Tool.log("Name: "+name);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                output.append("Name: "+name+"\n");
                                            }
                                        });
                                        File emoticonFile = new File(emoticonsFolder, name);
                                        if (!emoticonFile.exists()) {
                                            FileOutputStream fos = new FileOutputStream(emoticonFile);
                                            URL url = new URL("http://156.67.216.106/apps/sticker/emoticons/" + name);
                                            URLConnection c = url.openConnection();
                                            c.connect();
                                            InputStream stream = c.getInputStream();
                                            int read;
                                            byte[] buffer = new byte[8192];
                                            while ((read = stream.read(buffer)) != -1) {
                                                fos.write(buffer, 0, read);
                                            }
                                            fos.flush();
                                            fos.close();
                                            stream.close();
                                            Tool.log("Downloaded: " + name);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    output.append("Downloaded: " + name+"\n");
                                                }
                                            });
                                        }
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Tool.show(MainActivity.this, e.getMessage());
                                            }
                                        });
                                    }
                                }
                                downloadStickers();
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tool.show(MainActivity.this, e.getMessage());
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(request);
        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Tool.show(MainActivity.this, e.getMessage());
                }
            });
        }
    }

    public void downloadStickers() {
        try {
            Tool.log("Downloading stickers...");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    output.append("Downloading stickers...\n");
                }
            });
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET,
                    "http://156.67.216.106/apps/sticker/get_stickers.php", new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    Tool.log("Response: "+response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            output.append("Response: "+response+"\n");
                        }
                    });
                    final File stickerFolders = new File(getFilesDir(), "stickers");
                    if (!stickerFolders.exists()) {
                        stickerFolders.mkdirs();
                    }
                    try {
                        final JSONArray stickerFolderNames = new JSONArray(response);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < stickerFolderNames.length(); i++) {
                                    try {
                                        JSONObject stickerFolder = stickerFolderNames.getJSONObject(i);
                                        String stickerFolderName = stickerFolder.getString("name");
                                        File fStickerFolder = new File(stickerFolders, stickerFolderName);
                                        if (!fStickerFolder.exists()) {
                                            fStickerFolder.mkdirs();
                                        }
                                        JSONArray stickerFileNames = stickerFolder.getJSONArray("items");
                                        for (int j=0; j<stickerFileNames.length(); j++) {
                                            String stickerFileName = stickerFileNames.getString(j);
                                            File fStickerFile = new File(fStickerFolder, stickerFileName);
                                            if (!fStickerFile.exists()) {
                                                FileOutputStream fos = new FileOutputStream(fStickerFile);
                                                URL url = new URL("http://156.67.216.106/apps/sticker/stickers/" + stickerFolderName +"/" + stickerFileName);
                                                URLConnection c = url.openConnection();
                                                c.connect();
                                                InputStream stream = c.getInputStream();
                                                int read;
                                                byte[] buffer = new byte[8192];
                                                while ((read = stream.read(buffer)) != -1) {
                                                    fos.write(buffer, 0, read);
                                                }
                                                fos.flush();
                                                fos.close();
                                                stream.close();
                                            }
                                        }
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Tool.show(MainActivity.this, e.getMessage());
                                            }
                                        });
                                    }
                                }
                                downloadFonts();
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tool.show(MainActivity.this, e.getMessage());
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(request);
        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Tool.show(MainActivity.this, e.getMessage());
                }
            });
        }
    }

    public void downloadFonts() {
        try {
            Tool.log("Downloading fonts...");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    output.append("Downloading fonts...\n");
                }
            });
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET,
                    "http://156.67.216.106/apps/sticker/get_fonts.php", new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    Tool.log("Response: "+response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            output.append("Response: "+response+"\n");
                        }
                    });
                    final File fontsFolder = new File(getFilesDir(), "fonts");
                    if (!fontsFolder.exists()) {
                        fontsFolder.mkdirs();
                    }
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray emoticons = new JSONArray(response);
                                for (int i = 0; i < emoticons.length(); i++) {
                                    final String name = emoticons.getString(i);
                                    Tool.log("Name: "+name);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            output.append("Name: "+name+"\n");
                                        }
                                    });
                                    File fontFile = new File(fontsFolder, name);
                                    if (!fontFile.exists()) {
                                        FileOutputStream fos = new FileOutputStream(fontFile);
                                        URL url = new URL("http://156.67.216.106/apps/sticker/fonts/" + name);
                                        URLConnection c = url.openConnection();
                                        c.connect();
                                        InputStream stream = c.getInputStream();
                                        int read;
                                        byte[] buffer = new byte[8192];
                                        while ((read = stream.read(buffer)) != -1) {
                                            fos.write(buffer, 0, read);
                                        }
                                        fos.flush();
                                        fos.close();
                                        stream.close();
                                        Tool.log("Downloaded: " + name);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                output.append("Downloaded: " + name+"\n");
                                            }
                                        });
                                    }
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Tool.show(MainActivity.this, e.getMessage());
                                    }
                                });
                            }
                            Tool.save(MainActivity.this, "data_downloaded", true);
                            // Check current version
                            try {
                                URL url = new URL("http://156.67.216.106/apps/sticker/version");
                                URLConnection c = url.openConnection();
                                c.connect();
                                String response = "";
                                InputStream stream = c.getInputStream();
                                int read;
                                byte[] buffer = new byte[8192];
                                while ((read = stream.read(buffer)) != -1) {
                                    response += new String(buffer, 0, read);
                                }
                                stream.close();
                                response = response.trim();
                                final int versionCode = Integer.parseInt(response);
                                Tool.save(MainActivity.this, "version", versionCode);
                                Tool.save(MainActivity.this, "last_check_time", System.currentTimeMillis());
                                Tool.log("Version code: "+versionCode);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        output.append("Version code: "+versionCode+"\n");
                                    }
                                });
                                startActivity();
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
        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Tool.show(MainActivity.this, e.getMessage());
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                init();
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    boolean requestAgain = false;
                    for (String permission : permissions) {
                        if (shouldShowRequestPermissionRationale(permission)) {
                            requestAgain = true;
                            break;
                        }
                    }
                    if (requestAgain) {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setMessage(R.string.text1)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (Build.VERSION.SDK_INT >= 23) {
                                            requestPermissions(permissions, REQUEST_PERMISSIONS);
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                })
                                .create();
                        dialog.show();
                    } else {
                        finish();
                    }
                }
            }
        }
    }
}
