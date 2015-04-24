package functions;

/*
 * CDF.java
 *
 * 
 *
 */

/**
 *
 *CDF is Cumulative Distribution Function for a random variable
 *We're only really interested in drawing random values from it.
 * 
 */
import java.util.*;
import parameters.MyFormat;

//THIS VERSION IS FOR TESTING
//REPLACE WITH WITH parameters.MyFormat
/*
class MyFormat
{
    public MyFormat()
    {
        
    }
    public String format(String proto,double x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(proto,x).toString();

    }
    public String format(String proto,long x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(proto,x).toString();

    }
}
*/
/**
 * CDF is a cumulative distribution function, or, rather,
 * a means for picking random values from a distribution
 * described by one.
 * 
 * It is basically just a SampledFunction with "x" and "y" interchanged
 * so that we can take the inverse of a random uniform [0,1] value
 * to get the desired variate
 *
 * Created on April 6, 2006, 6:49 PM
 * @author sep
 */
public class CDF extends SampledFunction {
    /**Turns reporting of errors on/off*/
    private static final boolean OurDEBUG = false;

    /** Creates a new instance of CDF */
    public CDF() {
        super();
    }

    /**
     * Overrides the SampledFunction cretor of the same name
     * @param xx array with x values of points on graph of the cdf
     * @param yy array with the y values of points on the graph of the cdf
     */
    public CDF(double[] xx, double[] yy) {
        super(yy, xx);
        assert yy[y.length - 1] == 1.0;
    }

    /**
     * addPoint() over rides the addPoint of SampledFunction to simply 
     * interchange x and y. We use addPoint() when constructing an CDF
     * by reading "functionpoint"
     * elements from an .xml file
     * @param x the x value
     * @param y the y value
     */
    @Override
    public void addPoint(double x, double y) {
        super.addPoint(y, x);
    }

    public boolean isOkay()
    {
        if(x[0]!= 0) return false;
        if(x[x.length-1] !=1) return false;
        return true;
    }
    /**
     * For testing purposes
     * @param args not used
     */
    public static void main(String[] args) {
        
        /*chestnut near*/
        double[] x = {0.0,6.0,13.0,31.0,101.0,301};
        double[] y = {0.0, 0.45, 0.3, 0.15, 0.07, 0.03};
        double[] t = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,1.0};
        CDF cdf = new CDF(x, y);
        t = cdf.values(t);
        System.out.println(Arrays.toString(t));
        for (int i = 0; i < t.length; i++) {
            t[i] = Math.floor(t[i]);
        }
        System.out.println(Arrays.toString(t));
        System.out.println(cdf.toXML());
         System.out.println(cdf.toXML("NAME"));
         
          /*chestnut far*/
        double []xx = {0.0,6.0,13.0,31.0,101.0,301};
        double[] yy = {0.0, 0.07, 0.08, 0.17, 0.28, 0.4};
        double[] tt = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,1.0};
         tt = cdf.values(t);
        System.out.println(Arrays.toString(t));
        for (int i = 0; i < tt.length; i++) {
            tt[i] = Math.floor(tt[i]);
        }
    }

    /**
     * toXML overrides the function from SampledFunction to
     * rechange the order of x and y
     * @return a String giving an XML version of this instance
     * 17feb2013 Switched to overloading the toXML() function
     * 19feb2013 Changed from using NumberFormat to MyFormat
     */
//    @Override
//    public String toXML() {
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMaximumFractionDigits(2);
//        StringBuffer sb = new StringBuffer("<CDF>" + "\n");
//        for (int i = 0; i < x.length; i++) {
//            sb.append("<functionpoint x=\"" + nf.format(y[i]) + "\" y=\"" + nf.format(x[i]) + "\"/>\n");
//        }
//        sb.append("</CDF>");
//        return sb.toString();
//    }
    
     @Override
    public String toXML() {
        return(toXML("CDF"));
    }   

    @Override
    public String toXML(String name) {
       // NumberFormat nf = NumberFormat.getInstance();
       //nf.setMaximumFractionDigits(2);
        MyFormat nf=new MyFormat();
       StringBuilder sb = new StringBuilder("<" + name + ">" + "\n");
       for (int i = 0; i < x.length; i++) {
         //  sb.append("<functionpoint x=\"" + nf.format(y[i]) + "\" y=\"" + nf.format(x[i]) + "\"/>\n");
            sb.append("<functionpoint x=\"").append(nf.format("%6.4g",y[i])).append("\" y=\"").append(nf.format("%6.4g",x[i])).append("\"/>\n");
       }
       sb.append("</").append(name).append(">\n");
       return sb.toString();
       
        
        
      
    }
    
    public String toXML(String name, String options) {
       // NumberFormat nf = NumberFormat.getInstance();
       //nf.setMaximumFractionDigits(2);
        MyFormat nf=new MyFormat();
       StringBuilder sb = new StringBuilder("<" + name + " "+options +">" + "\n");
       for (int i = 0; i < x.length; i++) {
         //  sb.append("<functionpoint x=\"" + nf.format(y[i]) + "\" y=\"" + nf.format(x[i]) + "\"/>\n");
            sb.append("<functionpoint x= \"").append(nf.format("%6.4g",y[i])).append("\" y= \"").append(nf.format("%6.4g",x[i])).append("\"/>\n");
       }
       sb.append("</").append(name).append(">\n");
       return sb.toString();
       
        
        
      
    }
    
    @Override
     public String toRcode(String [] labels)
    {
        
        StringBuilder sb=new StringBuilder();
        sb.append(labels[0]).append("=c(");
        sb.append(x[0]).append(",").append(y[0]);
        for (int i=1;i<x.length;i++)
                {
                    sb.append(",").append(x[i]).append(",").append(y[i]);
                }
        sb.append(")\n");
        sb.append(labels[0]).append("=matrix(").append(labels[0]).append(",ncol=2,byrow=TRUE)\n");
      
        
            sb.append(labels[0]).append("=").append(labels[0]).append("[,c(2,1)]\n");
        
        sb.append("plot(").append(labels[0]).append(",type = \"b\",xlab=\"").append(labels[1]).append("\",ylab=\"").append(labels[2]).append("\",main=\"").append(labels[3]).append("\")\n");
        return sb.toString();
        
    }
}
