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
public class CDFTest {
    double []x={1,2,3,4,5};
    double []y={0,0.1,0.2,0.3,1.0};
    double []yy={0,0.1,0.2,0.3,0.4};
    
    public CDFTest() {
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
     * Test of addPoint method, of class CDF.
     */
      @Ignore
    @Test
    public void testAddPoint() {
        System.out.println("addPoint");
        double x = 0.0;
        double y = 0.0;
        CDF instance = new CDF();
        instance.addPoint(x, y);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isOkay method, of class CDF.
     */
    @Test
    public void testIsOkay() {
        System.out.println("isOkay");
        CDF instance = new CDF(x,yy);
        boolean expResult = false;
        boolean result = instance.isOkay();
        assertEquals(expResult, result);
        
        
        instance = new CDF(x,y);
        expResult = true;
       result = instance.isOkay();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class CDF.
     */
      @Ignore
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        CDF.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class CDF.
     */
      @Ignore
    @Test
    public void testToXML_0args() {
        System.out.println("toXML");
        CDF instance = new CDF();
        String expResult = "";
        String result = instance.toXML();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class CDF.
     */
      @Ignore
    @Test
    public void testToXML_String() {
        System.out.println("toXML");
        String name = "";
        CDF instance = new CDF();
        String expResult = "";
        String result = instance.toXML(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class CDF.
     */
      @Ignore
    @Test
    public void testToXML_String_String() {
        System.out.println("toXML");
        String name = "";
        String options = "";
        CDF instance = new CDF();
        String expResult = "";
        String result = instance.toXML(name, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toRcode method, of class CDF.
     */
    @Ignore
    @Test
    public void testToRcode() {
        System.out.println("toRcode");
        String[] labels = null;
        CDF instance = new CDF();
        String expResult = "";
        String result = instance.toRcode(labels);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
