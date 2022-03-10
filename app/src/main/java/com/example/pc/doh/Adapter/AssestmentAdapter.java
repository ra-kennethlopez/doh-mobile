package com.example.pc.doh.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.Activity.AssestmentDetailsActivity;
import com.example.pc.doh.Model.AssestmentModel;
import com.example.pc.doh.R;

import java.util.ArrayList;
import java.util.List;

public class AssestmentAdapter extends RecyclerView.Adapter<AssestmentAdapter.MyViewHolder> implements Filterable{

    private Context context;
    private List<AssestmentModel> list;
    private List<AssestmentModel> fullList;
    private onItemClickListener listener;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView code,date,currentstatus,name;
        LinearLayout view_container;


        public MyViewHolder(View itemView, final onItemClickListener listener) {
            super(itemView);
            view_container = itemView.findViewById(R.id.assestmentcontainer);
            code = itemView.findViewById(R.id.txtassesscode);
            name = itemView.findViewById(R.id.txtname);
            date = itemView.findViewById(R.id.txtassessdate);
            currentstatus = itemView.findViewById(R.id.txtassessstatus);
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

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public void setonItemClickListener(onItemClickListener mlistener){
        listener = mlistener;
    }
    //private AssesstmentAdapterListener listener;

    public AssestmentAdapter(Context context,List<AssestmentModel> list){
        this.context = context;
        this.list = list;
        Log.d("list",list.size()+"");
        this.fullList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.assestment_item_row,viewGroup,false);

        final MyViewHolder viewHolder = new MyViewHolder(view,listener) ;

       /* viewHolder.view_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, AssestmentDetailsActivity.class);
                context.startActivity(i);
                Animatoo.animateSlideLeft(context);


            }
        });*/

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.code.setText(list.get(i).getCode());
        myViewHolder.date.setText(list.get(i).getDate());
        myViewHolder.name.setText(list.get(i).getFaclityname());
        myViewHolder.currentstatus.setText(list.get(i).getStatus());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }







    @Override
    public Filter getFilter() {
        return filteritem;
    }

    private Filter filteritem = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AssestmentModel> filterlist = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                Log.d("fullLists",fullList.size()+"");
                filterlist.addAll(fullList);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(AssestmentModel item: fullList){
                      if(item.getCode().toLowerCase().contains(filterPattern) || item.getAppid().toLowerCase().contains(filterPattern)){
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
