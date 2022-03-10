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
import android.graphics.Paint;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationPart extends AppCompatActivity implements View.OnClickListener {

    TextView lblfacname;
    public static String asmt2l_id;
    ArrayList<Headers> list = new ArrayList<>();
    ArrayList<headerone> hlist = new ArrayList<>();
    HeadersAdapter hAdapter;
    RecyclerView headerrv;
    public static String id;
    public static String desc;
    InternetCheck checker;
    DatabaseHelper db;
    private String uid,userfullname;

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
    private String address;
    private String count;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partlayout);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checker = new InternetCheck(this);

        lblfacname = findViewById(R.id.facname);
        btngenerate = findViewById(R.id.btngenerate);
        btngenerate.setVisibility(View.GONE);
        btngenerate.setOnClickListener(this);
        db = new DatabaseHelper(this);

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        userfullname = user.getName();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(EvaluationPart.this, AssestmentDetailsActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(EvaluationPart.this);
            }
        });
        getSupportActionBar().setTitle("SERVICES");
        lblfacname.setText(EvaluationActivity.faclityname);
        lblfacname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu = new PopupMenu(getApplicationContext(), v);
                build_menu();
            }
        });


        headerrv = findViewById(R.id.headerrv);
        //get_parts_offline();
        //get_parts_offline();
        if (checker.checkHasInternet()) {
            get_parts_online();
            //get_parts_offline();
        } else {
            get_parts_offline();
        }
        //get_parts_offline();

        hAdapter = new HeadersAdapter(this, list);
        headerrv.setLayoutManager(new LinearLayoutManager(this));
        headerrv.setAdapter(hAdapter);
        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                path = "button";
                id = list.get(position).getAsmt2l_id();
                desc = list.get(position).getAsmt2l_desc();
                Intent headerone = new Intent(getApplicationContext(), EvaluationShowAssessment.class);
                startActivity(headerone);
                Animatoo.animateSlideLeft(EvaluationPart.this);

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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void build_menu(){
        int size = list.size();
        for(int i=0;i<size;i++){
            //int id = Integer.parseInt(list.get(i).getAsmt2l_id());
            Log.d("idss",list.get(i).getAsmt2l_id());
            SubMenu sub = menu.getMenu().addSubMenu(i,i,1,list.get(i).getAsmt2l_desc());
//            for(int j=0;j<hlist.size();j++){
//                if(list.get(i).getAsmt2l_id() == hlist.get(j).getHeaderid()){
//                    int id = Integer.parseInt(hlist.get(j).getId());
//                    sub.add(i,id,1,hlist.get(j).getDesc());
//                }
//            }
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("hlist",item.getItemId()+"");
                id = list.get(item.getItemId()).getAsmt2l_id();
                Intent headerone = new Intent(getApplicationContext(), EvaluationShowAssessment.class);
                startActivity(headerone);
                Animatoo.animateSlideLeft(EvaluationPart.this);
                return false;
            }
        });
        menu.show();
    }

    public void get_parts_offline() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        String pid = db.getpartid(EvaluationActivity.appid,"");
        Cursor det = db.get_item("tbl_assessment_part", "partid", pid);
        boolean checkifcomplied = false;
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
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("desc");
                            String assess = "false";
                            String hid = db.get_tbl_assessment_header(EvaluationActivity.appid,uid,id,"0");
                            if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                Log.d("check","true");
                                countassess++;
                                assess = db.get_tbl_assessment_header_assess(EvaluationActivity.appid,uid,id,"0");
                            }
                            list.add(new Headers(id,desc, assess,"",""));
                            //hAdapter.notifyDataSetChanged();
                            //get_header_offline(id);
                        }
                    }else{

                    }
                    Log.d("head",obj.getJSONArray("head").toString());
                    if(countassess == list.size()){
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
                det.moveToNext();
            }
        } else {
//            TextView lbl = findViewById(R.id.lblheadmessage);
//            lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }

    }

    private void part(final String count){
        Log.d("count",count);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.geteparts+EvaluationActivity.appid+"/"+count,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("eveluation",response);
                        String pid = db.getpartid(EvaluationActivity.appid,"");
                        if (db.checkDatas("tbl_assessment_part", "partid", pid)){
                            String[] ucolumns = {"json_data"};
                            String[] udata = {response};
                            if (db.update("tbl_assessment_part", ucolumns, udata, "appid",EvaluationActivity.appid)) {
                                Log.d("updatedata", "update");
                            } else {
                                Log.d("updatedata", "not update");
                            }
                        }else{
                            String[] dcolumns = {"json_data", "uid", "appid","monid"};
                            String[] datas = {response, uid, EvaluationActivity.appid,""};
                            if (db.add("tbl_assessment_part", dcolumns, datas, "")) {
                                Log.d("tbl_assessment_part", "added");
                            } else {
                                Log.d("tbl_assessment_part", "not added");
                            }
                        }
                        try {
                            JSONObject obj = new JSONObject(response);
                            address =  obj.getJSONObject("data").getString("mailingAddress");
                            JSONArray head = obj.getJSONArray("head");
                            int countassess = 0;
                            if(head.length()>0){
                                for(int i =0;i<head.length();i++){
                                    String id = head.getJSONObject(i).getString("id");
                                    String desc =head.getJSONObject(i).getString("desc");
                                    String hid = db.get_tbl_assessment_header(EvaluationActivity.appid,uid,id,"0");
                                    String assess = "false";
                                    if (db.checkDatas("tbl_save_assessment_header", "assessheadid", hid)){
                                        countassess++;
                                        assess = db.get_tbl_assessment_header_assess(EvaluationActivity.appid,uid,id,"0");
                                    }
                                    list.add(new Headers(id,desc, assess,"",""));
                                    get_showassessment(id,count);
                                }
                                hAdapter.notifyDataSetChanged();
                            }else{

                            }
                            Log.d("head",obj.getJSONArray("head").toString());
                            if(countassess == list.size()){
                                Log.d("countassess","true");
                                btngenerate.setVisibility(View.VISIBLE);
                            }else{
                                btngenerate.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //get_parts_offline();

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

    private void get_showassessment(final String id,final String count){
        String url = Urls.evaluationshowassessment+EvaluationActivity.appid+"/"+count+"/"+id;
        Log.d("eshowassessment",url);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.evaluationshowassessment+EvaluationActivity.appid+"/"+count+"/"+id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("eresponsess",response);

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.getJSONObject("data");
                            JSONArray head = obj.getJSONArray("head");
                            String sid = db.getsid(EvaluationActivity.appid,"",id);
                            if (db.checkDatas("tbl_show_assessment", "sid",sid)){
                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_show_assessment", ucolumns, udata, "sid",sid)) {
                                    Log.d("tbl_eshow_assessment", "update");
                                } else {
                                    Log.d("tbl_eshow_assessment", "not update");
                                }
                            }else{
                                String[] dcolumns = {"json_data", "uid", "appid","id","monid","count"};
                                String[] datas = {response, uid,EvaluationActivity.appid,id,"",count};
                                if (db.add("tbl_show_assessment", dcolumns, datas, "")) {
                                    Log.d("tbl_eshow_assessment", "added");
                                } else {
                                    Log.d("tbl_eshow_assessment", "not added");
                                }
                            }
                            //Log.d("head",head.toString());

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
                Log.d("params",params.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    private void get_parts_online(){
        Log.d("getcount","execute");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.getcount+EvaluationActivity.appid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            count = obj.getJSONObject("original").getInt("nextCount")+"";
                            part(obj.getJSONObject("original").getInt("nextCount")+"");

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

    private Image getDOH(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("doh.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(80,80);

        }
        catch(IOException ex)
        {

        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return image;
    }

    private Image getCheckMark(){
        Image image = null;
        try {
            // get input stream
            InputStream ims = getAssets().open("checks.png");
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

    @Override
    public void onClick(View view) {
        try {
            createPdfWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
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

    private Paragraph createParagraph(String text){
        Paragraph par = new Paragraph(text);
        par.setSpacingBefore(10f);
        par.setSpacingAfter(10f);

        return par;
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
                dialog = ProgressDialog.show(EvaluationPart.this, "",
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
        cell.setBorder(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }

        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"evaluation.pdf");

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

        //add doh icon
        PdfPCell doh = new PdfPCell(getDOH());
        doh.setPadding(10);
        doh.setVerticalAlignment(Element.ALIGN_CENTER);
        doh.setHorizontalAlignment(Element.ALIGN_CENTER);
        doh.setBorder(0);

        //add doh icon
        PdfPCell checkmark = new PdfPCell(getCheckMark());
        checkmark.setPadding(10);
        checkmark.setVerticalAlignment(Element.ALIGN_CENTER);
        checkmark.setHorizontalAlignment(Element.ALIGN_CENTER);
        checkmark.setBorder(0);

        //Document document = new Document(pagesize);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(5,5,5,5);
        PdfWriter.getInstance(document, output);
        document.open();
        float [] pointColumnWidths = {350F, 800F,100F};
        Log.d("count",count);

        //header
        PdfPTable header = new PdfPTable(pointColumnWidths);
        header.setTotalWidth(600);
        header.setLockedWidth(true);
        header.addCell(doh);
        header.addCell(createCell("Republic of the Philippines\nDepartment of Health\nHEALTH FACILITIES AND SERVICES REGULATORY BUREAU",0,0,0,false,""));
        header.addCell(createCell(" ",10,0,0,true,""));
        document.add(header);
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        //title
        Paragraph title = new Paragraph("CHECKLIST FOR REVIEW OF FLOOR PLANS",fontbold);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateandTime = sdf.format(new Date());
        //DETAILS
        float [] columnWidths = {450F, 400F,400F};
        PdfPTable details = new PdfPTable(columnWidths);
        details.addCell(createCell("Name of Health Facility:",3,0,0,false,""));
        details.addCell(createCell(EvaluationActivity.faclityname,3,0,2,false,""));
        details.addCell(createCell("Address:",3,0,0,false,""));
        details.addCell(createCell(address,3,0,2,false,""));
        details.addCell(createCell("Date:",3,0,0,false,""));
        details.addCell(createCell(currentDateandTime,3,0,0,false,""));
        String r = "";
        Paragraph revision = new Paragraph();
        if(count.equals("1")){
            revision.add(new Phrase("Review :1st"));
            revision.add(new Chunk(getCheckMark(), 0, 0));
            revision.add(new Phrase("2nd_ 3rd_"));
        }else if(count.equals("2")){
            revision.add(new Phrase("Review :1st_  2nd"));
            revision.add(new Chunk(getCheckMark(), 0, 0));
            revision.add(new Phrase("3rd_"));
        }else if(count.equals("3")){
            revision.add(new Phrase("Review :1st_  2nd_  3rd"));
            revision.add(new Chunk(getCheckMark(), 0, 0));
        }
        PdfPCell rev = new PdfPCell(revision);
        rev.setPadding(3);
        rev.setBorder(0);
        details.addCell(rev);
        document.add(details);
        //
        //answer
        PdfPTable body = new PdfPTable(1);
        body.setTotalWidth(800);


        Paragraph detanswer = new Paragraph();
        String h3id = "";
        Cursor c = db.get_tbl_assesscombinedptc_res(EvaluationActivity.appid);
        if(c != null && c.getCount()>0){
            Log.d("ptc","true");
            c.moveToFirst();
            while(!c.isAfterLast()){
                String choice = c.getString(c.getColumnIndex("evaluation"));
                String remarks = c.getString(c.getColumnIndex("remarks"));
                String assessname = c.getString(c.getColumnIndex("assessmentName"));
                String sub = c.getString(c.getColumnIndex("sub"));
                String h3headid = c.getString(c.getColumnIndex("asmtH3ID_FK"));
                String h3headback = c.getString(c.getColumnIndex("h3name"));
                if(!h3id.equals(h3headid)){
                    h3id = h3headid;
                    detanswer = new Paragraph();
                    detanswer.add(new Phrase(Html.fromHtml(h3headback).toString().replaceAll("\n", "").trim()));
                    detanswer.setSpacingBefore(12);
                    detanswer.setSpacingAfter(12);
                    Log.d("assessname",h3headback);
                    document.add(detanswer);
                }

                if(sub.equals("null")){
                    detanswer = new Paragraph();
                    if(choice.equals("1")){
                        detanswer.add(new Chunk(getCheck(), 0, 0));
                    }else{
                        detanswer.add(new Chunk(getWrong(), 0, 0));
                    }
                    detanswer.add(new Phrase(Html.fromHtml(assessname).toString().replaceAll("\n", "").trim()));
                    detanswer.setIndentationLeft(75);
                    detanswer.setSpacingBefore(12);
                    detanswer.setSpacingAfter(12);
                    Log.d("assessname",assessname);
                    document.add(detanswer);
                    if(!remarks.equals("")){
                        detanswer = new Paragraph();
                        detanswer.add(new Phrase("Remarks: "+Html.fromHtml(remarks).toString().replaceAll("\n", "").trim()));
                        detanswer.setIndentationLeft(95);
                        detanswer.setSpacingBefore(12);
                        detanswer.setSpacingAfter(12);
                        document.add(detanswer);
                    }
                }else{
                    detanswer = new Paragraph();
                    if(choice.equals("1")){
                        detanswer.add(new Chunk(getCheck(), 0, 0));
                    }else{
                        detanswer.add(new Chunk(getWrong(), 0, 0));
                    }
                    detanswer.add(new Phrase(Html.fromHtml(assessname).toString().replaceAll("\n", "").trim()));
                    detanswer.setIndentationLeft(95);
                    detanswer.setSpacingBefore(12);
                    detanswer.setSpacingAfter(12);
                    Log.d("assessname",assessname);
                    document.add(detanswer);
                    if(!remarks.equals("")){
                        detanswer = new Paragraph();
                        detanswer.add(new Phrase("Remarks: "+Html.fromHtml(remarks).toString().replaceAll("\n", "").trim()));
                        detanswer.setIndentationLeft(115);
                        detanswer.setSpacingBefore(12);
                        detanswer.setSpacingAfter(12);
                        document.add(detanswer);
                    }
                }
                c.moveToNext();
            }
            //document.add(body);
        }
        Paragraph comment = new Paragraph();
        comment.add(new Phrase("COMMENTS"));
        comment.setSpacingBefore(12);
        comment.setSpacingAfter(12);

        Cursor rec = db.get_tbl_assessrecommend(EvaluationActivity.appid);
        if(rec != null && rec.getCount()>0){

            document.add(comment);
            rec.moveToFirst();
            while(!rec.isAfterLast()){
                String noted = rec.getString(rec.getColumnIndex("details"));
                //textarea
                PdfPTable textarea = new PdfPTable(1);
                PdfPCell text = new PdfPCell();
                text.setPadding(100);
                text.setPhrase(new Phrase(noted));
                text.setVerticalAlignment(Element.ALIGN_TOP);
                text.setHorizontalAlignment(Element.ALIGN_LEFT);
                textarea.addCell(text);
                document.add(textarea);
                rec.moveToNext();
            }
        }

        Paragraph footer = new Paragraph();
        footer.add(new Phrase("Evaluated By: "+userfullname));
        footer.setSpacingBefore(12);
        footer.setSpacingAfter(12);
        document.add(footer);

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
