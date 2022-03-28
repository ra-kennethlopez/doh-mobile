package com.example.pc.doh.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.example.pc.doh.Adapter.AssestmentAdapter;
import com.example.pc.doh.Adapter.MonitoringAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.AssestmentModel;
import com.example.pc.doh.Model.Monitoring;
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

public class MonitoringActivity extends AppCompatActivity {
    RecyclerView monitorrv;
    MonitoringAdapter adapter;
    List<Monitoring> list = new ArrayList<>();
    TextView lblMessage;
    public static String type,code,faclityname,typefacility,date,status,appid;
    public static String monType;
    private String uid;
    private DatabaseHelper db;
    private InternetCheck checker;
    private SearchView searchView;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitoring_layout);

        lblMessage = findViewById(R.id.lblmessage);

        UserModel user = SharedPrefManager.getInstance(this).getUser();

        uid = user.getId();
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        swipe = findViewById(R.id.swipe);
        int c1 = getResources().getColor(R.color.color28A55F);
        swipe.setColorSchemeColors(c1,c1,c1);

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
                Intent assestment = new Intent(MonitoringActivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(MonitoringActivity.this);
            }
        });

        getSupportActionBar().setTitle("Monitoring");


        getMonitoringData();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMonitoringData();
                swipe.setRefreshing(false);
            }
        });

    }

    public void getMonitoringData() {
        list.clear();

        if(!checker.checkHasInternet()){
            Log.d("internet","false");
            get_monitoring_offline();
        }else{
            Log.d("internet","true");
            get_monitoring_online();
        }
    }

    private void showHideEmptyDataMessage() {
        if (list.size() == 0) {
            lblMessage.setVisibility(View.VISIBLE);
        } else {
            lblMessage.setVisibility(View.GONE);
        }
    }

    public void setAdapter(){
        adapter = new MonitoringAdapter(this,list);
        monitorrv = findViewById(R.id.monitorlist);
        monitorrv.setLayoutManager(new LinearLayoutManager(this));
        monitorrv.setAdapter(adapter);

        adapter.setonItemClickListener(new MonitoringAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(MonitoringActivity.this, MonitoringDetailsActivity.class);
                MonitoringActivity.this.startActivity(i);
                Animatoo.animateSlideLeft(MonitoringActivity.this);
                code = list.get(position).getCode();
                type = list.get(position).getType();
                faclityname = list.get(position).getFaclityname();
                typefacility = list.get(position).getTypefacility();
                date = list.get(position).getDate();
                status = list.get(position).getStatus();
                appid = list.get(position).getAppid();
                monType = "mon";
                HomeActivity.restpye = "monitoring";
                HomeActivity.licensetype = "monitoring";
                finish();
            }
        });
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
                if(list.size()>0){
                    adapter.getFilter().filter(query);
                }

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



    private void get_monitoring_offline(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        Cursor det = db.get_item("tbl_monitoring","uid",uid);
        if(det!=null && det.getCount()>0){
            Log.d("json","not null");
            det.moveToFirst();
            while (!det.isAfterLast()){
                try {
                    JSONObject items = new JSONObject(det.getString(det.getColumnIndex("json_data")));
                    JSONArray jsonArray = items.getJSONArray("data");
                    for(int i=0;i<jsonArray.length();i++){
                        String id = jsonArray.getJSONObject(i).getString("monid");
                        String date = jsonArray.getJSONObject(i).getString("date_added");
                        String status = jsonArray.getJSONObject(i).getString("monStatus");
                        String facilityname = jsonArray.getJSONObject(i).getString("name_of_faci");
                        String code = jsonArray.getJSONObject(i).getString("type_of_faci");
                        String typefacility = jsonArray.getJSONObject(i).getString("type_of_faci");
                        if(typefacility.equals("")){
                            typefacility = "";
                        }
                        String appid = jsonArray.getJSONObject(i).getString("appid");
                        list.add(new Monitoring(id,code,facilityname,typefacility,date,status,appid));
                    }
                    setAdapter();
                    adapter.notifyDataSetChanged();
                    bar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                det.moveToNext();
            }

        }

        showHideEmptyDataMessage();
    }

    private void get_monitoring_online(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.monitor,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                                String[] columns = {"uid","json_data"};
                                String[] data = {uid,response};
                                if(db.checkDatas("tbl_monitoring","uid",uid)){
                                    Log.d("checkdatas","found");
                                    if(db.update("tbl_monitoring",columns,data,"uid",uid)){
                                        Log.d("updatedata","update");
                                    }else{
                                        Log.d("updatedata","not update");
                                    }
                                }else{
                                    Log.d("checkdatas","not found");
                                    if(db.add("tbl_monitoring",columns,data,"")){
                                        Log.d("tbl_monitoring","added");
                                    }else{
                                        Log.d("tbl_monitoring","not added");
                                    }
                                }
                              JSONArray jsonArray = new JSONArray(response);
//                              JSONObject obj = new JSONObject(response);
                              //Log.d("daa",jsonArray.getJSONArray(0).toString());
                              JSONArray items = jsonArray.getJSONArray(0);
                            for(int i=0;i<items.length();i++){
                                String id = items.getJSONObject(i).getString("monid");
                                String date = items.getJSONObject(i).getString("date_added");
                                //String status = items.getJSONObject(i).getString("assessmentStatus");
//                                String status = items.getJSONObject(i).getString("monStatus");
                                String status = items.getJSONObject(i).getString("status");
                                String facilityname = items.getJSONObject(i).getString("name_of_faci");
                                String code = items.getJSONObject(i).getString("type_of_faci");
                                String typefacility = items.getJSONObject(i).getString("type_of_faci");
                                if(typefacility.equals("")){
                                        typefacility = "";
                                }

                                String appid = items.getJSONObject(i).getString("appid");
                                list.add(new Monitoring(id,code,facilityname,typefacility,date,status,appid));
                            }
                            setAdapter();
                            adapter.notifyDataSetChanged();
                            Log.d("monitorresponse",response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        bar.setVisibility(View.GONE);
                        Log.d("monitoring",response);

                        showHideEmptyDataMessage();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("isMobile","true");
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }




}
