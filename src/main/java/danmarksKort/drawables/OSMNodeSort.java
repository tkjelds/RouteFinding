package danmarksKort.drawables;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OSMNodeSort {

    /**
     * @param drawables en liste af drawables
     * @param axis      enten 0 eller 1
     * @return en sorteret liste af drawables, basseret på dens center
     */
    public List<Drawable> sortdrawablecenterbyaxis(List<Drawable> drawables, int axis) {
        if (axis == 0) {
            Collections.sort(drawables, new sortbyXCenter());
            return drawables;
        } else {
            Collections.sort(drawables, new sortbyYCenter());
            return drawables;
        }
    }
}

class sortbyXCenter implements Comparator<Drawable> {

    @Override
    public int compare(Drawable d1, Drawable d2) {
        return Float.compare(d1.getcenter().getx(), d2.getcenter().getx());
    }

}

class sortbyYCenter implements Comparator<Drawable> {

    @Override
    public int compare(Drawable d1, Drawable d2) {
        return Float.compare(d1.getcenter().gety(), d2.getcenter().gety());
    }
}
