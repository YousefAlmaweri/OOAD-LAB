package com.sms.model;

import java.util.ArrayList;
import java.util.List;

public class Evaluator extends User {
    private List<Student> assignedStudents;

    public Evaluator(String userID, String name, String email, String password) {
        super(userID, name, email, password, "Evaluator");
        this.assignedStudents = new ArrayList<>();
    }

    public List<Student> viewAssignedPresenters() {
        return assignedStudents;
    }

    public void submitEvaluation(Evaluation eval) {
        // This is typically handled by the controller to link with a student
    }

    public void addAssignedStudent(Student s) {
        this.assignedStudents.add(s);
    }

    public List<Student> getAssignedStudents() {
        return assignedStudents;
    }
}
