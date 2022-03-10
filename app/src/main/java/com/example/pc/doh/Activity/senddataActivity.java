package com.example.pc.doh.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.pc.doh.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class senddataActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lv;
    ArrayList list = new ArrayList();
    private BluetoothAdapter BA;

    int REQUEST_ENABLE = 1;
    Intent bluetooth;
    private Set<BluetoothDevice>pairedDevices;
    Button btnscan;
    ArrayAdapter adapter;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendactivity);


        BA = BluetoothAdapter.getDefaultAdapter();
        btnscan = findViewById(R.id.btnscan);
        lv = findViewById(R.id.pairlist);
        if(BA==null){

        }else{
            turnonbluetooth();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(senddataActivity.this,HomeActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(senddataActivity.this);
                turnoffbluetooth();

            }
        });
        getSupportActionBar().setTitle("Send Data");


        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                // When discovery finds a device
                Log.d("rescice","receive");
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                   /* if( !PairedDeviceNames.contains(device) && !newDevices.contains(device))
                        newDevices.add(device);*/
                    Log.d("BAbluetooth","found");
                    Log.d("BAbluetooth",device.getName());
                    Toast.makeText(getApplicationContext(),device.getName(),Toast.LENGTH_SHORT).show();
                    list.add(device.getName());

                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.d("BAbluetooth","discovering");
                    if(list.size() != 0)
                    {
                       /* deviceList.invalidateViews();
                        sectionAdapter.notifyDataSetChanged();*/
                    }
                    else
                    {
                        Toast.makeText(senddataActivity.this, "No New Devices Found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);


        btnscan.setOnClickListener(this);

    }




    private void turnonbluetooth(){
        if(!BA.isEnabled()){
              bluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
              startActivityForResult(bluetooth,REQUEST_ENABLE);
              visible();
        }
    }

    private void visible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);

    }




    private void listpaireddevice(){
        pairedDevices = BA.getBondedDevices();



        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());


        adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }



    private void turnoffbluetooth(){
        BA.disable();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ENABLE){
               if(resultCode == RESULT_OK){
                 Toast.makeText(getApplicationContext(),"Bluetooth Turn on",Toast.LENGTH_SHORT).show();
                   listpaireddevice();

               }else if(resultCode == RESULT_CANCELED){
                 Toast.makeText(getApplicationContext(),"Bluetooh Cancel",Toast.LENGTH_SHORT).show();
               }
        }
    }

    @Override
    public void onClick(View v) {
        BA.startDiscovery();
        Toast.makeText(getApplicationContext(),"Scanning",Toast.LENGTH_SHORT).show();
    }
}
