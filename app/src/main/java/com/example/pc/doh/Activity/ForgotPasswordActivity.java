package com.example.pc.doh.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.R;
import com.example.pc.doh.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtemail;
    Button btnok;
    InternetCheck checker;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_layout);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        // set the toolbar label
        //getSupportActionBar().setTitle("Hello world App");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(ForgotPasswordActivity.this,MainActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(ForgotPasswordActivity.this);
            }
        });
        checker = new InternetCheck(this);
        txtemail = findViewById(R.id.txt_email);
        btnok = findViewById(R.id.btnok);
        btnok.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btnok:
                if(checker.checkHasInternet()){
                    sendEmail();
                }else{
                    Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public void sendEmail(){
        //String url = "http://192.168.254.99:8080/doholrs4/employee/forgot";
        final String email = txtemail.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST,Urls.forgot,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("status").equals("success")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                builder.setTitle("Message");
                                builder.setMessage("Please check your Email to reset your Password");
                                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent main = new Intent(ForgotPasswordActivity.this,MainActivity.class);
                                        startActivity(main);
                                        ForgotPasswordActivity.this.finish();
                                    }
                                });
                                builder.create().show();


                            }else if(obj.getString("status").equals("error")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                builder.setTitle("MESSAGE");
                                builder.setMessage("No Account bound to this Email");
                                builder.setNeutralButton("OK",null);
                                builder.create().show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("email",email);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
