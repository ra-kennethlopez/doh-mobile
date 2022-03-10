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
import android.support.annotation.Nullable;
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
import com.example.pc.doh.Model.PersonnelPage;
import com.example.pc.doh.Model.UserModel;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationHeadersActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnsummary;
    public static String asmt2l_id;
    List<Headers> list = new ArrayList<>();
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluationheaders);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        btnsummary = findViewById(R.id.btnshowsummary);

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
                Intent assestment = new Intent(EvaluationHeadersActivity.this, AssestmentDetailsActivity.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(EvaluationHeadersActivity.this);
            }
        });

        getSupportActionBar().setTitle(EvaluationActivity.code);
        Log.d("evaluationheader","true");
        headerrv = findViewById(R.id.headerrv);
        hAdapter = new HeadersAdapter(this, list);
        headerrv.setLayoutManager(new LinearLayoutManager(this));
        headerrv.setAdapter(hAdapter);
        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                id = list.get(position).getAsmt2l_id();
                Intent assess = new Intent(getApplicationContext(), evaluatedetails.class);
                startActivity(assess);

            }
        });

        if (checker.checkHasInternet()) {
            get_specific_assessment_online();
        } else {
            get_specific_assessment_offline();
        }

        btnsummary.setOnClickListener(this);
    }

    public void get_specific_assessment_offline() {
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Cursor det = db.get_item("tbl_assessment_headers", "appid", EvaluationActivity.appid);
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
                            if (db.check_has_save_assessments(EvaluationActivity.appid)) {
                                //change the get_json_data_tbl_save_assessment_headers(uid,HomeActivity.appid)
                                String header = db.get_json_tbl_save_assessment_header(EvaluationActivity.appid);
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
                                list.add(new Headers(asmt2l_id, asmt2l_desc, assesscheckifcomplete,"",""));
                            } else {

                                list.add(new Headers(asmt2l_id, asmt2l_desc, "false","",""));
                            }


                        }
                        if (list.size() == 0) {
                            TextView lbl = findViewById(R.id.lblheadmessage);
                            lbl.setVisibility(View.VISIBLE);
                        }

                    }

                    if (headers.getBoolean("hasNull")) {
                        //change the check_has_save_assessment(String uid,String appid)
                        if (db.check_has_save_assessment(uid, EvaluationActivity.appid)) {
                            //change the get_json_data_tbl_save_assessment_headers(uid,HomeActivity.appid)  not3e
                            String assesscheckifcomplete = "false";
                            String other = "OTHERS";
                            String header = db.get_json_data_tbl_save_assessment_headers(uid, EvaluationActivity.appid);
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

                            list.add(new Headers("OTHERS", "OTHERS", assesscheckifcomplete,"",""));
                            get_data_headers_details("OTHERS", EvaluationActivity.appid);
                        } else {
                            list.add(new Headers("OTHERS", "OTHERS", "false","",""));
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
        StringRequest request = new StringRequest(Request.Method.GET, Urls.getevaluation + uid+"/"+EvaluationActivity.appid + "/" + EvaluationActivity.type,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("evaluateresponse",response);
                        try {
                            boolean checkifcomplied = false;
                            int cheader = 0;
                            int checkheader = 0;
                            //
                            JSONObject obj = new JSONObject(response);
                            if(obj.has("status")){
                                if(!obj.getString("status").equals("error")){

                                }else{
                                    Toast.makeText(EvaluationHeadersActivity.this,"No Data Available",Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                String[] columns = {"json_data"};
                                String[] data = {response};
                                if (db.checkDatas("tbl_assessment_headers", "appid", EvaluationActivity.appid)) {

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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(EvaluationHeadersActivity.this);
                                    builder.setTitle("DOHOLRS");
                                    builder.setMessage("Successfully Save Data");
                                    builder.setNeutralButton("OK",null);
                                    AlertDialog dialog = builder.create();
                                    dialog.setIcon(R.drawable.doh);
                                    dialog.show();



                                }

                                JSONObject headers = obj.getJSONObject("headers");
                                if (headers.length() > 0) {
                                    for (int i = 0; i < headers.length() - 1; i++) {
                                        JSONObject items = headers.getJSONObject(i + "");
                                        Log.d("items", items.toString());
                                        String asmt2l_id = items.getString("asmt2l_id");
                                        String asmt2l_desc = items.getString("asmt2l_desc");


                                        get_data_headers_details(asmt2l_id, EvaluationActivity.appid);



                                        String assesscheckifcomplete = "false";
                                        if (db.check_has_save_assessment(uid, EvaluationActivity.appid)) {
                                            String header = db.get_json_data_tbl_save_assessment_headers(uid, EvaluationActivity.appid);
                                            JSONArray head = new JSONArray(header);
                                            for (int h = 0; h < head.length(); h++) {
                                                String name = head.getString(h);
                                                JSONObject headname = new JSONObject(name);

                                                if (asmt2l_id.equals(headname.getString("name"))) {
                                                    Log.d("compiled","true");
                                                    checkifcomplied = true;
                                                    assesscheckifcomplete = "true";
                                                    checkheader++;
                                                    break;
                                                } else {
                                                    Log.d("compiled","false");
                                                    checkifcomplied = false;
                                                }

                                            }
                                            cheader++;
                                            list.add(new Headers(asmt2l_id, asmt2l_desc, assesscheckifcomplete,"",""));
                                        } else {

                                            list.add(new Headers(asmt2l_id, asmt2l_desc, "false","",""));
                                        }


                                    }
                                    if (list.size() == 0) {
                                        TextView lbl = findViewById(R.id.lblheadmessage);
                                        lbl.setVisibility(View.VISIBLE);
                                    }

                                }


                                if(cheader !=0){
                                    if (cheader == checkheader) {
                                        btnsummary.setVisibility(View.VISIBLE);
                                    }
                                }



                                hAdapter.notifyDataSetChanged();
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
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    public void get_data_headers_details(final String id, final String appid) {
        final ArrayList<PersonnelPage> temp = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Urls.evaluate + uid+"/"+EvaluationActivity.appid + "/" + EvaluationActivity.type + "/" + id + "?uid=" + uid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("evaluateeach",response);

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
                dialog = ProgressDialog.show(EvaluationHeadersActivity.this, "",
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


    private void createPdf() throws FileNotFoundException, DocumentException {
        int count = 0;


        //Log.d("filenames",f);


        File docsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }



        pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"evaluation.pdf");

        Rectangle pagesize = new Rectangle(612, 861);


        //Document document = new Document(pagesize);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(pagesize);
        document.setMargins(30,30,5,5);
        PdfWriter.getInstance(document, output);
        document.open();
        Font fontbold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLD);


        PdfPCell imgcell = new PdfPCell(getCheck());
        imgcell.setPadding(10);
        imgcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgcell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
        PdfPTable table = new PdfPTable(1);
        table.addCell(imgcell);

        Paragraph title = new Paragraph("1. PHYSICAL PLANT",fontbold);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        Paragraph details = new Paragraph();

        Paragraph details1 = new Paragraph(" 1.1 Administrative Service");
        details1.setIndentationLeft(50);
        Paragraph details2 = new Paragraph(" 1.1.1 Lobby");
        details2.setIndentationLeft(60);
        ///condition
        String[] det = {"1.1 Administrative Service","1.1.1 Lobby ","1.1.1 Waiting Area","1.1.2 Information and Reception Area and Admintting Section",
                         "1.1.3 Public Toilet (Male/Female/PWD)","1.1.4 Staff Toilet",
                         //second
                         "1.1.2 Business Office",
                         "1.1.3 Medical Records Office",
                         "1.1.4 Prayer Area/Room",
                         "1.1.5 Office of the Chief of Hospital",
                         "1.1.6 Laundry* and Linen Section",
                         "1.1.7 Maintenance and Housekeeping Section*",
                         "1.1.8 Parking Area for Transport Vehicle(Ambulance)",
                         "1.1.9 Supply Room",
                         "1.1.10 Waste Holding Room",
                         //end of second
                         "1.1.11 Dietary",
                         //third
                         "1.1.11.1 Dietitian Area",
                         "1.1.11.2 Supply Receiving Area*",
                         "1.1.11.3 Food Preparation Area",
                         "1.1.11.4 Cooking and Baking Area*",
                         "1.1.11.5 Cold and Dry Storage Area*",
                         "1.1.11.6 Serving and Food Assesmbly Area",
                         "1.1.11.7 Washing Area",
                         "1.1.11.8 Garbage Disposal Area",
                         "1.1.11.9 Dining Area",
                         "1.1.11.10 Toilet",
                         "1.1.12 Cadaver Holding Room",
                         "1.2 Clinical Service",
                         "1.2.1 Emergency Room",
                         "1.2.1.1 Waiting Area",
                         "1.2.11.2 Toilet",
                         "1.2.11.3 Nurses' Station with Work Area with Lavatory/Sink",
                         "1.2.11.4 Minor Operating Room/Surgical Area",
                         "1.2.11.5 Examination and Treatment Area with Lavatory/Sink",
                         "1.2.11.6 Observation Area",
                         "1.2.11.7 Equipment and Supply Storage Area",
                         "1.2.11.8 Wheeled Stretcher Area",
                         "1.2.11.9 Dining Area",
                         "1.2.11.10 Toilet",
                         "1.2.2 Outpatient Department (Separate from ER Complex)",
                         "1.2.2.1 Waiting Area",
                         "1.2.2.2 Toilet (Male/Female/PWD)",
                         "1.2.2.3 Admitting and Records Area",
                         "1.2.2.4 Examination and Treatment Area with Lavatory/Sink (OB, Medicine, Pedia, Surgery, Dental-optional)",
                         "1.2.3 Surgical and Obstetrical Service",
                         "1.2.3.1 Major Operating Room",
                         "1.2.3.2 Labor Room with Toilet",
                         "1.2.2.3 Admitting and Records Area",
                         "1.2.3.4 Recovery Room (To provide additional Area)",
                         "1.2.3.5 Sub-sterilizing Area /Work Area",
                         "1.2.3.6 Sterile Instrument, Supply and Storage Area",
                         "1.2.3.7 Scrub-up Area",
                         "1.2.3.8 Clean-up Area",
                         "1.2.3.9 Dressing-Room",
                         "1.2.3.10 Toilet",
                         "1.2.3.11 Nurses' station with Work Area",
                         "1.2.3.12 Wheeled Stretcher Area",
                         "1.2.3.13 Janitor's Closet with mop sink",
                         "1.2.4 Nursing Unit",
                         "1.2.4.1 Patient's Room with Toilet",
                         "1.2.4.2 Isolation Room with Toilet and Ante Room with sink, PPE Rack and Hamper",
                         "1.2.4.3 Nurses Station with Medication Area with Lavatory/Sink",
                         "1.2.4.4 Treatment Area",
                         "1.2.5 Central Sterilizing and Supply Room",
                         "1.2.5.1 Receiving and Cleaning Area",
                         "1.2.5.2 Inspection and Packaging Area",
                         "1.2.5.3 Sterilizing Room",
                         "1.2.5.4 Storage and Releasing Area",
                         "1.3 Nursing Service",
                         "1.3.1 Office of Chief Nurse",
                         "1.4 Ancilliary Service",
                         "1.4.1 Secondary Clinical Laboratory with Blood Station",
                         "1.4.1.1 Clinical Work Area with Lavatory/Sink (min. Floor Area: 20.00 sq.m).",
                         "1.4.1.2 Pathologist Area",
                         "1.4.1.3 Toilet",
                         "1.4.1.4 Extraction Area Separate from Clinical Lab. Work Area",
                         "1.4.2 Radiology - 1st Level",
                         "1.4.2.1 X-Ray Room with Control Booth, Dressing Area and Toilet",
                         "1.4.2.2 Dark Room",
                         "1.4.2.3 Film File and Storage Area",
                         "1.4.2.4 Radiologist Area",
                         "1.4.3 Pharmacy with work counter and sink",
                         "2.1 Floor plans properly identified and completely labeled",
                         "2.2 Conforms to applicable codes as part of normal professional service",
                         "2.2.1 Exits restricted to the following types: door leading directly outside the building, interior stair, ramp and exterior stair.",
                         "2.2.1.1 Minimum of two(2) exits, remote from each other, for each floor of the building",
                         "2.2.1.2 Patient Corridors and ramps for ingress and egress at least 2.44 meters in clear and onobstructed width",
                         "2.2.1.3 Exits terminate directly at an open space to the outside of the building",
                         "2.2.1.4 Minimum of one(1) toilet on each floor accessibe to the disabled",
                         "2.3 Meets prescribed functional programs",
                         "2.3.1 Main Entrance of the hospital directly accessible from public road",
                         "2.3.2 Ramp or elevator for clinical, Nursing and ancilliary service located on the upper floor",
                         "2.3.3 Administrative Service",
                         "2.3.3.1 Business office located near the entrance of the hospital",
                         "2.3.4 Emergency Service",
                         "2.3.4.1 Located in the ground floor to ensure easy access for patients",
                         "2.3.4.2 Separate Entrance to the emergency",
                         "2.3.4.3 Ramp for wheelchair access(with clear with of at least 1.22m or 4.ft)",
                         "2.3.4.4 Easily accessible to the clinical and ancilliary services (laboratory,radiology,pharmacy,operating room)",
                         "2.3.4.5 Nurse station located to permit observation of patient and control of access to entrance, waiting area, and treatment area",
                         "2.3.5 Outpatient Department",
                         "2.3.5.1 Located near the main entrance of the hospital to ensure easy access for patients",
                         "2.3.5.2 Separate toilets for patients and staff (Male/Female/PWD)",
                         "2.3.6 Surgical and Obstetrical Service",
                         "2.3.6.1 Located and arranged to prevent non-related traffic through the suite",
                         "2.3.6.2  Operating room and delivery room located as remote as practicable from the entrance to the suite to reduce traffic and provide greater asepsis",
                         "2.3.6.3 Operating room and delivery room arranged to prevent staff and patients to travel from one area to the other area",
                         "2.3.6.4 Dressing room arranged to avoid exposure to dirty areas after changing to surgical garments",
                         "2.3.6.5 Nurse station located to permit visual observation of patient and movement into the suite",
                         "2.3.6.6 Scrub-up area recessed into an alcove or other open space out of the main traffic",
                         "2.3.6.7 Sub-sterilizing are shall be provided and shall be accessible from the Operation Room and Delivery Room",
                         "2.3.7 Nursing Service",
                         "2.3.7.1 Nurse station located and designed to allow visual observation of patient and movement into the nursing unit",
                         "2.3.7.2 Nurse station provided in all nursing units of the hospital with a ratio of at least one(1) nurse station for every thirty-five(35) bends",
                         "2.3.7.3 Toilet immediately accessible from each room in a nursing unit",
                         "2.3.7.4 Separate rooms with toilets for male and female patients",
                         "2.3.8 Dietary, maintenance and other non patient contact services or located in areas away from normal traffic within the hospital, or located in separate buildings within the hospital premises",
                         "2.3.9.1 The dietary service shall be away from morgue with atleast 25-meter distance"



                          };
        document.add(title);

        for(int i=0;i<det.length;i++){
            Log.d("items",det[i]);
            if(i==0){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i==1){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>1 && i<6){
                //first
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);

            }else if(i>5 && i<15){
                //second
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i==15){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>15 && i<26){
                //third
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==26){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i==27){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i==28){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>28 && i<39){
                //fourth
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==39){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>39 && i<44){
                //fifth
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==44){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>44 && i<58){
                //sixth
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==58){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>58 && i<63){
                //seventh
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==63){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>63 && i<68){
                //eight
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==68){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i==69){
                //nine
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i==70){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i==71){

                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>71 && i<76){
                //ten
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==76){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>76 && i<81){
                //
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==81){
                //
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);

            }else if(i==82){
                //
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i==83){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(50);
            }else if(i>83 && i<89){
                //
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i==89){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(60);
            }else if(i>89 && i<92){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==92){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==93){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }else if(i==94){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i>94 & i<100){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }else if(i==100){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i>100 && i<103){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }else if(i==103){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i>103 && i<111){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }else if(i==111){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i>111 && i<116){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }else if(i==116){
                details = new Paragraph();
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(75);
            }else if(i==117){
                details = new Paragraph();
                details.add(new Chunk(getCheck(), 0, 0));
                details.add(new Phrase(det[i]));
                details.setIndentationLeft(85);
            }
            document.add(details);
            if(i==81){
                //title2
                Paragraph title2 = new Paragraph("2. PLANNING AND DESIGN",fontbold);
                title2.setSpacingBefore(10f);
                title2.setSpacingAfter(10f);
                document.add(title2);
            }

            Log.d("index",i+"");
        }




        document.close();

        previewPdf();

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



    private Image getCheck(){
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
