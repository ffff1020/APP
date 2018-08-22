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
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends ArrayAdapter<member> implements Filterable {
    private Context mContext;
    private List<member> memberList;
    private List<member> displayMemberList;
    private int resource;
    private MemberFilter mFilter;
    public MemberAdapter(Context context, int resource, @NonNull List<member> memberList){
        super(context, resource, memberList);
        this.mContext=context;
        this.memberList=memberList;
        this.resource=resource;
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
        viewHolder.tvTitle.setText(displayMemberList.get(position).getMember_name());
        Log.d("getViewMemberAdapter",memberList.size()+"");
        return convertView;
    }

    @Override
    public int getCount() {
        return displayMemberList.size();
    }

    @Nullable
    @Override
    public member getItem(int position) {
        return displayMemberList.get(position);
    }
    public MemberFilter getFilter(){
        if (mFilter == null) {
            mFilter = new MemberFilter();
        }
        return mFilter;
    }

    class MemberFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if ((prefix == null || prefix.length() == 0) ){
                results.values = memberList;
                results.count = memberList.size();
            } else {
                final ArrayList<member> newValues = new ArrayList<member>();
                int k=0;
                        for (int i = 0; i < memberList.size() && k<10; i++) {
                            final member value = memberList.get(i);
                            if (value.getMember_sn().contains(prefix)||value.getMember_name().contains(prefix) ) {
                                newValues.add(value);
                                k++;
                            }
                        }
                results.values = newValues;
                results.count = newValues.size();
            }
            Log.d("memberAdapter",results.count+"");
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            displayMemberList = (List<member>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
