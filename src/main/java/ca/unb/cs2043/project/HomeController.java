package ca.unb.cs2043.project;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import static java.time.temporal.ChronoUnit.DAYS;

public class HomeController implements Initializable {

    @FXML
    public VBox list_vbox;

    @FXML
    public Label my_lists;

    @FXML
    public BorderPane rootPane;

    private final ObservableList<String> observableList = FXCollections.observableArrayList();

    @FXML
    public Button open_list;

    @FXML
    public Button delete_list;

    @FXML
    public ListView<String> listView;

    @FXML
    public VBox alerts_vbox;

    @FXML
    public Label my_alerts;

    @FXML
    public VBox login_vbox;

    @FXML
    public Label login_label;

    @FXML
    public Button login_btn;

    Font title_font = Font.font("Courier", 18);

    Insets padding = new Insets(10, 50, 10, 10);

    String admin = "admin/admin.txt";

    String LISTS_FOLDER = "lists/";

    boolean alertsFound = false;

    enum LoginType {STUDENT, TA, PROFESSOR}

    LoginType loginType;

    ArrayList<ToDo> todos = new ArrayList<>();

    boolean incompleteListsExist = false;

    public void noListSelected() {
        rootPane.setCenter(new Label("No list selected."));
    }

    public class CustomComparator implements Comparator<ToDo> {
        @Override
        public int compare(ToDo o1, ToDo o2) {
            return o1.getDue_date().compareTo(o2.getDue_date());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initializing...");
        removeLineFromFile(admin, "");
        noListSelected();
        my_lists.setFont(title_font);
        my_alerts.setFont(title_font);
        login_label.setFont(title_font);
        list_vbox.setPadding(padding);
        alerts_vbox.setPadding(padding);
        login_vbox.setPadding(padding);
        String[] lines;

        // Alerts
        File folder = new File(LISTS_FOLDER);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try {
                    String list = txtFileToString(LISTS_FOLDER + file.getName());
                    lines = list.split("\\r?\\n");
                    for (String line : lines) {
                        if (line.length() > 0) {
                            String[] parts = line.split("\\|");
                            LocalDate duedate = LocalDate.parse(parts[1]);
                            boolean completed = !Objects.equals(parts[2], "incomplete");
                            ToDo todo = new ToDo(parts[0], duedate, completed);
                            todos.add(todo);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Collections.sort(todos, new CustomComparator());
        for (int i = 0; i < todos.size(); i++) {
            ToDo t = todos.get(i);
            long daysbetween = getDaysBetween(t.due_date.toString());
            System.out.println(t.title + " | " + t.due_date.toString() + " | " + daysbetween);
            if (daysbetween < 7) {
                alertsFound = true;
                Label alert = new Label();
                alert.setText(t.title + " is due in " + daysbetween + " days!");
                alerts_vbox.getChildren().add(alert);
            }
        }

        if (!alertsFound) {
            alerts_vbox.getChildren().add(new Label("Your alerts will appear here.\n\nAlerts are warnings that due\ndates are upcoming " +
                    "within 7\ndays, organized in order of\ndate.."));
        }

        // Check if admin.txt file is empty
        String lists_file;
        try {
            lists_file = getListsFile();
            lines = lists_file.split("\\r?\\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lists_file.length() == 0) {
            String no_lists = "No lists found. Create your first list!";
            list_vbox.getChildren().add(new Label(no_lists));
        }
        else {
            for (String line : lines) {
                String[] parts = line.split("\\|");
                observableList.add(parts[0]);
            }
            listView.setItems(observableList);
            listView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        open_list.setDisable(false);
                        delete_list.setDisable(false);
                    }
            );
        }
    }

    @FXML
    protected void create_list(ActionEvent e) {
        VBox create_list_vbox = new VBox();
        create_list_vbox.setPadding(padding);
        create_list_vbox.setSpacing(10);
        Label create_list_title = new Label("Create New List");
        create_list_title.setFont(title_font);
        TextField textfield = new TextField();
        textfield.setId("text_field");
        Button b = new Button();
        b.setText("Create");
        Label warning = new Label();
        create_list_vbox.getChildren().addAll(create_list_title,
                new Separator(), textfield, b, warning);
        b.setOnAction(event -> {
            String text_field_contents = textfield.getText();
            if (text_field_contents.length() == 0) {
                warning.setText("List name is blank. Add list title.");
            } else if (text_field_contents.length() > 15) {
                warning.setText("List name is too long. Must be <= 15 chars.");
            } else if (listNameIsBanned(text_field_contents)) {
                warning.setText("List name is unavailable. Choose a new one.");
            } else {
                try {
                    if (listNameAlreadyExists(text_field_contents)) {
                        warning.setText("List already exists. Use a new name.");
                    } else {
                        String path = Path.of(admin).toString();
                        String base_path = path.substring(0, path.length() - 9);
                        System.out.println(base_path);
                        File file1 = new File(LISTS_FOLDER + text_field_contents + ".txt");
                        try {
                            boolean result = file1.createNewFile();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        addLineToTxtFile(text_field_contents, path);
                        observableList.addAll(text_field_contents);
                        listView.setItems(observableList);
                        listView.getSelectionModel().select(observableList.size() - 1);
                        open_list();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        rootPane.setCenter(create_list_vbox);
    }

    private boolean listNameIsBanned(String text_field_contents) {
        return Objects.equals("admin", text_field_contents);
    }
    private boolean listNameAlreadyExists(String listName) throws IOException {
        String lists = getListsFile();
        String[] lines = lists.split("\\r?\\n");
        boolean res= false;
        for (String line : lines) {
            System.out.println("Comparing " + line + " to " + listName);
            if (Objects.equals(line, listName)) {
                res = true;
            }
        }
        return res;
    }

    public void addLineToTxtFile(String newLine, String path) {
        try (FileWriter fw = new FileWriter(path, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print("\n" + newLine);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    public String getListsFile() throws IOException {
        return txtFileToString(admin);
    }

    public String txtFileToString(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    public void delete_list(ActionEvent actionEvent) {
        VBox create_list_vbox = new VBox();
        create_list_vbox.setPadding(padding);
        create_list_vbox.setSpacing(10);
        Label create_list_title = new Label("Confirm Deletion");
        create_list_title.setFont(title_font);
        Button b = new Button();
        b.setText("Delete");
        create_list_vbox.getChildren().addAll(create_list_title, new Separator(),
                new Label("This action is not reversible."), b);
        b.setOnAction(event -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            removeLineFromFile(admin, selected);
            File myList = new File(LISTS_FOLDER + selected + ".txt");
            if (myList.isFile()) {
                if (myList.delete()) {
                    System.out.println("Deleted the list: " + LISTS_FOLDER + selected + ".txt");
                } else {
                    System.out.println("Failed to delete the list.");
                }
            }
            observableList.remove(selected);
            listView.setItems(observableList);
            noListSelected();
        });
        rootPane.setCenter(create_list_vbox);
    }

    public void removeLineFromFile(String file, String lineToRemove) {
        try {
            File inFile = new File(file);
            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }
            File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
            BufferedReader br = new BufferedReader(new FileReader(file));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals(lineToRemove)) {
                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();
            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }
            if (!tempFile.renameTo(inFile)) {
                System.out.println("Could not rename file");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public long getDaysBetween(String duedate) {
        LocalDate dateToday = LocalDate.now();
        LocalDate dateAfter = LocalDate.parse(duedate);
        return DAYS.between(dateToday, dateAfter);
    }

    public void open_list() throws IOException {
        VBox open_list_vbox = new VBox();
        open_list_vbox.setPadding(padding);
        open_list_vbox.setSpacing(10);
        String list_name = listView.getSelectionModel().getSelectedItem();
        System.out.println("Opening list..: " + list_name);
        Label create_list_title = new Label(list_name);
        create_list_title.setFont(title_font);
        open_list_vbox.getChildren().addAll(create_list_title, new Separator());
        String to_do = txtFileToString(LISTS_FOLDER + list_name + ".txt");
        VBox incomplete = new VBox();
        incomplete.getChildren().add(new Label("To Do:"));
        VBox completed = new VBox();
        completed.getChildren().add(new Label("Completed:"));
        if (to_do.length() == 0) {
            open_list_vbox.getChildren().add(new Label("You haven't added any to-dos yet."));
        } else {
            open_list_vbox.getChildren().addAll(incomplete, completed);
            String[] lines = to_do.split("\\r?\\n");
            for (String line : lines) {
                if (line.length() > 0) {
                    String[] lines2 = line.split("\\|");
                    if (Objects.equals(lines2[2], "incomplete")) {
                        HBox w = new HBox();
                        Button addgradebtn = new Button("Add Grade");
                        w.setSpacing(20);
                        long daysBetween = getDaysBetween(lines2[1]);
                        Label todo = new Label(lines2[0]);
                        String days_str = daysBetween + " days left!";
                        Label days = new Label(days_str);
                        days.setAlignment(Pos.BASELINE_RIGHT);
                        CheckBox c = new CheckBox();
                        c.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                            if (new_val) {
                                incomplete.getChildren().remove(w);
                                if (isLoggedInProfessor() || isLoggedInTA())
                                    w.getChildren().add(addgradebtn);
                                completed.getChildren().add(w);
                                days.setText("");
                                removeLineFromFile(LISTS_FOLDER + list_name + ".txt", lines2[0] + "|" + lines2[1] + "|incomplete");
                                addLineToTxtFile(lines2[0] + "|" + lines2[1] + "|completed", LISTS_FOLDER + list_name + ".txt");
                            }
                            if (!new_val) {
                                incomplete.getChildren().add(w);
                                w.getChildren().remove(addgradebtn);
                                completed.getChildren().remove(w);
                                days.setText(days_str);
                                removeLineFromFile(LISTS_FOLDER + list_name + ".txt", lines2[0] + "|" + lines2[1] + "|completed");
                                addLineToTxtFile(lines2[0] + "|" + lines2[1] + "|incomplete", LISTS_FOLDER + list_name + ".txt");
                            }
                        });
                        Label ungraded = new Label("ungraded");
                        addgradebtn.setOnAction(event -> {
                            System.out.println("Adding grade");
                            addgradebtn.setDisable(true);
                            TextField gradeTextField = new TextField();
                            Button saveGradeBtn = new Button("Save");
                            saveGradeBtn.setOnAction(event2 -> {
                                addgradebtn.setDisable(false);
                                w.getChildren().removeAll(gradeTextField, saveGradeBtn);
                                ungraded.setText(gradeTextField.getText() + "%");
                            });
                            w.getChildren().addAll(gradeTextField, saveGradeBtn);
                        });
                        w.getChildren().addAll(c, todo, days, ungraded);
                        incomplete.getChildren().add(w);
                    } else {
                        HBox w = new HBox();
                        Button addgradebtn = new Button("Add Grade");
                        w.setSpacing(20);
                        long daysBetween = getDaysBetween(lines2[1]);
                        Label todo = new Label(lines2[0]);
                        String days_str = daysBetween + " days left!";
                        Label days = new Label();
                        days.setAlignment(Pos.BASELINE_RIGHT);
                        CheckBox c = new CheckBox();
                        c.setSelected(true);
                        c.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                            if (new_val) {
                                incomplete.getChildren().remove(w);
                                if (isLoggedInProfessor() || isLoggedInTA())
                                    w.getChildren().add(addgradebtn);
                                completed.getChildren().add(w);
                                days.setText("");
                                removeLineFromFile(LISTS_FOLDER + list_name + ".txt", lines2[0] + "|" + lines2[1] + "|incomplete|ungraded");
                                addLineToTxtFile(lines2[0] + "|" + lines2[1] + "|completed|ungraded", LISTS_FOLDER + list_name + ".txt");
                            }
                            if (!new_val) {
                                incomplete.getChildren().add(w);
                                w.getChildren().remove(addgradebtn);
                                completed.getChildren().remove(w);
                                days.setText(days_str);
                                removeLineFromFile(LISTS_FOLDER + list_name + ".txt", lines2[0] + "|" + lines2[1] + "|completed|ungraded");
                                addLineToTxtFile(lines2[0] + "|" + lines2[1] + "|incomplete|ungraded", LISTS_FOLDER + list_name + ".txt");
                            }
                        });
                        Label ungraded = new Label("ungraded");
                        addgradebtn.setOnAction(event -> {
                            System.out.println("Adding grade");
                            addgradebtn.setDisable(true);
                            TextField gradeTextField = new TextField();
                            Button saveGradeBtn = new Button("Save");
                            saveGradeBtn.setOnAction(event2 -> {
                                addgradebtn.setDisable(false);
                                w.getChildren().removeAll(gradeTextField, saveGradeBtn);
                                ungraded.setText(gradeTextField.getText() + "%");
                            });
                            w.getChildren().addAll(gradeTextField, saveGradeBtn);
                        });
                        w.getChildren().addAll(c, todo, days, ungraded);
                        if (isLoggedInProfessor() || isLoggedInTA())
                            w.getChildren().add(addgradebtn);
                        completed.getChildren().add(w);
                    }
                }
            }
        }
        Button b = new Button("Create To-do");
        open_list_vbox.getChildren().addAll(new Separator(), b);
        b.setOnAction(event -> {
            b.setDisable(true);
            Button addgradebtn = new Button("Add Grade");
            HBox create = new HBox();
            DatePicker d = new DatePicker();
            TextField t = new TextField();
            create.getChildren().addAll(new Label("To-Do:     "), t);
            HBox create2 = new HBox();
            create2.getChildren().addAll(new Label("Due Date: "), d);
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            Label warning = new Label();
            save.setOnAction(event2 -> {
                String title = t.getText();
                if (d.getValue() == null) {
                    warning.setText("Enter a date. The date cannot be blank.");
                } else if (title.length() == 0) {
                    warning.setText("Enter a title. Title cannot be blank.");
                } else if (!todoDoesntExist(title)) {
                    warning.setText("Todo already exists. You cannot re-use names. Please choose a new name.");
                } else {
                    String todo = t.getText() + "|" + d.getValue() + "|incomplete|ungraded";
                    addLineToTxtFile(todo, LISTS_FOLDER + list_name + ".txt");
                    HBox w = new HBox();
                    w.setSpacing(20);
                    long daysBetween = getDaysBetween(String.valueOf(d.getValue()));
                    Label todo2 = new Label(t.getText());
                    String days_str = daysBetween + " days left!";
                    Label days = new Label(days_str);
                    days.setAlignment(Pos.BASELINE_RIGHT);
                    CheckBox c = new CheckBox();
                    c.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                        if (new_val) {
                            incomplete.getChildren().remove(w);
                            if (isLoggedInProfessor() || isLoggedInTA())
                                w.getChildren().add(addgradebtn);
                            completed.getChildren().add(w);
                            days.setText("");
                            removeLineFromFile(LISTS_FOLDER + list_name + ".txt", todo);
                            addLineToTxtFile(t.getText() + "|" + d.getValue() + "|completed|ungraded", LISTS_FOLDER + list_name + ".txt");
                        }
                        if (!new_val) {
                            incomplete.getChildren().add(w);
                            w.getChildren().remove(addgradebtn);
                            completed.getChildren().remove(w);
                            days.setText(days_str);
                            removeLineFromFile(LISTS_FOLDER + list_name + ".txt", t.getText() + "|" + d.getValue() + "|completed|ungraded");
                            addLineToTxtFile(todo, LISTS_FOLDER + list_name + ".txt");
                        }
                    });
                    Label ungraded = new Label("ungraded");
                    addgradebtn.setOnAction(event3 -> {
                        System.out.println("Adding grade");
                        addgradebtn.setDisable(true);
                        TextField gradeTextField = new TextField();
                        Button saveGradeBtn = new Button("Save");
                        saveGradeBtn.setOnAction(event4 -> {
                            addgradebtn.setDisable(false);
                            w.getChildren().removeAll(gradeTextField, saveGradeBtn);
                            ungraded.setText(gradeTextField.getText() + "%");
                        });
                        w.getChildren().addAll(gradeTextField, saveGradeBtn);
                    });
                    w.getChildren().addAll(c, todo2, days, ungraded);
                    incomplete.getChildren().add(w);
                    b.setDisable(false);
                    open_list_vbox.getChildren().removeAll(create, create2, save, cancel, warning);
                }
            });
            cancel.setOnAction(event2 -> {
                b.setDisable(false);
                open_list_vbox.getChildren().removeAll(create, create2, save, cancel, warning);
            });
            open_list_vbox.getChildren().addAll(create, create2, save, cancel, warning);
        });
        rootPane.setCenter(open_list_vbox);
    }
    public boolean todoDoesntExist(String title) {
        File folder = new File(LISTS_FOLDER);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try {
                    String list = txtFileToString(LISTS_FOLDER + file.getName());
                    String [] lines = list.split("\\r?\\n");
                    for (String line : lines) {
                        if (line.length() > 0) {
                            String[] parts = line.split("\\|");
                            if (Objects.equals(parts[0], title)) {
                                return false;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    public void login_to_system (ActionEvent e) {
        VBox v = new VBox();
        v.setPadding(padding);
        v.setSpacing(20);
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().add(v);
        Scene secondScene = new Scene(secondaryLayout, 400, 300);
        Label login_title = new Label("Login");

        VBox outer = new VBox();
        outer.setPadding(padding);
        outer.setSpacing(20);
        outer.setAlignment(Pos.CENTER);
        VBox inner1 = new VBox();
        inner1.setPadding(padding);
        inner1.setSpacing(20);
        VBox inner2 = new VBox();
        inner2.setPadding(padding);
        inner2.setSpacing(20);
        VBox inner3 = new VBox();
        inner3.setPadding(padding);
        inner3.setSpacing(20);

        Button student = new Button("Login as Student");
        student.setAlignment(Pos.CENTER);

        Button professor = new Button("Login as Professor");
        professor.setAlignment(Pos.CENTER);

        Button TA = new Button("Login as TA");
        TA.setAlignment(Pos.CENTER);

        inner1.getChildren().addAll(student);
        inner2.getChildren().addAll(professor);
        inner3.getChildren().addAll(TA);
        outer.getChildren().addAll(inner1, inner3, inner2);

        login_title.setFont(title_font);
        v.getChildren().addAll(login_title, outer);
        Stage newWindow = new Stage();
        newWindow.setTitle("Login");
        newWindow.setScene(secondScene);
        newWindow.setX(Main.stageWidth / 2);
        newWindow.setY(Main.stageHeight / 2);
        newWindow.show();

        student.setOnAction(event -> successfulLogin(LoginType.STUDENT, newWindow));
        TA.setOnAction(actionEvent -> successfulLogin(LoginType.TA, newWindow));
        professor.setOnAction(actionEvent -> successfulLogin(LoginType.PROFESSOR, newWindow));
    }

    public void successfulLogin(LoginType loginTypeIn, Stage newWindow) {
        login_vbox.getChildren().remove(login_btn);
        loginType = loginTypeIn;
        String newLoginType = loginTypeIn.toString().substring(0, 1).toUpperCase() + loginTypeIn.toString().toLowerCase().substring(1);
        Label loggedInAs = new Label("Logged in as " + newLoginType);
        Button logout = new Button("Logout");
        logout.setOnAction(e -> {
            login_vbox.getChildren().removeAll(logout, loggedInAs);
            login_vbox.getChildren().add(login_btn);
            loginType = null;
        });
        login_vbox.getChildren().addAll(loggedInAs, logout);
        newWindow.close();
    }

    public boolean isLoggedInStudent() {
        if (loginType == null)
            return false;
        return Objects.equals(loginType.toString(), "STUDENT");
    }

    public boolean isLoggedInProfessor() {
        if (loginType == null)
            return false;
        return Objects.equals(loginType.toString(), "PROFESSOR");
    }

    public boolean isLoggedInTA() {
        if (loginType == null)
            return false;
        return Objects.equals(loginType.toString(), "TA");
    }

    public void about_us() throws IOException {
        StackPane secondaryLayout = new StackPane();
        VBox v = new VBox();
        v.setAlignment(Pos.CENTER);
        Image image = new Image(Objects.requireNonNull(Main.class.getResource("todo-trans-cropped.png")).openStream());
        ImageView iv2 = new ImageView(image);
        v.getChildren().addAll(iv2, new Label("App Designed By:" +
                "\nHugo Cornellier, Daphne Dairo-Singerr, Kristyn Le, Md Jahidur Rahman Jahid"));
        secondaryLayout.getChildren().add(v);
        Scene secondScene = new Scene(secondaryLayout, 400, 300);
        Stage newWindow = new Stage();
        newWindow.setTitle("About Us");
        newWindow.setScene(secondScene);
        newWindow.setX(Main.stageWidth / 2);
        newWindow.setY(Main.stageHeight / 2);
        newWindow.show();
    }

    public void help() {
        VBox v = new VBox();
        v.setPadding(padding);
        v.setSpacing(20);
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().add(v);
        Scene secondScene = new Scene(secondaryLayout, 400, 300);
        Label help_title = new Label("Help");
        help_title.setFont(title_font);
        Label help_contents = new Label();
        help_contents.setText("To begin, create a list by clicking on New\n" +
                "at the bottom of the lists section (right side).\n" +
                "\n" +
                "To open a list, select the list in the view on\n" +
                "the right side, and then click the 'Open List'\n" +
                "button.\n" +
                "\n" +
                "To add a ToDo item, click the create button after\n" +
                "opening a list, then fill out the form.\n" +
                "\n" +
                "You will need to add a Title, as well as a date.\n" +
                "Initially, your item will be marked 'incomplete',\n" +
                "but you may mark it as completed when you finish it.\n" +
                "\n" +
                "This application will provide you with Alerts\n" +
                "when a due date is nearing. If the due date\n" +
                "is within 7 days, you will be notified at the\n" +
                "top left of your app. The due dates will be sorted\n" +
                "by date. " +
                "\n" +
                "To delete a list, select the list on the right and\n" +
                "click the 'Delete List' button.\n");
        v.getChildren().addAll(help_title, help_contents);
        Stage newWindow = new Stage();
        newWindow.setTitle("Help");
        newWindow.setScene(secondScene);
        newWindow.setX(Main.stageWidth / 2);
        newWindow.setY(Main.stageHeight / 2);
        newWindow.show();
    }

}
