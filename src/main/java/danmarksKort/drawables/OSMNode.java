package danmarksKort.drawables;

import java.io.Serializable;

public class OSMNode implements Serializable {
    float x, y;
    int id = -1;

    public OSMNode(float lon, float lat) {
        this.x = lon;
        this.y = lat;
    }

    public static double computeDistance(double x1, double x2, double y1, double y2) {

        x1 = Math.toRadians(x1);
        x2 = Math.toRadians(x2);
        y1 = Math.toRadians(y1);
        y2 = Math.toRadians(y2);

        // Haversine formula
        double dlon = x2 - x1;
        double dlat = y2 - y1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(x1) * Math.cos(x2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;
        // calculate the result
        double distance = (c * r);
        return distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float gety() {
        return y;
    }

    public float getx() {
        return x;
    }
}