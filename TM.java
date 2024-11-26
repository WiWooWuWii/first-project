
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TM extends JFrame {
    public TM() {

        setTitle("Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        JButton Login_Button = new JButton("Login");

        Login_Button.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (login(username, password)) {
                openTaskManager(username);
            } else {
                JOptionPane.showMessageDialog(TM.this, "Invalid username or password. Please try again.");
            }
        });

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(4, 2));
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel());
        loginPanel.add(Login_Button);
        loginPanel.add(new JLabel());

        setLayout(new BorderLayout());
        add(loginPanel, BorderLayout.CENTER);
    }

    private boolean login(String username, String password) {
        return username.equals("1") && password.equals("1");
    }

    private void openTaskManager(String username) {
        TaskManagerFrame TM = new TaskManagerFrame(username);
        TM.setVisible(true);
        dispose();                                                                                                      // Закрывает окно авторизации пользователя
    }

    private static class TaskManagerFrame extends JFrame {
        private final ArrayList<Task> tasks;
        private final JTable taskTable;
        private final DefaultTableModel tableModel;
        private final String username;

        public TaskManagerFrame(String username) {
            this.username = username;

            setTitle("Task Manager");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1200, 700);
            setLocationRelativeTo(null);

            tasks = new ArrayList<>();

            tableModel = new DefaultTableModel(new Object[]{"№", "Status", "Title", "Description", "Priority", "Due Date"}, 0);
            taskTable = new JTable(tableModel);

            JPanel buttonPanel = getjPanel();
            JScrollPane scrollPane = new JScrollPane(taskTable);

            add(scrollPane, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);

            getTextArea();

            loadTasksFromFile();
        }

        private void getTextArea() {                                                                                    // Вывод столбца description по клику мыши
            JTextArea textArea = new JTextArea("");
            textArea.setLineWrap(true);                                                                                 // Размещение текста в несколько строк
            textArea.setWrapStyleWord(true);                                                                            // длинные слова переносятся целиком
            add(new JScrollPane(textArea), "Center");

            MouseListener mouseListener = new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    int selectedRow = taskTable.getSelectedRow();

                    if (selectedRow != -1) {
                        Task task = tasks.get(selectedRow);
                        textArea.setText(null);
                        textArea.append(task.description);
                    }
                }
            };
            taskTable.addMouseListener(mouseListener);
        }

        private JPanel getjPanel() {
            JButton addButton = new JButton("Add");
            JButton addSubtaskButton = new JButton("Add Subtask");
            JButton editButton = new JButton("Edit");
            JButton completeButton = new JButton("Complete");
            JButton deleteButton = new JButton("Delete");

            addButton.addActionListener(e -> addTask());
            addSubtaskButton.addActionListener(e -> addSubtask());
            editButton.addActionListener(e -> editTask());
            completeButton.addActionListener(e -> completeTask());
            deleteButton.addActionListener(e -> deleteTask());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(addButton);
            buttonPanel.add(addSubtaskButton);
            buttonPanel.add(editButton);
            buttonPanel.add(completeButton);
            buttonPanel.add(deleteButton);

            return buttonPanel;
        }

        private void loadTasksFromFile() {

            try (BufferedReader reader = new BufferedReader(new FileReader(username + ".txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] taskData = line.split(",");
                    String number = taskData[0];
                    String status = taskData[1];
                    String title = taskData[2];
                    String description = taskData[3];
                    String priority = taskData[4];
                    String dueDate = taskData[5];
                    tasks.add(new Task(number, status, title, description, priority, dueDate));
                }
            } catch (IOException ignored) {
            }

            tasks.sort(new Comparator<>() {
                @Override
                public int compare(Task task1, Task task2) {

                    int priority1 = getPriorityValue(task1.priority);
                    int priority2 = getPriorityValue(task2.priority);
                    return Integer.compare(priority1, priority2);
                }

                private int getPriorityValue(String priority) {
                    return switch (priority) {
                        case "high" -> 0;
                        case "medium" -> 1;
                        case "low" -> 2;
                        default -> 3;
                    };
                }
            });

            for (Task task : tasks) {
                spinnerNumber();
                tableModel.addRow(new Object[]{task.number, task.status, task.title, task.description, task.priority, task.dueDate});
            }
        }

        private void addTask() {
            String number = String.valueOf(taskTable.getRowCount() + 1);
            String status = "NEW";
            String title = JOptionPane.showInputDialog(TaskManagerFrame.this, "Enter the task title:");
            String description = JOptionPane.showInputDialog(TaskManagerFrame.this, "Enter the task description:");

            String[] priorityOptions = {"high", "medium", "low"};
            String priority = (String) JOptionPane.showInputDialog(
                    TaskManagerFrame.this,
                    "Select the task priority:",
                    "Priority",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    priorityOptions,
                    "medium"
            );

            String dueDate;
            while (true) {
                dueDate = JOptionPane.showInputDialog(TaskManagerFrame.this, "Enter the task due date (yyyy-MM-dd):");
                if (isValidDateFormat(dueDate)) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(TaskManagerFrame.this, "Invalid date format. Please enter the date in yyyy-MM-dd format.");
                }
            }
            Task task = new Task(number, status, title, description, priority, dueDate);
            tasks.add(task);
            tableModel.addRow(new Object[]{task.number, task.status, task.title, task.description, task.priority, task.dueDate});

            saveTasksToFile();
        }

        private boolean isValidDateFormat(String date) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(date);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }

        private void addSubtask() {

        }

        private void editTask() {
            int selectedColumn = taskTable.getSelectedColumn();                                                         // Определяем колонку
            int selectedRow = taskTable.getSelectedRow();                                                               // Определяем строку

            switch (selectedColumn) {
                case 1 -> {
                    if (selectedRow != -1) {                                                                            // Номер строки от 0..., проверяем -1
                        Task task = tasks.get(selectedRow);                                                             // В конструктор Task вносим строки

                        if (task.status.equals("DONE")) {
                            String[] statusOptions = {"IN_PROGRESS", "DONE"};
                            task.status = (String) JOptionPane.showInputDialog(
                                    TaskManagerFrame.this,
                                    "Select the task status:",
                                    "Status",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    statusOptions,
                                    "IN_PROGRESS"
                            );

                            tableModel.setValueAt(task.status, selectedRow, selectedColumn);                            // вносим изменения в ячейку

                            saveTasksToFile();
                        }
                    }
                }
                case 2 -> {
                    if (selectedRow != -1) {                                                                            // Номер строки от 0..., проверяем -1
                        Task task = tasks.get(selectedRow);                                                             // В конструктор Task вносим строки

                        task.title = JOptionPane.showInputDialog(this, "Enter the new task title:", task.title);

                        tableModel.setValueAt(task.title, selectedRow, selectedColumn);                                 // вносим изменения в ячейку

                        saveTasksToFile();
                    }
                }
                case 3 -> {
                    if (selectedRow != -1) {                                                                            // Номер строки от 0..., проверяем -1
                        Task task = tasks.get(selectedRow);                                                             // В конструктор Task вносим строки

                        task.description = JOptionPane.showInputDialog(this, "Enter the new task description:", task.description);

                        tableModel.setValueAt(task.description, selectedRow, selectedColumn);                           // вносим изменения в ячейку

                        saveTasksToFile();
                    }
                }
                case 4 -> {
                    if (selectedRow != -1) {                                                                            // Номер строки от 0..., проверяем -1
                        Task task = tasks.get(selectedRow);                                                             // В конструктор Task вносим строки

                        String[] priorityOptions = {"high", "medium", "low"};
                        task.priority = (String) JOptionPane.showInputDialog(
                                TaskManagerFrame.this,
                                "Select the task priority:",
                                "Priority",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                priorityOptions,
                                "medium"
                        );

                        tableModel.setValueAt(task.priority, selectedRow, selectedColumn);                              // вносим изменения в ячейку

                        saveTasksToFile();
                    }
                }
                case 5 -> {
                    if (selectedRow != -1) {                                                                            // Номер строки от 0..., проверяем -1
                        Task task = tasks.get(selectedRow);                                                             // В конструктор Task вносим строки

                        while (true) {
                            task.dueDate = JOptionPane.showInputDialog(TaskManagerFrame.this, "Enter the task due date (yyyy-MM-dd):");
                            if (isValidDateFormat(task.dueDate)) {
                                break;
                            } else {
                                JOptionPane.showMessageDialog(TaskManagerFrame.this, "Invalid date format. Please enter the date in yyyy-mm-dd format.");
                            }
                        }

                        tableModel.setValueAt(task.dueDate, selectedRow, selectedColumn);                               // вносим изменения в ячейку

                        saveTasksToFile();
                    }
                }
                default -> saveTasksToFile();
            }
        }

        private void completeTask() {
            int selectedRow = taskTable.getSelectedRow();                                                               // Определяем строку

            if (selectedRow != -1) {                                                                                    // Номер строки от 0..., проверяем -1
                Task task = tasks.get(selectedRow);                                                                     // В конструктор Task вносим строки
                task.status = "DONE";

                tableModel.setValueAt(task.status, selectedRow, 1);                                        // вносим изменения в ячейку

                saveTasksToFile();
            }
        }

        private void deleteTask() {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                tasks.remove(selectedRow);
                tableModel.removeRow(selectedRow);

                saveTasksToFile();
            }
        }

        private void spinnerNumber() {
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                task.number = String.valueOf(i + 1);
            }
        }

        private void saveTasksToFile() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + ".txt"))) {
                for (Task task : tasks) {
                    if (task.status.equals("NEW")) {
                        task.status = "IN_PROGRESS";
                    }
                    writer.write(task.number + "," + task.status + "," + task.title.trim() + ","
                            + task.description.trim() + "," + task.priority + "," + task.dueDate.trim());        // trim - убирает пробелы
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(TaskManagerFrame.this, "Error occurred while saving tasks to file.");
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new TM().setVisible(true));
    }
}

