package com.sms.controller;

import com.sms.model.*;
import com.sms.data.DataStore;

public class SeminarController {
    private DataStore dataStore;

    public SeminarController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void registerStudent(Student student, String title, String abstractText, String supervisor, String filePath, Enums.PresentationType type) {
        student.registerSeminar(title, abstractText, supervisor);
        student.selectPresentationType(type);
        student.uploadMaterial(filePath);
        dataStore.save();
    }
}
