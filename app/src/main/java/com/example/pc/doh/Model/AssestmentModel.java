package com.example.pc.doh.Model;

public class AssestmentModel {

    private String type,code,faclityname,typefacility,date,status,appid;

    public AssestmentModel(String type, String code, String faclityname, String typefacility, String date, String status, String appid) {
        this.type = type;
        this.code = code;
        this.faclityname = faclityname;
        this.typefacility = typefacility;
        this.date = date;
        this.status = status;
        this.appid = appid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFaclityname() {
        return faclityname;
    }

    public void setFaclityname(String faclityname) {
        this.faclityname = faclityname;
    }

    public String getTypefacility() {
        return typefacility;
    }

    public void setTypefacility(String typefacility) {
        this.typefacility = typefacility;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }
}
