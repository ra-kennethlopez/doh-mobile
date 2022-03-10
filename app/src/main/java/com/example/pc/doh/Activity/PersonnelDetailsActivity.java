package com.example.pc.doh.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.example.pc.doh.Adapter.AssessQuestionAdapter;
import com.example.pc.doh.Adapter.PersonnelPageAdapter;
import com.example.pc.doh.BulletTextUtil;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;
import com.example.pc.doh.Model.PersonnelPage;
import com.example.pc.doh.Model.Save;
import com.example.pc.doh.Model.Srvasmtcols;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonnelDetailsActivity extends AppCompatActivity {
    ArrayList<PersonnelPage> list = new ArrayList<>();
    PersonnelPageAdapter adapter;
    ViewPager viewPager;
    TextView disp1,disp2,disp3;
    EditText remarks;

    private String uid;
    int pos = 0;
    int page = 0;
    static int spage = 0;
    DatabaseHelper db;
    InternetCheck checker;
    int jdata;
    Menu menu;
    RecyclerView rv;
    AssessQuestionAdapter questionAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facilitydetils);

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }


        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        UserModel user = SharedPrefManager.getInstance(this).getUser();
        disp1 = findViewById(R.id.txtdisp1);
        disp2 = findViewById(R.id.txtdisp2);
        disp3 = findViewById(R.id.txtdisp3);
        viewPager = findViewById(R.id.faciviewpager);
        //rv = findViewById(R.id.qlist);

        questionAdapter = new AssessQuestionAdapter(this,list);
        adapter = new PersonnelPageAdapter(this,list);
        //rv.setLayoutManager(new LinearLayoutManager(this));
        //rv.setAdapter(questionAdapter);
        uid = user.getId();


        get_data_offline();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assestment = new Intent(PersonnelDetailsActivity.this,AssessmentHeaderActvitiy.class);
                startActivity(assestment);
                Animatoo.animateSlideRight(PersonnelDetailsActivity.this);
            }
        });

        getSupportActionBar().setTitle(HomeActivity.code);

        //changes
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {


            }

            @Override
            public void onPageSelected(int i) {
                save_assessment_results(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {


            }
        });

