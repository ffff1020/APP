package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class GoodsAdapter extends ArrayAdapter<Goods> {
    private Context mContext;
    private MyFilter mFilter;
    private int resource;
    private List<Goods> mList;
    private List<Goods> mDisplayList;
    public GoodsAdapter(@NonNull Context context, int resource, @NonNull List<Goods>objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.mList=objects;
        this.mDisplayList=objects;
    }

    @Override
    public int getCount() {
        return mDisplayList.size();
    }

    @Nullable
    @Override
    public Goods getItem(int position) {
        return mDisplayList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
           // return convertView;
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(mDisplayList.get(position).getGoods_name());
        return convertView;
    }
    //返回过滤器
    public MyFilter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }
    class MyFilter extends Filter {
        private  String filterType;
        public void setMyFilter(String mFilterType){
            this.filterType=mFilterType;
        }
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if ((prefix == null || prefix.length() == 0) ){
                results.values = mList;
                results.count = mList.size();
            } else {
                final ArrayList<Goods> newValues = new ArrayList<Goods>();
                switch(filterType){
                    case Utils.GOODSFILTERCATEGORYID:
                        int category_id =Integer.parseInt(prefix.toString());
                        for (int i = 0; i < mList.size(); i++) {
                            final Goods value = mList.get(i);
                            if (value.getCategory_id() == category_id) {
                                newValues.add(value);
                            }
                        }
                        break;
                    case Utils.GOODSFILTERSEARCHVIEW:
                        for (int i = 0; i < mList.size(); i++) {
                            final Goods value = mList.get(i);
                            if (value.getGoods_sn().toLowerCase().contains(prefix.toString().toLowerCase())||value.getGoods_name().contains(prefix) ) {
                                newValues.add(value);
                            }
                        }
                        break;
                    case Utils.GOODSFILTERPROMOTE:
                        for (int i = 0; i < mList.size(); i++) {
                            final Goods value = mList.get(i);
                            if (value.getPromote()==1 ) {
                                newValues.add(value);
                            }
                        }
                        break;
                        default:
                            break;
                }
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mDisplayList = (List<Goods>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
