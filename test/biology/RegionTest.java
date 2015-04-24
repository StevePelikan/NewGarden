/*
 * Copyright (C) 2014 sep
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

import functions.RandomVariable;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author sep
 */
public class RegionTest {
    static Region ourRegion;
    static Rect r1,r2;
    public RegionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        r1=new Rect(0,0,10,10);
        r2=new Rect(10,10,20,20);
        ourRegion=new Region(0,0,20,20);
        ourRegion.rectangles.add(r1);
         ourRegion.rectangles.add(r2);
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addRect method, of class Region.
     */
    @Test
    public void testAddRect() {
        System.out.println("addRect");
        Rect r = null;
        Region instance = new Region(0,0,30,30);
        instance.addRect(r1);
       
        assertEquals(1,instance.rectangles.size());
        assertEquals(true,instance.inRegion(5, 5));
        assertEquals(false,instance.inRegion(15, 15));
        
         instance.addRect(r2);
          assertEquals(true,instance.inRegion(5, 5));
        assertEquals(true,instance.inRegion(15, 15));
         
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addPoly method, of class Region.
     */
     @Ignore
    @Test
    public void testAddPoly() {
        System.out.println("addPoly");
        ConvexPolygon p = null;
        Region instance = null;
        instance.addPoly(p);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of inRegion method, of class Region.
     */
    @Test
    public void testInRegion() {
        System.out.println("inRegion");
        float x = 5.0F;
        float y = 5.0F;
        //Region instance = null;
        boolean expResult = true;
        boolean result = ourRegion.inRegion(x, y);
        assertEquals(expResult, result);
        
        x=11.0f;
        expResult = false;
        result = ourRegion.inRegion(x, y);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of pickNPoints method, of class Region.
     */
    @Test
    public void testPickNPoints() {
        System.out.println("pickNPoints");
        int N = 4;
        Rect rect = null;
        RandomVariable rv = new RandomVariable();
        Region instance = new Region(0,0,30,30);
        instance.addRect(r1);
        instance.addRect(r1);
        int expResult = N;
        Location[] result = instance.pickNPoints(4, r1, rv);
        assertEquals(expResult, result.length);
        for(Location loc:result)
        {
            assertEquals(true,instance.inRegion(loc.X, loc.Y));
        }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of randomPoint method, of class Region.
     */
    @Test
    public void testRandomPoint() {
        System.out.println("randomPoint");
        Rect r = r1;
        RandomVariable rv = new RandomVariable();
        Region instance = new Region(0,0,30,30);
        instance.addRect(r1);
        instance.addRect(r2);
        Location expResult = null;
        Location result = instance.randomPoint(r, rv);
        assertEquals(true, r1.inRect(result.X, result.Y));
         assertEquals(true, instance.inRect(result.X, result.Y));
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class Region.
     */
     
    @Test
    public void testToXML() {
        System.out.println("toXML");
        Region instance = new Region(0,0,20,20);
        instance.addRect(r1);
        
        String result = instance.toXML();
        assertEquals(false, result.equals(""));
        System.out.println(result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

   
    
}
