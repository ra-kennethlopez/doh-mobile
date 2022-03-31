package com.example.pc.doh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String db_doh = "DOHOLRS";


    public static String tbl_assessment = "CREATE TABLE tbl_assessment"
                                            + "(" + "assessment_id" +
                                            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                            " TEXT, " + "uid" +
                                            " TEXT , type TEXT);";
    public static String tbl_monitoring = "CREATE TABLE tbl_monitoring"
                                            + "(" + "monitor_id" +
                                            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                            " TEXT, " + "uid" +
                                            " TEXT);";
    
    public static String tbl_user = "CREATE TABLE tbl_user"
                                    + "(" + "user_id" +
                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                    " TEXT, " + "uid" +
                                    " TEXT, "+"pinpassword"+
                                    " TEXT, "+"islogin"+
                                    " TEXT);";

    public static String tbl_assessment_part =  "CREATE TABLE tbl_assessment_part"
                                                    + "(" + "partid" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, appid TEXT,monid TEXT);";

    public static String tbl_assessment_headerone =  "CREATE TABLE tbl_assessment_headerone"
                                                + "(" + "honeid" +
                                                " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                " TEXT, " + "uid" +
                                                " TEXT, appid TEXT,id TEXT,monid TEXT);";
    public static String tbl_assessment_headertwo =  "CREATE TABLE tbl_assessment_headertwo"
                                                        + "(" + "htwoid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                        " TEXT, " + "uid" +
                                                        " TEXT, appid TEXT,id TEXT);";
    public static String tbl_assessment_headerthree =  "CREATE TABLE tbl_assessment_headerthree"
                                                    + "(" + "hthreeid" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, appid TEXT,id TEXT);";

    public static String tbl_show_assessment =  "CREATE TABLE tbl_show_assessment"
                                                    + "(" + "sid" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, appid TEXT,id TEXT, monid TEXT,count TEXT);";

    public static String tbl_assessment_details = "CREATE TABLE tbl_assessment_details"
                                                    + "(" + "det_id" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, asmt2l_id TEXT, appid TEXT);";

    public static String tbl_monitoring_details = "CREATE TABLE tbl_monitoring_details"
                                                    + "(" + "det_id" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, asmt2l_id TEXT, appid TEXT);";

    public static String tbl_assessment_headers = "CREATE TABLE tbl_assessment_headers"
                                                    + "(" + "headid" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, appid TEXT);";

    public static String tbl_monitoring_headers = "CREATE TABLE tbl_monitoring_headers"
                                                    + "(" + "headid" +
                                                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + "json_data" +
                                                    " TEXT, " + "uid" +
                                                    " TEXT, appid TEXT);";
    public static String tbl_SrvasmtcolsList_Result = "CREATE TABLE tbl_SrvasmtcolsList_Result"
                                                        + "(" + "resid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                        " TEXT, " + "appid" +
                                                        " TEXT, asmnt_id TEXT , jindex TEXT,answers_json_data, remarks TEXT, srvasmt_col TEXT);";

    public static String tbl_MonsrvasmtcolsList_Result = "CREATE TABLE tbl_MonsrvasmtcolsList_Result"
                                                        + "(" + "resid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                        " TEXT, " + "appid" +
                                                        " TEXT, asmnt_id TEXT , jindex TEXT,answers_json_data, remarks TEXT, srvasmt_col TEXT);";

    public static String tbl_save_assessment = "CREATE TABLE tbl_save_assessment"
                                                + "(" + "saveid" +
                                                " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                " TEXT, " + "appid" +
                                                " TEXT,apptype TEXT, headers TEXT, syncstatus TEXT, sharestatus TEXT,type TEXT);";

    public static String tbl_monsave_assessment = "CREATE TABLE tbl_monsave_assessment"
                                                + "(" + "saveid" +
                                                " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                " TEXT, " + "appid" +
                                                " TEXT,apptype TEXT, headers TEXT, syncstatus TEXT, sharestatus TEXT);";

    public static String tbl_initial_save_assessment = "CREATE TABLE tbl_initial_save_assessment"
                                                        + "(" + "initialid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                        " TEXT, " + "appid" +
                                                        " TEXT,initial_json_data TEXT, header TEXT, status TEXT);";

    public static String tbl_moninitial_save_assessment = "CREATE TABLE tbl_moninitial_save_assessment"
                                                            + "(" + "initialid" +
                                                            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                            " TEXT, " + "appid" +
                                                            " TEXT,initial_json_data TEXT, header TEXT, status TEXT);";

    public static String tbl_save_assessment_res =  "CREATE TABLE tbl_save_assessment_res"
                                                        + "(" + "resid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "uid" +
                                                        " TEXT, " + "appid" +
                                                        " TEXT,choice TEXT, remarks TEXT, headerid TEXT,jindex TEXT);";

    public static String tbl_save_assessment_header =  "CREATE TABLE tbl_save_assessment_header"
                                                        + "(" + "assessheadid" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "headerid" +
                                                        " TEXT, " + "headerlevel" +
                                                        " TEXT,assess TEXT, appid TEXT, uid TEXT,monid TEXT);";
    public static String assesscombined =  "CREATE TABLE assesscombined"
                                                        + "(" + "dupID" +
                                                        " INTEGER PRIMARY KEY AUTOINCREMENT, " + "asmtComb_FK" +
                                                        " TEXT, " + "assessmentName" +
                                                        " TEXT,assessmentSeq TEXT, assessmentHead TEXT, asmtH3ID_FK TEXT,"+
                                                        "h3name TEXT,asmtH2ID_FK TEXT,h2name TEXT,asmtH1ID_FK TEXT,h1name TEXT,partID TEXT,"+
                                                        "evaluation TEXT,remarks TEXT,evaluatedBy TEXT,appid TEXT,monid TEXT,epos TEXT,ename TEXT);";
    public static String assesscombinedtemp =  "CREATE TABLE assesscombinedtemp"
            + "(" + "dupID" +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "asmtComb_FK" +
            " TEXT, " + "assessmentName" +
            " TEXT,assessmentSeq TEXT, assessmentHead TEXT, asmtH3ID_FK TEXT,"+
            "h3name TEXT,asmtH2ID_FK TEXT,h2name TEXT,asmtH1ID_FK TEXT,h1name TEXT,partID TEXT,"+
            "evaluation TEXT,remarks TEXT,evaluatedBy TEXT,appid TEXT,monid TEXT,epos TEXT,ename TEXT);";
    public static String assesscombinedptc =  "CREATE TABLE assesscombinedptc"
            + "(" + "dupID" +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "asmtComb_FK" +
            " TEXT, " + "assessmentName" +
            " TEXT,assessmentSeq TEXT, assessmentHead TEXT, asmtH3ID_FK TEXT,"+
                "h3name TEXT,asmtH2ID_FK TEXT,h2name TEXT,asmtH1ID_FK TEXT,h1name TEXT,partID TEXT,parttitle TEXT,"+
            "evaluation TEXT,remarks TEXT,evaluatedBy TEXT,appid TEXT,sub TEXT,isdisplay TEXT,revision TEXT,ename TEXT);";
    public static String assessrecommend = "CREATE TABLE assessrecommend"
                                            + "(" + "reco" +
                                            " INTEGER PRIMARY KEY AUTOINCREMENT, " + "choice" +
                                            " TEXT, " + "details" +
                                            " TEXT,valfrom TEXT, valto TEXT, days TEXT,"+
                                            "monid TEXT,selfassess TEXT,revision TEXT,evaluatedby TEXT,appid TEXT,"+
                                            "t_details TEXT,noofbed TEXT,noofdialysis TEXT,conforme TEXT,conformeDesignation TEXT,ename TEXT,answer TEXT);";

    public DatabaseHelper(Context context) {
        super(context, db_doh, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tbl_assessment);
        db.execSQL(tbl_monitoring);
        db.execSQL(tbl_user);
        db.execSQL(tbl_assessment_details);
        db.execSQL(tbl_monitoring_details);
        db.execSQL(tbl_assessment_headers);
        db.execSQL(tbl_monitoring_headers);
        db.execSQL(tbl_SrvasmtcolsList_Result);
        db.execSQL(tbl_MonsrvasmtcolsList_Result);
        db.execSQL(tbl_save_assessment);
        db.execSQL(tbl_monsave_assessment);
        db.execSQL(tbl_initial_save_assessment);
        db.execSQL(tbl_moninitial_save_assessment);
        db.execSQL(tbl_assessment_part);
        db.execSQL(tbl_assessment_headerone);
        db.execSQL(tbl_assessment_headertwo);
        db.execSQL(tbl_assessment_headerthree);
        db.execSQL(tbl_show_assessment);
        db.execSQL(tbl_save_assessment_res);
        db.execSQL(tbl_save_assessment_header);
        db.execSQL(assesscombined);
        db.execSQL(assesscombinedptc);
        db.execSQL(assessrecommend);
        db.execSQL(assesscombinedtemp);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        // on upgrade drop older tables
        // create new tables


        

    }


    public boolean add(String table,String[] columns,String[] data,String status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for(int i=0;i<columns.length;i++){
            cv.put(columns[i],data[i]);
        }
        long result = db.insert(table,null,cv);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor list (String table){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table,null);
        return c;
    }



    public Cursor get_item (String table,String fieldid,String id){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where "+fieldid+"= '"+id+"'",null);
        return c;
    }

    public Cursor get_itemMon (String table,String appid,String monid){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where appid= '"+appid+"' AND monid = '"+monid+"'",null);
        return c;
    }
    public Cursor get_items (String table,String id,String appid){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where id = '"+id+"' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_itemsMon (String table,String id,String appid,String monid){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where id = '"+id+"' AND appid = '"+appid+"' AND monid= '"+monid+"'",null);
        return c;
    }



    public Cursor get_item_Assessment (String table,String uid,String type){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where type= '"+type+"' and uid = '"+uid+"'",null);
        return c;
    }

    public String get_item_AssessmentEvaluation (String table,String uid,String type){
        SQLiteDatabase db= this.getReadableDatabase();
        String s = "";
        Cursor c = db.rawQuery("SELECT json_data FROM "+table+" where type= '"+type+"' and uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                s = c.getString(c.getColumnIndex("json_data"));
                c.moveToNext();
            }
        }
        db.close();
        return s;
    }


    public Cursor get_item_json_data(String table,String asmntid,String appid){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where appid = '"+appid+"' and asmt2l_id = '"+asmntid+"'",null);
        return c;
    }

    public boolean checkData(String table,String posid,String asmt2lid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where asmt2l_id = '"+asmt2lid+"' and posid = '"+posid+"'",null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }

    public boolean checkJoinedData(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_SrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }
    //tbl_MonsrvasmtcolsList_Result

    public boolean checkJoinedDataMon(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_MonsrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }

    public String getassesid(String uid,String type){
        SQLiteDatabase db = this.getWritableDatabase();
        String aid = "";
        Cursor c = db.rawQuery("SELECT assessment_id FROM tbl_assessment where uid = '"+uid+"' and type = '"+type+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                aid = c.getString(c.getColumnIndex("assessment_id"));
                c.moveToNext();
            }

        }
        return aid;
    }

    public String getcomments(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String aid = "";
        Cursor c = db.rawQuery("SELECT details FROM assessrecommend where appid = '"+appid+"' and evaluatedby = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                aid = c.getString(c.getColumnIndex("details"));
                c.moveToNext();
            }

        }
        return aid;
    }

    public String gethoneid(String appid,String monid,String aid){
        SQLiteDatabase db = this.getWritableDatabase();
        String id = "";
        String sql = "";
        if(!monid.equals("")){
            sql = "SELECT honeid FROM tbl_assessment_headerone where id = '"+aid+"' and appid = '"+appid+"' and monid = '"+monid+"'";
        }else{
            sql = "SELECT honeid FROM tbl_assessment_headerone where id = '"+aid+"' and appid = '"+appid+"' and monid ISNULL";
        }
        Cursor c = db.rawQuery(sql,null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("honeid"));
                c.moveToNext();
            }

        }
        return id;
    }

    public String getpartid(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String pid = "";
        Cursor c = db.rawQuery("SELECT partid FROM tbl_assessment_part where appid = '"+appid+"' and monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                pid = c.getString(c.getColumnIndex("partid"));
                c.moveToNext();
            }

        }
        return pid;
    }

    public String getrecoidmon(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String pid = "";
        Cursor c = db.rawQuery("SELECT reco FROM assessrecommend where appid = '"+appid+"' and monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                pid = c.getString(c.getColumnIndex("reco"));
                c.moveToNext();
            }

        }
        return pid;
    }

    public String getrecoid(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String pid = "";
        Cursor c = db.rawQuery("SELECT reco FROM assessrecommend where appid = '"+appid+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                pid = c.getString(c.getColumnIndex("reco"));
                c.moveToNext();
            }

        }
        return pid;
    }

    public String getsid(String appid,String monid,String aid){
        SQLiteDatabase db = this.getWritableDatabase();
        String id = "";
        Cursor c = db.rawQuery("SELECT sid FROM tbl_show_assessment where id = '"+aid+"' and appid = '"+appid+"' and monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("sid"));
                c.moveToNext();
            }

        }
        return id;
    }

    public boolean checkDatas(String table,String fieldid,String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where "+fieldid+" = '"+id+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }

    public boolean checkLicensingDatas(String table,String uid,String type){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT type FROM "+table+" where uid = '"+uid+"' and type = '"+type+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }


    public boolean checkDatasAssesment(String table,String appid,String asmt2l_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where appid = '"+appid+"' and asmt2l_id='"+asmt2l_id+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }
    public boolean checkHeader(String table,String field,String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+table+" where "+field+" = '"+id+"'",null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;
    }



    public String get_tbl_SrvasmtcolsList_Result_id(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String rid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_SrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                rid = c.getString(c.getColumnIndex("resid"));
                c.moveToNext();
            }

        }
        return rid;
    }

    public String get_tbl_monSrvasmtcolsList_Result_id(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String rid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_MonsrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                rid = c.getString(c.getColumnIndex("resid"));
                c.moveToNext();
            }

        }
        return rid;
    }


    public String get_json_data_tbl_tbl_assessment_details(String uid,String asmtl_id,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT json_data from tbl_assessment_details where uid = '"+uid+"' and asmt2l_id = '"+asmtl_id+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public String get_json_data_tbl_tbl_assessment_details1(String asmtl_id,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT json_data from tbl_assessment_details where asmt2l_id = '"+asmtl_id+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public String get_json_data_tbl_monitoring_details1(String asmtl_id,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT json_data from tbl_monitoring_details where asmt2l_id = '"+asmtl_id+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public boolean check_if_exist_tbl_save_assessment_data(String appid,String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where appid = '"+appid+"' and uid='"+uid+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;

    }

    public boolean check_if_exist_tbl_monsave_assessment_data(String appid,String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_monsave_assessment where appid = '"+appid+"' and uid='"+uid+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;

    }

    public boolean check_if_exist_tbl_save_assessment_datas(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where appid = '"+appid+"'" ,null);
        if(c!=null && c.getCount()>0){
            return true;
        }
        return false;

    }

    public String get_json_data_tbl_save_assessment(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT submit_json_data FROM tbl_save_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("submit_json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public String get_tbl_initial_assessmen_id(String appid,String header){
        String id = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT initialid FROM tbl_initial_save_assessment where status = '0' and  appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("initialid"));
                c.moveToNext();
            }
        }
        return id;
    }

    public String get_tbl_initial_assessmen_id1(String appid,String header,String uid){
        String id = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT initialid FROM tbl_initial_save_assessment where status = '0' and  appid = '"+appid+"' and header = '"+header+"' and uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("initialid"));
                c.moveToNext();
            }
        }
        return id;
    }

    public String get_tbl_moninitial_assessmen_id(String appid,String header){
        String id = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT initialid FROM tbl_moninitial_save_assessment where status = '0' and  appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("initialid"));
                c.moveToNext();
            }
        }
        return id;
    }


    public String get_tbl_initial_assessmen_status(String appid,String header){
        String status = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT status FROM tbl_initial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                status = c.getString(c.getColumnIndex("status"));
                c.moveToNext();
            }
        }
        return status;
    }

    public String get_tbl_moninitial_assessmen_status(String appid,String header){
        String status = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT status FROM tbl_moninitial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                status = c.getString(c.getColumnIndex("status"));
                c.moveToNext();
            }
        }
        return status;
    }

    public String get_json_data_tbl_save_assessment_sync_status(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT submit_json_data FROM tbl_save_assessment where syncstatus = '0' and uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("submit_json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public boolean check_appid(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where syncstatus = '0' and type='assessment' and  uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
           check = true;
        }

        return check;
    }

    public boolean check_reslicense(String uid,String type){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where syncstatus = '0' and type='"+type+"' and  uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }

        return check;
    }


    public boolean check_monappid(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM tbl_monsave_assessment where syncstatus = '0' and uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }

        return check;
    }


    public Cursor get_appid(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String appid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where syncstatus = '0' and uid = '"+uid+"'",null);
        return c;
    }

    public Cursor get_resappid(String uid,String type){
        SQLiteDatabase db = this.getWritableDatabase();
        String appid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where syncstatus = '0' and uid = '"+uid+"' and type = '"+type+"'",null);
        return c;
    }



    public Cursor get_monappid(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String appid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_monsave_assessment where syncstatus = '0' and uid = '"+uid+"'",null);
        return c;
    }

    public String get_json_data_tbl_save_assessment_headers(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_save_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }


    public String get_json_data_tbl_monsave_assessment_headers(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_monsave_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }

    public String get_json_tbl_save_assessment_header(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_save_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }

    public String get_json_tbl_monsave_assessment_header(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_monsave_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }

    public Cursor get_json_data_tbl_save_assessments(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where type = 'assessment' and sharestatus = '0' and uid = '"+uid+"'",null);
        return c;
    }
    public Cursor get_combined(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '' AND evaluatedBy = '"+uid+"'",null);
        return c;
    }

    public Cursor get_combineddataappid(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_combineddataappidmon(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid != '' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_combineddataappidptc(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_recommendappid(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assessrecommend where monid = '' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_recommendappidmon(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assessrecommend where monid != '' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_combinedappid(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT appid FROM assesscombined where monid = ''",null);
        return c;
    }

    public Cursor get_combinedappidmonid(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT appid FROM assesscombined where monid != ''",null);
        return c;
    }

    public Cursor get_combinedappidptc(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT appid FROM assesscombinedptc",null);
        return c;
    }

    public Cursor get_combinedptc(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where evaluatedBy = '"+uid+"'",null);
        return c;
    }
    public Cursor get_combined_mon(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid != '' and evaluatedBy = '"+uid+"'",null);
        return c;
    }

    public Cursor get_headers(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid ISNULL or monid = ' ' and uid = '"+uid+"'",null);
        return c;
    }

    public Cursor get_recommend(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assessrecommend where evaluatedby = '"+uid+"'",null);
        return c;
    }

    public Cursor get_headers_mon(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid != '' and uid = '"+uid+"'",null);
        return c;
    }

    public Cursor get_json_data_tbl_save_evaluation(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where type = 'evaluation' and sharestatus = '0' and uid = '"+uid+"'",null);
        return c;
    }

    public Cursor get_json_data_tbl_monsave_assessments(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_monsave_assessment where sharestatus = '0' and uid = '"+uid+"'",null);
        return c;
    }



    public String get_json_data_tbl_save_assessment_header(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_save_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }

    public String get_json_data_tbl_monsave_assessment_header(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT headers FROM tbl_monsave_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("headers"));
                c.moveToNext();
            }
        }
        return jsondata;
    }

    public String get_reco(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        String rec = "";
        Cursor c = db.rawQuery("SELECT reco FROM assessrecommend where appid = '"+appid+"' and evaluatedby = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                rec = c.getString(c.getColumnIndex("reco"));
                c.moveToNext();
            }
        }
        return rec;
    }

    public Cursor get_initial_save(String appid,String header){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_initial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        return c;
    }

    public Cursor get_moninitial_save(String appid,String header){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_moninitial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        return c;
    }

    public boolean check_has_initial_save_assessment(String appid,String header){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM tbl_initial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }

    public boolean check_has_moninitial_save_assessment(String appid,String header){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM tbl_moninitial_save_assessment where appid = '"+appid+"' and header = '"+header+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }

    public boolean check_has_save_assessment(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT headers FROM tbl_save_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }

    public boolean check_has_monsave_assessment(String uid,String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT headers FROM tbl_monsave_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }

    public boolean check_has_save_assessments(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT headers FROM tbl_save_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }

    public boolean check_has_monsave_assessments(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT headers FROM tbl_monsave_assessment where appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        return check;
    }


    public boolean check_has_pin_password(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery("SELECT pinpassword FROM tbl_user where uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                String pinpass = c.getString(c.getColumnIndex("pinpassword"));
                if(pinpass.equals("")){
                    check = true;
                }
                c.moveToNext();
            }
        }
        return check;
    }
    public String get_tbl_SrvasmtcolsList_Result_json(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_SrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                jsondata = c.getString(c.getColumnIndex("answers_json_data"));
                c.moveToNext();
            }

        }
        return jsondata;
    }

    public String get_tbl_assessment_res(String appid,String uid,String headid,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String resid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_res where appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                resid = c.getString(c.getColumnIndex("resid"));
                c.moveToNext();
            }

        }
        return resid;
    }

    //

    public Cursor get_tbl_assesscombined_res(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assesscombinedptc_res(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc WHERE appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assesscombined_res_mon(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE monid = '"+monid+"' and appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assesscombined_res_improvement(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE evaluation = '1' AND remarks != '' AND appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assesscombined_res_improvement_mon(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE evaluation = '1' AND remarks != '' AND monid = '"+monid+"' AND appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assesscombined_res_compliance(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE evaluation = '0' AND remarks != '' AND appid = '"+appid+"' ORDER BY assessmentSeq",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE evaluation = '0' AND appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }


    public Cursor get_tbl_assesscombined_res_compliance_mon(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined WHERE evaluation = '0' AND remarks != '' AND monid = '"+monid+"' AND appid = '"+appid+"' ORDER BY assessmentSeq",null);
        return c;
    }

    public Cursor get_tbl_assessrecommend(String appid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assessrecommend WHERE appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_tbl_assessrecommend_mon(String appid,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assessrecommend WHERE monid = '"+monid+"' AND appid = '"+appid+"'",null);
        return c;
    }

    public Cursor get_tbl_assesscombinedtemp(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedtemp",null);
        return c;
    }

    public Cursor get_tbl_assesscombinedtempdata(String fk){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedtemp WHERE asmtComb_FK = '"+fk+"'",null);
        return c;
    }

    public Cursor get_tbl_assesscombinedtempid(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT asmtComb_FK,assessmentHead,assessmentName,appid,asmtH1ID_FK,monid,h3name,h1name FROM assesscombinedtemp",null);
        return c;
    }

    public String get_tbl_assesscombined(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy != '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedtemp(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedtemp where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombineddupid(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombined1(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public Cursor get_assesscombineddata(String dupid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where dupid = '"+dupid+"'",null);
        return c;
    }

    public Cursor get_assesscombinedptcdata(String dupid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where dupid = '"+dupid+"'",null);
        return c;
    }

    public String checkassesscombined(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = ''",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedptc(String appid,String uid,String jindex,String h3id,String h2id,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedmon(String appid,String uid,String jindex,String h3id,String h2id,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '"+monid+"' and appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedmon1(String appid,String uid,String jindex,String h3id,String h2id,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '"+monid+"' and appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedmonid(String appid,String uid,String jindex,String h3id,String h2id,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '"+monid+"' and appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }

    public String get_tbl_assesscombinedtempmon(String appid,String uid,String jindex,String h3id,String h2id,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedtemp where monid = '"+monid+"' and appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }
        }
        return dupID;
    }
    //
    public String countanswer(String partid,String hid,String appid){
        SQLiteDatabase db = this.getReadableDatabase();
        String count = "";
        Cursor c = db.rawQuery("SELECT COUNT(appid) as answer FROM assesscombined WHERE (NOT evaluation = '-1') AND evaluation != '' AND  asmtH3ID_FK = '"+partid+"' AND asmtH1ID_FK = '"+hid+"' AND appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                count = c.getString(c.getColumnIndex("answer"));
                c.moveToNext();
            }
        }
        db.close();
        return count;
    }

    public String countanswermon(String partid,String hid,String appid,String monid){
        SQLiteDatabase db = this.getReadableDatabase();
        String count = "";
        Cursor c = db.rawQuery("SELECT COUNT(appid) as answer FROM assesscombined WHERE (NOT evaluation = '-1') AND evaluation != '' AND asmtH3ID_FK = '"+partid+"' AND asmtH1ID_FK = '"+hid+"' AND appid = '"+appid+"' AND monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                count = c.getString(c.getColumnIndex("answer"));
                c.moveToNext();
            }
        }
        db.close();
        return count;
    }
    public Boolean get_tbl_assesscombineduid(String dupid,String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where dupid = '"+dupid+"' AND evaluatedBy = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        db.close();
        return check;
    }

    public String get_tbl_assesscombinedheaderone(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }

        }
        db.close();
        return dupID;
    }

    public String get_tbl_assesscombinedheaderonemon(String appid,String uid,String jindex,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        ///Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '"+monid+"' and appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where monid = '"+monid+"' and appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }

        }
        return dupID;
    }

    public Boolean get_tbl_assesscombinedptcuid(String dupid,String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Boolean check = false;
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where dupid = '"+dupid+"' AND evaluatedBy = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            check = true;
        }
        db.close();
        return check;
    }

    public String get_tbl_assesscombinedheaderptc(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                dupID = c.getString(c.getColumnIndex("dupID"));
                c.moveToNext();
            }

        }
        return dupID;
    }
    public Cursor get_tbl_assesscombinedheaderptcer(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombinedptc where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        return c;
    }

    public String get_tbl_assesscombinedevaluation(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String choice = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                choice = c.getString(c.getColumnIndex("evaluation"));
                c.moveToNext();
            }

        }
        return choice;
    }

    public String get_tbl_assesscombinedevaluationmon(String appid,String uid,String jindex,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String choice = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                choice = c.getString(c.getColumnIndex("evaluation"));
                c.moveToNext();
            }

        }
        return choice;
    }

    public String get_user_json_data(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        String choice = "";
        Cursor c = db.rawQuery("SELECT json_data FROM tbl_user where uid = '"+uid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                choice = c.getString(c.getColumnIndex("json_data"));
                c.moveToNext();
            }
        }
        return choice;
    }

    public String get_tbl_assesscombinedremarks(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String choice = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                choice = c.getString(c.getColumnIndex("remarks"));
                c.moveToNext();
            }

        }
        return choice;
    }

    public String get_tbl_assesscombinedremarksmon(String appid,String uid,String jindex,String h1id,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String choice = "";
        //Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"' and monid = '"+monid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                choice = c.getString(c.getColumnIndex("remarks"));
                c.moveToNext();
            }

        }
        return choice;
    }

    public Cursor get_tbl_assesscombined_data(String appid,String uid,String jindex,String h1id){
        SQLiteDatabase db = this.getWritableDatabase();
        String dupID = "";
        Cursor c = db.rawQuery("SELECT * FROM assesscombined where appid = '"+appid+"' and evaluatedBy = '"+uid+"' and asmtComb_FK = '"+jindex+"' and asmtH1ID_FK ='"+h1id+"'",null);

        return c;
    }

    public String get_tbl_assessment_header(String appid,String uid,String headid,String headerlevel){
        SQLiteDatabase db = this.getWritableDatabase();
        String assessheadid = "";
        //Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where appid = '"+appid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                assessheadid = c.getString(c.getColumnIndex("assessheadid"));
                c.moveToNext();
            }

        }
        return assessheadid;
    }



    public String get_tbl_assessment_headermon(String appid,String uid,String headid,String headerlevel,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String assessheadid = "";
        //Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid = '"+monid+"' and appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid = '"+monid+"' and appid = '"+appid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                assessheadid = c.getString(c.getColumnIndex("assessheadid"));
                c.moveToNext();
            }

        }
        return assessheadid;
    }

    public String get_tbl_assessment_header_mon(String appid,String uid,String headid,String headerlevel,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String assessheadid = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid = '"+monid+"' and appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                assessheadid = c.getString(c.getColumnIndex("assessheadid"));
                c.moveToNext();
            }

        }
        return assessheadid;
    }

    public String get_tbl_assessment_header_assess(String appid,String uid,String headid,String headerlevel){
        SQLiteDatabase db = this.getWritableDatabase();
        String assessheadid = "";
        //Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where appid = '"+appid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                assessheadid = c.getString(c.getColumnIndex("assess"));
                c.moveToNext();
            }
        }
        return assessheadid;
    }

    public String get_tbl_assessment_header_assessmon(String appid,String uid,String headid,String headerlevel,String monid){
        SQLiteDatabase db = this.getWritableDatabase();
        String assessheadid = "";
        //Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid = '"+monid+"' and appid = '"+appid+"' and uid = '"+uid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment_header where monid = '"+monid+"' and appid = '"+appid+"' and headerid = '"+headid+"' and headerlevel = '"+headerlevel+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                assessheadid = c.getString(c.getColumnIndex("assess"));
                c.moveToNext();
            }

        }
        return assessheadid;
    }

    public Cursor get_tbl_SrvasmtcolsList_Result(String appid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_SrvasmtcolsList_Result where appid = '"+appid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);

        return c;
    }
    public Cursor get_tbl_monSrvasmtcolsList_Result(String appid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String jsondata = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_MonsrvasmtcolsList_Result where appid = '"+appid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);

        return c;
    }

    public String get_tbl_SrvasmtcolsList_Result_remarks(String appid,String uid,String asmnt_id,String jindex){
        SQLiteDatabase db = this.getWritableDatabase();
        String remarks = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_SrvasmtcolsList_Result where appid = '"+appid+"' and uid = '"+uid+"' and asmnt_id = '"+asmnt_id+"' and jindex = '"+jindex+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                remarks = c.getString(c.getColumnIndex("remarks"));
                c.moveToNext();
            }

        }
        return remarks;
    }


    public String get_tbl_initial_data(String appid,String uid,String head){
        SQLiteDatabase db = this.getWritableDatabase();
        String json = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_initial_save_assessment where appid = '"+appid+"' and uid = '"+uid+"' and header = '"+head+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                json = c.getString(c.getColumnIndex("initial_json_data"));
                c.moveToNext();
            }

        }
        return json;
    }

    public String get_tbl_initial_datas(String appid,String head){
        SQLiteDatabase db = this.getWritableDatabase();
        String json = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_initial_save_assessment where appid = '"+appid+"' and header = '"+head+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                json = c.getString(c.getColumnIndex("initial_json_data"));
                c.moveToNext();
            }

        }
        return json;
    }

    public String get_tbl_moninitial_datas(String appid,String head){
        SQLiteDatabase db = this.getWritableDatabase();
        String json = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_moninitial_save_assessment where appid = '"+appid+"' and header = '"+head+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                json = c.getString(c.getColumnIndex("initial_json_data"));
                c.moveToNext();
            }

        }
        return json;
    }

    public String get_tbl_initial_id(String appid,String uid,String head){
        SQLiteDatabase db = this.getWritableDatabase();
        String json = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_initial_save_assessment where appid = '"+appid+"' and uid = '"+uid+"' and header = '"+head+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                json = c.getString(c.getColumnIndex("initialid"));
                c.moveToNext();
            }

        }
        return json;
    }

    public String get_tbl_moninitial_id(String appid,String uid,String head){
        SQLiteDatabase db = this.getWritableDatabase();
        String json = "";
        Cursor c = db.rawQuery("SELECT * FROM tbl_moninitial_save_assessment where appid = '"+appid+"' and uid = '"+uid+"' and header = '"+head+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                json = c.getString(c.getColumnIndex("initialid"));
                c.moveToNext();
            }

        }
        return json;
    }


    public Cursor get_all_tbl_SrvasmtcolsList_Result_remarks(String uid,String appid,String asmnt_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT jindex,answers_json_data,remarks,srvasmt_col FROM tbl_SrvasmtcolsList_Result where uid = '"+uid+"' and appid = '"+appid+"' and asmnt_id = '"+asmnt_id+"'",null);
        return c;
    }

    public Cursor get_all_tbl_monSrvasmtcolsList_Result_remarks(String uid,String appid,String asmnt_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT jindex,answers_json_data,remarks,srvasmt_col FROM tbl_MonsrvasmtcolsList_Result where uid = '"+uid+"' and appid = '"+appid+"' and asmnt_id = '"+asmnt_id+"'",null);
        return c;
    }


    public String getSaveAssessmentId(String uid,String appid){
        String id = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_save_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("saveid"));
                c.moveToNext();
            }

        }
        return id;
    }

    public String getmonSaveAssessmentId(String uid,String appid){
        String id = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tbl_monsave_assessment where uid = '"+uid+"' and appid = '"+appid+"'",null);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            while (!c.isAfterLast()){
                id = c.getString(c.getColumnIndex("saveid"));
                c.moveToNext();
            }

        }
        return id;
    }


    public boolean update(String table,String[] columns,String[] data,String fieldid,String id){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for(int i=0;i<columns.length;i++){
            cv.put(columns[i],data[i]);
        }
        int result = db.update(table,cv,fieldid+"=?",new String[]{id});
        if(result>0){
            return true;
        }else{
            return false;
        }
    }



    public Integer delete(String table,String columnid,String id){
        SQLiteDatabase db = this.getWritableDatabase();
        int i = db.delete(table,null,null);
        return i;
    }

}
