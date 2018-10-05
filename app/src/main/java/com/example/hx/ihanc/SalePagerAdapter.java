package com.example.hx.ihanc;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

public class SalePagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
    private List<SaleFragment> fragmentList;
    private List<member> memberList;
    private FragmentManager fm;
    private FragmentTransaction mCurTransaction;

    public SalePagerAdapter(FragmentManager fm, Context context, List<SaleFragment> fragmentList, List<member> members){
        super(fm);
        this.context = context;
        this.fragmentList = fragmentList;
        this.memberList = members;
        this.fm=fm;
    };

    @Override
    public Fragment getItem(int position) {
        if(memberList.size()==0) return null;
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return memberList.get(position).getMember_name();
    }
    @Override
    public int getItemPosition(Object object)   {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
