package com.example.pc.doh.Model;

public class Save {
    String pos;
    String remarks;


    public Save(String pos, String remarks) {
        this.pos = pos;
        this.remarks = remarks;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
