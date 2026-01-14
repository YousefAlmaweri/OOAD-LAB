package Lab_Exercise;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// =========================
// Core App Controller
// =========================
class App {
    private final JFrame frame;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final DataStore store;

    private User currentUser;

    // views
    private LoginPanel loginPanel;
    private StudentPanel studentPanel;
    private EvaluatorPanel evaluatorPanel;
    private CoordinatorPanel coordinatorPanel;

    App() {
        store = DataStore.loadOrCreate();

        frame = new JFrame("FCI Postgraduate Seminar Management System");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this, store);
        studentPanel = new StudentPanel(this, store);
        evaluatorPanel = new EvaluatorPanel(this, store);
        coordinatorPanel = new CoordinatorPanel(this, store);

        root.add(loginPanel, "LOGIN");
        root.add(studentPanel, "STUDENT");
        root.add(evaluatorPanel, "EVALUATOR");
        root.add(coordinatorPanel, "COORDINATOR");

        frame.setContentPane(root);
    }

    void show() {
        cards.show(root, "LOGIN");
        frame.setVisible(true);
    }

    JFrame getFrame() { return frame; }

    void login(User user) {
        currentUser = user;

        if (user.role == Role.STUDENT) {
            studentPanel.refresh();
            cards.show(root, "STUDENT");
        } else if (user.role == Role.EVALUATOR) {
            evaluatorPanel.refresh();
            cards.show(root, "EVALUATOR");
        } else {
            coordinatorPanel.refresh();
            cards.show(root, "COORDINATOR");
        }
    }

    void logout() {
        currentUser = null;
        loginPanel.clear();
        cards.show(root, "LOGIN");
    }

    User getCurrentUser() { return currentUser; }
}

// =========================
// Login Panel
// =========================
class LoginPanel extends JPanel {
    private final App app;
    private final DataStore store;

    private final JTextField username = UI.tf(18);
    private final JPasswordField password = new JPasswordField(18);

    LoginPanel(App app, DataStore store) {
        this.app = app;
        this.store = store;
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(520, 360));

        JLabel title = new JLabel("Seminar Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Login with your username and password");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JPanel top = new JPanel(new GridLayout(2, 1, 4, 4));
        top.add(title);
        top.add(subtitle);
        card.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Username"), g);
        g.gridx = 1;
        form.add(username, g);

        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("Password"), g);
        g.gridx = 1;
        password.setFont(password.getFont().deriveFont(14f));
        form.add(password, g);

        JButton loginBtn = UI.btn("Login");
        JButton resetBtn = UI.btn("Reset");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(resetBtn);
        actions.add(loginBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        add(card);

        loginBtn.addActionListener(e -> doLogin());
        resetBtn.addActionListener(e -> clear());

        // Enter to login
        password.addActionListener(e -> doLogin());
    }

    void clear() {
        username.setText("");
        password.setText("");
        username.requestFocusInWindow();
    }

    private void doLogin() {
        String u = username.getText().trim();
        String p = new String(password.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            UI.err(this, "Please enter username and password.");
            return;
        }

        User user = store.findUserByUsername(u);
        if (user == null || !Objects.equals(user.password, p)) {
            UI.err(this, "Invalid username or password.");
            return;
        }

        app.login(user);
    }
}

// =========================
// Base Panel w/ top bar
// =========================
abstract class RolePanel extends JPanel {
    protected final App app;
    protected final DataStore store;

    RolePanel(App app, DataStore store) {
        this.app = app;
        this.store = store;
    }

    protected JComponent topRightBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel who = new JLabel();
        who.setFont(who.getFont().deriveFont(Font.PLAIN, 13f));
        JButton logout = UI.btn("Logout");

        logout.addActionListener(e -> {
            store.save();
            app.logout();
        });

        javax.swing.Timer t = new javax.swing.Timer(400, e -> {
            User cu = app.getCurrentUser();
            if (cu != null) who.setText(cu.fullName + " • " + cu.role);
        });
        t.start();

        p.add(who);
        p.add(logout);
        return p;
    }

    abstract void refresh();
}

// =========================
// Student Panel
// =========================
class StudentPanel extends RolePanel {
    private final JTextField title = UI.tf(30);
    private final JTextArea abstractText = UI.ta(6, 30);
    private final JTextField supervisor = UI.tf(30);
    private final JComboBox<String> preferred = UI.combo("ORAL", "POSTER");
    private final JTextField filePath = UI.tf(25);
    private final JTextField posterBoardId = UI.tf(10);

    private final JLabel status = new JLabel(" ");

    private final JTable scheduleTable = new JTable();
    private final ScheduleTableModel scheduleModel;

