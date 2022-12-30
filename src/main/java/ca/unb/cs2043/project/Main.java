package ca.unb.cs2043.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static Boolean isSplashLoaded = false;

    public static Boolean isSplashEnded = false;

    public static double stageWidth;

    public static double stageHeight;

    public static BorderPane bp = new BorderPane();

    public static SplashView splashView;
    static {
        try {
            splashView = new SplashView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) {
        new MainController();
        bp.setCenter(splashView);
        stage.setScene(new Scene(bp, 1200, 800));
        stage.setTitle("To-Do Prestige | CS2043 Project");
        stage.show();
        stageHeight = stage.getHeight();
        stageWidth = stage.getWidth();
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            stageHeight = stage.getHeight();
            if (isSplashEnded) {
                BorderPane home = (BorderPane) bp.getCenter();
                VBox left = (VBox) home.getRight();
                for (Node nodeIn: left.getChildren()) {
                    if (nodeIn instanceof ListView) {
                        ((ListView<?>) nodeIn).setPrefHeight(stageHeight - 170);
                    }
                }
                home.setPrefHeight(stageHeight);
            }
        });
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            stageWidth = stage.getWidth();
            if (isSplashEnded) {
                BorderPane home = (BorderPane) bp.getCenter();
                home.setPrefWidth(stageWidth);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
