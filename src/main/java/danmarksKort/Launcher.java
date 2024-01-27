package danmarksKort;

import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(Runtime.getRuntime().maxMemory());
        View view = new View(primaryStage);
        var controller = new ViewController();
    }
}