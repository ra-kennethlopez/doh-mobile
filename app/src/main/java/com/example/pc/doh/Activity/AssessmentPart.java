package com.example.pc.doh.Activity;

import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
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
import com.example.pc.doh.Adapter.HeadersAdapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.Headers;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.Model.headerone;
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
import com.itextpdf.text.Rectangle;
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

public class AssessmentPart extends AppCompatActivity implements View.OnClickListener {


    TextView lblfacname;
    public static String asmt2l_id;
    ArrayList<Headers> list = new ArrayList<>();
    ArrayList<headerone> hlist = new ArrayList<>();
    HeadersAdapter hAdapter;
    RecyclerView headerrv;
    public static String id,hid;
    public static String desc,hdesc;
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
    PopupMenu menu;
    public static String path;
    Button btngenerate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partlayout);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checker = new InternetCheck(this);

        lblfacname = findViewById(R.id.facname);
        btngenerate = findViewById(R.id.btngenerate);
        btngenerate.setOnClickListener(this);
        db = new DatabaseHelper(this);

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
                Intent assestment = new Intent(AssessmentPart.this, AssestmentDetailsActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(AssessmentPart.this);
            }
        });
        getSupportActionBar().setTitle("SERVICES");
        lblfacname.setText(HomeActivity.faclityname);
        lblfacname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu = new PopupMenu(getApplicationContext(), v);
                build_menu();
            }
        });

        /*this.findViewById(R.id.hamburger_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            PopupMenu menu = new PopupMenu(getApplicationContext(), v);
            menu.getMenu().add(Menu.NONE, 1, 1, "Share");
            menu.getMenu().add(Menu.NONE, 2, 2, "Comment");
            menu.show();

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    int i = item.getItemId();
                    if (i == 1) {
                        //handle share
                        return true;
                    } else if (i == 2) {
                        //handle comment
                        return true;
                    } else {
                        return false;
                    }
                }

            });
         }
     });*/
        headerrv = findViewById(R.id.headerrv);

        if (checker.checkHasInternet()) {
            get_parts_online();
        } else {
            get_parts_offline();
        }

        Log.d("appid",HomeActivity.appid);
        hAdapter = new HeadersAdapter(this, list);
        headerrv.setLayoutManager(new LinearLayoutManager(this));
        headerrv.setAdapter(hAdapter);
        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                path = "button";
                id = list.get(position).getAsmt2l_id();
                desc = list.get(position).getAsmt2l_desc();
                Intent headerone = new Intent(getApplicationContext(), AssessmentHeaderOne.class);
                startActivity(headerone);
                Animatoo.animateSlideLeft(AssessmentPart.this);
            }
        });

        //btnsummary.setVisibility(View.VISIBLE);

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
                //generate();
            }
            return;
        }

    }


    private void build_menu(){
        int size = list.size();
        for(int i=0;i<size;i++){
            SubMenu sub = menu.getMenu().addSubMenu(i,i,1,list.get(i).getAsmt2l_desc());
            for(int j=0;j<hlist.size();j++){
               if(list.get(i).getAsmt2l_id() == hlist.get(j).getHeaderid()){
                   int id = Integer.parseInt(hlist.get(j).getId());
                   sub.add(i,id,1,hlist.get(j).getDesc());
               }
            }
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("hlist",item.getItemId()+"");
                Log.d("groupid",item.getGroupId()+"");
                for(int i=0;i<hlist.size();i++){
                   String hid = item.getItemId()+"";
                   if(hlist.get(i).getId().equals(hid)){
                       Log.d("hlistcheck","true");
                        path = "menu";

                        String aid = db.get_tbl_assessment_header(HomeActivity.appid,uid,hid,"1");
                        if (db.checkDatas("tbl_save_assessment_header", "assessheadid", aid)){
                           Log.d("found","true");
                           Toast.makeText(getApplicationContext(),"Already answered assessment.",Toast.LENGTH_SHORT).show();
                        }else{
                            AssessmentPart.hid = hid;
                            AssessmentPart.hdesc = hlist.get(i).getDesc();
                            id = list.get(item.getGroupId()).getAsmt2l_id();
                            desc = list.get(item.getGroupId()).getAsmt2l_desc();
                            //Log.d("hid",hid);
                            //Log.d("hdesc",hdesc);
                            //Log.d("id",id);
                            //Log.d("desc",desc);
                            Intent headerone = new Intent(getApplicationContext(), ShowAssessment.class);
                            startActivity(headerone);
                            Animatoo.animateSlideLeft(AssessmentPart.this);
                        }
                   }

//                   Log.d("hlistitem",hlist.get(i).getId());
                   //Log.d("menuitem",hid);

                }
                return false;
            }
        });
        menu.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    public void get_parts_offline() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        String pid = db.getpartid(HomeActivity.appid,"");
        Cursor det = db.get_item("tbl_assessment_part", "partid", pid);
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                int countassess = 0;
                int heads = 0;
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray head = obj.getJSONArray("head");
                    if(head.length()>0){
                        List<String> unique = new ArrayList<>();

                        for(int i =0;i<head.length();i++){
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("desc");
                            String hid = db.get_tbl_assessment_header(HomeActivity.appid,uid,id,"0");
                            String assess = "false";

                            if (!unique.contains(id)) {
                                if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                    countassess++;
                                    assess = db.get_tbl_assessment_header_assess(HomeActivity.appid,uid,id,"0");
                                }
                                list.add(new Headers(id,desc, assess,"",""));
                                unique.add(id);
                                get_header_offline(id);
                            }
                        }
                    }else{
                       heads = 1;
                    }
                    Log.d("head",obj.getJSONArray("head").toString());
                    if(countassess == list.size() && heads != 1){
                        Log.d("countassess","true");
                        btngenerate.setVisibility(View.VISIBLE);
                    }else{
                        btngenerate.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /*
                * try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("obj",obj.toString());
                            JSONArray head = obj.getJSONArray("head");

                            if(head.length()>0){
                               for(int i =0;i<head.length();i++){
                                   String id = head.getJSONObject(i).getString("id");
                                   String desc =head.getJSONObject(i).getString("desc");
                                   String hid = db.get_tbl_assessment_header(HomeActivity.appid,uid,id,"0");
                                   String assess = "false";
                                   if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                       assess = db.get_tbl_assessment_header_assess(HomeActivity.appid,uid,id,"0");
                                   }

                                   //jsonArray.put(item);
                                   list.add(new Headers(id,desc,assess,"",""));
                                   get_headerone_online(id);
                               }
                            }else{

                            }
                            Log.d("head",obj.getJSONArray("head").toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                * */
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
                det.moveToNext();
            }
        } else {
            TextView lbl = findViewById(R.id.lblheadmessage);
            lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }

    }

    private void get_header_offline(final String id){
        Cursor det = db.get_item("tbl_assessment_headerone", "id", id);
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray head = obj.getJSONArray("head");
                    int countassess = 0;
                    if(head.length()>0){
                        for(int i =0;i<head.length();i++){
                            String hid = head.getJSONObject(i).getString("id");
                            String hdesc =head.getJSONObject(i).getString("desc");
                            hlist.add(new headerone(hid,hdesc,id));
                        }
                    }else{

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                det.moveToNext();
            }
        }
    }
    private void get_headerone_online(final String id){
        //Urls.getheaderone+HomeActivity.appid+"/"+id
        Log.d("url",Urls.getheaderone+HomeActivity.appid);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.getheaderone+HomeActivity.appid+"/"+id
                ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("headerone",response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray head = obj.getJSONArray("head");
                            String honeid = db.gethoneid(HomeActivity.appid,"",id);
                            if (db.checkDatas("tbl_assessment_headerone", "honeid", honeid)){

                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_assessment_headerone", ucolumns, udata, "honeid",honeid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }

                            }else{

                                String[] dcolumns = {"json_data", "uid", "appid","id"};
                                String[] datas = {response, uid, HomeActivity.appid,id};
                                if (db.add("tbl_assessment_headerone", dcolumns, datas, "")) {
                                    Log.d("tbl_headerone", "added");
                                } else {
                                    Log.d("tbl_headerone", "not added");
                                }

                            }
                            if(head.length()>0){
                                for(int i =0;i<head.length();i++){
                                    String oid = head.getJSONObject(i).getString("id");
                                    String desc =head.getJSONObject(i).getString("desc");
                                    hlist.add(new headerone(oid,desc,id));
                                    get_showassessment(oid);
                                }
                            }else{

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
                params.put("isMobile","dan");
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void get_showassessment(final String id){

        StringRequest request = new StringRequest(Request.Method.POST, Urls.getassessmentdet+HomeActivity.appid+"/"+id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responsess",response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.getJSONObject("data");
                            JSONArray head = obj.getJSONArray("head");
                            String sid = db.getsid(HomeActivity.appid,"",id);
                            if (db.checkDatas("tbl_show_assessment", "sid",sid)){
                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_show_assessment", ucolumns, udata, "sid",sid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }
                            }else{
                                String[] dcolumns = {"json_data", "uid", "appid","id","monid"};
                                String[] datas = {response, uid,HomeActivity.appid,id,""};
                                if (db.add("tbl_show_assessment", dcolumns, datas, "")) {
                                    Log.d("tbl_show_assessment", "added");
                                } else {
                                    Log.d("tbl_show_assessment", "not added");
                                }
                            }
                            //Log.d("head",head.toString());3
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
                params.put("isMobile","dan");
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void get_parts_online() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        //+ HomeActivity.appid + "/" + HomeActivity.type
        Log.d("appid",HomeActivity.appid);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.getparts+HomeActivity.appid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("partsresponse",response);
                        String pid = db.getpartid(HomeActivity.appid,"");
                        if (db.checkDatas("tbl_assessment_part", "partid", pid)){
                            String[] ucolumns = {"json_data"};
                            String[] udata = {response};
                            if (db.update("tbl_assessment_part", ucolumns, udata, "appid",HomeActivity.appid)) {
                                Log.d("updatedata", "update");
                            } else {
                                Log.d("updatedata", "not update");
                            }
                        }else{
                            String[] dcolumns = {"json_data", "uid", "appid","monid"};
                            String[] datas = {response, uid, HomeActivity.appid,""};
                            if (db.add("tbl_assessment_part", dcolumns, datas, "")) {
                                Log.d("tbl_assessment_part", "added");
                            } else {
                                Log.d("tbl_assessment_part", "not added");
                            }

                        }
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("obj",obj.toString());
                            JSONArray head = obj.getJSONArray("head");
                            int countassess = 0;
                            int heads = 0;
                            if(head.length()>0){
                                List<String> unique = new ArrayList<>();

                                for(int i =0;i<head.length();i++){
                                   String id = head.getJSONObject(i).getString("id");
                                   String desc =head.getJSONObject(i).getString("desc");
                                   String hid = db.get_tbl_assessment_header(HomeActivity.appid,uid,id,"0");
                                   String assess = "false";

                                   if (!unique.contains(id)) {
                                       if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                           countassess++;
                                           assess = db.get_tbl_assessment_header_assess(HomeActivity.appid,uid,id,"0");
                                       }

                                       //jsonArray.put(item);
                                       list.add(new Headers(id,desc,assess,"",""));
                                       unique.add(id);
                                       get_headerone_online(id);
                                   }
                               }
                            }else{
                                heads = 1;
                            }
                            Log.d("head",obj.getJSONArray("head").toString());
                            if(countassess == list.size() && heads != 1){
                                Log.d("countassess","true");
                                btngenerate.setVisibility(View.VISIBLE);
                            }else{
                                btngenerate.setVisibility(View.GONE);
                            }
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
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    @Override
    public void onClick(View v) {
        recommendation.type = "Assessment";
        Intent recommend = new Intent(this,recommendation.class);
        startActivityForResult(recommend,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            createPdfWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private PdfPCell createCell(String text, int padding, int rowspan, int colspan, boolean ifbold, String color){
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
        if(color == "1"){
            cell.setBackgroundColor(new BaseColor(196,188,150));
        }else if(color == "2"){
            cell.setBackgroundColor(new BaseColor(148,138,84));
        }

        cell.setPadding(padding);

        return cell;
    }

    private Paragraph createParagraph(String text){
        Paragraph par = new Paragraph(text);
        par.setSpacingBefore(10f);
        par.setSpacingAfter(10f);
        return par;
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(AssessmentPart.this, "",
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

    private Image getNA(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("ban.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(30,30);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }


        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"assessment.pdf");

        Rectangle pagesize = new Rectangle(612, 861);

        //add check icon
        PdfPCell check = new PdfPCell(getCheck());
        check.setPadding(10);
        check.setVerticalAlignment(Element.ALIGN_CENTER);
        check.setHorizontalAlignment(Element.ALIGN_CENTER);

        //add wrong icon
        PdfPCell wrong = new PdfPCell(getWrong());
        wrong.setPadding(10);
        wrong.setVerticalAlignment(Element.ALIGN_CENTER);
        wrong.setHorizontalAlignment(Element.ALIGN_CENTER);

        //add NA icon
        PdfPCell na = new PdfPCell(getNA());
        na.setPadding(10);
        na.setVerticalAlignment(Element.ALIGN_CENTER);
        na.setHorizontalAlignment(Element.ALIGN_CENTER);

        //Document document = new Document(pagesize);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(5,5,5,5);
        PdfWriter.getInstance(document, output);
        document.open();
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN,20);
        Paragraph notes2 = new Paragraph("*Notes:\n" +
                "ER – Emergency Room\n" +
                "OR – Operating Room\n" +
                "DR – Delivery Room\n" +
                "NS – Nurses’ Station",normal);
        float [] pointColumnWidths = {1000F,230F,200F};
        PdfPTable table = new PdfPTable(pointColumnWidths);
        table.setTotalWidth(600);
        table.setLockedWidth(true);
        table.addCell(createCell("STANDARD AND REQUIREMENTS",10,0,0,true,"2"));
        table.addCell(createCell("COMPLIANT",10,0,0,true,"2"));
        table.addCell(createCell("REMARKS",10,0,0,true,"2"));

        PdfPTable table1 = new PdfPTable(pointColumnWidths);
        table1.setTotalWidth(600);
        table1.setLockedWidth(true);
        table1.addCell(createCell("STANDARD AND REQUIREMENTS",10,0,0,true,"2"));
        table1.addCell(createCell("COMPLIANT",10,0,0,true,"2"));
        table1.addCell(createCell("REMARKS",10,0,0,true,"2"));


        PdfPTable table2 = new PdfPTable(pointColumnWidths);
        table2.setTotalWidth(600);
        table2.setLockedWidth(true);
        table2.addCell(createCell("STANDARD AND REQUIREMENTS",10,0,0,true,"2"));
        table2.addCell(createCell("COMPLIANT",10,0,0,true,"2"));
        table2.addCell(createCell("REMARKS",10,0,0,true,"2"));

        //footer
        Paragraph assessment = createParagraph("Assessed By:");
        PdfPTable footer = new PdfPTable(3);
        footer.setTotalWidth(600);
        footer.setLockedWidth(true);
        footer.addCell(createCell("Printed Name",10,0,0,true,""));
        footer.addCell(createCell("Signature",10,0,0,true,""));
        footer.addCell(createCell("Position/Designation",10,0,0,true,""));
        String assessb = "";


        //end of footer

        //body
        String ab = "";
        Cursor c = db.get_tbl_assesscombined_res(HomeActivity.appid);
        if(c != null && c.getCount()>0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                String choice = c.getString(c.getColumnIndex("evaluation"));
                String remarks = c.getString(c.getColumnIndex("remarks"));
                String head = c.getString(c.getColumnIndex("assessmentHead"));
                String assessname = c.getString(c.getColumnIndex("assessmentName"));
                if(!ab.equals(c.getString(c.getColumnIndex("evaluatedBy")))){
                    footer.addCell(createCell(c.getString(c.getColumnIndex("ename")),10,0,0,true,""));
                    footer.addCell(createCell(" ",10,0,0,true,""));
                    footer.addCell(createCell(c.getString(c.getColumnIndex("epos")),10,0,0,true,""));
                    ab = c.getString(c.getColumnIndex("evaluatedBy"));
                }

                if(!head.equals("")){
                    table.addCell(createCell(String.valueOf(Html.fromHtml(head)),10,0,3,true,"1"));
                }
                table.addCell(createCell(String.valueOf(Html.fromHtml(assessname)),10,0,0,false,""));
                if(choice.equals("1")){
                    table.addCell(check);
                }else if(choice.equals("0")){
                    table.addCell(wrong);
                }else if(choice.equals("NA")){
                    table.addCell(na);
                }



                table.addCell(createCell(remarks,10,0,0,false,""));

                c.moveToNext();
            }
        }

        document.add(table);


        //end of body
        Cursor i = db.get_tbl_assesscombined_res_improvement(HomeActivity.appid);
        if(i != null && i.getCount()>0){
            Paragraph assess = new Paragraph("For Improvement",normal);
            assess.setSpacingBefore(10f);
            assess.setSpacingAfter(10f);
            document.add(assess);
            i.moveToFirst();
            while(!i.isAfterLast()){
                String choice = i.getString(i.getColumnIndex("evaluation"));
                String remarks = i.getString(i.getColumnIndex("remarks"));
                String head = i.getString(i.getColumnIndex("assessmentHead"));
                String assessname = i.getString(i.getColumnIndex("assessmentName"));
                if(!head.equals("")){
                    table1.addCell(createCell(String.valueOf(Html.fromHtml(head)),10,0,3,true,"1"));
                }
                table1.addCell(createCell(String.valueOf(Html.fromHtml(assessname)),10,0,0,false,""));
                if(choice.equals("1")){
                    table1.addCell(check);
                }else if(choice.equals("0")){
                    table1.addCell(wrong);
                }else if(choice.equals("NA")){
                    table1.addCell(na);
                }
                table1.addCell(createCell(remarks,10,0,0,false,""));

                i.moveToNext();
            }
            document.add(table1);
        }

        Cursor i2 = db.get_tbl_assesscombined_res_compliance(HomeActivity.appid);
        if(i2 != null && i2.getCount()>0){
            Paragraph assess = new Paragraph("For Compliance",normal);
            assess.setSpacingBefore(10f);
            assess.setSpacingAfter(10f);
            document.add(assess);
            i2.moveToFirst();
            while(!i2.isAfterLast()){
                String choice = i2.getString(i2.getColumnIndex("evaluation"));
                String remarks = i2.getString(i2.getColumnIndex("remarks"));
                String head = i2.getString(i2.getColumnIndex("assessmentHead"));
                String assessname = i2.getString(i2.getColumnIndex("assessmentName"));
                if(!head.equals("")){
                    table2.addCell(createCell(String.valueOf(Html.fromHtml(head)),10,0,3,true,"1"));
                }
                table2.addCell(createCell(String.valueOf(Html.fromHtml(assessname)),10,0,0,false,""));
                if(choice.equals("1")){
                    table2.addCell(check);
                }else if(choice.equals("0")){
                    table2.addCell(wrong);
                }else if(choice.equals("NA")){
                    table2.addCell(na);
                }
                table2.addCell(createCell(remarks,10,0,0,false,""));

                i2.moveToNext();
            }
            document.add(table2);
        }
        String recby = "";
        String pos = "";
        Cursor rec = db.get_tbl_assessrecommend(HomeActivity.appid);
        if(rec != null && rec.getCount()>0){
            Paragraph assess = new Paragraph("Recommendation",normal);
            document.add(assess);

            rec.moveToFirst();
            while(!rec.isAfterLast()){
                Paragraph message = new Paragraph();
                String monid = rec.getString(rec.getColumnIndex("monid"));
                String nobed = rec.getString(rec.getColumnIndex("noofbed"));
                String nodial = rec.getString(rec.getColumnIndex("noofdialysis"));
                String choice = rec.getString(rec.getColumnIndex("choice"));
                String valfrom = rec.getString(rec.getColumnIndex("valfrom"));
                String valto = rec.getString(rec.getColumnIndex("valto"));
                String days = rec.getString(rec.getColumnIndex("days"));
                String noted = rec.getString(rec.getColumnIndex("details"));
                recby = rec.getString(rec.getColumnIndex("conforme"));
                pos = rec.getString(rec.getColumnIndex("conformeDesignation"));
                String recoommend = "";
                if(monid.equals("")){
                    if(choice.equals("issuance")){
                     recoommend = "For Issuance of License to Operate with Validity date from "+valfrom+" to "+valto;
                     if(!nobed.equals("")){
                         recoommend += "\nWith:\n "+nobed+" bed Station";
                     }else{
                         recoommend += "\nWith:\n No bed Station";
                     }
                     if(!nodial.equals("")){
                         recoommend += "\n "+nodial+" Dialysis Station";
                     }else{
                         recoommend += "\n No Dialysis Station";
                     }
                     message = createParagraph(recoommend);

                    }else if(choice.equals("compliance")){
                     recoommend = "Issuance depends upon compliance to the recommendations given and submission of the following within "+
                     days+" days from the date of inspection:";
                     message = createParagraph(recoommend);
                    }else if(choice.equals("non")){

                    }
                }
                document.add(message);
                message = createParagraph(noted);
                document.add(message);
                rec.moveToNext();
            }


        }
        //2nd body

        //end 2nd body



        Paragraph assess2 = createParagraph("Received By:");
        PdfPTable footer2 = new PdfPTable(4);
        footer2.setTotalWidth(600);
        footer2.setLockedWidth(true);
        footer2.addCell(createCell("Printed Name",10,0,0,true,""));
        footer2.addCell(createCell("Signature",10,0,0,true,""));
        footer2.addCell(createCell("Position/Designation",10,0,0,true,""));
        footer2.addCell(createCell("Date Received",10,0,0,true,""));
        footer2.addCell(createCell(recby,10,0,0,true,""));
        footer2.addCell(createCell(" ",10,0,0,true,""));
        footer2.addCell(createCell(pos,10,0,0,true,""));
        footer2.addCell(createCell(" ",10,0,0,true,""));

        document.add(assessment);
        document.add(footer);
        document.add(assess2);
        document.add(footer2);
        document.close();

        previewPdf();
    }

    private void previewPdf() {
        Log.d("previewpdf","execute");
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Log.d("list",list.size()+"");

        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");
            startActivity(intent);
            Log.d("package","true");
        }else{
            //Toast.makeText(this,"Download a PDF Viewer to see the generated PDF",Toast.LENGTH_SHORT).show();
        }
    }
}
