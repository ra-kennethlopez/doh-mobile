package com.example.pc.doh.Model;

import java.util.List;
import java.util.Map;

public class PersonnelPage {
    String disp1,disp2,disp3,disp4;
    List<Srvasmtcols> srvasmtcolsList;
    int hasRemarks;
    String srvasmt_col,remarks,srvasmtcolsaans;
    String hasRemarksName;

    public PersonnelPage(String disp1, String disp2, String disp3, String disp4, List<Srvasmtcols> srvasmtcolsList, int hasRemarks, String srvasmt_col, String remarks, String srvasmtcolsaans, String hasRemarksName) {
        this.disp1 = disp1;
        this.disp2 = disp2;
        this.disp3 = disp3;
        this.disp4 = disp4;
        this.srvasmtcolsList = srvasmtcolsList;
        this.hasRemarks = hasRemarks;
        this.srvasmt_col = srvasmt_col;
        this.remarks = remarks;
        this.srvasmtcolsaans = srvasmtcolsaans;
        this.hasRemarksName = hasRemarksName;
    }

    public String getDisp1() {
        return disp1;
    }

    public void setDisp1(String disp1) {
        this.disp1 = disp1;
    }

    public String getDisp2() {
        return disp2;
    }

    public void setDisp2(String disp2) {
        this.disp2 = disp2;
    }

    public String getDisp3() {
        return disp3;
    }

    public void setDisp3(String disp3) {
        this.disp3 = disp3;
    }

    public String getDisp4() {
        return disp4;
    }

    public void setDisp4(String disp4) {
        this.disp4 = disp4;
    }

    public List<Srvasmtcols> getSrvasmtcolsList() {
        return srvasmtcolsList;
    }

    public void setSrvasmtcolsList(List<Srvasmtcols> srvasmtcolsList) {
        this.srvasmtcolsList = srvasmtcolsList;
    }

    public int getHasRemarks() {
        return hasRemarks;
    }

    public void setHasRemarks(int hasRemarks) {
        this.hasRemarks = hasRemarks;
    }

    public String getSrvasmt_col() {
        return srvasmt_col;
    }

    public void setSrvasmt_col(String srvasmt_col) {
        this.srvasmt_col = srvasmt_col;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSrvasmtcolsaans() {
        return srvasmtcolsaans;
    }

    public void setSrvasmtcolsaans(String srvasmtcolsaans) {
        this.srvasmtcolsaans = srvasmtcolsaans;
    }

    public String getHasRemarksName() {
        return hasRemarksName;
    }

    public void setHasRemarksName(String hasRemarksName) {
        this.hasRemarksName = hasRemarksName;
    }
}
