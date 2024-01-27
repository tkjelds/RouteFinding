package danmarksKort.mapelements;

import danmarksKort.drawables.Drawable;
import danmarksKort.drawables.LinePath;
import danmarksKort.drawables.OSMNode;
import danmarksKort.drawables.OSMWay;
import danmarksKort.routefinder.RoadType;
import javafx.scene.canvas.GraphicsContext;

import java.awt.geom.Point2D;
import java.io.Serializable;


public class Highway implements Drawable, Serializable {
    String name;
    Drawable figure;
    Boolean isOneWay;
    Boolean isMotorway;
    double maxspeed;

    public Highway(OSMWay currentWay) {
        figure = new LinePath(currentWay);
    }


    public Boolean getOneWay() {
        return isOneWay;
    }

    public void setOneWay(Boolean oneWay) {
        isOneWay = oneWay;
    }

    public void setMotorWay(Boolean motorway) {
        isMotorway = motorway;
    }

    public double getMaxspeed() {
        return maxspeed;
    }

    public void setMaxspeed(double maxspeed) {
        this.maxspeed = maxspeed;
    }

    public double getLength(OSMNode from, OSMNode to) {
        return Point2D.distance(from.getx(), from.gety(), to.getx(), to.gety());
    }

    @Override
    public void draw(GraphicsContext gc) {
        figure.draw(gc);
    }


    @Override
    public OSMNode[] getcoords() {
        return figure.getcoords();
    }

    @Override
    public OSMNode getfirst() {
        return figure.getfirst();
    }

    @Override
    public OSMNode getcenter() {
        return figure.getcenter();
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public RoadType getRoadType() {
        if (isMotorway == true) return RoadType.CAR;
        else return RoadType.BOTH;
    }
}
