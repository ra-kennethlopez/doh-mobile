package com.example.pc.doh.Model;

public class Srvasmtcols {
    private String name;
    private String Desc;
    private String Type;
    private String answer;

    public Srvasmtcols(String name, String desc, String type, String answer) {
        this.name = name;
        Desc = desc;
        Type = type;
        this.answer = answer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
