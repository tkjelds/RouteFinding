package danmarksKort.mapelements;

import danmarksKort.drawables.*;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Building implements Drawable, Serializable {
    Drawable figure;


    public Building(OSMWay way) {
        figure = new LinePath(way);
    }

    public Building(OSMRelation relation) {
        figure = new PolyLinePath(relation);
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
