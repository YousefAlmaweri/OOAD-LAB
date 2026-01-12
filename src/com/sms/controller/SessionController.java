package com.sms.controller;

import com.sms.model.*;
import com.sms.data.DataStore;
import java.util.Date;
import java.util.List;

public class SessionController {
    private DataStore dataStore;

    public SessionController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void createNewSession(Date date, String venue, String type, List<String> studentIDs, List<String> evaluatorIDs) {
        Session session = new Session("SESS-" + System.currentTimeMillis(), date, venue, type);
        
        for (String id : studentIDs) {
            Student s = dataStore.getStudentByID(id);
            if (s != null) session.addStudent(s);
        }
        
        for (String id : evaluatorIDs) {
            Evaluator e = dataStore.getEvaluatorByID(id);
            if (e != null) {
                session.addEvaluator(e);
                // Also assign students to evaluator for the module logic
                for (String sid : studentIDs) {
                    Student s = dataStore.getStudentByID(sid);
                    if (s != null) e.addAssignedStudent(s);
                }
            }
        }
        
        dataStore.addSession(session);
    }
}
