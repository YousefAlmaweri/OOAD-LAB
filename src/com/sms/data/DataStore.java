package com.sms.data;

import com.sms.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore implements Serializable {
    private static final String FILE_PATH = "sms_data.ser";
    private List<User> users;
    private List<Session> sessions;
    private List<Evaluation> evaluations;

    public DataStore() {
        users = new ArrayList<>();
        sessions = new ArrayList<>();
        evaluations = new ArrayList<>();
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        users.add(new Student("S001", "John Doe", "student@fci.edu", "pass123"));
        users.add(new Evaluator("E001", "Dr. Smith", "evaluator@fci.edu", "pass123"));
        users.add(new Coordinator("C001", "Admin", "coordinator@fci.edu", "pass123", "STAFF001"));
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataStore load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
                return (DataStore) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new DataStore();
    }

    public List<User> getUsers() { return users; }
    public List<Session> getSessions() { return sessions; }
    public List<Evaluation> getEvaluations() { return evaluations; }

    public void addSession(Session s) { sessions.add(s); save(); }
    public void addEvaluation(Evaluation e) { evaluations.add(e); save(); }
    
    public User login(String email, String password) {
        for (User u : users) {
            if (u.login(email, password)) return u;
        }
        return null;
    }

    public Student getStudentByID(String id) {
        for (User u : users) {
            if (u instanceof Student && u.getUserID().equals(id)) return (Student) u;
        }
        return null;
    }

    public Evaluator getEvaluatorByID(String id) {
        for (User u : users) {
            if (u instanceof Evaluator && u.getUserID().equals(id)) return (Evaluator) u;
        }
        return null;
    }
}
