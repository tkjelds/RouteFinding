package danmarksKort.drawables;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class LinePath implements Drawable, Serializable {
    public OSMNode[] coords;

    public LinePath(OSMWay way) {
        coords = new OSMNode[way.size()];
        for (int i = 0; i < way.size(); ++i) {
            coords[i] = way.get(i);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.stroke();
    }

    public void draweveryother(GraphicsContext gc) {
        gc.beginPath();
        traceeveryother(gc);
        gc.stroke();
    }

    @Override
    public OSMNode[] getcoords() {
        return coords;
    }

    @Override
    public OSMNode getfirst() {
        return coords[0];
    }

    @Override
    public OSMNode getcenter() {
        float x = 0;
        float y = 0;
        int pointcount = coords.length;
        for (int i = 0; i < pointcount; i++) {
            OSMNode point = getcoords()[i];
            x += point.getx();
            y += point.gety();
        }
        x = x / pointcount;
        y = y / pointcount;
        return new OSMNode(x, y);
    }


    public void trace(GraphicsContext gc) {
        gc.moveTo(coords[0].getx(), coords[0].gety());
        for (int i = 1; i < coords.length; i++) {
            gc.lineTo(coords[i].getx(), coords[i].gety());
        }
    }

    /**
     * Tracer hver anden node. Bliver primært brugt til coastlines hvor der er mange noder og præcision ikke er
     * nødvendigt
     *
     * @param gc GraphicsContext
     */
    public void traceeveryother(GraphicsContext gc) {
        gc.moveTo(coords[0].getx(), coords[0].gety());
        for (int i = 1; i < coords.length; i = i + 2) {
            gc.lineTo(coords[i].getx(), coords[i].gety());
        }
        if (coords.length % 2 == 1) {
            gc.lineTo(coords[coords.length - 1].getx(), coords[coords.length - 1].gety());
        }
    }


}