    StudentPanel(App app, DataStore store) {
        super(app, store);

        scheduleModel = new ScheduleTableModel(store);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.add(buildRegistrationCard(), BorderLayout.WEST);
        content.add(buildRightSide(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(UI.page("Student Dashboard", content, topRightBar()), BorderLayout.CENTER);
    }

    private JComponent buildRegistrationCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createTitledBorder("Registration & Submission"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Research Title"), g);
        g.gridx = 1; form.add(title, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Abstract"), g);
        g.gridx = 1;
        JScrollPane sp = new JScrollPane(abstractText);
        sp.setPreferredSize(new Dimension(340, 120));
        form.add(sp, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Supervisor"), g);
        g.gridx = 1; form.add(supervisor, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Preferred Type"), g);
        g.gridx = 1; form.add(preferred, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Material File Path"), g);
        g.gridx = 1;
        JPanel fp = new JPanel(new BorderLayout(6, 0));
        fp.add(filePath, BorderLayout.CENTER);
        JButton browse = UI.btn("Browse");
        fp.add(browse, BorderLayout.EAST);
        form.add(fp, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Poster Board ID (if Poster)"), g);
        g.gridx = 1; form.add(posterBoardId, g);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(status, BorderLayout.CENTER);

        JButton saveBtn = UI.btn("Save / Update Submission");
        bottom.add(saveBtn, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                filePath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        saveBtn.addActionListener(e -> saveSubmission());

        return panel;
    }

    private JComponent buildRightSide() {
        JPanel right = new JPanel(new BorderLayout(12, 12));

        JPanel card1 = new JPanel(new BorderLayout(8, 8));
        card1.setBorder(BorderFactory.createTitledBorder("Your Schedule & Session"));

        scheduleTable.setModel(scheduleModel);
        scheduleTable.setRowHeight(26);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        scheduleTable.getColumnModel().getColumn(0).setCellRenderer(center);

        card1.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        JPanel card2 = new JPanel(new BorderLayout(8, 8));
        card2.setBorder(BorderFactory.createTitledBorder("People’s Choice Vote"));
        JLabel hint = new JLabel("Vote for ONE presenter (can be yourself, but normally should not).");
        hint.setFont(hint.getFont().deriveFont(13f));

        JComboBox<Submission> voteCombo = new JComboBox<>();
        JButton voteBtn = UI.btn("Submit Vote");

        JPanel v = new JPanel(new BorderLayout(8, 8));
        v.add(hint, BorderLayout.NORTH);

        JPanel vv = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vv.add(voteCombo);
        vv.add(voteBtn);
        v.add(vv, BorderLayout.CENTER);

        card2.add(v, BorderLayout.CENTER);

        voteBtn.addActionListener(e -> {
            Submission s = (Submission) voteCombo.getSelectedItem();
            if (s == null) { UI.err(this, "No submissions available to vote."); return; }
            store.peoplesChoiceVotes.put(s.id, store.peoplesChoiceVotes.getOrDefault(s.id, 0) + 1);
            store.save();
            UI.info(this, "Vote recorded!");
        });

        right.add(card1, BorderLayout.CENTER);
        right.add(card2, BorderLayout.SOUTH);

        // refresh combo content on refresh()
        right.putClientProperty("voteCombo", voteCombo);

        return right;
    }

    private void saveSubmission() {
        User me = app.getCurrentUser();
        if (me == null) return;

        String t = title.getText().trim();
        String abs = abstractText.getText().trim();
        String sup = supervisor.getText().trim();
        String pref = (String) preferred.getSelectedItem();
        String path = filePath.getText().trim();
        String board = posterBoardId.getText().trim();

        if (t.isEmpty() || abs.isEmpty() || sup.isEmpty() || pref == null) {
            UI.err(this, "Please fill: title, abstract, supervisor, preferred type.");
            return;
        }

        Submission sub = store.findSubmissionByStudent(me.id);
        if (sub == null) {
            sub = new Submission(me.id);
            store.submissions.add(sub);
        }

        sub.researchTitle = t;
        sub.abstractText = abs;
        sub.supervisorName = sup;
        sub.preferredType = PresentationType.valueOf(pref);
        sub.materialFilePath = path;
        sub.posterBoardId = (sub.preferredType == PresentationType.POSTER) ? board : "";

        sub.submittedDate = LocalDate.now();
        store.save();

        status.setText("Saved on " + sub.submittedDate);
        UI.info(this, "Submission saved successfully.");
        refresh();
    }

    @Override
    void refresh() {
        User me = app.getCurrentUser();
        if (me == null) return;

        Submission sub = store.findSubmissionByStudent(me.id);
        if (sub == null) {
            title.setText("");
            abstractText.setText("");
            supervisor.setText("");
            preferred.setSelectedIndex(0);
            filePath.setText("");
            posterBoardId.setText("");
            status.setText("No submission yet.");
        } else {
            title.setText(UI.safe(sub.researchTitle));
            abstractText.setText(UI.safe(sub.abstractText));
            supervisor.setText(UI.safe(sub.supervisorName));
            preferred.setSelectedItem(sub.preferredType.name());
            filePath.setText(UI.safe(sub.materialFilePath));
            posterBoardId.setText(UI.safe(sub.posterBoardId));
            status.setText("Last saved: " + sub.submittedDate);
        }

        scheduleModel.setStudentId(me.id);
        scheduleModel.fireTableDataChanged();

        // update vote combo
        Component[] comps = findAllComponents(this);
        JComboBox<?> voteCombo = null;
        for (Component c : comps) {
            if (c instanceof JPanel p) {
                Object prop = p.getClientProperty("voteCombo");
                if (prop instanceof JComboBox<?> cb) { voteCombo = cb; break; }
            }
        }
        // fallback: search by clientProperty on right side (we set it there)
        if (voteCombo == null) {
            // try to find it by scanning all combos and matching item type
            for (Component c : comps) {
                if (c instanceof JComboBox<?> cb) {
                    // ignore preferred combo
                    if (cb == preferred) continue;
                    voteCombo = cb;
                    break;
                }
            }
        }
        if (voteCombo instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<Submission> vc = (JComboBox<Submission>) voteCombo;
            vc.removeAllItems();
            for (Submission s : store.submissions) vc.addItem(s);
            vc.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Submission s) {
                        User stu = store.findUser(s.studentUserId);
                        setText((stu != null ? stu.fullName : "Unknown") + " • " + s.preferredType + " • " + s.researchTitle);
                    }
                    return this;
                }
            });
        }
    }

    private Component[] findAllComponents(Container c) {
        List<Component> list = new ArrayList<>();
        Deque<Container> stack = new ArrayDeque<>();
        stack.push(c);
        while (!stack.isEmpty()) {
            Container cur = stack.pop();
            for (Component comp : cur.getComponents()) {
                list.add(comp);
                if (comp instanceof Container cc) stack.push(cc);
            }
        }
        return list.toArray(new Component[0]);
    }
}

// Shows student's schedule (session + time slot)
class ScheduleTableModel extends AbstractTableModel {
    private final DataStore store;
    private UUID studentId;

    private final String[] cols = {"Time", "Date", "Venue", "Session Type"};

    ScheduleTableModel(DataStore store) { this.store = store; }

    void setStudentId(UUID id) { this.studentId = id; }

    @Override public int getRowCount() {
        if (studentId == null) return 0;
        Submission sub = store.findSubmissionByStudent(studentId);
        if (sub == null) return 0;

        // Find any slot in any session where this submission is scheduled
        for (Session ses : store.sessions) {
            for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
                if (e.getValue() != null && e.getValue().equals(sub.id)) return 1;
            }
        }
        return 0;
    }

    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        if (studentId == null) return "";
        Submission sub = store.findSubmissionByStudent(studentId);
        if (sub == null) return "";

        for (Session ses : store.sessions) {
            for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
                if (e.getValue() != null && e.getValue().equals(sub.id)) {
                    return switch (columnIndex) {
                        case 0 -> e.getKey().format(DateTimeFormatter.ofPattern("HH:mm"));
                        case 1 -> ses.date.toString();
                        case 2 -> ses.venue;
                        case 3 -> ses.sessionType.toString();
                        default -> "";
                    };
                }
            }
        }
        return "";
    }
}

// =========================
// Evaluator Panel
// =========================
class EvaluatorPanel extends RolePanel {
    private final JTable assignedTable = new JTable();
    private final AssignedTableModel assignedModel;

