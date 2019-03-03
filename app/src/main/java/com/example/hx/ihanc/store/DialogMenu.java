package com.example.hx.ihanc.store;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DialogMenu extends DialogFragment {
    private stock stock;
    private int store_id;
    private OnSelectActions mListener;
    private Button lossButton;
    private Button detailButton;
    private Button transferButton;
    private Button exchangeButton;
    private Button increaseButton;
    private Button deleteButton;
    private Button carryButton;
    public static DialogMenu newInstance(String goods_name,int store_id){
        DialogMenu dialogMenu=new DialogMenu();
        Bundle b=new Bundle();
        b.putString("goods_name",goods_name);
        b.putInt("store_id",store_id);
        dialogMenu.setArguments(b);
        return dialogMenu;
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String res;
        res=getArguments().getString("goods_name");
        store_id=getArguments().getInt("store_id");
        try{
            JSONObject obj=new JSONObject(res);
            stock=new stock(
                    obj.getInt("stock_id"),
                    obj.getInt("goods_id"),
                    obj.getString("goods_name"),
                    obj.getString("inorder"),
                    obj.getDouble("number"),
                    obj.getInt("sum"),
                    obj.getInt("unit_id"),
                    obj.getString("unit_name")

            );
        }catch (JSONException e){e.printStackTrace();}
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.stock_menu,container,false);
        TextView goods=view.findViewById(R.id.goods_name);
        goods.setText(stock.goods_name);
        lossButton=view.findViewById(R.id.lossButton);
        detailButton=view.findViewById(R.id.detailButton);
        transferButton=view.findViewById(R.id.transferButton);
        exchangeButton=view.findViewById(R.id.exchangeButton);
        increaseButton=view.findViewById(R.id.increaseButton);
        deleteButton=view.findViewById(R.id.deleteButton);
        carryButton=view.findViewById(R.id.carryButton);
        if(stock.number<0){
            transferButton.setVisibility(View.GONE);
            carryButton.setVisibility(View.GONE);
            exchangeButton.setVisibility(View.GONE);
        }else if(MainActivity.storesArray.size()==1){
            exchangeButton.setVisibility(View.GONE);
        }
        if(stock.inorder.equals("0")){
            carryButton.setVisibility(View.GONE);
        }
        lossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnSelectUpdate(true);
                dismiss();
            }
        });
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnSelectUpdate(false);
                dismiss();
            }
        });
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.OnTransfer();
            }
        });
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.OnExchange();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.OnDelete();
            }
        });
        carryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.OnCarry();
            }
        });
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.OnDetail();
            }
        });
        return view;
    }

    public void setOnSelectActions(OnSelectActions mListener) {
        this.mListener = mListener;
    }

    public interface OnSelectActions{
         void OnSelectUpdate(boolean loss);
         void OnTransfer();
         void OnExchange();
         void OnDelete();
         void OnCarry();
         void OnDetail();
    }
}
