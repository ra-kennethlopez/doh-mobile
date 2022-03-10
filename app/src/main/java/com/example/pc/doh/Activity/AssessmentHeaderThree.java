package com.example.pc.doh.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.Adapter.AssestmentAdapter;
import com.example.pc.doh.Adapter.ExpandableListAdapter;
import com.example.pc.doh.Adapter.HeadersAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.AssestmentModel;
import com.example.pc.doh.Model.Headers;
import com.example.pc.doh.Model.MenuModel;
import com.example.pc.doh.Model.UserModel;
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

public class AssessmentHeaderThree extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    TextView lblfacname;

    int menuindex = 0;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private ExpandableListAdapter expandableListAdapter;
    List<AssestmentModel> assesslist = new ArrayList<>();
    AssestmentAdapter aAdapter;
    private Context mContext;
    private SearchView searchView;
    private String uid;
    public static String type,code,faclityname,typefacility,date,status,appid;
    DatabaseHelper db;
    InternetCheck checker;
    public static String monType;
    public static String restpye = "";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    public static String licensetype = "";
    public static String id;
    public static String desc;
    Handler myHandler;
    //
    ArrayList<Headers> list = new ArrayList<>();
    HeadersAdapter hAdapter;
    RecyclerView headerrv;
    TextView tooltitle;
    PopupMenu menu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assessmentheaders);

        UserModel user = SharedPrefManager.getInstance(this).getUser();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        uid = user.getId();
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        expandableListView = findViewById(R.id.expandableListView);
        lblfacname = findViewById(R.id.facname);
        tooltitle = findViewById(R.id.toolbar_title);
        tooltitle.setText("Areas");
        tooltitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent headerone = new Intent(getApplicationContext(), AssessmentHeaderTwo.class);
                startActivity(headerone);
                Animatoo.animateSlideRight(AssessmentHeaderThree.this);
            }
        });
        lblfacname.setText(HomeActivity.faclityname);
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






        if(checker.checkHasInternet()){
            get_headerthree_online();
        }else{
            get_headerthree_offline();
        }

        headerrv = findViewById(R.id.headerrv);
        hAdapter = new HeadersAdapter(this, list);
        headerrv.setLayoutManager(new LinearLayoutManager(this));
        headerrv.setAdapter(hAdapter);
        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                id = list.get(position).getAsmt2l_id();
                desc = list.get(position).getAsmt2l_desc();
                Intent showassess = new Intent(getApplicationContext(), ShowAssessment.class);
                startActivity(showassess);
                Animatoo.animateSlideLeft(AssessmentHeaderThree.this);
                finish();

            }
        });

        lblfacname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu = new PopupMenu(getApplicationContext(), v);
                build_menu();

            }
        });

    }

    private void build_menu(){

        Cursor det = db.get_item("tbl_assessment_part", "appid", HomeActivity.appid);
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray head = obj.getJSONArray("head");
                    if(head.length()>0){
                        for(int i =0;i<head.length();i++){
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("desc");
                            SubMenu sub = menu.getMenu().addSubMenu(i,i,1,desc);
                            Cursor hdet = db.get_item("tbl_assessment_headerone", "id",id);
                            if (hdet != null && hdet.getCount() > 0) {
                                hdet.moveToFirst();
                                while (!hdet.isAfterLast()) {
                                    String hjson = hdet.getString(det.getColumnIndex("json_data"));
                                    try {
                                        JSONObject hobj = new JSONObject(hjson);
                                        JSONArray hhead = hobj.getJSONArray("head");
                                        if(hhead.length()>0){
                                            for(int hi =0;i<hhead.length();i++){
                                                String hid = hhead.getJSONObject(hi).getString("id");
                                                String hdesc =hhead.getJSONObject(hi).getString("desc");
                                                int sid = Integer.parseInt(hid);
                                                sub.add(i,sid,1,hdesc);
                                            }
                                        }else{

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    hdet.moveToNext();
                                }
                            }
                        }
                    }else{

                    }
                    Log.d("head",obj.getJSONArray("head").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                det.moveToNext();
            }
        }

        menu.show();





    }

    private void save_assessment_header(){
        String id = db.get_tbl_assessment_header(HomeActivity.appid,uid,AssessmentHeaderTwo.id,"2");
        if (db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
//            String[] ucolumns = {"choice","remarks"};
//            String[] udata = {choice,txtr.getText().toString()};
//            if (db.update("tbl_save_assessment_res", ucolumns, udata, "resid",id)) {
//                Log.d("updatedata", "update");
//            } else {
//                Log.d("updatedata", "not update");
//            }
        }else{
            String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid"};
            String[] datas = {AssessmentHeaderTwo.id,"2","true",HomeActivity.appid,uid};
            if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                Log.d("tblsaveassessheader", "added");
            } else {
                Log.d("tblsaveassessheader", "not added");
            }
        }
    }

    private void get_headerthree_offline(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Cursor det = db.get_item("tbl_assessment_headerthree", "id", AssessmentHeaderTwo.id);
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray head = obj.getJSONArray("head");
                    if(head.length()>0){
                        for(int i =0;i<head.length();i++){
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("desc");
                            String hid = db.get_tbl_assessment_header(HomeActivity.appid,uid,id,"3");
                            String assess = "false";
                            if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                assess = db.get_tbl_assessment_header_assess(HomeActivity.appid,uid,id,"3");
                            }
                            list.add(new Headers(id,desc, assess,"",""));
                        }
                    }else{

                    }
                    Log.d("head",obj.getJSONArray("head").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
                det.moveToNext();
            }
        } else {
            TextView lbl = findViewById(R.id.lblheadmessage);
            lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }
    }

    private void get_headerthree_online(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        //+ HomeActivity.appid + "/" + HomeActivity.type
        StringRequest request = new StringRequest(Request.Method.POST, Urls.getheaderthree+HomeActivity.appid+"/"+AssessmentHeaderTwo.id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray head = obj.getJSONArray("head");
                            ArrayList<String> checkid = new ArrayList<>();
                            int countassess = 0;
                            if (db.checkDatas("tbl_assessment_headerthree", "id", AssessmentHeaderTwo.id)){
                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_assessment_headerthree", ucolumns, udata, "appid",HomeActivity.appid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }
                            }else{
                                String[] dcolumns = {"json_data", "uid", "appid","id"};
                                String[] datas = {response, uid, HomeActivity.appid,AssessmentHeaderTwo.id};
                                if (db.add("tbl_assessment_headerthree", dcolumns, datas, "")) {
                                    Log.d("tbl_headerthree", "added");
                                } else {
                                    Log.d("tbl_headerthree", "not added");
                                }
                            }
                            if(head.length()>0){
                                for(int i =0;i<head.length();i++){
                                    String id = head.getJSONObject(i).getString("id");
                                    String desc =head.getJSONObject(i).getString("desc");
                                    //check if the id already exist
                                    String hid = db.get_tbl_assessment_header(HomeActivity.appid,uid,id,"3");
                                    String assess = "false";
                                    if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                       countassess++;
                                       assess = db.get_tbl_assessment_header_assess(HomeActivity.appid,uid,id,"3");
                                    }
                                    list.add(new Headers(id,desc, assess,"",""));
                                }
                            }else{

                            }
                            Log.d("head",obj.getJSONArray("head").toString());
                            if(countassess == list.size()){
                                Log.d("countassess","true");
                                save_assessment_header();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
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
                        Intent main = new Intent(AssessmentHeaderThree.this,MainActivity.class);
                        startActivity(main);
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        finish();
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Animatoo.animateSlideRight(AssessmentHeaderThree.this);
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

        int id = menuItem.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
