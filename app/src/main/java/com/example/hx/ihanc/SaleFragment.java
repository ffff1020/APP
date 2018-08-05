package com.example.hx.ihanc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SaleFragment extends Fragment {
    private member member;
    private TextView title;
    private Button button;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sale_fragment, container, false);
        title=(TextView)view.findViewById(R.id.title);
        if(this.member!=null) {
            title.setText(this.member.getMember_name() + this.member.getMember_sn());
            Log.d("fragment.java",this.member.getMember_name() + this.member.getMember_sn());
        }
        button=(Button)view.findViewById(R.id.closeFragmentButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity parentActivity = (MainActivity ) getActivity();
                parentActivity.deleteCurrentSaleTabs();
            }
        });
        return view;
    }

    public void setMember(com.example.hx.ihanc.member member) {
        this.member = member;
    }

    public member getMember(){
        return member;
    }

}
