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

import functions.RandomValue;
import functions.RandomVariable;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sep
 * **/
 
public class NewRectangleTest {
    static NewRectangle nrp,nrr1,nrr0;
    
    public NewRectangleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        nrp=new NewRectangle(0,0,10,10,RECTANGLETYPE.PLAIN);
         nrr1=new NewRectangle(0,0,10,10,RECTANGLETYPE.RANDOM);
         nrr1.density=1.0f;
          nrr0=new NewRectangle(0,0,10,10,RECTANGLETYPE.RANDOM);
         nrr0.density=0.0f;
         nrr0.initialize();
         nrr1.initialize();
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
     * Test of onMap method, of class NewRectangle.
     */
    @Test
    public void testOnMap() {
        
        //first PLAIN
        System.out.println("onMap");
        float x = 0.0F;
        float y = 0.0F;
        NewRectangle instance = null;
        boolean expResult = false;
        boolean result = instance.onMap(x, y);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of inDynamic method, of class NewRectangle.
     */
    @Test
    public void testInDynamic() {
        System.out.println("inDynamic");
        float x = 0.0F;
        float y = 0.0F;
        NewRectangle instance =null;
        boolean expResult = false;
        boolean result = instance.inDynamic(x, y);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of inRectangle method, of class NewRectangle.
     */
    @Test
    public void testInRectangle_Location() {
        System.out.println("inRectangle");
         System.out.println("type PLAIN");
       
       
        Location L = null;
        NewRectangle instance = nrp;
        
        assertEquals(true, instance.inRectangle(new Location(5,5)));
        assertEquals(true, instance.inRectangle(new Location(0,0)));
        assertEquals(true, instance.inRectangle(new Location(0,10)));
         assertEquals(false, instance.inRectangle(new Location(5,50)));
           assertEquals(false, instance.inRectangle(new Location(-5,5)));
           
           System.out.println("type RANDOM");
           instance=nrr0;
           assertEquals(false, instance.inRectangle(new Location(5,5)));
        assertEquals(false, instance.inRectangle(new Location(0,0)));
        assertEquals(false, instance.inRectangle(new Location(0,10)));
         assertEquals(false, instance.inRectangle(new Location(5,50)));
           assertEquals(false, instance.inRectangle(new Location(-5,5)));
           
           instance=nrr1;
            assertEquals(true, instance.inRectangle(new Location(5,5)));
        assertEquals(true, instance.inRectangle(new Location(0,0)));
        assertEquals(true, instance.inRectangle(new Location(0,10)));
         assertEquals(false, instance.inRectangle(new Location(5,50)));
           assertEquals(false, instance.inRectangle(new Location(-5,5)));
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
    }

    /**
     * Test of inRectangle method, of class NewRectangle.
     */
    @Test
    public void testInRectangle_float_float() {
        System.out.println("inRectangle");
        System.out.println("type PLAIN");
        float x = 0.0F;
        float y = 0.0F;
        NewRectangle instance = nrp;
        
        assertEquals(true, instance.inRectangle(2, 2));
        assertEquals(false, instance.inRectangle(2, 200));
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class NewRectangle.
     */
    @Test
    public void testToXML() {
        System.out.println("toXML");
        NewRectangle instance = nrp;
        String expResult = "";
        String result = instance.toXML();
        System.out.println(result);
        assertEquals(true, result.contains("PLAIN"));
        assertEquals(false, result.contains("RANDOM"));
        assertEquals(false, result.contains("DYNAMIC"));
        
        
        
        instance=nrr0;
        result = instance.toXML();
        assertEquals(false, result.contains("PLAIN"));
        assertEquals(true, result.contains("RANDOM"));
        assertEquals(false, result.contains("DYNAMIC"));
        
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class NewRectangle.
     */
    @Test
    public void testUpdate_RandomValue() {
        System.out.println("update");
        RandomValue rval = null;
        NewRectangle instance = null;
        instance.update(rval);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class NewRectangle.
     */
    @Test
    public void testUpdate_int_RandomValue() {
        System.out.println("update");
        int year = 0;
        RandomValue rv = null;
        NewRectangle instance = null;
        instance.update(year, rv);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of XYtoN method, of class NewRectangle.
     */
    @Test
    public void testXYtoN() {
        System.out.println("XYtoN");
        float x = 0.0F;
        float y = 0.0F;
        NewRectangle instance = null;
        int expResult = 0;
        int result = instance.XYtoN(x, y);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nthTrue method, of class NewRectangle.
     */
    @Test
    public void testNthTrue() {
        System.out.println("nthTrue");
        int n = 0;
        NewRectangle instance = null;
        int expResult = 0;
        int result = instance.nthTrue(n);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of actualPointCount method, of class NewRectangle.
     */
    @Test
    public void testActualPointCount() {
        System.out.println("actualPointCount");
        NewRectangle instance = null;
        int expResult = 0;
        int result = instance.actualPointCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addLocation method, of class NewRectangle.
     */
    @Test
    public void testAddLocation_Location() {
        System.out.println("addLocation");
        Location l = null;
        NewRectangle instance = null;
        instance.addLocation(l);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addLocation method, of class NewRectangle.
     */
    @Test
    public void testAddLocation_Location_int() {
        System.out.println("addLocation");
        Location l = null;
        int current_year = 0;
        NewRectangle instance = null;
        instance.addLocation(l, current_year);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeLocation method, of class NewRectangle.
     */
    @Test
    public void testRemoveLocation() {
        System.out.println("removeLocation");
        Location l = null;
        NewRectangle instance = null;
        instance.removeLocation(l);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of initialize method, of class NewRectangle.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        NewRectangle instance = null;
        instance.initialize();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of randomLocations method, of class NewRectangle.
     */
    @Test
    public void testRandomLocations() {
        System.out.println("randomLocations");
        int howmany = 0;
        RandomVariable rv = null;
        NewRectangle instance = null;
        ArrayList<Location> expResult = null;
        ArrayList<Location> result = instance.randomLocations(howmany, rv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

   
    
}
