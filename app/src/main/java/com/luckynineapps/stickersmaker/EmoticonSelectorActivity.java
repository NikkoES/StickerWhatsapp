package com.luckynineapps.stickersmaker;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.luckynineapps.stickersmaker.emoticonselectorfragments.CustomFragment;
import com.luckynineapps.stickersmaker.emoticonselectorfragments.DefaultFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EmoticonSelectorActivity extends AppCompatActivity {
    TabLayout tabs;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon_selector);
        setTitle(R.string.text51);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Copy all emoticons
        File emoticonsFolder = new File(getFilesDir(), "emoticons");
        if (!emoticonsFolder.exists()) {
            emoticonsFolder.mkdirs();
        }
        DefaultFragment fr = new DefaultFragment();
        Bundle args = new Bundle();
        args.putString("author_name", "Smashicons");
        args.putString("author_url", "https://www.flaticon.com/authors/smashicons");
        args.putString("provider_name", "www.flaticon.com");
        args.putString("provider_url", "www.flaticon.com");
        adapter.addFragment(new DefaultFragment(), getResources().getString(R.string.text52));
        adapter.addFragment(new CustomFragment(), getResources().getString(R.string.text53));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
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

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragments;
        ArrayList<String> fragmentTitles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            fragmentTitles = new ArrayList<>();
        }

        public void addFragment(Fragment fr, String title) {
            fragments.add(fr);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return false;
    }
}
