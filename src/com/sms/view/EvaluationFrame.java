package com.sms.view;

import com.sms.controller.EvaluationController;
import com.sms.data.DataStore;
import com.sms.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EvaluationFrame extends JFrame {
    private JComboBox<String> studentCombo;
    private JTextArea detailsArea, commentsArea;
    private JTextField clarityField, methodologyField, resultsField, presentationField;
    private Evaluator evaluator;
    private EvaluationController controller;

    public EvaluationFrame(Evaluator evaluator, DataStore dataStore) {
        this.evaluator = evaluator;
        this.controller = new EvaluationController(dataStore);
        setTitle("Evaluator Panel - " + evaluator.getName());
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Student:"));
        studentCombo = new JComboBox<>();
        for (Student s : evaluator.getAssignedStudents()) {
            studentCombo.addItem(s.getUserID() + " - " + s.getName());
        }
        studentCombo.addActionListener(e -> updateStudentDetails());
        topPanel.add(studentCombo);
        add(topPanel, BorderLayout.NORTH);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setBorder(BorderFactory.createTitledBorder("Research Details"));
        add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JPanel scorePanel = new JPanel(new GridLayout(6, 2, 5, 5));
        scorePanel.add(new JLabel("Problem Clarity (0-25):"));
        clarityField = new JTextField(); scorePanel.add(clarityField);
        scorePanel.add(new JLabel("Methodology (0-25):"));
        methodologyField = new JTextField(); scorePanel.add(methodologyField);
        scorePanel.add(new JLabel("Results (0-25):"));
        resultsField = new JTextField(); scorePanel.add(resultsField);
        scorePanel.add(new JLabel("Presentation (0-25):"));
        presentationField = new JTextField(); scorePanel.add(presentationField);
        
        commentsArea = new JTextArea(3, 20);
        scorePanel.add(new JLabel("Comments:"));
        scorePanel.add(new JScrollPane(commentsArea));

        JButton submitBtn = new JButton("Submit Evaluation");
        submitBtn.addActionListener(e -> handleSubmit());
        scorePanel.add(submitBtn);

        add(scorePanel, BorderLayout.SOUTH);
        
        if (studentCombo.getItemCount() > 0) updateStudentDetails();
        setVisible(true);
    }

    private void updateStudentDetails() {
        String selected = (String) studentCombo.getSelectedItem();
        if (selected != null) {
            String id = selected.split(" - ")[0];
            Student s = controller.getStudentData(id);
            detailsArea.setText("Title: " + s.getResearchTitle() + "\n\nAbstract: " + s.getResearchAbstract());
        }
    }

    private void handleSubmit() {
        String selected = (String) studentCombo.getSelectedItem();
        if (selected != null) {
            String id = selected.split(" - ")[0];
            Student s = controller.getStudentData(id);
            controller.processEvaluation(s, 
                Double.parseDouble(clarityField.getText()),
                Double.parseDouble(methodologyField.getText()),
                Double.parseDouble(resultsField.getText()),
                Double.parseDouble(presentationField.getText()),
                commentsArea.getText());
            JOptionPane.showMessageDialog(this, "Evaluation Submitted!");
        }
    }
}
