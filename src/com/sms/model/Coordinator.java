package com.sms.model;

import java.util.Date;
import java.util.List;

public class Coordinator extends User {
    private String staffID;

    public Coordinator(String userID, String name, String email, String password, String staffID) {
        super(userID, name, email, password, "Coordinator");
        this.staffID = staffID;
    }

    public Session createSession(Date date, String venue, String type) {
        return new Session("SESS-" + System.currentTimeMillis(), date, venue, type);
    }

    public void assignParticipants(Session session, List<Student> students, List<Evaluator> evaluators) {
        for (Student s : students) session.addStudent(s);
        for (Evaluator e : evaluators) session.addEvaluator(e);
    }

    public Report generateReport() {
        return new Report("REP-" + System.currentTimeMillis(), "Consolidated Evaluation Report");
    }

    public Award manageAwardNomination(Enums.AwardType type) {
        return new Award("AWD-" + System.currentTimeMillis(), type, "Highest Score");
    }

    public String getStaffID() { return staffID; }
}
