package com.example.pc.doh.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.pc.doh.Model.showassessitem;
import com.example.pc.doh.R;

import java.util.List;

public class assessdetadapter extends RecyclerView.Adapter<assessdetadapter.viewholder>{

    private Context context;
    private List<showassessitem> list;
    private onTouchListener listener;
    private OnClickRadioListener onClickRadioListener;
    private OnRemarksTextChangeListener onRemarksTextChangeListener;

    public interface onTouchListener{
        void onTouch(int position);
    }

    public interface OnClickRadioListener {
        void onClickRadio(int position);
    }

    public interface OnRemarksTextChangeListener {
        void onRemarksTextChange(int position);
    }

    public void addItemTouchListener(onTouchListener mlistener){
        listener = mlistener;
    }

    public void addClickRadioListener(OnClickRadioListener listener) {
        onClickRadioListener = listener;
    }

    public void addRemarksTextChangeListener(OnRemarksTextChangeListener listener) {
        onRemarksTextChangeListener = listener;
    }

    public assessdetadapter(Context context, List<showassessitem> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<showassessitem> list) {
        this.list = list;
    }

    public static class viewholder extends RecyclerView.ViewHolder{
        LinearLayout layoutradioGrp,answer;
        EditText remarks;
        TextView details,lblremarks;
        RadioGroup rgchoice;
        ToggleButton btnremarks;

        private showassessitem saitem;
        public viewholder(@NonNull View itemView,final onTouchListener listener) {
            super(itemView);

            layoutradioGrp = itemView.findViewById(R.id.layoutrgchoice);
            remarks = itemView.findViewById(R.id.remark);
            details = itemView.findViewById(R.id.txtassessdet);
            rgchoice = itemView.findViewById(R.id.rgchoice);
            answer = itemView.findViewById(R.id.answer);
            btnremarks = itemView.findViewById(R.id.btnremarks);
            lblremarks = itemView.findViewById(R.id.lblremarks);

            btnremarks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        remarks.setVisibility(View.VISIBLE);
                        if(!remarks.getText().toString().equals("")){
                            lblremarks.setVisibility(View.VISIBLE);
                        }else{
                            lblremarks.setVisibility(View.GONE);
                        }
                    }else{
                        remarks.setVisibility(View.GONE);
                        if(!remarks.getText().toString().equals("")){
                            lblremarks.setVisibility(View.VISIBLE);
                        }else{
                            lblremarks.setVisibility(View.GONE);
                        }
                    }
                }
            });

            /*
            btnremarks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(chkremarks.equals("N")){
                        remarks.setVisibility(View.VISIBLE);
                    }else{
                        remarks.setVisibility(View.GONE);
                    }

                }
            });*/

            remarks.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    return false;
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){

                            listener.onTouch(position);
                        }
                    }
                    return false;
                }
            });
        }

        public void bindDataWithViewHolder(showassessitem dataItem,Context context,int pos){
            this.saitem=dataItem;
            layoutradioGrp.setTag(pos);
            answer.setTag(pos);
        }


    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.assessdet,viewGroup,false);
        // final MyViewHolder viewHolder = new MyViewHolder(view,listener) ;
        //view.setTag(i);
        //final LinearLayout layoutradioGrp = view.findViewById(R.id.layoutrgchoice);
//        RadioGroup radioGroup = view.findViewById(R.id.rgchoice);
//        for(int j=0;j<3;j++){
//            RadioButton radioButton = new RadioButton(context);
//            radioButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            radioButton.setButtonDrawable(R.drawable.custom_radio);
//            if(j==0){
//                radioButton.setText("YES");
//            }else if(j==1){
//                radioButton.setText("NO");
//            }else if(j==2){
//                radioButton.setText("N/A");
//            }
//            String id = i+""+j;
//            Log.d("id",id);
//            radioButton.setId(j);
//            radioGroup.addView(radioButton);
//
//        }

        final viewholder holder = new viewholder(view,listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewholder viewholder, int i) {
        int id = (i+1)*100;
        final RadioGroup rg = viewholder.rgchoice;
        EditText editTextRemarks = viewholder.remarks;

        editTextRemarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int pos = viewholder.getAdapterPosition();
                String remarks = editable.toString();

                if (!remarks.equals(list.get(pos).getRemarks())) {
                    list.get(pos).setRemarks(remarks);
                    if (onRemarksTextChangeListener != null) {
                        onRemarksTextChangeListener.onRemarksTextChange(pos);
                    }
                }
            }
        });

        String remarks = list.get(i).getRemarks();
        if(remarks.equals("")){
            viewholder.remarks.setText("");
            viewholder.lblremarks.setVisibility(View.GONE);
        }else{
            viewholder.remarks.setText(remarks);
            viewholder.lblremarks.setVisibility(View.VISIBLE);
        }

        for (int c = 0; c < rg.getChildCount(); c++) {
            View child = rg.getChildAt(c);

            if (child instanceof RadioButton && !child.hasOnClickListeners()) {
                if (((RadioButton) child).getText().toString().equals("SKIP")) {
                    rg.getChildAt(c).setVisibility(View.GONE);
                } else {
                    rg.getChildAt(c).setOnClickListener(new View.OnClickListener() {
                        public void setChoice(String choice) {
                            int pos = viewholder.getAdapterPosition();

                            if (list.get(pos).getChoice().equals(choice)) {
                                list.get(pos).setChoice("-1");
                                rg.clearCheck();
                            } else {
                                list.get(pos).setChoice(choice);
                            }
                        }

                        @Override
                        public void onClick(View view) {
                            String selected = ((RadioButton) view).getText().toString();
                            int pos = viewholder.getAdapterPosition();

                            switch (selected) {
                                case "YES":
                                    setChoice("1");
                                    break;
                                case "NO":
                                    setChoice("0");
                                    break;
                                case "N/A":
                                    setChoice("NA");
                                    break;
                                case "SKIP":
                                    setChoice("SKIP");
                                    break;
                            }

                            if (onClickRadioListener != null) {
                                onClickRadioListener.onClickRadio(pos);
                            }
                        }
                    });
                }
            }
        }

        int checkedRadioId = rg.getCheckedRadioButtonId();
        RadioButton checkedRadio = rg.findViewById(checkedRadioId);
        String choice = list.get(i).getChoice();

        if(choice.equals("1")){
            rg.check(R.id.yes);
        }else if(choice.equals("0")){
            rg.check(R.id.no);
        }else if(choice.equals("NA")){
            rg.check(R.id.na);
        }else if(choice.equals("SKIP")){
            rg.check(R.id.skip);
        } else {
            if (checkedRadio != null) {
                rg.clearCheck();
            }
        }

        if(list.get(i).getOtherheading() != null) {
            viewholder.details.setText(Html.fromHtml(list.get(i).getOtherheading()+list.get(i).getDisp()));
        } else{
            viewholder.details.setText(Html.fromHtml(list.get(i).getDisp()));
        }

        viewholder.remarks.setTag(i);
        viewholder.bindDataWithViewHolder(list.get(i),context,i);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }








}
