package danmarksKort.drawables;

import java.io.Serializable;

public class Node implements Serializable {
    RectHV rect;
    OSMNode location;
    Node leftChild;
    Node rightChild;
    Drawable figure;


    Node(OSMNode osmNode) {
        location = osmNode;
    }

}
