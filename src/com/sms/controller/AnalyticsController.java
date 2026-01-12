package com.sms.controller;

import com.sms.model.*;
import com.sms.data.DataStore;
import java.util.List;

public class AnalyticsController {
    private DataStore dataStore;

    public AnalyticsController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void calculateAwards() {
        List<Evaluation> allEvals = dataStore.getEvaluations();
        Award award = new Award("AWD-BEST", Enums.AwardType.BEST_ORAL, "Highest Score");
        award.computeWinner(allEvals);
        System.out.println("Award Winner: " + award.getWinnerID());
    }

    public void exportReport() {
        Report report = new Report("REP-FINAL", "Final Seminar Evaluation Report");
        report.generateAnalytics();
        report.exportToPDF();
    }
}
