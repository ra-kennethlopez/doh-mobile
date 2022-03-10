package com.example.pc.doh.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;

public class recommendation extends AppCompatActivity implements View.OnClickListener {
    LinearLayout l1,l2;
    Spinner cboChoice;
    ArrayList<String> item = new ArrayList<>();
    ArrayAdapter<String> adapter;
    Button btnsubmit;
    DatePicker dtp1,dtp2;
    EditText days,notes,nobeds,recby,pos,nodial;
    DatabaseHelper db;
    private String uid,uname;
    public static String type;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommendation);
        l1 = findViewById(R.id.layout1);
        l2 = findViewById(R.id.layout2);
        btnsubmit = findViewById(R.id.submit);
        dtp1 = findViewById(R.id.datePicker1);
        dtp2 = findViewById(R.id.datePicker2);
        days = findViewById(R.id.days);
        notes = findViewById(R.id.notes);
        nobeds = findViewById(R.id.nobeds);
        nodial = findViewById(R.id.nodialysis);
        recby = findViewById(R.id.recby);
        pos = findViewById(R.id.pos);
        db = new DatabaseHelper(this);


        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(HomeActivity.licensetype.equals("assessment")){
                    Intent assestment = new Intent(recommendation.this,AssessmentPart.class);
                    startActivity(assestment);
                    Animatoo.animateSlideRight(recommendation.this);
                }else if(HomeActivity.licensetype.equals("monitoring")){
                    Intent evaluation = new Intent(recommendation.this,MonitoringPart.class);
                    startActivity(evaluation);
                    Animatoo.animateSlideRight(recommendation.this);
                }else{
                    Intent evaluation = new Intent(recommendation.this,EvaluationPart.class);
                    startActivity(evaluation);
                    Animatoo.animateSlideRight(recommendation.this);
                }
            }
        });
        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        uname = user.getName();
        getSupportActionBar().setTitle("Recommendation");


        cboChoice = findViewById(R.id.choice);
        if(type.equals("Assessment")){
            item.add("For Issuance of License");
            item.add("For Compliance");
            item.add("For Non Issuance");
        }else if(type.equals("Monitoring")){
            item.add("Acceptable");
            item.add("Unacceptable Corrective Action");
            item.add("Others, Please specify");
            l1.setVisibility(View.GONE);
            l2.setVisibility(View.GONE);
        }

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        cboChoice.setAdapter(adapter);
        cboChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tutorialsName = parent.getItemAtPosition(position).toString();
                switch (position){
                    case 0:
                        if(type.equals("Assessment")){
                            l2.setVisibility(View.GONE);
                            l1.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 1:
                        if(type.equals("Assessment")){
                            l1.setVisibility(View.GONE);
                            l2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 2:
                        if(type.equals("Assessment")){
                            l1.setVisibility(View.GONE);
                            l2.setVisibility(View.GONE);
                        }

                        break;
                }
//                Toast.makeText(parent.getContext(), "Selected: " + position,          Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        btnsubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        String choice = "";
        String dt1 = "";
        String dt2 = "";
        String noted = notes.getText().toString();
        String d = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        String m="";
        String da="";
        String y="";
        String beds = "",dial="";
        String rec= recby.getText().toString();
        String p = pos.getText().toString();
        switch (cboChoice.getSelectedItemPosition()){
            case 0:
                choice = "issuance";
                m = dtp1.getMonth()+1+"";
                da = dtp1.getDayOfMonth()+"";
                y = dtp1.getYear()+"";
                //dt1 = m+"-"+da+"-"+y;
                dt1 = y+"-"+m+"-"+da;
                m = dtp2.getMonth()+1+"";
                da = dtp2.getDayOfMonth()+"";
                y = dtp2.getYear()+"";
                //dt2 = m+"-"+da+"-"+y;
                dt2 = y+"-"+m+"-"+da;
                beds = nobeds.getText().toString();
                dial = nodial.getText().toString();
                break;
            case 1:
                choice = "compliance";
                d = days.getText().toString();
                break;
            case 2:
                choice = "non";
                break;

        }

        if(type.equals("Assessment")){
            String[] columns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                    "t_details","noofbed","noofdialysis","conforme","conformeDesignation","ename"};
            String[] datas = {choice,noted,dt1,dt2,d,"","","",uid,HomeActivity.appid,currentDateandTime,beds,dial,rec,p,uname};
            String reco = db.getrecoid(HomeActivity.appid,"");
            Log.d("reco",reco);
            if (!db.checkDatas("assessrecommend", "reco", reco)){
                Log.d("reco",reco);
                if(db.add("assessrecommend",columns,datas,"")){
                    Log.d("assessrecommend", "added");
                }else{
                    Log.d("assessrecommend", "not added");
                }
            }else{
                String[] ucolumns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                        "t_details","noofbed","noofdialysis","conforme","conformeDesignation","ename"};
                String[] udatas = {choice,noted,dt1,dt2,d,"","","",uid,HomeActivity.appid,currentDateandTime,beds,dial,rec,p,uname};
                if (db.update("assessrecommend", ucolumns, udatas, "appid",HomeActivity.appid)) {
                    Log.d("assessrecommend", "update");
                } else {
                    Log.d("assessrecommend", "not update");
                }
            }
        }else{
            String[] columns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                    "t_details","noofbed","noofdialysis","conforme","conformeDesignation","ename"};
            String[] datas = {choice,noted,dt1,dt2,d,MonitoringActivity.type,"","",uid,MonitoringActivity.appid,currentDateandTime,beds,dial,rec,p,uname};
            String reco = db.getrecoidmon(MonitoringActivity.appid,MonitoringActivity.type);
            if (!db.checkDatas("assessrecommend", "reco", reco)){
                if(db.add("assessrecommend",columns,datas,"")){
                    Log.d("assessrecommend", "added");
                }else{
                    Log.d("assessrecommend", "not added");
                }
            }else{
                String[] ucolumns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                        "t_details","noofbed","noofdialysis","conforme","conformeDesignation","ename"};
                String[] udatas = {choice,noted,dt1,dt2,d,MonitoringActivity.type,"","",uid,MonitoringActivity.appid,currentDateandTime,beds,dial,rec,p,uname};
                if (db.update("assessrecommend", ucolumns, udatas, "appid",MonitoringActivity.appid)) {
                    Log.d("assessrecommend", "update");
                } else {
                    Log.d("assessrecommend", "not update");
                }
            }
        }



        Intent intent=new Intent();
        intent.putExtra("MESSAGE","message");
        setResult(0,intent);
        finish();//finishing activity
    }
}
