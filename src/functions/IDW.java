/*
 * Copyright (C) 2015 pelikan
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

import biology.Location;
import java.util.ArrayList;

/**
 *
 * @author pelikan
 */

/*
Inverse distance weighting interpolates a collection of points $\{(x_i,y_i)\}$
by taking $y(x)=\frac{\sum w_i(x)y_i}{\sum w_i(x)}$
where $w_i(x)= 1/d(x,x_i)^p$
and generally for $x's in 2 dimensions, $p>2$.
*/

class DataPoint
{
    double x,y,z;
    public DataPoint(double xx,double yy,double zz){x=xx;y=yy;z=zz;}
}
public class IDW {
    double power_parameter=3.0;
    double epsilon=0.00001;
    double d(DataPoint X,DataPoint Xi)
    {
        return Math.sqrt((X.x-Xi.x)*(X.x-Xi.x) + (X.y-Xi.y)*(X.y-Xi.y));
    }
    double W(DataPoint X, DataPoint Xi)
    {
        return Math.pow(1.0/d(X,Xi),power_parameter);
    }
    double value(DataPoint X)
    {
        double top=0;
        double bottom=0;
        for(DataPoint Xi: data)
        {
            if(d(Xi,X)<epsilon) return Xi.z;
            else
            {
                double temp=W(X,Xi);
                top+= temp*Xi.z;
                bottom+= temp;
            }
        }
        return top/bottom;
    }
    ArrayList<DataPoint> data;
    public void setPower(double pp){power_parameter=pp;}
    public double getPower(){return power_parameter;}
    public IDW()
    {
        data=new ArrayList<DataPoint>();
    }
    public void addDataPoint(double x, double y, double z)
    {
     data.add(new DataPoint(x,y,z));   
    }
    public void addDataPoint(DataPoint P)
    {
     data.add(P);   
    }
    public void removeDataPoint(DataPoint X)
    {
        for(DataPoint P:data)
        {
            if(d(X,P)<epsilon) data.remove(P);
        }
    }
    public static void main(String [] args)
    {
        IDW idw=new IDW();
        idw.addDataPoint(0, 0, 0);
        idw.addDataPoint(0, 1, 1);
        idw.addDataPoint(1, 0, 2);
        idw.addDataPoint(1, 1, 3);
        
        System.out.println(idw.value(new DataPoint(1.1,0.9,0)));
    }
    
}
