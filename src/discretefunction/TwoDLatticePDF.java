/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package discretefunction;

import functions.RandomValue;

/**
 *
 * @author pelikan
 * This class takes a positive (non-negative) function defined by interpolating
 * points and uses it as a pdf on a rectangular lattice, selecting
 * a lattice point according to the pdf by accept/reject method.
 * 
 * 28 March 2014  switched to using bilinear interpolation
 * rather than the more complicated and slower method I used earlier. Haven't
 * tested since making the change
 */
public class TwoDLatticePDF {
    int []x;
    int []y;
    float [][]f;
    int XLOW,XHIGH,YLOW,YHIGH;
    float MAX,MIN;
    boolean extrapolate=false;
    
    public TwoDLatticePDF()
    {
        
    }
    public  void setX(int [] xx){this.x=xx;  assert(checkMonotone(x)):"X points of TwoDLatticePDF are not monotone";}
     public  void setY(int [] yy){this.y=yy;  assert(checkMonotone(y)):"Y points of TwoDLatticePDF are not monotone";}
     public void setF(float []ff)
     {
         assert (x!= null): "X isn't defined yet in TwoDLatticePDF";
         assert (y!= null): "Y isn't defined yet in TwoDLatticePDF";
         assert(ff.length==x.length*y.length): "X,Y,F, wrong sizes in TwoDLatticePDF";
        f=new float[x.length][y.length];
        for(int row=0;row<y.length;row++)
        {
            
            for(int col=0;col<x.length;col++)
            {
                f[row][col]=ff[col+x.length*row];
            }
        }
     }
     public void setUP()
     {
          XLOW=x[0];
        XHIGH=x[x.length-1];
        YLOW=y[0];
        YHIGH=y[y.length-1];
        MAX=-1.0e10f;
        MIN=1.0e10f;
        for(int i=0;i<x.length;i++)
        {
            for(int j=0;j<y.length;j++)
            {
                if(MAX<f[i][j]) MAX=f[i][j];
                if(MIN>f[i][j]) MIN=f[i][j];
            }
        }
        assert(MIN>0.0f): "Minimum value of TwoDLatticePDF not positive";
     }
     public TwoDLatticePDF(int []xx,int[]yy, float []ff)
    {
         assert(checkMonotone(xx)):"X points of TwoDLatticePDF are not monotone";
        assert(checkMonotone(yy)):"Y points of TwoDLatticePDF are not monotone";
         assert(ff.length==x.length*y.length): "X,Y,F, wrong sizes in TwoDLatticePDF";
         
        this.x=xx;
        this.y=yy;
        f=new float[x.length][y.length];
        for(int row=0;row<y.length;row++)
        {
            
            for(int col=0;col<x.length;col++)
            {
                f[row][col]=ff[col+x.length*row];
            }
        }
        //this.f=ff;
       
      
        XLOW=x[0];
        XHIGH=x[x.length-1];
        YLOW=y[0];
        YHIGH=y[y.length-1];
        MAX=-1.0e10f;
        MIN=1.0e10f;
        for(int i=0;i<x.length;i++)
        {
            for(int j=0;j<y.length;j++)
            {
                if(MAX<f[i][j]) MAX=f[i][j];
                if(MIN>f[i][j]) MIN=f[i][j];
            }
        }
        assert(MIN>0.0f): "Minimum value of TwoDLatticePDF not positive";

    }
      public TwoDLatticePDF(int []xx,int[]yy, float [][]ff)
    {
        this.x=xx;
        this.y=yy;
       
        this.f=ff;
        assert(checkMonotone(x)):"X points of TwoDLatticePDF are not monotone";
        assert(checkMonotone(y)):"Y points of TwoDLatticePDF are not monotone";
        assert(x.length==f.length):"X,F wrong sizes in TwoDLatticePDF";
        assert(y.length==f[0].length):"Y,F wrong sizes in TwoDLatticePDF";
        XLOW=x[0];
        XHIGH=x[x.length-1];
        YLOW=y[0];
        YHIGH=y[y.length-1];
        MAX=-1.0e10f;
        MIN=1.0e10f;
        for(int i=0;i<x.length;i++)
        {
            for(int j=0;j<y.length;j++)
            {
                if(MAX<f[i][j]) MAX=f[i][j];
                if(MIN>f[i][j]) MIN=f[i][j];
            }
        }
        assert(MIN>0.0f): "Minimum value of TwoDLatticePDF not positive";

    }
     
