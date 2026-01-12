package com.sms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Submission implements Serializable {
    private String submissionID;
    private String filePath;
    private LocalDateTime uploadDate;
    private String status;

    public Submission(String submissionID, String filePath) {
        this.submissionID = submissionID;
        this.filePath = filePath;
        this.uploadDate = LocalDateTime.now();
        this.status = "Submitted";
    }

    public void updateFilePath(String path) {
        this.filePath = path;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters
    public String getSubmissionID() { return submissionID; }
    public String getFilePath() { return filePath; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public String getStatus() { return status; }
}
