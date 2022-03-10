package com.example.pc.doh.Activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.Adapter.ExpandableListAdapter;
import com.example.pc.doh.Adapter.assessdetadapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.MenuModel;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.Model.evshowassessitem;
import com.example.pc.doh.Model.showassessitem;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EvaluationShowAssessment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView lblfacname;

    int menuindex = 0;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private ExpandableListAdapter expandableListAdapter;
    private String uid,uname;
    TextView address,review,remarkalert,header;
    DatabaseHelper db;
    InternetCheck checker;
    Button btnsubmit,btndraft;
    LinearLayout evaluate;
    List<evshowassessitem> silist = new ArrayList<>();
    EditText txtcomments;
    String count = "";
    LinearLayout sitems;

    //

    TextView tooltitle;
    //list item initialize
    RecyclerView rv;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showassessment);

        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        uname = user.getName();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        rv = findViewById(R.id.sassesslist);
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        sitems = findViewById(R.id.sitems);
        sitems.setVisibility(View.GONE);
        evaluate = findViewById(R.id.evaluate);
        expandableListView = findViewById(R.id.expandableListView);
        lblfacname = findViewById(R.id.facname);
        tooltitle = findViewById(R.id.toolbar_title);
        address = findViewById(R.id.address);
        review = findViewById(R.id.review);
        header = findViewById(R.id.headercat);
        header.setVisibility(View.GONE);
        txtcomments = findViewById(R.id.txtcomment);

