package danmarksKort;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AlertBox {


    public static void display(String title, String message) {
        Button closeButton = new Button("Close");
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinHeight(300);
        window.setMinWidth(500);
        Label alertlabel = new Label();
        alertlabel.setText(message);
        closeButton.setOnAction(e -> window.close());
        VBox layout = new VBox();
        layout.getChildren().addAll(alertlabel, closeButton);
        layout.setAlignment(Pos.BASELINE_CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

    }

}