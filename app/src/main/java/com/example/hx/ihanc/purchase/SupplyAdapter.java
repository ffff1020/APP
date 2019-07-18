package com.example.hx.ihanc.purchase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.hx.ihanc.R;
import com.example.hx.ihanc.ViewHolder;
import java.util.ArrayList;
import java.util.List;

public class SupplyAdapter extends ArrayAdapter<Supply> implements Filterable {
    private Context mContext;
    private List<Supply> SupplyList;
    private List<Supply> displaySupplyList;
    private int resource;
    private SupplyFilter mFilter;
    public SupplyAdapter(Context context, int resource, @NonNull List<Supply> SupplyList){
        super(context, resource, SupplyList);
        this.mContext=context;
        this.SupplyList=SupplyList;
        this.resource=resource;
        this.displaySupplyList=SupplyList;
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
            //return convertView;
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(displaySupplyList.get(position).supply_name);
        return convertView;
    }

    @Override
    public int getCount() {
        return displaySupplyList.size();
    }

    @Nullable
    @Override
    public Supply getItem(int position) {
        return displaySupplyList.get(position);
    }
    public SupplyFilter getFilter(){
        if (mFilter == null) {
            mFilter = new SupplyAdapter.SupplyFilter();
        }
        return mFilter;
    }

    class SupplyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if ((prefix == null || prefix.length() == 0) ){
                results.values = SupplyList;
                results.count = SupplyList.size();
            } else {
                final ArrayList<Supply> newValues = new ArrayList<Supply>();
                int k=0;
                for (int i = 0; i < SupplyList.size() && k<10; i++) {
                    final Supply value = SupplyList.get(i);
                    if (value.supply_sn.toLowerCase().contains(prefix.toString().toLowerCase())||value.supply_name.contains(prefix) ) {
                        newValues.add(value);
                        k++;
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            // Log.d("SupplyAdapter",results.count+"");
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            displaySupplyList = (List<Supply>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}

    