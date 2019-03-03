package com.example.hx.ihanc.purchase;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hx.ihanc.ListPagerAdapter;
import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;

public class PurchaseMainFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Context mContext;
    private MainActivity parentActivity;
    private ListPagerAdapter mAdapter;
    private String[] title;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private int size=3;
    private PurchaseFragment purchaseFragment;
    public PurchaseMainFragment(){}
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
       // size=MainActivity.storesArray.size();
        title=new String[size];
        fragments=new Fragment[size];
        title[0]="新进货单";
        title[1]="历史进货";
        title[2]="应付管理";
        if (getArguments()==null)
            purchaseFragment=new PurchaseFragment();
        else
            purchaseFragment=PurchaseFragment.newInstance(getArguments());
        fragments[0]=purchaseFragment;
        fragments[1]=new PurchaseListFragment();
        fragments[2]=new PurchaseCreditFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        setArguments(purchaseFragment.getBundle());
        for (int i = 0; i <size ; i++) {
            fragmentTransaction.remove(fragments[i]);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
