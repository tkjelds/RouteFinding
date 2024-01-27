package danmarksKort.drawables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OSMWay extends ArrayList<OSMNode> {
    public OSMWay() {

    }

    /**
     * forsøger at merge 2 OSMways til 1.
     *
     * @param coastlineBefore
     * @param coastlineAfter
     * @return OSMWay der er lavet af de to OSMWays
     */
    public static OSMWay merge(OSMWay coastlineBefore, OSMWay coastlineAfter) {
        if (coastlineBefore == null) {
            return coastlineAfter;
        }
        if (coastlineAfter == null) {
            return coastlineBefore;
        }
        OSMWay mergedCoastline = new OSMWay();
        if (coastlineBefore.firstPoint() == coastlineAfter.firstPoint()) {
            mergedCoastline.addAll(coastlineBefore);
            Collections.reverse(mergedCoastline);
            mergedCoastline.remove(mergedCoastline.size() - 1);
            mergedCoastline.addAll(coastlineAfter);
        } else if (coastlineBefore.firstPoint() == coastlineAfter.lastPoint()) {
            mergedCoastline.addAll(coastlineAfter);
            mergedCoastline.remove(mergedCoastline.size() - 1);
            mergedCoastline.addAll(coastlineBefore);

        } else if (coastlineBefore.lastPoint() == coastlineAfter.firstPoint()) {
            mergedCoastline.addAll(coastlineBefore);
            mergedCoastline.remove(mergedCoastline.size() - 1);
            mergedCoastline.addAll(coastlineAfter);
        } else if (coastlineBefore.lastPoint() == coastlineAfter.lastPoint()) {
            List<OSMNode> temp = new ArrayList<>(coastlineAfter);
            Collections.reverse(temp);
            mergedCoastline.addAll(coastlineBefore);
            mergedCoastline.remove(mergedCoastline.size() - 1);
            mergedCoastline.addAll(temp);
        } else {
            throw new IllegalArgumentException("Cannot merge unconnected OSMWays");
        }

        return mergedCoastline;

    }

    public OSMNode firstPoint() {
        return get(0);
    }

    public OSMNode lastPoint() {
        return get(size() - 1);
    }
}