//        remarkalert = findViewById(R.id.remarkalert);
//        remarkalert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        btnsubmit = findViewById(R.id.btnsubmit);
        btndraft = findViewById(R.id.btndraft);
        //btnsubmit.setVisibility(View.GONE);
        tooltitle.setText("Assessment Details");
        tooltitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent headerthree = new Intent(getApplicationContext(), EvaluationPart.class);
                startActivity(headerthree);
                Animatoo.animateSlideRight(EvaluationShowAssessment.this);
                Toast.makeText(getApplicationContext(),"ENTRIES SAVE AS DRAFT",Toast.LENGTH_SHORT).show();
            }
        });
        lblfacname.setText(EvaluationActivity.faclityname);
        prepareMenuData();
        populateExpandableList();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.txtempname);
        navUsername.setText(user.getName());


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                    drawer.closeDrawer(Gravity.RIGHT);
                } else {
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });



        //instanstiate list
        rv.setVisibility(View.GONE);
        retrieveassessdetails();



        evaluate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // Show an alert dialog.


                // Return false, then android os will still process click event,
                // if return true, the on click listener will never be triggered.
                return false;
            }
        });


        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if all data has answer
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                if(!checkSaveDatas()){
                    for(int i=0;i<silist.size();i++){
                        String choice = silist.get(i).getChoice();
                        String dupid = db.get_tbl_assesscombinedheaderptc(EvaluationActivity.appid,uid,silist.get(i).getId(),EvaluationPart.id);
                        if (db.checkDatas("assesscombinedptc", "dupID", dupid)){
                            Log.d("check","true");
                            Boolean check = db.get_tbl_assesscombinedptcuid(dupid,uid);
                            if(check){
                                String[] ucolumns = {"evaluation","remarks"};
                                String[] udata = {choice,silist.get(i).getRemarks()};
                                if (db.update("assesscombinedptc", ucolumns, udata, "dupid",dupid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }
                            }
                        }else{
                            String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","parttitle",
                                    "evaluation","remarks","evaluatedBy","appid","sub","isdisplay","revision","ename"};
                            String[] datas = {silist.get(i).getId(),silist.get(i).getDisp(),silist.get(i).getSequence(),silist.get(i).getOtherheading(),
                                    silist.get(i).getH3headid(),silist.get(i).getH3headback(),"","",
                                    EvaluationPart.id,EvaluationPart.desc,EvaluationPart.desc,choice,silist.get(i).getRemarks(),uid,EvaluationActivity.appid,silist.get(i).getSub(),
                                    silist.get(i).getIsdisplay(),silist.get(i).getRevision(),uname};

                            if (db.add("assesscombinedptc", dcolumns, datas, "")) {
                                Log.d("assesscombinedptc", "added");
                            } else {
                                Log.d("assesscombinedptc", "not added");
                            }
                        }
                    }

                    String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid","monid"};
                    String[] datas = {EvaluationPart.id,"0","true",EvaluationActivity.appid,uid,""};
                    if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                        Log.d("tblsaveassessheader", "added");
                    } else {
                        Log.d("tblsaveassessheader", "not added");
                    }


                    String[] rcolumns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                            "t_details"};
                    String[] rdatas = {"",txtcomments.getText().toString(),"","","","","","",uid,EvaluationActivity.appid,currentDateandTime};
                    if (!db.checkDatas("assessrecommend", "appid", EvaluationActivity.appid)){
                        if(db.add("assessrecommend",rcolumns,rdatas,"")){
                            Log.d("assessrecommend", "added");
                        }else{
                            Log.d("assessrecommend", "not added");
                        }
                    }else{
                        Log.d("found","true");
                    }
                //alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(EvaluationShowAssessment.this);
                    builder.setTitle("DOHOLRS");
                    builder.setMessage("Successfully Save Assessment Results");
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent header = new Intent(EvaluationShowAssessment.this,EvaluationPart.class);
                            startActivity(header);
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setIcon(R.drawable.doh);
                    dialog.show();
                }
                Log.d("silength",silist.size()+"");


            }
        });
        btndraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if some data has no answer
                if(checkSaveDatas()){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    for(int i=0;i<silist.size();i++){
                        Log.d("choice",silist.get(i).getChoice());
                        Log.d("remarks",silist.get(i).getRemarks());
                        String choice = silist.get(i).getChoice();
                        if(!choice.equals("nochoice")){
                            String dupid = db.get_tbl_assesscombinedheaderptc(EvaluationActivity.appid,uid,silist.get(i).getId(),EvaluationPart.id);
                            if (db.checkDatas("assesscombinedptc", "dupID", dupid)){
                                Log.d("check","true");
                                Boolean check = db.get_tbl_assesscombinedptcuid(dupid,uid);
                                if(check){
                                    String[] ucolumns = {"evaluation","remarks"};
                                    String[] udata = {choice,silist.get(i).getRemarks()};
                                    if (db.update("assesscombinedptc", ucolumns, udata, "dupid",dupid)) {
                                        Log.d("updatedata", "update");
                                    } else {
                                        Log.d("updatedata", "not update");
                                    }
                                }
                            }else{
                                String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","parttitle",
                                        "evaluation","remarks","evaluatedBy","appid","sub","isdisplay","revision","ename"};
                                String[] datas = {silist.get(i).getId(),silist.get(i).getDisp(),silist.get(i).getSequence(),silist.get(i).getOtherheading(),
                                        silist.get(i).getH3headid(),silist.get(i).getH3headback(),"","",
                                        EvaluationPart.id,EvaluationPart.desc,EvaluationPart.desc,choice,silist.get(i).getRemarks(),uid,EvaluationActivity.appid,silist.get(i).getSub(),
                                        silist.get(i).getIsdisplay(),silist.get(i).getRevision(),uname};
                                if (db.add("assesscombinedptc", dcolumns, datas, "")) {
                                    Log.d("assesscombinedptc", "added");
                                } else {
                                    Log.d("assesscombinedptc", "not added");
                                }
                            }
                        }
                    }
                    //Log.d("comment",txtcomments.getText().toString());
                    String[] rcolumns = {"choice","details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid",
                            "t_details","ename"};
                    String[] rdatas = {"",txtcomments.getText().toString(),"","","","","",count,uid,EvaluationActivity.appid,currentDateandTime,uname};
                    String reco = db.get_reco(uid,EvaluationActivity.appid);
                    if (!db.checkDatas("assessrecommend", "reco", reco)){
                        if(db.add("assessrecommend",rcolumns,rdatas,"")){
                            Log.d("assessrecommend", "added");
                        }else{
                            Log.d("assessrecommend", "not added");
                        }
                    }else{
                        Log.d("found","true");
                        if (db.update("assessrecommend", rcolumns, rdatas, "appid",EvaluationActivity.appid)) {
                            Log.d("updatedata", "update");
                        } else {
                            Log.d("updatedata", "not update");
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(EvaluationShowAssessment.this);
                    builder.setTitle("DOHOLRS");
                    builder.setMessage("Successfully Save As Draft");
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent header = new Intent(EvaluationShowAssessment.this,EvaluationPart.class);
                            startActivity(header);
                            finish();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.setIcon(R.drawable.doh);
                    dialog.show();
                }

            }
        });
    }

    private boolean checkSaveDatas(){
        for(int i=0;i<silist.size();i++){
            RadioGroup r = findViewById(i);
            int selectedId = r.getCheckedRadioButtonId();
            Log.d("select",selectedId+"");
            if(selectedId == -1){
              return true;
            }
        }
        return false;
    }


    private void retrieveassessdetails(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        String sid = db.getsid(EvaluationActivity.appid,"",EvaluationPart.id);
        Cursor det = db.get_item("tbl_show_assessment", "sid", sid);
        String comments = db.getcomments(uid,EvaluationActivity.appid);
        txtcomments.setText(comments);
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                String count = det.getString(det.getColumnIndex("count"));
                this.count = count;
                String r = "";
                if(count.equals("1")){
                   r = "Review :1st √ 2nd_ 3rd_";
                }else if(count.equals("2")){
                   r = "Review :1st_  2nd √ 3rd_";
                }else if(count.equals("3")){
                   r = "Review :1st_  2nd_  3rd √";
                }
                review.setText(r);

                Log.d("json",json);
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject data = obj.getJSONObject("data");
                    JSONArray head = obj.getJSONArray("head");
                    //Log.d("head",head.toString());
                    String addr = data.getString("streetname") +", "+ data.getString("brgyname") + ", "+
                            data.getString("cmname") +", "+ data.getString("provname");
                    address.setText(addr);
                    String h3headid = "";
                    int yesid = 1000;
                    int noid = 2000;
                    //Create Design
                    if(head.length()>0){
                        Log.d("headlength",head.length()+"");
                        for(int s=0;s<head.length();s++){

                            String desc = head.getJSONObject(s).getString("description");
                            String id = head.getJSONObject(s).getString("id");
                            String otherhead = head.getJSONObject(s).getString("otherHeading");
                            String seq = head.getJSONObject(s).getString("sequence");
                            String sub = head.getJSONObject(s).getString("subFor");
                            String isdisplay = head.getJSONObject(s).getString("isdisplay");
                            String h3id = head.getJSONObject(s).getString("h3HeadID");
                            String h3headback = head.getJSONObject(s).getString("h2HeadBack");
                            String choice = "nochoice";
                            String remarks = "";
                            Cursor c = db.get_tbl_assesscombinedheaderptcer(EvaluationActivity.appid,uid,id,EvaluationPart.id);
                            if(c!=null && c.getCount()>0){
                                Log.d("check","true");
                                c.moveToFirst();
                                while (!c.isAfterLast()){
                                    choice = c.getString(c.getColumnIndex("evaluation"));
                                    remarks = c.getString(c.getColumnIndex("remarks"));
                                    c.moveToNext();
                                }

                            }
                            silist.add(new evshowassessitem(desc,choice,remarks,id,otherhead,seq,sub,count,isdisplay,h3id,h3headback));
                        }
                        for(int i=0;i<head.length();i++){
                            final int index = i;
                            String h3id = head.getJSONObject(i).getString("h3HeadID");
                            String h3headback = head.getJSONObject(i).getString("h2HeadBack");
                            String subFor = head.getJSONObject(i).getString("subFor");
                            final String desc = head.getJSONObject(i).getString("description");

                            LinearLayout.LayoutParams firstparams = new LinearLayout.LayoutParams
                                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            LinearLayout.LayoutParams secondparams = new LinearLayout.LayoutParams
                                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            LinearLayout.LayoutParams rgtextparams = new LinearLayout.LayoutParams
                                    (400, LinearLayout.LayoutParams.WRAP_CONTENT);
                            LinearLayout.LayoutParams subforparams = new LinearLayout.LayoutParams
                                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);



                            TextView firstheader = new TextView(EvaluationShowAssessment.this);
                            firstheader.setTypeface(null,Typeface.BOLD);
                            firstheader.setTextSize(18);

                            firstheader.setLayoutParams(firstparams);
                            firstheader.setPadding(5,5,5,5);

                            //
                            //firstheader
                            if(!h3headid.equals(h3id)){
                                h3headid = h3id;
                                firstheader.setText(h3headback);
                                evaluate.addView(firstheader);
                            }
                            //end of firstheader


                            //second
                            LinearLayout secondl = new LinearLayout(EvaluationShowAssessment.this);
                            secondl.setOrientation(LinearLayout.HORIZONTAL);

                            RadioGroup rg = new RadioGroup(EvaluationShowAssessment.this);
                            rg.setId(i);
                            rg.setOrientation(LinearLayout.HORIZONTAL);
                            rg.setLayoutParams(secondparams);
                            rg.setPadding(5,5,5,5);
                            RadioButton yes = new RadioButton(EvaluationShowAssessment.this);
                            yes.setId(yesid);
                            if(silist.get(i).getChoice().equals("1")){
                             yes.setChecked(true);
                            }
                            yes.setText("Yes");
                            RadioButton no = new RadioButton(EvaluationShowAssessment.this);
                            no.setId(noid);
                            no.setText("No");
                            if(silist.get(i).getChoice().equals("0")){
                                no.setChecked(true);
                            }
                            TextView rgtext = new TextView(EvaluationShowAssessment.this);
                            rgtext.setText(Html.fromHtml(desc).toString().replaceAll("\n", "").trim());
                            rgtextparams.setMargins(10,0,0,0);
                            rgtext.setLayoutParams(rgtextparams);
                            rgtext.setTextSize(20);

                            rg.addView(yes);
                            rg.addView(no);
                            rg.addView(rgtext);

                            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                                    Log.d("change","execute"+i);
                                    if(i == 1000){
                                       silist.get(index).setChoice("1");
                                    }else if(i == 2000){
                                       silist.get(index).setChoice("0");
                                    }
                                }
                            });

                            TextView secondt = new TextView(EvaluationShowAssessment.this);
                            secondt.setId(i);
                            secondparams.weight = 1.0f;
                            secondt.setGravity(Gravity.RIGHT);
                            secondt.setTextColor(Color.parseColor("#007bff"));
                            secondt.setLayoutParams(secondparams);
                            secondt.setTextSize(20);
                            secondt.setText("Add Remarks");
                            secondt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EvaluationShowAssessment.this);
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
                                    LinearLayout alertlayout = new LinearLayout(EvaluationShowAssessment.this);
                                    alertlayout.setOrientation(LinearLayout.VERTICAL);
                                    alertlayout.setLayoutParams(alertparams);
                                    alertlayout.setPadding(10,10,10,10);
                                    alertlayout.setBackgroundColor(Color.parseColor("#272b30"));
                                    //
                                    //create textview
                                    TextView header = new TextView(EvaluationShowAssessment.this);
                                    header.setLayoutParams(alertparams);
                                    header.setTextColor(Color.WHITE);
                                    header.setTextSize(20);
                                    header.setTypeface(null,Typeface.BOLD);
                                    header.setGravity(Gravity.CENTER);
                                    header.setText("Add Remarks on:\n"+Html.fromHtml(desc));
                                    //create editext
                                    final EditText edtremark = new EditText(EvaluationShowAssessment.this);
                                    editextparams.setMargins(10,10,10,10);
                                    edtremark.setLayoutParams(editextparams);
                                    edtremark.setBackground(ContextCompat.getDrawable(EvaluationShowAssessment.this,R.drawable.edittextborder));
                                    edtremark.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                                    edtremark.setGravity(Gravity.TOP);
                                    edtremark.setTextSize(12);
                                    edtremark.setPadding(10,10,10,10);
                                    edtremark.setText(silist.get(index).getRemarks());
                                    //create button layout
                                    LinearLayout btnlayout = new LinearLayout(EvaluationShowAssessment.this);
                                    btnlayout.setOrientation(LinearLayout.HORIZONTAL);
                                    btnlayout.setLayoutParams(alertparams);
                                    btnlayout.setPadding(10,10,10,10);
                                    btnlayout.setBackgroundColor(Color.parseColor("#272b30"));
                                    //create button
                                    Button btnyes = new Button(EvaluationShowAssessment.this);
                                    btnyes.setText("SAVE");
                                    btnyes.setLayoutParams(btnparams);
                                    btnyes.setTextColor(Color.WHITE);
                                    btnyes.setBackgroundColor(Color.parseColor("#007bff"));
                                    Button btnno = new Button(EvaluationShowAssessment.this);
                                    btnno.setText("CANCEL");
                                    btnno.setLayoutParams(btnparams);
                                    btnno.setTextColor(Color.WHITE);
                                    btnno.setBackgroundColor(Color.parseColor("#dc3545"));
                                    //
                                    btnlayout.addView(btnyes);
                                    btnlayout.addView(btnno);
                                    //end of button layout

                                    alertlayout.addView(header);
                                    alertlayout.addView(edtremark);
                                    alertlayout.addView(btnlayout);

                                    builder.setView(alertlayout);
                                    final AlertDialog dialog = builder.create();
                                    dialog.show();
                                    btnyes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Toast.makeText(EvaluationShowAssessment.this,edtremark.getText().toString(),Toast.LENGTH_SHORT).show();
                                            edtremark.setText(edtremark.getText().toString());
                                            silist.get(index).setRemarks(edtremark.getText().toString());
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


                            secondl.addView(rg);
                            secondl.addView(secondt);

                            if(subFor.equals("null")){
                                secondl.setLayoutParams(firstparams);
                                evaluate.addView(secondl);
                            }else{
                                subforparams.setMargins(50,0,0,0);
                                secondl.setLayoutParams(subforparams);
                                evaluate.addView(secondl);
                            }



                            //


                            //evaluate.addView(secondl);
                        }
                    }
