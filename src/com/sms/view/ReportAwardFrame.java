package com.sms.view;

import com.sms.controller.AnalyticsController;
import com.sms.data.DataStore;
import com.sms.model.Coordinator;
import javax.swing.*;
import java.awt.*;

public class ReportAwardFrame extends JFrame {
    private AnalyticsController controller;

    public ReportAwardFrame(Coordinator coordinator, DataStore dataStore) {
        this.controller = new AnalyticsController(dataStore);
        setTitle("Awards & Reports");
        setSize(400, 200);
        setLayout(new GridLayout(2, 1, 10, 10));

        JButton awardBtn = new JButton("Compute Award Winners");
        awardBtn.addActionListener(e -> {
            controller.calculateAwards();
            JOptionPane.showMessageDialog(this, "Awards Computed! Check console for details.");
        });
        add(awardBtn);

        JButton reportBtn = new JButton("Generate Final Report");
        reportBtn.addActionListener(e -> {
            controller.exportReport();
            JOptionPane.showMessageDialog(this, "Report Exported Successfully!");
        });
        add(reportBtn);

        setVisible(true);
    }
}
