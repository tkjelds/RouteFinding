package danmarksKort;

import javafx.scene.shape.Circle;

public class CustomCircle extends Circle {
    String address;
    boolean from;


    public CustomCircle(double centerX, double centerY, double radius) {
        this.setCenterX(centerX);
        this.setCenterY(centerY);
        this.setRadius(radius);
        from = false;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFrom(boolean from) {
        this.from = from;
    }
}
