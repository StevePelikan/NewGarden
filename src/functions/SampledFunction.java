package functions;

/*
 * SampledFunction.java
 *
 * Created on April 23, 2005, 10:06 PM
 */

import java.text.*;
import parameters.MyFormat;

/**
 * SampledFunction does PL interpolation
 * @author sep
 */
public class SampledFunction {

    public static final boolean DEBUG = false;
    public double[] x;
    public double[] y;

    /*
    //some functions to declare, test, enforce monotonicity
    //not currently used of fully implemented
    private boolean MonotoneIncreasing=false;
    private boolean MonotoneDecreasing=false;
    public void setMonotoneIncreasing(boolean v)
    {
    MonotoneIncreasing=v;
    assert this.monotoneIncreasing() : "SampledFunction is not monotone increasing in setMonotoneIncreasing";
    }
    public boolean getMonotoneIncreasing(){return (MonotoneIncreasing);}
    public void setMonotoneDecreasing(boolean v)
    {
    MonotoneDecreasing=v;
    assert this.monotoneDecreasing() : "SampledFunction is not monotone decreasing in setMonotoneDecreasing";
    }
    public boolean getMonotoneIDecreasing(){return (MonotoneDecreasing);}
     */
    /** Creates a new instance of SampledFunction */
    public SampledFunction() {
        if (DEBUG) {
            System.out.println("SampledFunction() called");

        }
        x = null;
        y = null;
    }

    public SampledFunction(double[] xx, double[] yy) {
        if (DEBUG) {
            //System.out.println("SampledFunction(double[],double[]) called");
            if (xx.length != yy.length) {
                System.out.println("SampledFunction ERROR: XX and YY have different lengths.");
            }

        }
        x = xx;
        y = yy;
        if (DEBUG) {
            if (!checkXMonotone()) {
                System.out.println("SampledFunction ERROR: X not monotone");
            }
        }
        //if WE ARE SUPPOSED TO BE MONOTONE, CHECK IF WE ARE
    }

    public boolean checkXMonotone() {
        boolean mono = true;
        if (x == null) {
            return true;
        }
        int i = 0;
        while (i < x.length - 1 && mono) {
            if (x[i] >= x[i + 1]) {
                mono = false;
            }
            i++;
        }
        return mono;
    }

    public boolean monotoneIncreasing() {
        if (y == null) {
            return true;
        }
        boolean mono = true;
        int i = 0;
        while (i < y.length - 1 && mono) {
            if (y[i] >= y[i + 1]) {
                mono = false;
            }
            i++;
        }
        return mono;
    }

    public boolean monotoneDecreasing() {
        if (y == null) {
            return true;
        }
        boolean mono = true;
        int i = 0;
        while (i < y.length - 1 && mono) {
            if (y[i] <= y[i + 1]) {
                mono = false;
            }
            i++;
        }
        return mono;
    }

    // public double [] getX(){return x;}
    //public double [] getY(){return y;}
    public double value(double t) {

        int i = 0;
        while (i <= x.length - 1 && x[i] <= t) {
            i++;
        }
        int j = i - 1;
        if (i <= 0) {
            return y[0];
        }
        if (i >= x.length) {
            return y[y.length - 1];
        } else {
            return y[j] + (y[i] - y[j]) * (t - x[j]) / (x[i] - x[j]);
        }
    }

    public double[] values(double[] t) {
        double[] ans = new double[t.length];
        for (int i = 0; i < t.length; i++) {
            ans[i] = value(t[i]);
        }
        return ans;
    }

    public double maximum_value() {
        double m = y[0];
        for (int i = 1; i < y.length; i++) {
            if (y[i] > m) {
                m = y[i];
            }
        }
        return m;
    }

    public double minimum_value() {
        double m = y[0];
        for (int i = 1; i < y.length; i++) {
            if (y[i] < m) {
                m = y[i];
            }
        }
        return m;
    }

