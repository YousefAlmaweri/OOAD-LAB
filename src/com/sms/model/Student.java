package com.sms.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private String researchTitle;
    private String researchAbstract;
    private String supervisorName;
    private Enums.PresentationType presentationType;
    private Submission submission;
    private List<Evaluation> evaluations;

    public Student(String userID, String name, String email, String password) {
        super(userID, name, email, password, "Student");
        this.evaluations = new ArrayList<>();
    }

    public void registerSeminar(String title, String abstractText, String supervisor) {
        this.researchTitle = title;
        this.researchAbstract = abstractText;
        this.supervisorName = supervisor;
    }

    public void selectPresentationType(Enums.PresentationType type) {
        this.presentationType = type;
    }

    public void uploadMaterial(String filePath) {
        if (this.submission == null) {
            this.submission = new Submission("SUB-" + userID, filePath);
        } else {
            this.submission.updateFilePath(filePath);
        }
    }

    public void attachEvaluation(Evaluation eval) {
        this.evaluations.add(eval);
    }

    // Getters
    public String getResearchTitle() { return researchTitle; }
    public String getResearchAbstract() { return researchAbstract; }
    public String getSupervisorName() { return supervisorName; }
    public Enums.PresentationType getPresentationType() { return presentationType; }
    public Submission getSubmission() { return submission; }
    public List<Evaluation> getEvaluations() { return evaluations; }
}
