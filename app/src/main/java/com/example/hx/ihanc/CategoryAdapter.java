package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<category> {
    private Context mContext;
    private List<category> categoryList;
    private int resource;
    private LongClickListener mListener=null;
    public CategoryAdapter(@NonNull Context context, int resource, @NonNull List<category> categoryList) {
        super(context, resource, categoryList);
        this.mContext=context;
        this.categoryList=categoryList;
        this.resource=resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
            //return convertView;
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(categoryList.get(position).getCategory_name());
        if(mListener!=null){
            viewHolder.tvTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.OnLongClick(categoryList.get(position));
                    return false;
                }
            });
            viewHolder.tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnClick(categoryList.get(position),view);
                }
            });

        }
      //  Log.d("getViewCategoryAdapter",categoryList.size()+"");
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            holder.tvTitle= convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.tvTitle.setText(categoryList.get(position).getCategory_name());
        return convertView;
    }

    public void setListener(LongClickListener listener){
        this.mListener=listener;
    }

    public interface LongClickListener{
         void OnLongClick(category item);
         void OnClick(category item,View view);
    }
}
