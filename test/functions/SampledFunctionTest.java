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

package functions;

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
public class SampledFunctionTest {
    double [] x={1,2,3,4,5};
    double [] y={10,20,30,40,50};
    
    double [] xx={2,1,3,4,5};
    double [] yy={10,20,30,50,40};
    public SampledFunctionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of checkXMonotone method, of class SampledFunction.
     */
    @Test
    public void testCheckXMonotone() {
        System.out.println("checkXMonotone");
        SampledFunction instance = new SampledFunction(x,y);
        boolean expResult = true;
        boolean result = instance.checkXMonotone();
        assertEquals(expResult, result);
        
        instance = new SampledFunction(xx,y);
        expResult =false;
        result = instance.checkXMonotone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of monotoneIncreasing method, of class SampledFunction.
     */
    @Test
    public void testMonotoneIncreasing() {
        System.out.println("monotoneIncreasing");
        SampledFunction instance = new SampledFunction(x,y);
        boolean expResult = true;
        boolean result = instance.monotoneIncreasing();
        assertEquals(expResult, result);
        
        instance = new SampledFunction(x,yy);
        expResult = false;
       result = instance.monotoneIncreasing();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of monotoneDecreasing method, of class SampledFunction.
     */
    @Test
    public void testMonotoneDecreasing() {
        System.out.println("monotoneDecreasing");
        SampledFunction instance = new SampledFunction(x,y);
        boolean expResult = false;
        boolean result = instance.monotoneDecreasing();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of value method, of class SampledFunction.
     */
    @Test
    public void testValue() {
        System.out.println("value at point");
        double t = 2.0;
        SampledFunction instance = new SampledFunction(x,y);
        double expResult = 20.0;
        double result = instance.value(t);
        assertEquals(expResult, result, 0.0);
        
        System.out.println("value interpolate");
        t=1.5;
        expResult = 15.0;
        result = instance.value(t);
        assertEquals(expResult, result, 0.0);
        
        
        System.out.println("value extrapolate low");
        t=0;
        expResult = 10.0;
        result = instance.value(t);
        assertEquals(expResult, result, 0.0);
        
        System.out.println("value extrapolate high");
        t=10;
        expResult = 50.0;
        result = instance.value(t);
        assertEquals(expResult, result, 0.0);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of values method, of class SampledFunction.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        double[] t = {0,1,1.5,10};
        SampledFunction instance = new SampledFunction(x,yy);
        double[] expResult = {10,10,15,40};
        double[] result = instance.values(t);
       //assertArrayEquals(expResult, result);
       for(int i=0;i<result.length;i++)
       {
           assertEquals(result[i],expResult[i],0);
       }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of maximum_value method, of class SampledFunction.
     */
    @Test
    public void testMaximum_value() {
        System.out.println("maximum_value");
        SampledFunction instance = new SampledFunction(x,yy);
        double expResult = 50.0;
        double result = instance.maximum_value();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of minimum_value method, of class SampledFunction.
     */
    @Test
    public void testMinimum_value() {
        System.out.println("minimum_value");
        SampledFunction instance = new SampledFunction(xx,yy);
        double expResult = 10.0;
        double result = instance.minimum_value();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addPoint method, of class SampledFunction.
     */
    @Test
    public void testAddPoint() {
        System.out.println("addPoint");
        double t = 0.0;
        double z = 0.0;
        SampledFunction instance = new SampledFunction(x,y);
        instance.addPoint(t, z);
        assertEquals(true,instance.checkXMonotone());
        assertEquals(instance.x.length,6);
        assertEquals(instance.value(0),0,0);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SampledFunction.
     */
    @Ignore
    @Test
    public void testToString() {
        System.out.println("toString");
        SampledFunction instance = new SampledFunction();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class SampledFunction.
     */
    @Ignore
    @Test
    public void testToXML_0args() {
        System.out.println("toXML");
        SampledFunction instance = new SampledFunction();
        String expResult = "";
        String result = instance.toXML();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class SampledFunction.
     */
    @Ignore
    @Test
    public void testToXML_String() {
        System.out.println("toXML");
        String name = "";
        SampledFunction instance = new SampledFunction();
        String expResult = "";
        String result = instance.toXML(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class SampledFunction.
     */
    @Ignore
    @Test
    public void testToXML_String_String() {
        System.out.println("toXML");
        String name = "";
        String options = "";
        SampledFunction instance = new SampledFunction();
        String expResult = "";
        String result = instance.toXML(name, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toRcode method, of class SampledFunction.
     */
    @Ignore
    @Test
    public void testToRcode() {
        System.out.println("toRcode");
        String[] labels = null;
        SampledFunction instance = new SampledFunction();
        String expResult = "";
        String result = instance.toRcode(labels);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
