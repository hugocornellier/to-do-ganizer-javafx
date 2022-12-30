package ca.unb.cs2043.project;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainController implements Initializable {

    @FXML
    private BorderPane root;

    public MainController() {
        if (!Main.isSplashLoaded) {
            try {
                loadSplashScreen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Main.isSplashLoaded) {
            try {
                loadSplashScreen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadSplashScreen() throws IOException {
        System.out.println("loading splash screen..");
        Main.isSplashLoaded = true;
        final FXMLLoader[] fxmlLoader = {
                new FXMLLoader(Main.class.getResource("splash.fxml"))
        };
        StackPane pane = fxmlLoader[0].load();
        Main.bp.setCenter(pane);

        // Set background splash/load image
        Image image = new Image(Objects.requireNonNull(Main.class.getResource("todo-trans-cropped.png")).openStream());
        ImageView iv2 = new ImageView(image);
        pane.getChildren().add(iv2);

        // Display splash for 3 seconds, then show main view
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(1);
        fadeOut.play();
        fadeOut.setOnFinished((e) -> {
            Main.isSplashEnded = true;
            FXMLLoader f = new FXMLLoader(Main.class.getResource("home.fxml"));
            try {
                BorderPane home = f.load();
                if (Main.stageWidth < 800) {
                    Main.stageWidth = 800;
                }
                if (Main.stageHeight < 500) {
                    Main.stageHeight = 500;
                }
                home.setPrefHeight(Main.stageHeight);
                home.setPrefWidth(Main.stageWidth);
                Main.bp.setCenter(home);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
