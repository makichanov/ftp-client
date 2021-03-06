package by.bsuir;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        Main.primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        stage.setTitle("Hello World");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}