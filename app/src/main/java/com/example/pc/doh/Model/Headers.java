package com.example.pc.doh.Model;

public class Headers {
    String asmt2l_id;
    String asmt2l_desc;
    String assesscomplete;
    String asmt2id;
    String asmt2desc;

    public Headers(String asmt2l_id, String asmt2l_desc, String assesscomplete,String asmt2id,String asmt2desc) {
        this.asmt2l_id = asmt2l_id;
        this.asmt2l_desc = asmt2l_desc;
        this.assesscomplete = assesscomplete;
        this.asmt2id = asmt2id;
        this.asmt2desc = asmt2desc;
    }

    public String getAsmt2id() {
        return asmt2id;
    }

    public void setAsmt2id(String asmt2id) {
        this.asmt2id = asmt2id;
    }

    public String getAsmt2desc() {
        return asmt2desc;
    }

    public void setAsmt2desc(String asmt2desc) {
        this.asmt2desc = asmt2desc;
    }

    public String getAsmt2l_id() {
        return asmt2l_id;
    }

    public void setAsmt2l_id(String asmt2l_id) {
        this.asmt2l_id = asmt2l_id;
    }

    public String getAsmt2l_desc() {
        return asmt2l_desc;
    }

    public void setAsmt2l_desc(String asmt2l_desc) {
        this.asmt2l_desc = asmt2l_desc;
    }

    public String getAssesscomplete() {
        return assesscomplete;
    }

    public void setAssesscomplete(String assesscomplete) {
        this.assesscomplete = assesscomplete;
    }
}
