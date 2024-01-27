package danmarksKort.routefinder;

import danmarksKort.drawables.OSMNode;

import java.io.Serializable;

public class RoadEdge implements Serializable {
    public double distance;
    OSMNode from;
    OSMNode to;
    RoadType roadType;
    double maxspeed;
    Boolean oneway;
    String roadname;

    public RoadEdge(OSMNode from, OSMNode to, Boolean oneway, double distance, RoadType roadType, String roadname, Double maxspeed) {
        this.from = from;
        this.to = to;
        this.oneway = oneway;
        this.distance = distance;
        this.roadType = roadType;
        this.roadname = roadname;
        this.maxspeed = maxspeed;
    }

    public double getMaxspeed() {
        return maxspeed;
    }

    public double getDistance() {
        return distance;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public OSMNode getFrom() {
        return from;
    }

    public OSMNode getTo() {
        return to;
    }

    public Boolean getOneway() {
        return oneway;
    }

    public double getValue(boolean isDistance) {
        if (isDistance) return OSMNode.computeDistance(to.getx(), from.getx(), to.gety(), from.gety());
        else return getTime();
    }

    private double getTime() {
        double distance = OSMNode.computeDistance(to.getx(), from.getx(), to.gety(), from.gety());
        return distance / maxspeed;
    }

    public String getRoadName() {
        return roadname;
    }
}
