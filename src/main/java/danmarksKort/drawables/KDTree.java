package danmarksKort.drawables;

import danmarksKort.mapelements.Highway;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KDTree implements Serializable {
    public Node root = null;
    private List<Drawable> searchresults;


    /**
     * Konstruktor til KD-træ
     *
     * @param figures Listen af Drawables der skal gemmes i KD-træet
     * @param depth   Den nuværende dybde af træet
     * @param bounds  afgrænsning af figurernes koordinater
     */
    public KDTree(List<Drawable> figures, int depth, RectHV bounds) {
        if (figures.size() > 0) {
            int axis = depth % 2;
            OSMNodeSort sort = new OSMNodeSort();
            sort.sortdrawablecenterbyaxis(figures, axis);
            int median = figures.size() / 2;
            root = new Node(figures.get(median).getcenter());
            root.location = figures.get(median).getcenter();
            root.rect = bounds;
            root.figure = figures.get(median);
            if (figures.size() != 1) {
                List<Drawable> leftchildlist = figures.subList(0, median);
                root.leftChild = new KDTree(leftchildlist, depth + 1, getNewRectHV(bounds, axis, "left", root.location)).root;
                if (figures.size() != 2) {
                    List<Drawable> rightchildlist = figures.subList(median, figures.size());
                    root.rightChild = new KDTree(rightchildlist, depth + 1, getNewRectHV(bounds, axis, "right", root.location)).root;

                }
            }
        }
    }


    private RectHV getNewRectHV(RectHV bounds, int axis, String position, OSMNode splitPoint) {
        float maxX = bounds.Xmax;
        float minX = bounds.Xmin;
        float maxY = bounds.Ymax;
        float minY = bounds.Ymin;
        if (axis == 0) {
            float split = splitPoint.getx();
            if (position == "left") {
                return new RectHV(split, minX, maxY, minY);
            }
            if (position == "right") {
                return new RectHV(maxX, split, maxY, minY);
            }

        }
        if (axis == 1) {
            float split = splitPoint.gety();
            if (position == "left") {
                return new RectHV(maxX, minX, split, minY);
            }
            if (position == "right") {
                return new RectHV(maxX, minX, maxY, split);
            }
        }
        throw new IndexOutOfBoundsException("Axis is not 0 or 1");
    }


    /**
     * Bliver brugt i viewcontrolleren hvor vores Afgrænsningsområde er programmets skærmareal
     *
     * @param searchrange Afgrænsningsområde for søgeområdet
     * @return En liste af drawables der befandtes i søgeområdet
     */
    public List<Drawable> range(RectHV searchrange) {
        searchresults = new ArrayList<>();
        searchrange = new RectHV(searchrange.Xmax, searchrange.Xmin, searchrange.Ymax, searchrange.Ymin);
        if (root.location != null) rangeSearch(root, searchrange);
        return searchresults;
    }

    private void rangeSearch(Node node, RectHV range) {
        if (range.isOverLapping(node.rect)) {
            if (range.contains(node.location)) {
                searchresults.add(node.figure);
            }
            if (node.leftChild != null) rangeSearch(node.leftChild, range);
            if (node.rightChild != null) rangeSearch(node.rightChild, range);
        }

    }


    /**
     * @param x           x koordinat for søgeposition
     * @param y           y koordinat for søgeposition
     * @param widestRoad  bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @param longestRoad bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @return Navnet på den tætteste node
     */
    public String getNearestRoad(float x, float y, float widestRoad, float longestRoad) {
        String roadname = "Bevæg musen tættere på en vej";
        List<Drawable> nearbyroads = range(new RectHV(x + widestRoad, x - widestRoad, y + longestRoad, y - longestRoad));
        if (nearbyroads.size() != 0) {
            double closestNode = 0.02;
            roadname = "";
            for (Drawable road : nearbyroads) {
                for (var node : road.getcoords()) {
                    if (Point2D.distance(x, y, node.getx(), node.gety()) < closestNode) {
                        closestNode = Point2D.distance(x, y, node.getx(), node.gety());
                        roadname = ((Highway) road).getName();
                    }
                }
            }
        }
        return roadname;
    }

    /**
     * @param x           x koordinat for søgeposition
     * @param y           y koordinat for søgeposition
     * @param widestRoad  bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @param longestRoad bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @return Id'et på den tætteste node
     */
    public int getNearestNodeId(float x, float y, float widestRoad, float longestRoad) {
        int nodeID = 0;
        List<Drawable> nearbyroads = range(new RectHV(x + widestRoad, x - widestRoad, y + longestRoad, y - longestRoad));
        if (nearbyroads.size() != 0) {
            double closestNode = 0.02;
            for (Drawable road : nearbyroads) {
                for (var node : road.getcoords()) {
                    if (Point2D.distance(x, y, node.getx(), node.gety()) < closestNode) {
                        closestNode = Point2D.distance(x, y, node.getx(), node.gety());
                        nodeID = node.getId();
                    }
                }
            }
        }
        return nodeID;
    }

    /**
     * @param x           x koordinat for søgeposition
     * @param y           y koordinat for søgeposition
     * @param widestRoad  bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @param longestRoad bliver tilføjet til vores koordinater til at lave en søge argrænsningsområde
     * @return den tætteste node
     */
    public OSMNode getNearestNode(float x, float y, float widestRoad, float longestRoad) {
        OSMNode node = new OSMNode(0, 0);
        List<Drawable> nearbyroads = range(new RectHV(x + widestRoad, x - widestRoad, y + longestRoad, y - longestRoad));
        if (nearbyroads.size() != 0) {
            double closestNode = 1.00;
            for (Drawable road : nearbyroads) {
                for (var nodetemp : road.getcoords()) {
                    if (Point2D.distance(x, y, nodetemp.getx(), nodetemp.gety()) < closestNode) {
                        closestNode = Point2D.distance(x, y, nodetemp.getx(), nodetemp.gety());
                        node = nodetemp;
                    }
                }
            }
        }
        return node;
    }
}
