package danmarksKort;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

class View {
    private static Stage stage;

    public View(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/GUI_v4.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("style.css");
            primaryStage.setScene(scene);
            stage = primaryStage;
            stage.setOnCloseRequest(e -> Platform.exit());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during  fxml load");
        }
    }

    public static Stage getStage() {
        return stage;
    }
}