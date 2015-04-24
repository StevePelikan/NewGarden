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
public class RectTest {
    
    public RectTest() {
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
     * Test of inRect method, of class Rect.
     */
    @Test
    public void testInRect() {
        System.out.println("inRect");
        float x = 0.5F;
        float y = 0.5F;
        Rect instance = new Rect(0.0f,0.0f, 1.0f,1.0f);
        boolean expResult = true;
        boolean result = instance.inRect(x, y);
        assertEquals(expResult, result);
        
        x= -0.1f;
        expResult = false;
        result = instance.inRect(x, y);
        assertEquals(expResult, result);
        
        x= 1.1f;
        expResult = false;
        result = instance.inRect(x, y);
        assertEquals(expResult, result);
        
        x= 0.0f;
        y=-0.1f;
        expResult = false;
        result = instance.inRect(x, y);
        assertEquals(expResult, result);
        
        y=1.1f;
        expResult = false;
        result = instance.inRect(x, y);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

   

   
    /**
     * Test of toXML method, of class Rect.
     */
    
    @Test
    public void testToXML() {
        System.out.println("toXML");
        Rect instance = new Rect(0.0f,0.0f, 1.0f,1.0f);
        String expResult = "";
        String result = instance.toXML();
        
        assertFalse(result.equals(""));
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
