package com.example.pc.doh.Activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssestmentDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnoption;
    TextView assesscode,assesstitle,assessdate,assessappcode,assessfacility,assessstatus;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assesstment_details);



        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        UserModel user = SharedPrefManager.getInstance(this).getUser();

        uid = user.getId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(HomeActivity.licensetype.equals("assessment")){
                    Intent assestment = new Intent(AssestmentDetailsActivity.this,HomeActivity.class);
                    startActivity(assestment);
                    Animatoo.animateSlideRight(AssestmentDetailsActivity.this);
                }else{
                    Intent evaluation = new Intent(AssestmentDetailsActivity.this,EvaluationActivity.class);
                    startActivity(evaluation);
                    Animatoo.animateSlideRight(AssestmentDetailsActivity.this);
                }
            }
        });

        btnoption = findViewById(R.id.btnassessoption);
        assesscode = findViewById(R.id.txtassesstype);
        assesstitle = findViewById(R.id.txtassesstitle);
        assessdate = findViewById(R.id.txtassessdate);
        assessappcode = findViewById(R.id.txtassessappcode);
        assessfacility = findViewById(R.id.txtassessfacility);
        assessstatus = findViewById(R.id.txtassessstatus);
        btnoption.setOnClickListener(this);
        Log.d("licensetype",HomeActivity.licensetype);
        //disp_details();
        if(HomeActivity.licensetype.equals("assessment")){
            disp_assessdetails();
        }else{
            disp_evaluatedetails();
        }
    }

    private void disp_evaluatedetails(){
        getSupportActionBar().setTitle(EvaluationActivity.code);
        assesscode.setText(EvaluationActivity.type);
        assesstitle.setText(EvaluationActivity.faclityname);

        assessappcode.setText(EvaluationActivity.code);
        if(EvaluationActivity.typefacility.equals("null")){
            assessfacility.setText("No details");
        }else{
            assessfacility.setText(EvaluationActivity.typefacility);
        }

        assessdate.setText(EvaluationActivity.date);
        assessstatus.setText(EvaluationActivity.status);
    }

    private void disp_assessdetails(){
        getSupportActionBar().setTitle(HomeActivity.code);

        assesscode.setText(HomeActivity.type);
        assesstitle.setText(HomeActivity.faclityname);

        assessappcode.setText(HomeActivity.code);
        if(HomeActivity.typefacility.equals("null")){
            assessfacility.setText("No details");
        }else{
            assessfacility.setText(HomeActivity.typefacility);
        }
        assessdate.setText(HomeActivity.date);
        assessstatus.setText(HomeActivity.status);
    }
    @Override
    public void onClick(View v) {

        if(HomeActivity.licensetype == "assessment"){

            Intent assessheader = new Intent(this,AssessmentPart.class);
            startActivity(assessheader);
            Animatoo.animateSlideLeft(AssestmentDetailsActivity.this);

        }else{

            Intent evaluationheader = new Intent(this,EvaluationPart.class);
            startActivity(evaluationheader);
            Animatoo.animateSlideLeft(AssestmentDetailsActivity.this);
        }

    }




}
