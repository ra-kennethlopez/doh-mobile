package com.example.pc.doh.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pc.doh.Model.PersonnelPage;
import com.example.pc.doh.R;

import java.util.ArrayList;

public class AssessQuestionAdapter extends RecyclerView.Adapter<AssessQuestionAdapter.itemholder>{


    private Context context;
    ArrayList<PersonnelPage> list;

    public AssessQuestionAdapter(Context context, ArrayList<PersonnelPage> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public itemholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.personnelpage,viewGroup,false);

        final AssessQuestionAdapter.itemholder viewHolder = new AssessQuestionAdapter.itemholder(view) ;

       /* viewHolder.view_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, AssestmentDetailsActivity.class);
                context.startActivity(i);
                Animatoo.animateSlideLeft(context);


            }
        });*/
        return viewHolder;
        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull itemholder itemholder, int i) {
       itemholder.disp1.setText(list.get(i).getDisp1());
       itemholder.disp2.setText(list.get(i).getDisp2());
       itemholder.disp3.setText(list.get(i).getDisp3());
       itemholder.disp4.setText(list.get(i).getDisp4());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class itemholder extends RecyclerView.ViewHolder{
        TextView disp1;
        TextView disp2;
        TextView disp3;
        TextView disp4;
        TextView disp5;
        EditText txtremarks;
        public itemholder(@NonNull View itemView) {
            super(itemView);

         disp1 = itemView.findViewById(R.id.txtdisp1);
         disp2 = itemView.findViewById(R.id.txtdisp2);
         disp3 = itemView.findViewById(R.id.txtdisp3);
         disp4 = itemView.findViewById(R.id.txtdisp4);
         disp5 = itemView.findViewById(R.id.lbldisp5);
         txtremarks = itemView.findViewById(R.id.remark);
        }
    }
}
