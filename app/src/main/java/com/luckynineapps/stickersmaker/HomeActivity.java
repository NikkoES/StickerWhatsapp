package com.luckynineapps.stickersmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.luckynineapps.stickersmaker.homefragments.LocalFragment;
import com.luckynineapps.stickersmaker.homefragments.WebFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    TabLayout tabs;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("WhatsApp Stickers");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        tabs.setTabRippleColor(null);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WebFragment(), getResources().getString(R.string.text3));
        adapter.addFragment(new LocalFragment(), getResources().getString(R.string.text2));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        final AdView ad = findViewById(R.id.ad);
        ad.loadAd(new AdRequest.Builder()
                .addTestDevice(Constants.TEST_DEVICE_ID)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.privacy_policy) {
            Intent i = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(i);
        } else if (id == R.id.terms) {
            Intent i = new Intent(this, TermsActivity.class);
            startActivity(i);
        } else if (id == R.id.exit) {
            finish();
        }
        return false;
    }
}