//                    for(int i=0;i<5;i++){
//                        LinearLayout.LayoutParams firstparams = new LinearLayout.LayoutParams
//                                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                        LinearLayout.LayoutParams secondparams = new LinearLayout.LayoutParams
//                                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//
//
//
//                        TextView firstheader = new TextView(EvaluationShowAssessment.this);
//                        firstheader.setTypeface(null,Typeface.BOLD);
//                        firstheader.setTextSize(18);
//
//                        firstheader.setLayoutParams(firstparams);
//                        firstheader.setPadding(5,5,5,5);
//                        if(i== 1){
//                            firstheader.setText("SAMPLE");
//                            evaluate.addView(firstheader);
//                        }
//
//
//                        //second
//                        LinearLayout secondl = new LinearLayout(EvaluationShowAssessment.this);
//                        secondl.setOrientation(LinearLayout.HORIZONTAL);
//
//                        RadioGroup rg = new RadioGroup(EvaluationShowAssessment.this);
//                        rg.setId(0);
//                        rg.setOrientation(LinearLayout.HORIZONTAL);
//                        rg.setLayoutParams(secondparams);
//                        rg.setPadding(5,5,5,5);
//                        RadioButton yes = new RadioButton(EvaluationShowAssessment.this);
//                        yes.setId(yesid);
//                        yes.setText("Yes");
//                        RadioButton no = new RadioButton(EvaluationShowAssessment.this);
//                        no.setId(noid);
//                        no.setText("No");
//                        TextView rgtext = new TextView(EvaluationShowAssessment.this);
//                        rgtext.setText("1.1 SAMPLE");
//                        secondparams.setMargins(10,0,0,0);
//                        secondparams.weight = 1.0f;
//                        rgtext.setLayoutParams(secondparams);
//                        rgtext.setTextSize(20);
//
//                        rg.addView(yes);
//                        rg.addView(no);
//                        rg.addView(rgtext);
//
//
//
//                        TextView secondt = new TextView(EvaluationShowAssessment.this);
//                        secondparams.weight = 1.0f;
//                        secondt.setGravity(Gravity.RIGHT);
//                        secondt.setTextColor(Color.parseColor("#007bff"));
//                        secondt.setLayoutParams(secondparams);
//                        secondt.setTextSize(20);
//                        secondt.setText("Add Remarks");
//
//
//                        secondl.addView(rg);
//                        secondl.addView(secondt);
//
//                        evaluate.addView(secondl);
//                    }






                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
                //btnsubmit.setVisibility(View.VISIBLE);
                det.moveToNext();
            }
        } else {
            TextView lbl = findViewById(R.id.lblheadmessage);
            //lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }

    }




    private void prepareMenuData() {

        MenuModel menuModel;
        ///Drawable img = getAlContext().getResources().getDrawable( R.drawable.smiley );
        menuModel = new MenuModel("Licensing Process", true, true,R.drawable.ic_sitemap,0);
        headerList.add(menuModel);
        List<MenuModel> childModelsList = new ArrayList<>();
        MenuModel childModel = new MenuModel("Assessment Tool", false, false,0,0);
        childModelsList.add(childModel);
        childModel = new MenuModel("Evaluation Tool", false, false,0,1);
        childModelsList.add(childModel);
        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Monitoring", true, true,R.drawable.ic_desktop_windows_black_24dp,1);
        headerList.add(menuModel);
        childModel = new MenuModel("Monitoring Tool", false, false,0,0);
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Account Settings", true, true,R.drawable.ic_settings_black_24dp,3);
        headerList.add(menuModel);

        if(checker.checkHasInternet()){
            childModel = new MenuModel("Change Password", false, false,0,0);
            childModelsList.add(childModel);

        }else{
            childModel = new MenuModel("Change Pin Password", false, false,0,1);
            childModelsList.add(childModel);


        }

        if(db.check_has_pin_password(uid)){
            childModel = new MenuModel("Set Pin Password", false, false,0,2);
            childModelsList.add(childModel);
        }






        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        menuModel = new MenuModel("Logout", false, false,R.drawable.ic_power_settings_new_black_24dp,4);
        headerList.add(menuModel);


    }

    private void populateExpandableList() {

        expandableListAdapter = new ExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        onBackPressed();
                    }
                }


                switch (headerList.get(groupPosition).index){
                    case 4:
                        Intent main = new Intent(EvaluationShowAssessment.this,MainActivity.class);
                        startActivity(main);
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        finish();
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Animatoo.animateSlideRight(EvaluationShowAssessment.this);
                        break;
                   /* case 7:

                        break;*/
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    switch (headerList.get(groupPosition).index){
                        case 0:
                            if(model.index==1){
                                Intent evaluation = new Intent(getApplicationContext(),EvaluationActivity.class);
                                startActivity(evaluation);
                            }
                            if(model.index==0){
                                Intent home = new Intent(getApplicationContext(),HomeActivity.class);
                                startActivity(home);
                            }
                            break;
                        case 1:
                            if(model.index==0){
                                Intent monitor = new Intent(getApplicationContext(),MonitoringActivity.class);
                                startActivity(monitor);
                            }
                            break;
                        case 2:
                            if(model.index==0){
                               /* Intent changepass = new Intent(getApplicationContext(),ChangePasswordActivity.class);
                                startActivity(changepass);*/
                            }
                            break;
                        case 3:
                            if(model.index==0){
                                Intent changepass = new Intent(getApplicationContext(),ChangePasswordActivity.class);
                                startActivity(changepass);
                            }
                            if(model.index==1){
                                Intent changepass = new Intent(getApplicationContext(),ChangePinPasswordActivity.class);
                                startActivity(changepass);
                            }
//                            if(model.index==2){
//                                set_pinpassword();
//
//                            }
                            break;
                        case 5:
                            if(model.index==0){


                            }
                            if(model.index==1){

                            }//mongenerate
                            if(model.index==2){


                            }//mongenerate
                            break;
                        case 6:
                            if(model.index==0){
                                /*Intent send = new Intent(getApplicationContext(),senddataActivity.class);
                                startActivity(send);*/
                                menuindex = 0;


                            }
                            if(model.index==1){
                                menuindex = 1;

                            }
                            if(model.index==2){
                                menuindex = 2;

                            }
                            /*if(model.index==1){
                                Intent receive = new Intent(getApplicationContext(),receivedataActivity.class);
                                startActivity(receive);

                            }*/
                            break;
                        case 7:


                            break;

                    }
                    onBackPressed();

                }

                return false;
            }
        });
    }





    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
