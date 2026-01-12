package com.sms.controller;

import com.sms.model.*;
import com.sms.data.DataStore;

public class EvaluationController {
    private DataStore dataStore;

    public EvaluationController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Student getStudentData(String studentID) {
        return dataStore.getStudentByID(studentID);
    }

    public void processEvaluation(Student student, double clarity, double methodology, double results, double presentation, String comments) {
        Evaluation eval = new Evaluation("EV-" + System.currentTimeMillis(), clarity, methodology, results, presentation, comments);
        student.attachEvaluation(eval);
        dataStore.addEvaluation(eval);
        dataStore.save();
    }
}
