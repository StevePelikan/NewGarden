package biology;


/*
 * Region.java
 *
 * Created on July 15, 2005, 3:57 PM
 Modified to work with continuous locations instead of grid points
 22 July 2006

 We now simply implement a single containing rectangle;
 Later we'll allow more complicated regions tha are unions of
 rectangles with sides vertical and horizontal (at least)
 * 
 * a Region is contained inside a big, bounding rectangle.
 * If no subregions (rectangles or convex polygons) are defined,
 * a point belongs to the region if it belongs to the bounding rectangle.
 * 
 * Otherwise, the point must also belong to one of the subregions.
 * 
 * (Union (subregions)) INTERSECT (bounding rectangle)
 */
/**
 *
 * @author sep
 */
import biology.Rect;
import biology.ConvexPolygon;
import java.util.*;
import java.text.NumberFormat;
import functions.RandomValue;
import functions.RandomVariable;
import parameters.SimData;

public class Region extends Rect {

    List<Rect> rectangles;
  //  List<DynamicRect> drectangles;
    List<ConvexPolygon> Polygons;

    //setp 2014 to iterate over rectangles in seedcollection
    public Iterator<Rect> getRectangles(){return rectangles.iterator();}
    public int numberRectangles(){return rectangles.size();}
    public Region(float xl, float yl, float xh, float yh) {
        super(xl, yl, xh, yh);
        rectangles = new ArrayList<Rect>();
       // drectangles=new ArrayList<DynamicRect>();
        Polygons = new ArrayList<ConvexPolygon>();
    }

    public void addRect(Rect r) {
        rectangles.add(r);
    }
 //public void addDRect(DynamicRect r) {
 //       drectangles.add(r);
 //   }
    public void addPoly(ConvexPolygon p) {
        Polygons.add(p);
    }

    public boolean inRegion(float x, float y) {
//false if outside the big bounding rectangle
        if (!this.inRect(x, y)) {
            return false;
        }
//otherwise, if no subregions are defined, true	
        if (rectangles.size() == 0 && Polygons.size() == 0) {
            return true;
        }
//and if there's subregions defined, true only if we're in one of 'em.
        Iterator<Rect> it = rectangles.iterator();
        while (it.hasNext()) {
            Rect r =  it.next();
           if (r.inRect(x, y)) {
                return true;
            }
        }
        //if we have convexpolygons
        Iterator<ConvexPolygon> ip = Polygons.iterator();
        while (it.hasNext()) {
            ConvexPolygon p =  ip.next();
            if (p.isInside(x, y)) {
                return true;
            }
        }
    //    Iterator<DynamicRect> ii=drectangles.iterator();
    //    while(ii.hasNext())
    //    {
    //        DynamicRect dr=ii.next();
    //        if(dr.inRect(y, y)) return true;
    //    }
        return false;

    }
    /**
     * We may have DynamicRect's in the Region and so
     * can only discard seeds that never have a chance of
     * establishing.
     * 
     * @param x
     * @param y
     * @return 
     */
   // public boolean onMap(float x, float y)
    //{
     //    if (!this.inRect(x, y)) {
      //      return false;
       // }
        // return true;
    //}
    
    /**
     * inDynamicRect
     * 
     * Could this location be in the Region at some future time?
     * used to see if a seed should be allowed at this location
     * @param rv 
     */
//public boolean inDynamicRect(float x, float y)
//{
//    Iterator<DynamicRect> it=drectangles.iterator();
//    while(it.hasNext())
//            {
//                DynamicRect dr=it.next();
//                if(x >=dr.XL && x <= dr.XH &&  y >= dr.YL && y<= dr.YH) return true;
//            }
//    return false;
//}
 //   public void makeInstances(RandomValue rv) {
 //       Iterator it = rectangles.iterator();
 //       while (it.hasNext()) {
 //           Rect r = (Rect) it.next();
 //           if (r.getClass().getName().equals("biology.SparseRect")) {
 //               ((SparseRect) r).makeInstance(rv);
 //              // SparseRect rr = (SparseRect) r;
 //              // rr.makeInstance(rv);
 //           }
 //       }
 //   }
    
