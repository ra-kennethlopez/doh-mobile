package com.example.pc.doh.Activity;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;
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
import com.example.pc.doh.AESUtils;
import com.example.pc.doh.Adapter.AssestmentAdapter;
import com.example.pc.doh.Adapter.ExpandableListAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.AssestmentModel;
import com.example.pc.doh.Model.MenuModel;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;
import com.itextpdf.text.DocumentException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView recyclerView;
    String message = "";
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
    Handler myHandler;
    SwipeRefreshLayout swipe;
    TextView lbl;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        UserModel user = SharedPrefManager.getInstance(this).getUser();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//        }
        uid = user.getId();
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Assessment");

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        expandableListView = findViewById(R.id.expandableListView);
        lbl = findViewById(R.id.lblmessage);
        swipe = findViewById(R.id.swipe);
        int c1 = getResources().getColor(R.color.color28A55F);
        swipe.setColorSchemeColors(c1,c1,c1);
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

        recyclerView = findViewById(R.id.assesetmentrv);

        if(checker.checkHasInternet()){
            get_assessment_online();
            if(check_islogin()){
                //set_pinpassword();
            }
        }else{
            get_assessment_offline();
        }

        Log.d("restype",restpye);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                assesslist.clear();
                if(checker.checkHasInternet()){
                    get_assessment_online();
                }else{
                    get_assessment_offline();
                }
            }
        });


    }


    private void set_pinpassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.dialog, null);
        final EditText pass = (EditText) mView.findViewById(R.id.etEmail);
        final EditText conpass = (EditText) mView.findViewById(R.id.etPassword);
        Button msave = (Button) mView.findViewById(R.id.btnsave);
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        msave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String p = pass.getText().toString();
                String cp = conpass.getText().toString();
                if(p.equals(cp)){

                    String[] columns = {"pinpassword"};
                    String[] data = {p};
                    if(db.update("tbl_user",columns,data,"uid",uid)){
                        Log.d("updatedata","update");

                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("DOHOLRS");
                        builder.setMessage("Successfully Set Pin Password");
                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                Animatoo.animateFade(HomeActivity.this);
                            }
                        });

                        android.support.v7.app.AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.doh);
                        dialog.show();
                    }else{
                        Log.d("updatedata","not update");
                    }
                    dialog.dismiss();
                }else{
                    Toast.makeText(HomeActivity.this,"Password and Confirm Password not match",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean check_islogin(){
        boolean check = false;
        Cursor det = db.get_item("tbl_user","uid",uid);
        if(det!=null && det.getCount()>0){

            det.moveToFirst();
            while (!det.isAfterLast()){

                if(det.getString(det.getColumnIndex("islogin")).equals("1")){
                    check = true;
                }

                det.moveToNext();
            }
        }else{
            Log.d("islogin","empty");
        }

        return check;
    }

    private void get_assessment_offline(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        recyclerView.setVisibility(View.GONE);
        lbl.setVisibility(View.GONE);
        Cursor det = db.get_item_Assessment("tbl_assessment",uid,"assessment");
        boolean check = false;
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while (!det.isAfterLast()){
                try {
                    JSONObject obj = new JSONObject(det.getString(det.getColumnIndex("json_data")));
                    JSONArray jsonArray = obj.getJSONArray("data");
                    if(obj.has("data")){
                        //Log.d("datajson",jsonArray.toString());
                        for(int i=0;i<jsonArray.length();i++){
                            String id = jsonArray.getJSONObject(i).getString("hfser_id");
                            String date = jsonArray.getJSONObject(i).getString("t_date");
                            String status = jsonArray.getJSONObject(i).getString("trns_desc");
                            String facilityname = jsonArray.getJSONObject(i).getString("facilityname");
                            String code = jsonArray.getJSONObject(i).getString("hfser_id") +"R"+ jsonArray.getJSONObject(i).getString("rgnid")+"-"+
                                    jsonArray.getJSONObject(i).getString("appid");
                            String typefacility = jsonArray.getJSONObject(i).getString("hgpdesc");
                            if(typefacility.equals("")){
                                typefacility = "";
                            }
                            String appid = jsonArray.getJSONObject(i).getString("appid");
                            if(id.equals("LTO") || id.equals("COA")){
                                assesslist.add(new AssestmentModel(id,code,facilityname,typefacility,date,status,appid));
                            }
                        }
                        setAdapter();
                    }else{
                        lbl.setVisibility(View.VISIBLE);
                        lbl.setText("No Data Available");
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    bar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                det.moveToNext();
            }


        }
    }

    private void get_assessment_online(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        recyclerView.setVisibility(View.GONE);
        lbl.setVisibility(View.GONE);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.assessment+uid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("assessresponse",response);
                            //changes
                            String[] columns = {"uid","json_data","type"};
                            String[] data = {uid,response,"assessment"};
                            if(db.checkLicensingDatas("tbl_assessment",uid,"assessment")){
                                Log.d("checkdatas","found");
                                String aid = db.getassesid(uid,"assessment");
                                Log.d("assessid",aid);
                                if(db.update("tbl_assessment",columns,data,"assessment_id",aid)){
                                    Log.d("updatedata","update");
                                }else{
                                    Log.d("updatedata","not update");
                                }
                            }else{
                                Log.d("checkdatas","not found");
                                if(db.add("tbl_assessment",columns,data,"")){
                                    Log.d("tbl_assessment","added");
                                }else{
                                    Log.d("tbl_assessment","not added");
                                }
                            }

                            JSONArray jsonArray = obj.getJSONArray("data");
                            if(obj.has("data")){
                                //Log.d("datajson",jsonArray.toString());
                                for(int i=0;i<jsonArray.length();i++){
                                    String id = jsonArray.getJSONObject(i).getString("hfser_id");
                                    String date = jsonArray.getJSONObject(i).getString("t_date");
                                    String status = jsonArray.getJSONObject(i).getString("trns_desc");
                                    String facilityname = jsonArray.getJSONObject(i).getString("facilityname");
                                    String code = jsonArray.getJSONObject(i).getString("hfser_id") +"R"+ jsonArray.getJSONObject(i).getString("rgnid")+"-"+
                                            jsonArray.getJSONObject(i).getString("appid");
                                    String typefacility = jsonArray.getJSONObject(i).getString("hgpdesc");
                                    if(typefacility.equals("")){
                                        typefacility = "";
                                    }
                                    String appid = jsonArray.getJSONObject(i).getString("appid");
                                    if(id.equals("LTO") || id.equals("COA")){
                                        assesslist.add(new AssestmentModel(id,code,facilityname,typefacility,date,status,appid));
                                    }

                                }
                                /*aAdapter.notifyDataSetChanged();*/
                               setAdapter();
                            }else{
                                TextView lbl = findViewById(R.id.lblmessage);
                                lbl.setVisibility(View.VISIBLE);
                            }

                            if(assesslist.size()==0){
                                lbl.setVisibility(View.VISIBLE);
                                lbl.setText("No Data Available");
                            }

                            recyclerView.setVisibility(View.VISIBLE);
                            bar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                       Log.d("assess",response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }

    public void setAdapter(){
        aAdapter = new AssestmentAdapter(HomeActivity.this,assesslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        recyclerView.setAdapter(aAdapter);


        aAdapter.setonItemClickListener(new AssestmentAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent i = new Intent(HomeActivity.this, AssestmentDetailsActivity.class);

                HomeActivity.this.startActivity(i);
                Animatoo.animateSlideLeft(HomeActivity.this);
                code = assesslist.get(position).getCode();
                type = assesslist.get(position).getType();
                faclityname = assesslist.get(position).getFaclityname();
                typefacility = assesslist.get(position).getTypefacility();
                date = assesslist.get(position).getDate();
                status = assesslist.get(position).getStatus();
                appid = assesslist.get(position).getAppid();
                monType = "license";
                restpye = "assessment";
                licensetype = "assessment";
                finish();


            }


        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuInflater inflater = new MenuInflater(getApplicationContext());


        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                //aAdapter.getFilter().filter(query);

                    aAdapter.getFilter().filter(query);


                return true;
            }
        });



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {



            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        menuModel = new MenuModel("Sync Data", true, true,R.drawable.ic_sync_black_24dp,5);
        headerList.add(menuModel);
        childModel = new MenuModel("Save Assessment Data Online", false, false,0,0);
        childModelsList.add(childModel);
        childModel = new MenuModel("Save Monitoring Data Online", false, false,0,1);
        childModelsList.add(childModel);
        childModel = new MenuModel("Save Evaluation Data Online", false, false,0,2);
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Share Data", true, true,R.drawable.ic_share,6);
        headerList.add(menuModel);
        childModel = new MenuModel("Send Assessment Data", false, false,0,0);
        childModelsList.add(childModel);

        childModel = new MenuModel("Send Monitoring Data", false, false,0,1);
        childModelsList.add(childModel);

        childModel = new MenuModel("Send Evaluaation Data", false, false,0,2);
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("Merge Files", true, true,R.drawable.ic_sync_black_24dp,7);
        headerList.add(menuModel);
        childModelsList = new ArrayList<>();
        childModel = new MenuModel("Merge Asessment Data", false, false,0,0);
        childModelsList.add(childModel);
        childModel = new MenuModel("Merge Monitoring Data", false, false,0,1);
        childModelsList.add(childModel);
        childModel = new MenuModel("Merge Evaluation Data", false, false,0,2);
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

    private void merged_data(){

        try {
            viewTextWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

    private void viewTextWrapper() throws FileNotFoundException, DocumentException {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            Log.d("menuindex",menuindex+"");
            if(menuindex == 70){
                //merge_files();
                execute_merge();
            }else if(menuindex == 0){
                create_text_file();
                //create_file();
            }else if(menuindex == 1){
                create_montext_file();
            }else if(menuindex == 71){
                mergemon_files();
            }else if(menuindex ==2){
                create_etext_file();
            }else if(menuindex == 72){
                merge_efiles();
            }

        }



    }

    private void createTextWrapper() throws FileNotFoundException, DocumentException {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            Log.d("menuindex",menuindex+"");
            if(menuindex == 0){
                create_text_file();
            }else if(menuindex == 70){
                //merge_files();
                execute_merge();
            }else if(menuindex == 1){
                create_montext_file();
            }else if(menuindex == 71){
                mergemon_files();
            }else if(menuindex ==2){
                create_etext_file();
            }else if(menuindex == 72){
                merge_efiles();
            }

        }



    }

    private void create_etext_file(){

        try {

            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");

            if (!root.exists()) {
                root.mkdirs(); // this will create folder.
            }
            File filepath = new File(root, "evaluation.txt");  // file path to save

            FileWriter writer = new FileWriter(filepath);
            try {

                JSONObject files = new JSONObject();
                JSONArray filesarr = new JSONArray();
                JSONObject items = new JSONObject();
                Log.d("evaluation","execute");
                //assesscombined

                JSONArray arrcombined = new JSONArray();
                Cursor c = db.get_combinedptc(uid);
                //loop
                if(c!=null && c.getCount()>0){
                    c.moveToFirst();
                    while(!c.isAfterLast()){
                        JSONObject combineditems = new JSONObject();
                        combineditems.put("asmtComb_FK",c.getString(c.getColumnIndex("asmtComb_FK")));
                        combineditems.put("assessmentName",c.getString(c.getColumnIndex("assessmentName")));
                        combineditems.put("assessmentSeq",c.getString(c.getColumnIndex("assessmentSeq")));
                        combineditems.put("assessmentHead",c.getString(c.getColumnIndex("assessmentHead")));
                        combineditems.put("asmtH3ID_FK",c.getString(c.getColumnIndex("asmtH3ID_FK")));
                        combineditems.put("h3name",c.getString(c.getColumnIndex("h3name")));
                        combineditems.put("asmtH2ID_FK",c.getString(c.getColumnIndex("asmtH2ID_FK")));
                        combineditems.put("h2name",c.getString(c.getColumnIndex("h2name")));
                        combineditems.put("asmtH1ID_FK",c.getString(c.getColumnIndex("asmtH1ID_FK")));
                        combineditems.put("h1name",c.getString(c.getColumnIndex("h1name")));
                        combineditems.put("partID",(c.getString(c.getColumnIndex("partID"))!=null)?c.getString(c.getColumnIndex("partID")):"");
                        combineditems.put("parttitle",c.getString(c.getColumnIndex("parttitle")));
                        combineditems.put("evaluation",c.getString(c.getColumnIndex("evaluation")));
                        combineditems.put("remarks",c.getString(c.getColumnIndex("remarks")));
                        combineditems.put("evaluatedBy",c.getString(c.getColumnIndex("evaluatedBy")));
                        combineditems.put("appid",c.getString(c.getColumnIndex("appid")));
                        combineditems.put("sub",c.getString(c.getColumnIndex("sub")));
                        combineditems.put("isdisplay",c.getString(c.getColumnIndex("isdisplay")));
                        combineditems.put("revision",c.getString(c.getColumnIndex("revision")));
                        combineditems.put("ename",c.getString(c.getColumnIndex("ename")));
                        arrcombined.put(combineditems);
                        c.moveToNext();
                    }
                    Log.d("combineditems",arrcombined.toString());
                }
                //loop

                //assesscombined

                //assessheader
                JSONArray arrheader = new JSONArray();
                Cursor h = db.get_headers(uid);
                //loop
                if(h!=null && h.getCount()>0){
                    h.moveToFirst();
                    while(!h.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("headerid",h.getString(h.getColumnIndex("headerid")));
                        headeritems.put("headerlevel",h.getString(h.getColumnIndex("headerlevel")));
                        headeritems.put("assess",h.getString(h.getColumnIndex("assess")));
                        headeritems.put("appid",h.getString(h.getColumnIndex("appid")));
                        headeritems.put("uid",h.getString(h.getColumnIndex("uid")));
                        arrheader.put(headeritems);
                        h.moveToNext();
                    }
                    Log.d("headeritem",arrheader.toString());
                }

                //assessrecommend
                JSONArray arrrecommend = new JSONArray();
                Cursor r = db.get_recommend(uid);
                if(r!=null && r.getCount()>0){
                    r.moveToFirst();
                    while(!r.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("choice",r.getString(r.getColumnIndex("choice")));
                        headeritems.put("details",r.getString(r.getColumnIndex("details")));
                        headeritems.put("valfrom",r.getString(r.getColumnIndex("valfrom")));
                        headeritems.put("valto",r.getString(r.getColumnIndex("valto")));
                        headeritems.put("days",r.getString(r.getColumnIndex("days")));
                        headeritems.put("monid",r.getString(r.getColumnIndex("monid")));
                        headeritems.put("selfassess",r.getString(r.getColumnIndex("selfassess")));
                        headeritems.put("revision",r.getString(r.getColumnIndex("revision")));
                        headeritems.put("evaluatedby",r.getString(r.getColumnIndex("evaluatedby")));
                        headeritems.put("appid",r.getString(r.getColumnIndex("appid")));
                        headeritems.put("t_details",r.getString(r.getColumnIndex("t_details")));
                        headeritems.put("noofbed",(r.getString(r.getColumnIndex("noofbed"))!=null)?r.getString(r.getColumnIndex("noofbed")):"");
                        headeritems.put("noofdialysis",(r.getString(r.getColumnIndex("noofdialysis"))!=null)?r.getString(r.getColumnIndex("noofdialysis")):"");
                        headeritems.put("conforme",(r.getString(r.getColumnIndex("conforme"))!=null)?r.getString(r.getColumnIndex("conforme")):"");
                        headeritems.put("conformeDesignation",(r.getString(r.getColumnIndex("conformeDesignation"))!=null)?r.getString(r.getColumnIndex("conformeDesignation")):"");
                        headeritems.put("ename",r.getString(r.getColumnIndex("ename")));
                        arrrecommend.put(headeritems);
                        r.moveToNext();
                    }

                }
                Log.d("recommenditems",arrrecommend.toString());

                //loop

                //assessheader

                //items.put("appid",HomeActivity.appid);
                items.put("uid",uid);
                items.put("assesscombined",arrcombined);
                items.put("assessheader",arrheader);
                items.put("assessrecommend",arrrecommend);
                filesarr.put(items);
                files.put("files",filesarr);
                Log.d("filess",files.toString());
                String encrypted = "";
                String sourceStr = "This is any source string";
                String decrypted = "";

                try {
                    encrypted = AESUtils.encrypt(files.toString());
                    Log.d("TEST", "encrypted:" + encrypted);
                    writer.append(encrypted);
                    //writer.append(files.toString());
                    writer.flush();
                    writer.close();
                    Log.d("message","write successfully");

                } catch (Exception e) {
                    e.printStackTrace();

                }

                bluetoothFunctionality("assessment.txt");

            } catch (JSONException e) {
                e.printStackTrace();
            }



            //result.setText(m);



        } catch (IOException e) {

            e.printStackTrace();

            //result.setText(e.getMessage().toString());

        }




    }

    private void create_file(){


        try {

            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");

            if (!root.exists()) {

                root.mkdirs(); // this will create folder.

            }
            File filepath = new File(root, "doh.txt");  // file path to save

            FileWriter writer = new FileWriter(filepath);
            try {



                JSONObject files = new JSONObject();
                JSONArray filesarr = new JSONArray();
                JSONObject items = new JSONObject();

                //assesscombined

                JSONArray arrcombined = new JSONArray();
                Cursor c = db.get_combined(uid);
                //loop
                if(c!=null && c.getCount()>0){
                    c.moveToFirst();
                    while(!c.isAfterLast()){
                        JSONObject combineditems = new JSONObject();
                        combineditems.put("asmtComb_FK",c.getString(c.getColumnIndex("asmtComb_FK")));
                        combineditems.put("assessmentName",c.getString(c.getColumnIndex("assessmentName")));
                        combineditems.put("assessmentSeq",c.getString(c.getColumnIndex("assessmentSeq")));
                        combineditems.put("assessmentHead",c.getString(c.getColumnIndex("assessmentHead")));
                        combineditems.put("asmtH3ID_FK",c.getString(c.getColumnIndex("asmtH3ID_FK")));
                        combineditems.put("h3name",c.getString(c.getColumnIndex("h3name")));
                        combineditems.put("asmtH2ID_FK",c.getString(c.getColumnIndex("asmtH2ID_FK")));
                        combineditems.put("h2name",c.getString(c.getColumnIndex("h2name")));
                        combineditems.put("asmtH1ID_FK",c.getString(c.getColumnIndex("asmtH1ID_FK")));
                        combineditems.put("h1name",c.getString(c.getColumnIndex("h1name")));
                        combineditems.put("partID",c.getString(c.getColumnIndex("partID")));
                        combineditems.put("evaluation",c.getString(c.getColumnIndex("evaluation")));
                        combineditems.put("remarks",c.getString(c.getColumnIndex("remarks")));
                        combineditems.put("evaluatedBy",c.getString(c.getColumnIndex("evaluatedBy")));
                        combineditems.put("appid",c.getString(c.getColumnIndex("appid")));
                        combineditems.put("monid",c.getString(c.getColumnIndex("monid")));
                        arrcombined.put(combineditems);
                        c.moveToNext();
                    }
                }
                //loop

                //assesscombined

                //assessheader
                JSONArray arrheader = new JSONArray();
                Cursor h = db.get_headers(uid);
                //loop
                if(h!=null && h.getCount()>0){
                    h.moveToFirst();
                    while(!h.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("headerid",h.getString(h.getColumnIndex("headerid")));
                        headeritems.put("headerlevel",h.getString(h.getColumnIndex("headerlevel")));
                        headeritems.put("assess",h.getString(h.getColumnIndex("assess")));
                        headeritems.put("appid",h.getString(h.getColumnIndex("appid")));
                        headeritems.put("uid",h.getString(h.getColumnIndex("uid")));
                        arrheader.put(headeritems);
                        h.moveToNext();
                    }

                }
                //loop

                //assessheader

                //items.put("appid",HomeActivity.appid);
                items.put("uid",uid);
                items.put("assesscombined",arrcombined);
                items.put("assessheader",arrheader);
                filesarr.put(items);
                files.put("files",filesarr);
                Log.d("filess",files.toString());
                String encrypted = "";
                String sourceStr = "This is any source string";
                String decrypted = "";
                try {
                    encrypted = AESUtils.encrypt(files.toString());
                    Log.d("TEST", "encrypted:" + encrypted);
                    writer.append(encrypted);
                    writer.flush();
                    writer.close();
                    Log.d("message","write successfully");

                } catch (Exception e) {
                    e.printStackTrace();

                }

                bluetoothFunctionality("");
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //result.setText(m);



        } catch (IOException e) {

            e.printStackTrace();

            //result.setText(e.getMessage().toString());

        }
    }


    private void create_text_file(){

        try {

            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");

            if (!root.exists()) {

                root.mkdirs(); // this will create folder.

            }
            File filepath = new File(root, "assessment.txt");  // file path to save

            FileWriter writer = new FileWriter(filepath);
            try {

                JSONObject files = new JSONObject();
                JSONArray filesarr = new JSONArray();
                JSONObject items = new JSONObject();

                //assesscombined

                JSONArray arrcombined = new JSONArray();
                Cursor c = db.get_combined(uid);
                //loop
                if(c!=null && c.getCount()>0){
                    c.moveToFirst();
                    while(!c.isAfterLast()){
                        JSONObject combineditems = new JSONObject();
                        combineditems.put("asmtComb_FK",c.getString(c.getColumnIndex("asmtComb_FK")));
                        combineditems.put("assessmentName",c.getString(c.getColumnIndex("assessmentName")));
                        combineditems.put("assessmentSeq",c.getString(c.getColumnIndex("assessmentSeq")));
                        combineditems.put("assessmentHead",c.getString(c.getColumnIndex("assessmentHead")));
                        combineditems.put("asmtH3ID_FK",c.getString(c.getColumnIndex("asmtH3ID_FK")));
                        combineditems.put("h3name",c.getString(c.getColumnIndex("h3name")));
                        combineditems.put("asmtH2ID_FK",c.getString(c.getColumnIndex("asmtH2ID_FK")));
                        combineditems.put("h2name",c.getString(c.getColumnIndex("h2name")));
                        combineditems.put("asmtH1ID_FK",c.getString(c.getColumnIndex("asmtH1ID_FK")));
                        combineditems.put("h1name",c.getString(c.getColumnIndex("h1name")));
                        combineditems.put("partID",c.getString(c.getColumnIndex("partID")));
                        combineditems.put("evaluation",c.getString(c.getColumnIndex("evaluation")));
                        combineditems.put("remarks",c.getString(c.getColumnIndex("remarks")));
                        combineditems.put("evaluatedBy",c.getString(c.getColumnIndex("evaluatedBy")));
                        combineditems.put("appid",c.getString(c.getColumnIndex("appid")));
                        combineditems.put("monid",c.getString(c.getColumnIndex("monid")));
                        combineditems.put("ename",c.getString(c.getColumnIndex("ename")));
                        arrcombined.put(combineditems);
                        c.moveToNext();
                    }
                    Log.d("combineditems",arrcombined.toString());
                }
                //loop

                //assesscombined

                //assessheader
                JSONArray arrheader = new JSONArray();
                Cursor h = db.get_headers(uid);
                //loop
                if(h!=null && h.getCount()>0){
                    h.moveToFirst();
                    while(!h.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("headerid",h.getString(h.getColumnIndex("headerid")));
                        headeritems.put("headerlevel",h.getString(h.getColumnIndex("headerlevel")));
                        headeritems.put("assess",h.getString(h.getColumnIndex("assess")));
                        headeritems.put("appid",h.getString(h.getColumnIndex("appid")));
                        headeritems.put("uid",h.getString(h.getColumnIndex("uid")));
                        arrheader.put(headeritems);
                        h.moveToNext();
                    }
                    Log.d("headeritems",arrheader.toString());

                }
                //assessrecommend
                JSONArray arrrecommend = new JSONArray();
                Cursor r = db.get_recommend(uid);
                if(r!=null && r.getCount()>0){
                    r.moveToFirst();
                    while(!r.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("choice",r.getString(r.getColumnIndex("choice")));
                        headeritems.put("details",r.getString(r.getColumnIndex("details")));
                        headeritems.put("valfrom",r.getString(r.getColumnIndex("valfrom")));
                        headeritems.put("valto",r.getString(r.getColumnIndex("valto")));
                        headeritems.put("days",r.getString(r.getColumnIndex("days")));
                        headeritems.put("monid",r.getString(r.getColumnIndex("monid")));
                        headeritems.put("selfassess",r.getString(r.getColumnIndex("selfassess")));
                        headeritems.put("revision",r.getString(r.getColumnIndex("revision")));
                        headeritems.put("evaluatedby",r.getString(r.getColumnIndex("evaluatedby")));
                        headeritems.put("appid",r.getString(r.getColumnIndex("appid")));
                        headeritems.put("t_details",r.getString(r.getColumnIndex("t_details")));
                        headeritems.put("noofbed",r.getString(r.getColumnIndex("noofbed")));
                        headeritems.put("noofdialysis",r.getString(r.getColumnIndex("noofdialysis")));
                        headeritems.put("conforme",r.getString(r.getColumnIndex("conforme")));
                        headeritems.put("conformeDesignation",r.getString(r.getColumnIndex("conformeDesignation")));
                        headeritems.put("ename",r.getString(r.getColumnIndex("ename")));
                        Log.d("evaluatedby",r.getString(r.getColumnIndex("evaluatedby")));
                        arrrecommend.put(headeritems);
                        r.moveToNext();
                    }
                    Log.d("recitems",arrrecommend.toString());

                }
                //loop

                //assessheader

                //items.put("appid",HomeActivity.appid);
                items.put("uid",uid);
                items.put("assesscombined",arrcombined);
                items.put("assessheader",arrheader);
                items.put("assessrecommend",arrrecommend);
                filesarr.put(items);
                files.put("files",filesarr);
                Log.d("filess",files.toString());
                String encrypted = "";
                String sourceStr = "This is any source string";
                String decrypted = "";

                try {
                    encrypted = AESUtils.encrypt(files.toString());
                    Log.d("TEST", "encrypted:" + encrypted);
                    writer.append(encrypted);
                    //writer.append(files.toString());
                    writer.flush();
                    writer.close();
                    Log.d("message","write successfully");

                } catch (Exception e) {
                    e.printStackTrace();

                }

                bluetoothFunctionality("assessment.txt");

            } catch (JSONException e) {
                e.printStackTrace();
            }



            //result.setText(m);



        } catch (IOException e) {

            e.printStackTrace();

            //result.setText(e.getMessage().toString());

        }



    }

    private void create_montext_file(){


        try {

            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");

            if (!root.exists()) {

                root.mkdirs(); // this will create folder.

            }
            Log.d("montext","execute");
            File filepath = new File(root, "monitoring.txt");  // file path to save

            FileWriter writer = new FileWriter(filepath);
            try {
                JSONObject files = new JSONObject();
                JSONArray filesarr = new JSONArray();
                JSONObject items = new JSONObject();

                //assesscombined

                JSONArray arrcombined = new JSONArray();
                Cursor c = db.get_combined_mon(uid);
                //loop
                if(c!=null && c.getCount()>0){
                    c.moveToFirst();
                    while(!c.isAfterLast()){
                        JSONObject combineditems = new JSONObject();

                        combineditems.put("asmtComb_FK",c.getString(c.getColumnIndex("asmtComb_FK")));
                        combineditems.put("assessmentName",c.getString(c.getColumnIndex("assessmentName")));
                        combineditems.put("assessmentSeq",c.getString(c.getColumnIndex("assessmentSeq")));
                        combineditems.put("assessmentHead",c.getString(c.getColumnIndex("assessmentHead")));
                        combineditems.put("asmtH3ID_FK",c.getString(c.getColumnIndex("asmtH3ID_FK")));
                        combineditems.put("h3name",c.getString(c.getColumnIndex("h3name")));
                        combineditems.put("asmtH2ID_FK",c.getString(c.getColumnIndex("asmtH2ID_FK")));
                        combineditems.put("h2name",c.getString(c.getColumnIndex("h2name")));
                        combineditems.put("asmtH1ID_FK",c.getString(c.getColumnIndex("asmtH1ID_FK")));
                        combineditems.put("h1name",c.getString(c.getColumnIndex("h1name")));
                        combineditems.put("partID",(c.getString(c.getColumnIndex("partID"))!=null)?c.getString(c.getColumnIndex("partID")):"");
                        combineditems.put("evaluation",c.getString(c.getColumnIndex("evaluation")));
                        combineditems.put("remarks",c.getString(c.getColumnIndex("remarks")));
                        combineditems.put("evaluatedBy",c.getString(c.getColumnIndex("evaluatedBy")));
                        combineditems.put("appid",c.getString(c.getColumnIndex("appid")));
                        combineditems.put("monid",c.getString(c.getColumnIndex("monid")));
                        combineditems.put("ename",c.getString(c.getColumnIndex("ename")));
                        arrcombined.put(combineditems);
                        c.moveToNext();
                    }
                }
                //loop

                //assesscombined

                //assessheader
                JSONArray arrheader = new JSONArray();
                Cursor h = db.get_headers_mon(uid);
                //loop
                if(h!=null && h.getCount()>0){
                    h.moveToFirst();
                    while(!h.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("headerid",h.getString(h.getColumnIndex("headerid")));
                        headeritems.put("headerlevel",h.getString(h.getColumnIndex("headerlevel")));
                        headeritems.put("assess",h.getString(h.getColumnIndex("assess")));
                        headeritems.put("appid",h.getString(h.getColumnIndex("appid")));
                        headeritems.put("monid",h.getString(h.getColumnIndex("monid")));
                        headeritems.put("uid",h.getString(h.getColumnIndex("uid")));
                        arrheader.put(headeritems);
                        h.moveToNext();
                    }

                }

                //assessrecommend
                JSONArray arrrecommend = new JSONArray();
                Cursor r = db.get_recommend(uid);
                if(r!=null && r.getCount()>0){
                    r.moveToFirst();
                    while(!r.isAfterLast()){
                        JSONObject headeritems = new JSONObject();
                        headeritems.put("choice",r.getString(r.getColumnIndex("choice")));
                        headeritems.put("details",r.getString(r.getColumnIndex("details")));
                        headeritems.put("valfrom",r.getString(r.getColumnIndex("valfrom")));
                        headeritems.put("valto",r.getString(r.getColumnIndex("valto")));
                        headeritems.put("days",r.getString(r.getColumnIndex("days")));
                        headeritems.put("monid",r.getString(r.getColumnIndex("monid")));
                        headeritems.put("selfassess",r.getString(r.getColumnIndex("selfassess")));
                        headeritems.put("revision",r.getString(r.getColumnIndex("revision")));
                        headeritems.put("evaluatedby",r.getString(r.getColumnIndex("evaluatedby")));
                        headeritems.put("appid",r.getString(r.getColumnIndex("appid")));
                        headeritems.put("t_details",r.getString(r.getColumnIndex("t_details")));
                        headeritems.put("noofbed",r.getString(r.getColumnIndex("noofbed")));
                        headeritems.put("noofdialysis",r.getString(r.getColumnIndex("noofdialysis")));
                        headeritems.put("conforme",r.getString(r.getColumnIndex("conforme")));
                        headeritems.put("conformeDesignation",r.getString(r.getColumnIndex("conformeDesignation")));
                        headeritems.put("ename",r.getString(r.getColumnIndex("ename")));
                        arrrecommend.put(headeritems);
                        r.moveToNext();
                    }

                }

                //loop

                //assessheader

                //items.put("appid",HomeActivity.appid);
                items.put("uid",uid);
                items.put("assesscombined",arrcombined);
                items.put("assessheader",arrheader);
                items.put("assessrecommend",arrrecommend);
                filesarr.put(items);
                files.put("files",filesarr);
                Log.d("filess",files.toString());
                String encrypted = "";
                String sourceStr = "This is any source string";
                String decrypted = "";

                try {
                    encrypted = AESUtils.encrypt(files.toString());
                    Log.d("TEST", "encrypted:" + encrypted);
                    writer.append(encrypted);
                    //writer.append(files.toString());
                    writer.flush();
                    writer.close();
                    Log.d("message","write successfully");

                } catch (Exception e) {
                    e.printStackTrace();

                }

                bluetoothFunctionality("monitoring.txt");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private Boolean checkConflict(JSONArray combined){
        Boolean check = false;
        try{
            if(combined.length()>0){
                for(int com=0;com<combined.length();com++){
                    String asmtComb_FK = combined.getJSONObject(com).getString("asmtComb_FK");
                    String asmtH1ID_FK = combined.getJSONObject(com).getString("asmtH1ID_FK");
                    String evaluatedBy = combined.getJSONObject(com).getString("evaluatedBy");
                    String appid = combined.getJSONObject(com).getString("appid");
                    String dupid = db.checkassesscombined(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK);
                    if (db.checkDatas("assesscombined", "dupID", dupid)){
                        Log.d("found","true");
                        check = true;
                        break;
                    }
                }
            }
        }catch (Exception ex){

        }
        return  check;
    }
    private void execute_merge(){
        //File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        String[] paths = {Environment.getExternalStorageDirectory().getPath()+"/Bluetooth","storage/sdcard1/bluetooth",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+""};
        String [] filename = {"assessment.txt","assessment-1.txt","assessment-2.txt","assessment-3.txt","assessment-4.txt","assessment-5.txt","assessment-6.txt","assessment-7.txt","assessment-8.txt","assessment-9.txt","assessment-10.txt"};
        int check = 0;
        String jsoncheck = "";
        String json = "";
        db.delete("assesscombinedtemp","","");
        AlertDialog.Builder message = new AlertDialog.Builder(HomeActivity.this);
        message.setTitle("DOHOLRS");
        int countcon = 0;
        int cnttemp = 0;
        //count the of path has no file to be merge.
        int checkmerge = 0;
        ProgressDialog pdialog;
        pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                "Merging Data. Please wait...", true);
        try{
            for(int i=0;i<paths.length;i++)
            {
                int cn = 0;
                for(int f=0;f<filename.length;f++)
                {
                    jsoncheck = loadJSON(paths[i],filename[f]);
                    Boolean checkCon  = false;

                    if(!jsoncheck.equals("")){
                        Log.d("jsoncheck",jsoncheck);
                        try {
                            String decrypted = AESUtils.decrypt(jsoncheck);
                            json = decrypted;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(!json.equals("")){
                            try {
                                JSONObject obj = new JSONObject(json);
                                Log.d("json",obj.toString());
                                JSONArray jsonArray = obj.getJSONArray("files");

                                for(int o=0;o<jsonArray.length();o++){
                                    String uid = jsonArray.getJSONObject(o).getString("uid");
                                    JSONArray combined = jsonArray.getJSONObject(o).getJSONArray("assesscombined");
                                    //insert combined
                                /*
                                if(!checkConflict(combined)){


                                }else{
                                    checkCon = true;
                                    break;
                                }*/

                                    if(combined.length()>0){
                                        for(int com=0;com<combined.length();com++){
                                            String asmtComb_FK = combined.getJSONObject(com).getString("asmtComb_FK");
                                            String assessmentName = combined.getJSONObject(com).getString("assessmentName");
                                            String assessmentSeq = combined.getJSONObject(com).getString("assessmentSeq");
                                            String assessmentHead = combined.getJSONObject(com).getString("assessmentHead");
                                            String asmtH3ID_FK = combined.getJSONObject(com).getString("asmtH3ID_FK");
                                            String h3name = combined.getJSONObject(com).getString("h3name");
                                            String asmtH2ID_FK = combined.getJSONObject(com).getString("asmtH2ID_FK");
                                            String h2name = combined.getJSONObject(com).getString("h2name");
                                            String asmtH1ID_FK = combined.getJSONObject(com).getString("asmtH1ID_FK");
                                            String h1name = combined.getJSONObject(com).getString("h1name");
                                            String partid = combined.getJSONObject(com).getString("partID");
                                            String evaluation = combined.getJSONObject(com).getString("evaluation");
                                            String remarks = combined.getJSONObject(com).getString("remarks");
                                            String evaluatedBy = combined.getJSONObject(com).getString("evaluatedBy");
                                            String ename = combined.getJSONObject(com).getString("ename");
                                            String appid = combined.getJSONObject(com).getString("appid");
                                            String dupid = db.get_tbl_assesscombined(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK);
                                            if (db.checkDatas("assesscombined", "dupID", dupid)){
                                                String duptid = "";
                                                Log.d("found","true");
                                                //insert in temp data
                                                //insert the data first from the database
                                                String[] acolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                                                        "evaluation","remarks","evaluatedBy","appid","monid","ename"};
                                                Cursor c = db.get_assesscombineddata(dupid);
                                                if(c!=null && c.getCount()>0){
                                                    c.moveToFirst();
                                                    while (!c.isAfterLast()){
                                                        duptid = db.get_tbl_assesscombinedtemp(c.getString(c.getColumnIndex("appid")),c.getString(c.getColumnIndex("evaluatedBy")),
                                                                c.getString(c.getColumnIndex("asmtComb_FK")), "", "",
                                                                c.getString(c.getColumnIndex("asmtH1ID_FK")));
                                                        String[] datas =
                                                                {
                                                                        c.getString(c.getColumnIndex("asmtComb_FK")),
                                                                        c.getString(c.getColumnIndex("assessmentName")),
                                                                        c.getString(c.getColumnIndex("assessmentSeq")),
                                                                        c.getString(c.getColumnIndex("assessmentHead")),
                                                                        c.getString(c.getColumnIndex("asmtH3ID_FK")),
                                                                        c.getString(c.getColumnIndex("h3name")),
                                                                        c.getString(c.getColumnIndex("asmtH2ID_FK")),
                                                                        c.getString(c.getColumnIndex("h2name")),
                                                                        c.getString(c.getColumnIndex("asmtH1ID_FK")),
                                                                        c.getString(c.getColumnIndex("h1name")),
                                                                        c.getString(c.getColumnIndex("partID")),
                                                                        c.getString(c.getColumnIndex("evaluation")),
                                                                        c.getString(c.getColumnIndex("remarks")),
                                                                        c.getString(c.getColumnIndex("evaluatedBy")),
                                                                        c.getString(c.getColumnIndex("appid")),
                                                                        c.getString(c.getColumnIndex("monid")),
                                                                        c.getString(c.getColumnIndex("ename"))
                                                                };
                                                        countcon++;
                                                        if (!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                            if (db.add("assesscombinedtemp", acolumns, datas, "")) {
                                                                Log.d("assesscombinedtemp", "added");
                                                            } else {
                                                                Log.d("assesscombinedtemp", "not added");
                                                            }
                                                        }else{cnttemp++;}

                                                        c.moveToNext();
                                                    }
                                                }

                                                //insert merge data from the database
                                                duptid = db.get_tbl_assesscombinedtemp(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK);
                                                String[] adatas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partid,
                                                        evaluation,remarks,evaluatedBy,appid,"",ename};

                                                countcon++;
                                                if (!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                    if (db.add("assesscombinedtemp", acolumns, adatas, "")) {
                                                        Log.d("assesscombinedtemp", "added");
                                                    } else {
                                                        Log.d("assesscombinedtemp", "not added");
                                                    }
                                                }else{cnttemp++;}

                                            }else{
                                                //for assesscombined not found in the database
                                                Log.d("found","false");
                                                String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                                                        "evaluation","remarks","evaluatedBy","appid","monid","ename"};
                                                String[] datas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partid,
                                                        evaluation,remarks,evaluatedBy,appid,"",ename};
                                                if (db.add("assesscombined", dcolumns, datas, "")) {
                                                    Log.d("assesscombined", "added");
                                                } else {
                                                    Log.d("assesscombined", "not added");
                                                }
                                            }
                                        }
                                    }
                                    //end of insert combined
                                    //insert header
                                    JSONArray header = jsonArray.getJSONObject(0).getJSONArray("assessheader");
                                    if(header.length()>0){
                                        for(int h=0;h<header.length();h++){
                                            String headerid = header.getJSONObject(h).getString("headerid");
                                            String headerlevel = header.getJSONObject(h).getString("headerlevel");
                                            String assess = header.getJSONObject(h).getString("assess");
                                            String appid = header.getJSONObject(h).getString("appid");
                                            String uids = header.getJSONObject(h).getString("uid");
                                            String id = db.get_tbl_assessment_header(appid,uids,headerid,headerlevel);
                                            if (!db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
                                                Log.d("found","false");
                                                String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid"};
                                                String[] datas = {headerid,headerlevel,assess,appid,uids};
                                                if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                                                    Log.d("tblsaveassessheader", "added");
                                                } else {
                                                    Log.d("tblsaveassessheader", "not added");
                                                }
                                            }else{
                                                Log.d("found","true");
                                            }
                                        }
                                    }
                                    //end of insert header
                                    //insert recommend
                                    JSONArray recommend = jsonArray.getJSONObject(0).getJSONArray("assessrecommend");
                                    if(recommend.length()>0){
                                        for(int r=0;r<recommend.length();r++){
                                            String choice = recommend.getJSONObject(r).getString("choice");
                                            String details = recommend.getJSONObject(r).getString("details");
                                            String valfrom = recommend.getJSONObject(r).getString("valfrom");
                                            String valto = recommend.getJSONObject(r).getString("valto");
                                            String days = recommend.getJSONObject(r).getString("days");
                                            String monid = recommend.getJSONObject(r).getString("monid");
                                            String selfassess = recommend.getJSONObject(r).getString("selfassess");
                                            String revision = recommend.getJSONObject(r).getString("revision");
                                            String evaluatedby = recommend.getJSONObject(r).getString("evaluatedby");
                                            String appid = recommend.getJSONObject(r).getString("appid");
                                            String t_details = recommend.getJSONObject(r).getString("t_details");
                                            String noofbed = recommend.getJSONObject(r).getString("noofbed");
                                            String noofdialysis = recommend.getJSONObject(r).getString("noofdialysis");
                                            String conforme = recommend.getJSONObject(r).getString("conforme");
                                            String conformeDesignation = recommend.getJSONObject(r).getString("conformeDesignation");
                                            String ename = recommend.getJSONObject(r).getString("ename");
                                            if (!db.checkDatas("assessrecommend", "appid", appid)){
                                                Log.d("found","false");
                                                String[] dcolumns = {"choice", "details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid","t_details","noofbed","noofdialysis","conforme",
                                                        "conformeDesignation","ename"};
                                                String[] datas = {choice,details,valfrom,valto,days,monid,selfassess,revision,evaluatedby,appid,t_details,noofbed,noofdialysis,conforme,conformeDesignation,ename};
                                                if (db.add("assessrecommend", dcolumns, datas, "")) {
                                                    Log.d("assessrecommend", "added");
                                                } else {
                                                    Log.d("assessrecommend", "not added");
                                                }
                                            }else{
                                                Log.d("found","true");
                                            }
                                        }
                                    }
                                    //end of recommned
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }else{
                        cn++;
                    }
                }
                if(cn == 11){ checkmerge++; }
            }

            //after all merge
            if(countcon != 0 && countcon != cnttemp){

                message.setMessage("There are some data have the same assessments answer.Please determine what data do you want to merge.");
                message.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent comp = new Intent(getApplicationContext(),compareactivity.class);
                        comp.putExtra("merge","assess");
                        startActivity(comp);
                        Animatoo.animateSlideLeft(HomeActivity.this);
                    }
                });

                //message.setMessage("Successfully Merged Files");
                //message.setNeutralButton("OK",null);
            }else if(checkmerge == 3){
                message.setMessage("No file to be merged.");
                message.setNeutralButton("OK",null);
            }else{
                message.setMessage("Successfully Merged Files");
                message.setNeutralButton("OK",null);
            }
            pdialog.dismiss();
            AlertDialog dialog3 = message.create();
            dialog3.setIcon(R.drawable.doh);
            dialog3.show();

        }catch (Exception ex){
            pdialog.dismiss();
            Toast.makeText(getApplicationContext(),"Error " + ex.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    private void merge_efiles(){
        //File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        String[] paths = {Environment.getExternalStorageDirectory().getPath()+"/Bluetooth","storage/sdcard1/bluetooth",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+""};
        String [] filename = {"evaluation.txt","evaluation-1.txt","evaluation-2.txt","evaluation-3.txt","evaluation-4.txt","evaluation-5.txt","evaluation-6.txt","evaluation-7.txt","evaluation-8.txt","evaluation-9.txt","evaluation-10.txt"};
        int check = 0;
        String jsoncheck = "";
        String json = "";
        db.delete("assesscombinedtemp","","");
        AlertDialog.Builder message = new AlertDialog.Builder(HomeActivity.this);
        message.setTitle("DOHOLRS");
        int countcon = 0;
        int cnttemp = 0;
        int checkmerge = 0;
        ProgressDialog pdialog;
        pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                "Merging Data. Please wait...", true);
        try{
            for(int i=0;i<paths.length;i++){
                int cn=0;
                for(int f=0;f<filename.length;f++){
                    jsoncheck = loadJSON(paths[i],filename[f]);
                    //AlertDialog.Builder message = new AlertDialog.Builder(HomeActivity.this);
                    //message.setTitle("DOHOLRS");
                    if(!jsoncheck.equals("")){
                        Log.d("jsoncheck",jsoncheck);
                        try {
                            String decrypted = AESUtils.decrypt(jsoncheck);
                            json = decrypted;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(!json.equals("")){
                            try {
                                JSONObject obj = new JSONObject(json);
                                Log.d("json",obj.toString());
                                JSONArray jsonArray = obj.getJSONArray("files");
                                for(int o=0;o<jsonArray.length();o++){
                                    String uid = jsonArray.getJSONObject(o).getString("uid");
                                    JSONArray combined = jsonArray.getJSONObject(o).getJSONArray("assesscombined");
                                    //insert combined
                                    for(int com=0;com<combined.length();com++){
                                        String asmtComb_FK = combined.getJSONObject(com).getString("asmtComb_FK");
                                        String assessmentName = combined.getJSONObject(com).getString("assessmentName");
                                        String assessmentSeq = combined.getJSONObject(com).getString("assessmentSeq");
                                        String assessmentHead = combined.getJSONObject(com).getString("assessmentHead");
                                        String asmtH3ID_FK = combined.getJSONObject(com).getString("asmtH3ID_FK");
                                        String h3name = combined.getJSONObject(com).getString("h3name");
                                        String asmtH2ID_FK = combined.getJSONObject(com).getString("asmtH2ID_FK");
                                        String h2name = combined.getJSONObject(com).getString("h2name");
                                        String asmtH1ID_FK = combined.getJSONObject(com).getString("asmtH1ID_FK");
                                        String h1name = combined.getJSONObject(com).getString("h1name");
                                        String partID = combined.getJSONObject(com).getString("partID");
                                        String parttitle = combined.getJSONObject(com).getString("parttitle");
                                        String evaluation = combined.getJSONObject(com).getString("evaluation");
                                        String remarks = combined.getJSONObject(com).getString("remarks");
                                        String evaluatedBy = combined.getJSONObject(com).getString("evaluatedBy");
                                        String ename = combined.getJSONObject(com).getString("ename");
                                        String appid = combined.getJSONObject(com).getString("appid");
                                        String sub = combined.getJSONObject(com).getString("sub");
                                        String isdisplay = combined.getJSONObject(com).getString("isdisplay");
                                        String revision = combined.getJSONObject(com).getString("revision");
                                        String dupid = db.get_tbl_assesscombinedptc(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK);
                                        String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID","parttitle",
                                                "evaluation","remarks","evaluatedBy","appid","sub","isdisplay","revision","ename"};
                                        String[] datas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partID,parttitle,
                                                evaluation,remarks,evaluatedBy,appid,sub,isdisplay,revision,ename};
                                        if (db.checkDatas("assesscombinedptc", "dupID", dupid)){
                                            String duptid = "";
                                            Log.d("found","true");
                                            //insert in temp data
                                            String[] acolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                                                    "evaluation","remarks","evaluatedBy","appid","monid","ename"};
                                            Cursor c = db.get_assesscombinedptcdata(dupid);
                                            if(c!=null && c.getCount()>0){
                                                c.moveToFirst();
                                                while (!c.isAfterLast()){
                                                    duptid = db.get_tbl_assesscombinedtemp(c.getString(c.getColumnIndex("appid")),
                                                            c.getString(c.getColumnIndex("evaluatedBy")),
                                                            c.getString(c.getColumnIndex("asmtComb_FK")), "", "",
                                                            c.getString(c.getColumnIndex("asmtH1ID_FK")));
                                                    String[] datass =
                                                            {
                                                                    c.getString(c.getColumnIndex("asmtComb_FK")),
                                                                    c.getString(c.getColumnIndex("assessmentName")),
                                                                    c.getString(c.getColumnIndex("assessmentSeq")),
                                                                    c.getString(c.getColumnIndex("assessmentHead")),
                                                                    c.getString(c.getColumnIndex("asmtH3ID_FK")),
                                                                    c.getString(c.getColumnIndex("h3name")),
                                                                    c.getString(c.getColumnIndex("asmtH2ID_FK")),
                                                                    c.getString(c.getColumnIndex("h2name")),
                                                                    c.getString(c.getColumnIndex("asmtH1ID_FK")),
                                                                    c.getString(c.getColumnIndex("h1name")),
                                                                    c.getString(c.getColumnIndex("partID")),
                                                                    c.getString(c.getColumnIndex("evaluation")),
                                                                    c.getString(c.getColumnIndex("remarks")),
                                                                    c.getString(c.getColumnIndex("evaluatedBy")),
                                                                    c.getString(c.getColumnIndex("appid")),
                                                                    "",
                                                                    c.getString(c.getColumnIndex("ename"))
                                                            };
                                                    countcon++;
                                                    if (!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                        if (db.add("assesscombinedtemp", acolumns, datass, "")) {
                                                            Log.d("assesscombinedtemp", "added");
                                                        } else {
                                                            Log.d("assesscombinedtemp", "not added");
                                                        }
                                                    }else{cnttemp++;}
                                                    c.moveToNext();
                                                }
                                            }
                                            duptid = db.get_tbl_assesscombinedtemp(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK);
                                            String[] adatas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partID,
                                                    evaluation,remarks,evaluatedBy,appid,"",ename};
                                            countcon++;
                                            if (!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                if (db.add("assesscombinedtemp", acolumns, adatas, "")) {
                                                    Log.d("assesscombinedtemp", "added");
                                                } else {
                                                    Log.d("assesscombinedtemp", "not added");
                                                }
                                            }else{cnttemp++;}
                                        }else{
                                            Log.d("found","false");
                                            if (db.add("assesscombinedptc", dcolumns, datas, "")) {
                                                Log.d("assesscombinedptc", "added");
                                            } else {
                                                Log.d("assesscombinedptc", "not added");
                                            }
                                        }
                                    }
                                    //end of insert combined
                                    //insert header
                                    JSONArray header = jsonArray.getJSONObject(0).getJSONArray("assessheader");
                                    for(int h=0;h<header.length();h++){
                                        String headerid = header.getJSONObject(h).getString("headerid");
                                        String headerlevel = header.getJSONObject(h).getString("headerlevel");
                                        String assess = header.getJSONObject(h).getString("assess");
                                        String appid = header.getJSONObject(h).getString("appid");
                                        String uids = header.getJSONObject(h).getString("uid");
                                        String id = db.get_tbl_assessment_header(appid,uids,headerid,headerlevel);
                                        if (!db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
                                            Log.d("found","false");
                                            String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid"};
                                            String[] datas = {headerid,headerlevel,assess,appid,uids};
                                            if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                                                Log.d("tblsaveassessheader", "added");
                                            } else {
                                                Log.d("tblsaveassessheader", "not added");
                                            }
                                        }else{
                                            Log.d("found","true");
                                        }
                                    }
                                    //end of insert header
                                    //insert recommend
                                    JSONArray recommend = jsonArray.getJSONObject(0).getJSONArray("assessrecommend");
                                    for(int r=0;r<recommend.length();r++){
                                        String choice = recommend.getJSONObject(r).getString("choice");
                                        String details = recommend.getJSONObject(r).getString("details");
                                        String valfrom = recommend.getJSONObject(r).getString("valfrom");
                                        String valto = recommend.getJSONObject(r).getString("valto");
                                        String days = recommend.getJSONObject(r).getString("days");
                                        String monid = recommend.getJSONObject(r).getString("monid");
                                        String selfassess = recommend.getJSONObject(r).getString("selfassess");
                                        String revision = recommend.getJSONObject(r).getString("revision");
                                        String evaluatedby = recommend.getJSONObject(r).getString("evaluatedby");
                                        String ename = recommend.getJSONObject(r).getString("ename");
                                        String appid = recommend.getJSONObject(r).getString("appid");
                                        String t_details = recommend.getJSONObject(r).getString("t_details");
                                        String noofbed = recommend.getJSONObject(r).getString("noofbed");
                                        String noofdialysis = recommend.getJSONObject(r).getString("noofdialysis");
                                        String conforme = recommend.getJSONObject(r).getString("conforme");
                                        String conformeDesignation = recommend.getJSONObject(r).getString("conformeDesignation");
                                        if (!db.checkDatas("assessrecommend", "appid", appid)){
                                            Log.d("found","false");
                                            String[] dcolumns = {"choice", "details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid","t_details",
                                                    "noofbed","noofdialysis","conforme","conformeDesignation","ename"};
                                            String[] datas = {choice,details,valfrom,valto,days,monid,selfassess,revision,evaluatedby,appid,t_details,noofbed,noofdialysis,conforme,conformeDesignation,ename};
                                            if (db.add("assessrecommend", dcolumns, datas, "")) {
                                                Log.d("assessrecommend", "added");
                                            } else {
                                                Log.d("assessrecommend", "not added");
                                            }
                                        }else{
                                            Log.d("found","true");
                                        }
                                    }
                                    //end of recommned


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }else{
                        cn++;
                    }
                }
                if(cn == 11){checkmerge++;}
            }
            Log.d("count",countcon+"");
            //after all merge
            if(countcon != 0 && cnttemp != countcon){
                message.setMessage("There are some data have the same assessments answer.Please determine what data do you want to merge.");
                message.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent comp = new Intent(getApplicationContext(),compareactivity.class);
                        comp.putExtra("merge","evaluate");
                        startActivity(comp);
                        Animatoo.animateSlideLeft(HomeActivity.this);
                    }
                });
            }else if(checkmerge == 3){
                message.setMessage("No file to be merged.");
                message.setNeutralButton("OK",null);
            }else{
                message.setMessage("Successfully Merged Files");
                message.setNeutralButton("OK",null);
            }
            pdialog.dismiss();
            AlertDialog dialog3 = message.create();
            dialog3.setIcon(R.drawable.doh);
            dialog3.show();
        }catch (Exception ex){
            pdialog.dismiss();
            Toast.makeText(getApplicationContext(), "Error:"+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void merge_files(){
       /* ProgressDialog dialog = ProgressDialog.show(HomeActivity.this, "",
                "Merge Files. Please wait...", true);*/
        String sdcardPath = Environment.getExternalStorageDirectory().getPath() + "/";
        Environment.getDataDirectory().getPath();
        Log.d("sdcardPath",Environment.getExternalStorageDirectory().getPath());
        String[] paths = {Environment.getExternalStorageDirectory().getPath()+"/Bluetooth","storage/sdcard1/bluetooth"};
        int check = 0;
        String jsoncheck = "";
        String json = "";
        for(int i=0;i<paths.length;i++){
            jsoncheck = loadJSON(paths[i],"doh.txt");
         if(!jsoncheck.equals("")){
             Log.d("jsoncheck","found");
             String [] filename = {"doh.txt","doh-1.txt","doh-2.txt","doh-3.txt","doh-4.txt","doh-5.txt","doh-6.txt","doh-7.txt","doh-8.txt","doh-9.txt","doh-10.txt"};
             AlertDialog.Builder message = new AlertDialog.Builder(HomeActivity.this);
             message.setTitle("DOHOLRS");
             message.setMessage("Successfully Merged Files");
             message.setNeutralButton("OK",null);
             AlertDialog dialog3 = message.create();
             dialog3.setIcon(R.drawable.doh);
             //for
             for(int f=0;f<filename.length;f++){
                 json = loadJSON(paths[i],filename[f]);
                 try {
                     String decrypted = AESUtils.decrypt(json);
                     json = decrypted;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 //statement
                 if(!json.equals("")){

                     try {
                         JSONObject obj = new JSONObject(json);
                         if(obj.getString("type").equals("assessment")){
                             JSONArray jsonArray = obj.getJSONArray("files");
                             for(int index=0;index<jsonArray.length();index++){
                                 String appid= jsonArray.getJSONObject(index).getString("appid");

                                 String apptype = jsonArray.getJSONObject(index).getString("apptype");
                                 String appheader = jsonArray.getJSONObject(index).getString("appheader");
                                 String uid = jsonArray.getJSONObject(index).getString("uid");
                                 String data = jsonArray.getJSONObject(index).getString("filename");
                                 Log.d("appid",appid);
                                 Log.d("apptype",apptype);
                                 Log.d("appheader",appheader);
                                 Log.d("uid",uid);
                                 Log.d("data",data);

                                 JSONArray jsonArrayhead = new JSONArray();
                                 JSONObject head = new JSONObject();
                                 head.put("name",appheader);
                                 jsonArrayhead.put(head);

                                 String[] columns = {"uid","appid","headers","apptype","syncstatus","sharestatus"};
                                 String[] adatas = {this.uid,appid,jsonArrayhead.toString(),apptype,"0","0"};
                                 String[] initialdata = {this.uid,appid,data,appheader,"0"};
                                 String[] initialcol = {"uid","appid","initial_json_data","header","status"};
                                 //return;return;
                    /*if(db.check_if_exist_tbl_save_assessment_data(appid,this.uid)){
                        AlertDialog.Builder message3 = new AlertDialog.Builder(this);
                        message.setTitle("DOHOLRS");
                        message.setMessage("Already Have this files");
                        return;
                    }*/

                                 if(db.check_if_exist_tbl_save_assessment_data(appid,this.uid)){


                                     Log.d("checkss","found");
                                     String headers = db.get_json_data_tbl_save_assessment_headers(this.uid,appid);
                                     Log.d("headers",headers);

                                     JSONArray jsonArray1 = new JSONArray(headers);
                                     JSONObject head1 = new JSONObject();
                                     head1.put("name",appheader);
                                     jsonArray1.put(head1);
                                     //String[] columns = {"uid","appid","headers","apptype","syncstatus"};
                                     Log.d("headers",jsonArray1.toString());
                                     String[] ucol = {"uid","appid","headers","apptype","syncstatus","sharestatus"};
                                     String[] udatas = {this.uid,appid,jsonArray1.toString(),apptype,"0","0"};



                                     if(!db.check_has_initial_save_assessment(appid,appheader)){

                                         Log.d("checkintial","not found");
                                         if(db.update("tbl_save_assessment",columns,udatas,"appid",appid)){
                                             if(db.add("tbl_initial_save_assessment",initialcol,initialdata,"")){
                                                 Log.d("tbl_initial","added");
                                             }else{
                                                 Log.d("tbl_initial","added");
                                             }
                                         }else{
                                             Log.d("updatedata","not update");
                                         }
                                         check++;
                                     }else{
                                         Log.d("checkintial","found");
                                         String id = db.get_tbl_initial_id(appid,uid,appheader);
                                         if(db.update("tbl_initial_save_assessment",initialcol,initialdata,"initialid",id)){
                                             Log.d("tbl_initial","update");
                                         }else{
                                             Log.d("tbl_initial","update");
                                         }


                                     }
                                 }else{
                                     Log.d("checkss","not found");
                                     if(db.add("tbl_save_assessment",columns,adatas,"")){
                                         Log.d("tbl_save_assessment","added");
                                         if(db.add("tbl_initial_save_assessment",initialcol,initialdata,"")){
                                             Log.d("tbl_initial","added");
                                         }else{
                                             Log.d("tbl_initial","added");
                                         }
                                     }else{
                                         Log.d("tbl_save_assessment","not added");
                                     }
                                 }

                             }


                         }



                /*if(check != jsonArray.length()){

                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("DOHOLRS");
                            builder.setMessage("Already save assessment results");
                            builder.setNeutralButton("OK",null);
                            AlertDialog dialog2 = builder.create();
                            dialog2.setIcon(R.drawable.doh);
                            dialog2.show();

                }else if(check == jsonArray.length()){

                            dialog3.show();
                }*/




                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }else{

//                     AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
//                     builder.setTitle("DOHOLRS");
//                     builder.setMessage("No Files to be merged");
//                     builder.setNeutralButton("OK",null);
//                     AlertDialog dialog2 = builder.create();
//                     dialog2.setIcon(R.drawable.doh);
//                     dialog2.show();
                 }
                 //end of statement
             }//end of for loop
             dialog3.show();
              break;
         }else{
             Log.d("jsoncheck","not found"); }
        }
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)



    }
    private void mergemon_files(){
        Log.d("mergemonitoring","run");
        String[] paths = {Environment.getExternalStorageDirectory().getPath()+"/Bluetooth","storage/sdcard1/bluetooth",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+""};
        String [] filename = {"monitoring.txt","monitoring-1.txt","monitoring-2.txt","monitoring-3.txt","monitoring-4.txt","monitoring-5.txt","monitoring-6.txt","monitoring-7.txt","monitoring-8.txt","monitoring-9.txt","monitoring-10.txt"};
        int check = 0;
        db.delete("assesscombinedtemp","","");
        String jsoncheck = "";
        String json = "";
        AlertDialog.Builder message = new AlertDialog.Builder(HomeActivity.this);
        message.setTitle("DOHOLRS");
        int countcon = 0;
        int cnttemp = 0;
        //count the of path has no file to be merge.
        int checkmerge = 0;
        ProgressDialog pdialog;
        pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                "Merging Data. Please wait...", true);
        try{
            for(int i=0;i<paths.length;i++)
            {
                int cn = 0;
                for(int f=0;f<filename.length;f++){
                    jsoncheck = loadJSON(paths[i],filename[f]);
                    if(!jsoncheck.equals("")){
                        Log.d("jsoncheck",jsoncheck);
                        try {
                            String decrypted = AESUtils.decrypt(jsoncheck);
                            json = decrypted;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(!json.equals("")){
                            try {
                                JSONObject obj = new JSONObject(json);
                                Log.d("json",obj.toString());
                                JSONArray jsonArray = obj.getJSONArray("files");
                                for(int o=0;o<jsonArray.length();o++){
                                    String uid = jsonArray.getJSONObject(o).getString("uid");
                                    JSONArray combined = jsonArray.getJSONObject(o).getJSONArray("assesscombined");
                                    //insert combined
                                    for(int com=0;com<combined.length();com++){
                                        String asmtComb_FK = combined.getJSONObject(com).getString("asmtComb_FK");
                                        String assessmentName = combined.getJSONObject(com).getString("assessmentName");
                                        String assessmentSeq = combined.getJSONObject(com).getString("assessmentSeq");
                                        String assessmentHead = combined.getJSONObject(com).getString("assessmentHead");
                                        String asmtH3ID_FK = combined.getJSONObject(com).getString("asmtH3ID_FK");
                                        String h3name = combined.getJSONObject(com).getString("h3name");
                                        String asmtH2ID_FK = combined.getJSONObject(com).getString("asmtH2ID_FK");
                                        String h2name = combined.getJSONObject(com).getString("h2name");
                                        String asmtH1ID_FK = combined.getJSONObject(com).getString("asmtH1ID_FK");
                                        String h1name = combined.getJSONObject(com).getString("h1name");
                                        String partID = combined.getJSONObject(com).getString("partID");
                                        String evaluation = combined.getJSONObject(com).getString("evaluation");
                                        String remarks = combined.getJSONObject(com).getString("remarks");
                                        String evaluatedBy = combined.getJSONObject(com).getString("evaluatedBy");
                                        String ename = combined.getJSONObject(com).getString("ename");
                                        String appid = combined.getJSONObject(com).getString("appid");
                                        String monid = combined.getJSONObject(com).getString("monid");
                                        String dupid = db.get_tbl_assesscombinedmon1(appid,"", asmtComb_FK, "", "", asmtH1ID_FK,monid);
                                        if (db.checkDatas("assesscombined", "dupID", dupid)){
                                            Log.d("dupid",dupid);
                                            Log.d("found","true");
                                            String duptid = "";
                                            //insert in temp data
                                            String[] acolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                                                    "evaluation","remarks","evaluatedBy","appid","monid","ename"};
                                            Cursor c = db.get_assesscombineddata(dupid);
                                            if(c!=null && c.getCount()>0){
                                                c.moveToFirst();
                                                while (!c.isAfterLast()){
                                                    countcon++;
                                                    String[] datas =
                                                            {
                                                                    c.getString(c.getColumnIndex("asmtComb_FK")),
                                                                    c.getString(c.getColumnIndex("assessmentName")),
                                                                    c.getString(c.getColumnIndex("assessmentSeq")),
                                                                    c.getString(c.getColumnIndex("assessmentHead")),
                                                                    c.getString(c.getColumnIndex("asmtH3ID_FK")),
                                                                    c.getString(c.getColumnIndex("h3name")),
                                                                    c.getString(c.getColumnIndex("asmtH2ID_FK")),
                                                                    c.getString(c.getColumnIndex("h2name")),
                                                                    c.getString(c.getColumnIndex("asmtH1ID_FK")),
                                                                    c.getString(c.getColumnIndex("h1name")),
                                                                    c.getString(c.getColumnIndex("partID")),
                                                                    c.getString(c.getColumnIndex("evaluation")),
                                                                    c.getString(c.getColumnIndex("remarks")),
                                                                    c.getString(c.getColumnIndex("evaluatedBy")),
                                                                    c.getString(c.getColumnIndex("appid")),
                                                                    c.getString(c.getColumnIndex("monid")),
                                                                    c.getString(c.getColumnIndex("ename"))
                                                            };
                                                    duptid = db.get_tbl_assesscombinedtempmon(c.getString(c.getColumnIndex("appid")),
                                                            c.getString(c.getColumnIndex("evaluatedBy")),
                                                            c.getString(c.getColumnIndex("asmtComb_FK")), "", "",
                                                            c.getString(c.getColumnIndex("asmtH1ID_FK")),
                                                            c.getString(c.getColumnIndex("monid")));
                                                    if (!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                        if (db.add("assesscombinedtemp", acolumns, datas, "")) {
                                                            Log.d("assesscombinedtemp", "added");
                                                        } else {
                                                            Log.d("assesscombinedtemp", "not added");
                                                        }
                                                    }else{cnttemp++;}
                                                    c.moveToNext();
                                                }
                                            }
                                            countcon++;
                                            duptid = db.get_tbl_assesscombinedtempmon(appid,evaluatedBy, asmtComb_FK, "", "", asmtH1ID_FK,monid);
                                            String[] adatas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partID,
                                                    evaluation,remarks,evaluatedBy,appid,monid,ename};
                                            if(!db.checkDatas("assesscombinedtemp", "dupID", duptid)){
                                                if (db.add("assesscombinedtemp", acolumns, adatas, "")) {
                                                    Log.d("assesscombinedtemp", "added");
                                                } else {
                                                    Log.d("assesscombinedtemp", "not added");
                                                }
                                            }else{cnttemp++;}
                                        }else{
                                            Log.d("found","false");
                                            String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                                                    "evaluation","remarks","evaluatedBy","appid","monid","ename"};
                                            String[] datas = {asmtComb_FK, assessmentName, assessmentSeq,assessmentHead,asmtH3ID_FK,h3name,asmtH2ID_FK,h2name,asmtH1ID_FK,h1name,partID,
                                                    evaluation,remarks,evaluatedBy,appid,monid,ename};
                                            if (db.add("assesscombined", dcolumns, datas, "")) {
                                                Log.d("assesscombined", "added");
                                            } else {
                                                Log.d("assesscombined", "not added");
                                            }
                                        }
                                    }
                                    //end of insert combined
                                    //insert header
                                    JSONArray header = jsonArray.getJSONObject(0).getJSONArray("assessheader");
                                    for(int h=0;h<header.length();h++){
                                        String headerid = header.getJSONObject(h).getString("headerid");
                                        String headerlevel = header.getJSONObject(h).getString("headerlevel");
                                        String assess = header.getJSONObject(h).getString("assess");
                                        String appid = header.getJSONObject(h).getString("appid");
                                        String uids = header.getJSONObject(h).getString("uid");
                                        String monid = header.getJSONObject(h).getString("monid");
                                        String id = db.get_tbl_assessment_headermon(appid,uids,headerid,headerlevel,monid);
                                        if (!db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
                                            Log.d("found","false");
                                            String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid","monid"};
                                            String[] datas = {headerid,headerlevel,assess,appid,uids,monid};
                                            if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                                                Log.d("tblsaveassessheader", "added");
                                            } else {
                                                Log.d("tblsaveassessheader", "not added");
                                            }
                                        }else{
                                            Log.d("found","true");
                                        }
                                    }
                                    //end of insert header
                                    //insert recommend
                                    JSONArray recommend = jsonArray.getJSONObject(0).getJSONArray("assessrecommend");
                                    for(int r=0;r<recommend.length();r++){
                                        String choice = recommend.getJSONObject(r).getString("choice");
                                        String details = recommend.getJSONObject(r).getString("details");
                                        String valfrom = recommend.getJSONObject(r).getString("valfrom");
                                        String valto = recommend.getJSONObject(r).getString("valto");
                                        String days = recommend.getJSONObject(r).getString("days");
                                        String monid = recommend.getJSONObject(r).getString("monid");
                                        String selfassess = recommend.getJSONObject(r).getString("selfassess");
                                        String revision = recommend.getJSONObject(r).getString("revision");
                                        String evaluatedby = recommend.getJSONObject(r).getString("evaluatedby");
                                        String ename = recommend.getJSONObject(r).getString("ename");
                                        String appid = recommend.getJSONObject(r).getString("appid");
                                        String t_details = recommend.getJSONObject(r).getString("t_details");
                                        String noofbed = recommend.getJSONObject(r).getString("noofbed");
                                        String noofdialysis = recommend.getJSONObject(r).getString("noofdialysis");
                                        String conforme = recommend.getJSONObject(r).getString("conforme");
                                        String conformeDesignation = recommend.getJSONObject(r).getString("conformeDesignation");
                                        if (!db.checkDatas("assessrecommend", "appid", appid)){
                                            Log.d("found","false");
                                            String[] dcolumns = {"choice", "details", "valfrom","valto","days","monid","selfassess","revision","evaluatedby","appid","t_details","noofbed",
                                                    "noofdialysis","conforme","conformeDesignation","ename"};
                                            String[] datas = {choice,details,valfrom,valto,days,monid,selfassess,revision,evaluatedby,appid,t_details,noofbed,noofdialysis,conforme,conformeDesignation,ename};
                                            if (db.add("assessrecommend", dcolumns, datas, "")) {
                                                Log.d("assessrecommend", "added");
                                            } else {
                                                Log.d("assessrecommend", "not added");
                                            }
                                        }else{
                                            Log.d("found","true");
                                        }
                                    }
                                    //end of recommned

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }else{
                        cn++;
                    }
                }


                if(cn == 11){checkmerge++;}
            }
            Log.d("checkmerge",checkmerge+"");
            Log.d("countcon",countcon+"");
            Log.d("cnttemp",cnttemp+"");
            //after all merge
            if(countcon != 0 && countcon != cnttemp){
                message.setMessage("There are some data have the same assessments answer.Please determine what data do you want to merge.");
                message.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent comp = new Intent(getApplicationContext(),compareactivity.class);
                        comp.putExtra("merge","monitor");
                        startActivity(comp);
                        Animatoo.animateSlideLeft(HomeActivity.this);
                    }
                });
            }else if(checkmerge == 3){
                message.setMessage("No file to be merged.");
                message.setNeutralButton("OK",null);
            }else{
                message.setMessage("Successfully Merged Files");
                message.setNeutralButton("OK",null);
            }
            pdialog.dismiss();
            AlertDialog dialog3 = message.create();
            dialog3.setIcon(R.drawable.doh);
            dialog3.show();
        }catch (Exception ex){
            pdialog.dismiss();
            Toast.makeText(getApplicationContext(), "Error:"+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        viewTextWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    //changes in loadJSON
    public String loadJSON(String path,String filename) {
        String json = "";
        //Get the text file
        File file = new File(path,filename);
        // i have kept text.txt in the sd-card

        if(file.exists())   // check if file exist
        {
            //Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                json = line;
                Log.d("lineline",line);

            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }
            //Set the text

        }
        else
        {

        }
        return json;

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
                        Intent main = new Intent(HomeActivity.this,MainActivity.class);
                        startActivity(main);
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        finish();
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Animatoo.animateSlideRight(HomeActivity.this);
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
                                Animatoo.animateSlideLeft(HomeActivity.this);
                            }
                            break;
                        case 1:
                            if(model.index==0){
                                Intent monitor = new Intent(getApplicationContext(),MonitoringActivity.class);
                                startActivity(monitor);
                                Animatoo.animateSlideLeft(HomeActivity.this);
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
                                Animatoo.animateSlideLeft(HomeActivity.this);
                            }
                            if(model.index==1){
                                Intent changepass = new Intent(getApplicationContext(),ChangePinPasswordActivity.class);
                                startActivity(changepass);
                                Animatoo.animateSlideLeft(HomeActivity.this);
                            }
                            if(model.index==2){
                                set_pinpassword();

                            }
                            break;
                        case 5:
                            if(model.index==0){
                                if(checker.checkHasInternet()){
                                    generate();
                                    Log.d("sends","click");
                                    //sends();
                                    //Log.d("sendassess",message);
                                }else{
                                    Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if(model.index==1){
                                if(checker.checkHasInternet()){
                                    mongenerate();
                                }else{
                                    Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                }
                            }//mongenerate
                            if(model.index==2){
                                if(checker.checkHasInternet()){
                                    Egenerate();
                                }else{
                                    Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                }

                            }//mongenerate
                            break;
                        case 6:
                            if(model.index==0){
                                /*Intent send = new Intent(getApplicationContext(),senddataActivity.class);
                                startActivity(send);*/
                                menuindex = 0;
                                try {
                                    createTextWrapper();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }

                            }
                            if(model.index==1){
                                menuindex = 1;
                                try {
                                    createTextWrapper();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(model.index==2){
                                menuindex = 2;
                                try {
                                    createTextWrapper();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                            }
                            /*if(model.index==1){
                                Intent receive = new Intent(getApplicationContext(),receivedataActivity.class);
                                startActivity(receive);

                            }*/
                            break;
                        case 7:
                            if(model.index==0){
                                menuindex = 70;
                                merged_data();
                            }else if(model.index==1){
                                menuindex = 71;
                                merged_data();
                            }else if(model.index==2){
                                menuindex = 72;
                                merged_data();
                            }

                            break;

                    }
                        onBackPressed();
                }
                return false;
            }
        });
    }

    public void bluetoothFunctionality(String filename) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"
                + filename;

        Log.d("pathpath",path);
        File file = new File(path);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(intent);
    }


    private void generate(){
        class gen extends AsyncTask<Void, Void, String> {
            ProgressDialog pdialog;

            @Override
            protected String doInBackground(Void... voids) {
                return sends();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                        "Sending Results. Please wait...", true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                assesslist.clear();
                if(checker.checkHasInternet()){
                    get_assessment_online();
                }else{
                    get_assessment_offline();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

                builder.setTitle("DOHOLRS");
                builder.setMessage(s);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setIcon(R.drawable.doh);
                dialog.show();


                //hiding the progressbar after completion
                pdialog.dismiss();
            }
        }

        //executing the async task
        gen c = new gen();
        c.execute();
    }

    private void Egenerate(){
        class createPdf extends AsyncTask<Void, Void, String> {
            ProgressDialog pdialog;

            @Override
            protected String doInBackground(Void... voids) {
                return sendsptc();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                        "Sending Results. Please wait...", true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("DOHOLRS");
                builder.setMessage(s);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.setIcon(R.drawable.doh);
                dialog.show();

                //hiding the progressbar after completion
                pdialog.dismiss();
            }
        }

        //executing the async task
        createPdf c = new createPdf();
        c.execute();
    }


    private void mongenerate(){
        class createPdf extends AsyncTask<Void, Void, String> {
            ProgressDialog pdialog;

            @Override
            protected String doInBackground(Void... voids) {


                return sendsmon();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pdialog = ProgressDialog.show(HomeActivity.this, "DOHOLRS",
                        "Sending Results. Please wait...", true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("DOHOLRS");
                builder.setMessage(s);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.setIcon(R.drawable.doh);
                dialog.show();
                //hiding the progressbar after completion
                pdialog.dismiss();
            }
        }

        //executing the async task
        createPdf c = new createPdf();
        c.execute();
    }

    public String send(){
        String message = "";
        try {

            JSONObject files = new JSONObject();
            JSONArray filesarr = new JSONArray();
            JSONObject items = new JSONObject();

            //assesscombined

            JSONArray arrcombined = new JSONArray();
            Cursor c = db.get_combined(uid);
            //loop
            if(c!=null && c.getCount()>0){
                c.moveToFirst();
                while(!c.isAfterLast()){
                    JSONObject combineditems = new JSONObject();
                    combineditems.put("asmtComb_FK",c.getString(c.getColumnIndex("asmtComb_FK")));
                    combineditems.put("assessmentName",c.getString(c.getColumnIndex("assessmentName")));
                    combineditems.put("assessmentSeq",c.getString(c.getColumnIndex("assessmentSeq")));
                    combineditems.put("assessmentHead",c.getString(c.getColumnIndex("assessmentHead")));
                    combineditems.put("asmtH3ID_FK",(c.getString(c.getColumnIndex("asmtH3ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtComb_FK")):"");
                    combineditems.put("h3name",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("h3name")):"");
                    combineditems.put("asmtH2ID_FK",(c.getString(c.getColumnIndex("asmtH2ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH2ID_FK")):"");
                    combineditems.put("h2name",(c.getString(c.getColumnIndex("h2name"))!=null)?c.getString(c.getColumnIndex("h2name")):"");
                    combineditems.put("asmtH1ID_FK",c.getString(c.getColumnIndex("asmtH1ID_FK")));
                    combineditems.put("h1name",c.getString(c.getColumnIndex("h1name")));
                    combineditems.put("evaluation",c.getString(c.getColumnIndex("evaluation")));
                    combineditems.put("remarks",c.getString(c.getColumnIndex("remarks")));
                    combineditems.put("evaluatedBy",c.getString(c.getColumnIndex("evaluatedBy")));
                    combineditems.put("appid",c.getString(c.getColumnIndex("appid")));
                    combineditems.put("monid",c.getString(c.getColumnIndex("monid")));
                    arrcombined.put(combineditems);
                    c.moveToNext();
                }
            }
            //loop

            //assesscombined

            //items.put("appid",HomeActivity.appid);
            items.put("uid",uid);
            items.put("assesscombined",arrcombined);
            filesarr.put(items);
            files.put("data",filesarr);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public String sends(){

        final JSONObject appidd = new JSONObject();
        ArrayList<String> nrec = new ArrayList<>();
        ArrayList<String> srec = new ArrayList<>();
        int com = 0;
        int reco = 0;
        Boolean rec = false;
        Boolean send = false;
        try{
            JSONObject items;
            JSONArray arrrecommend;
            JSONArray arrcombined;
            Cursor a = db.get_combinedappid();
            //get appid
            int i = 0;
            if(a!=null && a.getCount()>0){
                send = true;
                a.moveToFirst();

                while(!a.isAfterLast()){
                    String appid = a.getString(a.getColumnIndex("appid"));
                    //filter by appid and assessmentcombined
                    Cursor c = db.get_combineddataappid(appid);
                    items = new JSONObject();
                    arrcombined = new JSONArray();
                    arrrecommend = new JSONArray();
                    //loop
                    if(c!=null && c.getCount()>0){
                        com = 1;
                        c.moveToFirst();
                        while(!c.isAfterLast()){
                            JSONObject combineditems = new JSONObject();
                            combineditems.put("asmtComb_FK",(c.getString(c.getColumnIndex("asmtComb_FK"))!=null)?c.getString(c.getColumnIndex("asmtComb_FK")):"");
                            combineditems.put("assessmentName",(c.getString(c.getColumnIndex("assessmentName"))!=null)?c.getString(c.getColumnIndex("assessmentName")):"");//assessmentName
                            combineditems.put("assessmentSeq",(c.getString(c.getColumnIndex("assessmentSeq"))!=null)?c.getString(c.getColumnIndex("assessmentSeq")):"");
                            combineditems.put("assessmentHead",(c.getString(c.getColumnIndex("assessmentHead"))!=null)?c.getString(c.getColumnIndex("assessmentHead")):"");
                            combineditems.put("asmtH3ID_FK",(c.getString(c.getColumnIndex("asmtH3ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH3ID_FK")):"");
                            combineditems.put("h3name",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("h3name")):"");//h3name
                            combineditems.put("asmtH2ID_FK",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("asmtH2ID_FK")):"");//asmtH2ID_FK
                            combineditems.put("h2name",(c.getString(c.getColumnIndex("h2name"))!=null)?c.getString(c.getColumnIndex("h2name")):"");//h2name
                            combineditems.put("asmtH1ID_FK",(c.getString(c.getColumnIndex("asmtH1ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH1ID_FK")):"");
                            combineditems.put("h1name",(c.getString(c.getColumnIndex("h1name"))!=null)?c.getString(c.getColumnIndex("h1name")):"");
                            combineditems.put("partID",(c.getString(c.getColumnIndex("partID"))!=null)?c.getString(c.getColumnIndex("partID")):"");
                            combineditems.put("evaluation",(c.getString(c.getColumnIndex("evaluation"))!=null)?c.getString(c.getColumnIndex("evaluation")):"");
                            combineditems.put("remarks",(c.getString(c.getColumnIndex("remarks"))!=null)?c.getString(c.getColumnIndex("remarks")):"");
                            combineditems.put("evaluatedBy",(c.getString(c.getColumnIndex("evaluatedBy"))!=null)?c.getString(c.getColumnIndex("evaluatedBy")):"");
                            combineditems.put("appid",(c.getString(c.getColumnIndex("appid"))!=null)?c.getString(c.getColumnIndex("appid")):"");
                            combineditems.put("monid",(c.getString(c.getColumnIndex("monid"))!=null)?c.getString(c.getColumnIndex("monid")):"");
                            arrcombined.put(combineditems);
                            c.moveToNext();
                        }
                    }else{
                        com = 0;
                    }
                    Cursor r = db.get_recommendappid(appid);
                    if(r!=null && r.getCount()>0){
                        rec = true;
                        reco = 1;
                        r.moveToFirst();
                        while(!r.isAfterLast()){
                            JSONObject ritems = new JSONObject();
                            ritems.put("choice",(r.getString(r.getColumnIndex("choice"))!=null)?r.getString(r.getColumnIndex("choice")):"");
                            ritems.put("details",(r.getString(r.getColumnIndex("details"))!=null)?r.getString(r.getColumnIndex("details")):"");
                            ritems.put("valfrom",(r.getString(r.getColumnIndex("valfrom"))!=null)?r.getString(r.getColumnIndex("valfrom")):"");
                            ritems.put("valto",(r.getString(r.getColumnIndex("valto"))!=null)?r.getString(r.getColumnIndex("valto")):"");
                            ritems.put("days",(r.getString(r.getColumnIndex("days"))!=null)?r.getString(r.getColumnIndex("days")):"");
                            ritems.put("monid",(r.getString(r.getColumnIndex("monid"))!=null)?r.getString(r.getColumnIndex("monid")):"");
                            ritems.put("selfassess",(r.getString(r.getColumnIndex("selfassess"))!=null)?r.getString(r.getColumnIndex("selfassess")):"");
                            ritems.put("revision",(r.getString(r.getColumnIndex("revision"))!=null)?r.getString(r.getColumnIndex("revision")):"");
                            ritems.put("evaluatedby",(r.getString(r.getColumnIndex("evaluatedby"))!=null)?r.getString(r.getColumnIndex("evaluatedby")):"");
                            ritems.put("appid",(r.getString(r.getColumnIndex("appid"))!=null)?r.getString(r.getColumnIndex("appid")):"");
                            ritems.put("t_details",(r.getString(r.getColumnIndex("t_details"))!=null)?r.getString(r.getColumnIndex("t_details")):"");
                            ritems.put("noofbed",(r.getString(r.getColumnIndex("noofbed"))!=null)?r.getString(r.getColumnIndex("noofbed")):"");
                            ritems.put("noofdialysis",(r.getString(r.getColumnIndex("noofdialysis"))!=null)?r.getString(r.getColumnIndex("noofdialysis")):"");
                            ritems.put("conforme",(r.getString(r.getColumnIndex("conforme"))!=null)?r.getString(r.getColumnIndex("conforme")):"");
                            ritems.put("conformeDesignation",(r.getString(r.getColumnIndex("conformeDesignation"))!=null)?r.getString(r.getColumnIndex("conformeDesignation")):"");
                            arrrecommend.put(ritems);
                            r.moveToNext();
                        }
                        srec.add(appid);
                    }else{
                        rec = false;
                        reco = 0;
                        nrec.add(appid);
                    }

                    items.put("data",arrcombined);
                    items.put("recommendation",arrrecommend);
                    Log.d("recommmend",arrrecommend.toString());
                    appidd.put(appid,items);
                    //end
                    //Log.d("responsess",appidd.toString());
                    Log.d("comapp",com+" "+appid);
                    Log.d("recoapp",reco+""+appid);
                    if(com == 1 && reco == 1){
                        Log.d("ready","true"+appid);
                        StringRequest request = new StringRequest(Request.Method.POST, Urls.sendassessment,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("responseresponse",response);
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
                                params.put("apptype","LTO");
                                params.put("data",appidd.toString());
                                //Log.d("paramss",params.toString());
                                return params;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(request);

                    }
                    a.moveToNext();
                }
                //Log.d("responsess",appidd.toString());
            }
            //
            Log.d("responsess",appidd.toString());
            if(send && (nrec.size() == 0)){
                message = "Successfully Save Data Online";
            }else{
                String message = "";
                if(srec.size()>0){
                    message += "Applications successfully synchronized with server are the following:\n"+TextUtils.join(",", srec)+"\n";
                }
                if(nrec.size()>0){
                    message += "\nSome applications are not synchronized with server because recommendations were not found on the following applications:\n"+TextUtils.join(",", nrec);
                }else{
                    message = "No data to be sync online";
                }
                this.message = message;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            this.message = e.getMessage();
        }
        return message;
    }
    public String sendsptc(){
        final JSONObject appidd = new JSONObject();
        ArrayList<String> nrec = new ArrayList<>();
        ArrayList<String> srec = new ArrayList<>();
        int com = 0;
        int reco = 0;
        Boolean data = false;
        try{

            JSONObject items;
            JSONArray arrrecommend;
            JSONArray arrcombined;
            Cursor a = db.get_combinedappidptc();
            //get appid
            int i = 0;
            if(a!=null && a.getCount()>0){
                a.moveToFirst();
                data = true;
                while(!a.isAfterLast()){
                    String appid = a.getString(a.getColumnIndex("appid"));
                    //filter by appid and assessmentcombined
                    Cursor c = db.get_combineddataappidptc(appid);
                    items = new JSONObject();
                    arrcombined = new JSONArray();
                    arrrecommend = new JSONArray();
                    //loop
                    if(c!=null && c.getCount()>0){
                        com = 1;
                        c.moveToFirst();
                        while(!c.isAfterLast()){
                            JSONObject combineditems = new JSONObject();
                            combineditems.put("asmtComb_FK",(c.getString(c.getColumnIndex("asmtComb_FK"))!=null)?c.getString(c.getColumnIndex("asmtComb_FK")):"");
                            combineditems.put("assessmentName",(c.getString(c.getColumnIndex("assessmentName"))!=null)?c.getString(c.getColumnIndex("assessmentName")):"");//assessmentName
                            combineditems.put("assessmentSeq",(c.getString(c.getColumnIndex("assessmentSeq"))!=null)?c.getString(c.getColumnIndex("assessmentSeq")):"");
                            combineditems.put("assessmentHead",(c.getString(c.getColumnIndex("assessmentHead"))!=null)?c.getString(c.getColumnIndex("assessmentHead")):"");
                            combineditems.put("asmtH3ID_FK",(c.getString(c.getColumnIndex("asmtH3ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH3ID_FK")):"");
                            combineditems.put("h3name",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("h3name")):"");//h3name
                            combineditems.put("asmtH2ID_FK",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("asmtH2ID_FK")):"");//asmtH2ID_FK
                            combineditems.put("h2name",(c.getString(c.getColumnIndex("h2name"))!=null)?c.getString(c.getColumnIndex("h2name")):"");//h2name
                            combineditems.put("asmtH1ID_FK",(c.getString(c.getColumnIndex("asmtH1ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH1ID_FK")):"");
                            combineditems.put("h1name",(c.getString(c.getColumnIndex("h1name"))!=null)?c.getString(c.getColumnIndex("h1name")):"");
                            combineditems.put("partID",(c.getString(c.getColumnIndex("partID"))!=null)?c.getString(c.getColumnIndex("partID")):"");
                            combineditems.put("parttitle",(c.getString(c.getColumnIndex("parttitle"))!=null)?c.getString(c.getColumnIndex("parttitle")):"");
                            combineditems.put("evaluation",(c.getString(c.getColumnIndex("evaluation"))!=null)?c.getString(c.getColumnIndex("evaluation")):"");
                            combineditems.put("remarks",(c.getString(c.getColumnIndex("remarks"))!=null)?c.getString(c.getColumnIndex("remarks")):"");
                            combineditems.put("evaluatedBy",(c.getString(c.getColumnIndex("evaluatedBy"))!=null)?c.getString(c.getColumnIndex("evaluatedBy")):"");
                            combineditems.put("appid",(c.getString(c.getColumnIndex("appid"))!=null)?c.getString(c.getColumnIndex("appid")):"");
                            combineditems.put("sub",(c.getString(c.getColumnIndex("sub"))!=null)?c.getString(c.getColumnIndex("sub")):"");
                            combineditems.put("isdisplay",(c.getString(c.getColumnIndex("isdisplay"))!=null)?c.getString(c.getColumnIndex("isdisplay")):"");
                            combineditems.put("revision",(c.getString(c.getColumnIndex("revision"))!=null)?c.getString(c.getColumnIndex("revision")):"");
                            arrcombined.put(combineditems);
                            c.moveToNext();
                        }

                    }else{
                        com = 0;
                    }

                    Cursor r = db.get_recommendappid(appid);

                    if(r!=null && r.getCount()>0){
                        reco = 1;
                        r.moveToFirst();
                        while(!r.isAfterLast()){
                            JSONObject ritems = new JSONObject();
                            ritems.put("choice",(r.getString(r.getColumnIndex("choice"))!=null)?r.getString(r.getColumnIndex("choice")):"");
                            ritems.put("details",(r.getString(r.getColumnIndex("details"))!=null)?r.getString(r.getColumnIndex("details")):"");
                            ritems.put("valfrom",(r.getString(r.getColumnIndex("valfrom"))!=null)?r.getString(r.getColumnIndex("valfrom")):"");
                            ritems.put("valto",(r.getString(r.getColumnIndex("valto"))!=null)?r.getString(r.getColumnIndex("valto")):"");
                            ritems.put("days",(r.getString(r.getColumnIndex("days"))!=null)?r.getString(r.getColumnIndex("days")):"");
                            ritems.put("monid",(r.getString(r.getColumnIndex("monid"))!=null)?r.getString(r.getColumnIndex("monid")):"");
                            ritems.put("selfassess",(r.getString(r.getColumnIndex("selfassess"))!=null)?r.getString(r.getColumnIndex("selfassess")):"");
                            ritems.put("revision",(r.getString(r.getColumnIndex("revision"))!=null)?r.getString(r.getColumnIndex("revision")):"");
                            ritems.put("evaluatedby",(r.getString(r.getColumnIndex("evaluatedby"))!=null)?r.getString(r.getColumnIndex("evaluatedby")):"");
                            ritems.put("appid",(r.getString(r.getColumnIndex("appid"))!=null)?r.getString(r.getColumnIndex("appid")):"");
                            ritems.put("t_details",(r.getString(r.getColumnIndex("t_details"))!=null)?r.getString(r.getColumnIndex("t_details")):"");
                            ritems.put("noofbed",(r.getString(r.getColumnIndex("noofbed"))!=null)?r.getString(r.getColumnIndex("noofbed")):"");
                            ritems.put("noofdialysis",(r.getString(r.getColumnIndex("noofdialysis"))!=null)?r.getString(r.getColumnIndex("noofdialysis")):"");
                            ritems.put("conforme",(r.getString(r.getColumnIndex("conforme"))!=null)?r.getString(r.getColumnIndex("conforme")):"");
                            ritems.put("conformeDesignation",(r.getString(r.getColumnIndex("conformeDesignation"))!=null)?r.getString(r.getColumnIndex("conformeDesignation")):"");
                            arrrecommend.put(ritems);
                            r.moveToNext();
                        }
                        srec.add(appid);
                    }else{
                        reco = 0;
                        nrec.add(appid);
                    }
                    items.put("data",arrcombined);
                    items.put("recommendation",arrrecommend);
                    appidd.put(appid,items);
                    //end
                    Log.d("comapp",com+" "+appid);
                    Log.d("recoapp",reco+" "+appid);
                    if(com ==1 && reco == 1){
                        Log.d("ready",appid);
                        StringRequest request = new StringRequest(Request.Method.POST, Urls.sendassessment,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("responseresponse",response);
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
                                params.put("apptype","PTC");
                                params.put("data",appidd.toString());
                                //Log.d("paramss",params.toString());
                                return params;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(request);
                    }
                    //Log.d("responsess",appidd.toString());
                    a.moveToNext();
                }
                //Log.d("responsess",appidd.toString());
            }
            //
            Log.d("responsess",appidd.toString());
            Log.d("url",Urls.sendassessment);
            if(data && nrec.size() == 0){
                message = "Successfully Send Data to Server";
            }else{
                String message = "";
                if(srec.size()>0){
                    message += "Applications successfully synchronized with server are the following:\n"+TextUtils.join(",", srec)+"\n";
                }
                if(nrec.size()>0){
                    message = "Some applications are not synchronized with server because recommendations were not found on the following applications:\n"+TextUtils.join(",", nrec);
                }else{
                    message = "No data to be sync online";
                }
                this.message = message;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            this.message = e.getMessage();
        }

        return message;
    }

    public String sendsmon(){
        final JSONObject appidd = new JSONObject();
        ArrayList<String> nrec = new ArrayList<>();
        ArrayList<String> srec = new ArrayList<>();
        int com = 0;
        int reco = 0;
        Boolean data = false;
        try{

            JSONObject items;
            JSONArray arrrecommend;
            JSONArray arrcombined;
            Cursor a = db.get_combinedappidmonid();
            //get appid
            int i = 0;
            if(a!=null && a.getCount()>0){
                a.moveToFirst();
                data = true;
                while(!a.isAfterLast()){
                    String appid = a.getString(a.getColumnIndex("appid"));
                    //filter by appid and assessmentcombined
                    Cursor c = db.get_combineddataappidmon(appid);
                    items = new JSONObject();
                    arrcombined = new JSONArray();
                    arrrecommend = new JSONArray();
                    //loop
                    if(c!=null && c.getCount()>0){
                        com = 1;
                        c.moveToFirst();
                        while(!c.isAfterLast()){
                            JSONObject combineditems = new JSONObject();
                            combineditems.put("asmtComb_FK",(c.getString(c.getColumnIndex("asmtComb_FK"))!=null)?c.getString(c.getColumnIndex("asmtComb_FK")):"");
                            combineditems.put("assessmentName",(c.getString(c.getColumnIndex("assessmentName"))!=null)?c.getString(c.getColumnIndex("assessmentName")):"");//assessmentName
                            combineditems.put("assessmentSeq",(c.getString(c.getColumnIndex("assessmentSeq"))!=null)?c.getString(c.getColumnIndex("assessmentSeq")):"");
                            combineditems.put("assessmentHead",(c.getString(c.getColumnIndex("assessmentHead"))!=null)?c.getString(c.getColumnIndex("assessmentHead")):"");
                            combineditems.put("asmtH3ID_FK",(c.getString(c.getColumnIndex("asmtH3ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH3ID_FK")):"");
                            combineditems.put("h3name",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("h3name")):"");//h3name
                            combineditems.put("asmtH2ID_FK",(c.getString(c.getColumnIndex("h3name"))!=null)?c.getString(c.getColumnIndex("asmtH2ID_FK")):"");//asmtH2ID_FK
                            combineditems.put("h2name",(c.getString(c.getColumnIndex("h2name"))!=null)?c.getString(c.getColumnIndex("h2name")):"");//h2name
                            combineditems.put("asmtH1ID_FK",(c.getString(c.getColumnIndex("asmtH1ID_FK"))!=null)?c.getString(c.getColumnIndex("asmtH1ID_FK")):"");
                            combineditems.put("h1name",(c.getString(c.getColumnIndex("h1name"))!=null)?c.getString(c.getColumnIndex("h1name")):"");
                            combineditems.put("partID",(c.getString(c.getColumnIndex("partID"))!=null)?c.getString(c.getColumnIndex("partID")):"");
                            combineditems.put("evaluation",(c.getString(c.getColumnIndex("evaluation"))!=null)?c.getString(c.getColumnIndex("evaluation")):"");
                            combineditems.put("remarks",(c.getString(c.getColumnIndex("remarks"))!=null)?c.getString(c.getColumnIndex("remarks")):"");
                            combineditems.put("evaluatedBy",(c.getString(c.getColumnIndex("evaluatedBy"))!=null)?c.getString(c.getColumnIndex("evaluatedBy")):"");
                            combineditems.put("appid",(c.getString(c.getColumnIndex("appid"))!=null)?c.getString(c.getColumnIndex("appid")):"");
                            combineditems.put("monid",(c.getString(c.getColumnIndex("monid"))!=null)?c.getString(c.getColumnIndex("monid")):"");
                            arrcombined.put(combineditems);
                            c.moveToNext();
                        }

                    }else{
                        com = 0;
                    }

                    Cursor r = db.get_recommendappidmon(appid);
                    if(r!=null && r.getCount()>0){
                        reco = 1;
                        r.moveToFirst();
                        while(!r.isAfterLast()){
                            JSONObject ritems = new JSONObject();
                            ritems.put("choice",(r.getString(r.getColumnIndex("choice"))!=null)?r.getString(r.getColumnIndex("choice")):"");
                            ritems.put("details",(r.getString(r.getColumnIndex("details"))!=null)?r.getString(r.getColumnIndex("details")):"");
                            ritems.put("valfrom",(r.getString(r.getColumnIndex("valfrom"))!=null)?r.getString(r.getColumnIndex("valfrom")):"");
                            ritems.put("valto",(r.getString(r.getColumnIndex("valto"))!=null)?r.getString(r.getColumnIndex("valto")):"");
                            ritems.put("days",(r.getString(r.getColumnIndex("days"))!=null)?r.getString(r.getColumnIndex("days")):"");
                            ritems.put("monid",(r.getString(r.getColumnIndex("monid"))!=null)?r.getString(r.getColumnIndex("monid")):"");
                            ritems.put("selfassess",(r.getString(r.getColumnIndex("selfassess"))!=null)?r.getString(r.getColumnIndex("selfassess")):"");
                            ritems.put("revision",(r.getString(r.getColumnIndex("revision"))!=null)?r.getString(r.getColumnIndex("revision")):"");
                            ritems.put("evaluatedby",(r.getString(r.getColumnIndex("evaluatedby"))!=null)?r.getString(r.getColumnIndex("evaluatedby")):"");
                            ritems.put("appid",(r.getString(r.getColumnIndex("appid"))!=null)?r.getString(r.getColumnIndex("appid")):"");
                            ritems.put("t_details",(r.getString(r.getColumnIndex("t_details"))!=null)?r.getString(r.getColumnIndex("t_details")):"");
                            ritems.put("noofbed",(r.getString(r.getColumnIndex("noofbed"))!=null)?r.getString(r.getColumnIndex("noofbed")):"");
                            ritems.put("noofdialysis",(r.getString(r.getColumnIndex("noofdialysis"))!=null)?r.getString(r.getColumnIndex("noofdialysis")):"");
                            ritems.put("conforme",(r.getString(r.getColumnIndex("conforme"))!=null)?r.getString(r.getColumnIndex("conforme")):"");
                            ritems.put("conformeDesignation",(r.getString(r.getColumnIndex("conformeDesignation"))!=null)?r.getString(r.getColumnIndex("conformeDesignation")):"");
                            arrrecommend.put(ritems);
                            r.moveToNext();
                        }
                        srec.add(appid);
                    }else{
                        reco = 0;
                        nrec.add(appid);
                    }
                    items.put("data",arrcombined);
                    items.put("recommendation",arrrecommend);
                    appidd.put(appid,items);
                    //end
                    Log.d("comapp",com+" "+appid);
                    Log.d("recoapp",reco+" "+appid);
                    if(com == 1 && reco == 1){
                        Log.d("ready",appid);
                        StringRequest request = new StringRequest(Request.Method.POST, Urls.sendassessment,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("responseresponse",response);
                                        if(response.equals("true")){
                                            Log.d("message","true");
                                        }else{
                                            Log.d("message","false");
                                        }
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
                                params.put("apptype","MON");
                                params.put("data",appidd.toString());
                                //Log.d("paramss",params.toString());
                                return params;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(request);
                    }
                    //Log.d("responsess",appidd.toString());
                    a.moveToNext();
                }
                //Log.d("responsess",appidd.toString());
            }
            //
            Log.d("responsess",appidd.toString());
            if(data && nrec.size()== 0){
                message = "Successfully Send Data to Server";
            }else{
                String message = "";
                if(srec.size()>0){
                    message += "Applications successfully synchronized with server are the following:\n"+TextUtils.join(",", srec)+"\n";
                }
                if(nrec.size()>0){
                    message += "Some applications are not synchronized with server because recommendations were not found on the following applications:\n"+TextUtils.join(",", nrec);
                }else{
                    message = "No data to be sync online";
                }
                this.message = message;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            this.message = e.getMessage();
        }

        return message;
    }


    public String send_request(){
        //send_assessment("",appid,type,"");
        String message = "";


//        if(db.check_reslicense(uid,"assessment")){
//
//
//            Cursor c = db.get_resappid(uid,"assessment");
//            if(c!=null && c.getCount()>0){
//                c.moveToFirst();
//                String initial = "";
//                String id = "";
//                String status = "";
//
//                while (!c.isAfterLast()){
//                    String appid = c.getString(c.getColumnIndex("appid"));
//                    String type = c.getString(c.getColumnIndex("apptype"));
//                    String headers = db.get_json_data_tbl_save_assessment_headers(uid,appid);
//
//                    try {
//                        JSONArray aheaders = new JSONArray(headers);
//
//                        for(int he=0;he<aheaders.length();he++){
//                            String name = aheaders.getString(he);
//                            JSONObject headname = new JSONObject(name);
//                            status = db.get_tbl_initial_assessmen_status(appid,headname.getString("name"));
//                            initial = db.get_tbl_initial_datas(appid,headname.getString("name"));
//                            id = db.get_tbl_initial_assessmen_id1(appid,headname.getString("name"),uid);
//                            //changes
//                            if(!id.equals("")){
//                                Log.d("ididi",id);
//                                send_assessment(initial,appid,type,id);
//                                Log.d("status",status);
//                            }else{
//                                Log.d("sync","1");
//                                Log.d("status",status);
//                            }
//                            Thread.sleep(1000);
//
//
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    c.moveToNext();
//
//                }
//
//
//
//                /*AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("DOHOLRS");
//                builder.setMessage("Successfully Save Data Online");
//                builder.setNegativeButton("OK",null);
//                AlertDialog dialog = builder.create();
//                dialog.setIcon(R.drawable.doh);
//                dialog.show();*/
//                message = "success";
//            }
//
//        }else{
//            message = "error";
//        }

        return message;
    }

    public String send_Erequest(){
        String message = "";

        if(db.check_reslicense(uid,"evaluation")){
            Log.d("check_reslicense","found");

            Cursor c = db.get_resappid(uid,"evaluation");
            if(c!=null && c.getCount()>0){
                Log.d("check_reslicenses","found");
                c.moveToFirst();
                String initial = "";
                String id = "";
                String status = "";

                while (!c.isAfterLast()){
                    String appid = c.getString(c.getColumnIndex("appid"));
                    String type = c.getString(c.getColumnIndex("apptype"));
                    String headers = db.get_json_data_tbl_save_assessment_headers(uid,appid);

                    try {
                        JSONArray aheaders = new JSONArray(headers);

                        for(int he=0;he<aheaders.length();he++){
                            String name = aheaders.getString(he);
                            JSONObject headname = new JSONObject(name);
                            Log.d("headname",headname.getString("name"));
                            status = db.get_tbl_initial_assessmen_status(appid,headname.getString("name"));
                            initial = db.get_tbl_initial_datas(appid,headname.getString("name"));
                            id = db.get_tbl_initial_assessmen_id1(appid,headname.getString("name"),uid);
                            Log.d("initial",initial);
                            //changes
                            if(!id.equals("")){
                                Log.d("ididi",id);
                                send_eassessment(initial,appid,type,id);
                                Log.d("status",status);

                            }else{
                                Log.d("sync","1");
                                Log.d("status",status);
                            }
                            Thread.sleep(1000);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    c.moveToNext();

                }
                message = "success";
            }

        }else{
            Log.d("check_reslicense","found");
            message = "error";
        }

        return message;
    }

    public String monsend_request(){
        String message = "";

        if(db.check_monappid(uid)){


            Cursor c = db.get_monappid(uid);
            if(c!=null && c.getCount()>0){
                c.moveToFirst();
                String initial = "";
                String id = "";
                String status = "";

                while (!c.isAfterLast()){
                    String appid = c.getString(c.getColumnIndex("appid"));
                    String type = c.getString(c.getColumnIndex("apptype"));
                    String headers = db.get_json_data_tbl_monsave_assessment_headers(uid,appid);

                    try {
                        JSONArray aheaders = new JSONArray(headers);

                        for(int he=0;he<aheaders.length();he++){
                            String name = aheaders.getString(he);
                            JSONObject headname = new JSONObject(name);
                            status = db.get_tbl_moninitial_assessmen_status(appid,headname.getString("name"));
                            initial = db.get_tbl_moninitial_datas(appid,headname.getString("name"));
                            id = db.get_tbl_moninitial_assessmen_id(appid,headname.getString("name"));

                            if(!id.equals("")){
                                Log.d("ididi",id);
                                //send_assessment(initial,appid,type,id);
                                Log.d("status",status);

                            }else{
                                Log.d("sync","1");
                                Log.d("status",status);
                            }
                            Thread.sleep(1000);




                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    c.moveToNext();

                }

                message = "success";
            }

        }else{
            message = "error";
        }

        return message;
    }

    private void send_assessment(final String save_assessment,final String appid,final String type,final String id){
        StringRequest request = new StringRequest(Request.Method.GET, Urls.sendassessment,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responseresponse",response);
                       /* try {
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("status").equals("success")){
                                Log.d("status",obj.getString("status"));
                                String[] columns = {"status"};
                                String[] datas = {"1"};
                                if(db.update("tbl_initial_save_assessment",columns,datas,"initialid",id)){
                                   Log.d("update","succcess");
                                }else{
                                    Log.d("update","error");
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
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
                //params.put("assessment",save_assessment);
                params.put("isMobile","dan");
                //.put("apptype",type);
                //params.put("appid",appid);
                //params.put("uid",uid);
                Log.d("paramss",params.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }

    private void send_eassessment(final String save_assessment,final String appid,final String type,final String id){
        StringRequest request = new StringRequest(Request.Method.POST, Urls.sendassessment+uid+"/"+appid+"/"+type,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responseresponse",response);
//                        try {
//                            JSONObject obj = new JSONObject(response);
//                            /*if(obj.getString("status").equals("success")){
//                                Log.d("status",obj.getString("status"));
//                                String[] columns = {"status"};
//                                String[] datas = {"1"};
//                                if(db.update("tbl_initial_save_assessment",columns,datas,"initialid",id)){
//                                    Log.d("update","succcess");
//                                }else{
//                                    Log.d("update","error");
//                                }
//                            }*/
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
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
                params.put("assessment",save_assessment);
                params.put("isMobile","true");
                params.put("apptype",type);
                params.put("appid",appid);
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }

}
