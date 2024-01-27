package danmarksKort.mapelements;

import danmarksKort.drawables.*;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Grassland implements Drawable, Serializable {
    Drawable figure;

    public Grassland(OSMWay currentWay) {
        figure = new LinePath(currentWay);
    }

    public Grassland(OSMRelation currentRelation) {
        figure = new PolyLinePath(currentRelation);
    }

    @Override
    public void draw(GraphicsContext gc) {
        figure.draw(gc);
        gc.fill();
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
}
