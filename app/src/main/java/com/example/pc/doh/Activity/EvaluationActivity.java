package com.example.pc.doh.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.Adapter.AssestmentAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.AssestmentModel;
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

public class EvaluationActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private String uid;
    DatabaseHelper db;
    private InternetCheck checker;
    List<AssestmentModel> assesslist = new ArrayList<>();
    AssestmentAdapter aAdapter;
    public static String type,code,faclityname,typefacility,date,status,appid,count;
    public static String monType;
    public static String restpye = "";
    private SearchView searchView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluationlayout);
        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        recyclerView = findViewById(R.id.assesetmentrv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(EvaluationActivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(EvaluationActivity.this);
            }
        });

        getSupportActionBar().setTitle("Evaluation");

        if(checker.checkHasInternet()){
            get_assessment_online();
        }else{
            get_assessment_offline();
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
                if(assesslist.size()>0){
                    aAdapter.getFilter().filter(query);
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

    private void get_assessment_offline(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        recyclerView.setVisibility(View.GONE);
        Cursor det = db.get_item_Assessment("tbl_assessment",uid,"evaluation");
        boolean check = false;
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while (!det.isAfterLast()){
                try {
                    JSONObject obj = new JSONObject(det.getString(det.getColumnIndex("json_data")));
                    JSONArray jsonArray = obj.getJSONArray("BigData");
                    if(obj.has("BigData")){
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
                            if(id.equals("PTC")){
                                assesslist.add(new AssestmentModel(id,code,facilityname,typefacility,date,status,appid));
                            }
                        }
                        setAdapter();
                    }else{
                        TextView lbl = findViewById(R.id.lblmessage);
                        lbl.setVisibility(View.VISIBLE);
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
        StringRequest request = new StringRequest(Request.Method.POST, Urls.evaluation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("evaluateresponse",response);
                        try {
                            JSONObject obj = new JSONObject(response);

                            //changes
                            String[] columns = {"uid","json_data","type"};
                            String[] data = {uid,response,"evaluation"};
                            Log.d("userid",uid);
                            //SELECT * FROM tbl_assessment where uid = 'ADMIN' and type = 'evaluation'
                            if(db.checkLicensingDatas("tbl_assessment",uid,"evaluation")){
                                Log.d("checkdatas","found");
                                String aid = db.getassesid(uid,"evaluation");
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


                            JSONArray jsonArray = obj.getJSONArray("BigData");
                            if(obj.has("BigData")){
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
                                    if(id.equals("PTC")){
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
                                TextView lbl = findViewById(R.id.lblmessage);
                                lbl.setVisibility(View.VISIBLE);
                                lbl.setText("No Data Available");
                            }

                            recyclerView.setVisibility(View.VISIBLE);
                            bar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
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

    public void setAdapter(){
        aAdapter = new AssestmentAdapter(EvaluationActivity.this,assesslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(EvaluationActivity.this));
        recyclerView.setAdapter(aAdapter);


        aAdapter.setonItemClickListener(new AssestmentAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {


                Intent i = new Intent(EvaluationActivity.this, AssestmentDetailsActivity.class);

                EvaluationActivity.this.startActivity(i);
                Animatoo.animateSlideLeft(EvaluationActivity.this);
                code = assesslist.get(position).getCode();
                type = assesslist.get(position).getType();
                faclityname = assesslist.get(position).getFaclityname();
                typefacility = assesslist.get(position).getTypefacility();
                date = assesslist.get(position).getDate();
                status = assesslist.get(position).getStatus();
                appid = assesslist.get(position).getAppid();
                monType = "license";
                restpye = "assessment";
                HomeActivity.licensetype = "evaluation";
                finish();


            }


        });
    }
}
