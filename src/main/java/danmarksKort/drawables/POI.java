package danmarksKort.drawables;

import java.io.Serializable;

public class POI implements Serializable {
    private float Xmin, Xmax, Ymax, Ymin;
    private String poiSelected;
    private double Distance;

    public POI(float maxX, float minX, float maxY, float minY) {
        this.Xmax = maxX;
        this.Xmin = minX;
        this.Ymax = maxY;
        this.Ymin = minY;
    }

    /**
     * @param xmax
     * @param ymax
     * @param ymin
     * @param xmin
     * @return en rektangle, som består af en linepath
     */
    public static LinePath getLinePath(float xmax, float ymax, float ymin, float xmin) {
        OSMNode node1 = new OSMNode(xmax, ymax);
        OSMNode node2 = new OSMNode(xmax, ymin);
        OSMNode node3 = new OSMNode(xmin, ymin);
        OSMNode node4 = new OSMNode(xmin, ymax);
        OSMWay rectangle;
        rectangle = new OSMWay();
        rectangle.add(node1);
        rectangle.add(node2);
        rectangle.add(node3);
        rectangle.add(node4);
        rectangle.add(node1);
        return new LinePath(rectangle);
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public String getPoiSelected() {
        return poiSelected;
    }

    public void setPoiSelected(String poiSelected) {
        this.poiSelected = poiSelected;
    }

    public LinePath getRect(POI rect) {
        return getLinePath(rect.Xmax, rect.Ymax, rect.Ymin, rect.Xmin);
    }

    public float getYmin() {
        return Ymin;
    }

    public float getYmax() {
        return Ymax;
    }

    public float getXmin() {
        return Xmin;
    }

    public float getXmax() {
        return Xmax;
    }
}