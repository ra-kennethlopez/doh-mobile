package com.example.pc.doh.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtoldpass,txtnewpass,txtretypepass;
    Button btnsave;
    InternetCheck check;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

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
                Intent assestment = new Intent(ChangePasswordActivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(ChangePasswordActivity.this);
            }
        });
        check = new InternetCheck(this);
        getSupportActionBar().setTitle("Account Settings");
        txtoldpass = findViewById(R.id.txt_old_pass);
        txtnewpass = findViewById(R.id.txt_new_pass);
        txtretypepass = findViewById(R.id.txt_retype_pass);
        btnsave = findViewById(R.id.btnpass);
        btnsave.setOnClickListener(this);




    }

    private void changepass(){
        String oldpass = txtoldpass.getText().toString();
        String newpass = txtnewpass.getText().toString();
        String retype = txtretypepass.getText().toString();

        if(TextUtils.isEmpty(oldpass)){
            txtoldpass.setError("Old Pass Empty!");
            return;
        }
        if(newpass.length()<10){
            txtnewpass.setError("10 to 32 characters in length");
            return;
        }

        if(isValidPassword(newpass)){
            txtnewpass.setError("Password Must have Upper Case,Lower Case and Number");
            return;
        }



        if(retype.length()<10){
            txtretypepass.setError("10 to 32 characters in length");
            return;
        }

        if(isValidPassword(retype)){
            txtretypepass.setError("Password Must have Upper Case,Lower Case and Number");
            return;
        }

        if(newpass.equals(retype)){
           /*StringRequest request = new StringRequest(Request.Method.POST, Urls.forgot,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params= new HashMap<>();
                params.put("","");
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);*/
        }
    }
    @Override
    public void onClick(View v) {
        if(check.checkHasInternet()){
            changepass();
        }else{
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    private static boolean checkString(String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (Character.isDigit(ch)) {
                numberFlag = true;
            } else if (Character.isUpperCase(ch)) {
                capitalFlag = true;
            } else if (Character.isLowerCase(ch)) {
                lowerCaseFlag = true;
            }
            if (numberFlag && capitalFlag && lowerCaseFlag)
                return true;
        }
        return false;
    }
}
