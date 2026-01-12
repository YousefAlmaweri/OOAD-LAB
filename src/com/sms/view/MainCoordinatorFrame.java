package com.sms.view;

import com.sms.data.DataStore;
import com.sms.model.Coordinator;
import javax.swing.*;
import java.awt.*;

public class MainCoordinatorFrame extends JFrame {
    public MainCoordinatorFrame(Coordinator coordinator, DataStore dataStore) {
        setTitle("Coordinator Dashboard - " + coordinator.getName());
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 10, 10));

        JButton sessionBtn = new JButton("Manage Sessions");
        sessionBtn.addActionListener(e -> new SessionManagerFrame(coordinator, dataStore));
        add(sessionBtn);

        JButton reportBtn = new JButton("Awards & Reports");
        reportBtn.addActionListener(e -> new ReportAwardFrame(coordinator, dataStore));
        add(reportBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame(dataStore);
        });
        add(logoutBtn);

        setVisible(true);
    }
}
