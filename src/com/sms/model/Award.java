package com.sms.model;

import java.io.Serializable;
import java.util.List;

public class Award implements Serializable {
    private String awardID;
    private Enums.AwardType category;
    private String winnerID;
    private String criteria;

    public Award(String awardID, Enums.AwardType category, String criteria) {
        this.awardID = awardID;
        this.category = category;
        this.criteria = criteria;
    }

    public void computeWinner(List<Evaluation> allEvals) {
        // Logic to compute winner based on evaluations
        if (!allEvals.isEmpty()) {
            // Placeholder: pick the one with highest mark
            this.winnerID = "Winner determined from " + allEvals.size() + " evaluations";
        }
    }

    // Getters
    public String getAwardID() { return awardID; }
    public Enums.AwardType getCategory() { return category; }
    public String getWinnerID() { return winnerID; }
    public String getCriteria() { return criteria; }
}
