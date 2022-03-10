package com.example.pc.doh.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.Model.conansitem;
import com.example.pc.doh.Model.conitem;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class compareactivity extends AppCompatActivity implements View.OnClickListener {
    DatabaseHelper db;
    private String uid,uname;
    LinearLayout lcomp;
    Button btnsubmit;
    List<conitem> citem = new ArrayList<>();
    List<conansitem> sitem = new ArrayList<>();
    String mergemodule = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare);
        Bundle b = this.getIntent().getExtras();
        mergemodule = b.getString("merge");
        db = new DatabaseHelper(this);

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        btnsubmit = findViewById(R.id.btnsubmit);
        lcomp = findViewById(R.id.lcomp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(compareactivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(compareactivity.this);
                db.delete("assesscombinedtemp","","");
            }
        });
        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        uname = user.getName();
        getSupportActionBar().setTitle("Conflict Merge Data");

        displayconflict();
        //generate();
        btnsubmit.setOnClickListener(this);
    }
    private void insertansitem(int i){
        sitem.add(new conansitem(i+"",
                citem.get(i).getAsmtComb_FK(),
                citem.get(i).getAssessmentName(),
                citem.get(i).getAssessmentSeq(),
                citem.get(i).getAssessmentHead(),
                citem.get(i).getAsmtH3ID_FK(),
                citem.get(i).getH3name(),
                citem.get(i).getAsmtH2ID_FK(),
                citem.get(i).getH2name(),
                citem.get(i).getAsmtH1ID_FK(),
                citem.get(i).getH1name(),
                citem.get(i).getPartid(),
                citem.get(i).getEvaluation(),
                citem.get(i).getRemarks(),
                citem.get(i).getEvaluatedBy(),
                citem.get(i).getEname(),
                citem.get(i).getAppid(),
                citem.get(i).getMonid()
        ));
    }

    private void generate(){
        class gen extends AsyncTask<Void, Void, String> {
            ProgressDialog pdialog;

            @Override
            protected String doInBackground(Void... voids) {
                displayconflict();
            return "sample";
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pdialog = ProgressDialog.show(compareactivity.this, "DOHOLRS",
                        "Display Conflict Results. Please wait...", true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion

            }
        }

        //executing the async task
        gen c = new gen();
        c.execute();
    }
    private void displayconflict(){
        ProgressDialog pdialog;
        pdialog = ProgressDialog.show(compareactivity.this, "DOHOLRS",
                "Display Conflict Results. Please wait...", true);


        int i = 0;
        LinearLayout.LayoutParams firstparams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams secondparams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1.5f);
        Cursor c = db.get_tbl_assesscombinedtempid();
        final Cursor ci = db.get_tbl_assesscombinedtemp();
        if(ci!=null){
            ci.moveToFirst();
            while (!ci.isAfterLast()){
                citem.add(new conitem(ci.getString(ci.getColumnIndex("asmtComb_FK")),
                        ci.getString(ci.getColumnIndex("assessmentName")),
                        ci.getString(ci.getColumnIndex("assessmentSeq")),
                        ci.getString(ci.getColumnIndex("assessmentHead")),
                        ci.getString(ci.getColumnIndex("asmtH3ID_FK")),
                        ci.getString(ci.getColumnIndex("h3name")),
                        ci.getString(ci.getColumnIndex("asmtH2ID_FK")),
                        ci.getString(ci.getColumnIndex("h2name")),
                        ci.getString(ci.getColumnIndex("asmtH1ID_FK")),
                        ci.getString(ci.getColumnIndex("h1name")),
                        ci.getString(ci.getColumnIndex("partID")),
                        ci.getString(ci.getColumnIndex("evaluation")),
                        ci.getString(ci.getColumnIndex("remarks")),
                        ci.getString(ci.getColumnIndex("evaluatedBy")),
                        ci.getString(ci.getColumnIndex("ename")),
                        ci.getString(ci.getColumnIndex("appid")),
                        ci.getString(ci.getColumnIndex("monid"))));
                ci.moveToNext();
            }
        }


        final String asmtComb_FK = "";
        String choices="";
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                //Conflict Questions
                TextView firstheadername = new TextView(compareactivity.this);
                firstheadername.setTypeface(null, Typeface.BOLD);
                firstheadername.setTextSize(18);
                firstheadername.setText(c.getString(c.getColumnIndex("h3name"))+" > "+
                                        c.getString(c.getColumnIndex("h1name"))+"\n\n");
                firstheadername.setLayoutParams(firstparams);
                firstheadername.setPadding(5,5,5,5);

                TextView firstheader = new TextView(compareactivity.this);
                firstheader.setTypeface(null, Typeface.BOLD);
                firstheader.setTextSize(12);
                firstheader.setText(Html.fromHtml(c.getString(c.getColumnIndex("assessmentHead"))
                        +c.getString(c.getColumnIndex("assessmentName"))));
                firstheader.setLayoutParams(firstparams);
                firstheader.setPadding(5,5,5,5);

                TextView secondheader = new TextView(compareactivity.this);
                secondheader.setTypeface(null, Typeface.BOLD);
                secondheader.setTextSize(15);
                secondheader.setText("Choose Answer");
                secondheader.setLayoutParams(firstparams);
                secondheader.setPadding(5,5,5,5);
                Button newanswer = new Button(compareactivity.this);
                //c.getString(c.getColumnIndex("asmtComb_FK")))
                newanswer.setId(Integer.parseInt(c.getString(c.getColumnIndex("asmtComb_FK"))));
                newanswer.setLayoutParams(firstparams);
                newanswer.setPadding(5,5,5,5);
                newanswer.setTextColor(Color.WHITE);
                newanswer.setBackgroundColor(Color.parseColor("#28A55F"));
                newanswer.setText("Create New Answer");

                lcomp.addView(firstheadername);
                lcomp.addView(firstheader);
                lcomp.addView(secondheader);
                //end of Conflict Questions

                Cursor a = db.get_tbl_assesscombinedtempdata(c.getString(c.getColumnIndex("asmtComb_FK")));
                if(a!=null && a.getCount()>0){
                    a.moveToFirst();
                    while (!a.isAfterLast()){
                        //answer
                        //linear1
                        LinearLayout l1 = new LinearLayout(compareactivity.this);
                        l1.setLayoutParams(firstparams);
                        l1.setOrientation(LinearLayout.VERTICAL);
                        //cardview
                        final CardView card = new CardView(compareactivity.this);
                        firstparams.setMargins(5,5,5,5);
                        card.setLayoutParams(firstparams);
                        card.setPadding(5,5,5,5);
                        card.setId(i++);

                        //linear2
                        LinearLayout l2 = new LinearLayout(compareactivity.this);
                        l2.setLayoutParams(firstparams);
                        l2.setOrientation(LinearLayout.HORIZONTAL);
                        //linear3
                        LinearLayout l3 = new LinearLayout(compareactivity.this);
                        l3.setLayoutParams(secondparams);
                        l3.setOrientation(LinearLayout.VERTICAL);

                        //name
                        TextView name = new TextView(compareactivity.this);
                        name.setLayoutParams(firstparams);
                        name.setTextSize(20);
                        name.setText(a.getString(a.getColumnIndex("ename")));
                        l3.addView(name);

                        //choice
                        TextView choice = new TextView(compareactivity.this);
                        choice.setLayoutParams(firstparams);
                        choice.setTextSize(20);
                        switch (a.getString(a.getColumnIndex("evaluation"))){
                            case "1": choices = "Yes";break;
                            case "0": choices = "No"; break;
                            default: choices = "NA"; break;
                        }
                        choice.setText("Choice: "+choices);
                        l3.addView(choice);

                        //remarks
                        TextView remarks = new TextView(compareactivity.this);
                        remarks.setLayoutParams(firstparams);
                        remarks.setTextSize(20);
                        remarks.setText("Remarks: "+a.getString(a.getColumnIndex("remarks")));
                        l3.addView(remarks);
                        //end of linear3

                        //add on click
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int i = view.getId();



                                Log.d("cid",i+"");
                                if(sitem.size()!=0){
                                    for(int s=0;s<sitem.size();s++){
                                        Log.d("sid",sitem.get(s).getId());
                                        if(!sitem.get(s).getId().equals(i+"") &&
                                                sitem.get(s).getAsmtComb_FK().equals(citem.get(i).getAsmtComb_FK())){
                                            Log.d("found","true");
                                            Log.d("sidfound",sitem.get(s).getId());
                                            CardView card2 = findViewById(Integer.valueOf(sitem.get(s).getId()));
                                            card2.setBackgroundColor(Color.WHITE);
                                        }

                                    }
                                    //if exist remove insert else insert it in the list
                                    boolean check = false;
                                    int index = 0;
                                    for(int s=0;s<sitem.size();s++){
                                        if(sitem.get(s).getAsmtComb_FK().equals(citem.get(i).getAsmtComb_FK())){
                                            Log.d("message","removeinsert");
                                            sitem.remove(index);
                                            insertansitem(i);
                                            check = true;
                                            card.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cardborder));
                                            break;
                                        }
                                    }
                                    if(!check){
                                        Log.d("message","insert");
                                        insertansitem(i);
                                        card.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cardborder));
                                    }
                                }else{
                                    Log.d("message","insert");
                                    insertansitem(i);
                                    card.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cardborder));
                                }


                            }
                        });
                        l2.addView(l3);
                        //end of linear2
                        card.addView(l2);



                        l1.addView(card);
                        //end of cardview

                        //end of linear1
                        //end of answers
                        lcomp.addView(l1);
                        a.moveToNext();
                    }
                }
                //asmtComb_FK,assessmentHead,assessmentName,appid,asmtH1ID_FK
                final String appid = c.getString(c.getColumnIndex("appid"));
                final String asmtH1ID_FK = c.getString(c.getColumnIndex("asmtH1ID_FK"));
                final String asmtComb_FKs = c.getString(c.getColumnIndex("asmtComb_FK"));
                final String assessmentHead = c.getString(c.getColumnIndex("assessmentHead"));
                final String assessmentName = c.getString(c.getColumnIndex("assessmentName"));
                final String monid = c.getString(c.getColumnIndex("monid"));
                newanswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       //continue here
                        AlertDialog.Builder builder = new AlertDialog.Builder(compareactivity.this);
                        //create design
                        LinearLayout.LayoutParams alertparams = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams editextparams = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.MATCH_PARENT, 200);
                        LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        btnparams.weight = 1.0f;
                        btnparams.setMargins(5,5,5,5);
                        //create linearlayout
                        LinearLayout alertlayout = new LinearLayout(compareactivity.this);
                        alertlayout.setOrientation(LinearLayout.VERTICAL);
                        alertlayout.setLayoutParams(alertparams);
                        alertlayout.setPadding(10,10,10,10);
                        alertlayout.setBackgroundColor(Color.parseColor("#272b30"));
                        //
                        //create textview
                        TextView header = new TextView(compareactivity.this);
                        header.setLayoutParams(alertparams);
                        header.setTextColor(Color.WHITE);
                        header.setTextSize(20);
                        header.setTypeface(null,Typeface.BOLD);
                        header.setGravity(Gravity.CENTER);
                        header.setText("Answer:");
                        TextView complied = new TextView(compareactivity.this);
                        complied.setLayoutParams(alertparams);
                        complied.setTextColor(Color.WHITE);
                        complied.setTextSize(18);
                        complied.setTypeface(null,Typeface.BOLD);
                        complied.setText("Complied:");
                        //create radio button
                        int yesid = 1000;
                        int noid = 2000;
                        int naid = 3000;

                        ColorStateList colorStateList = new ColorStateList(
                                new int[][]{

                                        new int[]{-android.R.attr.state_enabled}, //disabled
                                        new int[]{android.R.attr.state_enabled} //enabled
                                },
                                new int[] {

                                        Color.BLACK //disabled
                                        ,Color.WHITE //enabled

                                }
                        );

                        final RadioGroup rgchoice = new RadioGroup(compareactivity.this);
                        rgchoice.setId(0);
                        rgchoice.setLayoutParams(alertparams);
                        rgchoice.setOrientation(LinearLayout.VERTICAL);
                        RadioButton ryes = new RadioButton(compareactivity.this);
                        ryes.setText("Yes");
                        ryes.setId(yesid);
                        ryes.setTextColor(Color.WHITE);
                        ryes.setButtonTintList(colorStateList);
                        RadioButton rno = new RadioButton(compareactivity.this);
                        rno.setText("No");
                        rno.setId(noid);
                        rno.setTextColor(Color.WHITE);
                        rno.setButtonTintList(colorStateList);
                        RadioButton rna = new RadioButton(compareactivity.this);
                        rna.setText("NA");
                        rna.setTextColor(Color.WHITE);
                        rna.setId(naid);
                        rna.setButtonTintList(colorStateList);
                        rgchoice.addView(ryes);
                        rgchoice.addView(rno);
                        if(!mergemodule.equals("evaluate")){
                            rgchoice.addView(rna);
                        }
                        int id = rgchoice.getCheckedRadioButtonId();


                        TextView remarks = new TextView(compareactivity.this);
                        remarks.setLayoutParams(alertparams);
                        remarks.setTextColor(Color.WHITE);
                        remarks.setTextSize(18);
                        remarks.setTypeface(null,Typeface.BOLD);
                        remarks.setText("Remarks:");
                        //create editext
                        final EditText edtremark = new EditText(compareactivity.this);
                        editextparams.setMargins(10,10,10,10);
                        edtremark.setLayoutParams(editextparams);
                        edtremark.setBackground(ContextCompat.getDrawable(compareactivity.this,R.drawable.edittextborder));
                        edtremark.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        edtremark.setGravity(Gravity.TOP);
                        edtremark.setTextSize(12);
                        edtremark.setPadding(10,10,10,10);
                        //edtremark.setText("sample");
                        //create button layout
                        LinearLayout btnlayout = new LinearLayout(compareactivity.this);
                        btnlayout.setOrientation(LinearLayout.HORIZONTAL);
                        btnlayout.setLayoutParams(alertparams);
                        btnlayout.setPadding(10,10,10,10);
                        btnlayout.setBackgroundColor(Color.parseColor("#272b30"));
                        //create button
                        Button btnyes = new Button(compareactivity.this);
                        btnyes.setText("SAVE");
                        btnyes.setLayoutParams(btnparams);
                        btnyes.setTextColor(Color.WHITE);
                        btnyes.setBackgroundColor(Color.parseColor("#007bff"));
                        Button btnno = new Button(compareactivity.this);
                        btnno.setText("CANCEL");
                        btnno.setLayoutParams(btnparams);
                        btnno.setTextColor(Color.WHITE);
                        btnno.setBackgroundColor(Color.parseColor("#dc3545"));
                        //
                        btnlayout.addView(btnyes);
                        btnlayout.addView(btnno);
                        //end of button layout
                        alertlayout.addView(header);
                        alertlayout.addView(complied);
                        alertlayout.addView(rgchoice);
                        alertlayout.addView(remarks);
                        alertlayout.addView(edtremark);
                        alertlayout.addView(btnlayout);
                        //check if this alertdialog already answer
                        if(sitem.size()>0){
                            for(int i=0;i<sitem.size();i++){
                                if(sitem.get(i).getId().equals(asmtComb_FKs)){
                                    Log.d("messageshow","true");
                                    String choice = sitem.get(i).getEvaluation();
                                    if(choice == "1"){
                                        ryes.setChecked(true);
                                    }else if(choice == "0"){
                                        rno.setChecked(true);
                                    }else{
                                        rna.setChecked(true);
                                    }
                                    edtremark.setText(sitem.get(i).getRemarks());
                                    break;
                                }
                            }
                        }
                        builder.setView(alertlayout);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        btnyes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //final String evaluate = evaluation;
                                Log.d("choice",rgchoice.getCheckedRadioButtonId()+"");
                                int choiceid = rgchoice.getCheckedRadioButtonId();
                                String evaluate;
                                if(choiceid == 1000){
                                    evaluate = "1";
                                }else if(choiceid == 2000){
                                    evaluate = "0";
                                }else{
                                    evaluate = "NA";
                                }
                                String remarks = edtremark.getText().toString();
                                boolean check = false;
                                //Toast.makeText(EvaluationShowAssessment.this,edtremark.getText().toString(),Toast.LENGTH_SHORT).show();
                                if(sitem.size()!=0){
                                    for(int s=0;s<sitem.size();s++){
                                        if(sitem.get(s).getAsmtComb_FK().equals(asmtComb_FKs)){
                                            sitem.remove(s);
                                            sitem.add(new conansitem(asmtComb_FKs,
                                                    asmtComb_FKs,
                                                    assessmentName,
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    asmtH1ID_FK,
                                                    "",
                                                    "",
                                                    evaluate,
                                                    remarks,
                                                    uid,
                                                    uname,
                                                    appid,
                                                    monid
                                            ));
                                            break;
                                        }
                                    }
                                    if(!check){
                                        sitem.add(new conansitem(asmtComb_FKs,
                                                asmtComb_FKs,
                                                assessmentName,
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                asmtH1ID_FK,
                                                "",
                                                "",
                                                evaluate,
                                                remarks,
                                                uid,
                                                uname,
                                                appid,
                                                monid
                                        ));
                                    }

                                }else{
                                    sitem.add(new conansitem(asmtComb_FKs,
                                            asmtComb_FKs,
                                            assessmentName,
                                            "",
                                            assessmentHead,
                                            "",
                                            "",
                                            "",
                                            "",
                                            asmtH1ID_FK,
                                            "",
                                            "",
                                            evaluate,
                                            remarks,
                                            uid,
                                            uname,
                                            appid,
                                            monid
                                    ));
                                }
                                dialog.hide();
                            }
                        });
                        btnno.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.hide();
                            }
                        });
                    }
                });
                lcomp.addView(newanswer);
                c.moveToNext();
            }
        }

        pdialog.dismiss();

    }

    @Override
    public void onClick(View view) {
       Log.d("silist",sitem.size()+"");
       Log.d("sample",mergemodule);

       String message = "";
        if(sitem.size()>0){
            for(int i=0;i<sitem.size();i++){
                String dupid = "";
                if(mergemodule.equals("assess")){
                    dupid = db.get_tbl_assesscombineddupid(sitem.get(i).getAppid(),sitem.get(i).getEvaluatedBy(),
                            sitem.get(i).getAsmtComb_FK(), "", "", sitem.get(i).getAsmtH1ID_FK());
                }else if(mergemodule.equals("monitor")){
                    dupid = db.get_tbl_assesscombinedmonid(sitem.get(i).getAppid(),sitem.get(i).getEvaluatedBy(),
                            sitem.get(i).getAsmtComb_FK(), "", "", sitem.get(i).getAsmtH1ID_FK(),
                            sitem.get(i).getMonid());
                }else if(mergemodule.equals("evaluate")){
                    dupid = db.get_tbl_assesscombinedptc(sitem.get(i).getAppid(),sitem.get(i).getEvaluatedBy(),
                            sitem.get(i).getAsmtComb_FK(), "", "", sitem.get(i).getAsmtH1ID_FK());
                }

                if(!mergemodule.equals("evaluate")){
                    String[] dcolumns = {"evaluation","remarks","evaluatedBy","appid","monid","ename"};
                    String[] datas = {sitem.get(i).getEvaluation(),sitem.get(i).getRemarks(),
                            sitem.get(i).getEvaluatedBy(),sitem.get(i).getAppid(),sitem.get(i).getMonid(),sitem.get(i).getEname()};
                    Log.d("dupid",dupid);
                    if (db.update("assesscombined", dcolumns, datas, "dupID",dupid)) {
                        Log.d("assesscombined", "update");
                        message = "Successfully Merged Data";
                    }  else {
                        Log.d("assesscombined", "not update");
                        message = "Error on Merging Data";
                    }
                }else{
                    String[] dcolumns = {"evaluation","remarks","evaluatedBy","appid","ename"};
                    String[] datas = {sitem.get(i).getEvaluation(),sitem.get(i).getRemarks(),
                            sitem.get(i).getEvaluatedBy(),sitem.get(i).getAppid(),sitem.get(i).getEname()};
                    Log.d("dupid",dupid);
                    Log.d("evaluation",sitem.get(i).getEvaluation());
                    Log.d("remarks",sitem.get(i).getRemarks());
                    if (db.update("assesscombinedptc", dcolumns, datas, "dupID",dupid)) {
                        Log.d("assesscombinedptc", "update");
                        message = "Successfully Merged Data";
                    }  else {
                        Log.d("assesscombinedptc", "not update");
                        message = "Error on Merging Data";
                    }
                }
            }
            db.delete("assesscombinedtemp","","");
            android.app.AlertDialog.Builder messages = new android.app.AlertDialog.Builder(compareactivity.this);
            messages.setTitle("DOHOLRS");
            messages.setMessage(message);
            messages.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent comp = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(comp);
                    Animatoo.animateSlideRight(compareactivity.this);
                }
            });
            android.app.AlertDialog dialog3 = messages.create();
            dialog3.setIcon(R.drawable.doh);
            dialog3.show();

        }else{
            Toast.makeText(getApplicationContext(),"Please Choose data to be merge.",Toast.LENGTH_SHORT).show();
        }
    }
}