//        viewPager. setPageTransformer(true, new VerticalPageTransformer());



    }




    private void save_assessment_results(int i){

        page = i + 1;

        View view =viewPager.findViewWithTag("remarks" + pos );

        if(page == list.size()){
            menu.getItem(0).setVisible(true);
        }else{
            menu.getItem(0).setVisible(false);
        }



        if(view!=null){
            EditText txtr = view.findViewById(R.id.remark);

            try {
                JSONObject obj = new JSONObject();
                JSONArray result = new JSONArray();
                JSONObject head = new JSONObject();
                String answer = "";

                JSONArray srvasmtcol = new JSONArray(list.get(pos).getSrvasmt_col());


                for(int index=0;index<list.get(pos).getSrvasmtcolsList().size();index++){
                    if(view.findViewById(index+pos+100)!=null){

                        RadioGroup r = view.findViewById(index+pos+100);
                        int selectedId = r.getCheckedRadioButtonId();
                        Log.d("selectedid",selectedId+"");
                        if(selectedId == 0){
                            list.get(pos).getSrvasmtcolsList().get(index).setAnswer("yes");
                            answer = "true";
                        }else if(selectedId == -1){
                            Toast.makeText(this,"Please choose your answer",Toast.LENGTH_SHORT).show();
                            viewPager.setCurrentItem(pos, false);
                            return;
                        }else if(selectedId == 1){
                            list.get(pos).getSrvasmtcolsList().get(index).setAnswer("no");
                            if(TextUtils.isEmpty(txtr.getText().toString())){
                                txtr.setError("Please Enter your Remarks. Thank You");
                                viewPager.setCurrentItem(pos, false);
                                return;
                            }
                            answer = "false";
                        }


                        //Create json object for saving data
                        JSONArray jsonArray = new JSONArray();
                        JSONObject ans = new JSONObject();
                        ans.put("name",list.get(pos).getSrvasmtcolsList().get(index).getName());
                        ans.put("result",answer);
                        jsonArray.put(ans);
                        head.put(srvasmtcol.getString(index),jsonArray);
                        result.put(head);

                    }
                }
                Log.d("textremarks",list.get(pos).getHasRemarksName());
                list.get(pos).setRemarks(txtr.getText().toString());
                obj.put("result",result);




                JSONObject remarksobj = new JSONObject();
                JSONArray resultremarks = new JSONArray();
                JSONObject headremarks = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject ans = new JSONObject();
                ans.put("name",list.get(pos).getHasRemarksName());
                ans.put("result",txtr.getText().toString());
                jsonArray.put(ans);
                headremarks.put("Remarks",jsonArray);
                resultremarks.put(headremarks);
                remarksobj.put("result",resultremarks);
                String[]columns = {"uid","appid","asmnt_id","jindex","answers_json_data","remarks","srvasmt_col"};
                String[]data = {uid,HomeActivity.appid,AssessmentHeaderActvitiy.id,pos+"",obj.toString(),remarksobj.toString(),list.get(pos).getSrvasmt_col()};

                if(db.checkJoinedData(HomeActivity.appid,uid,AssessmentHeaderActvitiy.id,pos+"")){
                    Log.d("checkdatas","found");
                    String resid = db.get_tbl_SrvasmtcolsList_Result_id(HomeActivity.appid,uid,AssessmentHeaderActvitiy.id,pos+"");
                    Log.d("resid",resid);
                    if(db.update("tbl_SrvasmtcolsList_Result",columns,data,"resid",resid)){
                        Log.d("updatedata","update");
                    }else{
                        Log.d("updatedata","not update");
                    }
                }else{
                    Log.d("checkdatas","not found");
                    if(db.add("tbl_SrvasmtcolsList_Result",columns,data,"")){
                        Log.d("tbl_assessment_details","added");
                    }else{
                        Log.d("tbl_assessment_details","not added");
                    }
                }


                Toast.makeText(PersonnelDetailsActivity.this,page+" out of "+list.size()+"",Toast.LENGTH_SHORT).show();



            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        pos = i;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assessdetmenu, menu);
        this.menu = menu;

        if(list.size() == 1){
            menu.getItem(0).setVisible(true);
        }else{
            menu.getItem(0).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
          //String appid,String uid)
        if (id == R.id.action_submit) {
            //get the json data in tbl_assessment_details
            //View view =viewPager.findViewWithTag("remarks" + pos );
            if(list.size() == 1){

                //save last data in assessment
                save_assessment_results(pos);
                //save all results in assessment
                save_all_results();
            }else if(list.size() == page){
                Log.d("listsize",list.size()+"");
                //save last data in assessment
               /* Log.d("pos",pos+"");*/
                save_assessment_results(pos);
                //save all results in assessment
                save_all_results();

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void save_all_results(){
        //change the get_json_data_tbl_tbl_assessment_details(uid,AssessmentHeaderActvitiy.id,HomeActivity.appid);
        String json_data = db.get_json_data_tbl_tbl_assessment_details1(AssessmentHeaderActvitiy.id,HomeActivity.appid);
        Cursor res = db.get_all_tbl_SrvasmtcolsList_Result_remarks(uid,HomeActivity.appid,AssessmentHeaderActvitiy.id);
        if(res!=null && res.getCount()>0){
            res.moveToFirst();
            try {

                ///appid json
                JSONObject app = new JSONObject(json_data);
                JSONObject appdata = app.getJSONObject("AppData");
                //get the filenames string
                String files = app.getString("filenames");
                Log.d("filenamesfilenames",files);
                JSONArray filenames = new JSONArray(files);

                String[] filename = new String[filenames.length()];
                for(int f=0;f<filenames.length();f++){
                    filename[f] = filenames.getString(f);
                }

                String fname = implode(", ", filename);
                int is=0;
                ///create json for saving offline assessment results
                JSONArray data = new JSONArray();
                JSONObject items = new JSONObject();
                JSONArray result = new JSONArray();

                Map<String,String> mapitems = new HashMap<String,String>();

                Map<String,String> maprs = new HashMap<String,String>();

                List<Map<String,String>> litems = new ArrayList<>();
                Log.d("uiduid",appdata.getString("uid"));

                JSONObject rs = new JSONObject();
                rs.put("filename",fname);
                rs.put("assessor",app.getString("assessor"));

                mapitems.put("header","'"+AssessmentHeaderActvitiy.id+"'");
                mapitems.put("monType","'"+HomeActivity.monType+"'");
                mapitems.put("appID","'"+HomeActivity.appid+"'");
                //changes
                mapitems.put("org","'"+app.getString("org")+"'");
                mapitems.put("facilityname","'"+appdata.getString("facilityname")+"'");
                mapitems.put("filename","'"+fname+"'");
                mapitems.put("assessor","'"+app.getString("assessor")+"'");


                while (!res.isAfterLast()){
                    Log.d("resres",is++ + "");
                    String answer = res.getString(res.getColumnIndex("answers_json_data"));
                    String cols = res.getString(res.getColumnIndex("srvasmt_col"));
                    String remarks = res.getString(res.getColumnIndex("remarks"));

                    ///get the result of clm001
                    JSONObject ans = new JSONObject(answer);
                    JSONArray r = ans.getJSONArray("result");
                    JSONArray jsonArraycols = new JSONArray(cols);
                    if(r!=null && r.length()>0){
                        ///get the clm
                        JSONObject re = r.getJSONObject(0);
                        Log.d("rrrrrrr",r.length()+"");
                        Log.d("rererer",re.toString());

                        for(int i=0;i<re.length();i++){
                            String clm = re.getString(jsonArraycols.getString(i));
                            Log.d("clmclm",clm);
                            Log.d("clmclmsss",i+"");
                            JSONArray c = new JSONArray(clm);
                            JSONObject cobj = c.getJSONObject(0);
                            rs.put(cobj.getString("name"),cobj.getString("result"));
                            mapitems.put("'"+cobj.getString("name")+"'","'"+cobj.getString("result")+"'");
                        }
                    }


                    //get the remarks
                    JSONObject re = new JSONObject(remarks);
                    JSONArray rre = re.getJSONArray("result");
                    if(rre!=null && rre.length()>0){
                        JSONObject remark = rre.getJSONObject(0);
                        JSONArray arrayres = new JSONArray(remark.getString("Remarks"));
                        JSONObject objremarks = arrayres.getJSONObject(0);
                        String resremarks = "null";
                        if(!objremarks.getString("result").equals("")){
                            resremarks = objremarks.getString("result");
                        }

                        rs.put("'"+objremarks.getString("name")+"'","'"+resremarks+"'");
                        //
                        mapitems.put("'"+objremarks.getString("name")+"'","'"+resremarks+"'");
                    }



                    res.moveToNext();
                }
                result.put(rs);
                ///add the results to json array

                maprs.put(AssessmentHeaderActvitiy.id,mapitems.toString());
                items.put(AssessmentHeaderActvitiy.id,rs);
                data.put(items);

                litems.add(0,mapitems);

                Log.d("jsonarraydata",data.toString());
                JSONArray litemsarray = new JSONArray(litems.toString());
                JSONObject saveassesss = litemsarray.getJSONObject(0);
                Log.d("litemsarray",litemsarray.toString());
                Log.d("saveassess",saveassesss.toString());


                JSONArray jsonArrayhead = new JSONArray();
                JSONObject head = new JSONObject();
                head.put("name",AssessmentHeaderActvitiy.id);
                jsonArrayhead.put(head);




                ///saving result to local storage
                String[] columns = {"uid","appid","headers","apptype","syncstatus","sharestatus","type"};
                String[] datas = {uid,HomeActivity.appid,jsonArrayhead.toString(),HomeActivity.type,"0","0","assessment"};
                ///tbl initial table columns and datas
                String[] initialdata = {uid,HomeActivity.appid,saveassesss.toString(),AssessmentHeaderActvitiy.id,"0"};
                String[] initialcol = {"uid","appid","initial_json_data","header","status"};

                ///create alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("DOHOLRS");
                builder.setMessage("Successfully Save Assessment Results");
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent header = new Intent(PersonnelDetailsActivity.this,AssessmentHeaderActvitiy.class);
                        startActivity(header);
                        finish();
                    }
                });

                AlertDialog dialog = builder.create();

               dialog.setIcon(R.drawable.doh);
               //change the check_if_exist_tbl_save_assessment_data
                //check_if_exist_tbl_save_assessment_datas
                if(db.check_if_exist_tbl_save_assessment_data(HomeActivity.appid,uid)){

                    Log.d("checkdatas","found");
                    //String submit_json_data = db.get_json_data_tbl_save_assessment(uid,HomeActivity.appid);
                    //change the get_json_data_tbl_save_assessment_headers

                    /*JSONObject submit = new JSONObject(submit_json_data);


                    String header = db.get_json_data_tbl_save_assessment_headers(uid,HomeActivity.appid);
                    JSONArray aheaders = new JSONArray(header);
                    JSONObject save = new JSONObject();
                    for(int he=0;he<aheaders.length();he++){
                        String name = aheaders.getString(he);
                        JSONObject headname = new JSONObject(name);
                        save.put(headname.getString("name"),submit.getString(headname.getString("name")));
                    }*/


                    /*for(int h=0;h<jsonArrayhead.length();h++){
                        String name = aheaders.getString(h);
                        JSONObject headname = new JSONObject(name);
                        save.put(headname.getString("name"),saveassesss.getString(headname.getString("name")));


                    }*/


                    //changes


                    /*

                    for(int he=0;he<aheaders.length();he++){

                        for(int s=0;s<submit.length();s++){
                            JSONObject item = submit.getJSONObject(0);
                        }
                        *//*String name = aheaders.getString(he);
                        JSONObject headname = new JSONObject(name);
                        //Log.d("JSONObject h",h.getString(headname.getString("name")));
                        Log.d("submit submit",submit.getString(headname.getString("name")));
                        items.put(headname.getString("name"),submit.getString(headname.getString("name")));
                        Log.d("items",items.toString());*//*

                    }*/



                    ///get the json

                    Log.d("saveassess",saveassesss.toString());


                    String headers = db.get_json_data_tbl_save_assessment_header(HomeActivity.appid);
                    JSONArray jsonArray1 = new JSONArray(headers);
                    JSONObject head1 = new JSONObject();
                    head1.put("name",AssessmentHeaderActvitiy.id);
                    jsonArray1.put(head1);
                    Log.d("ehader",jsonArray1.toString());
                    //changes
                    //change the udatas = {uid,HomeActivity.appid,jsonArray1.toString(),HomeActivity.type,"0","0"};
                    String[] ucolumns = {"appid","headers","apptype","syncstatus","sharestatus"};
                    String[] udatas = {HomeActivity.appid,jsonArray1.toString(),HomeActivity.type,"0","0"};
                    String getid = db.getSaveAssessmentId(uid,HomeActivity.appid);
                    if(db.update("tbl_save_assessment",ucolumns,udatas,"saveid",getid)){
                        Log.d("updatedata","update");
                        if(db.add("tbl_initial_save_assessment",initialcol,initialdata,"")){
                            Log.d("tbl_initial","added");
                        }else{
                            Log.d("tbl_initial","added");
                        }
                    }else{
                        Log.d("updatedata","not update");
                    }

                    dialog.show();


                }else{
                    //add

                    Log.d("checkdatas","not found");
                    if(db.add("tbl_save_assessment",columns,datas,"")){
                        Log.d("tbl_save_assessment","added");
                        if(db.add("tbl_initial_save_assessment",initialcol,initialdata,"")){
                            Log.d("tbl_initial","added");
                        }else{
                            Log.d("tbl_initial","added");
                        }
                    }else{
                        Log.d("tbl_save_assessment","not added");
                    }
                    dialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    ///implode comma
    public static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            //data.length - 1 => to not add separator at the end
            if (!data[i].matches(" *")) {//empty string are ""; " "; "  "; and so on
                sb.append(data[i]);
                sb.append(separator);
            }
        }
        sb.append(data[data.length - 1].trim());
        return sb.toString();
    }


    public void get_data_offline(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final LinearLayout layout = findViewById(R.id.assesslayout);
        final ArrayList<PersonnelPage> temp = new ArrayList<>();
        int count = 1;
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        layout.setVisibility(View.GONE);
        Cursor det = db.get_item_json_data("tbl_assessment_details",AssessmentHeaderActvitiy.id,HomeActivity.appid);
        if(det!=null && det.getCount()>0){
            det.moveToFirst();
            while(!det.isAfterLast()){
                String json = det.getString(det.getColumnIndex("json_data"));
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject appdata = obj.getJSONObject("AppData");
                    String facilityname = appdata.getString("facilityname");
                    String name = appdata.getString("streetname") +", "+ appdata.getString("brgyname") + ", "+
                            appdata.getString("cmname") +", "+ appdata.getString("provname") + ", "+appdata.getString("zipcode");
                    String isInspected = "";
                    if(appdata.getString("isInspected") == null){
                        isInspected = "Status: For Inspection";
                    }else{
                        isInspected = "Status: Approved Inspection";
                    }

                    disp1.setText(facilityname);
                    disp2.setText(name);
                    disp3.setText(isInspected);

                    JSONObject joinedData = obj.getJSONObject("joinedData");
                    jdata = joinedData.length();
                    for(int i=0;i<joinedData.length()-1;i++){

                        if (joinedData.has(i+"")) {
                            JSONObject item = joinedData.getJSONObject(i+"");

                           /* Log.d("item",item.toString());*/
                            String title_name = item.getString("title_name");
                            String asmt2l_desc = item.getString("asmt2l_desc");
                            String asmt2_desc = item.getString("asmt2_desc");
                            String asmt2sd_desc = item.getString("asmt2sd_desc");
                            String srvasmt_col = item.getString("srvasmt_col");

                            if(asmt2sd_desc.equals("null")){
                                asmt2sd_desc = "No Details";
                            }

                            int hasRemarks = item.getInt("hasRemarks");
                           /* Log.d("srvasmt_col",srvasmt_col+"");*/
                            List<Srvasmtcols> srvasmtcolsList = new ArrayList<>();
                            JSONArray srvasmtcol = new JSONArray(srvasmt_col);
                          /*  Log.d("srvasmtcolarray",srvasmtcol.toString());
                            Log.d("srvasmtcollength",srvasmtcol.length()+"");*/
                            String jsondata = db.get_tbl_SrvasmtcolsList_Result_json(HomeActivity.appid,uid,AssessmentHeaderActvitiy.id,i+"");
                            String remarks = db.get_tbl_SrvasmtcolsList_Result_remarks(HomeActivity.appid,uid,AssessmentHeaderActvitiy.id,i+"");
                            //changes
                            String initialchoice = "true";
                            String resname = "";
                            String initialRemarks = "";
                           /* Log.d("Remarks",remarks);*/
                            if(!remarks.equals("")){
                                JSONObject dataobj = new JSONObject(remarks);
                                JSONArray jsonArray = dataobj.getJSONArray("result");
                                if(jsonArray!=null && jsonArray.length()>0){
                                    String col = jsonArray.getJSONObject(0).getString("Remarks");
                                    if(!col.equals("")){
                                        JSONArray ans = new JSONArray(col);
                                        initialRemarks = ans.getJSONObject(0).getString("result");
                                    }

                                }else{
                                    Log.d("jsonArray","null");
                                }
                            }

                            for(int s=0;s<srvasmtcol.length();s++){

                                resname = "seq"+item.getString("srvasmt_seq")+"/comp"+item.getString("asmt2_id")+"/count"+count+"/"+"part"+item.getString("facid")+"/headCode"+item.getString("headCode")+"/"+
                                        item.getString("hospitalType")+srvasmtcol.getString(s)+item.getString("asmt2_desc");

                                if(!jsondata.equals("")){
                                    JSONObject dataobj = new JSONObject(jsondata);
                                    JSONArray jsonArray = dataobj.getJSONArray("result");

                                    if(jsonArray!=null && jsonArray.length()>0){
                                        Log.d("jsonArray","not null");
                                        String col = jsonArray.getJSONObject(0).getString(srvasmtcol.getString(s));
                                        JSONArray ans = new JSONArray(col);
                                        Log.d("result",ans.getJSONObject(0).getString("result"));
                                        initialchoice = ans.getJSONObject(0).getString("result");
                                    }else{
                                        Log.d("jsonArray","null");
                                    }


                                }

                                srvasmtcolsList.add(new Srvasmtcols(resname,joinedData.getString(srvasmtcol.getString(s)+"Desc"),joinedData.getString(srvasmtcol.getString(s)+"Type"),initialchoice));
                                count++;
                            }



                            resname = "seq"+item.getString("srvasmt_seq")+"/remarks"+item.getString("asmt2_id")+"/count"+count+"/"+"part"+item.getString("facid")+"/headCode"+item.getString("headCode")+"/"+
                                    item.getString("hospitalType")+item.getString("asmt2_desc");

                            count++;
                            list.add(new PersonnelPage(title_name,asmt2l_desc,asmt2_desc,asmt2sd_desc,srvasmtcolsList,hasRemarks,srvasmt_col,initialRemarks,"",resname));

                        }else{
                            break;
                        }

                    }
                    //changes
                    adapter.notifyDataSetChanged();
                    questionAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                det.moveToNext();
            }
        }
    }

    public void get_data_online(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final LinearLayout layout = findViewById(R.id.assesslayout);
        final ArrayList<PersonnelPage> temp = new ArrayList<>();
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40,165,95), PorterDuff.Mode.SRC_IN);
        layout.setVisibility(View.GONE);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.assess + HomeActivity.appid + "/" + HomeActivity.type + "/" + AssessmentHeaderActvitiy.id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("responsedata",response);
                            String[] columns = {"json_data"};
                            String[] data = {response};
                            if(db.checkDatas("tbl_assessment_details","asmt2l_id",AssessmentHeaderActvitiy.id)){
                                Log.d("checkdatas","found");
                                if(db.update("tbl_assessment_details",columns,data,"asmt2l_id",AssessmentHeaderActvitiy.id)){
                                    Log.d("updatedata","update");
                                }else{
                                    Log.d("updatedata","not update");
                                }

                            }else{
                                Log.d("checkdatas","not found");
                                String[] dcolumns = {"json_data","uid","asmt2l_id"};
                                String[] datas = {response,uid,AssessmentHeaderActvitiy.id};
                                if(db.add("tbl_assessment_details",dcolumns,datas,"")){
                                    Log.d("tbl_assessment_details","added");
                                }else{
                                    Log.d("tbl_assessment_details","not added");
                                }

                            }

                            JSONObject appdata = obj.getJSONObject("AppData");
                            String facilityname = appdata.getString("facilityname");
                            String name = appdata.getString("streetname") +", "+ appdata.getString("brgyname") + ", "+
                                    appdata.getString("cmname") +", "+ appdata.getString("provname") + ", "+appdata.getString("zipcode");
                            String isInspected = "";
                            if(appdata.getString("isInspected") == null){
                                isInspected = "Status: Fon Inspection";
                            }else{
                                isInspected = "Status: Approved Inspection";
                            }

                            disp1.setText(facilityname);
                            disp2.setText(name);
                            disp3.setText(isInspected);

                            JSONObject joinedData = obj.getJSONObject("joinedData");

                            for(int i=0;i<joinedData.length()-1;i++){

                                if (joinedData.has(i+"")) {
                                    JSONObject item = joinedData.getJSONObject(i+"");

                                    Log.d("item",item.toString());
                                    String title_name = item.getString("title_name");
                                    String asmt2l_desc = item.getString("asmt2l_desc");
                                    String asmt2_desc = item.getString("asmt2_desc");
                                    String asmt2sd_desc = item.getString("asmt2sd_desc");
                                    String srvasmt_col = item.getString("srvasmt_col");
                                    if(asmt2sd_desc.equals("null")){
                                        asmt2sd_desc = "No Details";
                                    }
                                    int hasRemarks = item.getInt("hasRemarks");
                                    Log.d("hasRemarks",hasRemarks+"");
                                    List<Srvasmtcols> srvasmtcolsList = new ArrayList<>();
                                    JSONArray srvasmtcol = new JSONArray(srvasmt_col);
                                    Log.d("srvasmtcolarray",srvasmtcol.toString());
                                    Log.d("srvasmtcollength",srvasmtcol.length()+"");
                                    for(int s=0;s<srvasmtcol.length();s++){
                                        Log.d("srvasmtcoldata",srvasmtcol.getString(s)+"Desc");
                                        Log.d(srvasmtcol.getString(s)+"Desc",joinedData.getString(srvasmtcol.getString(s)+"Desc"));
                                        Log.d("srvasmtcoldata",srvasmtcol.getString(s)+"Type");
                                        Log.d(srvasmtcol.getString(s)+"Desc",joinedData.getString(srvasmtcol.getString(s)+"Type"));
                                        srvasmtcolsList.add(new Srvasmtcols("resname",joinedData.getString(srvasmtcol.getString(s)+"Desc"),joinedData.getString(srvasmtcol.getString(s)+"Type"),"yes"));

                                    }
                                    Log.d("srvasmtcollengthafter",srvasmtcolsList.size()+"");
                                    /*String[] columns = {"asmt2l_id","choices","remarks","posid"};
                                    String[] data = {"sample",joinedData.toString(),"sapmle","sample"};
                                    db.add("tbl_assess_save",columns,data,"");*/
                                    list.add(new PersonnelPage(title_name,asmt2l_desc,asmt2_desc,asmt2sd_desc,srvasmtcolsList,hasRemarks,"","","",""));

                                }else{
                                    break;
                                }

                            }

                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        bar.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
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

//    private class VerticalPageTransformer implements ViewPager.PageTransformer {
//
//        @Override
//        public void transformPage(View view, float position) {
//
//            if (position < -1) { // [-Infinity,-1)
//                // This page is way off-screen to the left.
//                view.setAlpha(0);
//
//            } else if (position <= 1) { // [-1,1]
//                view.setAlpha(1);
//
//                // Counteract the default slide transition
//                view.setTranslationX(view.getWidth() * -position);
//
//                //set Y position to swipe in from top
//                float yPosition = position * view.getHeight();
//                view.setTranslationY(yPosition);
//
//            } else { // (1,+Infinity]
//                // This page is way off-screen to the right.
//                view.setAlpha(0);
//            }
//        }
//    }



}
