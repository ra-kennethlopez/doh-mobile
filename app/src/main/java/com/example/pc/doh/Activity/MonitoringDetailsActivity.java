package com.example.pc.doh.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;

public class MonitoringDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnoption;
    TextView assesscode,assesstitle,assessdate,assessappcode,assessfacility,assessstatus;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitordetails);
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
                Intent assestment = new Intent(MonitoringDetailsActivity.this,MonitoringActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(MonitoringDetailsActivity.this);
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
        disp_details();
    }

    private void disp_details(){
        getSupportActionBar().setTitle(MonitoringActivity.faclityname);
        assesscode.setText(MonitoringActivity.type);
        assesstitle.setText(MonitoringActivity.faclityname);

        assessappcode.setText(MonitoringActivity.code);
        if(MonitoringActivity.typefacility.equals("0")){
            assessfacility.setText("For Inspection");
        }else{
            assessfacility.setText("For Approved");
        }

        assessdate.setText(MonitoringActivity.date);
        /*
        if(MonitoringActivity.status.equals("0")){
            assessstatus.setText("For Inspection");
        }else{
            assessstatus.setText("For Approved");
        }*/
        assessstatus.setText(MonitoringActivity.status);

    }

    @Override
    public void onClick(View v) {
        Intent assessheader = new Intent(this,MonitoringPart.class);
        startActivity(assessheader);
        Animatoo.animateSlideLeft(MonitoringDetailsActivity.this);
    }
}
