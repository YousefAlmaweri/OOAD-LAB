package com.sms.model;

import java.io.Serializable;

public class Evaluation implements Serializable {
    private String evaluationID;
    private double problemClarityScore;
    private double methodologyScore;
    private double resultsScore;
    private double presentationScore;
    private String comments;
    private double totalMark;

    public Evaluation(String evaluationID, double problemClarityScore, double methodologyScore, 
                      double resultsScore, double presentationScore, String comments) {
        this.evaluationID = evaluationID;
        this.problemClarityScore = problemClarityScore;
        this.methodologyScore = methodologyScore;
        this.resultsScore = resultsScore;
        this.presentationScore = presentationScore;
        this.comments = comments;
        this.totalMark = calculateTotal();
    }

    public double calculateTotal() {
        return problemClarityScore + methodologyScore + resultsScore + presentationScore;
    }

    // Getters
    public String getEvaluationID() { return evaluationID; }
    public double getProblemClarityScore() { return problemClarityScore; }
    public double getMethodologyScore() { return methodologyScore; }
    public double getResultsScore() { return resultsScore; }
    public double getPresentationScore() { return presentationScore; }
    public String getComments() { return comments; }
    public double getTotalMark() { return totalMark; }
}
