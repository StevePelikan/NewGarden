/*
 * Copyright (C) 2014 pelikan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package biology;

import functions.CDF;
import functions.RandomValue;
import functions.RandomVariable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author pelikan
 */
class LocationComparator implements Comparator<Location> {

    public int compare(Location L1, Location L2) {
        if (L1.equals(L2)) {
            return 0;
        }
        if (L1.X < L2.X) {
            return -1;
        }
        if (L1.Y < L2.Y) {
            return -1;
        }
        return 1;

    }
}
public class NewRectangle {

    static final boolean DEBUGPLAIN = false;
    static final boolean DEBUGDYNAMIC = false;
    static final boolean DEBUGSPARSE = false;
    static final boolean DEBUGRANDOM = false;
    static final boolean DEBUG = false;
    int XL, XH, YL, YH, LEFT, RIGHT, BOTTOM, TOP;
    public RECTANGLETYPE ourType;
    String name = null;
    RandomValue rval;

    TreeMap<Location, Integer> locations;
    public CDF clustersperyear, clusterlifetime, clustersize;
    public float cpy_mean;
    public int cpy_value, cpy_min, cpy_max;
    public float cl_mean;
    public int cl_min, cl_max, cl_value;
    public float cs_mean;
    public int cs_min, cs_max, cs_value;

    public DISTRIBUTION clustersizedistribution, clusternumberdistribution, clusterlifedistribution;

    BitSet bits;
    int WIDTH = 0;
    int size = 0;
    public float density = 0.0f;

    boolean isDynamic, isRandom, isSparse;

    /**
     * We need to save seeds that may establish in the future if they lie in a
     * DYNAMIC rectangle even if their location isn't currently viable. This
     * method tests for this situation.
     *
     * @param x
     * @param y
     * @return
     */
    boolean onMap(float x, float y) {
        if (inRectangle(x, y)) {
            return true;
        }
        if (ourType == RECTANGLETYPE.DYNAMIC) {
            if (x < XL || x > XH || y < YL || y > YH) {
                return false;
            }
            return true;
        }
        return false;
    }

    boolean inDynamic(float x, float y) {
        if (ourType == RECTANGLETYPE.DYNAMIC && onMap(x, y)) {
            return true;
        }
        return false;
    }

    boolean inRectangle(Location L) {
        return inRectangle(L.X, L.Y);
    }

    boolean inRectangle(float x, float y) {

        switch (ourType) {
            case PLAIN:
                if (x < XL || x > XH || y < YL || y > YH) {
                    return false;
                }
                return true;

            case SPARSE:
                return false;
            case RANDOM:
                if (x < XL || x > XH || y < YL || y > YH) {
                    return false;
                }
                boolean ans = bits.get(XYtoN(x, y));
                return ans;
            case DYNAMIC:
                return locations.containsKey(new Location((int) Math.floor(x), (int) Math.floor(y)));
        }

        return false;
    }