     public final boolean checkMonotone(int[]x) {
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
     
//    float value(int tx, int ty)
//    {
//        assert(tx>=x[0] && tx<=x[x.length-1]) : "tx ="+tx+" out of range "+x[0]+"-"+x[x.length-1];
//        assert(ty>=y[0] && ty<=y[y.length-1]) : "ty ="+ty+" out of range "+y[0]+"-"+y[y.length-1];
//        int xi = 0;
//        int xj;
//        while (xi <= x.length - 1 && x[xi] <= tx) {
//            xi++;
//        }
//        xj= xi - 1;
//        if(xi==0){xj=0;xi=1;}
//        if(xi==x.length){xi=x.length-1;xj=xi-1;}
//        
//        //tx is between x[xj] < x[xi]
//        //unless xi==0 or xi>=x.length
//        int yi = 0;
//        while (yi <= x.length - 1 && y[yi] <= ty) {
//            yi++;
//        }
//        int yj = yi - 1;
//        if(yi==0){yj=0;yi=1;}
//        if(yi==y.length){yi=y.length-1;yj=yi-1;}
//        //x[xj],y[yj],x[xi],y[yj],x[xi],y[yi],x[xj],y[yi]
//        //all triple to find best fit to tx,ty
//        int maxat=0;
//        double d2=(tx-x[xj])*(tx-x[xj])+(ty-y[yj])*(ty-y[yj]);
//        double max=d2;
//
//        d2=(tx-x[xi])*(tx-x[xi])+(ty-y[yj])*(ty-y[yj]);
//        if(d2>max){ maxat=1; max=d2;}
//
//        d2=(tx-x[xi])*(tx-x[xi])+(ty-y[yi])*(ty-y[yi]);
//        if(d2>max){ maxat=2; max=d2;}
//
//        d2=(tx-x[xj])*(tx-x[xj])+(ty-y[yi])*(ty-y[yi]);
//        if(d2>max){ maxat=3; max=d2;}
//
//        //ignore the corner farthest from tx,ty
//        
//        /*
//         * We want (sum a_i) (x,y)=\sum a_i points[i]
//         * since we will then return sum a_i points[i].f/ sum a_i
//         * a_1 points[1].x + a_2 points[2].x + (1-a_1-a_2)points[3].x=x
//         * a_1 points[1].y + a_2 points[2].y + (1-a_1-a_2)points[3].y=y
//         */
//        float value=0.0f;
//        switch(maxat)
//        {
//            case 0:
//                value=plinterp(tx,ty,x[xi],y[yi],f[xi][yi],
//                                 x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//                break;
//            case 1:
//                value=plinterp(tx,ty,x[xi],y[yi],f[xi][yi],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//                break;
//            case 2:
//                value=plinterp(tx,ty,x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//                break;
//            case 3:
//                 value=plinterp(tx,ty,x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xi],y[yi],f[xi][yi]);
//                 break;
//
//        }
//        /*if(maxat==0)//leave out xj yj
//        {
//            value=plinterp(tx,ty,x[xi],y[yi],f[xi][yi],
//                                 x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//        }
//
//        if(maxat==1)//leave out xi yj
//        {
//            value=plinterp(tx,ty,x[xi],y[yi],f[xi][yi],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//        }
//        if(maxat==2)//leave out xi yi
//        {
//            value=plinterp(tx,ty,x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xj],y[yi],f[xj][yi]);
//        }
//
//         if(maxat==3)//leave out xj yi
//        {
//            value=plinterp(tx,ty,x[xi],y[yj],f[xi][yj],
//                                 x[xj],y[yj],f[xj][yj],
//                                 x[xi],y[yi],f[xi][yi]);
//        }*/
//
//        return value;
//
//
//    }
//    
    
//    float plinterp(double tx, double ty,double x1,double y1,double f1,
//            double x2,double y2,double f2,double x3,double y3,double f3)
//    {
//        /*
//         * We want (sum a_i) (x,y)=\sum a_i points[i]
//         * since we will then return sum a_i points[i].f/ sum a_i
//         * a_1 x1 + a_2 x2 + (1-a_1-a_2)x3=tx
//         * a_1 y1 + a_2 y2 + (1-a_1-a_2)y3=ty
//         * 
//         * a1 (x1-x3) +a2(x2-x3) +x3=tx
//         * a1 (y1-y3) +a2(y2-y3) +y3=ty
//         * 
//         * a1 (x1-x3) +a2(x2-x3) =(tx-x3)
//         * a1 (y1-y3) +a2(y2-y3) =(ty-y3)
//         * 
//         * 
//         * a1 (x1-x3) +a2(x2-x3)(y1-y3) =(tx-x3)(y1-y3)
//         * a1 (y1-y3) +a2(y2-y3)(x1-x3) =(ty-y3)(x1-x3)
//         * a2( (x2-x3)(y1-y3)-(y2-y3)(x1-x3))=(tx-x3)(y1-y3)-(ty-y3)(x1-x3)
//         * 
//         */
//        double det=(x2-x3)*(y1-y3)-(y2-y3)*(x1-x3);
//        double a2=((tx-x3)*(y1-y3)-(ty-y3)*(x1-x3))/det;
//        double a1=((tx-x3)-a2*(x2-x3))/(x1-x3);
//        double a3=1.0-a1-a2;
//        return (float)(a1*f1+a2*f2+a3*f3);
//         
//    }
//    
     public float value(float tx,float ty)
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
   public  int [] randomPoint(RandomValue rval)
    {
     int [] ans=new int[2];
     while(true)
     {
         ans[0]=rval.RandomInt(XLOW, XHIGH);
         ans[1]=rval.RandomInt(YLOW, YHIGH);
         if(rval.Uniform(0,1,1)[0]< value(ans[0],ans[1])/MAX) return ans;
         
     }
     
  
    }
    public String toXML(String name, String [] attributes)
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
        sb.append("\n<Vector name =\"x\" length = \"").append(x.length).append("\" values =\"").append(makeString(x)).append("\"");    
         sb.append(">\n<Vector name =\"y\" length = \"").append(y.length).append("\" values =\"").append(makeString(y)).append("\""); 
         float[]fff=new float[x.length*y.length];
         for(int row=0;row<y.length;row++)
         {
             for(int col=0;col<x.length;col++)
             {
                 fff[row*y.length+col]=f[row][col];
             }
         }
          sb.append(">\n<Vector name =\"f\" length = \"").append(x.length*y.length).append("\" values =\"").append(makeString(fff)).append("\"/>"); 
       //  sb.append("/>"); 
          sb.append("\n</").append(name).append(">\n");
        return sb.toString();
    }
    public String toXML()
    {
        return toXML("TwoDLatticePDF",null);
    }
    
    String makeString(float[] v)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<v.length-1;i++)
        {
            sb.append("").append(v[i]).append(",");
        }
        sb.append("").append(v[v.length-1]);
        return sb.toString();
    }
     String makeString(int[] v)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<v.length-1;i++)
        {
            sb.append("").append(v[i]).append(",");
        }
        sb.append("").append(v[v.length-1]);
        return sb.toString();
    }
    public static void main(String [] args)
    {
        RandomValue rv=new RandomValue();
        int []x={0,5,10};
        int []y={0,5,10};
        float [][]f={{1,2,3},{4,5,6},{7,8,9}};
        float []fff={2,3,4,5,6,7,8,9,1};
        
        TwoDLatticePDF pdf=new TwoDLatticePDF(x,y,f);
        int[]ans=pdf.randomPoint(rv);
        System.out.println("("+ans[0]+","+ans[1]+")");
        System.out.println(pdf.makeString(x));
        System.out.println(pdf.toXML());
        
        TwoDLatticePDF pdf1=new TwoDLatticePDF(x,y,fff);
        String[] ats={"a","A","b","B"};
         System.out.println(pdf1.toXML("TestName",ats));
        
    }
    
}
