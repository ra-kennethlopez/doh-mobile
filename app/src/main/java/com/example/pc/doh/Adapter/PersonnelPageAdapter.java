package com.example.pc.doh.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.doh.Model.PersonnelPage;
import com.example.pc.doh.Model.Srvasmtcols;
import com.example.pc.doh.R;

import java.util.ArrayList;
import java.util.List;

public class PersonnelPageAdapter extends PagerAdapter {

    Context context;
    ArrayList<PersonnelPage> list = new ArrayList<>();
    LayoutInflater inflater;
    EditText txtremarks;
    String remarks;
    int pos;
    public PersonnelPageAdapter(Context context, ArrayList<PersonnelPage> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.personnelpage,container,false);
        TextView disp1 = view.findViewById(R.id.txtdisp1);
        TextView disp2 = view.findViewById(R.id.txtdisp2);
        TextView disp3 = view.findViewById(R.id.txtdisp3);
        TextView disp4 = view.findViewById(R.id.txtdisp4);
        TextView disp5 = view.findViewById(R.id.lbldisp5);
        txtremarks = view.findViewById(R.id.remark);
        final LinearLayout layoutradioGrp = view.findViewById(R.id.layoutrgchoice);

        List<Srvasmtcols> l = list.get(position).getSrvasmtcolsList();
        for(int i=0;i<l.size();i++){
            if(l.get(i).getType().equals("Boolean")){
                TextView name = new TextView(context);
                name.setText(l.get(i).getDesc());
                name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                name.setPadding(7,7,7,7);
                RadioGroup radioGroup = new RadioGroup(context);
                radioGroup.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                radioGroup.setOrientation(LinearLayout.HORIZONTAL);
                radioGroup.setPadding(5,5,5,5);
                radioGroup.setId(i+position+100);
                for(int j=0;j<2;j++){
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    radioButton.setButtonDrawable(R.drawable.custom_radio);
                    if(j==0){
                        radioButton.setText("YES");
                        if(l.get(i).getAnswer().equals("true")){
                            radioButton.setChecked(true);
                        }
                    }else{
                        radioButton.setText("NO");
                        if(l.get(i).getAnswer().equals("false")){
                            radioButton.setChecked(true);
                        }
                    }

                    radioButton.setId(j);
                    radioGroup.addView(radioButton);

                }
                layoutradioGrp.addView(name);
                layoutradioGrp.addView(radioGroup);
            }else if (l.get(i).getType().equals("Text")){
                disp5.setText(l.get(i).getDesc());
            }

        }

        disp1.setText(list.get(position).getDisp1());
        disp2.setText(list.get(position).getDisp2());
        disp3.setText(list.get(position).getDisp3());
        disp4.setText(Html.fromHtml(list.get(position).getDisp4()));
        txtremarks.setText(list.get(position).getRemarks());
        txtremarks.setOnTouchListener(new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   v.setFocusable(true);
                   v.setFocusableInTouchMode(true);
                   return false;
               }
        });



        view.setTag("remarks"+position);
        container.addView(view);
        return view;
    }



    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }








}
