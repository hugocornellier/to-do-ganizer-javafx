package ca.unb.cs2043.project;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class SplashView extends BorderPane {

    BorderPane temp;

    public SplashView() throws IOException {
        final FXMLLoader[] fxmlLoader = {
                new FXMLLoader(Main.class.getResource("splash.fxml"))
        };
        StackPane pane = fxmlLoader[0].load();

        // Set background splash/load image
        Image image = new Image(Objects.requireNonNull(Main.class.getResource("todo-trans-cropped.png")).openStream());
        ImageView iv2 = new ImageView(image);
        pane.getChildren().add(iv2);
        this.setCenter(pane);
    }
}
