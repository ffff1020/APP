package com.example.hx.ihanc.store;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hx.ihanc.BankListFragment;
import com.example.hx.ihanc.ListPagerAdapter;
import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.creditFragment;
import com.example.hx.ihanc.saleListFragment;

import java.util.Map;

public class StoreFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Context mContext;
    private MainActivity parentActivity;
    private ListPagerAdapter mAdapter;
    private String[] title;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private int size;
    public StoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext=context;
        parentActivity=(MainActivity ) getActivity();
        fragmentManager=parentActivity.getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("storeFragment","onCreateView");
        // Inflate the layout for this fragment
        setData();
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        viewPager=view.findViewById(R.id.viewPager);
        tabLayout=view.findViewById(R.id.tabLayout);
        mAdapter=new ListPagerAdapter(parentActivity.getSupportFragmentManager(),mContext,title,fragments);
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        mAdapter.notifyDataSetChanged();
        return view;
    }

    private void setData(){
        size=MainActivity.storesArray.size();
        title=new String[size];
        fragments=new Fragment[size];
        for (int i = 0; i < size; i++) {
            store item=MainActivity.storesArray.get(i);
            title[i]=item.getStore_name();
            fragments[i]=StoreItemFragment.newInstance(item.getStore_id());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i <size ; i++) {
            fragmentTransaction.remove(fragments[i]);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

}
