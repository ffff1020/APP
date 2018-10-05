package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;

import java.util.List;

public class ListPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private String[] titles;
    private Fragment[] fragments;

    public ListPagerAdapter(FragmentManager fm, Context context, String[] titles,Fragment[] fragments ){
        super(fm);
        this.context = context;
        this.titles = titles;
        this.fragments=fragments;
    };


    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position) {
        if(titles.length==0) return null;
        return fragments[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
    @Override
    public int getItemPosition(Object object)   {
        return PagerAdapter.POSITION_NONE;
    }


}
