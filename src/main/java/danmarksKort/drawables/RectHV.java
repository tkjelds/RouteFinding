package danmarksKort.drawables;

import java.io.Serializable;

public class RectHV implements Serializable {
    float Xmin, Xmax, Ymax, Ymin;

    public RectHV(float maxX, float minX, float maxY, float minY) {
        this.Xmax = maxX;
        this.Xmin = minX;
        this.Ymax = maxY;
        this.Ymin = minY;
    }

    public String toString() {
        return "[" + Xmin + ", " + Xmax + "] x [" + Ymin + ", " + Ymax + "]";
    }

    /**
     * Checker om en punkt ligger inde i rektanglet
     *
     * @param point OSMNode
     */
    public boolean contains(OSMNode point) {
        return (point.getx() > this.Xmin) && (point.getx() < this.Xmax) &&
                (point.gety() > this.Ymin) && (point.gety() < this.Ymax);

    }

    /**
     * Checker om to rektangler ligger oven på hinanden
     *
     * @param other
     * @return
     */
    public boolean isOverLapping(RectHV other) {
        return (this.Xmin < other.Xmax) && (this.Xmax > other.Xmin) && (this.Ymax > other.Ymin) && (this.Ymin < other.Ymax);
    }

}