    String toXML() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder();
        switch (ourType) {
            case PLAIN:
                sb.append("<NewRectangle XL=\"").append(nf.format(XL)).append("\" ");

                sb.append("YL=\"").append(nf.format(YL)).append("\" ");
                sb.append("XH=\"").append(nf.format(XH)).append("\" ");
                sb.append("YH=\"").append(nf.format(YH)).append("\" type=\"" + ourType.toString() + "\" ");
                if (name != null) {
                    sb.append(" name = \"" + name + "\" ");
                }
                sb.append("/>\n");
                break;
            case RANDOM:
                sb.append("<NewRectangle XL=\"").append(nf.format(XL)).append("\" ");
                if (name != null) {
                    sb.append(" name = \"" + name + "\" ");
                }
                sb.append("YL=\"").append(nf.format(YL)).append("\" ");
                sb.append("XH=\"").append(nf.format(XH)).append("\" ");
                sb.append("YH=\"").append(nf.format(YH)).append("\" ");
                sb.append("density=\"").append(nf.format(density)).append("\" type=\"" + ourType.toString() + "\" ");
                if (name != null) {
                    sb.append(" name = \"" + name + "\" ");
                }
                sb.append("/>\n");

                break;
            case DYNAMIC:
                sb.append("<NewRectangle  XL=\"").append(nf.format(XL)).append("\" ");
                if (name != null) {
                    sb.append(" name = \"" + name + "\" ");
                }
                sb.append("YL=\"").append(nf.format(YL)).append("\" ");
                sb.append("XH=\"").append(nf.format(XH)).append("\" ");
                sb.append("YH=\"").append(nf.format(YH)).append("\" ");
                sb.append(" type =\"" + ourType.toString() + "\"");
                if (name != null) {
                    sb.append(" name = \"" + name + "\" ");
                }
                sb.append(">\n");

                //ClustersPerYear
                //if cdf generate the cdf
                //otherwise put in appropraite attributes.
                switch (clusternumberdistribution) {
                    case CDF:

                        sb.append(this.clustersperyear.toXML("ClustersPerYear", "distribution=\"cdf\" "));
                        break;
                    case CONSTANT:
                        //<ClustersPerYear value=""/>
                        sb.append("<ClustersPerYear distribution=\"constant\" value =\"" + cpy_value + "\"/>\n");
                        break;
                    case UNIFORM:
                        //<ClustersPerYear min="" max=""/>
                        sb.append("<ClustersPerYear distribution=\"uniform\" min=\"" + this.cpy_min + "\" max = \"" + this.cpy_max + "\"/>\n");
                        break;
                    case POISSON:
                        //<ClustersPerYear mean=""/>
                        sb.append("<ClustersPerYear distribution=\"poisson\" mean =\"" + this.cpy_mean + "\"/>\n");
                        break;
                }
                //SitesPerCluster
                switch (this.clustersizedistribution) {
                    case CDF:
                        sb.append(this.clustersize.toXML("SitesPerCluster", "distribution=\"cdf\" "));
                        break;
                    case CONSTANT:
                        sb.append("<SitesPerCluster distribution=\"constant\" value =\"" + this.cs_value + "\"/>\n");
                        break;
                    case UNIFORM:
                        sb.append("<SitesPerCluster distribution=\"uniform\" min=\"" + this.cs_min + "\" max = \"" + this.cs_max + "\"/>\n");
                        break;
                    case POISSON:
                        sb.append("<SitesPerCluster distribution=\"poisson\" mean =\"" + this.cs_mean + "\"/>\n");
                        break;
                }

                switch (this.clusterlifedistribution) {
                    case CDF:
                        sb.append(this.clusterlifetime.toXML("SiteLifetime", "distribution=\"cdf\" "));
                        break;
                    case CONSTANT:
                        sb.append("<SiteLifetime distribution=\"constant\" value =\"" + this.cl_value + "\"/>\n");
                        break;
                    case UNIFORM:
                        sb.append("<SiteLifetime distribution=\"uniform\" min=\"" + this.cl_min + "\" max = \"" + this.cl_max + "\"/>\n");
                        break;
                    case POISSON:
                        sb.append("<SiteLifetime distribution=\"poisson\" mean =\"" + this.cl_mean + "\"/>\n");
                        break;
                }

                sb.append("</NewRectangle>\n");

        }
        return sb.toString();
    }

    /**
     * update adds and removes clusters if we are DYNAMIC it re-selects
     * locations if we are RANDOM
     *
     * @param rval
     * 
     * June 2014 THere's no need for this method any more
     * RANDOM NewRectangles are initialize() ed
     * and DYNAMIC NR's are updated with information
     * about the current year.
     */
    public void update(RandomValue rval) {
        if (rval != null) {
            this.rval = rval;
        }
        switch (ourType) {
            case PLAIN:
                break;
            case SPARSE:
                break;
            case RANDOM:
                for (int i = 0; i < size; i++) {
                    double p = rval.Uniform(0, 1, 1)[0];
                    if (p < density) {
                        bits.set(i, true);
                    } else {
                        bits.set(i, false);
                    }
                }
                break;
            case DYNAMIC:
                updateLocations(0);
                makeClusters(0);
                break;

        }
    }

    public void update(int year, RandomValue rv) {
        if (rval == null) {
            rval = rv;
        }
        if (ourType == RECTANGLETYPE.DYNAMIC) {
            updateLocations(year);
            makeClusters(year);
        }
    }

    public NewRectangle(int x1, int x2, int x3, int x4, RECTANGLETYPE type, String name) {
        this.name = name;
        ourType = type;
        XL = x1;
        XH = x3;
        YL = x2;
        YH = x4;
        BOTTOM = YL;
        TOP = YH;
        LEFT = XL;
        RIGHT = XH;
        rval = new RandomValue();
        switch (type) {
            case PLAIN:
                isDynamic = false;
                isRandom = false;
                isSparse = false;
                break;
            case RANDOM:
                isDynamic = false;
                isRandom = true;
                isSparse = false;
                size = (int) Math.floor((XH - XL + 1) * (YH - YL + 1));
                WIDTH = RIGHT - LEFT + 1;
                bits = new BitSet(size);
                bits.set(0, size);
                if (density == 1.0f) {
                    for (int i = 0; i < size; i++) {
                        bits.set(i, true);
                    }
                }
                break;
            case SPARSE:
                isDynamic = false;
                isRandom = false;
                isSparse = true;
                break;
            case DYNAMIC:
                isDynamic = true;
                isRandom = false;
                isSparse = true;
                locations = new TreeMap<Location, Integer>(new LocationComparator());
                break;

        }
    }

    public NewRectangle(int x1, int x2, int x3, int x4, RECTANGLETYPE type) {
        ourType = type;
        XL = x1;
        XH = x3;
        YL = x2;
        YH = x4;
        BOTTOM = YL;
        TOP = YH;
        LEFT = XL;
        RIGHT = XH;
        rval = new RandomValue();
        switch (type) {
            case PLAIN:
                isDynamic = false;
                isRandom = false;
                isSparse = false;
                break;
            case RANDOM:
                isDynamic = false;
                isRandom = true;
                isSparse = false;
                size = (int) Math.floor((XH - XL + 1) * (YH - YL + 1));
                WIDTH = RIGHT - LEFT + 1;
                bits = new BitSet(size);
                bits.set(0, size);
                if (density == 1.0f) {
                    for (int i = 0; i < size; i++) {
                        bits.set(i, true);
                    }
                }
                break;
            case SPARSE:
                isDynamic = false;
                isRandom = false;
                isSparse = true;
                break;
            case DYNAMIC:
                isDynamic = true;
                isRandom = false;
                isSparse = true;
                locations = new TreeMap<Location, Integer>(new LocationComparator());
                break;

        }
    }

    // SOME SPARSE STUFF ... NOW RANDOM
    int XYtoN(float x, float y) {
        return ((int) Math.floor((x - LEFT) + (y - BOTTOM) * WIDTH));
    }

    private int[] NtoXY(int n) {
        int[] ans = new int[2];

        ans[0] = (int) Math.floor(XL + n % WIDTH);
        ans[1] = (int) Math.floor(YL + (n - ans[0]) / WIDTH);

        return ans;
    }
    /*
     * what is the location of the nth bit that is true?
     */

    int nthTrue(int n) {
        assert (ourType == RECTANGLETYPE.RANDOM) : "asked nthTrue() for non-random";
        assert (n < size) : "n too big in SparseRect.nthTrue(): n=" + n + "size =" + size;
        assert (n < bits.cardinality()) : "n too big in SparseRect cardinality: n=" + n + "card =" + bits.cardinality();
        int count = 0;
        int i = -1;
        while (count < n) {
            i++;
            if (bits.get(i)) {
                count++;
            }

        }
        return i;
    }

    int actualPointCount() {
        switch (ourType) {
            case RANDOM:
                return bits.cardinality();

            case DYNAMIC:
                return locations.size();
            case PLAIN:
                return (XH - XL + 1) * (YH - YL + 1);
            default:
                return 0;
        }
    }

    public void addLocation(Location l) {
        bits.set(l.X - LEFT + (l.Y - BOTTOM) * WIDTH, true);
    }

    public void addLocation(Location l, int current_year) {
        switch (ourType) {
            case PLAIN:
                break;
            case RANDOM:
                bits.set(l.X - LEFT + (l.Y - BOTTOM) * WIDTH, true);
                break;
            case DYNAMIC:
                //ONLY TO BE USED IN YEAR 0!!!!
                //pick a life time
                int lifetime = current_year + getLifetime();
                locations.put(l, lifetime);

        }
    }

    public void removeLocation(Location l) {
        bits.set(l.X - LEFT + (l.Y - BOTTOM) * WIDTH, false);
    }
    //SOME DYNAMIC STUFF

    private ArrayList<Location> updateLocations(int year) {
        ArrayList<Location> removed = new ArrayList<Location>();
        // for(Location L : locations.keySet())
        Set<Map.Entry<Location, Integer>> set = locations.entrySet();

        Iterator<Map.Entry<Location, Integer>> it;
        it = locations.entrySet().iterator();
        if (DEBUGDYNAMIC) {
            System.out.println("updateLocations():" + locations.size());
        }
        while (it.hasNext()) {
            Map.Entry<Location, Integer> entry;
            entry = it.next();
            Location L = entry.getKey();
            int val = entry.getValue();
            if (val < year) {
                if (DEBUGDYNAMIC) {
                    System.out.println("removing " + L.toString());
                }
                it.remove();
            }
            removed.add(L);
        }
        if (DEBUGDYNAMIC) {
            System.out.println("updateLocations():" + locations.size());
        }
        return removed;
    }

    /**
     * Generate new viable locations in clusters according to statistics
     * provided.
     *
     * At some point we want to allow other distributions and this would be one
     * routine of a collection called by makeNewViableLocations()
     */
    private int getnumberClusters() {
        int answer;
        switch (clusternumberdistribution) {
            case CDF:
                answer = (int) Math.floor(clustersperyear.value(rval.Uniform(0, 1, 1)[0]));
                return answer;
            case CONSTANT:
                answer = cpy_value;
                return answer;
            case POISSON:
                answer = (int) Math.floor(rval.Poisson(cpy_mean, 1)[0]);
                return answer;
            case UNIFORM:
                answer = (int) Math.floor(rval.Uniform(cpy_min, cpy_max, 1)[0]);
                return answer;
            default:
                return 1;

        }

    }

    private int getLifetime() {
        int answer;
        switch (this.clusterlifedistribution) {
            case CDF:
                answer = (int) Math.floor(clusterlifetime.value(rval.Uniform(0, 1, 1)[0]));
                return answer;
            case CONSTANT:
                answer = cl_value;
                return answer;
            case POISSON:
                answer = (int) Math.floor(rval.Poisson(cl_mean, 1)[0]);
                return answer;
            case UNIFORM:
                answer = (int) Math.floor(rval.Uniform(cl_min, cl_max, 1)[0]);
                return answer;
            default:
                return 1;

        }

    }

    private int getClusterSize() {
        int answer;
        switch (clustersizedistribution) {
            case CDF:
                answer = (int) Math.floor(clustersize.value(rval.Uniform(0, 1, 1)[0]));
                return answer;
            case CONSTANT:
                answer = cs_value;
                return answer;
            case POISSON:
                answer = (int) Math.floor(rval.Poisson(cs_mean, 1)[0]);
                return answer;
            case UNIFORM:
                answer = (int) Math.floor(rval.Uniform(cs_min, cs_max, 1)[0]);
                return answer;
            default:
                return 1;

        }

    }

    private void makeClusters(int current_year) {
        //pick number

        int numberClusters = getnumberClusters();
        if (DEBUGDYNAMIC) {
            System.out.println("makeClusters():" + locations.size());
        }
        for (int i = 0; i < numberClusters; i++) {
            int lifetime = current_year + getLifetime();

            int clsize = getClusterSize();

            //pick a location for cluster uniformly in rectangle
            int XXc = rval.RandomInt(LEFT, RIGHT, 1)[0];
            int YYc = rval.RandomInt(BOTTOM, TOP, 1)[0];
            if (DEBUGDYNAMIC) {
                System.out.println(numberClusters + " " + i + " life= " + lifetime + " size=  " + clsize + " (" + XXc + " " + YYc + ")");
            }  //generate a patch of viable sites
            int xlow = Math.max(XXc - clsize, LEFT);
            int xhigh = Math.min(RIGHT, XXc + clsize);
            int ylow = Math.max(YYc - clsize, BOTTOM);
            int yhigh = Math.min(YYc + clsize, TOP);
            for (int xd = xlow; xd <= xhigh; xd++) {
                for (int yd = ylow; yd <= yhigh; yd++) {
                    Location LL = new Location(xd, yd);

                    if (locations.containsKey(LL)) {
                        //update lifetime
                        int life = locations.get(LL);
                        if (life < lifetime) {
                            locations.put(LL, lifetime);
                        }
                    } else {
                        locations.put(LL, lifetime);
                    }

                }
            }
        }
        if (DEBUG) {
            System.out.println("makeClusters():" + locations.size());
        }

    }

    /**
     * This method it to be used at the start of every run of the simulation
     * WE MAY WANT TO preIterate() the DYNAMIC NewRectangles.
     * 
     * RANDOM NewRectangles are assigned new viable sites
     * DYNAMIC NewRectangles have their list of viable sites cleared
     */
    public void initialize() {
        switch (ourType) {
            case RANDOM:

                if (rval != null) {
                    for (int i = 0; i < size; i++) {
                        double p = rval.Uniform(0, 1, 1)[0];
                        if (p < density) {
                            bits.set(i, true);
                        } else {
                            bits.set(i, false);
                        }
                    }
                }
                break;
            case DYNAMIC:
                locations.clear();
        }
    }

    /**
     * return a list of howmany randomly chosen, distinct, viable locations in a
     * random order
     *
     * @param howmany
     * @return
     */
    public ArrayList<Location> randomLocations(int howmany, RandomVariable rv) {
        ArrayList<Location> ourLocations = new ArrayList<Location>();
        int[] selected_points;
        switch (ourType) {
            case PLAIN:
                int thisX,
                 thisY;
                int number_gridpoints = (YH - YL + 1) * (XH - XL + 1);
                assert howmany <= number_gridpoints : "Asking for too many Locations in randomLocations() PLAIN";
                selected_points = rv.selectnfromN(howmany, number_gridpoints);
                selected_points = rv.permuteList(selected_points);
                int xwidth = XH - XL + 1;
                for (int i = 0; i < howmany; i++) {
                    int thispoint = selected_points[i];
                    thisX = XL + (thispoint % xwidth);
                    thisY = YL + (thispoint - (thispoint % xwidth)) / xwidth;
                    ourLocations.add(new Location(thisX, thisY));
                }
                break;
            case RANDOM:
                assert howmany <= actualPointCount() : "Asking for too many Locations in randomLocations() RANDOM";
                selected_points = rv.selectnfromN(howmany, this.actualPointCount());
                selected_points = rv.permuteList(selected_points);
                for (int i = 0; i < howmany; i++) {
                    int[] coords = NtoXY(nthTrue(selected_points[i]));
                    ourLocations.add(new Location(coords[0], coords[1]));
                }
                break;
            case DYNAMIC:
                assert locations.size() >= howmany : "Asking for too many Locations in randomLocations() DYNAMIC";
              // selected_points = rv.selectnfromN(howmany, locations.size());
                //  selected_points = rv.permuteList(selected_points);
                //   selected_points = rv.permuteList(selected_points);
                //Get the Set of entries so we can chose form them
                // Set<Map.Entry<Location, Integer>> set = locations.entrySet();
                Set<Location> lset = locations.keySet();
                Location[] larray = lset.toArray(new Location[0]);
                selected_points = rv.selectnfromN(howmany, larray.length);
                selected_points = rv.permuteList(selected_points);
                for (int i = 0; i < howmany; i++) {
                    ourLocations.add(larray[selected_points[i]]);
                }
                break;
        }
        return ourLocations;

    }
