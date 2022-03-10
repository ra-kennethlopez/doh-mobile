package com.example.pc.doh.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.pc.doh.Adapter.AssestmentAdapter;
import com.example.pc.doh.Adapter.HeadersAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.Headers;
import com.example.pc.doh.Model.PersonnelPage;
import com.example.pc.doh.Model.Save;
import com.example.pc.doh.Model.Srvasmtcols;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;

import com.itextpdf.text.ListItem;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentHeaderActvitiy extends AppCompatActivity implements View.OnClickListener {

    Button btnsummary;
    TextView lblfacname;
    public static String asmt2l_id;
    ArrayList<Headers> list = new ArrayList<>();
    HeadersAdapter hAdapter;
    RecyclerView headerrv;
    public static String id;
    InternetCheck checker;
    DatabaseHelper db;
    private String uid;

    private static final String TAG = "PdfCreatorActivity";
    private EditText mContentEditText;
    private Button mCreateButton;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    Image image;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    ProgressDialog dialog;
    String filename = "";
    //{id}/{apptype}/{HH001}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assessment_header);
        
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        btnsummary = findViewById(R.id.btnshowsummary);
        lblfacname = findViewById(R.id.facname);
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
                Intent assestment = new Intent(AssessmentHeaderActvitiy.this, AssestmentDetailsActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(AssessmentHeaderActvitiy.this);
            }
        });
        getSupportActionBar().setTitle("SERVICES");
        lblfacname.setText(HomeActivity.faclityname);
        headerrv = findViewById(R.id.headerrv);

        if (checker.checkHasInternet()) {
            get_specific_assessment_online();
        } else {
            get_specific_assessment_offline();
        }
        hAdapter = new HeadersAdapter(this, list);
        headerrv.setLayoutManager(new LinearLayoutManager(this));
        headerrv.setAdapter(hAdapter);
        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                id = list.get(position).getAsmt2l_id();
                Intent assess = new Intent(getApplicationContext(), PersonnelDetailsActivity.class);
                startActivity(assess);

            }
        });


        btnsummary.setOnClickListener(this);
        //btnsummary.setVisibility(View.VISIBLE);
        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateheader();
                    }
                }
        );
    }


    private void updateheader() {
        // TODO implement a refresh
        mySwipeRefreshLayout.setRefreshing(false); // Disables the refresh icon
    }

    public void get_specific_assessment_offline() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Cursor det = db.get_item("tbl_assessment_headers", "appid", HomeActivity.appid);
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject headers = obj.getJSONObject("headers");

                    if (headers.length() > 0) {
                        for (int i = 0; i < headers.length() - 1; i++) {
                            JSONObject items = headers.getJSONObject(i + "");

                            String asmt2l_id = items.getString("asmt2l_id");
                            String asmt2l_desc = items.getString("asmt2l_desc");

                            //change the check_has_save_assessment(String uid,String appid)
                            if (db.check_has_save_assessments(HomeActivity.appid)) {
                                //change the get_json_data_tbl_save_assessment_headers(uid,HomeActivity.appid)
                                String header = db.get_json_tbl_save_assessment_header(HomeActivity.appid);
                                String assesscheckifcomplete = "false";
                                JSONArray head = new JSONArray(header);
                                for (int h = 0; h < head.length(); h++) {
                                    String name = head.getString(h);
                                    JSONObject headname = new JSONObject(name);
                                    if (asmt2l_id.equals(headname.getString("name"))) {
                                        assesscheckifcomplete = "true";
                                        checkifcomplied = true;
                                        break;
                                    } else {
                                        checkifcomplied = false;
                                    }

                                }
                                //list.add(new Headers(asmt2l_id, asmt2l_desc, assesscheckifcomplete));
                            } else {

                                //list.add(new Headers(asmt2l_id, asmt2l_desc, "false"));
                            }


                        }
                        if (list.size() == 0) {
                            TextView lbl = findViewById(R.id.lblheadmessage);
                            lbl.setVisibility(View.VISIBLE);
                        }

                    }

                    if (headers.getBoolean("hasNull")) {
                        //change the check_has_save_assessment(String uid,String appid)
                        if (db.check_has_save_assessment(uid, HomeActivity.appid)) {
                            //change the get_json_data_tbl_save_assessment_headers(uid,HomeActivity.appid)  not3e
                            String assesscheckifcomplete = "false";
                            String other = "OTHERS";
                            String header = db.get_json_data_tbl_save_assessment_headers(uid, HomeActivity.appid);
                            JSONArray head = new JSONArray(header);
                            for (int h = 0; h < head.length(); h++) {
                                String name = head.getString(h);
                                JSONObject headname = new JSONObject(name);

                                if (other.equals(headname.getString("name"))) {

                                    assesscheckifcomplete = "true";
                                    checkifcomplied = true;
                                    break;
                                } else {
                                    checkifcomplied = false;
                                }

                            }

                            //list.add(new Headers("OTHERS", "OTHERS", assesscheckifcomplete));
                            get_data_headers_details("OTHERS", HomeActivity.appid);
                        } else {
                            //list.add(new Headers("OTHERS", "OTHERS", "false"));
                        }
                    }

                    bar.setVisibility(View.GONE);
                    sv.setVisibility(View.VISIBLE);
                    if (checkifcomplied == true) {
                        btnsummary.setVisibility(View.VISIBLE);
                        /*Log.d("summary","true");*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                det.moveToNext();
            }
        } else {
            TextView lbl = findViewById(R.id.lblheadmessage);
            lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }

    }

    public void get_specific_assessment_online() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        //+ HomeActivity.appid + "/" + HomeActivity.type
        StringRequest request = new StringRequest(Request.Method.POST, Urls.getassessment,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);
                        try {
                            boolean checkifcomplied = false;
                            int cheader = 0;
                            int checkheader = 0;
                            //
                            JSONObject obj = new JSONObject(response);

                            String[] columns = {"json_data"};
                            String[] data = {response};
                            if (db.checkDatas("tbl_assessment_headers", "appid", HomeActivity.appid)) {

                                if (db.update("tbl_assessment_headers", columns, data, "appid", obj.getJSONObject("AppData").getString("appid"))) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }

                            } else {

                                String[] dcolumns = {"json_data", "uid", "appid"};
                                String[] datas = {response, uid, obj.getJSONObject("AppData").getString("appid")};
                                if (db.add("tbl_assessment_headers", dcolumns, datas, "")) {
                                    Log.d("tbl_assessment_headers", "added");
                                } else {
                                    Log.d("tbl_assessment_headers", "not added");
                                }

                                AlertDialog.Builder builder = new AlertDialog.Builder(AssessmentHeaderActvitiy.this);
                                builder.setTitle("DOHOLRS");
                                builder.setMessage("Successfully Save Data");
                                builder.setNeutralButton("OK",null);
                                AlertDialog dialog = builder.create();
                                dialog.setIcon(R.drawable.doh);
                                dialog.show();

                                /*Toast.makeText(AssessmentHeaderActvitiy.this, "Save Data", Toast.LENGTH_SHORT).show();*/

                            }

                            JSONObject headers = obj.getJSONObject("headers");
                            if (headers.length() > 0) {
                                for (int i = 0; i < headers.length() - 1; i++) {
                                    JSONObject items = headers.getJSONObject(i + "");
                                    Log.d("items", items.toString());
                                    String asmt2l_id = items.getString("asmt2l_id");
                                    String asmt2l_desc = items.getString("asmt2l_desc");


                                    get_data_headers_details(asmt2l_id, HomeActivity.appid);


                                    String assesscheckifcomplete = "false";
                                    if (db.check_has_save_assessment(uid, HomeActivity.appid)) {
                                        String header = db.get_json_data_tbl_save_assessment_headers(uid, HomeActivity.appid);
                                        JSONArray head = new JSONArray(header);
                                        for (int h = 0; h < head.length(); h++) {
                                            String name = head.getString(h);
                                            JSONObject headname = new JSONObject(name);

                                            if (asmt2l_id.equals(headname.getString("name"))) {
                                                checkifcomplied = true;
                                                assesscheckifcomplete = "true";
                                                checkheader++;
                                                break;
                                            } else {
                                                checkifcomplied = false;
                                            }

                                        }
                                        cheader++;
                                        //list.add(new Headers(asmt2l_id, asmt2l_desc, assesscheckifcomplete));
                                    } else {

                                        //list.add(new Headers(asmt2l_id, asmt2l_desc, "false"));
                                    }


                                }
                                if (list.size() == 0) {
                                    TextView lbl = findViewById(R.id.lblheadmessage);
                                    lbl.setVisibility(View.VISIBLE);
                                }

                            }

                            if (headers.getBoolean("hasNull")) {
                                String other = "OTHERS";
                                String assesscheckifcomplete = "false";
                                get_data_headers_details("OTHERS", HomeActivity.appid);
                                if (db.check_has_save_assessment(uid, HomeActivity.appid)) {
                                    String header = db.get_json_data_tbl_save_assessment_headers(uid, HomeActivity.appid);
                                    JSONArray head = new JSONArray(header);
                                    for (int h = 0; h < head.length(); h++) {
                                        String name = head.getString(h);
                                        JSONObject headname = new JSONObject(name);
                                        Log.d("namesss", headname.getString("name"));
                                        if (other.equals(headname.getString("name"))) {
                                            Log.d("checksssss", "found");
                                            checkifcomplied = true;
                                            assesscheckifcomplete = "true";
                                            checkheader++;
                                            break;
                                        } else {
                                            checkifcomplied = false;
                                        }

                                    }
                                    cheader++;
                                    //list.add(new Headers("OTHERS", "OTHERS", assesscheckifcomplete));

                                } else {
                                    //list.add(new Headers("OTHERS", "OTHERS", "false"));
                                }


                            }
                            if(cheader!=0){
                                if (cheader == checkheader) {
                                    btnsummary.setVisibility(View.VISIBLE);
                                }
                            }



                            hAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        bar.setVisibility(View.GONE);
                        sv.setVisibility(View.VISIBLE);
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
                params.put("isMobile","dan");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    public boolean checkhospitalpart2(){
        boolean check = false;
        Cursor det = db.get_item_json_data("tbl_assessment_details","HH001",HomeActivity.appid);
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while(!det.isAfterLast()){
                String json = det.getString(det.getColumnIndex("json_data"));

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject appdata = obj.getJSONObject("AppData");
                    String files = obj.getString("filenames");
                    Log.d("filenamesfilenames",files);
                    JSONArray filenames = new JSONArray(files);
                    for(int f=0;f<filenames.length();f++){
                        filename = filenames.getString(f);
                        if(filename.equals("hospitalpart4level2")){
                            check = true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                det.moveToNext();
            }
        }
        return check;
    }

    public boolean checkhospitalpart1(){
        boolean check = false;
        Cursor det = db.get_item_json_data("tbl_assessment_details","HH001",HomeActivity.appid);
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while(!det.isAfterLast()){
                String json = det.getString(det.getColumnIndex("json_data"));

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject appdata = obj.getJSONObject("AppData");
                    String files = obj.getString("filenames");
                    Log.d("filenamesfilenames",files);
                    JSONArray filenames = new JSONArray(files);
                    for(int f=0;f<filenames.length();f++){
                        filename = filenames.getString(f);
                        if(filename.equals("hospitalpart4level1")){
                            check = true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                det.moveToNext();
            }
        }
        return check;
    }


    public void get_data_headers_details(final String id, final String appid) {
        final ArrayList<PersonnelPage> temp = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, Urls.assess + HomeActivity.appid + "/" + HomeActivity.type + "/" + id + "?uid=" + uid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        String[] columns = {"json_data"};
                        String[] data = {response};
                        if (db.checkDatasAssesment("tbl_assessment_details", appid, id)) {
                            Log.d("checkdatas", "found");
                            if (db.update("tbl_assessment_details", columns, data, "asmt2l_id", id)) {
                                Log.d("updatedata", "update");
                            } else {
                                Log.d("updatedata", "not update");
                            }

                        } else {


                            String[] dcolumns = {"json_data", "uid", "asmt2l_id", "appid"};
                            String[] datas = {response, uid, id, appid};

                            if (db.add("tbl_assessment_details", dcolumns, datas, "")) {
                                Log.d("tbl_assessment_details", "added");

                            } else {
                                Log.d("tbl_assessment_details", "not added");
                            }

                        }


                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }


    private void createPdfWrapper() throws FileNotFoundException, DocumentException {

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
        } else {

           generate();

        }
    }


    private void generate(){
        class generatePdf extends AsyncTask<Void, Void, String> {
            private ProgressBar progressBar;
            ProgressDialog dialog;

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    createPdf();
                    //createnursingservices();
                    //createhospitallevel3();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(AssessmentHeaderActvitiy.this, "",
                        "Create Summmary. Please wait...", true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion
                dialog.dismiss();
            }
        }

        //executing the async task
        generatePdf c = new generatePdf();
        c.execute();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
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
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



    public String get_col_results(String header,String jindex,String appid){
        Cursor result = db. get_tbl_SrvasmtcolsList_Result(appid,header,jindex);
        String res = "";
        if(result!=null && result.getCount()>0){
            result.moveToFirst();
            while (!result.isAfterLast()){
                String jsondata = result.getString(result.getColumnIndex("answers_json_data"));
                String scol = result.getString(result.getColumnIndex("srvasmt_col"));
                try {
                    JSONArray srvasmtcol = new JSONArray(scol);
                    for(int s=0;s<srvasmtcol.length();s++){


                        if(!jsondata.equals("")){
                            JSONObject dataobj = new JSONObject(jsondata);
                            JSONArray jsonArray = dataobj.getJSONArray("result");

                            if(jsonArray!=null && jsonArray.length()>0){

                                String col = jsonArray.getJSONObject(0).getString(srvasmtcol.getString(s));
                                JSONArray ans = new JSONArray(col);
                                res = ans.getJSONObject(0).getString("result");

                            }else{

                            }


                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result.moveToNext();
            }



        }


        return res;

    }


    public String get_col_results1(String header,String jindex,String appid,int scols){
        Cursor result = db. get_tbl_SrvasmtcolsList_Result(appid,header,jindex);
        String res = "";
        if(result!=null && result.getCount()>0){
            result.moveToFirst();
            while (!result.isAfterLast()){
                String jsondata = result.getString(result.getColumnIndex("answers_json_data"));
                String scol = result.getString(result.getColumnIndex("srvasmt_col"));
                try {
                    JSONArray srvasmtcol = new JSONArray(scol);
                    if(!jsondata.equals("")){
                        JSONObject dataobj = new JSONObject(jsondata);
                        JSONArray jsonArray = dataobj.getJSONArray("result");

                        if(jsonArray!=null && jsonArray.length()>0){
                            Log.d("jsonArray","not null");
                            String col = jsonArray.getJSONObject(0).getString(srvasmtcol.getString(scols));
                            JSONArray ans = new JSONArray(col);
                            res = ans.getJSONObject(0).getString("result");

                        }else{
                            Log.d("jsonArray","null");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result.moveToNext();
            }



        }


        return res;

    }

    public String get_remarks_results(String header,String jindex,String appid){
        Cursor result = db. get_tbl_SrvasmtcolsList_Result(appid,header,jindex);
        String res = "";
        if(result!=null && result.getCount()>0){
            result.moveToFirst();
            while (!result.isAfterLast()){
                String jsondata = result.getString(result.getColumnIndex("remarks"));
                String scol = result.getString(result.getColumnIndex("srvasmt_col"));
                try {
                    JSONArray srvasmtcol = new JSONArray(scol);
                    for(int s=0;s<srvasmtcol.length();s++){


                        if(!jsondata.equals("")){
                            JSONObject dataobj = new JSONObject(jsondata);
                            JSONArray jsonArray = dataobj.getJSONArray("result");

                            if(jsonArray!=null && jsonArray.length()>0){
                                Log.d("jsonArray","not null");
                                String col = jsonArray.getJSONObject(0).getString("Remarks");
                                JSONArray ans = new JSONArray(col);
                                Log.d("result",ans.getJSONObject(0).getString("result"));
                                res = ans.getJSONObject(0).getString("result");

                            }else{
                                Log.d("jsonArray","null");
                            }


                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result.moveToNext();
            }



        }


        return res;

    }

    private Paragraph createtitle(String text){
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLD);
        Paragraph title = new Paragraph(text,fontbold);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        title.setIndentationLeft(70f);
        title.setIndentationRight(100f);
        return title;
    }

    private Paragraph createParagraph(String text){
        Paragraph par = new Paragraph(text);
        par.setSpacingBefore(10f);
        par.setSpacingAfter(10f);
        par.setIndentationLeft(70f);
        par.setIndentationRight(100f);
        return par;
    }

    private PdfPCell createCell(String text,int padding,int rowspan,int colspan,boolean ifbold){
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        PdfPCell cell;
        if(ifbold == true){
            cell = new PdfPCell(new Paragraph(text,fontbold));

        }else{
            cell = new PdfPCell(new Paragraph(text));
        }
        if(colspan != 0){
            cell.setColspan(colspan);
        }
        if(rowspan !=0){
            cell.setRowspan(rowspan);
        }
        cell.setPadding(padding);

        return cell;
    }

    private void createhospitallevel3() throws FileNotFoundException, DocumentException{
        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"assessment.pdf");
        //Document document = new Document(pagesize);
        Rectangle pagesize = new Rectangle(612, 861);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(5,5,5,5);
        PdfWriter.getInstance(document, output);
        document.open();

        Paragraph title = createtitle("ATTACHMENT 2.A – PERSONNEL");
        Paragraph title2 = createtitle("ATTACHMENT 3.B – PHYSICAL PLANT");
        Paragraph title3 = createtitle("OBSERVATION/FINDINGS (may use separate additional sheet if needed)");
        Paragraph title4 = createtitle("ATTACHMENT 3.C- EQUIPMENT/INSTRUMENT");
        Paragraph title5 = createtitle("ATTACHMENT 3.D – EMERGENCY CART CONTENTS FOR LEVEL 3 HOSPITAL");
        Paragraph title6 = createtitle("*Notes:\n" +
                "        ER – Emergency Room\n" +
                "        OR – Operating Room\n" +
                "        DR – Delivery Room\n" +
                "        NS – Nurses’ Station");



        float [] pointColumnWidths = {450F, 700F, 700F,350F,350F,430F};
        PdfPTable tablelevel3 = new PdfPTable(pointColumnWidths);
        tablelevel3.setTotalWidth(600);
        tablelevel3.setLockedWidth(true);
        PdfPTable table2level3 = new PdfPTable(3);
        table2level3.setTotalWidth(600);
        table2level3.setLockedWidth(true);
        PdfPTable table3level3 = new PdfPTable(1);
        table3level3.setTotalWidth(600);
        table3level3.setLockedWidth(true);
        PdfPTable table4level3 = new PdfPTable(5);
        table4level3.setTotalWidth(600);
        table4level3.setLockedWidth(true);
        float [] pointColumn = {500F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,300F};
        PdfPTable table5level3 = new PdfPTable(pointColumn);
        table5level3.setTotalWidth(600);
        table5level3.setLockedWidth(true);

        String[][] desc = {{"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"},
                           //index 1 colspan 6
                           {"TOP MANAGEMENT (Should be full-time)"},
                           {"Chief of Hospital/Medical Director","Licensed physician \n" +
                                   "\n" +
                                   "Government hospital: (a) Master’s Degree in Hospital Administration or related course (MPH, MBA, MPA, MHSA, etc.) AND at least five (5) years hospital experience in a supervisory or managerial position (b) Grandfather’s Clause may be accepted in place of the Master’s degree in Hospital administration (or related course), provided that the individual is fifty-five (55) and above, AND currently holding the position for at least five years. \n" +
                                   "\n" +
                                   "Private Hospital:\n" +
                                   "Master’s Degree in Hospital Administration or related course (MPH, MBA, MPA, MHSA, etc.) AND/OR at least five (5) years hospital experience in a supervisory or managerial position","DOCUMENT REVIEW\n" +
                                   "Diploma for Master’s Degree\n" +
                                   "Updated Physician PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/Appointment (notarized)\n" +
                                   "Service Record/Certificate of Employment (proof of Hospital supervisory/ managerial experience)","1","",""},
                           {"Chief of Clinics/ Chief of Medical Professional Services","","Licensed physician\n" +
                                   "Fellow/diplomate of a specialty/subspecialty society\n" +
                                   "At least five (5) years hospital experience in a clinical supervisory or managerial position","DOCUMENT REVIEW\n" +
                                   "Diploma/ Certificate from Specialty society, if applicable\n" +
                                   "Updated Physician PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/Appointment (notarized)\n" +
                                   "Service Record/Certificate of Employment (proof of clinical supervisory/managerial experience in hospital)","1",""},
                           //index 4 rowspan 2 in items index 2
                           {"Department Head (Specialty)","Licensed physician\n" +
                                   "Fellow/diplomate in a specialty/Sub specialty society of the department he/she heads","DOCUMENT REVIEW\n" +
                                   "Diploma/ Certificate from Specialty society\n" +
                                   "Updated Physician PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/Appointment (notarized)","1 per department","",""},
                           {"Training Officer for Physicians","Licensed physician\n" +
                                   "Fellow / diplomate in a specialty/ Subspecialty society of the department he/ she heads","1","",""},
                           {"Chief Nurse/Director of Nursing","Licensed nurse\n" +
                                   "Master’s Degree in Nursing AND at least five (5) years of clinical experience in a Supervisory or managerial position in nursing (R.A. No. 9173)","DOCUMENT REVIEW\n" +
                                   "Diploma for Master’s Degree\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/ Appointment (notarized)\n" +
                                   "Service Record/ Certificate of Employment (proof of hospital supervisory/ managerial experience)","1","",""},
                           {"Chief Administrative Officer / Hospital Administrator","Government Hospital: \n" +
                                   "(a) Master’s Degree in Hospital administration or related course (MPH, MBA, MPA, MHSA, etc.) AND at least five (5) years hospital experience in a supervisory or managerial position (b) Grandfather’s Clause may be accepted in place of the Master’s Degree in Hospital Administration (or related course), provided that the individual is fifty- five (55) and above , AND currently holding the position for at least five years. \n" +
                                   "Private Hospital:\n" +
                                   "Master’s Degree in hospital administration or related course (MPH, MBA, MPA, MHSA, etc.) AND/OR at least five (5) years hospital experience in a supervisory or managerial position","Diploma for Master’s Degree\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/ Appointment (notarized)\n" +
                                   "Service Record/ Certificate of Employment (proof of hospital supervisory/ managerial experience)","1","",""},
                           //index 8 colspan 6
                           {"ADMINISTRATIVE SERVICES"},
                           //index 9 rowspan 7 in item index 3
                           {"Accountant","Certified Public Accountant (may be outsourced)","DOCUMENT REVIEW\n" +
                                   "Diploma/Certificate of units earned\n" +
                                   "Updated PRC license (if applicable)\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           //index 10 rowspan 6 in item index 1
                           {"Billing Officer","With Bachelor’s Degree relevant to the job","1","",""},
                           {"Book keeper","1","",""},
                           {"Budget/Finance Officer","1","",""},
                           {"Cashier","1","",""},
                           {"Human Resources Management Officer / Personnel Officer","1","",""},
                           {"Clerk, pool","1:50 beds","",""},
                           //index 16
                           {"Engineer (full time)","Licensed Engineer","DOCUMENT REVIEW\n" +
                                   "Diploma\n" +
                                   "Updated PRC license\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           //index 17 rowspan 2 in item index 1
                           {"Supply Officer/-Storekeeper","With appropriate training and experience","DOCUMENT REVIEW\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           {"Laundry Worker","1","","",""},
                           {"Medical Records officer","Bachelor’s Degree\n" +
                                   "Training in ICD 10\n" +
                                   "Training in Medical Records Management","DOCUMENT REVIEW\n" +
                                   "Diploma\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           //index 20 rowspan 2 in item index 2
                           {"Medical Social worker (Full Time)","Licensed social worker","DOCUMENT REVIEW\n" +
                                   "Diploma/Certificate of units earned\n" +
                                   "Updated PRC license Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           {"Nutritionist –Dietician (Full Time)","Licensed Nutritionist-Dietician","1","",""},
                           //index 22 rowspan 2 in item index 2
                           {"Driver","Licensed driver","DOCUMENT REVIEW\n" +
                                   "Proof of Employment / Appointment (notarized)","1","",""},
                           {"Cook","","1","",""},
                           //index 24 rowspan 2 in item index 2 and item index 3
                           {"Building Maintenance Man/Utility Worker","May be outsourced\n" +
                                   "Security guard must be licensed.","DOCUMENT REVIEW\n" +
                                   "Relevant Training\n" +
                                   "Licensed if applicable\n" +
                                   "Proof of Employment / Appointment (notarized) if employed by hospital\n" +
                                   "Notarized MOA if outsourced","1 per shift","",""},
                           {"Security Guard (licensed)","1 per shift\t","",""},
                           //index 26 colspan 6
                           {"CLINICAL SERVICES"},
                           //index 27 rowspan 4 in item index 2
                           {"Consultant Staff in Ob-Gyn. Pediatrics, Medicine. Surgery, and Anesthesia \n" +
                                   "\n" +
                                   "*Hospital may have additional consultants from other specialties.","Licensed physician\n" +
                                   "Fellow/Diplomate\n" +
                                   "ACLS certified (for Surgeons and Anesthesiologists)","DOCUMENT REVIEW\n" +
                                   "Certificate from Specialty society, if applicable (for Board Certified)\n" +
                                   "Residency Training Certificate (for Board Eligible)\n" +
                                   "Certificate of Residency Training/ Medical Specialists (*DOH Medical specialist, last exam was in 1989)\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)","All consultants must be board certified","",""},
                           {"Intensive Care Unit: Multidisciplinary Team composed of, but not limited to, board certified Cardiologist, Pulmonologist, Neurologist, Pulmonologist Surgeon Anaesthesiologist OR an Intensivist","Licensed physician\n" +
                                   "Fellow/Diplomate","A team composed of at least 1 per specialty (May be part time or visiting consultant) OR a intensivist","",""},
                           {"Neonatal Intensive Care Unit: A multidisciplinary team compose of, but not limited to, pediatric, cardiologist, pediatric nephrologist, pediatric Pulmonologist OR a neonatologist","Licensed physician\n" +
                                   "Fellow/Diplomate","A team composed of at least 1 per specialty (May be part time or visiting consultant) OR a neonatologist","",""},
                           {"High Risk Pregnancy Unit: General Obstetricians, preferably with a Perinatologist, and a referral team of IM specialists","Licensed physician\n" +
                                   "Fellow/Diplomate","General Obstetricians, preferably with a Perinatologist, and a referral team of IM specialists (May be part time or visiting consultant)\t","",""},
                           {"Resident Physician on duty (Shall not go on duty for more than 48 hours straight).","Licensed physician","DOCUMENT REVIEW\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)\n" +
                                   "Schedule of duty approved by Medical Director/Chief of Hospital","Wards- 1:20 beds at any given time PLUS ER – at least 1 at any given time\n" +
                                   "\n" +
                                   "*This ratio does not include Resident Physicians on Duty that shall be required for add-on services such as dialysis facility. It shall be counted separately.","",""},
                           {"Rehabilitation Medicine Specialist","Licensed physician\n" +
                                   "Fellow/Diplomate","DOCUMENT REVIEW\n" +
                                   "Certificate from Specialty society, if applicable (for Board Certified)\n" +
                                   "Residency Training Certificate (for Board Eligible)\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment/ Appointment (notarized)","1","",""},
                           //index 33 colspan 6
                           {"NURSING SERVICES"},
                           {"Assistant Chief Nurse","Licensed nurse\n" +
                                   "At least twenty (20) units towards Master’s Degree in\\ Nursing\n" +
                                   "At least three (3) years-experience in supervisory/ managerial position in nursing","DOCUMENT REVIEW\n" +
                                   "Diploma/Certificate of Units Earned\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)\n" +
                                   "Service Record/Certificate of Employment (Proof of supervisory/managerial experience in nursing)","1:100 Beds","",""},
                           {"Supervising Nurse/Nurse Managers","Licensed nurse\n" +
                                   "With at least nine (9) units of Master’s Degree in Nursing\n" +
                                   "At least two (2) years experience in general nursing service administration","DOCUMENT REVIEW\n" +
                                   "Diploma/Certificate of Units Earned\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)\n" +
                                   "Service Record/Certificate of Employment (Proof of General nursing service Administration experience)","1 per Department-Office hours only (8am-5pm)","",""},
                           //index 36 rowspan 3 in item index 2
                           {"Head Nurse/Senior Nurse","Licensed nurse\n" +
                                   "With at least two (2) years-hospital experience\n" +
                                   "BLS certified","DOCUMENT REVIEW\n" +
                                   "Diploma\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of trainings attended\n" +
                                   "Proof of employment (notarized)\n" +
                                   "If nursing staffing is outsourced: Validity of the contract of of employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                                   "Schedule of duty approved by Chief Nurse","1 per shift per clinical department","",""},
                           {"Staff Nurse","Licensed nurse\n" +
                                   "with at least (2) years- hospital experience\n" +
                                   "BLS certified","Wards – 1:12 beds at any time (1 reliever for every 3 RNs)","",""},
                           {"Staff Nurse in every Critical Unit (CCU, ICU, NICU, PICU, SICU. HRPU, etc.)","Licensed nurse\n" +
                                   "Certificate of Training in Critical Care Nursing, ACLS","1:3 beds at any time per shift (plus 1 reliever per 3 CCU RNs)","",""},
                           //index 39 rowspan 2 in item index 2 and index 1
                           {"Nursing Attendants","highschool graduate\n" +
                                   "With relevant health-related training","DOCUMENT REVIEW\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment (notarized)","1:24 beds at any time (1 reliever for every 3 NAs)","",""},
                           {"Nursing Attendant in CCUs","1:12 beds at any time (plus 1 reliever for every 3 NAs)","",""},
                            //index 41 rowspan 4 in item index 2
                           {"Operating Room Nurses: \n" +
                                   "-scrub Nurse (SN) \n" +
                                   "-Circulating Nurse (CN)","Training OR Nursing","DOCUMENT REVIEW\n" +
                                   "Diploma\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized\n" +
                                   "If outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                                   "schedule of duty approved by Chief Nurse","1 SN and 1 CN per functioning OR per shift (plus 1 reliever for every 3 nurses)","",""},
                           {"Delivery room Nurse\t\n","Training in Maternal and Child Nursing (may be in house training or training in Essential Integrated Newborn Care [EINC])\n" +
                                   "Training in BLS and ACLS","1 per 3 delivery table per shift (plus 1 reliever for every 3 nurses)","",""},
                           {"Emergency Room Nurse","Licensed nurse\n" +
                                   "Training in Trauma Nursing, ACLS and other relevant training","1:3 beds per shift (plus 1 reliever for every 3 nurses)","",""},
                           {"Outpatient Department Nurse","Licensed nurse\n" +
                                   "Training in BLS","1 Office hours only (8am-5pm)","",""},
                           //index 45 rowspan 3 in item index 2
                           {"Dentist – MOA if outsourced but the dental service should be within the vicinity of hospital","Licensed dentist","DOCUMENT REVIEW\n" +
                                   "Diploma\n" +
                                   "Updated PRC license\n" +
                                   "Certificates of Trainings attended\n" +
                                   "Proof of Employment / Appointment (notarized)\n" +
                                   "If outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of thehospital’s LTO.","1 Office hours only (8am-5pm)","",""},
                           {"Physical Therapist","licensed physical therapist","1","",""},
                           {"Respiratory Therapist","Licensed respiratory therapist or licensed nurse with respiratory therapy training","1 per shift","",""},
                           //index 48 three column
                           {"DOCUMENTS","COMPLIED","REMARKS"},
                           {"1.DOH – Approved PTC","",""},
                           {"2.DOH Approved Floor Plan","",""},
                           {"3.Checklist for Review of Floor Plans (accomplished)","",""},
                           //index 52 one column
                           {" "},
                           //index 53 5 column
                           {"EQUIPMENT/INSTRUMENT\n" +
                                   "(Functional)","QUANTITY","AREA","COMPLIED","REMARKS"},
                           //index 54 colspan 5
                           {"ADMINISTRATIVE SERVICE"},
                           {"Ambulance\n" +
                                   "Available 24/7\n" +
                                   "Physically present if not being used during time of inspection/monitoring","1","Parking","",""},
                           {"Computer with Internet Access","1","Administrative Office","",""},
                           {"Emergency Light","1 per unit or area","lobby, hallway, nurses’ station, office/unit and stairways","",""},
                           {"Fire Extinguishers","1 per unit or area","Lobby, hallway, nurses’ station, office/unit and stairways","",""},
                           {"LCD Projector","1","Conference Room","",""},
                           {"Generator set with automatic Transfer Switch (ATS)","1","Genset house","",""},
                           //index 61 colspan 5
                           {"KITCHEN DIETARY"},
                           //index 62 rowspan 9 in item index 2
                           {"Exhaust fan","1","Kitchen","",""},
                           {"Food Conveyor or equivalent (closed-type)","1","",""},
                           {"Blender/Osteorizer","1","",""},
                           {"Oven","1","",""},
                           {"Stove","1","",""},
                           {"Refrigerator/Freezer","1","",""},
                           {"Utility cart","1","",""},
                           {"Garbage Receptacle with Cover (color-coded)","1 for each color","",""},
                            //index 70 colspan 5
                           {"EMERGENCY ROOM"},
                           //index 71 rowspan 28 in item index 2
                           {"Bag-valve-mask Unit \n" +
                                   "- Adult\n" +
                                   "- Pediatric","1\n" +
                                   "1","ER","",""},
                           {"Calculator for dose computation","1","",""},
                           {"Clinical Weighing scale","1","",""},
                           {"Defibrillator with paddles","1","",""},
                           {"Delivery set, primigravid","2 sets","",""},
                           {"Delivery set, multigravid","2 sets","",""},
                           {"ECG Machine with leads","1","",""},
                           {"EENT Diagnostic set with Ophthalmoscope and Otoscope","1 set","",""},
                           {"Emergency Cart (for contents, refer to separate list).","1","",""},
                           {"Examining table","1","",""},
                            {"Examining table (with Stirrups for OB-Gyne)","1","",""},
                            {"Gooseneck lamp/Examining Light","1","",""},
                            {"Instrument/Mayo Table","1","",""},
                            {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thoracostomy, Cutdown, etc.)","2 sets","",""},
                            {"Nebulizer","1","",""},
                            {"Negatoscope","1","",""},
                            {"Neurologic Hammer","1","",""},
                            {"OR Light (portable or equivalent)","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or With tank holder if not pipeline","2","",""},
                            {"Pulse Oximeter","1","",""},
                            {"Sphygmomanometer, Non-mercurial \n" +
                                    "- Adult Cuff \n" +
                                    "- Pediatric Cuff","1","",""},
                            {"OR Light (portable or equivalent)","1\n" +
                                    "1\n" +
                                    "1","",""},
                            {"Stethoscope","1","",""},
                            {"Suturing Set","2 sets","",""},
                            {"Thermometer, non –mercurial \n" +
                                    "- Oral \n" +
                                    "- Rectal","1\n" +
                                    "1","",""},
                            {"Vaginal Speculum, Different Sizes","1 for each different size","",""},
                            {"Wheelchair","1","",""},
                            {"Wheeled Stretcher with guard/side rails And wheel lock or anchor.","1","",""},
                            //index 99
                            {"OUT-PATIENT DEPARTMENT"},
                            //index 100 rowsspan 14 in item index 2
                            {"Clinical Height and Weight Scale","1","OPD","",""},
                            {"EENT Diagnostic set with ophthalmoscope and otoscope\t","1","",""},
                            {"Gooseneck lamp/Examining Light","1","",""},
                            {"Examining table with wheel lock or anchor","1","",""},
                            {"Instrument/Mayo Table","1","",""},
                            {"Minor Instrument Set 1 set","1","",""},
                            {"Neurologic Hammer","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Peak flow meter - Adult - Pediatric","1\n" +
                                    "1","",""},
                            {"Sphygmomanometer, N0n-mercurial - Adult - Pediatric","1\n" +
                                    "1","",""},
                            {"Stethoscope","1","",""},
                            {"Thermometer, non-mercurial\n" +
                                    "- Oral\n" +
                                    "- Rectal","1","",""},
                            {"Suture Removal Set","1","",""},
                            {"Wheelchair / Wheeled Stretcher","1","",""},
                            //index 114
                            {"OPERATING ROOM"},
                            //index 115 rowspan 22 in item index 2
                            {"Air conditioning Unit","1","","",""},
                            {"Anesthesia Machine","1","",""},
                            {"Cardiac Monitor with Pulse Oximeter","1","",""},
                            {"Caesarean Section Instrument","1","",""},
                            {"Defibrillator with paddles","1","",""},
                            {"Electrocautery machine","1","",""},
                            {"Emergency Cart (for contents, refer to separate list)","1","",""},
                            {"Instrument / Mayo table","1","",""},
                            {"Laparotomy pack (Linen pack)","1 per OR","",""},
                            {"Laparatomy / Major Instrument Set","1 per OR","",""},
                            {"Laryngoscopes with different sizes of blades","1","",""},
                            {"Operating room light","1 per OR","",""},
                            {"Operating room table","1 per OR","",""},
                            {"Orthopedic Instrument Set","1 set","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1 per OR","",""},
                            {"Rechargeable Emergency Light (in case generator malfunction)","1 per OR","",""},
                            {"Sphygmomanometer, N0n-mercurial \n" +
                                    "- Adult cuff \n" +
                                    "- Pediatric cuff","1 per OR\n" +
                                    "1 per OR","",""},
                            {"Spinal Set","1","",""},
                            {"Stethoscope","1","",""},
                            {"Suction Apparatus","1","",""},
                            {"Thermometer, non-mercurial \n" +
                                    "- Oral \n" +
                                    "- Rectal","1\n" +
                                    "1","",""},
                            {"Wheeled Stretcher with guard/side rails and wheel lock or anchor.","1","",""},
                            //index 137
                            {"POST ANESTHESIA CARE UNIT RECOVERY ROOM"},
                            //index 138 rowspan 9 in item index 2
                            {"Air conditioning Unit","1","PACU/RR","",""},
                            {"Cardiac Monitor","1","",""},
                            {"Defibrillator with paddles","1 (if separate from the OR Complex)","",""},
                            {"Emergency Cart (for contents, refer to separate list)","1 (if separate from the OR Complex)","",""},
                            {"Mechanical / patient bed, with guard side rails and wheel lock or anchored","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Sphygmomanometer, N0n-mercurial \n" +
                                    "- Adult cuff \n" +
                                    "- Pediatric cuff","1\n" +
                                    "1","",""},
                            {"Stethoscope","1","",""},
                            {"Thermometer, non- mercurial","1","",""},
                            //index 147 colspan 5
                            {"LABOR ROOM"},
                            //index 148 rowspan 7 in item index 2
                            {"Fetal Doppler","1","Labor Room","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Patient Bed","1","",""},
                            {"Pulse Oximeter","1","",""},
                            {"Sphygmomanometer, N0n-mercurial","1","",""},
                            {"Stethoscope","1","",""},
                            {"Thermometer, Non- mercurial","1","",""},
                            //index 155 colspan 5
                            {"DELIVERY ROOM"},
                            //index 156 rowspan
                            {"Air conditioning Unit","1","DR","",""},
                            {"Bag valve mask unit (Adult and pediatric)","1","",""},
                            {"Bassinet","1","",""},
                            {"Clinical Infant Weighing scale","1","",""},
                            {"Defibrillator with paddles","1 (if DR is separate from the OR Complex)","",""},
                            {"Delivery set, primigravid","1 set","",""},
                            {"Delivery room light","1","",""},
                            {"Delivery room table","1","",""},
                            {"Dilatation and Curettage set","1 set","",""},
                            {"Emergency Cart (for contents, refer to separate list)","1 (if DR is separate from OR Complex)","",""},
                            {"Instrument/Mayo Table","1","",""},
                            {"Kelly Pad or equivalent","1","",""},
                            {"Laryngoscope with different sizes of blades","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Rechargeable Emergency Light (in case generator malfunction)","1","",""},
                            {"Sphygmomanometer, N0n-mercurial","1","",""},
                            {"Stethoscope","1","",""},
                            {"Suction Apparatus","1","",""},
                            {"Wheeled Stretcher","1","",""},
                            //index 175 colspan 5
                            {"HIGH RISK PREGNANCY UNIT"},
                            //index 176 rowspan 6 in item index 2
                            {"Cardiac Monitor with Pulse Oximeter","1","HRPU","",""},
                            {"Cardiotocography (CTG) Machine","1","",""},
                            {"Fetal Doppler","1","",""},
                            {"Oxygen Unit \n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Patient bed with side rails","1","",""},
                            {"Suction apparatus","1","",""},
                            //index 182
                            {"NEONATAL INTENSIVE CARE UNIT (NICU)"},
                             //index 183 rowspan 21 in item index 2
                            {"Air conditioning Unit","1","NICU","",""},
                            {"Bassinet","1","",""},
                            {"Bilirubin Light/ Phototherapy machine or equivalent","1","",""},
                            {"Cardiac Monitor with Pulse Oximeter","1","",""},
                            {"Clinical Infant Bag-valve mask unit","1","",""},
                            {"Clinical Infant weighing scale","1","",""},
                            {"Defibrillator with paddles","1","",""},
                            {"EENT Diagnostic Set with ophthalmoscope and otoscope","1","",""},
                            {"Emergency Cart (for contents, refer to separate list)","1","",""},
                            {"Glucometer","1","",""},
                            {"Incubator","1","",""},
                            {"Infusion pump/ Syringe pump","1","",""},
                            {"Laryngoscope with neonatal blades of different sizes","1","",""},
                            {"Mechanical Ventilator (May be outsourced)","","",""},
                            {"Neonatal Stethoscope","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Refrigerator for Breast milk storage","1","",""},
                            {"Sphygmomanometer, N0n-mercurial\n" +
                                    "- for neonate","1","",""},
                            {"Suction apparatus","1","",""},
                            {"Thermometer, non-mercurial","1","",""},
                            {"Umbilical Cannulation set","1 set","",""},
                            //index 204 colspan 5
                            {"INTENSIVE CARE UNIT (ICU) – For all types of ICU (PICU, SICU, Medical ICU, etc.)"},
                            //index 205 rowspan 16 in item index 2
                            {"Air conditioning Unit","1","ICU","",""},
                            {"Bag-valve-mask Unit\n" +
                                    "- Adult\n" +
                                    "- Pediatric","1\n" +
                                    "1","",""},
                            {"Cardiac Monitor with Pulse Oximeter","1","",""},
                            {"Defibrillator with paddles","1","",""},
                            {"Emergency Cart (for contents, refer to separate list)","1","",""},
                            {"EENT Diagnostic Set with ophthalmoscope and otoscope","1","",""},
                            {"Infusion pump","1","",""},
                            {"Laryngoscope with different sizes of blades","1","",""},
                            {"Mechanical Bed","Depending on the number of beds declared","",""},
                            {"Mechanical Ventilator/ Respirator (May be outsourced)","1","",""},
                            {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thoracostomy, Cutdown, etc.)","1","",""},
                            {"Oxygen Unit \n" +
                                    "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                            {"Sphygmomanometer, non-mercurial (reserved for sudden breakdown of cardiac monitor) \n" +
                                    "- Adult cuff for adult unit - Pediatric cuff for pediatric unit","1\n" +
                                    "1","",""},
                            {"Stethoscope","1","",""},
                            {"Suction apparatus","1","",""},
                            //index 220 colspan 5
                            {"NURSING UNIT/WARD"},
                             //index 221 rowspan 15 in item index 2
                            {"Bag-Valve-mask Unit \n" +
                                    "- Adult\n" +
                                    "- Pediatric","1\n" +
                                    "1","NURSING UNIT/WARD","",""},
                            {"Clinical Height and Weight Scale","1","",""},
                            {"Defibrillator with paddles","1","",""},
                            {"Emergency cart or equivalent (refer to separate list for the contents)","1","",""},
                            {"EENT Diagnostic Set with ophthalmoscope and otoscope","1","",""},
                            {"Laryngoscope with different sizes of blades","1","",""},
                            {"Mechanical/Patient bed With locked, if wheeled; with guard or side rails","ABC","",""},
                            {"Bedside Table","ABC","",""},
                            {"Nebulizer","1","",""},
                            {"Neurologic Hammer","1","",""},
                            {"Oxygen Unit\n" +
                                    "Tank is anchored/chained if not pipeline","1","",""},
                            {"Sphygmomanometer, Non-Mercurial \n" +
                                    "- Adult cuff\n" +
                                    "- Pediatric cuff","1\n" +
                                    "1","",""},
                            {"Stethoscope","1","",""},
                            {"Suction Apparatus","1","",""},
                            {"Thermometer, non-mercurial \n" +
                                    "- Oral \n" +
                                    "- Rectal","1\n" +
                                    "1","",""},
                            //index 236 to 238 colspan 5
                            {"DIALYSIS CLINIC\n" +
                                    "(Specify if Hemodialysis or Peritoneal dialysis or both) \n" +
                                    "Refer to Assessment Tool for Dialysis Clinics"},
                            {"AMBULATORY SURGICAL CLINIC\n" +
                                    "Refer to Assessment Tool for ASC"},
                            {"RESPIRATORY / PULMONARY UNIT"},
                            //index 239 rowspan 4
                            {"ABF Machine","1","Respiratory/ Pulmonary Unit","","",""},
                            {"Pulmonary Function Test (PFT) or Peak Expiratory Flow Rate (PEFR) Tube","","",""},
                            {"Spirometer","1","",""},
                            {"Nebulizer","1","",""},
                            //index 243 colspan 5
                            {"PHYSICAL MEDICINE AND REHABILITATION UNIT"},
                            //index 244 rowspan 21 in item index 2
                            {"Bicycle ergonometer","1","PMRU","",""},
                            {"Cervical Traction","1","",""},
                            {"Cold Therapy Products","1","",""},
                            {"Diagonal mirrors","1","",""},
                            {"Dynamometer","1","",""},
                            {"Exercise Plight/ Bed","1","",""},
                            {"Exercise Stairs with Rails","1","",""},
                            {"Goniometer","1","",""},
                            {"Hot Therapy Products","1","",""},
                            {"Light Therapy","1","",""},
                            {"Lumbar Traction","1","",""},
                            {"Overhead pulley","1","",""},
                            {"Paraffin Wax","1","",""},
                            {"Parallel Bars","1","",""},
                            {"Pedometer","1","",""},
                            {"Pulley System","1","",""},
                            {"Therapy machine","1","",""},
                            {"Therapy mats","1","",""},
                            {"Therapy rolls","1","",""},
                            {"Therapy wedges","1","",""},
                            {"Transcutaneous Electric Nerve Stimulator (TENS)","1","",""},
                            //index 265 colspan 5
                            {"DENTAL CLINIC - not required if services is by Referral"},
                            //index 266 rowspan 25 in item index 2
                            {"Air compressor","1","DENTAL CLINIC","",""},
                            {"Autoclave","1","",""},
                            {"Bone file, stainless","1","",""},
                            {"Cotton pliers","1","",""},
                            {"Cowhorn Forceps","1","",""},
                            {"Dental Chair Unit","1","",""},
                            {"Explorer, double-end","1","",""},
                            {"Forceps, No. 8","","",""},
                            {"Forceps, No.17 Upper molar","1","",""},
                            {"Forceps, No. 18 Upper molar","1","",""},
                            {"Forceps, No. 150 Maxillary Universal","1","",""},
                            {"Forceps, No. 150 S Primary Teeth","1","",""},
                            {"Forceps, No. 151 Lower Universal","1","",""},
                            {"Forceps, No. 151 Mandibular Pre-molar","1","",""},
                            {"Forceps, No. 151 S Lower Primary Teeth","1","",""},
                            {"Gum separator","1","",""},
                            {"High speed handpiece with Burr remover","1","",""},
                            {"Low speed handpiece, Angeled head","1","",""},
                            {"Mouth mirror explorer","1","",""},
                            {"Periosteal elevator No. 9, double-end","1","",""},
                            {"Rongeur","1","",""},
                            {"Root elevator","1","",""},
                            {"Scaler Jacquettes Set No. 1,2, and 3","1","",""},
                            {"Surgical Chisel","1","",""},
                            {"Surgical Malette","1","",""},
                            //index 291 colspan 5
                            {"CENTRAL STERILIZING & SUPPLY ROOM"},
                            {"Autoclave/Steam Sterilizer","1","CSSR","",""},
                             //index 293 colspan 5
                            {"MORGUE"},
                            //index 294 rowspan 4 in item index 2
                            {"Autopsy","1","MORGUE","",""},
                            {"Autopsy instrument set","1","",""},
                            {"Cadaver freezer","1","",""},
                            {"Cadaver Shower","1","",""},
                            //index 298 colspan
                            {"EMERGENCY CART CONTENTS","ER","OR","DR","ICU","NICU","HRPU","NS 1","NS 2","NS 3","NS 4","NS 5","NS 6","NS 7","OTHERS","OTHERS","REMARKS"},
                            {"Adenosine 6 mg/2mL vial","","","","","","","","","","","","","","","",""},
                            {"Amiodarone 150mg/3mL ampule","","","","","","","","","","","","","","","",""},
                            {"Anti-tetanus serum (either equine-based antiserum or human antiserum)","","","","","","","","","","","","","","","",""},
                            {"Aspirin USP grade (325 mg/tablet)","","","","","","","","","","","","","","","",""},
                            {"Atropine 1 mg/ml ampule","","","","","","","","","","","","","","","",""},
                            {"B-adrenergic agonists (i.e. Salbutamol 2mg/ml)","","","","","","","","","","","","","","","",""},
                            {"Benzodiazepine (Diazepam 10mg/2ml ampule and/or Midazolam) (in high alert box)","","","","","","","","","","","","","","","",""},
                            {"Calcium (usually calcium gluconate 10% solution in 10 mL ampule)","","","","","","","","","","","","","","","",""},
                            {"Clopidogrel 75 mg tablet","","","","","","","","","","","","","","","",""},
                            {"D5W 250 mL","","","","","","","","","","","","","","","",""},
                            {"D50W 50mg/vial","","","","","","","","","","","","","","","",""},
                            {"Digoxin 0.5mg/2mL ampule","","","","","","","","","","","","","","","",""},
                            {"Diphenhydramine 50mg/mL ampule\t","","","","","","","","","","","","","","","",""},
                            {"Dobutamine 250mg/5mL ampule","","","","","","","","","","","","","","","",""},
                            {"Dopamine 200mg/5mL ampule/vial\t","","","","","","","","","","","","","","","",""},
                            {"Epinephrine 1mg/ml ampule","","","","","","","","","","","","","","","",""},
                            {"Furosemide 20mg/2ml ampule","","","","","","","","","","","","","","","",""},
                            {"Haloperidol 50mg/mL ampule","","","","","","","","","","","","","","","",""},
                            {"Hydrocortisone 250mg/2mL vial","","","","","","","","","","","","","","","",""},
                            {"Lidocaine 10% in 50mL spray","","","","","","","","","","","","","","","",""},
                            {"Lidocaine 2% solution vial 1g/50ml","","","","","","","","","","","","","","","",""},
                            {"Magnesium sulphate 1g/2mL ampule","","","","","","","","","","","","","","","",""},
                            {"Mannitol 20% solution in 500ml/bottle\t","","","","","","","","","","","","","","","",""},
                            {"Methylprednisolone 4mg/tablet","","","","","","","","","","","","","","","",""},
                            {"Metoclopramide 10mg/2mL ampule","","","","","","","","","","","","","","","",""},
                            {"Morphine sulphate 10mg/mL ampule (in high alert box)","","","","","","","","","","","","","","","",""},
                            {"Nitroglycerin inj. 10mg/10mL ampule or Isosorbide dinitrate 5mg SL tablet or 10 mg/10mL ampule","","","","","","","","","","","","","","","",""},
                            {"Noradrenaline 2mg/2mL ampule","","","","","","","","","","","","","","","",""},
                            {"Paracetamol 300mg/ampule (IV preparation)","","","","","","","","","","","","","","","",""},
                            {"Phenobarbital 120mg/ml ampule IV or 30mg tablet (in high alert box)","","","","","","","","","","","","","","","",""},
                            {"Phenytoin 100mg/capsule or 100 mg.2mL ampule","","","","","","","","","","","","","","","",""},
                            {"Plain LRS 1L/bottle","","","","","","","","","","","","","","","",""},
                            {"Plain NSS 1L/bottle-0.9% Sodium Chloride","","","","","","","","","","","","","","","",""},
                            {"Potassium Chloride 40mEq/20mL vial (in high alert box)","","","","","","","","","","","","","","","",""},
                            {"Vitamin B1/6/12 vial (1g B1, 1g B6, 0.01gB12 in 10 mL vial)","","","","","","","","","","","","","","","",""},
                            {"Sodium bicarbonate 50mEq/50mL ampule","","","","","","","","","","","","","","","",""},
                            {"Verapamil 5 mg/2ml ampule","","","","","","","","","","","","","","","",""},
                            //index 335 colspan 17
                            {"EQUIPMENT/SUPPLIES"},
                            {"Airway adjuncts","","","","","","","","","","","","","","","",""},
                            {"Airway / Intubation Kit (with stylet and bag valve masks)","","","","","","","","","","","","","","","",""},
                            {"Alcohol disinfectant","","","","","","","","","","","","","","","",""},
                            {"Aseptic bulb syringe","","","","","","","","","","","","","","","",""},
                            {"Calculator","","","","","","","","","","","","","","","",""},
                            {"Capillary Blood Glucose (CBG) Kit","","","","","","","","","","","","","","","",""},
                            {"Cardiac Board","","","","","","","","","","","","","","","",""},
                            {"Cardiac / EKG Leads","","","","","","","","","","","","","","","",""},
                            {"Endotracheal Tubes, all sizes","","","","","","","","","","","","","","","",""},
                            {"Flashlights or Pen lights","","","","","","","","","","","","","","","",""},
                            {"Gloves, sterile","","","","","","","","","","","","","","","",""},
                            {"Gloves,non-sterile","","","","","","","","","","","","","","","",""},
                            {"Laryngoscope with different sizes of blades","","","","","","","","","","","","","","","",""},
                            {"Nasal cannula","","","","","","","","","","","","","","","",""},
                            {"Protective face shield or mask or goggles","","","","","","","","","","","","","","","",""},
                            {"Standard face mask","","","","","","","","","","","","","","","",""},
                            {"Sterile gauze (pre-folded and individually packed)","","","","","","","","","","","","","","","",""},
                            {"Urethral catheter","","","","","","","","","","","","","","","",""},
                            {"Urine collection bag","","","","","","","","","","","","","","","",""},
                            {"Waterproof aprons","","","","","","","","","","","","","","","",""}

                           };




        for(int i = 0; i < desc.length; i++) {
            for(int j = 0; j< desc[i].length; j++) {
                String d = desc[i][j];
                if(i == 1 || i == 8 || i == 26 || i == 33){
                    tablelevel3.addCell(createCell(d,10,0,6,true));
                }else if((i == 4 && j == 2) || (i == 17 && j == 1) || (i == 20 && j == 2) || (i == 22 && j == 2) || (i == 24 && (j == 2 || j == 3))
                          || (i == 39 && (j == 1 || j == 2))){
                    tablelevel3.addCell(createCell(d,10,2,0,false));
                }else if(i == 9 && j == 2){
                    tablelevel3.addCell(createCell(d,10,7,0,false));
                }else if(i == 10 && j == 1){
                    tablelevel3.addCell(createCell(d,10,6,0,false));
                }else if( (i == 27 && j == 2) || (i == 41 && j == 2) ){
                    tablelevel3.addCell(createCell(d,10,4,0,false));
                }else if((i == 36 && j == 2) || (i == 45 && j == 2)){
                    tablelevel3.addCell(createCell(d,10,3,0,false));
                }else if(i > 47 && i < 52){
                    table2level3.addCell(createCell(d,10,0,0,false));
                }else if(i == 52){
                    table3level3.addCell(createCell(d,100,0,0,false));
                }else if(i>52 && i<298){
                    if(i == 54 || i == 70|| i == 61 || i == 99 || i==114 || i==137 || i==147 ||
                       i== 175 || i == 155 || i == 182 || i==204 || i== 220 || i==243 || (i>235 && i<239) ||
                       i== 265 || i == 291 || i== 293){
                        table4level3.addCell(createCell(d,10,0,5,false));
                    }else if((i == 62 || i==138) && j == 2){
                        table4level3.addCell(createCell(d,10,9,0,false));
                    }else if(i == 71 && j == 2){
                        table4level3.addCell(createCell(d,10,28,0,false));
                    }else if(i == 100 && j == 2){
                        table4level3.addCell(createCell(d,10,14,0,false));
                    }else if(i == 115 && j == 2){
                        table4level3.addCell(createCell(d,10,22,0,false));
                    }else if(i == 148 && j == 2){
                        table4level3.addCell(createCell(d,10,7,0,false));
                    }else if(i == 156 && j == 2){
                        table4level3.addCell(createCell(d,10,20,0,false));
                    }else if(i == 176 && j == 2){
                        table4level3.addCell(createCell(d,10,6,0,false));
                    }else if(i == 205 && j == 2){
                        table4level3.addCell(createCell(d,10,16,0,false));
                    }else if(i == 221 && j == 2){
                        table4level3.addCell(createCell(d,10,15,0,false));
                    }else if((i == 239 || i== 294)&& j == 2){
                        table4level3.addCell(createCell(d,10,15,0,false));
                    }else if((i == 183 || i== 244 )&& j == 2){
                        table4level3.addCell(createCell(d,10,21,0,false));
                    }else if(i == 266 && j == 2){
                        table4level3.addCell(createCell(d,10,25,0,false));
                    }else{
                        table4level3.addCell(createCell(d,10,0,0,false));
                    }
                }else if(i>297){
                      if(i==336){
                          table5level3.addCell(createCell(d,10,0,17,false));
                      }else{
                          table5level3.addCell(createCell(d,10,0,0,false));
                      }

                }else{
                    tablelevel3.addCell(createCell(d,10,0,0,false));
                }


            } // end j for loop

        } // end i for loop



        document.add(title);
        document.add(tablelevel3);
        document.add(title2);
        document.add(table2level3);
        document.add(title3);
        document.add(table3level3);
        document.add(title4);
        document.add(table4level3);
        document.add(title5);
        document.add(table5level3);
        document.add(title6);
        document.close();
        previewPdf();
    }




    private void createnursingservices() throws FileNotFoundException, DocumentException{
        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"assessment.pdf");
        //Document document = new Document(pagesize);
        Rectangle pagesize = new Rectangle(612, 861);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(5,5,5,5);
        PdfWriter.getInstance(document, output);
        document.open();
//        float [] pointColumnWidths = {800F, 800F, 800F,800F,800F,800F};


        ///document
        PdfPTable table = new PdfPTable(6);
        table.setTotalWidth(600);
        table.setLockedWidth(true);
        PdfPCell cell;
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLD);

        Paragraph title = createtitle("DOH STANDARDS (Indicators) for HOSPITALS");
        Paragraph title2 = createtitle("Instructions");
        Paragraph par1 = createParagraph("In the appropriate box, place a check mark (✓) if the hospital is compliant or X-mark if not compliant.\n" +
                "Interview at least 10 patients and 10 hospitals staff members.\n" +
                "Conduct docement review of at least 10 sample documents.");




        String[][] desc = {{"CRITERIA","INDICATOR","EVIDENCE","AREAS","COMPLIED","REMARKS"},
                //index 1
                {"I. PATIENT CARE \n" +
                        "      A. ACCESS \n" +
                        "            Standard: Appropriate professionals perform coordinated and sequenced patient assessment to reduce waste and unnecessary repitition."},
                //1st Row index 2
                {"1. NURSING SERVICES \n" +
                        "Moderate Nursing Care and Management","Licensed and appropriately trained nursing personnel assigned in special and critical areas","DOCUMENT REVIEW \n" +
                        "PRC Valid license \n" +
                        "Certificate of relevant training","Wards, ER, OPD","",""},
                //2nd Row index 3
                {"2. Nurses make use of Nursing Process in the care of patient","Charts have nurses’ notes \n" +
                        "\n" +
                        "Presence of Nursing manual and properly utilized Kardex","CHART REVIEW \n" +
                        "Patients’ charts from medical records or wards have nurses’ notes \n" +
                        "\n" +
                        "DOCUMENTS \n" +
                        "Patients’ charts Kardex","Wards \n" +
                        "\n" +
                        "Medical Records Office","",""},
                //2nd table
                //index 4
                {"B. IMPLEMENTATION OF CARE \n" +
                        "       Standard: Medicines are administered in a standardized and systematic manner. Diagnostic examinations appropriate to the provider or organization’s service capability and usual case mix are available and are performed by qualified personnel"},
                //index 5
                {"3. Medicines are administered in a timely, safe, appropriate and controlled manner","All medicines are administered observing the five (5) R’s of medication which are: \n" +
                        "Right patient\n" +
                        "Right medication\n" +
                        "Right dose\n" +
                        "Right route\n" +
                        "Right time","CHART REVIEW \n" +
                        "Check patients charts for the accuracy of medicine administration.","ER \n" +
                        "Wards","",""},
                {"4. Only qualified personnel order, prescribe, dispense prepare, and administer drugs.","All doctors, pharmacists and nurses have updated licenses","INTERVIEW \n" +
                        "Randomly check the licenses of some doctors, nurses and pharmacists if they are updated.","Wards \n" +
                        "Pharmacy \n" +
                        "ER \n" +
                        "OPD","",""},
                {"5. Prescriptions or orders are verified and patients are properly identified before medications are administered","Proof that prescriptions or orders are verified before medications are administered","INTERVIEW \n" +
                        "Ask staff how they verify orders from doctors prior to administration of medicines. \n" +
                        "\n" +
                        "OBSERVE \n" +
                        "How staff verifies the prescriptions or orders for medicines with the doctor’s order.","Wards \n" +
                        "ER\t","",""},
                {"6. patients are properly identified before medicines are administered","Proof that patients are correctly identified prior to administration of medications","INTERVIEW \n" +
                        "Verify from patients if they were correctly identified prior to drug administration. \n" +
                        "\n" +
                        "OBSERVE \n" +
                        "If the staff verifies the identity of patient prior to administration of medications (patient should be the one to state his/her name.)","Wards \n" +
                        "ER","",""},
                {"7. Medicine administration is properly documented in the patient chart","All charts have proper documentation of medicine administration","CHART REVIEW \n" +
                        "Medication sheet in patient chart from medical records or from the wards.","Medical records office wards","",""},
                //index 10
                {"II. SAFE PRACTICE AND ENVIRONMENT \n" +
                        "       A. INFECTION CONTROL \n" +
                        "            Standard: the organization uses a coordinated system- wide approach to reduce the risks of healthcare- associated infections."},
                {"8. There are programs for prevention and treatment of needle stick injuries, and policies and procedures for the safe disposal of used needles are documented and monitored","Presence of policies and procedures on the prevention and treatment of needle stick injuries and safe disposal of needles","INTERVIEW \n" +
                        "Ask staff their policies on needle stick injury \n" +
                        "\n" +
                        "OBSERVE \n" +
                        "Use of PPEs in doing minor surgeries, IV insertions, etc.","ER \n" +
                        "Wards","",""},
                //index 12
                {"   Standard: Cleaning, disinfecting, drying, packaging and sterilizing of equipment, and maintenance of associated environment, conform to relevant statutory requirements and codes of practice."},
                {"9. Policies and procedures on cleaning, disinfecting, drying, packaging and sterilizing of equipment, instruments and supplies.","Presence of policies and procedures on cleaning, disinfecting, drying, packaging and sterilizing of equipment, instruments and supplies","DOCUMENT REVIEW\n" +
                        "Policies and procedures\n" +
                        "Logbooks on packaging and sterilizing of and equipment, instruments supplies\n" +
                        "OBSERVE \n" +
                        "Designated areas for receiving, cleaning, disinfecting, drying packaging, sterilizing and releasing of sterilized equipment, instruments and supplies.","CSSR","",""}

        };

        for(int i = 0; i < desc.length; i++) {
            for(int j = 0; j< desc[i].length; j++) {
                String d = desc[i][j];

                if(i==0){
                    table.addCell(createCell(d,10,0,0,true));
                }else if(i == 1 || i == 4 || i == 10 || i == 12){
                    table.addCell(createCell(d,10,0,6,true));
                }else{
                    table.addCell(createCell(d,10,0,0,false));
                }


            } // end j for loop

        } // end i for loop






        document.add(title);
        document.add(title2);
        document.add(par1);
        document.add(table);



        document.close();
        previewPdf();

    }

    private void createPdf() throws FileNotFoundException, DocumentException {
        int count = 0;
        boolean part2 = checkhospitalpart2();
        boolean part1 = checkhospitalpart1();
        Log.d("part2",part2+"");
        Log.d("part1",part1+"");
        //Log.d("filenames",f);
        //createnursingservices();

        File docsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }



        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"assessment.pdf");

        Rectangle pagesize = new Rectangle(612, 861);


        //Document document = new Document(pagesize);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(5,5,5,5);
        PdfWriter.getInstance(document, output);
        document.open();

        PdfPCell imgcell = new PdfPCell(getCheck());
        imgcell.setPadding(10);
        imgcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgcell.setHorizontalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell imgcell2 = new PdfPCell(getWrong());
        imgcell2.setPadding(10);
        imgcell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgcell2.setHorizontalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell imgcell3 = new PdfPCell(getWrong1());
        imgcell3.setPadding(10);
        imgcell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgcell3.setHorizontalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell imgcell1 = new PdfPCell(getCheck1());
        imgcell1.setPadding(10);
        imgcell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgcell1.setHorizontalAlignment(Element.ALIGN_MIDDLE);
        // Creating a table
        float [] pointColumnWidths = {450F, 700F, 700F,350F,350F,430F};
        PdfPTable table = new PdfPTable(pointColumnWidths);
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLD);
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN,10);
        //first text
        //part 1
        Paragraph title = new Paragraph("PART IV - LEVEL 1 ATTACHMENT 1.A - PERSONAL",fontbold);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        int colindex=0;
        int personnel = 0;
        int personnel2 = 0;
        int equipment = 0;
        int equipment2 = 0;
        int hh04 = 0;
        int hh042 = 0;
        int hh05 = 0;
        int hh052 = 0;
        int hpp02 = 0;
        int hpp022 = 0;
        int others = 0;
        int others1 = 0;

        // Adding cells to the table

        //add cell headers
        String headers[] = {"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"};
        Phrase p;
        PdfPCell cell;
        //add cell headers
        for(int h=0;h<headers.length;h++){
            p = new Phrase(headers[h],fontbold);
            cell = new PdfPCell(p);
            cell.setPaddingBottom(10);
            cell.setPaddingTop(10);
            cell.setBackgroundColor(new BaseColor(148,138,84));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }



        PdfPTable table1 = new PdfPTable(1);
        cell = new PdfPCell(new Paragraph("TOP MANAGEMENT (Should be full-time)",fontbold));
        cell.setPaddingBottom(10);
        cell.setPaddingTop(10);
        cell.setBackgroundColor(new BaseColor(196,188,150));
        table1.addCell(cell);

        PdfPTable table2 = new PdfPTable(pointColumnWidths);

        String[][] desc = {{"Chief of\n" +
                "Hospital/Medical\n" +
                "Director\n",
                "• Licensed physician\n" +
                        "• Havecompleted at least twenty (20) units\n" +
                        "towards a Master's Degreein Hospital\n" +
                        "Administration or related course(MPH,etc)\n" +
                        "OR at least five(5) years hospital experience\n" +
                        "in a supervisory or managerial position",
                "DOCUMENT REVIEW\n" +
                        "Diploma/Certificate of\n" +
                        "units earned\n" +
                        "Updated Physician PRC\n" +
                        "license\n" +
                        "Certificate of Training\n" +
                        "attended\n" +
                        "Proof of\n" +
                        "Employment/Appointment\n" +
                        "(notarized)\n" +
                        "Service Record/Certificate\n" +
                        "of Employment (proof of\n" +
                        "hospital\n" +
                        "supervisory/managerial\n" +
                        "experience)\n",
                "1","",""},
                {"Chief Nurse/Director\n" +
                        "of Nursing","Licensed nurse\n" +
                        "Master's Degreein Nursing AND at least\n" +
                        "five(5) years of experiencein a supervisory\n" +
                        "or managerial position in nursing (R.A. No.\n" +
                        "9173)\n","DOCUMENTARY REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificate of Trainings\n" +
                        "attended\n" +
                        "Proof of\n" +
                        "Employment/Appointment\n" +
                        "(notarized)\n" +
                        "Service Record/Certificate\n" +
                        "of Employment (proof of\n" +
                        "supervisory/managerial\n" +
                        "experiencein nursing)","1","",""},
                {"Chief Administrative\n" +
                        "Officer/Hospital\n" +
                        "Administrator","Havecompleted at least twenty (20) Units towards\n" +
                        "Master's Degreein Hospital Administration or\n" +
                        "related course(MPH, MBA, MPA, MHSA,etc.) OR at\n" +
                        "least five(5) years hospital experiencein a\n" +
                        "supervisory/managerial position.\n","DOCUMENTARY REVIEW\n" +
                        "Diploma/Certificate of units\n" +
                        "earned\n" +
                        "Updated PRC license\n" +
                        "Certificate of Trainings\n" +
                        "attended\n" +
                        "Proof of\n" +
                        "Employment/Appointment\n" +
                        "(notarized)\n" +
                        "Service Record/Certificate\n" +
                        "of Employment (proof of\n" +
                        "hospital\n" +
                        "supervisory/managerial)","1","",""}};

        PdfPCell celldesc = new PdfPCell();
        celldesc.setHorizontalAlignment(Element.ALIGN_LEFT);
        celldesc.setPaddingLeft(5);
        celldesc.setPaddingBottom(10);
        celldesc.setPaddingTop(10);
        for(int i = 0; i < desc.length; i++) {
            Log.d("personnel",personnel+"");
            for(int j = 0; j< desc[0].length; j++) {

                String d = desc[i][j];
                if(j==4){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    Log.d("resres",res);
                    if(res.equals("true")){
                        table2.addCell(imgcell);
                    }else{
                        table2.addCell(imgcell2);
                    }

                }else if(j==5){
                    String res = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    Phrase par = new Phrase(res);
                    celldesc.setPhrase(par);
                    table2.addCell(celldesc);
                }else{
                    Phrase par = new Phrase(d,normal);
                    celldesc.setPhrase(par);
                    table2.addCell(celldesc);
                }

            } // end j for loop
            personnel++;
        } // end i for loop

        PdfPTable table3 = new PdfPTable(1);
        cell = new PdfPCell(new Paragraph("ADMINISTRATIVE SERVICES",fontbold));
        cell.setPaddingBottom(10);
        cell.setPaddingTop(10);
        cell.setBackgroundColor(new BaseColor(196,188,150));
        table3.addCell(cell);



        // will have cell 1 to cell 4.
        PdfPTable table4 = new PdfPTable(pointColumnWidths);

        String[][] desc2 = {{"Accountant","Bachelor's Degree in Accountancy (may be outsourced)","DOCUMENT REVIEW\n" +
                "Diploma/Certificate of units earned\n" +
                "Updated PRC license (if applicable)\n" +
                "Certificate of Trainings attended\n" +
                "Proof of Employment","1","",""},{"Billing Officer","With Bachelor's Degree relevant to job","1","",""},{"Budget/Finance Officer","","1","",""},{"Cashier","1","",""},{"Human Resource Management Officer/Personenel Officer","1","",""},{"Bookkeeper","1","",""}};
        for(int i = 0; i < desc2.length; i++) {
            Log.d("count",count+"");
            for(int j = 0; j< desc2[i].length; j++) {

                Log.d("personnel",personnel+"");

                if(j==2 && i ==0){
                    //PdfPCell cells = new PdfPCell(new Phrase(desc2[i][j],normal));
                    PdfPCell cells = celldesc;
                    cells.setPhrase(new Phrase(desc2[i][j],normal));
                    cells.setRowspan(6);
                    table4.addCell(cells);
                }else if(i==2 && j==1){
                    PdfPCell  cells = celldesc;
                    cells.setPhrase(new Phrase(desc2[i][j],normal));
                    cells.setRowspan(4);
                    table4.addCell(cells);
                }else if((desc2[i].length == 4 && j==2) || (desc2[i].length == 6 && j==4) || (desc2[i].length == 5 && j==3)){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table4.addCell(imgcell);
                    }else{
                        table4.addCell(imgcell2);
                    }

                }else if((desc2[i].length == 4 && j==3) || (desc2[i].length == 6 && j==5) || (desc2[i].length == 5 && j==4)){
                    String res = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(res));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table4.addCell(cells);
                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc2[i][j],normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table4.addCell(cells);
                }


            } // end j for loop
            table4.completeRow();
            personnel++;
            count++;
        } // end i for loop
        PdfPTable table5 = new PdfPTable(pointColumnWidths);
        String[][] desc3 = {{"Supply Officer/storekeeper",
                "With appropriate training and experience",
                "DOCUMENT REVIEW\n" +
                "Certificate of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)",
                "1","",""},
                {"Medical Records officer",
                        "Bachelor's Degree\n" +
                "Training in ICD 10\n" +
                "Training in Medical Records Management",
                        "DOCUMENT REVIEW\n" +
                "Diploma/Certificate of units earned\n" +
                "Certificate of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)",
                        "1","",""}};

        for(int i = 0; i < desc3.length; i++) {
            for(int j = 0; j< desc3[i].length; j++) {
                Log.d("personnel",personnel+"");
                String d = desc3[i][j];
                if(j==4){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table5.addCell(imgcell);
                    }else{
                        table5.addCell(imgcell2);
                    }
                }else if(j==5){
                    String res = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(res,normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table5.addCell(cells);

                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc3[i][j],normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table5.addCell(cells);
                }



            } // end j for loop

            personnel++;
            count++;
        } // end i for loop
        String[][] desc4 = {{"Medical Social worker (Full Time)","Licensed social worker","DOCUMENT REVIEW\n" +
                "Diploma/Certificate of units earned\n" +
                "Update PRC license\n" +
                "Certificate of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)","1","11","11"},{"Nutritionist-Dletician (Full Time)","License nutritionist","1","12","12"},{"Utility Worker","May be outsourced\n" +
                "\n" +
                "Security guard must be licensed","DOCUMENT REVIEW\n" +
                "Relevant Training\n" +
                "Licensed, if applicable\n" +
                "Proof of Employment/Appointment (notarized)\n" +
                "Notarized MOA if outsourced","1 per shift","13","13"},{"Security Guard","1 per shift","14","14"},{"Laundry worker","1","15","15"}};
        for(int i = 0; i < desc4.length; i++) {
            for(int j = 0; j< desc4[i].length; j++) {

                Log.d("checks",j+"");
                if(j==2 && i ==0){
                    //PdfPCell cells = new PdfPCell(new Phrase(desc2[i][j],normal));
                    PdfPCell cells = celldesc;
                    cells.setPhrase(new Phrase(desc4[i][j],normal));
                    cells.setRowspan(2);
                    table5.addCell(cells);
                }else if(i==2 && j==1){
                    PdfPCell  cells = celldesc;
                    cells.setPhrase(new Phrase(desc4[i][j],normal));
                    cells.setRowspan(3);
                    table5.addCell(cells);
                }else if(i==2 && j==2){
                    PdfPCell  cells = celldesc;
                    cells.setPhrase(new Phrase(desc4[i][j],normal));
                    cells.setRowspan(3);
                    table5.addCell(cells);
                }else if((desc4[i].length == 6 && j==4) || (desc4[i].length == 5 && j==3) || (desc4[i].length == 4 && j==2)){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table5.addCell(imgcell);
                    }else{
                        table5.addCell(imgcell2);
                    }
                }else if((desc4[i].length == 6 && j==5) || (desc4[i].length == 5 && j==4) || (desc4[i].length == 4 && j==3)){
                    String res = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(res,normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table5.addCell(cells);
                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc4[i][j],normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table5.addCell(cells);
                }


            } // end j for loop
            personnel++;
            table5.completeRow();
            count++;
        } // end i for loop
        PdfPTable table6 = new PdfPTable(1);
        cell = new PdfPCell(new Paragraph("CLINICAL SERVICES",fontbold));
        cell.setPaddingBottom(10);
        cell.setPaddingTop(10);
        cell.setBackgroundColor(new BaseColor(196,188,150));
        table6.addCell(cell);

        PdfPTable table7 = new PdfPTable(pointColumnWidths);
        String[][] desc5 = {{"Consultant Staff in Og-Gyn Pediatrics, Medicine, Surgery, and Anesthesia.\n" +
                "\n" +
                "*Hospital may have additional consultants from other specialties","Licensed physician\n" +
                "Fellow/Diplomate\n" +
                "ACLS certified (for Surgeons and Anesthesiologist)","DOCUMENT REVIEW\n" +
                "Certificate from Specialty society, if applicable (for Board Certified)\n" +
                "Residency Training Certificate (for Board Eligible)\n" +
                "Certificate of Residency Training/ Medical\n" +
                "\n" +
                "Specialist(*DOH Medical Specialist, last exam was in 1989)\n" +
                "\n" +
                "Updated PRC license\n" +
                "Certificates of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)","All consultants must be at least board eligible. At least one consultant must be board certified per specialty. ","16","16"},{"Resident Physician on Duty (Shall not go on duty for more than 48 hours straight).","Licensed physician","DOCUMENT REVIEW\n" +
                "Updated PRC license\n" +
                "Certificates of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)\n" +
                "Schedule of duty approved by Medical Director/Chief of Hospital","Wards – 1:20 beds at any given time PLUS ER – at least 1 at any given time\n" +
                "\n" +
                "*This ratio does not include Resident Physicians on Duty that shall be required for add-on services such as dialysis facility. It shall be counted separately.\n" +
                "\n","17","17"}};

        for(int i = 0; i < desc5.length; i++) {
            for(int j = 0; j< desc5[i].length; j++) {

                if(j==4){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table7.addCell(imgcell);
                    }else{
                        table7.addCell(imgcell2);
                    }
                }else if(j==5){
                    String remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(remarks,normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table7.addCell(cells);
                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc5[i][j]));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table7.addCell(cells);
                }


            } // end j for loop
            personnel++;
        } // end i for loop



        PdfPTable table8 =  new PdfPTable(1);
        cell = new PdfPCell(new Paragraph("NURSING SERVICES",fontbold));
        cell.setPaddingBottom(10);
        cell.setPaddingTop(10);
        cell.setBackgroundColor(new BaseColor(196,188,150));
        table8.addCell(cell);

        PdfPTable table9 = new PdfPTable(pointColumnWidths);
        String[][] desc6 = {{"Supervising Nurse/Nurse Manager","Licensed nurse\n" +
                "With at least nine (9) units of Master’s Degree in Nursing\n" +
                "At least two (2) years experience in general nursing service administration.","DOCUMENT REVIEW\n" +
                "Diploma/Certificate of Units Earned\n" +
                "Updated PRC license\n" +
                "Certificates of Trainings attended\n" +
                "Proof of Employment/Appointment (notarized)\n" +
                "Service Record/Certificate of employment (Proof of general nursing service administration experience)","1:50 Beds Office hours only (8am to 5pm)","18","18"},
                {"Head Nurse/Senior Nurse","Licensed nurse\n" +
                        "With at least 2 years hospital experience\n" +
                        "BLS certified","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificate of trainings attended\n" +
                        "Proof of employment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity period of the hospital’s LTO.\n" +
                        "Schedule of duty approved by Chief Nurse","1:15 staff nurses","19","19"},
                {"Staff Nurse","Licensed nurse\n" +
                        "BLS certified","Ward - 1:12 Beds at any given time (plus 1 reliever for every 3 RNs)","20","20"},
                {"Nursing Attendant","Highschool graduate\n" +
                        "With relevant health related training (may be in house training)","DOCUMENT REVIEW\n" +
                        "Certificate of trainings attended\n" +
                        "Proof of Employment/ Appointment (notarized)","1:24 beds at any given time (plus 1 reliever for every 3 NAs)","21","21"},
                {"Operating Room Nurse:\n" +
                        "\n" +
                        "-Scrub Nurse (SN)\n" +
                        "\n" +
                        "-Circulating Nurse (CN)","Licensed nurse\n" +
                        "Training in OR Nursing\n" +
                        "Training in BLS and ACLS","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificate of trainings attended\n" +
                        "Proof of employment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity period of the hospital’s LTO.\n" +
                        "Schedule of duty","1 SN and 1 CN per functioning OR per shift (plus 1 reliever for every 3 nurses)","22","22"},
                {"Delivery room Nurse","Licensed nurse\n" +
                        "Training in Maternal and Child Nursing (maybe in house training or training in Essential Integrated Newborn Care [EINC])\n" +
                        "Training in BLS and ACLS","1 per 3 delivery table per shift (plus 1 reliever for every 3 nurses)","23","23"},
                {"Emergency Room Nurse","Licensed nurse\n" +
                        "Training in Trauma Nursing, ACLS and other relevant training","approved by Chief Nurse","1:3 beds per shift (plus 1 reliever for every 3 nurses)","24","24"},
                {"Outpatient Department Nurse","Licensed nurse\n" +
                        "Training n BLS","","1","25","25"}};

        for(int i = 0; i < desc6.length; i++) {
            for(int j = 0; j< desc6[i].length; j++) {

                if(j==2 && i==1){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(new Phrase(desc6[i][j],normal));
                    cells.setRowspan(2);
                    table9.addCell(cells);
                }else if(j==2 && i==4){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(new Phrase(desc6[i][j],normal));
                    cells.setRowspan(2);
                    table9.addCell(cells);
                }else if((desc6[i].length == 6 && j==4) || (desc6[i].length == 5 && j==3)){
                    String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table9.addCell(imgcell);
                    }else{
                        table9.addCell(imgcell2);
                    }
                }else if((desc6[i].length == 6 && j==5) || (desc6[i].length == 5 && j==4)){
                    String remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(remarks,normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table9.addCell(cells);
                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc6[i][j],normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table9.addCell(cells);
                }


            } // end j for loop
            table9.completeRow();
            personnel++;
            count++;

        } // end i for loop

        Paragraph title2 = new Paragraph("ATTACHMENT 1.B - PHYSICAL PLANT",fontbold);
        title2.setPaddingTop(30);
        title2.setSpacingBefore(10f);
        title2.setSpacingAfter(10f);

        PdfPTable table10 = new PdfPTable(3);
        String[][] desc7 = {{"DOCUMENTS","COMPLIED","REMARKS"},{"1. DOH –Approved PTC","",""},{"2. DOH Approved Floor Plan","",""},
                {"2. Checklist for Review of Floor Plans (accomplished)","",""}};

        for(int i = 0; i < desc7.length; i++) {
            for(int j = 0; j< desc7[i].length; j++) {
                String d = desc7[i][j];

                Phrase par = new Phrase(d,normal);
                if(i==0){
                    PdfPCell cells = new PdfPCell();
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(148,138,84));
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPhrase(par);
                    table10.addCell(cells);
                }else if(j==1){
                    String res = get_col_results("OTHERS",others+"",HomeActivity.appid);
                    if(res.equals("true")){
                        table10.addCell(imgcell);
                    }else{
                        table10.addCell(imgcell2);
                    }
                }else if(j==2){
                    String remarks = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
                    PdfPCell cells = new PdfPCell(new Phrase(remarks,normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table10.addCell(cells);
                    others++;
                }else{
                    PdfPCell cells = new PdfPCell(new Phrase(desc7[i][j],normal));
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    table10.addCell(cells);
                }

            } // end j for loop
            count++;
        } // end i for loop

        //

        Paragraph title3 = new Paragraph("OBSERVATION/FINDINGS (may use separate additional sheet if needed)",fontbold);
        title3.setPaddingTop(30);
        title3.setSpacingBefore(10f);
        title3.setSpacingAfter(10f);


        PdfPTable table11 = new PdfPTable(1);
        PdfPCell text = new PdfPCell();
        text.setPadding(100);
        String r = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
        text.setPhrase(new Phrase(r));
        table11.addCell(text);
        others++;

        Paragraph title4 = new Paragraph("ATTACHMENT 1.C - EQUIPMENT INSTRUMENT",fontbold);
        title4.setPaddingTop(30);
        title4.setSpacingBefore(10f);
        title4.setSpacingAfter(10f);


        Paragraph title5 = new Paragraph("ATTACHMENT 1.D - EMERGENCY CART CONTENTS FOR LEVEL 1 HOSPITAL",fontbold);
        title5.setPaddingTop(30);
        title5.setSpacingBefore(10f);
        title5.setSpacingAfter(10f);

        Paragraph notes = new Paragraph("*Notes:\n" +
                "ER – Emergency Room\n" +
                "OR – Operating Room\n" +
                "DR – Delivery Room\n" +
                "NS – Nurses’ Station " +
                "ATTACHMENT 1.E - ADD-ON SERVICE\n" +
                "\n" +
                "Level 1 hospitals applying for the following add-on services must comply first with the licensing\n" +
                "\n" +
                "standards for the following:\n" +
                "\n" +
                "1. Physical plant of the desired add-on service by securing an approved DOH Permit to Construct; and\n" +
                "\n" +
                "2. Licensing standards for the required ancillary and support units (e.g. tertiary clinical laboratory, Level 2 x-ray facility, board certified specialists, and respiratory therapy unit).\n" +
                "\n" +
                "Thus, it is still strongly recommended to upgrade to a higher level of hospital.",normal);
        notes.setPaddingTop(30);
        notes.setSpacingBefore(10f);
        notes.setSpacingAfter(10f);



        Paragraph title6 = new Paragraph("A. INTENSE CARE UNIT (ICU)",fontbold);
        title6.setPaddingTop(30);
        title6.setSpacingBefore(10f);
        title6.setSpacingAfter(10f);

        Paragraph title7 = new Paragraph("B. NEONTAL INTENSIVE CARE UNIT (NICU)",fontbold);
        title7.setPaddingTop(30);
        title7.setSpacingBefore(10f);
        title7.setSpacingAfter(10f);

        Paragraph title8 = new Paragraph("C. HIGH RISK PREGNANCY UNIT (HRPU)",fontbold);
        title8.setPaddingTop(30);
        title8.setSpacingBefore(10f);
        title8.setSpacingAfter(10f);

        Paragraph title9 = new Paragraph("PART IV - LEVEL 1 ATTACHMENT 2.A – PERSONNEL",fontbold);
        title9.setPaddingTop(30);
        title9.setSpacingBefore(10f);
        title9.setSpacingAfter(10f);

        Paragraph notes1 = new Paragraph("D. AMBULATORYSURGICAL CLINICS (ASC)\n" +
                "\n" +
                "- Refer to assessment tool for (ASC)\n" +
                "\n" +
                "E. DIALYSIS CLINICS\n" +
                "\n" +
                "- Refer to assessment tool for Dialysis Clinics",normal);



        Paragraph title10 = new Paragraph("ATTACHMENT 2.B – PHYSICAL TEST",fontbold);
        title10.setPaddingTop(30);
        title10.setSpacingBefore(10f);
        title10.setSpacingAfter(10f);

        Paragraph title11 = new Paragraph("OBSERVATIONS/FINDINGS (may use separate additional sheets if needed):",fontbold);
        title11.setPaddingTop(30);
        title11.setSpacingBefore(10f);
        title11.setSpacingAfter(10f);

        Paragraph title12 = new Paragraph("ATTACHMENT 2.C- EQUIPMENT/INSTRUMENT",fontbold);
        title12.setPaddingTop(30);
        title12.setSpacingBefore(10f);
        title12.setSpacingAfter(10f);

        Paragraph title13 = new Paragraph("ATTACHMENT 2.D – EMERGENCY CART CONTENTS FOR LEVEL 2 HOSPITAL",fontbold);
        title13.setPaddingTop(30);
        title13.setSpacingBefore(10f);
        title13.setSpacingAfter(10f);

        Paragraph notes2 = new Paragraph("*Notes:\n" +
                "ER – Emergency Room\n" +
                "OR – Operating Room\n" +
                "DR – Delivery Room\n" +
                "NS – Nurses’ Station",normal);
        notes2.setPaddingTop(30);
        notes2.setSpacingBefore(10f);
        notes2.setSpacingAfter(10f);

        Paragraph inspected = new Paragraph("Inspected By:",fontbold);
        inspected.setPaddingTop(30);
        inspected.setSpacingBefore(10f);
        inspected.setSpacingAfter(10f);

        PdfPTable namesign = new PdfPTable(3);
        namesign.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        namesign.addCell(new Phrase("Printed Name"));
        namesign.addCell(new Phrase("Signature"));
        namesign.addCell(new Phrase("Position/Designation"));
        namesign.addCell(new Phrase(uid));
        namesign.addCell(new Phrase(" "));
        namesign.addCell(new Phrase(" "));

        PdfPTable footer = new PdfPTable(1);
        footer.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        footer.addCell(new Phrase("Signature"));
        footer.addCell(new Phrase("Printed Name"));
        footer.addCell(new Phrase("Position/Designation"));
        footer.addCell(new Phrase("Date"));

        PdfPTable table12 = new PdfPTable(5);
        PdfPTable table13 = new PdfPTable(1);
        PdfPTable table14 = new PdfPTable(5);
        PdfPTable table15 = new PdfPTable(1);
        PdfPTable table16 = new PdfPTable(5);
        PdfPTable table17 = new PdfPTable(1);
        PdfPTable table18 = new PdfPTable(5);
        PdfPTable table19 = new PdfPTable(1);
        PdfPTable table20 = new PdfPTable(5);
        PdfPTable table21 = new PdfPTable(1);
        PdfPTable table22 = new PdfPTable(5);
        PdfPTable table23 = new PdfPTable(1);
        PdfPTable table24 = new PdfPTable(5);
        PdfPTable table25 = new PdfPTable(1);
        PdfPTable table26 = new PdfPTable(5);
        PdfPTable table27 = new PdfPTable(1);
        PdfPTable table28 = new PdfPTable(5);
        PdfPTable table29 = new PdfPTable(1);
        PdfPTable table30 = new PdfPTable(5);
        PdfPTable table31 = new PdfPTable(1);
        PdfPTable table32 = new PdfPTable(5);
        PdfPTable table33 = new PdfPTable(1);
        PdfPTable table34 = new PdfPTable(5);
        float [] pointColumn = {500F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,300F};
        PdfPTable table35 = new PdfPTable(pointColumn);
        PdfPTable table36 = new PdfPTable(1);
        PdfPTable table37 = new PdfPTable(pointColumn);
        PdfPTable table38 = new PdfPTable(1);
        PdfPTable table39 = new PdfPTable(pointColumnWidths);
        PdfPTable table40 = new PdfPTable(1);
        PdfPTable table41 = new PdfPTable(4);
        PdfPTable table42 = new PdfPTable(1);
        PdfPTable table43 = new PdfPTable(pointColumnWidths);
        PdfPTable table44 = new PdfPTable(1);
        PdfPTable table45 = new PdfPTable(4);
        PdfPTable table46 = new PdfPTable(1);
        PdfPTable table47 = new PdfPTable(pointColumnWidths);
        PdfPTable table48 = new PdfPTable(1);
        PdfPTable table49 = new PdfPTable(4);
        PdfPTable table50 = new PdfPTable(pointColumnWidths);
        PdfPTable table51 = new PdfPTable(pointColumnWidths);
        PdfPTable table52 = new PdfPTable(1);
        PdfPTable table53 =  new PdfPTable(pointColumnWidths);
        PdfPTable table54 = new PdfPTable(1);
        PdfPTable table55 =  new PdfPTable(pointColumnWidths);
        PdfPTable table56 = new PdfPTable(1);
        PdfPTable table57 =  new PdfPTable(pointColumnWidths);
        PdfPTable table58 = new PdfPTable(3);
        PdfPTable table59 =  new PdfPTable(1);
        PdfPTable table60 =  new PdfPTable(5);
        PdfPTable table61 =  new PdfPTable(5);
        PdfPTable table62 =  new PdfPTable(5);
        PdfPTable table63 =  new PdfPTable(5);
        PdfPTable table64 =  new PdfPTable(5);
        PdfPTable table65 =  new PdfPTable(5);
        PdfPTable table66 =  new PdfPTable(1);
        PdfPTable table67 =  new PdfPTable(5);
        PdfPTable table68 =  new PdfPTable(1);
        PdfPTable table69 =  new PdfPTable(5);
        PdfPTable table70 =  new PdfPTable(1);
        PdfPTable table71 =  new PdfPTable(5);
        PdfPTable table72 =  new PdfPTable(1);
        PdfPTable table73 =  new PdfPTable(5);
        PdfPTable table74 =  new PdfPTable(1);
        PdfPTable table75 =  new PdfPTable(5);
        PdfPTable table76 =  new PdfPTable(1);
        PdfPTable table77 =  new PdfPTable(5);
        PdfPTable table78 =  new PdfPTable(5);
        PdfPTable table79 =  new PdfPTable(5);
        float [] pointColumns2 = {500F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,100F,300F};
        PdfPTable table80 = new PdfPTable(pointColumns2);
        PdfPTable table81 = new PdfPTable(pointColumns2);
        String[][] desc8= {{"EQUIPMENT/INSTRUMENT (Functional)","QUANTITY","AREA","COMPLIED","REWARD"},{"ADMINISTRATIVE SERVICE"},
                {"Ambulance\n" +
                        "If owned by hospital, available 24/7 and physical present if not being used during time of inspection/monitoring\n" +
                        "If outsourced, shall be on call but able to respond within reasonable time.","1","Parking","",""},
                {"Computer with Internet Access","1","Administrative Office","",""},
                {"Emergency Light","","Lobby, hallway, nurses’ station, office/unit and stairways\t","","1"},
                {"Fire Extinguishers","1 per unit or area","Lobby, hallway, nurses’ station, office/unit and stairways","",""}
                ,{"Generator set with automatic Transfer Switch (ATS)","1","Genset house","",""},
                {"KITCHEN DIETARY"},
                {"Exhaust fan","1","Kitchen","",""},
                {"Food Conveyor or equivalent (closed-type)","1","",""},
                {"Food Scale","1","",""},
                {"Blender/Osteorizer","1","",""},
                {"Oven","1","",""},
                {"Stove","1","",""},
                {"Refrigerator/Freezer","1","",""},
                {"Utility cart","1","",""},
                {"Garbage Receptacle with Cover (color-coded)","1 for each color","",""},
                {"EMERGENCY ROOM"},
                {"Bag-valve-mask Unit\n" +
                        "Adult\n" +
                        "Pediatric","1","ER","",""},
                {"Calculator for dose computation","1","",""},
                {"Clinical Weighing scale","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"Delivery set, primigravid","2 sets","",""},
                {"Delivery set, multigravid","2 sets","",""},
                {"ECG Machine with leads","1","",""},
                {"EENT Diagnostic set with Ophthalmoscope and Otoscope","1","",""},
                {"Emergency Cart (for contents, refer to separate list).","1","",""},
                {"Examining table","1","",""},
                {"Examining table (with Stirrups for OB-Gyne)","1","",""},
                {"Glucometer with strips","1","23","23"},
                {"Gooseneck lamp/Examining Light","1","",""},
                {"Instrument/Mayo Table","1","",""},
                {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thoracostomy, Cutdown, etc.)","2 sets","",""},
                {"Nebulizer","1","",""},
                {"Neurologic Hammer","1","",""},
                {"OR Light (portable or equivalent)","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not from pipeline","2","",""},
                {"Pulse Oximeter","1","",""},
                {"Sphygmomanometer, Non-mercurial\n" +
                        "Adult Cuff\n" +
                        "Pediatric Cuff","1 \n" +
                        "1 ","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Suturing Set","2 sets","",""},
                {"Thermometer, non –mercurial\n" +
                        "Oral\n" +
                        "Rectal","1 \n" +
                        "1 ","",""},
                {"Vaginal Speculum, Different Sizes","1 for each different size","",""},
                {"Wheelchair","1","",""},
                {"Wheeled Stretcher with guard/side rails and wheel lock or anchor.","1","",""},
                {"OUT-PATIENT DEPARTMENT"},
                {"Clinical Height and Weight Scale","1","OPD","",""},
                {"EENT Diagnostic set with ophthalmoscope and otoscope","1","",""},
                {"Gooseneck lamp/Examining Light","1","",""},
                {"Examining table with wheel lock or anchor","1","",""},
                {"Instrument/Mayo Table","1","",""},
                {"Minor Instrument Set","1","",""},
                {"Neurologic Hammer","1","",""},
                {"Oxygen Unit\n" +
                        "\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Peak flow meter\n" +
                        "Adult\n" +
                        "Pediatric","1 \n" +
                        "1 ","",""},
                {"Sphygmomanometer, N0n-mercurial\n" +
                        "Adult\n" +
                        "Pediatric","1 \n" +
                        "1 ","",""},
                {"Stethoscope","1","",""},
                {"Thermometer, non-mercurial\n" +
                        "Oral\n" +
                        "Rectal","1 \n" +
                        "1 ","",""},
                {"Suture Removal Set","1","",""},
                {"Wheelchair / Wheeled Stretcher","1","",""},
                {"OPERATING ROOM"},
                {"Air conditioning Unit","1/OR","OR","",""},
                {"Anesthesia Machine","1/OR","",""},
                {"Cardiac Monitor with Pulse Oximeter","1/OR","",""},
                {"Caesarean Section Instrument","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"Electrocautery machine","1","",""},
                {"Emergency Cart (for contents, refer to separate list)","1","",""},
                {"Glucometer with strips","","",""},
                {"Instrument / Mayo table","1","",""},
                {"Laparotomy pack (Linen pack)","1 per OR","",""},
                {"Laparatomy / Major Instrument Set","1 per OR","",""},
                {"Laryngoscopes with different sizes of blades","1","",""},
                {"Operating room light","1 per OR","",""},
                {"Operating room table","1 per OR","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1 per OR","",""},
                {"Rechargeable Emergency Light (in case generator malfunction)","1 per OR","",""},
                {"Sphygmomanometer, N0n-mercurial\n" +
                        "Adult cuff\n" +
                        "Pediatric cuff","1 per OR \n" +
                        "1 per OR ","",""},
                {"Spinal Set","1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, non-mercurial\n" +
                        "Oral\n" +
                        "Rectal","1","",""},
                {"Wheeled Stretcher with guard/side rails and wheel lock or anchor.","1","",""},
                {"POST ANESTHESIA CARE UNIT / RECOVERY ROOM"},
                {"Air conditioning unit","1","PACU/RR","",""},
                {"Cardiac Monitor","1","",""},
                {"Defibrillator with paddles","1 (if separate from the OR Complex)","",""},
                {"Emergency Cart (for contents, refer to separate list)","1 (if separate from the OR Complex)","",""},
                {"Glucometer with strips","","",""},
                {"Mechanical / patient bed, with guard side rails and wheel lock or anchored","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Pulse Oximeter","","",""},
                {"Sphygmomanometer, N0n-mercurial\n" +
                        "Adult cuff\n" +
                        "Pediatric cuff","1 \n" +
                        "1 ","",""},
                {"Stethoscope","1","",""},
                {"Thermometer, non- mercurial","1","",""},
                {"LABOR ROOM"},
                {"Fetal Doppler","1","","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Patient Bed","1","",""},
                {"Pulse Oximeter","1","",""},
                {"Sphygmomanometer, N0n-mercurial","1","",""},
                {"Stethoscope","1","",""},
                {"Thermometer, Non- mercurial","1","",""},
                {"DELIVERY ROOM"},
                {"Air-conditioning Unit","1","DR","",""},
                {"Bag valve mask unit (Adult and pediatric)","1","",""},
                {"Bassinet","","",""},
                {"Clinical Infant Weighing scale","1","",""},
                {"Defibrillator with paddles","1 (if DR is separate from OR Complex)","",""},
                {"Delivery set, primigravid","1 set","",""},
                {"Delivery set, multigravid","2 set","",""},
                {"Delivery room light","1 (if DR is separate from OR Complex)","",""},
                {"Defibrillator with paddles","1","",""},
                {"Delivery room table","1","",""},
                {"Emergency Cart (for contents, refer to separate list)","1 (if DR is separate from OR Complex)","",""},
                {"Instrument / Mayo Table","1","",""},
                {"Kelly Pad or equivalent","1","",""},
                {"Laryngoscope with different sizes of blades","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Rechargeable Emergency Light (in case generator malfunction)","1","",""},
                {"Sphygmomanometer, N0n-mercurial","1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Wheeled Stretcher","1","",""},
                {"NURSING UNIT/WARD"},
                {"Bag-Valve-Mask Unit\n" +
                        "Adult\n" +
                        "Pediatric","1 \n" +
                        "1 ","NURSING UNIT/WARD","",""},
                {"Clinical Height and Weight Scale","1","",""},
                {"Defibrillator with paddles","1","","Nursing units located on the same floor may share the defibrillator and the E-cart, Provided that they are not more than 50 meters away from each other."},
                {"Emergency cart or equivalent (refer to separate list for the contents)","1","",""},
                {"EENT Diagnostic set with ophthalmoscope and otoscope","1","",""},
                {"Laryngoscope with different sizes of blades","1","",""},
                {"Mechanical/Patient bed with lock, if wheeled; with guard or side rails","ABC","",""},
                {"Bedside Table","ABC","",""},
                {"Nebulizer","1","",""},
                {"Neurologic Hammer","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ if not pipeline","1","",""},
                {"Sphygmomanometer, N0n-mercurial\n" +
                        "Adult cuff\n" +
                        "Pediatric cuff","1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, non –mercurial\n" +
                        "Oral\n" +
                        "Rectal","1","",""},
                {"CENTRAL STERILIZING & SUPPLY ROOM"},
                {"Autoclave/steam Sterilizer","1","CSSR","",""},
                {"CADAVER HOLDING AREA/ROOM"},
                {"Bed or stretcher for cadaver","1","CADAVER HOLDING AREA","",""},
                {"EMERGENCY CART CONTENTS","ER","OR","DR","NS 1","NS 2","NS 3","NS 4","NS 5","NS 6","NS 7","NS 8","NS 9","NS 10","NS 11","NS 12","REMARKS"},
                {"Adenosine 6 mg/2mL vial","","","","","","","","","","","","","","","",""},
                {"Amiodarone 150mg/3mL ampule","","","","","","","","","","","","","","","",""},
                {"Anti-tetanus serum (either equine-based antiserum or human antiserum)","","","","","","","","","","","","","","","",""},
                {"Aspirin USP grade (325 mg/tablet)","","","","","","","","","","","","","","","",""},
                {"Atropine 1 mg/ml ampule\t","","","","","","","","","","","","","","","",""},
                {"B-adrenergic agonists (i.e. Salbutamol 2mg/ml)","","","","","","","","","","","","","","","",""},
                {"Benzodiazepine (Diazepam 10mg/2ml ampule and/or Midazolam) (in high alert box)\t","","","","","","","","","","","","","","","",""},
                {"Calcium (usually calcium gluconate 10% solution in 10 mL ampule)","","","","","","","","","","","","","","","",""},
                {"Clopidogrel 75 mg tablet\t","","","","","","","","","","","","","","","",""},
                {"D5W 250 mL","","","","","","","","","","","","","","","",""},
                {"D50W 50mg/vial","","","","","","","","","","","","","","","",""},
                {"Digoxin 0.5mg/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Diphenhydramine 50mg/mL ampule","","","","","","","","","","","","","","","",""},
                {"Dobutamine 250mg/5mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Dopamine 200mg/5mL ampule/vial","","","","","","","","","","","","","","","",""},
                {"Epinephrine 1mg/ml ampule\t","","","","","","","","","","","","","","","",""},
                {"Haloperidol 50mg/mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Hydrocortisone 250mg/2mL vial","","","","","","","","","","","","","","","",""},
                {"Lidocaine 10% in 50mL spray\t","","","","","","","","","","","","","","","",""},
                {"Lidocaine 2% solution vial 1g/50ml","","","","","","","","","","","","","","","",""},
                {"Magnesium sulphate 1g/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Mannitol 20% solution in 500ml/bottle","","","","","","","","","","","","","","","",""},
                {"Methylprednisolone 4mg/tablet","","","","","","","","","","","","","","","",""},
                {"Metoclopramide 10mg/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Morphine sulphate 10mg/mL ampule (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Nitroglycerin inj. 10mg/10mL ampule or Isosorbide dinitrate 5mg SL tablet or 10 mg/10mL ampule","","","","","","","","","","","","","","","",""},
                {"Paracetamol 300mg/ampule (IV preparation)","","","","","","","","","","","","","","","",""},
                {"Phenobarbital 120mg/ml ampule IV or 30mg tablet (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Phenytoin 100mg/capsule or 100 mg.2mL ampule","","","","","","","","","","","","","","","",""},
                {"Plain LRS 1L/bottle","","","","","","","","","","","","","","","",""},
                {"Plain NSS 1L/bottle-0.9% Sodium Chloride","","","","","","","","","","","","","","","",""},
                {"Potassium Chloride 40mEq/20mL vial (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Vitamin B1/6/12 vial (1g B1, 1g B6, 0.01gB12 in 10 mL vial)\t","","","","","","","","","","","","","","","",""},
                {"Sodium bicarbonate 50mEq/50mL ampule","","","","","","","","","","","","","","","",""},
                {"Verapamil 5 mg/2ml ampule","","","","","","","","","","","","","","","",""},
                {"EQUIPMENT/SUPPLIES"},
                {"Airway adjuncts\t","","","","","","","","","","","","","","","",""},
                {"Airway / Intubation Kit (with stylet and bag valve masks)","","","","","","","","","","","","","","","",""},
                {"Alcohol disinfectant","","","","","","","","","","","","","","","",""},
                {"Aseptic bulb syringe\t","","","","","","","","","","","","","","","",""},
                {"Calculator","","","","","","","","","","","","","","","",""},
                {"Capillary Blood Glucose (CBG) Kit","","","","","","","","","","","","","","","",""},
                {"Cardiac Board","","","","","","","","","","","","","","","",""},
                {"Endotracheal Tubes, all sizes\t","","","","","","","","","","","","","","","",""},
                {"Flashlights or Pen lights","","","","","","","","","","","","","","","",""},
                {"Gloves, sterile","","","","","","","","","","","","","","","",""},
                {"Gloves,non-sterile","","","","","","","","","","","","","","","",""},
                {"Laryngoscope with different sizes of blades","","","","","","","","","","","","","","","",""},
                {"Nasal cannula","","","","","","","","","","","","","","","",""},
                {"Protective face shield or mask or goggles","","","","","","","","","","","","","","","",""},
                {"Standard face mask\t","","","","","","","","","","","","","","","",""},
                {"Sterile gauze (pre-folded and individually packed)\t","","","","","","","","","","","","","","","",""},
                {"Syringes (different volumes)\t","","","","","","","","","","","","","","","",""},
                {"Urethral catheter","","","","","","","","","","","","","","","",""},
                {"Urine collection bag","","","","","","","","","","","","","","","",""},
                {"Waterproof aprons","","","","","","","","","","","","","","","",""},
                {"I. ICU PERSONNEL"},
                {"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"},
                {"Multidisciplinary Team composed of, but not limited to, board certified Cardiologist, Pulmonologist, Neurologist, Pulmonologist OR an Intensivist","Licensed physician\n" +
                        "Fellow/Diplomate","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate from Specialty society\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/ Appointment (notarized)","A team composed of at least 1 per specialty (May be part time or visiting consultant/s) OR an intensivist","",""},
                {"Nurse","Licensed nurse\n" +
                        "Certificate of Training in Critical Care Nursing, ACLS","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/ Appointment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                        "Schedule of duty approved by Chief Nurse","1:3 beds at any time per shift (plus 1 reliever for every 3 RNs)","",""},
                {"Nursing Attendant","Highschool graduate\n" +
                        "With relevant health-related training (may be in house training)","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment (notarized)","1:12 beds at any time (plus 1 reliever for every 3 NA/MWs)","26","26"},
                {"II. ICU EQUIPMENT"},
                {"EQUIPMENT/INSTRUMENT (Functional)","QUANTITY","COMPLIED","REMARKS"},
                {"Air Conditioning Unit ","1","",""},
                {"Bag-valve-mask Unit\n" +
                        "Adult\n" +
                        "Pediatric","1 \n" +
                        "1 ","",""},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"BDefibrillator with paddles","1","",""},
                {"EENT Diagnostic set with ophthalmoscope and otoscope","1 set","",""},
                {"Emergency Cart (for contents, refer to separate list).","1","",""},
                {"Infusion pump","1","",""},
                {"Larynfoscope with different sizes of blades","1","",""},
                {"Mechanical Bed","Depending on the number of beds applied","",""},
                {"Mechanical Ventilator (May be outsourced)","1","",""},
                {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thorascostomy, Cutdown, etc.)","1 set","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Sphygmomanometer, Non-mercurial\n" +
                        "Adult Cuff\n" +
                        "Pediatric Cuff","1 \n" +
                        "1 ","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, Non-mercurial","1","",""},
                {"I. NICU PERSONNEL"},
                {"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"},
                {"Multidisciplinary team composed of, but not limited to, pediatric cardiologist, nephrologist, pediatric pulmonologist OR a neonatologist","Licensed physician\n" +
                        "Fellow/Diplomate","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate from Specialty society\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","A team composed of at least 1 per specialty (May be part time or vising consultant) OR a neonatologist","",""},
                {"Nurse","Licensed nurse\n" +
                        "Certificate of Training in Critical Care Nursing, ACLS","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificate of trainings attended\n" +
                        "Proof of employment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of the hospital’s LTO. - Schedule of duty approved by Chief Nurse","1:3 bassinets/ incubator/ warmer (1 reliever for Every 3 RNs)","",""},
                {"Nursing attendants/Midwife","Highschool graduate\n" +
                        "With relevant health-related training (may be in house training","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment (notarized)","1:12 bassinets/ incubator/ warmer (1 reliever for every 3 NAs)","",""},
                {"II. NICU EQUIPMENT"},
                {"EQUIPMENT/INSTRUMENT (Functional)","QUANTITY","COMPLIED","REMARKS"},
                {"Air Conditioning Unit","1","",""},
                {"Bassinet","1","",""},
                {"Bilirubin Light/ Phototherapy machine or equivalent","","",""},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"Clinical Infant Bag-valve mask unit","1","",""},
                {"Clinical Infant weighing scale","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"EENT Diagnostic Set with ophthalmoscope and otoscope","1","",""},
                {"Emergency Cart (for contents, refer to separate list)","1","",""},
                {"Glucometer","1","",""},
                {"Incubator","Depending of the number of beds applied","",""},
                {"Infusion pump","1","",""},
                {"Laryngoscope with neonatal blades of different sizes","1","",""},
                {"Mechanical Ventilator (May be outsourced)","1","",""},
                {"Neonatal Stethoscope","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Refrigerator for Breast milk storage","1","",""},
                {"Sphygmomanometer, N0b-mercurial - Neonate","1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, N0n-mercurial","1","",""},
                {"Umbilical Cannulation set","1 set","",""},
                {"I. HRPU PERSONEL"},
                {"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"},
                {"General obstetricians, preferably with a Perinatologist, and a referral team of IM specialists","General obstetricians, preferably with a Perinatologist, and a referral team of IM specialists","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate from Specialty society\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/ Appointment (notarized)","General Obstetricians, Perinatologist, and IM specialists (May be part time or visiting consultant)","",""},
                {"Nurse","Licensed nurse\n" +
                        "Certificate of Training in Critical Care Nursing, ACLS","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/ Appointment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                        "Schedule of duty approved by Chief Nurse","1:3 beds at any time per shift (plus 1 reliever for every 3 RNs)","",""},
                {"Nursing Attendants/Midwife","Highschool graduate\n" +
                        "With relevant health-related training (may be in house training)","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment (notarized)","1:12 beds at any time (plus 1 reliever for every 3 NA/MWs)","",""},
                {"II. HRPU EQUIPMENT"},
                {"EQUIPMENT/INSTRUMENT (Functional)","QUANTITY","COMPLIED","REMARKS"},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"Cardiotocography (CTG) Machine","1","",""},
                {"Fetal Doppler","1","",""},
                {"Oxygen Unit Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Patient bed with side rails","Refer to approved PTC","",""},
                {"Sphygmomanometer, Non-mercurial","1","",""},
                {"Suction Apparatus","1","",""},
                {"POSITION","QUALIFICATION","EVIDENCE","NUMBER/RATIO","COMPLIED","REMARKS"},
                {"Chief of Hospital/Medical Director","Licensed physician\n" +
                        "Have completed at least twenty (20) units towards a Master’s Degree in Hospital Administration or related course (MPH, MBA, MPA, MHSA, etc.) AND at least five (5) years hospital experience in a supervisory or managerial position","DOCUMENT REVIEW\n" +
                        "Diploma/ Certificate of units earned\n" +
                        "Updated Physician PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/Appointment (notarized)\n" +
                        "Service Record/Certificate of Employment (proof of Hospital supervisory/ managerial experience","1","27","27"},
                {"Chief of Clinics/ Chief of Medical Professional Services","Licensed physician\n" +
                        "Fellow/diplomate of a specialty/subspecialty society\n" +
                        "At least five (5) years hospital experience in a clinical supervisory or managerial position","DOCUMENT REVIEW\n" +
                        "Diploma/ Certificate from Specialty society\n" +
                        "Updated Physician PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/Appointment (notarized)\n" +
                        "Service Record/Certificate of Employment (proof of clinical supervisory/managerial experience in hospital)","1","28","28"},
                {"Department Head (Specialty)","Licensed physician\n" +
                        "Fellow/diplomate in a specialty/Sub specialty society of the department he/she heads","DOCUMENT REVIEW\n" +
                        "Diploma/ Certificate from Specialty society\n" +
                        "Updated Physician PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/Appointment (notarized)","1 per department","29","29"},
                {"Chief Nurse/Director of Nursing","Licensed nurse\n" +
                        "Master’s Degree in Nursing AND at least five (5) years of clinical experience in a Supervisory or managerial position in nursing (R.A. No. 9173)","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment/Appointment (notarized)\n" +
                        "Service Record/Certificate of Employment (proof of supervisory/managerial experience in nursing)","","30","30"},
                {"Chief Administrative Officer/Hospital Administrator","Have completed at least twenty (20) Units towards Master’s Degree in Hospital Administration or related course (MPH, MBA, MPA, MHSA, etc.) AND at least five (5) years hospital experience in a supervisory/ managerial position","","1","31","31"},
                {"ADMINISTRATIVE SERVICES"},
                {"Accountant","Certified Public Accountant (may be outsourced)","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate of units earned\n" +
                        "Updated PRC license (if applicable)\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","1","32","32"},
                {"Billing Officer","With Bachelor’s Degree relevant to the job","1","33","33"},
                {"Book keeper","1","34","34"},
                {"Budget/Finance Officer","1","35","35"},
                {"Cashier","1","36","36"},
                {"Human Resources Management Officer / Personnel Officer","1","37","37"},
                {"Engineer (full time)","Licensed Engineer","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Proof of Employment / Appointment (notarized)","1","38","38"},
                {"Supply Officer/-Storekeeper","With appropriate training and experience","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","1","39","39"},
                {"Laundry Worker","1","40","40"},
                {"Medical Records officer","Bachelor’s Degree\n" +
                        "Training in ICD 10\n" +
                        "Training in Medical Records Management","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","1","41","41"},
                {"Medical Social worker (Full Time)","Licensed social worker","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate of units earned\n" +
                        "Updated PRC license Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","1","42","42"},
                {"Nutritionist –Dietician (Full Time)","Licensed Nutritionist-Dietician","","1","43","43"},
                {"Building Maintenance Man/Utility Worker","May be outsourced\n" +
                        "Security guard must be licensed.","DOCUMENT REVIEW\n" +
                        "Relevant Training\n" +
                        "Licensed if applicable\n" +
                        "Proof of Employment / Appointment (notarized) if employed by hospital\n" +
                        "Notarized MOA if outsourced","1 per shift","",""},
                {"Security Guard (licensed)","1 per shift","44","44"},
                {"CLINICAL SERVICES"},
                {"Consultant Staff in Ob-Gyn. Pediatrics, Medicine. Surgery, and Anesthesia \n" +
                        "\n" +
                        "*Hospital may have additional consultants from other specialties.","Licensed physician\n" +
                        "Fellow/Diplomate\n" +
                        "ACLS certified (for Surgeons and Anesthesiologists)","DOCUMENT REVIEW\n" +
                        "Certificate from Specialty society, if applicable (for Board Certified)\n" +
                        "Residency Training Certificate (for Board Eligible)\n" +
                        "Certificate of Residency Training/ Medical Specialists (*DOH Medical specialist, last exam was in 1989)\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","At least 50% of the consultants per specialty are board certified","45","45"},
                {"Intensive Care Unit: Multidisciplinary Team composed of, but not limited to, board certified Cardiologist, Pulmonologist, Neurologist. Pulmonologist Preferably OR an intensivist","Licensed physician\n" +
                        "Fellow/Diplomate","A team composed of at least 1 per specialty (May be part time or visiting consultant) OR a intensivist","46","46"},
                {"Neonatal Intensive Care Unit: A multidisciplinary team compose of, but not limited to, pediatric, cardiologist, pediatric nephrologist, pediatric Pulmonologist OR a neonatologist","Licensed physician\n" +
                        "Fellow/Diplomate","A team composed of at least 1 per specialty (May be part time or visiting consultant) OR a neonatologist","47","47"},
                {"High Risk Pregnancy Unit: General Obstetricians, preferably with a Perinatologist, and a referral team of IM specialists","Licensed physician\n" +
                        "Fellow/Diplomate","DOCUMENT REVIEW\n" +
                        "Certificate from Specialty society, if applicable (for Board Certified)\n" +
                        "Residency Training Certificate (for Board Eligible)\n" +
                        "Certificate of Residency Training/ Medical Specialists (*DOH Medical Specialist, last exam was in 1989)\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)","General Obstetricians, preferably with a Perinatologist, and a referral team of IM specialists (May be part time or visiting consultant)","48","48"},
                {"Resident Physician on duty (Shall not go on duty for more than 48 hours straight).","Licensed physician","DOCUMENT REVIEW\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)\n" +
                        "Schedule of duty approved by Medical Director/Chief of Hospital","Wards- 1:20 beds at any given time PLUS ER – at least 1 at any given time\n" +
                        "\n" +
                        "*This ratio does not include Resident Physicians on Duty that shall be required for add-on services such as dialysis facility. It shall be counted separately.","49","49"},
                {"NURSING SERVICES"},
                {"Assistant Chief Nurse","Licensed nurse\n" +
                        "At least twenty (20) units towards Master’s Degree in\\ Nursing\n" +
                        "At least three (3) years-experience in supervisory/ managerial position in nursing","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate of Units Earned\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)\n" +
                        "Service Record/Certificate of Employment (proof of supervisory/ managerial experience in nursing)","1:100 Beds","50","50"},
                {"Supervising Nurse/Nurse Managers","Licensed nurse\n" +
                        "With at least nine (9) units of Master’s Degree in Nursing\n" +
                        "At least two (2) years experience in general nursing service administration","DOCUMENT REVIEW\n" +
                        "Diploma/Certificate of Units Earned\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment / Appointment (notarized)\n" +
                        "Service Record/Certificate of Employment (Proof of General nursing service Administration experience)","1 per Department-Office hours only (8am-5pm)","51","51"},

                {"Head Nurse/Senior Nurse\t\n","Licensed nurse\n" +
                        "With at least two (2) years-hospital experience\n" +
                        "BLS certified","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of trainings attended\n" +
                        "Proof of employment (notarized)\n" +
                        "If nursing staffing is outsourced: Validity of the contract of of employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                        "Schedule of duty approved by Chief Nurse","1 per shift per clinical department","52","52"},
                {"Staff Nurse","Licensed nurse\n" +
                        "BLS certified","Wards – 1:12 beds at any time (1 reliever for every 3 RNs)","53","53"},
                {"Staff Nurse in every Critical Unit (CCU, ICU, NICU, PICU, SICU. HRPU, etc.)","Licensed nurse\n" +
                        "Certificate of Training in Critical Care Nursing, ACLS","Licensed nurse\n" +
                        "Certificate of Training in Critical Care Nursing, ACLS","54","54"},
                {"Nursing Attendants in wards","highschool graduate\n" +
                        "With relevant health related training (maybe in house training)","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment (notarized)","DOCUMENT REVIEW\n" +
                        "Certificates of Trainings attended\n" +
                        "Proof of Employment (notarized)","",""},
                {"Nursing attendant in CCU","1:12 beds at any time (plus 1 reliever for every 3 NA/MWs)","55","55"},
                {"Operating Room Nurses: \n" +
                        "-scrub Nurse (SN) \n" +
                        "-Circulating Nurse (CN)","Licensed nurse\n" +
                        "Training in OR Nursing\n" +
                        "Training in BLS and ACLS","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of trainings attended\n" +
                        "Proof of Employment / Appointment (notarized\n" +
                        "If outsourced: Validity of the contract of employment should be at least one (1) year and within the validity period of the hospital’s LTO.","1 SN and 1 CN per functioning OR per shift (plus 1 reliever for every 3 nurses)","56","56"},
                {"Delivery room Nurse","Licensed nurse\n" +
                        "Training in Maternal and Child Nursing (may be in house training or","1 per delivery table per shift (plus 1 reliever for every 3","57","57"},
                {"","training in Essential Integrated Newborn Care [EINC ])\n" +
                        "Training in BLS and ACLS","Employment should be at least one (1) year and within the validity period of the hospital’s LTO.\n" +
                        "Schedule of duty Approved by Chief Nurse","nurses)","",""},
                {"Emergency Room Nurse","Licensed nurse\n" +
                        "Training in Trauma Nursing, ACLS and other relevant training","1:3 beds per shift (plus 1 reliever for every 3 nurses)","58","58"},
                {"Outpatient Department Nurse","Licensed nurse\n" +
                        "Training in BLS","1 per Department-Office hours only (8am-5pm)","59","59"},
                {"Dentist – MOA if outsourced but the dental service should be within the vicinity of hospital","Licensed dentist","DOCUMENT REVIEW\n" +
                        "Diploma\n" +
                        "Updated PRC license\n" +
                        "Certificates of Trainings attended\n" +
                        "Employment should be at least one (1) year and within the validity period of the hospital’s LTO.","1 Office hours only (8am-5pm)","60","60"},
                {"Respiratory Therapist","Licensed respiratory therapist or licensed nurse with respiratory therapy training","1 per shift","61","61"},
                {"DOCUMENTS","COMPLIED","REMARKS"},
                {"1.\tDOH – Approved PTC","",""},
                {"2.\tDOH Approved Floor Plan","",""},
                {"3.\tChecklist for Review of Floor Plans (accomplished)","",""},
                {" "},
                {"Ambulance\n" +
                        "Available 24/7\n" +
                        "Physically present if not being used during time of inspection/monitoring","1","Parking","",""},
                {"Computer with Internet Access","1","Administrative Office","",""},
                {"Emergency Light","","Lobby, hallway, nurses’ station, office/unit and stairways","",""},
                {"Fire Extinguishers","1 per unit or area","Lobby, hallway, nurses’ station, office/unit and stairways","",""},
                {"Generator set with automatic Transfer Switch (ATS)","1","Genset house","",""},
                {"Exhaust fan","1","Kitchen","",""},
                {"Food Conveyor or equivalent (closed-type)","1","",""},
                {"Food Scale","1","",""},
                {"Blender/Osteorizer","","",""},
                {"Oven","1","",""},
                {"Stove","1","",""},
                {"Refrigerator/Freezer","1","",""},
                {"Utility cart","1","",""},
                {"Garbage Receptacle with Cover (color-coded)","1 for each color","",""},
                {"Bag-valve-mask Unit \n" +
                        "- Adult\n" +
                        "- Pediatric","1\n" +
                        "1","ER","",""},
                {"Calculator for dose computation","1","",""},
                {"Clinical Weighing scale","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"Delivery set, primigravid","2 sets","",""},
                {"Delivery set, multigravid","2 sets","",""},
                {"ECG Machine with leads","1","",""},
                {"EENT Diagnostic set with Ophthalmoscope and Otoscope\t","1set","",""},
                {"Emergency Cart (for contents, refer to separate list).","1","",""},
                {"Examining table","1","",""},
                {"Examining table (with Stirrups for OB-Gyne","1","",""},
                {"Glucometer with strips","1","",""},
                {"Gooseneck lamp/Examining Light","1","",""},
                {"Instrument/Mayo Table","1","",""},
                {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thoracostomy, Cutdown, etc.)","2 sets","",""},
                {"Nebulizer","1","",""},
                {"Negatoscope","1set","",""},
                {"Neurologic Hammer","1","",""},
                {"OR Light (portable or equivalent)","1","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not from pipeline","2","",""},
                {"Pulse Oximeter","1","",""},
                {"Sphygmomanometer, Non-mercurial \n" +
                        "- Adult Cuff \n" +
                        "- Pediatric Cuff","1\n" +
                        "1\n" +
                        "1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Suturing Set","2 sets","",""},
                {"Thermometer, non –mercurial \n" +
                        "- Oral \n" +
                        "- Rectal","1\n" +
                        "1","",""},
                {"Vaginal Speculum, Different Sizes","1 for each different size","",""},
                {"Wheelchair","1","",""},
                {"Wheeled Stretcher with guard/side rails and wheel lock or anchor.","1",""},
                {"Clinical Height and Weight Scale","1","OPD","",""},
                {"EENT Diagnostic set with ophthalmoscope and otoscope","1 set","",""},
                {"Gooseneck lamp/Examining Light","1","",""},
                {"Examining table with wheel lock or anchor","1","",""},
                {"Instrument/Mayo Table","1","",""},
                {"Minor Instrument Set 1 set","1 set","",""},
                {"Neurologic Hammer","1","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Peak flow meter - Adult - Pediatric","1\n" +
                        "1","",""},
                {"Sphygmomanometer, N0n-mercurial - Adult - Pediatric","1\n" +
                        "1","",""},
                {"Thermometer, non-mercurial\n" +
                        "- Oral\n" +
                        "- Rectal","1\n" +
                        "1","",""},
                {"Suture Removal Set","1 set","",""},
                {"Wheelchair / Wheeled Stretcher","1","",""},
                {"Anesthesia Machine","1","OR","",""},
                {"Air conditioning Unit","1","",""},
                {"Anesthesia Machine","1","",""},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"Caesarean Section Instrument","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"Electrocautery machine","1","",""},
                {"Emergency Cart (for contents, refer to separate list)","1","",""},
                {"Glucometer with strips","","",""},
                {"Instrument / Mayo table","1","",""},
                {"Laparotomy pack (Linen pack)","1 per OR","",""},
                {"Laparatomy / Major Instrument Set","1 per OR","",""},
                {"Laryngoscopes with different sizes of blades","1","",""},
                {"Operating room light","1 per OR","",""},
                {"Operating room table","1 per OR","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1 per OR","",""},
                {"Rechargeable Emergency Light (in case generator malfunction)","1 per OR","",""},
                {"Sphygmomanometer, N0n-mercurial \n" +
                        "- Adult cuff \n" +
                        "- Pediatric cuff","1 per OR\n" +
                        "1 per OR","",""},
                {"Spinal Set","1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, non-mercurial \n" +
                        "- Oral \n" +
                        "- Rectal","1\n" +
                        "1","",""},
                {"Wheeled Stretcher with guard/side rails and wheel lock or anchor.","1","",""},
                {"Air conditioning Unit","1","PACU/RR","",""},
                {"Cardiac Monitor","1","",""},
                {"Defibrillator with paddles","1 (if separate from the OR Complex)","",""},
                {"Emergency Cart (for contents, refer to separate list)","1 (if separate from the OR Complex)","",""},
                {"Mechanical / patient bed, with guard side rails and wheel lock or anchored","1","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Sphygmomanometer, N0n-mercurial \n" +
                        "- Adult cuff \n" +
                        "- Pediatric cuff","1\n" +
                        "1","",""},
                {"Stethoscope","1","","",""},
                {"Thermometer, non- mercurial","1","",""},
                {"Fetal Doppler","1","Labor Room","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Patient Bed","1","",""},
                {"Pulse Oximeter","1","",""},
                {"Sphygmomanometer, N0n-mercurial","1","",""},
                {"Stethoscope","1","",""},
                {"Thermometer, Non- mercurial","1","",""},
                {"Air conditioning Unit","1","DR","",""},
                {"Bag valve mask unit (Adult and pediatric)","1","",""},
                {"Bassinet","1","",""},
                {"Clinical Infant Weighing scale","1","",""},
                {"Defibrillator with paddles","Defibrillator with paddles 1 (if DR is separate from OR Complex)","",""},
                {"Delivery set, primigravid","1 set","",""},
                {"Delivery set, multigravid","2 sets","",""},
                {"Delivery room light","1","",""},
                {"Delivery room table","1","",""},
                {"Dilatation and Curettage set","1 set","",""},
                {"Emergency Cart (for contents, refer to separate list)","1 (if DR is separate from OR Complex)","",""},
                {"Instrument/Mayo Table","1","",""},
                {"Kelly Pad or equivalent","1","",""},
                {"Laryngoscope with different sizes of blades","1","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Rechargeable Emergency Light (in case generator malfunction)","1","",""},
                {"Sphygmomanometer, N0n-mercurial","1","",""},
                {"Stethoscope","1","",""},
                {"Suction Apparatus","1","",""},
                {"Wheeled Stretcher","1","","",""},
                {"HIGH RISK PREGNANCY UNIT"},
                {"Cardiac Monitor with Pulse Oximeter","1","HRPU","",""},
                {"Cardiotocography (CTG) Machine","1","",""},
                {"Fetal Doppler","1","",""},
                {"Oxygen Unit \n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Patient bed with side rails","1","",""},
                {"Suction apparatus","1","",""},
                {"Sphygmomanometer – Non-mercurial","1","",""},
                {"NEONATAL INTENSIVE CARE UNIT (NICU)"},
                {"Air conditioning Unit","1","NICU","",""},
                {"Bassinet","1","",""},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"Bassinet","1","",""},
                {"Clinical Infant Bag-valve mask unit","1","",""},
                {"Clinical Infant weighing scale","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"EENT Diagnostic Set with ophthalmoscope and otoscope","1 set","",""},
                {"Emergency Cart (for contents, refer to separate list)","1","",""},
                {"Glucometer","1","",""},
                {"Incubator","1","",""},
                {"Infusion pump/ Syringe pump","1","",""},
                {"Laryngoscope with neonatal blades of different sizes","1","",""},
                {"Mechanical Ventilator (May be outsourced)","1","",""},
                {"Neonatal Stethoscope","1","",""},
                {"Oxygen Unit\n" +
                        "Tank is anchored/chained/ strapped or with tank holder if not pipeline","1","",""},
                {"Refrigerator for Breast milk storage","1","",""},
                {"Sphygmomanometer, N0n-mercurial\n" +
                        "- for neonate","1","",""},
                {"Suction apparatus","1","",""},
                {"Thermometer, non-mercurialt","1","",""},
                {"Umbilical Cannulation set","1","",""},
                {"INTENSIVE CARE UNIT (ICU) – For all types of ICU (PICU, SICU, Medical ICU, etc.)"},
                {"Air conditioning Unit","1","ICU","",""},
                {"Bag-valve-mask Unit\n" +
                        "- Adult\n" +
                        "- Pediatric","1\n" +
                        "1","",""},
                {"Cardiac Monitor with Pulse Oximeter","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"EENT Diagnostic Set with ophthalmoscope and otoscope","1 set","",""},
                {"Infusion pump","1","",""},
                {"Laryngoscope with different sizes of blades","1 set","",""},
                {"Mechanical Bed","Depending on the number of beds declared","",""},
                {"Mechanical Ventilator/ Respirator (May be outsourced)\t","1","",""},
                {"Minor Instrument Set (May be used for Tracheostomy, Closed Tube Thoracostomy, Cutdown, etc.)","1 set","",""},
                {"Emergency_Cart(for contents, refer to separate list)\t","1","",""},
                {"Oxygen Unit tank is anchored/chained/strapped or with tank holder if not pipeline","1","",""},
                {"Sphygmomanometer, non-mercurial(reserved for sudden breakdown of cardiac monitor)\n" +
                        "- Adult cuff for adult unit\n" +
                        "- Pediatric cuff for pediatric unit","1\n" +
                        "1","",""},
                {"Stethoscope","1","",""},
                {"Suction apparatus","1","",""},
                {"NURSING UNIT/WARD"},
                {"Bag-valve-mask Unit\n" +
                        "- Adult\n" +
                        "- Pediatric","NURSING UNIT/WARDs","1\n" +
                        "1","",""},
                {"Clinical Height and Weight Scale","1","",""},
                {"Defibrillator with paddles","1","",""},
                {"Emergency cart or equivalent (refer to separate list for the contents)","1","",""},
                {"EENT Diagnostic Set with ophthalmoscope and otoscope","1 set","",""},
                {"Laryngoscope with different sizes of blades","1 set","",""},
                {"Mechanical/Patient bed with lock, if wheeled, with guard or side rails","ABC","",""},
                {"Bedside Table","ABC","",""},
                {"Neurologic Hammer","1","",""},
                {"Oxygen Unit Tank is anchored/chained if not pipeline","1","",""},
                {"Sphygmomanometer, Non-Mercurial \n" +
                        "- Adult cuff\n" +
                        "- Pediatric cuff","1\n" +
                        "1","",""},
                {"Stethoscope","1\n" +
                        "1","",""},
                {"Suction Apparatus","1","",""},
                {"Thermometer, non-mercurial \n" +
                        "- Oral \n" +
                        "- Rectal","1\n" +
                        "1","",""},
                {"RESPIRATORY/PULMONARY UNIT"},
                {"ABG Machine","1","Respiratory/ Pulmonary Unit","",""},
                {"Pulmonary Function Test (PFT) or Peak Expiratory Flow Rate (PEFR) Tube","1","",""},
                {"Spirometer","1","",""},
                {"Nebulizer","1","",""},
                {"DENTAL CLINIC"},
                {"Air compressor","1","DENTAL CLINIC","",""},
                {"Autoclave","1","",""},
                {"Bone file, stainless","1","",""},
                {"Cotton pliers","1","",""},
                {"Cowhorn Forceps","1","",""},
                {"Dental Chair Unit","1","",""},
                {"Explorer, double-end","1","",""},
                {"Forceps, No. 8","1","",""},
                {"Forceps, No.17 Upper molar","1","",""},
                {"Forceps, No. 18 Upper molar","1","",""},
                {"Forceps, No. 150 Maxillary Universal","1","",""},
                {"Forceps, No. 150 S Primary Teeth","1","",""},
                {"Forceps, No. 151 Lower Universal","1","",""},
                {"Forceps, No. 151 Mandibular Pre-molar","1","",""},
                {"Forceps, No. 151 S Lower Primary Teeth","1","",""},
                {"Gum separator","1","",""},
                {"High speed handpiece with Burr remover","1","",""},
                {"Low speed handpiece, Angeled head","1","",""},
                {"Mouth mirror explorer","1","",""},
                {"Periosteal elevator No. 9, double-end","1","",""},
                {"Rongeur","1","",""},
                {"Root elevator","1","",""},
                {"Scaler Jacquettes Set No. 1,2, and 3","1","",""},
                {"Surgical Chisel","1","",""},
                {"Surgical Malette","1","",""},
                {"Autoclave/Steam Sterilizer","1","CSSR","",""},
                {"Bed or stretcher for cadaver","1","CADAVER HOLDING AREA","",""},
                {"EMERGENCY CART CONTENTS","ER","OR","DR","ICU","NICU\t","HRPU","NS 1","NS 2","NS 3","NS 4","NS 5","NS 6","NS 7","OTHERS","OTHERS","REMARKS"},
                {"Adenosine 6 mg/2mL vial","","","","","","","","","","","","","","","",""},
                {"Amiodarone 150mg/3mL ampule","","","","","","","","","","","","","","","",""},
                {"Anti-tetanus serum (either equine-based antiserum or human antiserum)","","","","","","","","","","","","","","","",""},
                {"Aspirin USP grade (325 mg/tablet)","","","","","","","","","","","","","","","",""},
                {"Atropine 1 mg/ml ampule","","","","","","","","","","","","","","","",""},
                {"B-adrenergic agonists (i.e. Salbutamol 2mg/ml)\t","","","","","","","","","","","","","","","",""},
                {"Benzodiazepine (Diazepam 10mg/2ml ampule and/or Midazolam) (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Calcium (usually calcium gluconate 10% solution in 10 mL ampule)","","","","","","","","","","","","","","","",""},
                {"Clopidogrel 75 mg tablet","","","","","","","","","","","","","","","",""},
                {"D5W 250 mL\t","","","","","","","","","","","","","","","",""},
                {"D50W 50mg/vial\t","","","","","","","","","","","","","","","",""},
                {"Digoxin 0.5mg/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Diphenhydramine 50mg/mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Dobutamine 250mg/5mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Dopamine 200mg/5mL ampule/vial","","","","","","","","","","","","","","","",""},
                {"Epinephrine 1mg/ml ampule","","","","","","","","","","","","","","","",""},
                {"Furosemide 20mg/2ml ampule","","","","","","","","","","","","","","","",""},
                {"Haloperidol 50mg/mL ampule","","","","","","","","","","","","","","","",""},
                {"Hydrocortisone 250mg/2mL vial\t","","","","","","","","","","","","","","","",""},
                {"Lidocaine 10% in 50mL spray","","","","","","","","","","","","","","","",""},
                {"Lidocaine 2% solution vial 1g/50ml\t","","","","","","","","","","","","","","","",""},
                {"Magnesium sulphate 1g/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Mannitol 20% solution in 500ml/bottle","","","","","","","","","","","","","","","",""},
                {"Methylprednisolone 4mg/tablet","","","","","","","","","","","","","","","",""},
                {"Metoclopramide 10mg/2mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Morphine sulphate 10mg/mL ampule (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Nitroglycerin inj. 10mg/10mL ampule or Isosorbide dinitrate 5mg SL tablet or 10 mg/10mL ampule","","","","","","","","","","","","","","","",""},
                {"Noradrenaline 2mg/2mL ampule","","","","","","","","","","","","","","","",""},
                {"Paracetamol 300mg/ampule (IV preparation)","","","","","","","","","","","","","","","",""},
                {"Phenobarbital 120mg/ml ampule IV or 30mg tablet (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Phenytoin 100mg/capsule or 100 mg.2mL ampule\t","","","","","","","","","","","","","","","",""},
                {"Plain NSS 1L/bottle-0.9% Sodium Chloride","","","","","","","","","","","","","","","",""},
                {"Potassium Chloride 40mEq/20mL vial (in high alert box)","","","","","","","","","","","","","","","",""},
                {"Vitamin B1/6/12 vial (1g B1, 1g B6, 0.01gB12 in 10 mL vial)","","","","","","","","","","","","","","","",""},
                {"Sodium bicarbonate 50mEq/50mL ampule","","","","","","","","","","","","","","","",""},
                {"Verapamil 5 mg/2ml ampule","","","","","","","","","","","","","","","",""},
                {"Airway adjuncts","","","","","","","","","","","","","","","",""},
                {"Airway / Intubation Kit (with stylet and bag valve masks)","","","","","","","","","","","","","","","",""},
                {"Alcohol disinfectant\t","","","","","","","","","","","","","","","",""},
                {"Aseptic bulb syringe","","","","","","","","","","","","","","","",""},
                {"Calculator","","","","","","","","","","","","","","","",""},
                {"Capillary Blood Glucose (CBG) Kit\t","","","","","","","","","","","","","","","",""},
                {"Cardiac Board","","","","","","","","","","","","","","","",""},
                {"Endotracheal Tubes, all sizes","","","","","","","","","","","","","","","",""},
                {"Flashlights or Pen lights","","","","","","","","","","","","","","","",""},
                {"Gloves, sterile\t","","","","","","","","","","","","","","","",""},
                {"Gloves,non-sterile\t","","","","","","","","","","","","","","","",""},
                {"Laryngoscope with different sizes of blades","","","","","","","","","","","","","","","",""},
                {"Nasal cannula\t","","","","","","","","","","","","","","","",""},
                {"Protective face shield or mask or goggles","","","","","","","","","","","","","","","",""},
                {"Standard face mask\t","","","","","","","","","","","","","","","",""},
                {"Sterile gauze (pre-folded and individually packed)\t","","","","","","","","","","","","","","","",""},
                {"Syringes (different volumes)","","","","","","","","","","","","","","","",""},
                {"Urethral catheter\t","","","","","","","","","","","","","","","",""},
                {"Urine collection bag\t","","","","","","","","","","","","","","","",""},
                {"Waterproof aprons\t","","","","","","","","","","","","","","","",""}
        };

        for(int i = 0; i < desc8.length; i++) {
            for(int j = 0; j< desc8[i].length; j++) {
                String d = desc8[i][j];
                if(j==4){
                    d="√";
                }
                Phrase par = new Phrase(d,normal);
                if(i==0){
                    PdfPCell cells = celldesc;
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(148,138,84));
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPhrase(par);
                    table12.addCell(cells);
                }else if(i==1){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table13.addCell(cells);
                }else if(i>1 && i<7){
                    if(j==3){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table14.addCell(imgcell);
                        }else{
                            table14.addCell(imgcell2);
                        }
                    }else if(j==4){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        table14.addCell(remarks);
                        equipment++;
                    }else{
                        PdfPCell cell4 = new PdfPCell();
                        cell4.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell4.setPaddingLeft(5);
                        cell4.setPaddingBottom(10);
                        cell4.setPaddingTop(10);
                        cell4.setPhrase(new Phrase(par));
                        table14.addCell(cell4);
                    }

                }else if(i==7){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table15.addCell(cells);
                }else if(i>7 && i<17){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==8 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(9);
                        table16.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        //table16.addCell(imgcell);
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table16.addCell(imgcell);
                        }else{
                            table16.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        //table16.addCell(imgcell);
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table16.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table16.addCell(cells);
                    }

                }else if(i==17){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table17.addCell(cells);
                }else if(i>17 && i<=45){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==18 && j==2){

                        cells.setPhrase(par);
                        cell.setVerticalAlignment(Element.ALIGN_CENTER);
                        cells.setRowspan(45);
                        table18.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        //table18.addCell(imgcell);
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table18.addCell(imgcell);
                        }else{
                            table18.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table18.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table18.addCell(cells);
                    }

                }else if(i==46){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table19.addCell(cells);
                }else if(i>46 && i<61){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==47 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table20.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){

                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table20.addCell(imgcell);
                        }else{
                            table20.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table20.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table20.addCell(cells);
                    }

                }else if(i==61){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table21.addCell(cells);
                }else if(i>61 && i<83){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==62 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table22.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table22.addCell(imgcell);
                        }else{
                            table22.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table22.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table22.addCell(cells);
                    }

                }else if(i==84){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table23.addCell(cells);
                }else if(i>83 && i<96){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==85 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table24.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table24.addCell(imgcell);
                        }else{
                            table24.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table24.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table24.addCell(cells);
                    }

                }else if(i==96){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table25.addCell(cells);
                }else if(i>96 && i<104){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==97 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table26.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table26.addCell(imgcell);
                        }else{
                            table26.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table26.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table26.addCell(cells);
                    }

                }else if(i==104){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table27.addCell(cells);
                }else if(i>104 && i<125){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==105 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table28.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table28.addCell(imgcell);
                        }else{
                            table28.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table28.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table28.addCell(cells);
                    }

                }else if(i==125){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table29.addCell(cells);
                }else if(i>125 && i<141){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==126 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(60);
                        table30.addCell(cells);
                    }else if((desc8[i].length == 5 && j==3) || (desc8[i].length == 4 && j==2)){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        ///c=hange

                        if(i!=128){
                            if(res.equals("true")){
                                table30.addCell(imgcell);
                            }else{
                                table30.addCell(imgcell2);
                            }
                        }else{
                            table30.addCell(new Phrase(""));
                        }

                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        if(i!=128){
                            String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                            cells.setPhrase(new Phrase(remarks));
                            table30.addCell(cells);
                            equipment++;
                        }else{
                            table30.addCell(par);
                        }

                    }else{
                        cells.setPhrase(par);
                        table30.addCell(cells);
                    }

                }else if(i==141){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table31.addCell(cells);
                }else if(i==142){

                    if(j==3){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        ///c=hange
                        if(res.equals("true")){
                            table32.addCell(imgcell);
                        }else{
                            table32.addCell(imgcell2);
                        }
                    }else if(j==4){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        table32.addCell(new Phrase(remarks));
                        equipment++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingLeft(5);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table32.addCell(cells);
                    }

                }else if(i==143){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table33.addCell(cells);
                }else if(i==144){
                    if(j==3){
                        String res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        ///c=hange
                        if(res.equals("true")){
                            table34.addCell(imgcell);
                        }else{
                            table34.addCell(imgcell2);
                        }
                    }else if(j==4){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        table34.addCell(new Phrase(remarks));
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingLeft(5);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table34.addCell(cells);
                    }
                }
                else if(i==145){
                    PdfPCell cells = new PdfPCell();
                    cells.setPhrase(par);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(214,227,188));
                    table35.addCell(cells);
                }else if(i>145 && i<181){
                    if(j!=0 && j!=16){
                        //table35.addCell("35 index "+i);
                        colindex=0;
                        String res = get_col_results1("OTHERS",others+"",HomeActivity.appid,colindex);
                        if(res.equals("true")){
                            table35.addCell(imgcell1);
                        }else{
                            table35.addCell(imgcell2);
                        }
                        colindex++;
                    }else if(j==16){
                        String remarks = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
                        table35.addCell(new Phrase(remarks));
                        others++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table35.addCell(cells);
                    }

                }else if(i==181){
                    PdfPCell cells = new PdfPCell();
                    cells.setPhrase(par);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(214,227,188));
                    table36.addCell(cells);
                }else if(i>181 && i<202){
                    /*182 -189*/
                    colindex=0;
                    if(j!=0 && j!=16){
                        if(i>181 && i<190){
                            Log.d("hho41",hh04+"");
                            String res = get_col_results1("HH004",hh04+"",HomeActivity.appid,colindex);
                            Log.d("hh004res",res);
                            if(res.equals("true")){
                                table37.addCell(imgcell1);
                            }else{
                                table37.addCell(imgcell3);
                            }
                            colindex++;
                        }else{
                            String res = get_col_results1("OTHERS",others+"",HomeActivity.appid,colindex);
                            if(res.equals("true")){
                                table37.addCell(imgcell1);
                            }else{
                                table37.addCell(imgcell2);
                            }
                            colindex++;

                        }

                    }else if(j==16){
                        if(i>181 && i<190){
                            String remarks = get_remarks_results("HH004",hh04+"",HomeActivity.appid);
                            table37.addCell(new Phrase(remarks));
                            hh04++;
                        }else{
                            String remarks = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
                            table37.addCell(new Phrase(remarks));
                            others++;
                        }


                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table37.addCell(cells);
                    }

                }else if(i==202){

                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table38.addCell(cells);
                }else if(i==203){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table39.addCell(cells);
                }else if(i>203 && i<207){
                    if(j==4){
                        if(i==206){
                            String res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                            if(res.equals("true")){
                                table39.addCell(imgcell);
                            }else{
                                table39.addCell(imgcell2);
                            }
                        }else{
                            String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                            if(res.equals("true")){
                                table39.addCell(imgcell);
                            }else{
                                table39.addCell(imgcell2);
                            }
                        }


                    }else if(j==5){
                        if(i==206){
                            String res = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                            PdfPCell cells = new PdfPCell();
                            cells.setPhrase(new Phrase(res));
                            cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells.setPaddingBottom(10);
                            cells.setPaddingTop(10);
                            table39.addCell(cells);
                            personnel++;
                        }else{
                            String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                            PdfPCell cells = new PdfPCell();
                            cells.setPhrase(new Phrase(res));
                            cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells.setPaddingBottom(10);
                            cells.setPaddingTop(10);
                            table39.addCell(cells);
                            hh05++;
                        }

                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table39.addCell(cells);
                    }

                }else if(i==207){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table40.addCell(cells);
                }else if(i==208){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table41.addCell(cells);

                }else if(i>208 && i<225){
                    if(j==2){
                        String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table41.addCell(imgcell);
                        }else{
                            table41.addCell(imgcell2);
                        }
                    }else if(j==3){
                        String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(res));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table41.addCell(cells);
                        hh05++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table41.addCell(cells);
                    }

                }else if(i==225){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table42.addCell(cells);
                }else if(i==226){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table43.addCell(cells);
                }else if(i>227 && i<230){
                    if(j==4){
                        String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table43.addCell(imgcell);
                        }else{
                            table43.addCell(imgcell2);
                        }
                    }else if(j==5){
                        String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(res));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table43.addCell(cells);
                        hh05++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table43.addCell(cells);
                    }

                }else if(i==230){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table44.addCell(cells);
                }else if(i==231){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table45.addCell(cells);
                }else if(i>231 && i<253){
                    if(j==2){
                        String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table45.addCell(imgcell);
                        }else{
                            table45.addCell(imgcell2);
                        }
                    }else if(j==3){
                        String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(res));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table45.addCell(cells);
                        hh05++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table45.addCell(cells);
                    }
                }else if(i==253){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table46.addCell(cells);
                }else if(i==254){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cells.setBackgroundColor(new BaseColor(196,188,150));
                    table47.addCell(cells);
                }else if(i>254 && i<258){
                    if(j==4){
                        String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table47.addCell(imgcell);
                        }else{
                            table47.addCell(imgcell2);
                        }
                    }else if(j==5){
                        String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(res));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table47.addCell(cells);
                        hh05++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table47.addCell(cells);
                    }

                }else if(i==258){

                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table48.addCell(cells);
                }else if(i==259){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table49.addCell(cells);
                }else if(i>259 && i<267){
                    if(j==2){
                        String res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        if(res.equals("true")){
                            table49.addCell(imgcell);
                        }else{
                            table49.addCell(imgcell2);
                        }
                    }else if(j==3){
                        String res = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(res));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table49.addCell(cells);
                        hh05++;
                    }else {
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table49.addCell(cells);
                    }

                }else if(i==267){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table50.addCell(cells);
                }else if(i>267 && i<273){
                    if(j==4){
                        String res = "";
                        if(part1){

                            res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                            if(res.equals("true")){
                                table51.addCell(imgcell);
                            }else{
                                table51.addCell(imgcell2);
                            }
                    }else if(j==5){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(new Phrase(remarks));
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table51.addCell(cells);
                        personnel++;
                        personnel2++;

                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setPhrase(par);
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        table51.addCell(cells);
                    }

                }else if(i==273){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table52.addCell(cells);
                }else if(i>273 && i<288){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==274 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(6);
                        table53.addCell(cells);
                    }else if(i==275 && j==1){
                        cells.setPhrase(par);
                        cells.setRowspan(5);
                        table53.addCell(cells);
                    }else if(i==281 && (j==1 || j==2)){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table53.addCell(cells);
                    }else if(i==286 && (j==1 || j==2)){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table53.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        if(i!=286){

                            String res = "";
                            if(part1){
                                res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                            }
                            if(res.equals("true")){
                                table53.addCell(imgcell);
                            }else{
                                table53.addCell(imgcell2);
                            }
                        }else{
                            //HPP002
                            //hpp022
                            String res = "";
                            if(part1){
                                res = get_col_results("HPP002",hpp02+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("HPP002",hpp022+"",HomeActivity.appid);
                            }
                            if(res.equals("true")){
                                table53.addCell(imgcell);
                            }else{
                                table53.addCell(imgcell2);
                            }
                        }

                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        if(i!=286){
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                            }
                            PdfPCell cells1 = new PdfPCell();
                            cells1.setPhrase(new Phrase(remarks));
                            cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells1.setPaddingBottom(10);
                            cells1.setPaddingTop(10);
                            table53.addCell(cells);
                            personnel++;
                            personnel2++;
                        }else{
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("HPP002",hpp02+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("HPP002",hpp022+"",HomeActivity.appid);
                            }
                            PdfPCell cells1 = new PdfPCell();
                            cells1.setPhrase(new Phrase(remarks));
                            cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells1.setPaddingBottom(10);
                            cells1.setPaddingTop(10);
                            table53.addCell(cells);
                            hpp02++;
                            hpp022++;
                        }

                    }else{
                        cells.setPhrase(par);
                        table53.addCell(cells);

                    }
                }else if(i==288){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table54.addCell(cells);
                }else if(i>288 && i< 294){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==289 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(3);
                        table55.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table55.addCell(imgcell);
                        }else{
                            table55.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells1 = new PdfPCell();
                        cells1.setPhrase(new Phrase(remarks));
                        cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells1.setPaddingBottom(10);
                        cells1.setPaddingTop(10);
                        table55.addCell(cells);
                        personnel++;
                        personnel2++;

                    }else{
                        cells.setPhrase(par);
                        table55.addCell(cells);
                    }


                }else if(i==294){
                    PdfPCell cells = celldesc;
                    cells.setPhrase(par);
                    cells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    cell.setBackgroundColor(new BaseColor(196,188,150));
                    table56.addCell(cells);
                }else if(i>294 && i<302){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==297 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(3);
                        table57.addCell(cells);
                    }else if(i==300 && (j==1 || j==2)){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table57.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        if(i!=300){
                            String res = "";
                            if(part1){
                                res = get_col_results("HPP002",personnel+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("HPP002",personnel2+"",HomeActivity.appid);
                            }
                            if(res.equals("true")){
                                table57.addCell(imgcell);
                            }else{
                                table57.addCell(imgcell2);
                            }
                        }else{
                            table57.addCell(imgcell);
                        }


                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells1 = new PdfPCell();
                        cells1.setPhrase(new Phrase(remarks));
                        cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells1.setPaddingBottom(10);
                        cells1.setPaddingTop(10);
                        table57.addCell(cells);
                        personnel++;
                        personnel2++;


                    }else{
                        cells.setPhrase(par);
                        table57.addCell(cells);
                    }

                }else if(i>301 && i<304){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==302 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table57.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table57.addCell(imgcell);
                        }else{
                            table57.addCell(imgcell2);
                        }

                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells1 = new PdfPCell();
                        cells1.setPhrase(new Phrase(remarks));
                        cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells1.setPaddingBottom(10);
                        cells1.setPaddingTop(10);
                        table57.addCell(cells);
                        personnel++;
                        personnel2++;

                    }else{
                        cells.setPhrase(par);
                        table57.addCell(cells);
                    }

                }else if(i>303 && i<307){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==304 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(3);
                        table57.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        if(i!=304){
                            String res = "";
                            if(part1){
                                res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                            }

                            if(res.equals("true")){
                                table57.addCell(imgcell);
                            }else{
                                table57.addCell(imgcell2);
                            }
                        }else{
                            table57.addCell(imgcell);
                        }


                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells1 = new PdfPCell();
                        cells1.setPhrase(new Phrase(remarks));
                        cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells1.setPaddingBottom(10);
                        cells1.setPaddingTop(10);
                        table57.addCell(cells);
                        personnel++;
                    }else{
                        cells.setPhrase(par);
                        table57.addCell(cells);
                    }

                }else if(i>306 && i<309){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==307 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table57.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH001",personnel2+"",HomeActivity.appid);
                        }

                        if(res.equals("true")){
                            table57.addCell(imgcell);
                        }else{
                            table57.addCell(imgcell2);
                        }

                    }else if((desc8[i].length == 4 && j==3) || (desc8[i].length == 6 && j==5) || (desc8[i].length == 5 && j==4)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH001",personnel+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH001",personnel2+"",HomeActivity.appid);
                        }
                        PdfPCell cells1 = new PdfPCell();
                        cells1.setPhrase(new Phrase(remarks));
                        cells1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells1.setPaddingBottom(10);
                        cells1.setPaddingTop(10);
                        table57.addCell(cells);
                        personnel++;
                        personnel2++;
                    }else{
                        cells.setPhrase(par);
                        table57.addCell(cells);
                    }

                }else if(i>308 && i<313){

                    if(i==309){
                        PdfPCell hcells = celldesc;
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(196,188,150));
                        table58.addCell(hcells);
                    }else{
                        if(j==1){
                            String res = "";
                            if(part1){
                                res = get_col_results("OTHERS",others+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("OTHERS",others1+"",HomeActivity.appid);
                            }
                            if(res.equals("true")){
                                table58.addCell(imgcell);
                            }else{
                                table58.addCell(imgcell2);
                            }

                        }else if(j==2){
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("OTHERS",others1+"",HomeActivity.appid);
                            }
                            PdfPCell cells = new PdfPCell();
                            cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells.setPaddingLeft(5);
                            cells.setPaddingBottom(10);
                            cells.setPaddingTop(10);
                            cells.setPhrase(new Phrase(remarks));
                            table58.addCell(cells);
                            others++;
                            others1++;
                        }else{
                            PdfPCell cells = new PdfPCell();
                            cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cells.setPaddingLeft(5);
                            cells.setPaddingBottom(10);
                            cells.setPaddingTop(10);
                            cells.setPhrase(par);
                            table58.addCell(cells);
                        }

                    }
                }else if(i==313){
                    PdfPCell textarea = new PdfPCell();
                    textarea.setPadding(100);
                    textarea.setPhrase(new Phrase(" "));
                    table59.addCell(text);
                }else if(i>313 && i<319){

                    if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)) {
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table60.addCell(imgcell);
                        }else{
                            table60.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        PdfPCell cells = new PdfPCell();
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingLeft(5);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        cells.setPhrase(new Phrase(remarks));
                        table60.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingLeft(5);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        cells.setPhrase(par);
                        table60.addCell(cells);
                    }

                }else if(i>318 && i<328){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==319 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(9);
                        table61.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table61.addCell(imgcell);
                        }else{
                            table61.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table61.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table61.addCell(cells);
                    }
                }else if(i>327 && i<357){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==328 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(30);
                        table62.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table62.addCell(imgcell);
                        }else{
                            table62.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table62.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table62.addCell(cells);
                    }
                }else if(i>356 && i<402){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==357 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(13);
                        table63.addCell(cells);
                    }else if(i==370 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(23);
                        table63.addCell(cells);
                    }else if(i==393 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(7);
                        table63.addCell(cells);
                    }else if(i==400 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(2);
                        table63.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table63.addCell(imgcell);
                        }else{
                            table63.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table63.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table63.addCell(cells);
                    }
                }else if(i>401 && i<409){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==402 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(13);
                        table64.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table64.addCell(imgcell);
                        }else{
                            table64.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table64.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table64.addCell(cells);
                    }
                }else if(i>408 && i<429){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==409 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table65.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table65.addCell(imgcell);
                        }else{
                            table65.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table65.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table65.addCell(cells);
                    }
                }else if(i==429){
                    Log.d("foundindex",i+"");
                    PdfPCell hcells = new PdfPCell();
                    hcells.setPhrase(par);
                    hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    hcells.setPaddingBottom(10);
                    hcells.setPaddingTop(10);
                    hcells.setBackgroundColor(new BaseColor(196,188,150));
                    table66.addCell(hcells);
                }else if(i>429 && i<437){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==430 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table67.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",hh052+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table67.addCell(imgcell);
                        }else{
                            table67.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH005",hh052+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table67.addCell(cells);
                        hh05++;
                        hh052++;
                    }else{
                        cells.setPhrase(par);
                        table67.addCell(cells);
                    }
                }else if(i==437){
                    PdfPCell hcells = new PdfPCell();
                    hcells.setPhrase(par);
                    hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                    hcells.setPaddingBottom(10);
                    hcells.setPaddingTop(10);
                    hcells.setBackgroundColor(new BaseColor(196,188,150));
                    table68.addCell(hcells);
                }else if(i>437 && i<459){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==438 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table69.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",hh052+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table69.addCell(imgcell);
                        }else{
                            table69.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH005",hh052+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table69.addCell(cells);
                        hh05++;
                        hh052++;
                    }else{
                        cells.setPhrase(par);
                        table69.addCell(cells);
                    }
                }else if(i>458 && i<475){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==459){
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(196,188,150));
                        table70.addCell(hcells);
                    }else if(i==460 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table71.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",hh052+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table71.addCell(imgcell);
                        }else{
                            table71.addCell(imgcell2);
                        }


                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH005",hh05+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH005",hh052+"",HomeActivity.appid);
                        }

                        cells.setPhrase(new Phrase(remarks));
                        table71.addCell(cells);
                        hh05++;
                        hh052++;
                    }else{
                        cells.setPhrase(par);
                        table71.addCell(cells);
                    }
                }else if(i>474 && i<490){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==475){
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(196,188,150));
                        table72.addCell(hcells);
                    }else if(i==476 && j==1){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table73.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table73.addCell(imgcell);
                        }else{
                            table73.addCell(imgcell2);
                        }

                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        cells.setPhrase(new Phrase(remarks));
                        table73.addCell(cells);
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table73.addCell(cells);
                    }
                }else if(i>489 && i<495){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==490){
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(196,188,150));
                        table74.addCell(hcells);
                    }else if(i==491 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(21);
                        table75.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table75.addCell(imgcell);
                        }else{
                            table75.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        table75.addCell(new Phrase(remarks));
                        equipment++;
                        equipment2++;
                    }else{
                        cells.setPhrase(par);
                        table75.addCell(cells);
                    }
                }else if(i>494 && i<521){
                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==495){
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(196,188,150));
                        table76.addCell(hcells);
                    }else if(i==496 && j==2){
                        cells.setPhrase(par);
                        cells.setRowspan(50);
                        table77.addCell(cells);
                    }else if((desc8[i].length == 4 && j==2) || (desc8[i].length == 6 && j==4) || (desc8[i].length == 5 && j==3)){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table77.addCell(imgcell);
                        }else{
                            table77.addCell(imgcell2);
                        }
                    }else if((desc8[i].length == 5 && j==4) || (desc8[i].length == 4 && j==3)){
                        String remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        cells.setPhrase(new Phrase(remarks));
                        table77.addCell(cells);
                        equipment++;
                    }else{
                        cells.setPhrase(par);
                        table77.addCell(cells);
                    }
                }else if(i==521){
                    if(j==3){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table78.addCell(imgcell);
                        }else{
                            table78.addCell(imgcell2);
                        }
                    }else if(j==4){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        table78.addCell(new Phrase(remarks));
                        equipment++;
                        equipment2++;
                    }else{
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        table78.addCell(hcells);
                    }

                }else if(i==522){
                    if(j==3){
                        String res = "";
                        if(part1){
                            res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        if(res.equals("true")){
                            table79.addCell(imgcell);
                        }else{
                            table79.addCell(imgcell2);
                        }
                    }else if(j==4){
                        String remarks = "";
                        if(part1){
                            remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                        }else if(part2){
                            remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                        }
                        table79.addCell(new Phrase(remarks));
                        equipment++;
                        equipment2++;
                    }else{
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        table79.addCell(hcells);
                    }
                }else if(i>522 && i<560){

                    PdfPCell cells = new PdfPCell();
                    cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cells.setPaddingLeft(5);
                    cells.setPaddingBottom(10);
                    cells.setPaddingTop(10);
                    if(i==523){
                        PdfPCell hcells = new PdfPCell();
                        hcells.setPhrase(par);
                        hcells.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hcells.setPaddingBottom(10);
                        hcells.setPaddingTop(10);
                        hcells.setBackgroundColor(new BaseColor(214,227,188));
                        table80.addCell(hcells);
                    }else{
                        colindex = 0;
                        if(j!=0 && j!=16){
                            String res = "";
                            if(part1){
                                res = get_col_results("HH003",equipment+"",HomeActivity.appid);
                            }else if(part2){
                                res = get_col_results("HH003",equipment2+"",HomeActivity.appid);
                            }
                            if(res.equals("true")){
                                table80.addCell(imgcell1);
                            }else{
                                table80.addCell(imgcell3);
                            }
                            colindex++;
                        }else if(j==16){
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("HH003",equipment+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("HH003",equipment2+"",HomeActivity.appid);
                            }
                            table80.addCell(new Phrase(remarks));
                            others++;
                            others1++;
                        }else{
                            cells.setPhrase(par);
                            table80.addCell(cells);
                        }

                    }
                }else if(i>559){

                    colindex = 0;
                    if(j!=0 && j!=16){
                        if(i>559 && i<568){
                            Log.d("hho42",hh04+"");
                            String res ="";
                            if(part1){
                                res = get_col_results1("HH004",hh04+"",HomeActivity.appid,colindex);
                            }else if(part2){
                                res = get_col_results1("HH004",hh042+"",HomeActivity.appid,colindex);
                            }
                            Log.d("hh004res2",res);
                            if(res.equals("true")){
                                table81.addCell(imgcell1);
                            }else{
                                table81.addCell(imgcell3);
                            }
                            colindex++;
                        }else{

                            String res = "";
                            if(part1){
                                res =  get_col_results1("OTHERS",others+"",HomeActivity.appid,colindex);
                            }else if(part2){
                                res =  get_col_results1("OTHERS",others1+"",HomeActivity.appid,colindex);
                            }
                            if(res.equals("true")){
                                table81.addCell(imgcell1);
                            }else{
                                table81.addCell(imgcell3);
                            }
                            colindex++;
                        }
                    }else if(j==16){
                        if(i>559 && i<568){
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("HH004",hh04+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("HH004",hh042+"",HomeActivity.appid);
                            }
                            table81.addCell(new Phrase(remarks));
                            hh04++;
                            hh042++;
                        }else{
                            String remarks = "";
                            if(part1){
                                remarks = get_remarks_results("OTHERS",others+"",HomeActivity.appid);
                            }else if(part2){
                                remarks = get_remarks_results("OTHERS",others1+"",HomeActivity.appid);
                            }
                            table81.addCell(new Phrase(remarks));
                            others++;
                            others1++;
                        }

                    }else{
                        PdfPCell cells = new PdfPCell();
                        cells.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cells.setPaddingLeft(5);
                        cells.setPaddingBottom(10);
                        cells.setPaddingTop(10);
                        cells.setPhrase(par);
                        table81.addCell(cells);
                    }

                }

            }
            Log.d("index",i+"");
            count++;
        }

        Log.d("counts",count+"");
        Log.d("equipment",equipment+"");
        Log.d("personnel",personnel+"");
        Log.d("personnel2",personnel2+"");
        Log.d("hh04",hh04+"");
        Log.d("hh042",hh042+"");
        Log.d("hh05",hh05+"");
        Log.d("hh052",hh052+"");
        Log.d("hpp02",hpp02+"");
        Log.d("others",others+"");
        Log.d("others1",others1+"");
        /*  title - PART IV - LEVEL 1 ATTACHMENT 1.A - PERSONAL
            title 2 - ATTACHMENT 1.B - PHYSICAL PLANT
            title 4 - ATTACHMENT 1.C - EQUIPMENT INSTRUMENT
            notes - ATTACHMENT 1.E - ADD-ON SERVICE
            title9 - PART IV - LEVEL 1 ATTACHMENT 2.A – PERSONNEL
            title10 - ATTACHMENT 2.B – PHYSICAL TEST
            title12 = ATTACHMENT 2.C- EQUIPMENT/INSTRUMENT
            title13 -ATTACHMENT 2.D – EMERGENCY CART CONTENTS FOR LEVEL 2 HOSPITAL
            inspected - Inspected By:
            namesign*/
        if(part1){
            document.add(title);
            document.add(table);
            document.add(table1);
            document.add(table2);
            document.add(table3);
            document.add(table4);
            document.add(table5);
            document.add(table6);
            document.add(table7);
            document.add(table8);
            document.add(table9);
            document.add(title2);
            document.add(table10);
            document.add(title3);
            document.add(table11);
            document.add(title4);
            document.add(table12);
            document.add(table13);
            document.add(table14);
            document.add(table15);
            document.add(table16);
            document.add(table17);
            document.add(table18);
            document.add(table19);
            document.add(table20);
            document.add(table21);
            document.add(table22);
            document.add(table23);
            document.add(table24);
            document.add(table25);
            document.add(table26);
            document.add(table27);
            document.add(table28);
            document.add(table29);
            document.add(table30);
            document.add(table31);
            document.add(table32);
            document.add(table33);
            document.add(table34);
            document.add(title5);
            document.add(table35);
            document.add(table36);
            document.add(table37);
            document.add(notes);
            document.add(title6);
            document.add(table38);
            document.add(table39);
            document.add(table40);
            document.add(table41);
            document.add(title7);
            document.add(table42);
            document.add(table43);
            document.add(table44);
            document.add(table45);
            document.add(title8);
            document.add(table46);
            document.add(table47);
            document.add(table48);
            document.add(table49);
            document.add(notes1);
        }
        if(part2){
            document.add(title9);
            document.add(table50);
            document.add(table1);
            document.add(table51);
            document.add(table52);
            document.add(table53);
            document.add(table54);
            document.add(table55);
            document.add(table56);
            document.add(table57);
            document.add(title10);
            document.add(table58);
            document.add(title11);
            document.add(table59);
            document.add(title12);
            document.add(table12);
            document.add(table13);
            document.add(table60);
            document.add(table15);
            document.add(table61);
            document.add(table17);
            document.add(table62);
            document.add(table19);
            document.add(table63);
            document.add(table25);
            document.add(table64);
            document.add(table27);
            document.add(table65);
            document.add(table66);
            document.add(table67);
            document.add(table68);
            document.add(table69);
            document.add(table70);
            document.add(table71);
            document.add(table72);
            document.add(table73);
            document.add(table74);
            document.add(table75);
            document.add(table76);
            document.add(table77);
            document.add(table31);
            document.add(table78);
            document.add(table33);
            document.add(table79);
            document.add(title13);
            document.add(table80);
            document.add(table36);
            document.add(table81);
        }


        //part 2

        document.add(notes2);
        document.add(inspected);
        document.add(namesign);
        document.add(footer);
        document.close();

        previewPdf();

    }

    private Image getCheck(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("check.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(20,20);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }

    private Image getWrong(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("cross.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(20,20);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }

    private Image getWrong1(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("cross.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(10,10);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }

    private Image getCheck1(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("check.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(10,10);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }


    private void previewPdf() {

        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");

            startActivity(intent);
        }else{
            Toast.makeText(this,"Download a PDF Viewer to see the generated PDF",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {

        try {
            createPdfWrapper();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
