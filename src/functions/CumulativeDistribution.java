package functions;

/*
 * CumulativeDistribution.java
 *
 * Created on April 6, 2006, 2:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author sep
 */
public class CumulativeDistribution extends SampledFunction{
    static final boolean DEBUG=false;
    /** Creates a new instance of CumulativeDistribution */
    
    
    
    public CumulativeDistribution(double []xx, double [] yy) {
        super(yy,xx);
        if(DEBUG)
        {
            if(yy[yy.length-1]!=1.0)
            {
                System.out.println("Creating a CDF without max value 1.0");
            }
          //  if(! isMonotone())
          //  {
          //      System.out.println("The CDF isn't monotone");
          //  }
        }
     
        assert yy[yy.length-1]==1.0;
        
    }
    public boolean isMonotone()
    {
        boolean monotone=true;
        int i=1;
        while(i<x.length && monotone)
        {
            if(y[i-1]>y[i]) {monotone=false;}
            i++;
        }
        return monotone;
    }
   
     public static void main(String[] args) {
        // TODO code application logic here
         double []dx = {0.0,1.0,2.0,3.0};
    double [] dy={0.25,0.5,0.75,1.0};
        CumulativeDistribution cdf=new CumulativeDistribution(dx,dy);
        int v= (int)Math.floor(cdf.value(0.7))+1;
        System.out.println(v);
    }
}
