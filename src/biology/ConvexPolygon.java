package biology;


import java.util.*;

/** Maintains a list of points that are vertices
 * of a convex polygon. You've got to supply the points
 * in order around the edge of the polygon (i.e. either counter-clockwise
 * or clockwise)
 *
 * @author sep
 */
public class ConvexPolygon {

    /**The vertices of the polygon*/
    ArrayList<Vertex> Vertices;

    /** Creates a ConvexPolygon 
     */
    public ConvexPolygon() {
        Vertices = new ArrayList<Vertex>();
    }

    /**
     * For building incrementally as we read consecutive
     * vertices, as from an .xml file
     * @param v the (next) vertex
     */
    public void addVertex(Vertex v) {
        Vertices.add(v);
    }

    /**
     * For building incrementally
     * @param x
     * @param y
     */
    public void addVertex(float x, float y) {
        Vertices.add(new Vertex(x, y));
    }

    /**
     *
     * @return a string that is an .xml representation
     * of this polygon
     */
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ConvexPolygon>\n");
        Iterator it = Vertices.iterator();
        while (it.hasNext()) {
            Vertex v = (Vertex) it.next();
            sb.append(v.toXML());
        }
        sb.append("</ConvexPloygon>\n");
        return sb.toString();
    }

    /**
     *
     * @param x the x-coordinate of the point
     * @param y the y coordinate of the point
     * @return true/false according to whether the
     * point is inside the polygon or not.
     */
    public boolean isInside(float x, float y) {
        int n = Vertices.size();
        Vertex V = new Vertex(x, y);
        for (int j = 0; j < n; j++) {
            if (!sameSideAs(Vertices.get(j % n),
                    Vertices.get((j + 1) % n), Vertices.get((j + 2) % n), V)) {
                return false;
            }
        }
        return true;
    }

    /**
     *Is v4 on the same side of the line determined by v1 and v2
     * as v3 is?
     *
     */
    boolean sameSideAs(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        if ((((v2.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (v2.y - v1.y))
                * ((v2.x - v1.x) * (v4.y - v1.y) - (v4.x - v1.x) * (v2.y - v1.y))) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValid() {
        int n = Vertices.size();
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                if (!sameSideAs(Vertices.get(j % n),
                        Vertices.get((j + 1) % n), Vertices.get((j + 2) % n), Vertices.get(k))) {
                    return false;
                }
            }
        }
        return true;
    }
}