    private final JTextArea detailsArea = UI.ta(10, 40);
    private final JSpinner spProblem = UI.spinnerInt(1, 5, 3);
    private final JSpinner spMethod = UI.spinnerInt(1, 5, 3);
    private final JSpinner spResults = UI.spinnerInt(1, 5, 3);
    private final JSpinner spPresentation = UI.spinnerInt(1, 5, 3);
    private final JTextArea commentArea = UI.ta(5, 40);

    private UUID selectedSubmissionId;

    EvaluatorPanel(App app, DataStore store) {
        super(app, store);
        assignedModel = new AssignedTableModel(store);

        setLayout(new BorderLayout());
        add(UI.page("Evaluator Dashboard", buildContent(), topRightBar()), BorderLayout.CENTER);
    }

    private JComponent buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.55);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createTitledBorder("Assigned Presentations"));

        assignedTable.setModel(assignedModel);
        assignedTable.setRowHeight(26);
        assignedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<AssignedTableModel> sorter = new TableRowSorter<>(assignedModel);
        assignedTable.setRowSorter(sorter);

        JTextField search = UI.tf(20);
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String txt = search.getText().trim();
                if (txt.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);

        left.add(top, BorderLayout.NORTH);
        left.add(new JScrollPane(assignedTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createTitledBorder("Evaluation (Rubrics + Comments)"));

        detailsArea.setEditable(false);
        commentArea.setText("");

        JPanel evalForm = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; evalForm.add(new JLabel("Problem Clarity (1-5)"), g);
        g.gridx = 1; evalForm.add(spProblem, g);

        y++;
        g.gridx = 0; g.gridy = y; evalForm.add(new JLabel("Methodology (1-5)"), g);
        g.gridx = 1; evalForm.add(spMethod, g);

        y++;
        g.gridx = 0; g.gridy = y; evalForm.add(new JLabel("Results (1-5)"), g);
        g.gridx = 1; evalForm.add(spResults, g);

        y++;
        g.gridx = 0; g.gridy = y; evalForm.add(new JLabel("Presentation (1-5)"), g);
        g.gridx = 1; evalForm.add(spPresentation, g);

        y++;
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        evalForm.add(new JLabel("Comments"), g);

        y++;
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        JScrollPane csp = new JScrollPane(commentArea);
        csp.setPreferredSize(new Dimension(420, 110));
        evalForm.add(csp, g);

        JButton save = UI.btn("Save Evaluation");
        JButton openFile = UI.btn("Open Material Path");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(openFile);
        actions.add(save);

        JPanel topRight = new JPanel(new BorderLayout(8, 8));
        JScrollPane dsp = new JScrollPane(detailsArea);
        dsp.setPreferredSize(new Dimension(450, 160));
        topRight.add(dsp, BorderLayout.CENTER);

        right.add(topRight, BorderLayout.NORTH);
        right.add(evalForm, BorderLayout.CENTER);
        right.add(actions, BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);

        assignedTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = assignedTable.getSelectedRow();
            if (row < 0) return;
            int modelRow = assignedTable.convertRowIndexToModel(row);
            selectedSubmissionId = assignedModel.getSubmissionIdAt(modelRow);
            loadSubmissionForEvaluation();
        });

        save.addActionListener(e -> saveEvaluation());
        openFile.addActionListener(e -> openMaterialPath());

        return split;
    }

    private void loadSubmissionForEvaluation() {
        User me = app.getCurrentUser();
        if (me == null || selectedSubmissionId == null) return;

        Submission s = store.findSubmission(selectedSubmissionId);
        if (s == null) return;

        User stu = store.findUser(s.studentUserId);
        StringBuilder sb = new StringBuilder();
        sb.append("Presenter: ").append(stu != null ? stu.fullName : "Unknown").append("\n");
        sb.append("Type: ").append(s.preferredType).append("\n");
        sb.append("Title: ").append(s.researchTitle).append("\n");
        sb.append("Supervisor: ").append(s.supervisorName).append("\n");
        sb.append("Poster Board ID: ").append(UI.safe(s.posterBoardId)).append("\n");
        sb.append("Material Path: ").append(UI.safe(s.materialFilePath)).append("\n\n");
        sb.append("Abstract:\n").append(s.abstractText).append("\n");

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);

        Evaluation ev = store.findEvaluation(me.id, s.id);
        if (ev != null) {
            spProblem.setValue(ev.problemClarity);
            spMethod.setValue(ev.methodology);
            spResults.setValue(ev.results);
            spPresentation.setValue(ev.presentation);
            commentArea.setText(UI.safe(ev.comments));
        } else {
            spProblem.setValue(3);
            spMethod.setValue(3);
            spResults.setValue(3);
            spPresentation.setValue(3);
            commentArea.setText("");
        }
    }

    private void saveEvaluation() {
        User me = app.getCurrentUser();
        if (me == null) return;
        if (selectedSubmissionId == null) {
            UI.err(this, "Select a presentation from the assigned list first.");
            return;
        }

        Submission s = store.findSubmission(selectedSubmissionId);
        if (s == null) { UI.err(this, "Submission not found."); return; }

        Evaluation ev = store.findEvaluation(me.id, s.id);
        if (ev == null) {
            ev = new Evaluation();
            ev.evaluatorUserId = me.id;
            ev.submissionId = s.id;
            store.evaluations.add(ev);
        }

        ev.problemClarity = (int) spProblem.getValue();
        ev.methodology = (int) spMethod.getValue();
        ev.results = (int) spResults.getValue();
        ev.presentation = (int) spPresentation.getValue();
        ev.comments = commentArea.getText().trim();
        ev.submittedDate = LocalDate.now();

        store.save();
        UI.info(this, "Evaluation saved successfully.");
        refresh();
    }

    private void openMaterialPath() {
        if (selectedSubmissionId == null) { UI.err(this, "Select a presentation first."); return; }
        Submission s = store.findSubmission(selectedSubmissionId);
        if (s == null) return;
        if (s.materialFilePath == null || s.materialFilePath.isBlank()) {
            UI.err(this, "No material file path submitted by student.");
            return;
        }
        // We won't actually open a file (OS-specific). Just show the path.
        UI.info(this, "Material path:\n" + s.materialFilePath);
    }

    @Override
    void refresh() {
        User me = app.getCurrentUser();
        if (me == null) return;
        assignedModel.setEvaluatorId(me.id);
        assignedModel.fireTableDataChanged();
        selectedSubmissionId = null;
        detailsArea.setText("");
        commentArea.setText("");
    }
}

