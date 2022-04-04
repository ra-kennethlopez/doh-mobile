package com.example.pc.doh;

public class MonGenReport {
    String choice,
            details,
            valFrom,
            valTo,
            days,
            monId,
            selfAssess,
            revision,
            evaluatedBy,
            appId,
            tDetails,
            noOfBed,
            noOfDialysis,
            conforme,
            conformeDesignation,
            eName;

    public MonGenReport(String choice, String details, String valFrom, String valTo, String days, String monId, String selfAssess, String revision, String evaluatedBy, String appId, String tDetails, String noOfBed, String noOfDialysis, String conforme, String conformeDesignation, String eName) {
        this.choice = choice;
        this.details = details;
        this.valFrom = valFrom;
        this.valTo = valTo;
        this.days = days;
        this.monId = monId;
        this.selfAssess = selfAssess;
        this.revision = revision;
        this.evaluatedBy = evaluatedBy;
        this.appId = appId;
        this.tDetails = tDetails;
        this.noOfBed = noOfBed;
        this.noOfDialysis = noOfDialysis;
        this.conforme = conforme;
        this.conformeDesignation = conformeDesignation;
        this.eName = eName;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getValFrom() {
        return valFrom;
    }

    public void setValFrom(String valFrom) {
        this.valFrom = valFrom;
    }

    public String getValTo() {
        return valTo;
    }

    public void setValTo(String valTo) {
        this.valTo = valTo;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getMonId() {
        return monId;
    }

    public void setMonId(String monId) {
        this.monId = monId;
    }

    public String getSelfAssess() {
        return selfAssess;
    }

    public void setSelfAssess(String selfAssess) {
        this.selfAssess = selfAssess;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getEvaluatedBy() {
        return evaluatedBy;
    }

    public void setEvaluatedBy(String evaluatedBy) {
        this.evaluatedBy = evaluatedBy;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String gettDetails() {
        return tDetails;
    }

    public void settDetails(String tDetails) {
        this.tDetails = tDetails;
    }

    public String getNoOfBed() {
        return noOfBed;
    }

    public void setNoOfBed(String noOfBed) {
        this.noOfBed = noOfBed;
    }

    public String getNoOfDialysis() {
        return noOfDialysis;
    }

    public void setNoOfDialysis(String noOfDialysis) {
        this.noOfDialysis = noOfDialysis;
    }

    public String getConforme() {
        return conforme;
    }

    public void setConforme(String conforme) {
        this.conforme = conforme;
    }

    public String getConformeDesignation() {
        return conformeDesignation;
    }

    public void setConformeDesignation(String conformeDesignation) {
        this.conformeDesignation = conformeDesignation;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }
}