 //   public void updateDynamics(SimData sd,RandomValue rv)
 //   {
 //        Iterator it = rectangles.iterator();
 //       while (it.hasNext()) {
 //           Rect r = (Rect) it.next();
 //           if(r.getClass().getName().equals("biology.DynamicRect")){
            
  //          ((DynamicRect) r).updateSelf(sd.current_year,  rv);
              
  //            }
  //             // SparseRect rr = (SparseRect) r;
  //             // rr.makeInstance(rv);
            
  //      }
  //  }

    /**
     *
     * @param N number of points to pick
     * @param rect the rectangle to pick them from (could be a region)
     * @param rv a source for random values
     * @return a list of N distinct points from the rectangle rect that are also
     * in the region.
     *
     * It may not be possible to meet these requirements and so all we do is
     * make a decent try and then give up while reporting the problem
     */
    public Location[] pickNPoints(int N, Rect rect, RandomVariable rv) {
        int MaxTries = 5 * N;
        int found = 0;
        int tries = 0;
       // int size = (rect.TOP - rect.BOTTOM + 1) * (rect.RIGHT - rect.LEFT + 1);
        ArrayList<Location> locations = new ArrayList<Location>();

        ourloop: while (tries < MaxTries && found < N) {
            //pick a point
            Location l = this.randomPoint(rect, rv);
            //if it is in rect and in region and not in our list
            if(l==null){tries++; continue ourloop;}
            boolean inset = false;
            for (Location ll : locations) {
                if (l.equals(ll)) {
                    inset = true;
                }
            }
            if (!inset) {
                locations.add(l);
                found++;
            }
            //save it in found++
            //tries
            tries++;
        }
        Location[] answer = null;
        if (found <N) {
            System.out.println("Region.pickNPoints(): failed.");
            answer = new Location[found];


            int i = 0;
            for (Location ll : locations) {
               
                answer[i++] = ll;
            }

        } else {
 answer = new Location[N];
            int i = 0;
            for (Location ll : locations) {
               
                answer[i++] = ll;
            }
        }
        return answer;
    }

    Location randomPoint(Rect r, RandomVariable rv) {
        int tries = 0;
        int MaxTries = 30;
        boolean done = false;
        int col = 0, row = 0;
        while (!done && tries < MaxTries) {
            col = rv.RandomInt(r.LEFT, r.RIGHT, 1)[0];
            row = rv.RandomInt(r.BOTTOM, r.TOP, 1)[0];
            if (inRegion(col, row)) {
                done = true;
            }
            tries++;
        }
        if (done) {
            return new Location(col, row);
        } else {
            System.out.println("Region.randomPoint(): failed.");
            return null;
        }
    }

    @Override
    public String toXML() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder("");

        sb.append("<Region XL=\"").append(nf.format(XL)).append("\" ");

        sb.append("YL=\"").append(nf.format(YL)).append("\" ");

        sb.append("XH=\"").append(nf.format(XH)).append("\" ");

        sb.append("YH=\"").append(nf.format(YH)).append("\" >\n");

        // Iterator it = rectangles.iterator();
        // while (it.hasNext()) {
        //     Rect r = (Rect) it.next();
        //     sb.append(r.toXML());
        //}
        for (Rect r : rectangles) {
            sb.append(r.toXML());
        }
      //  for(DynamicRect dr: drectangles)
       // {
        //    sb.append(dr.toXML());
       // }

        for (ConvexPolygon p : Polygons) {
            sb.append(p.toXML());
        }
        //     it = Polygons.iterator();
        //    while (it.hasNext()) {
        //       ConvexPolygon p = (ConvexPolygon) it.next();
        //       sb.append(p.toXML());
        //   }
        sb.append("\n</Region>\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        Region r = new Region(-2, -2, 6, 6);
        Rect r1 = new Rect(0, 0, 3, 3);
        r.addRect(r1);
        System.out.println(r.inRegion(-3, 3));
        System.out.println(r.inRegion(0, 1));
        System.out.println(r.toXML());
        Location[] result = r.pickNPoints(3, r1, new RandomVariable());
        System.out.println(Arrays.toString(result));
    }
}
