package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class UnitAdapter extends ArrayAdapter<Unit> implements SpinnerAdapter {
    private List<Unit> unitList;
    private List<Unit> displayUnitList;
    private int resource;
    private Context mContext;
    private UnitFilter mFilter;
    public UnitAdapter(Context context, int resource, @NonNull List<Unit> objects){
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.unitList=objects;
        this.displayUnitList=objects;
    }
    @Override
    public int getCount() {
        return displayUnitList.size();
    }

    @Nullable
    @Override
    public Unit getItem(int position) {
        return displayUnitList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.unit);
            convertView.setTag(viewHolder);
            viewHolder.tvTitle.setText(displayUnitList.get(position).getUnit_name());
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(displayUnitList.get(position).getUnit_name());
      //  Log.d("unitAdapter",displayUnitList.get(position).getUnit_name());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            holder.tvTitle= convertView.findViewById(R.id.unit);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(displayUnitList.get(position).getUnit_name());
        return convertView;
    }

    public int getPosition(int unitId){
        Log.d("modifyGetPosition",displayUnitList.size()+"");
        for(int i=0;i<displayUnitList.size();i++){
           if(displayUnitList.get(i).getUnit_id()==unitId){
                return i;
            }
        }
        return -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NonNull
    @Override
    public UnitFilter getFilter() {
        if(mFilter==null){
            mFilter=new UnitFilter();
        }
        return mFilter;
    }

    class UnitFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if ((charSequence == null || charSequence.length() == 0) ){
                results.values = unitList;
                results.count = unitList.size();
            } else {
                final ArrayList<Unit> newValues=new ArrayList<>();
                String[] units=charSequence.toString().split("and");
                for(int i=0;i<units.length;i++){
                  for(int j=0;j<unitList.size();j++){
                      //Log.d("unit",unitList.get(j).getUnit_id()+"");
                      if(units[i].trim().equals(String.valueOf(unitList.get(j).getUnit_id()))){
                          newValues.add(unitList.get(j));
                      }
                  }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            displayUnitList = (List<Unit>) results.values;
           // Utils.currentUnitList=(List<Unit>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
