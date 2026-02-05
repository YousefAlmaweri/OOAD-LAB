package Lab_Exercise.V2;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

enum Role { STUDENT, EVALUATOR, COORDINATOR }
enum PresentationType { ORAL, POSTER }
enum SessionType { ORAL, POSTER }

class User implements Serializable {
    UUID id = UUID.randomUUID();
    String username;
    String password;
    String fullname;
    Role role;

    User(String username, String password, String fullname, Role role) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
    }

    @Override public String toString() {
        return fullname + " (" + username + ")";
    }
}

class Submission implements Serializable {
    UUID id = UUID.randomUUID();
    UUID studentUserId;

    String researchTitle;
    String abstractText;
    String supervisorName;
    PresentationType presentationType;

    String materialFilePath; // slides/poster path
    String posterBoardId;     // used if poster
    LocalDate submittedDate = LocalDate.now();

    Submission(UUID studentUserId) {
        this.studentUserId = studentUserId;
    }
}

class Session implements Serializable {
    UUID id = UUID.randomUUID();
    LocalDate date;
    String venue;
    SessionType sessionType;

    // time slots: map time -> presenter submission id
    LinkedHashMap<LocalTime, UUID> timeSlotToSubmission = new LinkedHashMap<>();

    // evaluator assignments: evaluator -> list of submission ids (within this session)
    LinkedHashMap<UUID, List<UUID>> evaluatorToSubmissions = new LinkedHashMap<>();

    Session(LocalDate date, String venue, SessionType type) {
        this.date = date;
        this.venue = venue;
        this.sessionType = type;
    }

    String displayName() {
        return date + " | " + venue + " | " + sessionType;
    }
}

class Evaluation implements Serializable {
    UUID id = UUID.randomUUID();
    UUID evaluatorUserId;
    UUID submissionId;

    // Rubrics: Problem Clarity, Methodology, Results, Presentation
    int problemClarity;  // 1-5
    int methodology;     // 1-5
    int results;         // 1-5
    int presentation;    // 1-5
    String comments;

    LocalDate submittedDate = LocalDate.now();

    double avgScore() {
        return (problemClarity + methodology + results + presentation) / 4.0;
    }
}


// Poster board criteria management (Coordinator)
// - Manages poster board IDs and criteria/notes for each board
class PosterBoardCriteria implements Serializable {
    UUID id = UUID.randomUUID();
    String boardId;     // e.g., "B12"
    String criteria;    // requirements/criteria for posters placed on this board
    String notes;       // optional logistics notes
    LocalDate updatedDate = LocalDate.now();

    PosterBoardCriteria(String boardId, String criteria, String notes) {
        this.boardId = boardId;
        this.criteria = criteria;
        this.notes = notes;
    }

    @Override public String toString() {
        return boardId;
    }
}


class AwardWinners implements Serializable {
    UUID bestOralSubmissionId;
    UUID bestPosterSubmissionId;
    UUID peoplesChoiceSubmissionId;
    LocalDate computedOn = LocalDate.now();
}
