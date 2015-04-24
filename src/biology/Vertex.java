package biology;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Steve
 */
public class Vertex {
    float x,y;
    public Vertex(float xx,float yy){x=xx;y=yy;}
    @Override
    public String toString()
    {
        StringBuffer sb=new StringBuffer();
        sb.append("x= "+x+"  y= "+y+"\n");
        return sb.toString();
    }
    public String toXML()
    {
        StringBuffer sb=new StringBuffer();
        sb.append("<Vertex x=\""+x+"\" y\""+y+"/>\n");
        return sb.toString();
    }  

}
