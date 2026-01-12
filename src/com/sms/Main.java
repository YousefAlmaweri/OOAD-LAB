package com.sms;

import com.sms.data.DataStore;
import com.sms.view.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataStore dataStore = DataStore.load();
            new LoginFrame(dataStore);
        });
    }
}
