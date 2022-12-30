module ca.unb.cs2043.project {
    requires javafx.controls;
    requires javafx.fxml;


    opens ca.unb.cs2043.project to javafx.fxml;
    exports ca.unb.cs2043.project;
}