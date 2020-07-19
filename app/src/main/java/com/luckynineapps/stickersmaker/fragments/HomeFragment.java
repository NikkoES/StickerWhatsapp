package com.luckynineapps.stickersmaker.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luckynineapps.stickersmaker.HomeActivity;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.homefragments.LocalFragment;
import com.luckynineapps.stickersmaker.homefragments.WebFragment;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    View v;
    HomeActivity activity;
    TabLayout tabs;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (HomeActivity)getActivity();
        tabs = v.findViewById(R.id.tabs);
        viewPager = v.findViewById(R.id.view_pager);
        tabs.setTabRippleColor(null);
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new WebFragment(), activity.getResources().getString(R.string.text3));
        adapter.addFragment(new LocalFragment(), activity.getResources().getString(R.string.text2));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
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
}
