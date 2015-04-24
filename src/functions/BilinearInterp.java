/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package functions;
import java.util.Arrays;
import parameters.MyFormat;
/**
 * BilinearInterp does bilinear interpolation
 * assuming it has been supplied with the values of a function
 * on a uniform grid.
 *
 * If extrapolate is true, it rounds variables down/up
 * to reach the boundary of the grid if the argument lies outside.
 *
 * If extrapolate is false it reports an error (assert does if -ea)
 * if the argument is outside the grid.
 * @author sep
 */
public class BilinearInterp {
    private double []x;
    private double []y;
    private double [][]f;
    private double delta=1.0f;
    private boolean extrapolate=false;
    public BilinearInterp(double []xx,double []yy,double[][]ff)
    {
        x=xx;
        y=yy;
        f=ff;
        delta=x[1]-x[0];
        assert(isValid()):"Invalid data for BilinearInterp";
    }
    public BilinearInterp reallycopy()
    {
        double []xx=new double[x.length];
        double []yy=new double[y.length];
        double [][]ff=new double[f.length][f[0].length];
        for(int i=0;i<x.length;i++) xx[i]=x[i];
        for(int i=0;i<y.length;i++) yy[i]=y[i];
        for(int i=0;i<ff.length;i++)
        {
            for(int j=0;j<f[0].length;j++)
            {
                ff[i][j]=f[i][j];
            }
        }
        return (new BilinearInterp(xx,yy,ff));
    }
    private boolean isValid()
    {
        boolean ans=true;
        double tol=0.0001;
        delta=x[1]-x[0];
        for(int i=1;i<x.length;i++)
        {
            if(Math.abs(x[i]-x[i-1] - delta)>tol)
            {
                ans=false;
                return ans;
            }
        }
        for(int i=1;i<y.length;i++)
        {
            if(Math.abs(y[i]-y[i-1] - delta)>tol)
            {
                ans=false;
                return ans;
            }
        }
        return ans;
    }
    public boolean setExtrapolate(boolean val)
    {
        this.extrapolate=val;
        return val;
    }
    public boolean getExtrapolate()
    {
        return this.extrapolate;
    }
    public double[] values(double []tx,double[] ty)
    {
        assert(tx.length== ty.length):"bad data in values()";
        double []ans=new double[tx.length];
        for(int i=0;i<tx.length;i++)
        {
            ans[i]=value(tx[i],ty[i]);
        }
        return ans;
    }
    public double value(double tx,double ty)
    {
        if(!extrapolate)
        {
        assert (x[0]<=tx && tx<= x[x.length-1]): "tx = "+tx+" out of range";
        assert (y[0]<=ty && ty<= y[y.length-1]): "ty = "+ty+" out of range";
        }
        else
        {
            if (tx<x[0]) tx=x[0];
            if (tx>x[x.length-1]) tx= x[x.length-1];
            if (ty<y[0]) ty=y[0];
            if (ty>y[y.length-1]) ty= y[y.length-1];

        }

        int xi,yi;
        //find xi so x[xi]<= tx <=x[xi+1] and 0\le xi\le x.length-2
        xi=0;
        while(tx<x[xi] && xi<x.length-2) xi++;
        yi=0;
        while(ty<y[yi] && yi<y.length-2) yi++;
        tx -= x[xi];
        tx/=(x[xi+1]-x[xi]);
        ty -= y[yi];
        ty/=(y[yi+1]-y[yi]);
        return f[xi][yi]+
                (f[xi+1][yi]-f[xi][yi])*tx+
                (f[xi][yi+1]-f[xi][yi])*ty+
                (f[xi][yi]+f[xi+1][yi+1]-f[xi][yi+1]-f[xi+1][yi])*tx*ty;

    }
    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("BilinearInterp:\n extrapolate = \""+extrapolate+"\"\n");
        sb.append("x= "+Arrays.toString(x)+"\n");
        sb.append("y="+Arrays.toString(y)+"\n");
        sb.append("f=[");
        for(int i=0;i<x.length;i++)
        {
            sb.append(Arrays.toString(f[i]));
            if(i<x.length-1) sb.append(",\n");
        }
        sb.append("]\n");
        return sb.toString();
    }
     private String makeString(double[] v)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<v.length-1;i++)
        {
            sb.append("").append(v[i]).append(",");
        }
        sb.append("").append(v[v.length-1]);
        return sb.toString();
    }
     
     //xname=".." yname="..." fname="...." 
     /**
      * 
      * @param name the text of the XML element's name for begin/end
      * @param vectornames then names of the x, y, and functionvalue Vectors
      * @param attributes additional attributes for the main XML element
      *     and even number of Strings giving attribute names and values
      *     to include in the beginning of the main XML element
      * @return A String that represents this BilinearInterp in XML
      *     suitable for parsing by SimDataReader
      */
      public String toXML(String name, String[] vectornames,String [] attributes)
    {
        StringBuilder sb=new StringBuilder();
        sb.append("<").append(name).append(" ");
        //add attributes
        if(attributes !=null)
        {
            for(int i=0;i<attributes.length/2;i++)
            {
                sb.append(attributes[2*i]).append("= \"").append(attributes[2*i+1]).append("\" ");
            }
        }
        sb.append(">");
        sb.append("\n<Vector name =\""+vectornames[0]+"\" length = \"").append(x.length).append("\" values =\"").append(makeString(x)).append("\"");    
         sb.append(">\n<Vector name =\""+vectornames[1]+"\" length = \"").append(y.length).append("\" values =\"").append(makeString(y)).append("\""); 
         double[]fff=new double[x.length*y.length];
         for(int row=0;row<y.length;row++)
         {
             for(int col=0;col<x.length;col++)
             {
                 fff[row*y.length+col]=f[row][col];
             }
         }
          sb.append(">\n<Vector name =\""+vectornames[2]+"\" length = \"").append(x.length*y.length).append("\" values =\"").append(makeString(fff)).append("\"/>"); 
       //  sb.append("/>"); 
          sb.append("\n</").append(name).append(">\n");
        return sb.toString();
    }
    public String toXML(String name)
    {
        StringBuilder sb=new StringBuilder();
        sb.append("<"+name+" ");
        sb.append(" extrapolate =\""+extrapolate+"\"\n");
        sb.append(" x =\""+Arrays.toString(x)+"\"\n");
        sb.append(" y =\""+Arrays.toString(y)+"\"\n");
        sb.append(" f = \"[");
         for(int i=0;i<x.length;i++)
        {
            sb.append(Arrays.toString(f[i]));
            if(i<x.length-1) sb.append(",");
        }
        sb.append("]\"");
        sb.append("/>");
        return sb.toString();

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
   sb.append(mf.format(this.y[this.y.length-1])+"\"\n");
  
   sb.append(names[2]+" = \"");
    
     for(int i=0;i<x.length;i++)
        {
            for(int j=0;j<y.length;j++)
            {sb.append(f[i][j]);
            if(i<x.length-1 || j <y.length-1) sb.append(",");
            }
        }
     sb.append("\"");
   
   return sb.toString();
}
    public static void main(String [] args)
    {
        double []x={0.0f,2.0f,4.0f};
        double []y={4.0f,6.0f,8.0f};
        double [][]f={{1.0f,2.0f,3.0f},{4.0f,5.0f,6.0f},{7.0f,8.0f,9.0f}};
        BilinearInterp bli=new BilinearInterp(x,y,f);
        bli.setExtrapolate(true);
        System.out.println(bli.value(5.0f, 8.0f));
        System.out.println(bli.toXML("spline"));
        
         double []xx={0,10};
        double[] yy={0,10};
        double [][]ff={{0,10},{20,40}};
         bli=new BilinearInterp(xx,yy,ff);
      System.out.println("Value at (0,0)="+bli.value(0,0)+" should equal 0");
System.out.println("Value at (0,10)="+bli.value(0,10)+" should equal 10");
  System.out.println("Value at (10,0)="+bli.value(10,0)+" should equal 20");
   System.out.println("Value at (10,10)="+bli.value(10,10)+" should equal 40");
   
    double []xxx={1,2,3};
        double []yyy={5,6,7};
        double [][]fff={{1,2,3},{4,5,6},{7,8,9}};

        BilinearInterp f2=new BilinearInterp(xxx,yyy,fff);
        System.out.println("Value at (1,5)="+f2.value(1,5)+" should equal 1");
        System.out.println("Value at (1,6)="+f2.value(1,6)+" should equal 2");
        System.out.println("Value at (2,6)="+f2.value(2,6)+" should equal 5");
        System.out.println("Value at (2,7)="+f2.value(2,7)+" should equal 6");

        System.out.println("Value at (3,7)="+f2.value(3,7)+" should equal 9");

        System.out.println("Value at (1.5,5.05)="+f2.value(1.5f,5.05f));
        System.out.println("Value at (1.0,6.10)="+f2.value(1.0f,6.10f));
        
        String []vectornames={"depx","depy","depf"};
        String [] atts={"use","true","depressionalleles","3,4,5,6",
    "depression_reproduction","true",
    "depression_mortality","true"};
        System.out.println(f2.toXML("InbreedingDepression",vectornames ,atts));
        String[] names={"x","y","f"};
        System.out.println(f2.toAttributes(names));
    }
}
