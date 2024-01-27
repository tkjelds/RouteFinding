package danmarksKort.drawables;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolyLinePath extends ArrayList<LinePath> implements Drawable {

    public PolyLinePath(OSMRelation currentRelation) {
        if (currentRelation != null)
            for (OSMWay way : currentRelation) {
                add(new LinePath(way));
            }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        for (LinePath line : this) {
            line.trace(gc);
        }
        gc.stroke();
    }


    /**
     * @return en liste af alle OSMNoder i en PolyLinePath
     */
    @Override
    public OSMNode[] getcoords() {
        OSMNode[] osmNodes;
        List<OSMNode> osmNodeList = new ArrayList<>();
        for (var line : this) {
            OSMNode[] temp = line.coords;
            List templist = Arrays.asList(temp);
            osmNodeList.addAll(templist);
        }
        osmNodes = new OSMNode[osmNodeList.size()];
        for (int i = 0; i < osmNodeList.size(); i++) {
            osmNodes[i] = osmNodeList.get(i);
        }
        return osmNodes;
    }

    @Override
    public OSMNode getfirst() {
        if (size() != 0) return getcoords()[0];
        return null;
    }

    @Override
    public OSMNode getcenter() {
        float x = 0;
        float y = 0;
        int pointcount = getcoords().length;
        for (int i = 0; i < pointcount; i++) {
            OSMNode point = getcoords()[i];
            x += point.getx();
            y += point.gety();
        }
        x = x / pointcount;
        y = y / pointcount;
        return new OSMNode(x, y);
    }
}