class AssignedTableModel extends AbstractTableModel {
    private final DataStore store;
    private UUID evaluatorId;

    private final String[] cols = {"Presenter", "Type", "Title", "Session", "Avg Score", "Your Status"};

    AssignedTableModel(DataStore store) { this.store = store; }
    void setEvaluatorId(UUID id) { this.evaluatorId = id; }

    private List<UUID> assignedSubmissionIds() {
        if (evaluatorId == null) return List.of();
        List<UUID> out = new ArrayList<>();
        for (Session s : store.sessions) {
            List<UUID> list = s.evaluatorToSubmissions.get(evaluatorId);
            if (list != null) out.addAll(list);
        }
        // remove duplicates
        return out.stream().distinct().collect(Collectors.toList());
    }

    UUID getSubmissionIdAt(int row) {
        return assignedSubmissionIds().get(row);
    }

    @Override public int getRowCount() { return assignedSubmissionIds().size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        UUID sid = assignedSubmissionIds().get(rowIndex);
        Submission sub = store.findSubmission(sid);
        if (sub == null) return "";

        User stu = store.findUser(sub.studentUserId);

        String sessionName = "-";
        for (Session ses : store.sessions) {
            if (ses.timeSlotToSubmission.containsValue(sid)) {
                // find time
                String time = ses.timeSlotToSubmission.entrySet().stream()
                        .filter(e -> sid.equals(e.getValue()))
                        .map(e -> e.getKey().format(DateTimeFormatter.ofPattern("HH:mm")))
                        .findFirst().orElse("--:--");
                sessionName = ses.date + " " + time + " @ " + ses.venue;
                break;
            }
        }

        double avg = store.avgSubmissionScore(sid);
        boolean done = (evaluatorId != null && store.findEvaluation(evaluatorId, sid) != null);

        return switch (columnIndex) {
            case 0 -> (stu != null ? stu.fullName : "Unknown");
            case 1 -> sub.preferredType.toString();
            case 2 -> sub.researchTitle;
            case 3 -> sessionName;
            case 4 -> UI.fmtScore(avg);
            case 5 -> done ? "Evaluated" : "Pending";
            default -> "";
        };
    }
}

// =========================
// Coordinator Panel
// =========================
class CoordinatorPanel extends RolePanel {

    private final JTabbedPane tabs = new JTabbedPane();

    // User management
    private final JTable userTable = new JTable();
    private final UserTableModel userModel;

    private final JTextField uUsername = UI.tf(14);
    private final JTextField uFullName = UI.tf(18);
    private final JPasswordField uPassword = new JPasswordField(14);
    private final JComboBox<String> uRole = UI.combo("STUDENT", "EVALUATOR", "COORDINATOR");

    // Sessions
    private final JTable sessionTable = new JTable();
    private final SessionTableModel sessionModel;

    private final JTextField sDate = UI.tf(10);   // yyyy-mm-dd
    private final JTextField sVenue = UI.tf(16);
    private final JComboBox<String> sType = UI.combo("ORAL", "POSTER");

    private final JTextField slotTime = UI.tf(6); // HH:mm

    // Assignments
    private final JComboBox<Session> assignSessionCombo = new JComboBox<>();
    private final JComboBox<User> assignEvaluatorCombo = new JComboBox<>();
    private final JComboBox<Submission> assignSubmissionCombo = new JComboBox<>();
    private final JTextArea assignPreview = UI.ta(10, 38);

    // Reports
    private final JTextArea reportArea = UI.ta(18, 60);

    CoordinatorPanel(App app, DataStore store) {
        super(app, store);
        userModel = new UserTableModel(store);
        sessionModel = new SessionTableModel(store);

        setLayout(new BorderLayout());
        add(UI.page("Coordinator Dashboard", buildContent(), topRightBar()), BorderLayout.CENTER);
    }

    private JComponent buildContent() {
        tabs.addTab("User Management", buildUserTab());
        tabs.addTab("Sessions & Slots", buildSessionTab());
        tabs.addTab("Assignments", buildAssignmentTab());
        tabs.addTab("Awards & Reports", buildReportsTab());
        return tabs;
    }

