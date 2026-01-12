package com.sms.view;

import com.sms.data.DataStore;
import com.sms.model.*;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private DataStore dataStore;

    public LoginFrame(DataStore dataStore) {
        this.dataStore = dataStore;
        setTitle("Seminar Management System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        add(new JLabel("Email:", SwingConstants.CENTER));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:", SwingConstants.CENTER));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        add(loginBtn);

        setVisible(true);
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        User user = dataStore.login(email, password);

        if (user != null) {
            dispose();
            if (user instanceof Student) new RegistrationFrame((Student) user, dataStore);
            else if (user instanceof Evaluator) new EvaluationFrame((Evaluator) user, dataStore);
            else if (user instanceof Coordinator) new MainCoordinatorFrame((Coordinator) user, dataStore);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!");
        }
    }
}
