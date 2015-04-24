package parameters;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Steve
 */
import biology.Region;
import java.util.*;
public class SummaryRegions {
    public ArrayList<Region> Regions;
public SummaryRegions()
{
    Regions=new ArrayList<Region>();
}
public void addRegion(Region r)
{
    Regions.add(r);
}

/*17 May 2014 for SeedCollection
Determine the number of the summary region that the location belongs to.

We return the number of the first Summary Region that contains the point
or -1 if none of them do.
*/
public int whichSummaryRegion(float x, float y)
{
    for(int i=0;i<Regions.size();i++)
    {
        Region r= Regions.get(i);
        if(r.inRegion(x, y)) return i;
    }
    return -1;
}
public String toXML()
{
    StringBuffer sb=new StringBuffer();
    sb.append("<SummaryRegions>\n");
    Iterator<Region> it=Regions.iterator();
    while(it.hasNext())
    {
        Region r= (Region)it.next();
        sb.append(r.toXML());
    }
    sb.append("</SummaryRegions>\n");
    return sb.toString();
}
}