    // -----------------
    // User Management
    // -----------------
    private JComponent buildUserTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        userTable.setModel(userModel);
        userTable.setRowHeight(26);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Create New User"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Username"), g);
        g.gridx = 1; form.add(uUsername, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Full Name"), g);
        g.gridx = 1; form.add(uFullName, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Password"), g);
        g.gridx = 1;
        uPassword.setFont(uPassword.getFont().deriveFont(14f));
        form.add(uPassword, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Role"), g);
        g.gridx = 1; form.add(uRole, g);

        JButton add = UI.btn("Add User");
        JButton delete = UI.btn("Delete Selected");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(delete);
        actions.add(add);

        add.addActionListener(e -> addUser());
        delete.addActionListener(e -> deleteSelectedUser());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void addUser() {
        String username = uUsername.getText().trim();
        String fullName = uFullName.getText().trim();
        String password = new String(uPassword.getPassword());
        String role = (String) uRole.getSelectedItem();

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || role == null) {
            UI.err(this, "Fill all user fields.");
            return;
        }
        if (store.findUserByUsername(username) != null) {
            UI.err(this, "Username already exists.");
            return;
        }

        store.users.add(new User(username, password, fullName, Role.valueOf(role)));
        store.save();

        uUsername.setText("");
        uFullName.setText("");
        uPassword.setText("");

        userModel.fireTableDataChanged();
        refreshCombos();
        UI.info(this, "User added successfully.");
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UI.err(this, "Select a user to delete."); return; }

        int modelRow = userTable.convertRowIndexToModel(row);
        User u = userModel.getAt(modelRow);
        if (u == null) return;

        // prevent deleting yourself
        User me = app.getCurrentUser();
        if (me != null && me.id.equals(u.id)) {
            UI.err(this, "You cannot delete the currently logged-in user.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete user: " + u.fullName + " (" + u.username + ")?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // remove related assignments/evals/submissions
        store.users.removeIf(x -> x.id.equals(u.id));
        store.submissions.removeIf(s -> s.studentUserId.equals(u.id));
        store.evaluations.removeIf(ev -> ev.evaluatorUserId.equals(u.id));
        for (Session ses : store.sessions) {
            ses.evaluatorToSubmissions.remove(u.id);
            // clean submissions removed
            ses.timeSlotToSubmission.entrySet().removeIf(e -> store.findSubmission(e.getValue()) == null);
            for (Map.Entry<UUID, List<UUID>> e : ses.evaluatorToSubmissions.entrySet()) {
                e.getValue().removeIf(sid -> store.findSubmission(sid) == null);
            }
        }

        store.save();
        userModel.fireTableDataChanged();
        sessionModel.fireTableDataChanged();
        refreshCombos();
        UI.info(this, "User deleted and related data cleaned.");
    }

    // -----------------
    // Sessions
    // -----------------
    private JComponent buildSessionTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        sessionTable.setModel(sessionModel);
        sessionTable.setRowHeight(26);

        JPanel create = new JPanel(new GridBagLayout());
        create.setBorder(BorderFactory.createTitledBorder("Create Session"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; create.add(new JLabel("Date (YYYY-MM-DD)"), g);
        g.gridx = 1; create.add(sDate, g);

        y++;
        g.gridx = 0; g.gridy = y; create.add(new JLabel("Venue"), g);
        g.gridx = 1; create.add(sVenue, g);

        y++;
        g.gridx = 0; g.gridy = y; create.add(new JLabel("Session Type"), g);
        g.gridx = 1; create.add(sType, g);

        JButton createBtn = UI.btn("Create Session");
        JButton deleteBtn = UI.btn("Delete Selected Session");

        JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        slotPanel.setBorder(BorderFactory.createTitledBorder("Add Time Slot (HH:mm) to Selected Session"));
        slotPanel.add(new JLabel("Time:"));
        slotPanel.add(slotTime);
        JButton addSlot = UI.btn("Add Slot");
        slotPanel.add(addSlot);

        createBtn.addActionListener(e -> createSession());
        deleteBtn.addActionListener(e -> deleteSession());
        addSlot.addActionListener(e -> addTimeSlotToSelected());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(create, BorderLayout.CENTER);

        JPanel topRight = new JPanel(new BorderLayout(8, 8));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(deleteBtn);
        btns.add(createBtn);
        topRight.add(btns, BorderLayout.NORTH);
        topRight.add(slotPanel, BorderLayout.CENTER);

        top.add(topRight, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);

        return panel;
    }

    private void createSession() {
        String d = sDate.getText().trim();
        String v = sVenue.getText().trim();
        String t = (String) sType.getSelectedItem();

        if (d.isEmpty() || v.isEmpty() || t == null) {
            UI.err(this, "Fill session date, venue, type.");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(d);
            Session ses = new Session(date, v, SessionType.valueOf(t));
            // provide some default slots (optional): not auto — keep manual for control
            store.sessions.add(ses);
            store.save();
            sessionModel.fireTableDataChanged();
            refreshCombos();
            UI.info(this, "Session created.");
        } catch (Exception ex) {
            UI.err(this, "Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private void deleteSession() {
        int row = sessionTable.getSelectedRow();
        if (row < 0) { UI.err(this, "Select a session to delete."); return; }

        int modelRow = sessionTable.convertRowIndexToModel(row);
        Session ses = sessionModel.getAt(modelRow);
        if (ses == null) return;

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete session: " + ses.displayName() + " ?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        store.sessions.removeIf(x -> x.id.equals(ses.id));
        store.save();
        sessionModel.fireTableDataChanged();
        refreshCombos();
        UI.info(this, "Session deleted.");
    }

    private void addTimeSlotToSelected() {
        int row = sessionTable.getSelectedRow();
        if (row < 0) { UI.err(this, "Select a session first."); return; }

        int modelRow = sessionTable.convertRowIndexToModel(row);
        Session ses = sessionModel.getAt(modelRow);
        if (ses == null) return;

        String timeTxt = slotTime.getText().trim();
        if (timeTxt.isEmpty()) { UI.err(this, "Enter time (HH:mm)."); return; }

        try {
            LocalTime time = LocalTime.parse(timeTxt);
            if (ses.timeSlotToSubmission.containsKey(time)) {
                UI.err(this, "This time slot already exists.");
                return;
            }
            ses.timeSlotToSubmission.put(time, null);
            // keep sorted by time
            ses.timeSlotToSubmission = ses.timeSlotToSubmission.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue,
                            (a,b)->a, LinkedHashMap::new
                    ));

            store.save();
            sessionModel.fireTableDataChanged();
            refreshCombos();
            UI.info(this, "Time slot added.");
        } catch (Exception ex) {
            UI.err(this, "Invalid time. Use HH:mm (e.g., 09:30).");
        }
    }

    // -----------------
    // Assignments Tab
    // -----------------
    private JComponent buildAssignmentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder("Assign Presenter & Evaluator"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; top.add(new JLabel("Session"), g);
        g.gridx = 1; top.add(assignSessionCombo, g);

        y++;
        g.gridx = 0; g.gridy = y; top.add(new JLabel("Presenter Submission"), g);
        g.gridx = 1; top.add(assignSubmissionCombo, g);

        y++;
        g.gridx = 0; g.gridy = y; top.add(new JLabel("Evaluator"), g);
        g.gridx = 1; top.add(assignEvaluatorCombo, g);

        JButton assignToSlot = UI.btn("Assign Presenter to Next Available Slot");
        JButton assignEval = UI.btn("Assign Evaluator to Presenter");
        JButton clearSlot = UI.btn("Remove Presenter From Slot");
        JButton refresh = UI.btn("Refresh Lists");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(refresh);
        btns.add(clearSlot);
        btns.add(assignEval);
        btns.add(assignToSlot);

        assignPreview.setEditable(false);
        JScrollPane previewSP = new JScrollPane(assignPreview);
        previewSP.setBorder(BorderFactory.createTitledBorder("Session Preview"));

        panel.add(top, BorderLayout.NORTH);
        panel.add(previewSP, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refresh());
        assignSessionCombo.addActionListener(e -> updatePreview());
        assignToSlot.addActionListener(e -> assignPresenterToNextSlot());
        assignEval.addActionListener(e -> assignEvaluatorToPresenter());
        clearSlot.addActionListener(e -> removePresenterFromSession());

        return panel;
    }

    private void updatePreview() {
        Session ses = (Session) assignSessionCombo.getSelectedItem();
        if (ses == null) { assignPreview.setText("No session selected."); return; }

        StringBuilder sb = new StringBuilder();
        sb.append("SESSION: ").append(ses.displayName()).append("\n\n");
        sb.append("Time Slots:\n");
        for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
            String time = e.getKey().format(DateTimeFormatter.ofPattern("HH:mm"));
            UUID sid = e.getValue();
            if (sid == null) {
                sb.append("  ").append(time).append("  - [EMPTY]\n");
            } else {
                Submission sub = store.findSubmission(sid);
                User stu = (sub != null) ? store.findUser(sub.studentUserId) : null;
                sb.append("  ").append(time).append("  - ")
                        .append(stu != null ? stu.fullName : "Unknown")
                        .append(" | ").append(sub != null ? sub.preferredType : "?")
                        .append(" | ").append(sub != null ? sub.researchTitle : "Missing Submission")
                        .append("\n");
            }
        }

        sb.append("\nEvaluator Assignments:\n");
        if (ses.evaluatorToSubmissions.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (Map.Entry<UUID, List<UUID>> e : ses.evaluatorToSubmissions.entrySet()) {
                User eval = store.findUser(e.getKey());
                sb.append("  ").append(eval != null ? eval.fullName : "Unknown Evaluator").append(":\n");
                for (UUID sid : e.getValue()) {
                    Submission sub = store.findSubmission(sid);
                    User stu = (sub != null) ? store.findUser(sub.studentUserId) : null;
                    sb.append("     - ")
                            .append(stu != null ? stu.fullName : "Unknown")
                            .append(" | ").append(sub != null ? sub.researchTitle : "Missing")
                            .append("\n");
                }
            }
        }

        assignPreview.setText(sb.toString());
        assignPreview.setCaretPosition(0);
    }

    private void assignPresenterToNextSlot() {
        Session ses = (Session) assignSessionCombo.getSelectedItem();
        Submission sub = (Submission) assignSubmissionCombo.getSelectedItem();
        if (ses == null || sub == null) { UI.err(this, "Select session and submission."); return; }

        // Ensure type matches session type
        if ((ses.sessionType == SessionType.ORAL && sub.preferredType != PresentationType.ORAL) ||
            (ses.sessionType == SessionType.POSTER && sub.preferredType != PresentationType.POSTER)) {
            UI.err(this, "Submission type does not match session type.");
            return;
        }

        // ensure submission not already scheduled anywhere else
        for (Session other : store.sessions) {
            if (other.timeSlotToSubmission.containsValue(sub.id)) {
                UI.err(this, "This submission is already scheduled in another session.");
                return;
            }
        }

        for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
            if (e.getValue() == null) {
                ses.timeSlotToSubmission.put(e.getKey(), sub.id);
                store.save();
                sessionModel.fireTableDataChanged();
                updatePreview();
                UI.info(this, "Presenter assigned to slot: " + e.getKey().format(DateTimeFormatter.ofPattern("HH:mm")));
                return;
            }
        }

        UI.err(this, "No empty time slots. Add more time slots first.");
    }

    private void removePresenterFromSession() {
        Session ses = (Session) assignSessionCombo.getSelectedItem();
        Submission sub = (Submission) assignSubmissionCombo.getSelectedItem();
        if (ses == null || sub == null) { UI.err(this, "Select session and submission."); return; }

        boolean removed = false;
        for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
            if (sub.id.equals(e.getValue())) {
                ses.timeSlotToSubmission.put(e.getKey(), null);
                removed = true;
                break;
            }
        }
        if (!removed) {
            UI.err(this, "This submission is not in the selected session.");
            return;
        }

        // remove evaluator links to this submission in that session
        for (Map.Entry<UUID, List<UUID>> en : ses.evaluatorToSubmissions.entrySet()) {
            en.getValue().removeIf(id -> id.equals(sub.id));
        }

        store.save();
        updatePreview();
        UI.info(this, "Presenter removed from session slots and evaluator links cleaned.");
    }

    private void assignEvaluatorToPresenter() {
        Session ses = (Session) assignSessionCombo.getSelectedItem();
        Submission sub = (Submission) assignSubmissionCombo.getSelectedItem();
        User eval = (User) assignEvaluatorCombo.getSelectedItem();
        if (ses == null || sub == null || eval == null) { UI.err(this, "Select session, submission, evaluator."); return; }

        if (eval.role != Role.EVALUATOR) {
            UI.err(this, "Selected user is not an evaluator.");
            return;
        }

        // must be scheduled in session
        if (!ses.timeSlotToSubmission.containsValue(sub.id)) {
            UI.err(this, "Presenter must be scheduled in this session before assigning evaluator.");
            return;
        }

        ses.evaluatorToSubmissions.putIfAbsent(eval.id, new ArrayList<>());
        List<UUID> list = ses.evaluatorToSubmissions.get(eval.id);
        if (!list.contains(sub.id)) list.add(sub.id);

        store.save();
        updatePreview();
        UI.info(this, "Evaluator assigned.");
    }

    // -----------------
    // Reports Tab
    // -----------------
    private JComponent buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        reportArea.setEditable(false);
        JScrollPane sp = new JScrollPane(reportArea);

        JButton computeAwards = UI.btn("Compute Winners");
        JButton generateSchedule = UI.btn("Generate Schedule Report");
        JButton generateFinalReport = UI.btn("Generate Final Evaluation Report");
        JButton exportCSV = UI.btn("Export Reports to CSV");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(exportCSV);
        top.add(generateFinalReport);
        top.add(generateSchedule);
        top.add(computeAwards);

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        computeAwards.addActionListener(e -> computeWinners());
        generateSchedule.addActionListener(e -> showScheduleReport());
        generateFinalReport.addActionListener(e -> showFinalReport());
        exportCSV.addActionListener(e -> exportAllCSVs());

        return panel;
    }

    private void computeWinners() {
        // Best Oral: highest avg score among ORAL (with at least 1 evaluation)
        UUID bestOral = null;
        double bestOralScore = -1;

        UUID bestPoster = null;
        double bestPosterScore = -1;

        for (Submission s : store.submissions) {
            double avg = store.avgSubmissionScore(s.id);
            int evalCount = store.evaluationsForSubmission(s.id).size();
            if (evalCount == 0) continue; // require at least 1 evaluation

            if (s.preferredType == PresentationType.ORAL && avg > bestOralScore) {
                bestOralScore = avg;
                bestOral = s.id;
            }
            if (s.preferredType == PresentationType.POSTER && avg > bestPosterScore) {
                bestPosterScore = avg;
                bestPoster = s.id;
            }
        }

        // People’s Choice: highest votes (ties -> highest avg score)
        UUID pc = null;
        int bestVotes = -1;
        double tieAvg = -1;

        for (Submission s : store.submissions) {
            int votes = store.peoplesChoiceCount(s.id);
            double avg = store.avgSubmissionScore(s.id);
            if (votes > bestVotes) {
                bestVotes = votes;
                tieAvg = avg;
                pc = s.id;
            } else if (votes == bestVotes && votes >= 0) {
                // tie-breaker on avg score
                if (avg > tieAvg) {
                    tieAvg = avg;
                    pc = s.id;
                }
            }
        }

        store.winners.bestOralSubmissionId = bestOral;
        store.winners.bestPosterSubmissionId = bestPoster;
        store.winners.peoplesChoiceSubmissionId = pc;
        store.winners.computedOn = LocalDate.now();
        store.save();

        UI.info(this, "Winners computed. Generate reports to view details.");
        showFinalReport();
    }

    private void showScheduleReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SEMINAR SCHEDULE REPORT ===\n");
        sb.append("Generated on: ").append(LocalDate.now()).append("\n\n");

        List<Session> sorted = new ArrayList<>(store.sessions);
        sorted.sort(Comparator.comparing((Session s) -> s.date).thenComparing(s -> s.venue));

        for (Session ses : sorted) {
            sb.append("Session: ").append(ses.displayName()).append("\n");
            if (ses.timeSlotToSubmission.isEmpty()) {
                sb.append("  (No slots created)\n\n");
                continue;
            }
            for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
                String time = e.getKey().format(DateTimeFormatter.ofPattern("HH:mm"));
                UUID sid = e.getValue();
                if (sid == null) {
                    sb.append("  ").append(time).append(" - [EMPTY]\n");
                } else {
                    Submission sub = store.findSubmission(sid);
                    User stu = (sub != null) ? store.findUser(sub.studentUserId) : null;
                    sb.append("  ").append(time).append(" - ")
                            .append(stu != null ? stu.fullName : "Unknown")
                            .append(" | ").append(sub != null ? sub.researchTitle : "Missing")
                            .append(" | ").append(sub != null ? sub.preferredType : "?")
                            .append("\n");
                }
            }
            sb.append("\n");
        }

        reportArea.setText(sb.toString());
        reportArea.setCaretPosition(0);
    }

    private void showFinalReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FINAL EVALUATION REPORT ===\n");
        sb.append("Generated on: ").append(LocalDate.now()).append("\n\n");

        sb.append("Winners (computed on ").append(store.winners.computedOn).append(")\n");
        sb.append(" - Best Oral: ").append(winnerLine(store.winners.bestOralSubmissionId)).append("\n");
        sb.append(" - Best Poster: ").append(winnerLine(store.winners.bestPosterSubmissionId)).append("\n");
        sb.append(" - People’s Choice: ").append(winnerLine(store.winners.peoplesChoiceSubmissionId)).append("\n\n");

        sb.append("Submissions Summary:\n");
        for (Submission s : store.submissions) {
            User stu = store.findUser(s.studentUserId);
            double avg = store.avgSubmissionScore(s.id);
            int votes = store.peoplesChoiceCount(s.id);
            int evalCount = store.evaluationsForSubmission(s.id).size();

            sb.append("- ").append(stu != null ? stu.fullName : "Unknown")
                    .append(" | ").append(s.preferredType)
                    .append(" | Avg Score: ").append(UI.fmtScore(avg))
                    .append(" (").append(evalCount).append(" evals)")
                    .append(" | Votes: ").append(votes)
                    .append("\n  Title: ").append(s.researchTitle)
                    .append("\n  Supervisor: ").append(s.supervisorName)
                    .append("\n  Poster Board: ").append(UI.safe(s.posterBoardId))
                    .append("\n");
            List<Evaluation> evs = store.evaluationsForSubmission(s.id);
            if (evs.isEmpty()) {
                sb.append("  Evaluations: (none)\n\n");
            } else {
                sb.append("  Evaluations:\n");
                for (Evaluation e : evs) {
                    User ev = store.findUser(e.evaluatorUserId);
                    sb.append("   * ").append(ev != null ? ev.fullName : "Unknown Evaluator")
                            .append(" | PC=").append(e.problemClarity)
                            .append(" M=").append(e.methodology)
                            .append(" R=").append(e.results)
                            .append(" P=").append(e.presentation)
                            .append(" | Avg=").append(UI.fmtScore(e.avgScore()))
                            .append("\n     Comment: ").append(UI.safe(e.comments)).append("\n");
                }
                sb.append("\n");
            }
        }

        // basic analytics
        sb.append("=== BASIC ANALYTICS ===\n");
        long total = store.submissions.size();
        long oral = store.submissions.stream().filter(x -> x.preferredType == PresentationType.ORAL).count();
        long poster = total - oral;

        sb.append("Total Submissions: ").append(total).append("\n");
        sb.append("Oral: ").append(oral).append(" | Poster: ").append(poster).append("\n\n");

        reportArea.setText(sb.toString());
        reportArea.setCaretPosition(0);
    }

    private String winnerLine(UUID submissionId) {
        if (submissionId == null) return "(none)";
        Submission s = store.findSubmission(submissionId);
        if (s == null) return "(missing submission)";
        User stu = store.findUser(s.studentUserId);
        return (stu != null ? stu.fullName : "Unknown") + " • " + s.preferredType +
                " • Avg " + UI.fmtScore(store.avgSubmissionScore(s.id)) +
                " • Votes " + store.peoplesChoiceCount(s.id) +
                " • " + s.researchTitle;
    }

    private void exportAllCSVs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        if (!dir.exists() && !dir.mkdirs()) {
            UI.err(this, "Cannot create directory.");
            return;
        }

        try {
            exportUsersCSV(new File(dir, "users.csv"));
            exportScheduleCSV(new File(dir, "schedule.csv"));
            exportEvaluationsCSV(new File(dir, "evaluations.csv"));
            exportAwardsCSV(new File(dir, "awards.csv"));
            UI.info(this, "Exported CSV files to:\n" + dir.getAbsolutePath());
        } catch (Exception ex) {
            UI.err(this, "Export failed: " + ex.getMessage());
        }
    }

    private void exportUsersCSV(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("username,fullName,role\n");
        for (User u : store.users) {
            sb.append(csv(u.username)).append(",")
                    .append(csv(u.fullName)).append(",")
                    .append(csv(u.role.toString())).append("\n");
        }
        writeUtf8(file, sb.toString());
    }

    private void exportScheduleCSV(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("sessionDate,venue,sessionType,time,presenter,submissionType,title\n");

        List<Session> sorted = new ArrayList<>(store.sessions);
        sorted.sort(Comparator.comparing((Session s) -> s.date).thenComparing(s -> s.venue));

        for (Session ses : sorted) {
            for (Map.Entry<LocalTime, UUID> e : ses.timeSlotToSubmission.entrySet()) {
                String time = e.getKey().format(DateTimeFormatter.ofPattern("HH:mm"));
                UUID sid = e.getValue();
                String presenter = "";
                String stype = "";
                String title = "";
                if (sid != null) {
                    Submission sub = store.findSubmission(sid);
                    User stu = (sub != null) ? store.findUser(sub.studentUserId) : null;
                    presenter = (stu != null ? stu.fullName : "Unknown");
                    stype = (sub != null ? sub.preferredType.toString() : "");
                    title = (sub != null ? sub.researchTitle : "");
                }
                sb.append(csv(ses.date.toString())).append(",")
                        .append(csv(ses.venue)).append(",")
                        .append(csv(ses.sessionType.toString())).append(",")
                        .append(csv(time)).append(",")
                        .append(csv(presenter)).append(",")
                        .append(csv(stype)).append(",")
                        .append(csv(title)).append("\n");
            }
        }
        writeUtf8(file, sb.toString());
    }

    private void exportEvaluationsCSV(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("evaluator,presenter,submissionType,title,problemClarity,methodology,results,presentation,avgScore,comments\n");
        for (Evaluation e : store.evaluations) {
            User ev = store.findUser(e.evaluatorUserId);
            Submission sub = store.findSubmission(e.submissionId);
            User stu = (sub != null) ? store.findUser(sub.studentUserId) : null;
            sb.append(csv(ev != null ? ev.fullName : "Unknown")).append(",")
                    .append(csv(stu != null ? stu.fullName : "Unknown")).append(",")
                    .append(csv(sub != null ? sub.preferredType.toString() : "")).append(",")
                    .append(csv(sub != null ? sub.researchTitle : "")).append(",")
                    .append(e.problemClarity).append(",")
                    .append(e.methodology).append(",")
                    .append(e.results).append(",")
                    .append(e.presentation).append(",")
                    .append(UI.fmtScore(e.avgScore())).append(",")
                    .append(csv(e.comments)).append("\n");
        }
        writeUtf8(file, sb.toString());
    }

    private void exportAwardsCSV(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("award,winner\n");
        sb.append(csv("Best Oral")).append(",").append(csv(winnerLine(store.winners.bestOralSubmissionId))).append("\n");
        sb.append(csv("Best Poster")).append(",").append(csv(winnerLine(store.winners.bestPosterSubmissionId))).append("\n");
        sb.append(csv("People's Choice")).append(",").append(csv(winnerLine(store.winners.peoplesChoiceSubmissionId))).append("\n");
        writeUtf8(file, sb.toString());
    }

    private static void writeUtf8(File file, String content) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\n") || t.contains("\r")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    // -----------------
    // Refresh everything
    // -----------------
    @Override
    void refresh() {
        userModel.fireTableDataChanged();
        sessionModel.fireTableDataChanged();
        refreshCombos();
        updatePreview();
    }

    private void refreshCombos() {
        // sessions
        assignSessionCombo.removeAllItems();
        for (Session s : store.sessions) assignSessionCombo.addItem(s);
        assignSessionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Session s) setText(s.displayName());
                return this;
            }
        });

        // evaluators
        assignEvaluatorCombo.removeAllItems();
        for (User u : store.users) if (u.role == Role.EVALUATOR) assignEvaluatorCombo.addItem(u);

        // submissions
        assignSubmissionCombo.removeAllItems();
        for (Submission s : store.submissions) assignSubmissionCombo.addItem(s);
        assignSubmissionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Submission s) {
                    User stu = store.findUser(s.studentUserId);
                    setText((stu != null ? stu.fullName : "Unknown") + " • " + s.preferredType + " • " + s.researchTitle);
                }
                return this;
            }
        });
    }
}

