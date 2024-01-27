package danmarksKort.utility;

import danmarksKort.routefinder.RoadEdge;
import danmarksKort.routefinder.RoadType;

import java.io.Serializable;
import java.util.LinkedList;

public class EWDigraph implements Serializable {

    public LinkedList<RoadEdge>[] adjacencyLists;
    private int nmbrOfNodes;
    private int nmbrOfEdges;

    public EWDigraph(int nmbrOfNodes) {
        if (nmbrOfNodes < 0) {
            throw new IllegalArgumentException("Number of vertices in a Digraph must be nonnegative");
        }
        this.nmbrOfNodes = nmbrOfNodes;
        this.nmbrOfEdges = 0;
        adjacencyLists = (LinkedList<RoadEdge>[]) new LinkedList[nmbrOfNodes];
    }

    public void addEdge(RoadEdge edge) {
        switch (edge.getRoadType()) {
            case CAR:
                if (edge.getOneway()) {
                    addEdgeWay(edge);
                } else {
                    addEdgeWay(new RoadEdge(edge.getTo(), edge.getFrom(), edge.getOneway(), edge.getDistance(), edge.getRoadType(), edge.getRoadName(), edge.getMaxspeed()));
                    addEdgeWay(edge);
                }
            case PEDESTRIAN:
                addEdgeWay(new RoadEdge(edge.getTo(), edge.getFrom(), edge.getOneway(), edge.getDistance(), edge.getRoadType(), edge.getRoadName(), edge.getMaxspeed()));
                addEdgeWay(edge);
                break;
            case BOTH:
                if (edge.getOneway()) {
                    addEdgeWay(edge);
                    addEdgeWay(new RoadEdge(edge.getTo(), edge.getFrom(), edge.getOneway(), edge.getDistance(), RoadType.PEDESTRIAN, edge.getRoadName(), edge.getMaxspeed()));
                } else {
                    addEdgeWay(new RoadEdge(edge.getTo(), edge.getFrom(), edge.getOneway(), edge.getDistance(), edge.getRoadType(), edge.getRoadName(), edge.getMaxspeed()));
                    addEdgeWay(edge);
                }
                break;
        }
    }

    private void addEdgeWay(RoadEdge edge) {
        if (adjacencyLists[edge.getFrom().getId()] == null) {
            adjacencyLists[edge.getFrom().getId()] = new LinkedList<>();
        }
        adjacencyLists[edge.getFrom().getId()].add(edge);
        nmbrOfEdges++;
    }

    public int getNmbrOfNodes() {
        return nmbrOfNodes;
    }
}