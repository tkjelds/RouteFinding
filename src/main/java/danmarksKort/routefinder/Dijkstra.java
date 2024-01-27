package danmarksKort.routefinder;

import danmarksKort.utility.EWDigraph;
import danmarksKort.utility.IndexMinPQ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tager udgangspunkt i Algs4 Biblioteket
 *
 * @source https://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
 */

public class Dijkstra implements Serializable {
    public EWDigraph ewDigraph;
    public RoadType roadType;
    private double[] distTo;
    private RoadEdge[] edgeTo;
    private IndexMinPQ<Double> priorityQueue;
    private boolean isDistance;

    /**
     * @param ewDigraph  en graf hvor at edgesne er retninger og har en vægt f.eks. distance
     * @param isDistance beskriver om edgesnes værdi skal være basseret på distance eller tid over distance
     * @param from       er et NodeID som er knuden man kommer fra
     * @param to         er et NodeID som er knuden man kommer til
     * @param roadType   beskriver vilke edges dijkstra kan 'relaxe'
     */
    public Dijkstra(EWDigraph ewDigraph, int from, int to, boolean isDistance, RoadType roadType) {
        this.isDistance = isDistance;
        this.ewDigraph = ewDigraph;
        this.roadType = roadType;
        distTo = new double[ewDigraph.getNmbrOfNodes()];
        edgeTo = new RoadEdge[ewDigraph.getNmbrOfNodes()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);
        distTo[from] = 0.0;
        priorityQueue = new IndexMinPQ<>(ewDigraph.getNmbrOfNodes());
        priorityQueue.insert(from, distTo[from]);
        while (!priorityQueue.isEmpty()) {
            int node = priorityQueue.delMin();

            if (node == to) {
                return;
            }
            for (RoadEdge edge : ewDigraph.adjacencyLists[node]) {
                if (edge.getRoadType() == roadType || edge.getRoadType() == RoadType.BOTH) relax(edge);
            }
        }
    }

    private void relax(RoadEdge edge) {
        int v = edge.getFrom().getId(), w = edge.getTo().getId();
        if (distTo[w] > distTo[v] + edge.getValue(isDistance)) {
            distTo[w] = distTo[v] + edge.getValue(isDistance);
            edgeTo[w] = edge;
            if (priorityQueue.contains(w)) {
                priorityQueue.decreaseKey(w, distTo[w]);
            } else {
                priorityQueue.insert(w, distTo[w]);
            }
        }
    }

    /**
     * @return retunerer en double kan enten være tid eller distance ud fra isDistance, som er fra start til knude nr v
     */
    public double distTo(int v) {
        return distTo[v];
    }

    /**
     * @return retunere en boolean om der er en vej til knude nr v
     */
    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    /**
     * @return retunerer en arrayliste af RoadEdges fra start til knude nr v
     */
    public ArrayList<RoadEdge> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        ArrayList<RoadEdge> path = new ArrayList<>();
        var e = edgeTo[v];
        while (e != null) {

            path.add(e);
            e = edgeTo[e.getFrom().getId()];
        }
        return path;
    }
}
