package com.example.hx.ihanc.store;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.hx.ihanc.R;

public class DialogUpdateStock extends DialogFragment {
    private boolean loss;
    private View view;
    private String goods_name;
    private OnUpdateStock mListener;
    private TextView tv;
    public static DialogUpdateStock newInstance(boolean loss,String goods_name){
        DialogUpdateStock d=new DialogUpdateStock();
        Bundle bundle=new Bundle();
        bundle.putBoolean("loss",loss);
        bundle.putString("goods_name",goods_name);
        d.setArguments(bundle);
        return d;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loss=getArguments().getBoolean("loss");
        goods_name=getArguments().getString("goods_name");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.dialog_update_stock,container,false);
        TextView textView=view.findViewById(R.id.goods_name);
        Button saveButton=view.findViewById(R.id.saveButton);
        Button cancelButton=view.findViewById(R.id.cancelButton);
        tv=view.findViewById(R.id.number);
        String info;
        if (loss){
            info=goods_name+"报损";
            textView.setText(info);
            textView.setTextColor(Color.RED);
            saveButton.setText("报损");
        }else{
            info=goods_name+"报溢";
            textView.setText(info);
            textView.setTextColor(Color.parseColor("#058e35"));
            saveButton.setText("报溢");
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                double number=Double.parseDouble(tv.getText().toString().trim());
                if(loss)number=(-1)*number;
                mListener.OnUpdateStock(number);
            }
        });
        return view;
    }

    public void setOnUpDateStock(OnUpdateStock mListener){
        this.mListener=mListener;
    }

    public interface OnUpdateStock{
        void OnUpdateStock(double number);
    }
}
