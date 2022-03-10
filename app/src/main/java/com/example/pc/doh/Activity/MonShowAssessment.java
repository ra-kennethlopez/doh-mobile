package com.example.pc.doh.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.Adapter.ExpandableListAdapter;
import com.example.pc.doh.Adapter.assessdetadapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.MenuModel;
import com.example.pc.doh.Model.Monitoring;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.Model.showassessitem;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonShowAssessment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView lblfacname;

    int menuindex = 0;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private ExpandableListAdapter expandableListAdapter;
    private String uid,uname;
    TextView address,review,header,items,ansitems;
    DatabaseHelper db;
    InternetCheck checker;
    Button btnsubmit,btndraft;
    LinearLayout lcomment;

    //

    TextView tooltitle;
    //list item initialize
    RecyclerView rv;
    List<showassessitem> silist = new ArrayList<>();
    assessdetadapter adapter;
    String position = "";
    String hid,hdesc;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showassessment);

        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        uid = user.getId();
        uname = user.getName();
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        rv = findViewById(R.id.sassesslist);

        if(MonitoringPart.hid!=null){
            Log.d("found","true");
            hid = MonitoringPart.hid;
            hdesc = MonitoringPart.hdesc;
            Log.d("hid",hid);
            Log.d("hdesc",hdesc);
        }else{
            hid = MonitorHeaderOne.id;
            hdesc = MonitorHeaderOne.desc;
            Log.d("found","false");
            Log.d("hid",hid);
            Log.d("hdesc",hdesc);
        }

        String json = db.get_user_json_data(uid);
        Log.d("json",json);
        try {
            JSONObject obj = new JSONObject(json);
            position = obj.getJSONObject("data").getString("position");
            //Log.d("position",obj.getJSONObject("data").getString("position"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        expandableListView = findViewById(R.id.expandableListView);
        lblfacname = findViewById(R.id.facname);
        tooltitle = findViewById(R.id.toolbar_title);
        address = findViewById(R.id.address);
        review = findViewById(R.id.review);
        header = findViewById(R.id.headercat);
        items = findViewById(R.id.items);
        ansitems = findViewById(R.id.ansitems);
        btnsubmit = findViewById(R.id.btnsubmit);
        btndraft = findViewById(R.id.btndraft);
        lcomment = findViewById(R.id.lcomments);
        lcomment.setVisibility(View.GONE);
        btndraft.setVisibility(View.GONE);
        btnsubmit.setVisibility(View.GONE);
        tooltitle.setText("Assessment Details");
        tooltitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonitoringPart.hid = null;
                MonitoringPart.hdesc = null;
                Intent headerthree = new Intent(getApplicationContext(), MonitorHeaderOne.class);
                startActivity(headerthree);
                Animatoo.animateSlideRight(MonShowAssessment.this);
                //Toast.makeText(getApplicationContext(),"ENTRIES SAVE AS DRAFT",Toast.LENGTH_SHORT).show();
            }
        });
        lblfacname.setText(MonitoringActivity.faclityname);
        review.setVisibility(View.GONE);
        header.setText(MonitoringPart.desc + "    >    "+hdesc);
        ansitems.setText("Answer item(s) : "+db.countanswermon(MonitoringPart.id,hid, MonitoringActivity.appid,
                MonitoringActivity.type)+"");
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

        adapter = new assessdetadapter(this,silist);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        adapter.addItemTouchListener(new assessdetadapter.onTouchListener() {
            @Override
            public void onTouch(int position) {
                int size = silist.size() - 1;
                if(size == position)
                {
                    btnsubmit.setVisibility(View.VISIBLE);
                    btndraft.setVisibility(View.VISIBLE);
                }else{
                    btnsubmit.setVisibility(View.GONE);
                    btndraft.setVisibility(View.GONE);
                }
                Log.d("touch",position+"");
                save_assessment(position);
                int page = position + 1;
                ansitems.setText("Answer item(s) : "+db.countanswermon(MonitoringPart.id,hid, MonitoringActivity.appid,MonitoringActivity.type));
                items.setText(page+" of "+silist.size());

            }
        });

       retrieveassessdetails();
