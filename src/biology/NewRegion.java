
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

import functions.RandomValue;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pelikan
 */
public class NewRegion {
    	/*BoundingBox*/
	public int XL,XH,YL,YH,LEFT,RIGHT,BOTTOM,TOP;
	List<NewRectangle> rectangles;
	public boolean onMap(Location L)
        {
            return onMap((float)L.X, (float)L.Y);
        }
        /**
         * Determine whether Location(x,y) is on one of
         * the static or random rectangles or lies inside
         * a dynamic rectangle (though maybe not at a currently viable site)
         * @param x
         * @param y
         * @return 
         */
	public boolean onMap(float x, float y)
        {
            assert rectangles != null: "rectangles is null in onMap("+x+","+y+")";
             for(NewRectangle r: rectangles)
            {
                if(r.onMap(x, y)) return true;
            }
            return false;
        
        }
        /**
         * Determine whether the point Location(x,y) is in one
         * of the rectangles
         * @param x
         * @param y
         * @return 
         */
	public boolean inRegion(float x, float y)
        {
            assert rectangles != null: "rectangles is null in inRegion("+x+","+y+")";
            for(NewRectangle r: rectangles)
            {
                if(r.inRectangle(x, y)) return true;
            }
            return false;
        }
	public String toXML()
        {
             NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder("");

        sb.append("<NewRegion XL=\"").append(nf.format(XL)).append("\" ");

        sb.append("YL=\"").append(nf.format(YL)).append("\" ");

        sb.append("XH=\"").append(nf.format(XH)).append("\" ");

        sb.append("YH=\"").append(nf.format(YH)).append("\" >\n");
        for(NewRectangle r:rectangles)
        {
            sb.append(r.toXML());
        }
        sb.append("\n</NewRegion>\n");
        return sb.toString();
        }
	public void addRectangle(NewRectangle r){
            rectangles.add(r);
            if(r.isDynamic) containsDynamic=true;
            if(r.isSparse) containsSparse=true;
            if(r.isRandom) containsRandom=true;
        }
	public boolean containsDynamic,containsRandom,containsSparse;
        
        public NewRegion(int x1, int x2, int x3,int x4)
        {
            
            XL=x1;
            XH=x3;
            YL=x2;
            YH=x4;
            BOTTOM=YL;
            TOP=YH;
            LEFT=XL;
            RIGHT=XH;
            rectangles=new ArrayList<NewRectangle>();
            containsDynamic=false;
            containsRandom=false;
            containsSparse=false;
        }
        /**
         * updates the dynamic rectangles for the specified year
         * They store the provided RandomValue for future use
         * @param year
         * @param rv A RandomValue to be stored and used by DynamicRectangles
         * if not null
         */
        public void update(int year, RandomValue rv)
        {
            for(NewRectangle r: rectangles)
            {
                if(r.ourType==RECTANGLETYPE.DYNAMIC) r.update(year,rv);
            }
        }
        public static void main(String [] args)
        {
            NewRegion nr=new NewRegion(0,0,10,10);
            NewRectangle r=new NewRectangle(0,0,5,5,RECTANGLETYPE.PLAIN);
            nr.addRectangle(r);
            System.out.println("PLAIN rectangle "+nr.inRegion(1, 1)+" "+nr.onMap(1,1));
            
            System.out.println(nr.inRegion(6, 6));
            NewRectangle rr=new NewRectangle(6,6,10,10,RECTANGLETYPE.RANDOM);
            rr.density=0.5f;
            rr.update(new RandomValue());
            nr.addRectangle(rr);
            System.out.println("RANDOM rectangle size= "+rr.size+" cardinality = "+rr.actualPointCount());
             System.out.println(nr.inRegion(1, 1));
            System.out.println(nr.inRegion(6, 6));
            
            NewRectangle rrr=new NewRectangle(6,0,10,5,RECTANGLETYPE.DYNAMIC);
            rrr.clusterlifedistribution=DISTRIBUTION.CONSTANT;
            rrr.cl_value=2;
            rrr.clusternumberdistribution=DISTRIBUTION.CONSTANT;
            rrr.cpy_value=2;
            rrr.clustersizedistribution=DISTRIBUTION.CONSTANT;
            rrr.cs_value=1;
            
            //update rrr
            rrr.update(null);
            rrr.update(null);
            nr.addRectangle(rrr);
            System.out.println("DYNAMIC rectangle "+nr.inRegion(1, 1)+" "+nr.onMap(1,1));
            
            System.out.println(nr.inRegion(6, 6));
            System.out.println(nr.inRegion(7, 2));
            
            nr.initialize();
            nr.update(2, null);
            System.out.println("Point counts: "+rr.actualPointCount()+" "+rrr.actualPointCount());
            System.out.println(nr.toXML());
            
            
        }
       
        /**
         * Initializes all the (sub) rectangles if they are random
         */
        public void initialize()
        {
            for(NewRectangle r: rectangles) r.initialize();
        }
         /**
         * 
         * @param L
         * @return a NewRectangle in the NewRegion that contains the Location
         * if possible or a DYNAMIC NewRectangle that has the Location L onMap
         */
        public NewRectangle NewRectangleContaining(Location L)
        {
            for(NewRectangle r: rectangles)
            {
                if(r.inRectangle(L.X,L.Y)) return r;
            }
            for(NewRectangle r: rectangles)
            {
                if(r.onMap(L.X, L.Y)) return r;
            }
            return null;
        }
    
}
