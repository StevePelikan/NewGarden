package biology;

/*
 * Location.java
 *
 * Created on February 28, 2005, 6:45 PM
 */

/**
 * This class is used to store the location (essentially
 * x and y coordinates) of a plant
 * @author sep
 */
public class Location {
    private static final double PIOVER2=1.5707963267948966;
    public static final double TPIOVER2=4.71238898038469;
    /** Creates a new instance of Location */
    public Location() {
    }
    public int X,Y;
    /**
     * Create a location given the coordinates for it
     * @param x the x coordinate of the location
     * @param y the y coordinate of the location
     */
    public Location(int x, int y){X=x; Y=y;}
    /**
     * Returns a String version of the location
     * @return String to display/print the location
     */
    @Override
    public String toString(){return ("location:(X = "+X+",Y = "+Y+")");}
    /**
     * Returns the distance between this location and l
     * Distance is measured as 1-norm.
     * @param l the location to find the distance to
     * @return The 1-norm distance from here to l
     */
    public float dist(Location l)
    {
        return (float)Math.max(Math.abs(l.X-X),Math.abs(l.Y-Y));
        
    }
    /**
     * 
     * @param l a displacement (location)
     * @return new location which is moved from "here"
     * by specified displacement.
     */
    public Location add(Location l)
    {
        return(new Location(X+l.X,Y+l.Y));
    }
    public boolean equals(Location l)
    {
        return( X==l.X && Y==l.Y);
        //above is superior to below based on profile of process
        //
       // if((X-l.X)* (X-l.X)+ (Y-l.Y)*  (Y-l.Y)==0) return true;
       // return false;
    }
    public boolean isequal(Location l)
    {
        return( X==l.X && Y==l.Y);
        //above is superior to below based on profile of process
        //
       // if((X-l.X)* (X-l.X)+ (Y-l.Y)*  (Y-l.Y)==0) return true;
       // return false;
    }
    /**
     * MARCH 2014
     * To be used with PollenDirection in determining
     * whether a potential pollen source is upwind form a potential maternal parent.
     * @param x
     * @param y
     * @return 
     * 0 degrees is north, degrees increase clockwise.
     * we return the bearing from our location to the specified
     * one.
     */
     public float bearingTo(float x, float y)
    {
       double ans=0.0;
       x=x-this.X;
       y=y-this.Y;
       if(x>0){ans=PIOVER2 - Math.atan(y/x);}
       if(x<0){ans= TPIOVER2-Math.atan(y/(x));}
       if(x==0&&y>=0){ans= 0.0;}
       if(x==0&& y<0){ans= Math.PI;}
       return (float) Math.toDegrees(ans);
    }
      public float bearingTo(Location L)
    {
       return bearingTo(L.X,L.Y);
    }
     public static void main(String [] args)
     {
         Location LL=new Location(1,1);
         System.out.println(LL.dist(new Location(2,2)));
         System.out.println(LL.bearingTo(2,2)+"=45");
         System.out.println(LL.bearingTo(2,1)+"=90");
         System.out.println(LL.bearingTo(2,0)+"=135");
         System.out.println(LL.bearingTo(1,0)+"=180");
         System.out.println(LL.bearingTo(-1,1)+"=270");
         System.out.println(LL.bearingTo(1,1)+"=0");
         System.out.println(LL.bearingTo(1,2)+"=0");
         System.out.println(LL.bearingTo(0,20)+"= 360-");
         System.out.println(LL.bearingTo(0,-20)+"= 180+");
     }
}