/**
 * 
 * @param rv 
 * update a DYNAMIC rectangle a number of times
 * to get it somewhere near equilibrium distribution
 * of viable sites I DOUBT 5 updates is enough.
 */
    public void preIterate(RandomValue rv) {
        int UPDATES= -5;
        switch (ourType) {
            case DYNAMIC:
                initialize();
                for (int y = UPDATES; y < 0; y++) {
                    update(y, rv);
                }
                break;
            case PLAIN:
            case RANDOM:
                 return;

        }
    }

    public static void main(String[] args) {
        NewRectangle r = new NewRectangle(0, 0, 100, 100, RECTANGLETYPE.DYNAMIC);
        r.clusternumberdistribution = DISTRIBUTION.CONSTANT;
        r.cpy_value = 1;
        r.clusterlifedistribution = DISTRIBUTION.CONSTANT;
        r.cl_value = 1;
        r.clustersizedistribution = DISTRIBUTION.CONSTANT;
        r.cs_value = 3;
        System.out.println(r.actualPointCount());

        r.preIterate(new RandomValue());
System.out.println(r.actualPointCount());

        r.update(0, new RandomValue());
        System.out.println(r.actualPointCount());

        r.update(1, new RandomValue());
        System.out.println(r.actualPointCount());
        r.update(2, new RandomValue());
        System.out.println(r.actualPointCount());
        r.update(3, new RandomValue());
        System.out.println(r.actualPointCount());
        System.out.println("Random Locations in a DYNAMIC NewRectangle (0,0)-(100,100)");
        System.out.println("with " + r.actualPointCount() + " viable locations");

        ArrayList<Location> locs = r.randomLocations(10, new RandomVariable());
        for (Location l : locs) {
            System.out.println(l.toString() + ":" + r.inRectangle(l));
        }

        System.out.println("Random Locations in a RANDOM NewRectangle (0,0)-(100,100)");
       
        r = new NewRectangle(0, 0, 100, 100, RECTANGLETYPE.RANDOM);
        r.density = 0.5f;
        r.initialize();
         System.out.println("With "+r.actualPointCount()+" viable locations");
        locs = r.randomLocations(10, new RandomVariable());
        for (Location l : locs) {
            System.out.println(l.toString() + ":" + r.inRectangle(l));
        }

    }
}
