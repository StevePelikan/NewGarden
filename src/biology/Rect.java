package biology;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Steve
 */

import functions.RandomVariable;
import java.text.NumberFormat;

public class Rect {

    public float XL, XH, YL, YH;
    public int LEFT, RIGHT, TOP, BOTTOM;
    public int H,W,BoundaryMax;
    

    public Rect(float xl, float yl, float xh, float yh) {
        assert xl<= xh : "Bad XL XH in Rect() xl = "+xl+" xh = "+xh;
        assert yl<= yh : "Bad YL YH in Rect() yl = "+yl+" yh = "+yh;
        XL = xl;
        XH = xh;
        YL = yl;
        YH = yh;
        LEFT = (int) XL;
        RIGHT = (int) XH;
        TOP = (int) YH;
        BOTTOM = (int) YL;
        H=(int)(YH-YL+1);
        W=(int) (XH-XL+1);
        BoundaryMax=(2*W+2*H-4);
    }
/*
    BOUNDARY
    We parametrize the boundary with a single int k
    0<= k <= 2W + 2H-5
    so that 0 is (XL,YL) and k increases counter-clockwise
    14 September Boundary parametrization for
    TRANSECT seed collection
    
    Sept 2014
*/
    /**
     * 
     * @param k parameter 0<= k <= 2w+2H-5
     * @return the kth point along the boundary
     */
   /* public Location boundaryPoint(int k)
    {
        //int W=(int) (XH-XL+1);
        //int H=(int)(YH-YL+1);
        //k=k% (2*W+2*H-4);
        k=k%BoundaryMax;
        //assert 0 <= k && k <= 2*W+2*H-5: 
        if(0<= k && k <= W-1) return new Location((int)(XL+k),(int)YL);
        if(W-1<=k && k <= W+H-2) return new Location((int)XH,(int)(YL-(k-(W-1))));
        if(k<= W+H-2 && k<= 2*W+H-3) return new Location((int)(XH-(k-(W+H-2))),(int)YH);
        //if(2*W+H-3<=k && k <=2*W+2*H-5) return new Location((int)XL,(int)(YH-(k-(2*W+H-3))));
        return new Location((int)XL,(int)(YH-(k-(2*W+H-3))));
    }*/
     public Location boundaryPoint(int k)
    {
        int x,y;
        k=k%BoundaryMax;
        if(0<= k && k <= W-1)
        {
            y=BOTTOM;
            x=(int) LEFT +k;
        }
        else if(W-1 <= k && k <= W+H-2)
        {
            x=RIGHT;
            y= BOTTOM+(k-(W-1));
        }
        else if(W+H-2<=k && k <=2*W+H-3)
        {
            y=TOP;
            x= RIGHT-(k- (W+H-2));
        }
        else
        {
            x=LEFT;
            y= TOP-(k-(2*W+H-3));
        }
        return new Location(x,y);
    }

    
    
    /**
     * 
     * @param L a location on the boundary of this Rect
     * @return the parameter value k for the point
     * or -1 if L isn't on the boundary
     */
    public int parameterValue(Location L)
    {
        int Y=L.Y;
        int X=L.X;
        //int W=(int) (XH-XL+1);
        //int H=(int)(YH-YL+1);
        if(Y==(int) YL) return (int)(X-XL);
        if(X==(int) XH) return (int)(W-1+Y-YL);
        if(Y==(int) YH) return (int)(W+H-2+XH-X);
        if(X==(int)XL)
        {
            int k= (int)(2*W+H-3+(YH-Y));
            return k%BoundaryMax;
        }
        return -1;
    }
    /**
     * 
     * @param rv A RandomValue
     * @return a uniformly selected point on the boundary
     */
    public Location randomBoundaryPoint(RandomVariable rv)
    {
       
        int k=rv.RandomInt(0, BoundaryMax-1);
        return boundaryPoint(k);
    }
       public Location innerNormal(int k)
    {
        return innerNormal(boundaryPoint(k%BoundaryMax));
    }
    public Location innerNormal(Location L)
    {
        if(L.X== LEFT && L.Y>BOTTOM && L.Y < TOP) return new Location(1,0);
        else if(L.X== RIGHT && L.Y>BOTTOM && L.Y < TOP) return new Location(-1,0);
        else if(L.Y==BOTTOM && L.X > LEFT && L.X < RIGHT) return new Location(0,1);
        else if(L.Y==TOP && L.X > LEFT && L.X < RIGHT) return new Location(0,-1);
        else return new Location(0,0);
        
    }
        public Location inwardnormal(Location loc)
        {
            if(loc.X==XH && loc.Y != YL && loc.Y!=YH) return new Location(-1,0);
            if(loc.X==XL && loc.Y != YL && loc.Y!=YH) return new Location(1,0);
            if(loc.Y==YL && loc.X != XH && loc.X!=XL) return new Location(0,1);
            if(loc.Y==YH && loc.X != XH && loc.X!=XL) return new Location(0,-1);
            return new Location(0,0);
        }
    public boolean inRect(float x, float y) {
        if (x < XL || x > XH || y < YL || y > YH) {
            return false;
        }
        return true;
    }
    
    public boolean inRect(Location L) {
        return inRect(L.X,L.Y);
    }
   // public boolean isSparse(){return false;}
   // public boolean isDynamic(){return false;}
    public String toXML() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder();
        sb.append("<Rectangle XL=\"").append(nf.format(XL)).append("\" ");
        sb.append("YL=\"").append(nf.format(YL)).append("\" ");
        sb.append("XH=\"").append(nf.format(XH)).append("\" ");
        sb.append("YH=\"").append(nf.format(YH)).append("\" />\n");
        return (sb.toString());
    }
      public static void main(String [] args)
    {
        Rect r= new Rect(0,0,100,100);
        System.out.println(r.toXML());
        System.out.println("(5,5)"+r.inRect(5, 5));
        System.out.println("(5,101)"+r.inRect(5, 101));
        //Make sure boundary points are in the rect
        for(int i=0; i<r.BoundaryMax;i++)
        {
            if(! r.inRect(r.boundaryPoint(i)))
            {
                System.out.println("Boundary not in rect:"+r.toXML()+"i ="+i);
            }
           
        }
         RandomVariable rv=new RandomVariable();
       
        for(int i=0;i<10;i++)
        {
           
            Location b= r.randomBoundaryPoint(rv);
            System.out.println("Point "+b+" has normal "+r.innerNormal(b));
            int k = r.parameterValue(b);
            Location c =r.boundaryPoint(k);
            System.out.println(""+k+" "+b+" "+c);
        }
    }
}

