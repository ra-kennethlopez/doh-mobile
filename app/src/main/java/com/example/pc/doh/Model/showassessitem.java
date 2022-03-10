package com.example.pc.doh.Model;

public class showassessitem {
    String disp,choice,remarks,id,otherheading,sequence;



    public showassessitem(String disp, String choice, String remarks, String id, String otherheading, String sequence) {
        this.disp = disp;
        this.choice = choice;
        this.remarks = remarks;
        this.id = id;
        this.otherheading = otherheading;
        this.sequence = sequence;
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
}
