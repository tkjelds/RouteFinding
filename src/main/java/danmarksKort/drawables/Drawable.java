package danmarksKort.drawables;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
    void draw(GraphicsContext gc);

    OSMNode[] getcoords();

    OSMNode getfirst();

    OSMNode getcenter();
}