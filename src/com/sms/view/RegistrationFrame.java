package com.sms.view;

import com.sms.controller.SeminarController;
import com.sms.data.DataStore;
import com.sms.model.*;
import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    private JTextField titleField, supervisorField, filePathField;
    private JTextArea abstractArea;
    private JComboBox<Enums.PresentationType> typeCombo;
    private Student student;
    private SeminarController controller;

    public RegistrationFrame(Student student, DataStore dataStore) {
        this.student = student;
        this.controller = new SeminarController(dataStore);
        setTitle("Student Registration - " + student.getName());
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(7, 1, 5, 5));
        panel.add(new JLabel("Research Title:"));
        titleField = new JTextField();
        panel.add(titleField);

        panel.add(new JLabel("Supervisor Name:"));
        supervisorField = new JTextField();
        panel.add(supervisorField);

        panel.add(new JLabel("Presentation Type:"));
        typeCombo = new JComboBox<>(Enums.PresentationType.values());
        panel.add(typeCombo);

        panel.add(new JLabel("Material File Path:"));
        filePathField = new JTextField();
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseBtn, BorderLayout.EAST);
        panel.add(filePanel);

        add(panel, BorderLayout.NORTH);
        
        abstractArea = new JTextArea();
        abstractArea.setBorder(BorderFactory.createTitledBorder("Research Abstract"));
        add(new JScrollPane(abstractArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit Registration");
        submitBtn.addActionListener(e -> {
            controller.registerStudent(student, titleField.getText(), abstractArea.getText(), 
                                     supervisorField.getText(), filePathField.getText(), 
                                     (Enums.PresentationType) typeCombo.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Registration Successful!");
        });
        add(submitBtn, BorderLayout.SOUTH);

        setVisible(true);
    }
}
