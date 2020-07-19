package com.luckynineapps.stickersmaker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.startapp.android.publish.ads.nativead.NativeAdDetails;
import com.startapp.android.publish.ads.nativead.NativeAdPreferences;
import com.startapp.android.publish.ads.nativead.StartAppNativeAd;
import com.startapp.android.publish.adsCommon.Ad;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.startapp.android.publish.adsCommon.adListeners.AdEventListener;

public class TestActivity extends AppCompatActivity {
    RelativeLayout ctr01;

    @SuppressWarnings("all")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "210600301", true);
        setContentView(R.layout.activity_test);
        ctr01 = findViewById(R.id.ctr01);
        final StartAppNativeAd ad = new StartAppNativeAd(this);
        NativeAdPreferences pref = new NativeAdPreferences()
                .setAdsNumber(1)
                .setAutoBitmapDownload(true)
                .setPrimaryImageSize(2);
        ad.loadAd(pref, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad0) {
                Tool.log("onReceiveAd()");
                NativeAdDetails detail = ad.getNativeAds().get(0);
                Tool.log("Title: "+detail.getTitle());
                Tool.log("Desc: "+detail.getDescription());
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                Tool.log("Failed to receive ad");
            }
        });
    }
}
