package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class CreditDetailAdpter extends ArrayAdapter<CreditDetail> {
    private Context mContext;
    private List<CreditDetail> creditDetailsList;
    private int resource;
    private MyOnCheckChangeListener myOnCheckChangeListener;
    private static HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    private checkBoxListener mCheckBoxListener;
    public CreditDetailAdpter(@NonNull Context context, int resource, @NonNull List<CreditDetail> creditDetailsList,checkBoxListener myOnCheckChangeListener){
        super(context, resource, creditDetailsList);
        this.mContext=context;
        this.resource=resource;
        this.creditDetailsList=creditDetailsList;
        this.mCheckBoxListener=myOnCheckChangeListener;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.timeTV=convertView.findViewById(R.id.credit_detail_time);
            viewHolder.goods_nameTV=convertView.findViewById(R.id.credit_detail_goods);
            viewHolder.summaryTV=convertView.findViewById(R.id.credit_detail_summary);
            viewHolder.checkBox=convertView.findViewById(R.id.checkbox);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.checkBox.setTag(position);
       // viewHolder.checkBox.setOnCheckedChangeListener(myOnCheckChangeListener);
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Log.d("creditDialogAdapter",b+":"+isSelected.get(position));
                boolean myCheck=isSelected.get(position)!=b;
                if(b)
                {
                    viewHolder.checkBox.setChecked(true);
                    isSelected.put(position, true);
                }
                else
                {
                    viewHolder.checkBox.setChecked(false);
                    isSelected.put(position, false);
                }
                if(myCheck)mCheckBoxListener.onCheckboxChange(position,compoundButton,b);
            }
        });
        viewHolder.timeTV.setText(creditDetailsList.get(position).getTime());
        viewHolder.goods_nameTV.setText(creditDetailsList.get(position).getGoods_name());
        viewHolder.summaryTV.setText(creditDetailsList.get(position).getSummary());
        if(creditDetailsList.get(position).isGroup()){
            viewHolder.timeTV.setVisibility(View.GONE);
            viewHolder.checkBox.setVisibility(View.GONE);
        }else{
            viewHolder.timeTV.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if(isSelected.get(position))
            {
                viewHolder.checkBox.setChecked(true);
            }else
            {
                viewHolder.checkBox.setChecked(false);
            }

        }
        return convertView;
    }

    public class ViewHolder{
        TextView timeTV;
        TextView goods_nameTV;
        TextView summaryTV;
        CheckBox checkBox;

    }

    public CreditDetail getItem(int i){
        return creditDetailsList.get(i);
    }

    public static abstract class MyOnCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // TODO Auto-generated method stub
            myOnCheckChange((Integer) buttonView.getTag(),buttonView,isChecked);
        }
        public abstract void myOnCheckChange(int position, CompoundButton buttonView,
                                             boolean isChecked);
    }

    public interface checkBoxListener{
        void onCheckboxChange(int position, CompoundButton buttonView,
                              boolean isChecked);
    }

    public void setIsSelected(int size){
        for (int i = 0; i <size ; i++) {
            isSelected.put(i,false);
        }
    }
}
