package com.example.pc.doh.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pc.doh.Model.Headers;
import com.example.pc.doh.R;

import java.util.List;

public class HeadersAdapter extends RecyclerView.Adapter<HeadersAdapter.ViewHolder>{

    Context context;
    LayoutInflater inflater;
    List<Headers> list;
    private onItemClickListener listener;
    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public void setonItemClickListener(onItemClickListener mlistener){
        listener = mlistener;
    }

    public HeadersAdapter(Context context, List<Headers> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.header_item,viewGroup,false);
        final ViewHolder viewHolder = new HeadersAdapter.ViewHolder(view,listener) ;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.btnheader.setText(list.get(i).getAsmt2l_desc());
        if(list.get(i).getAssesscomplete().equals("true")){

            viewHolder.btnheader.setEnabled(false);
            viewHolder.btnheader.setBackground(ContextCompat.getDrawable(context, R.drawable.assesmentbuttonbg));
            viewHolder.btnheader.setTextColor(Color.WHITE);
            //layout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ready))

        }
        //viewHolder.btnheader.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return list.size() ;
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder{
        Button btnheader;

        public ViewHolder(@NonNull View itemView,final onItemClickListener listener) {
            super(itemView);
            btnheader = itemView.findViewById(R.id.btnheaders);
            btnheader.setOnClickListener(new View.OnClickListener() {
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


        /*
        *  public MyViewHolder(View itemView, final onItemClickListener listener) {
            super(itemView);
            view_container = itemView.findViewById(R.id.assestmentcontainer);
            code = itemView.findViewById(R.id.txtassesscode);
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
        * */
    }
}
