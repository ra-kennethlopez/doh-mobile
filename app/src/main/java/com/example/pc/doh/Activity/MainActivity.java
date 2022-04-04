package com.example.pc.doh.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.example.pc.doh.Activity.HomeActivity;
import com.example.pc.doh.BuildConfig;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.NetworkStateChecker;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnlogin;
    EditText txtlogin,txtpass;
    TextView forgotpass,txtversion;
    InternetCheck checker;

    ScrollView sv;
    DatabaseHelper db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        txtversion = findViewById(R.id.txtversion);
        sv = findViewById(R.id.loginpage);
        txtlogin = findViewById(R.id.txt_login);
        txtpass = findViewById(R.id.txt_pass);
        forgotpass = findViewById(R.id.txt_forgot_pass);
        btnlogin = findViewById(R.id.btnlogin);
        btnlogin.setOnClickListener(this);
        forgotpass.setOnClickListener(this);

        txtversion.setText("Version "+BuildConfig.VERSION_NAME);
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, HomeActivity.class));
            Animatoo.animateFade(this);

            return;
        }



    }

    @Override
    public void onClick(View v) {
        //login();
        int id = v.getId();
        switch (id){
            case R.id.btnlogin:
                if(checker.checkHasInternet()){
                    onlinelogin();
                }else{
                    offline_login();
                }
                break;
            case R.id.txt_forgot_pass:
                Intent forgotpass = new Intent(this,ForgotPasswordActivity.class);
                startActivity(forgotpass);
                break;
        }
       /* Intent home = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(home);*/
    }

    private void offline_login(){
        final String login = txtlogin.getText().toString();
        final String pass = txtpass.getText().toString();

        if(TextUtils.isEmpty(login)){
            txtlogin.setError("Username is Empty");
            return;
        }
        if(TextUtils.isEmpty(pass)){
            txtpass.setError("Password is Empty");
            return;
        }
        final ProgressBar bar = findViewById(R.id.loginProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Cursor det = db.list("tbl_user");
        boolean check = false;
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while (!det.isAfterLast()){
                String uid = det.getString(det.getColumnIndex("uid"));
                String pinpassword = det.getString(det.getColumnIndex("pinpassword"));
                if(uid.equals(login.toUpperCase()) && pass.equals(pinpassword)){
                    check = true;
                    try {
                        JSONObject obj = new JSONObject(det.getString(det.getColumnIndex("json_data")));
                        JSONObject jsonArray = obj.getJSONObject("data");
                        UserModel user = new UserModel(jsonArray.getString("uid"),"","", jsonArray.getString("name"));
                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                        Intent home = new Intent(getApplicationContext(),HomeActivity.class);
                        home.putExtra("uid",jsonArray.getString("uid"));
                        startActivity(home);
                        finish();
                        Animatoo.animateSlideLeft(MainActivity.this);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                det.moveToNext();
            }
            if(!check){
                Toast.makeText(this,"Incorrect Username or Password",Toast.LENGTH_SHORT).show();
                //Toast.makeText(this,"You must login first in online. Thank You",Toast.LENGTH_LONG).show();
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
            }
        }else{

                Toast.makeText(this,"You must login first in online. Thank You.",Toast.LENGTH_LONG).show();
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);


        }


    }


    private void onlinelogin(){
        final String login = txtlogin.getText().toString();
        final String pass = txtpass.getText().toString();
//        final String login = "MOBILEUSER";
//        final String pass = "!Password1234";

//        final String login = "ADMIN";
//        final String pass = "!Password1234";

//        final String login = "JJSORIANO";
//        final String pass = "!Password12345";
        if(TextUtils.isEmpty(login)){
            txtlogin.setError("Username is Empty");
            return;
        }
        if(TextUtils.isEmpty(pass)){
            txtpass.setError("Password is Empty");
            return;
        }

        final ProgressBar bar = findViewById(R.id.loginProgress);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        StringRequest request = new StringRequest(Request.Method.POST,Urls.employee,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("======================", "onResponse: " + response);

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject jsonArray = new JSONObject();

                            if(obj.has("data")){
                                jsonArray = obj.getJSONObject("data");
                                ///saving offline purpose
                                String[] columns = {"uid","json_data","pinpassword","islogin"};
                                String[] data = {jsonArray.getString("uid"),response,"","0"};
                                if(db.checkDatas("tbl_user","uid",jsonArray.getString("uid"))){
                                 Log.d("checkdatas","found");
                                    if(db.update("tbl_user",columns,data,"uid",jsonArray.getString("uid"))){
                                        Log.d("updatedata","update");
                                    }else{
                                        Log.d("updatedata","not update");
                                    }
                                }else{
                                    Log.d("checkdatas","not found");
                                    String[] datas = {jsonArray.getString("uid"),response,"","1"};
                                    if(db.add("tbl_user",columns,datas,"")){
                                        Log.d("tbl_user","added");
                                    }else{
                                        Log.d("tbl_user","not added");
                                    }

                                }

                            }


                            String status = obj.getString("status");
                            if(status.equals("success")){
                                 UserModel user = new UserModel(jsonArray.getString("uid"),"","", jsonArray.getString("name"));
                                 //storing the user in shared preferences
                                 SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                 Intent home = new Intent(getApplicationContext(),HomeActivity.class);
                                 home.putExtra("uid",jsonArray.getString("uid"));
                                 startActivity(home);
                                 finish();
                                 Animatoo.animateSlideLeft(MainActivity.this);
                            }else{
                                  Toast.makeText(getApplicationContext(),obj.getString("message"),Toast.LENGTH_SHORT).show();
                                  sv.setVisibility(View.VISIBLE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("message",response);
                        bar.setVisibility(View.GONE);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("uname",login);
                params.put("pass",pass);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

}
