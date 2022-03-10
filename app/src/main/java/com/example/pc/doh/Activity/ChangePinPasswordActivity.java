package com.example.pc.doh.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePinPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtoldpass,txtnewpass,txtretypepass;
    Button btnsave;
    DatabaseHelper db;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepinpassword);

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        db = new DatabaseHelper(this);
        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(ChangePinPasswordActivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(ChangePinPasswordActivity.this);
            }
        });
        getSupportActionBar().setTitle("Account Settings");
        txtoldpass = findViewById(R.id.txt_old_pass);
        txtnewpass = findViewById(R.id.txt_new_pass);
        txtretypepass = findViewById(R.id.txt_retype_pass);
        btnsave = findViewById(R.id.btnpass);
        btnsave.setOnClickListener(this);




    }

    public String get_pin_password(){
        Cursor det = db.get_item("tbl_user","uid",uid);
        String password = "";
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while(!det.isAfterLast()){
                String pass = det.getString(det.getColumnIndex("pinpassword"));
                password = pass;
                det.moveToNext();
            }
        }
        return password;
    }

    @Override
    public void onClick(View v) {
        String oldpass = txtoldpass.getText().toString();
        String newpass = txtnewpass.getText().toString();
        String retype = txtretypepass.getText().toString();
        String pinpassword = get_pin_password();
        if(TextUtils.isEmpty(oldpass)){
            txtoldpass.setError("Old Pass Empty!");
            return;
        }
        if(TextUtils.isEmpty(newpass)){
            txtnewpass.setError("New Pass Empty!");
            return;
        }

        if(TextUtils.isEmpty(retype)){
            txtretypepass.setError("Re Type Pass Empty!");
            return;
        }

        if(!newpass.equals(retype)){
            txtnewpass.setError("New Pass Not Match in Re type Pass!");
            return;
        }

        if(!retype.equals(newpass)){
            txtretypepass.setError("Re type Pass Not Match in New Pass!");
            return;
        }

        if(newpass.equals(retype)){
            Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show();
            String[] columns = {"pinpassword"};
            String[] data = {newpass};
            if(db.update("tbl_user",columns,data,"uid",uid)){
                Log.d("updatedata","update");
                Toast.makeText(this,"Successfully Change Password",Toast.LENGTH_SHORT).show();
                Intent home = new Intent(this,HomeActivity.class);
                startActivity(home);
                finish();
            }else{
                Log.d("updatedata","not update");
            }
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
