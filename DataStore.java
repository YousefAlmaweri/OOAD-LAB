package Lab_Exercise;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class DataStore implements Serializable {
    private static final String DATA_FILE = "seminar_data.ser";

    List<User> users = new ArrayList<>();
    List<Submission> submissions = new ArrayList<>();
    List<Session> sessions = new ArrayList<>();
    List<Evaluation> evaluations = new ArrayList<>();

    // People’s Choice votes: submissionId -> count
    Map<UUID, Integer> peoplesChoiceVotes = new HashMap<>();

    AwardWinners winners = new AwardWinners();

    static DataStore loadOrCreate() {
        File f = new File(DATA_FILE);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof DataStore ds) return ds;
            } catch (Exception ex) {
                ex.printStackTrace();
                // fall through to recreate
            }
        }
        DataStore ds = new DataStore();
        ds.seedDefaults();
        ds.save();
        return ds;
    }

    void seedDefaults() {
        users.add(new User("coordinator", "admin123", "FCI Coordinator", Role.COORDINATOR));
        users.add(new User("evaluator1", "pass123", "Evaluator One", Role.EVALUATOR));
        users.add(new User("student1", "pass123", "Student One", Role.STUDENT));
    }

    synchronized void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to save data: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- helpers
    User findUserByUsername(String username) {
        for (User u : users) {
            if (u.username.equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    User findUser(UUID id) {
        for (User u : users) if (u.id.equals(id)) return u;
        return null;
    }

    Submission findSubmission(UUID id) {
        for (Submission s : submissions) if (s.id.equals(id)) return s;
        return null;
    }

    Submission findSubmissionByStudent(UUID studentUserId) {
        for (Submission s : submissions) {
            if (s.studentUserId.equals(studentUserId)) return s;
        }
        return null;
    }

    Evaluation findEvaluation(UUID evaluatorId, UUID submissionId) {
        for (Evaluation e : evaluations) {
            if (e.evaluatorUserId.equals(evaluatorId) && e.submissionId.equals(submissionId)) return e;
        }
        return null;
    }

    List<Evaluation> evaluationsForSubmission(UUID submissionId) {
        return evaluations.stream().filter(e -> e.submissionId.equals(submissionId)).collect(Collectors.toList());
    }

    double avgSubmissionScore(UUID submissionId) {
        List<Evaluation> list = evaluationsForSubmission(submissionId);
        if (list.isEmpty()) return 0.0;
        double sum = 0;
        for (Evaluation e : list) sum += e.avgScore();
        return sum / list.size();
    }

    int peoplesChoiceCount(UUID submissionId) {
        return peoplesChoiceVotes.getOrDefault(submissionId, 0);
    }
}