class UserTableModel extends AbstractTableModel {
    private final DataStore store;
    private final String[] cols = {"Username", "Full Name", "Role"};

    UserTableModel(DataStore store) { this.store = store; }

    User getAt(int row) {
        if (row < 0 || row >= store.users.size()) return null;
        return store.users.get(row);
    }

    @Override public int getRowCount() { return store.users.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        User u = store.users.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> u.username;
            case 1 -> u.fullName;
            case 2 -> u.role.toString();
            default -> "";
        };
    }
}

class SessionTableModel extends AbstractTableModel {
    private final DataStore store;
    private final String[] cols = {"Date", "Venue", "Type", "#Slots", "Filled Slots", "#Eval Assignments"};

    SessionTableModel(DataStore store) { this.store = store; }

    Session getAt(int row) {
        if (row < 0 || row >= store.sessions.size()) return null;
        return store.sessions.get(row);
    }

    @Override public int getRowCount() { return store.sessions.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        Session s = store.sessions.get(rowIndex);
        long filled = s.timeSlotToSubmission.values().stream().filter(Objects::nonNull).count();
        int assignments = s.evaluatorToSubmissions.values().stream().mapToInt(List::size).sum();
        return switch (columnIndex) {
            case 0 -> s.date.toString();
            case 1 -> s.venue;
            case 2 -> s.sessionType.toString();
            case 3 -> s.timeSlotToSubmission.size();
            case 4 -> filled;
            case 5 -> assignments;
            default -> "";
        };
    }
}
