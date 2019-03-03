package com.example.hx.ihanc;


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
import android.view.inputmethod.InputMethodManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Context mContext;
    private MainActivity parentActivity;
    private ListPagerAdapter mAdapter;
    private String[] title;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private int current=0;

    public ListFragment() {
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
        //Log.d("listFragment","onCreateView");
        // Inflate the layout for this fragment
        setData();
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        //Log.d("listFragment","onCreateView");
        viewPager=view.findViewById(R.id.viewPager);
        tabLayout=view.findViewById(R.id.tabLayout);
        mAdapter=new ListPagerAdapter(parentActivity.getSupportFragmentManager(),mContext,title,fragments);
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        mAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(current);
        return view;
    }

    private void setData(){
        title=new String[4];
        fragments=new Fragment[4];
        title[0]="历史销售";
        title[1]="销售订单";
        title[2]="收款明细";
        title[3]="应收管理";
        fragments[0]=new saleListFragment();
        fragments[1]=saleListFragment.newInstance(true);
        fragments[2]=new BankListFragment();
        fragments[3]=new creditFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragments[0]);
        fragmentTransaction.remove(fragments[1]);
        fragmentTransaction.remove(fragments[2]);
        fragmentTransaction.remove(fragments[3]).commitAllowingStateLoss();
    }

    public void setPushInfo(String title){
        if(title.equals("order"))current=1;
    }



}
