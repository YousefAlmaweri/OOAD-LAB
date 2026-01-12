package com.sms.view;

import com.sms.controller.SessionController;
import com.sms.data.DataStore;
import com.sms.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SessionManagerFrame extends JFrame {
    private JTextField venueField, typeField;
    private JList<String> studentList, evaluatorList;
    private SessionController controller;

    public SessionManagerFrame(Coordinator coordinator, DataStore dataStore) {
        this.controller = new SessionController(dataStore);
        setTitle("Session Management");
        setSize(500, 500);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Venue:"));
        venueField = new JTextField(); inputPanel.add(venueField);
        inputPanel.add(new JLabel("Session Type:"));
        typeField = new JTextField(); inputPanel.add(typeField);
        add(inputPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new GridLayout(1, 2));
        DefaultListModel<String> sModel = new DefaultListModel<>();
        for (User u : dataStore.getUsers()) if (u instanceof Student) sModel.addElement(u.getUserID());
        studentList = new JList<>(sModel);
        listPanel.add(new JScrollPane(studentList));

        DefaultListModel<String> eModel = new DefaultListModel<>();
        for (User u : dataStore.getUsers()) if (u instanceof Evaluator) eModel.addElement(u.getUserID());
        evaluatorList = new JList<>(eModel);
        listPanel.add(new JScrollPane(evaluatorList));
        add(listPanel, BorderLayout.CENTER);

        JButton createBtn = new JButton("Create Session");
        createBtn.addActionListener(e -> {
            controller.createNewSession(new Date(), venueField.getText(), typeField.getText(),
                                      studentList.getSelectedValuesList(), evaluatorList.getSelectedValuesList());
            JOptionPane.showMessageDialog(this, "Session and Assignments Saved!");
        });
        add(createBtn, BorderLayout.SOUTH);

        setVisible(true);
    }
}
