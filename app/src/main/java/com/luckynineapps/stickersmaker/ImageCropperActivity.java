package com.luckynineapps.stickersmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.luckynineapps.stickersmaker.imagecropperfragments.AutoCroppingFragment;
import com.luckynineapps.stickersmaker.imagecropperfragments.ManualCroppingFragment;

import java.util.ArrayList;

public class ImageCropperActivity extends AppCompatActivity {
    NonSwipeableViewPager viewPager;
    TabLayout tabs;
    ViewPagerAdapter adapter;
    ManualCroppingFragment manualCropper;
    AutoCroppingFragment autoCropper;
    ArrayList<OnBackPressedListener> backPressedListeners = new ArrayList<>();
    public Bitmap imageBitmap;

    @SuppressWarnings("all")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setTitle(R.string.text28);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        byte[] imageData = getIntent().getByteArrayExtra("image");
        Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        imageBitmap = image;
        tabs.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        manualCropper = new ManualCroppingFragment();
        autoCropper = new AutoCroppingFragment();
        adapter.addFragment(manualCropper, "Manual");
        adapter.addFragment(autoCropper, "Auto");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {

                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
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

    public void addOnBackPressedListener(OnBackPressedListener listener) {
        backPressedListeners.add(listener);
    }

    public void undo(View view) {
        if (tabs.getSelectedTabPosition() == 0) {
            manualCropper.undo();
        }
    }

    public void redo(View view) {
        if (tabs.getSelectedTabPosition() == 0) {
            manualCropper.redo();
        }
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
        public Fragment getItem(int position) {
            return fragments.get(position);
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
        getMenuInflater().inflate(R.menu.menu_image_cropper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.apply) {
            String imagePath = "";
            if (tabs.getSelectedTabPosition() == 0) {
                // Manual
                imagePath = manualCropper.save();
            } else if (tabs.getSelectedTabPosition() == 1) {
                // Auto
                imagePath = autoCropper.save();
            }
            if (imagePath != null) {
                Intent i = new Intent();
                i.putExtra("image_path", imagePath);
                setResult(RESULT_OK, i);
                finish();
            }
        } else if (id == R.id.cancel) {
            if (tabs.getSelectedTabPosition() == 0) {
                if (manualCropper.edited) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage(R.string.text30)
                            .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.text_no, null)
                            .create();
                    dialog.show();
                } else {
                    finish();
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (backPressedListeners.size() > 0) {
            for (int i=0; i<backPressedListeners.size(); i++) {
                if (tabs.getSelectedTabPosition() == i) {
                    OnBackPressedListener listener = backPressedListeners.get(i);
                    listener.onBackPressed();
                    break;
                }
            }
        } else {
            super.onBackPressed();
        }
    }
}
