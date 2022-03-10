package com.example.pc.doh.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.pc.doh.Model.AssestmentModel;
import com.example.pc.doh.Model.Monitoring;
import com.example.pc.doh.R;

import java.util.ArrayList;
import java.util.List;

public class MonitoringAdapter extends RecyclerView.Adapter<MonitoringAdapter.viewHolder> implements Filterable {

    private Context context;
    private List<Monitoring> list;
    private onItemClickListener listener;
    private List<Monitoring> fullList;

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public void setonItemClickListener(onItemClickListener mlistener){
        listener = mlistener;
    }
    //private AssesstmentAdapterListener listener;

    public MonitoringAdapter(Context context, List<Monitoring> list) {
        this.context = context;
        this.list = list;
        fullList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        view = inflater.inflate(R.layout.monitoring_item_row,viewGroup,false);
        viewHolder holder = new viewHolder(view,listener);
        //final MyViewHolder viewHolder = new MyViewHolder(view,listener) ;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder viewHolder, int i) {
        viewHolder.code.setText(list.get(i).getCode());
        viewHolder.date.setText(list.get(i).getDate());
        viewHolder.name.setText(list.get(i).getFaclityname());
        viewHolder.currentstatus.setText(list.get(i).getStatus());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        TextView code,date,currentstatus,name;
        public viewHolder(@NonNull View itemView,final onItemClickListener listener) {
            super(itemView);
             code = itemView.findViewById(R.id.txtmonitorcode);
             name = itemView.findViewById(R.id.txtmonitorname);
             date = itemView.findViewById(R.id.txtmonitordate);
             currentstatus = itemView.findViewById(R.id.txtmonitortatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    @Override
    public Filter getFilter() {
        return filteritem;
    }

    private Filter filteritem = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Monitoring> filterlist = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                Log.d("fullLists",fullList.size()+"");
                filterlist.addAll(fullList);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(Monitoring item: fullList){
                    if(item.getFaclityname().toLowerCase().contains(filterPattern) || item.getAppid().toLowerCase().contains(filterPattern)){
                        filterlist.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
