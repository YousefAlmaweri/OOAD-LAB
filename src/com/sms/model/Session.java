package com.sms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session implements Serializable {
    private String sessionID;
    private Date sessionDate;
    private String venue;
    private String sessionType;
    private List<Student> presenterList;
    private List<Evaluator> evaluatorList;

    public Session(String sessionID, Date sessionDate, String venue, String sessionType) {
        this.sessionID = sessionID;
        this.sessionDate = sessionDate;
        this.venue = venue;
        this.sessionType = sessionType;
        this.presenterList = new ArrayList<>();
        this.evaluatorList = new ArrayList<>();
    }

    public void addStudent(Student s) {
        presenterList.add(s);
    }

    public void addEvaluator(Evaluator e) {
        evaluatorList.add(e);
    }

    // Getters
    public String getSessionID() { return sessionID; }
    public Date getSessionDate() { return sessionDate; }
    public String getVenue() { return venue; }
    public String getSessionType() { return sessionType; }
    public List<Student> getPresenterList() { return presenterList; }
    public List<Evaluator> getEvaluatorList() { return evaluatorList; }
}
