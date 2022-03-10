package com.example.pc.doh.Model;

public class evshowassessitem {
    String disp,choice,remarks,id,otherheading,sequence,sub,revision,isdisplay,h3headid,h3headback;

    public evshowassessitem(String disp, String choice, String remarks, String id, String otherheading, String sequence, String sub, String revision, String isdisplay, String h3headid, String h3headback) {
        this.disp = disp;
        this.choice = choice;
        this.remarks = remarks;
        this.id = id;
        this.otherheading = otherheading;
        this.sequence = sequence;
        this.sub = sub;
        this.revision = revision;
        this.isdisplay = isdisplay;
        this.h3headid = h3headid;
        this.h3headback = h3headback;
    }

    public String getDisp() {
        return disp;
    }

    public void setDisp(String disp) {
        this.disp = disp;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOtherheading() {
        return otherheading;
    }

    public void setOtherheading(String otherheading) {
        this.otherheading = otherheading;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getIsdisplay() {
        return isdisplay;
    }

    public void setIsdisplay(String isdisplay) {
        this.isdisplay = isdisplay;
    }

    public String getH3headid() {
        return h3headid;
    }

    public void setH3headid(String h3headid) {
        this.h3headid = h3headid;
    }

    public String getH3headback() {
        return h3headback;
    }

    public void setH3headback(String h3headback) {
        this.h3headback = h3headback;
    }
}