    public void addPoint(double t, double z) {
        if (x == null || y == null) {
            x = new double[1];
            y = new double[1];
            x[0] = t;
            y[0] = z;
            return;
        }
        double[] newx = new double[x.length + 1];
        double[] newy = new double[y.length + 1];
        int i = 0;
        while (i < x.length && x[i] <= t)// changed < tp <= 8 July 2006
        {
            newx[i] = x[i];
            newy[i] = y[i];
            i++;
        }
        newx[i] = t;
        newy[i] = z;
        while (i < x.length) {
            newx[i + 1] = x[i];
            newy[i + 1] = y[i];
            i++;
        }
        x = newx;
        y = newy;
        //IF WE ARE SUPPOSED TO BE MONOTONE, CHECK IF WE ACTUALLY ARE
    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuffer sb = new StringBuffer("Number points = " + x.length + "\n");
        //sb.append("Monotonicity:"+MonotoneDecreasing+" "+MonotoneIncreasing);
        for (int i = 0; i < x.length; i++) {
            sb.append("(" + nf.format(x[i]) + "," + nf.format(y[i]) + ")\n");
        }
        return sb.toString();
    }

    public String toXML() {
        return toXML("SampledFunction");
       // NumberFormat nf = NumberFormat.getInstance();
       // nf.setMaximumFractionDigits(2);
       // StringBuffer sb = new StringBuffer("<SampledFunction>" + "\n");
       // for (int i = 0; i < x.length; i++) {
       //     sb.append("<functionpoint x=\"" + nf.format(x[i]) + "\" y=\"" + nf.format(y[i]) + "\"/>\n");
       // }
       // sb.append("</SampledFunction>");
       // return sb.toString();
    }
    /**
     * 
     * @param names the names of the attributes (in order)
     * @return a string containing the x- and y- data as comma separated lists
     * 
     * new 29 June 2014, to be used with InbreedingDepression
     * and more generally to support storing sampled functions in XML
     * using attributes rather than Vectors or functionpoints
     */
public String toAttributes(String [] names)
{
    MyFormat mf=new MyFormat();
    StringBuilder sb=new StringBuilder();
    sb.append(names[0]+" = \"");
   for(int i=0;i<=this.x.length-2;i++) sb.append(mf.format(this.x[i])+",");
   sb.append(mf.format(this.x[this.x.length-1])+"\"\n");
    sb.append(names[1]+" = \"");
   for(int i=0;i<=this.y.length-2;i++) sb.append(mf.format(this.y[i])+",");
   sb.append(mf.format(this.x[this.y.length-1])+"\"\n");
   return sb.toString();
}
    public String toXML(String name) {
        //NumberFormat nf = NumberFormat.getInstance();
        //nf.setMaximumFractionDigits(2);
        MyFormat mf=new MyFormat();
        StringBuffer sb = new StringBuffer("<" + name + ">" + "\n");
        for (int i = 0; i < x.length; i++) {
            sb.append("<functionpoint x=\"" + mf.format(x[i]) + "\" y=\"" + mf.format(y[i]) + "\"/>\n");
        }
        sb.append("</" + name + ">\n");
        return sb.toString();
    }
     public String toXML(String name,String options) {
        //NumberFormat nf = NumberFormat.getInstance();
        //nf.setMaximumFractionDigits(2);
        MyFormat mf=new MyFormat();
        StringBuffer sb = new StringBuffer("<" + name + " "+ options+" >" + "\n");
        for (int i = 0; i < x.length; i++) {
            sb.append("<functionpoint x=\"" + mf.format(x[i]) + "\" y=\"" + mf.format(y[i]) + "\"/>\n");
        }
        sb.append("</" + name + ">\n");
        return sb.toString();
    }
    /**
     * NEW 29 June 2013
     * @param labels name, xlab, ylab, main
     * @return R statements to plot the graph
     * a function to return a String containing R statements
     * to graph the function
     */
   
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
        
        sb.append("plot(").append(labels[0]).append(",type = \"b\",xlab=\"").append(labels[1]).append("\",ylab=\"").append(labels[2]).append("\",main=\"").append(labels[3]).append("\")\n");
        return sb.toString();
        
    }
    public static void main(String[] args)
    {
        SampledFunction sf=new SampledFunction();
        sf.addPoint(2,3);
        sf.addPoint(4,6);
        sf.addPoint(8, 9);
        System.out.println(sf.toXML("Test"));
        String []names={"x","y"};
        System.out.println(sf.toAttributes(names));
       
    }
}
