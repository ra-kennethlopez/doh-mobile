package com.example.pc.doh.Model;

public class headerone {
    String id,desc,headerid;

    public headerone(String id, String desc, String headerid) {
        this.id = id;
        this.desc = desc;
        this.headerid = headerid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHeaderid() {
        return headerid;
    }

    public void setHeaderid(String headerid) {
        this.headerid = headerid;
    }
}
