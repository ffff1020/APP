package com.example.hx.ihanc;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hx.ihanc.ui.datasetting.DataSettingFragment;

public class DataSettingActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    //private Context mContext;
    private ListPagerAdapter mAdapter;
    private String[] title;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_setting_activity);
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DataSettingFragment.newInstance())
                    .commitNow();
        }*/
        setData();
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);
        mAdapter=new ListPagerAdapter(getSupportFragmentManager(),this,title,fragments);
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        mAdapter.notifyDataSetChanged();
    }

    private void setData(){
        title=new String[2];
        fragments=new Fragment[2];
        title[0]="初期应收录入";
        title[1]="初期应付录入";
        fragments[0]=new DataSettingFragment();
        fragments[1]=new DataSettingFragment();
    }
}
