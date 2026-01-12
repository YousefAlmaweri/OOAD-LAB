package com.sms.model;

import java.io.Serializable;
import java.util.Date;

public class Report implements Serializable {
    private String reportID;
    private Date generatedDate;
    private String reportContent;

    public Report(String reportID, String reportContent) {
        this.reportID = reportID;
        this.generatedDate = new Date();
        this.reportContent = reportContent;
    }

    public void exportToPDF() {
        System.out.println("Exporting report " + reportID + " to PDF...");
    }

    public void generateAnalytics() {
        System.out.println("Generating analytics for report " + reportID + "...");
    }

    // Getters
    public String getReportID() { return reportID; }
    public Date getGeneratedDate() { return generatedDate; }
    public String getReportContent() { return reportContent; }
}