//        if(checker.checkHasInternet()){
//            Log.d("internet","true");
//            get_showassessment();
//        }else{
//
//            Log.d("internet","false");
//        }


        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(silist.size() == 1){
                    save_assessment(0);
                    save_assessment_header();
                }else{
                    //menu.getItem(0).setVisible(false);
                    boolean check = false;
                    boolean skipcheck = false;
                    //check if all results are answer
                   int index = 0;
                    for(int i=0;i<silist.size();i++){
                       if(silist.get(i).getChoice().equals("")){
                           Log.d("choice","true");
                           index = i;
                           check = true;
                           break;
                       }else if(silist.get(i).getChoice().equals("SKIP"))
                       {
                           skipcheck = true;
                       }
                    }
                   final int pos = index;
                   if(check){
                       AlertDialog.Builder builder = new AlertDialog.Builder(MonShowAssessment.this);
                       builder.setTitle("DOHOLRS");
                       builder.setMessage("Please answer all assessments.");
                       builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               rv.scrollToPosition(pos);
                           }
                       });
                       AlertDialog dialog = builder.create();
                       dialog.setIcon(R.drawable.doh);
                       dialog.show();
                   }else if(skipcheck){
                       AlertDialog.Builder builder = new AlertDialog.Builder(MonShowAssessment.this);
                       builder.setTitle("DOHOLRS");
                       builder.setMessage("Some questions are skip.Please save as draft.");
                       builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               //rv.scrollToPosition(pos);
                           }
                       });
                       AlertDialog dialog = builder.create();
                       dialog.setIcon(R.drawable.doh);
                       dialog.show();
                   }else {
                       save_assessment_header();
                   }
                    //save_assessment_header();
                }
            }
        });

        btndraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonitoringPart.hid = null;
                MonitoringPart.hdesc = null;
                Intent headerthree = new Intent(getApplicationContext(), MonitorHeaderOne.class);
                startActivity(headerthree);
                Animatoo.animateSlideRight(MonShowAssessment.this);
                Toast.makeText(getApplicationContext(),"ENTRIES SAVE AS DRAFT",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        items.setText("1 of "+silist.size());


    }

    @Override
    public void onBackPressed() {
        MonitoringPart.hid = null;
        MonitoringPart.hdesc = null;
        super.onBackPressed();
    }

    private void save_assessment(int pos){
        View view =rv.findViewWithTag(pos);
        if(view!=null){
            EditText txtr = view.findViewById(R.id.remark);
            RadioGroup r = view.findViewById(R.id.rgchoice);
            int selectedId = r.getCheckedRadioButtonId();
            String choice = "";
            //rv.setNestedScrollingEnabled(true);

            switch (selectedId){
                case R.id.yes:
                    choice = "1";
                    //txtr.setError(null);
                    rv.setLayoutFrozen(false);
                    silist.get(pos).setChoice("1");
                    break;
                case R.id.no:
                    choice = "0";
                    silist.get(pos).setChoice("0");
                    rv.setLayoutFrozen(false);
                    /*
                    if(TextUtils.isEmpty(txtr.getText().toString())){
                        txtr.setError("Please Enter your Remarks. Thank You");
                        rv.setNestedScrollingEnabled(false);
                        return;
                    }*/
                    break;
                case R.id.na:
                    silist.get(pos).setChoice("NA");
                    rv.setLayoutFrozen(false);
                    choice = "NA";
                    break;
                case R.id.skip:
                    silist.get(pos).setChoice("SKIP");
                    rv.setLayoutFrozen(false);
                    choice = "SKIP";
                    break;
                default:
                    choice = "nochoice";
                    Toast.makeText(getApplicationContext(),"Please Choose your Answer",Toast.LENGTH_SHORT).show();
                    rv.scrollToPosition(pos);
                    rv.setLayoutFrozen(true);
                    break;

            }
            if(txtr.getText().toString() != null && !txtr.getText().toString().equals("")){
                Log.d("remark",txtr.getText().toString());
                silist.get(pos).setRemarks(txtr.getText().toString());
            }
            Log.d("choice",choice);
            Log.d("id",silist.get(pos).getId());
            Log.d("desc",silist.get(pos).getDisp());
            Log.d("otherheading",silist.get(pos).getOtherheading());
            Log.d("sequence",silist.get(pos).getSequence());
            if(choice != "nochoice"){
                String dupid = db.get_tbl_assesscombinedheaderonemon(MonitoringActivity.appid,uid,silist.get(pos).getId(),hid,MonitoringActivity.type);
                if (db.checkDatas("assesscombined", "dupID", dupid)){
                    Log.d("check","true");
                    Boolean check = db.get_tbl_assesscombineduid(dupid,uid);
                    if(check){
                        String[] ucolumns = {"evaluation","remarks"};
                        String[] udata = {choice,txtr.getText().toString()};
                        if (db.update("assesscombined", ucolumns, udata, "dupid",dupid)) {
                            Log.d("updatedata", "update");
                        } else {
                            Log.d("updatedata", "not update");
                        }
                    }
                }else{
                    Log.d("check","false");
                    String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name",
                            "evaluation","remarks","evaluatedBy","appid","monid","epos","ename","partID"};
                    String[] datas = {silist.get(pos).getId(),silist.get(pos).getDisp(),silist.get(pos).getSequence(),silist.get(pos).getOtherheading(),
                            MonitoringPart.id,MonitoringPart.desc,"","",
                            hid,hdesc,choice,txtr.getText().toString(),uid,MonitoringActivity.appid,MonitoringActivity.type,position,uname,MonitoringPart.id};

                    if (db.add("assesscombined", dcolumns, datas, "")) {
                        Log.d("assesscombined", "added");
                    } else {
                        Log.d("assesscombined", "not added");
                    }
                }
            }else{

            }
        }
    }

    private void save_assessment_header(){
        String id = db.get_tbl_assessment_header_mon(MonitoringActivity.appid,uid,hid,"1",MonitoringActivity.type);
        if (db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
//            String[] ucolumns = {"choice","remarks"};
//            String[] udata = {choice,txtr.getText().toString()};
//            if (db.update("tbl_save_assessment_res", ucolumns, udata, "resid",id)) {
//                Log.d("updatedata", "update");
//            } else {
//                Log.d("updatedata", "not update");
//            }
        }else{
            String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid","monid"};
            String[] datas = {hid,"1","true",MonitoringActivity.appid,uid,MonitoringActivity.type};
            if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                Log.d("tblsaveassessheader", "added");
            } else {
                Log.d("tblsaveassessheader", "not added");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MonShowAssessment.this);
            builder.setTitle("DOHOLRS");
            builder.setMessage("Successfully Save Assessment Results");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MonitoringPart.hid = null;
                    MonitoringPart.hdesc = null;
                    Intent header = new Intent(MonShowAssessment.this,MonitorHeaderOne.class);
                    startActivity(header);
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setIcon(R.drawable.doh);
            dialog.show();
        }

    }

    private void get_showassessment(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Log.d("getshowassessment","execute");
        Log.d("appid",MonitoringActivity.appid);
        //Log.d("oneid",MonitorHeaderOne.id);
        Log.d("type",MonitoringActivity.type);

        StringRequest request = new StringRequest(Request.Method.POST, Urls.getassessmentdet+MonitoringActivity.appid+"/"+
                hid+"/"+MonitoringActivity.type,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responsess",response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.getJSONObject("data");
                            JSONArray head = obj.getJSONArray("head");
                            String sid = db.getsid(MonitoringActivity.appid,MonitoringActivity.type,hid);
                            if (db.checkDatas("tbl_show_assessment", "sid",sid)){
                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_show_assessment", ucolumns, udata, "sid",sid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }
                            }else{
                                String[] dcolumns = {"json_data", "uid", "appid","id","monid"};
                                String[] datas = {response, uid,MonitoringActivity.appid,hid,MonitoringActivity.type};
                                if (db.add("tbl_show_assessment", dcolumns, datas, "")) {
                                    Log.d("tbl_show_assessment", "added");
                                } else {
                                    Log.d("tbl_show_assessment", "not added");
                                }
                            }
                            //Log.d("head",head.toString());
                            String addr = data.getString("streetname") +", "+ data.getString("brgyname") + ", "+
                                    data.getString("cmname") +", "+ data.getString("provname");
                            address.setText(addr);
                            if(head.length()>0){
                                for(int i =0;i<head.length();i++){
                                    String id = head.getJSONObject(i).getString("id");
                                    String desc =head.getJSONObject(i).getString("description");
                                    String otherhead = "";
                                    String seq = head.getJSONObject(i).getString("sequence");
                                    String choice = "";
                                    String remarks = "";
                                    if(!head.getJSONObject(i).isNull("otherHeading")){
                                        Log.d("otherheading"," not null");
                                        otherhead = head.getJSONObject(i).getString("otherHeading");
                                        desc = head.getJSONObject(i).getString("description");
                                    }
                                    String dupid = db.get_tbl_assesscombinedheaderonemon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                                    if(!dupid.equals("")){
                                        Log.d("dupid","true");
                                        if (db.checkDatas("assesscombined", "dupID", dupid)){
                                            choice = db.get_tbl_assesscombinedevaluationmon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                                            remarks = db.get_tbl_assesscombinedremarksmon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                                        }
                                    }
                                    silist.add(new showassessitem(desc,choice,remarks,id,otherhead,seq));
                                }
                            }else{

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        btnsubmit.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                        sv.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("isMobile","dan");
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void retrieveassessdetails(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        String sid = db.getsid(MonitoringActivity.appid,MonitoringActivity.type,hid);
        Cursor det = db.get_item("tbl_show_assessment", "sid",sid);
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                Log.d("json",json);
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject data = obj.getJSONObject("data");
                    JSONArray head = obj.getJSONArray("head");
                    //Log.d("head",head.toString());
                    String addr = data.getString("streetname") +", "+ data.getString("brgyname") + ", "+
                            data.getString("cmname") +", "+ data.getString("provname");
                    address.setText(addr);
                    if(head.length()>0){
                        for(int i =0;i<head.length();i++){
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("description");
                            String otherhead = "";
                            String seq = head.getJSONObject(i).getString("sequence");
                            String choice = "";
                            String remarks = "";
                            if(!head.getJSONObject(i).isNull("otherHeading")){
                                Log.d("otherheading"," not null");
                                otherhead = head.getJSONObject(i).getString("otherHeading");
                                desc = head.getJSONObject(i).getString("description");
                            }
                            Log.d("headid",id);

                            String dupid = db.get_tbl_assesscombinedheaderonemon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                            if(!dupid.equals("")){
                                Log.d("dupid","true");
                                if (db.checkDatas("assesscombined", "dupID", dupid)){
                                    choice = db.get_tbl_assesscombinedevaluationmon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                                    remarks = db.get_tbl_assesscombinedremarksmon(MonitoringActivity.appid,uid,id,hid,MonitoringActivity.type);
                                }
                            }
                            silist.add(new showassessitem(desc,choice,remarks,id,otherhead,seq));
                        }
                    }else{

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
                btnsubmit.setVisibility(View.VISIBLE);
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
                        Intent main = new Intent(MonShowAssessment.this,MainActivity.class);
                        startActivity(main);
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        finish();
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Animatoo.animateSlideRight(MonShowAssessment.this);
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
