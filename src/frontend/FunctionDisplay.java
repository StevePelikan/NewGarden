package frontend;

/*
 * FunctionDisplay.java
 *
 * Created on April 23, 2005, 10:46 PM
 */
import functions.SampledFunction;
import javax.swing.*;
import java.awt.*;
import java.awt.Graphics2D.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;
/**
 *
 * @author sep
 */
public class FunctionDisplay extends JComponent
        implements /*Scrollable*/ MouseListener, MouseMotionListener,ActionListener {
    public editableSampledFunction sf=null;
    BufferedImage ourImage=null;
    int ourWidth,ourHeight;
    int xlow,xhigh,ylow,yhigh;
    double maxX,minX,maxY,minY;
    double Xslope,Yslope;
    int MarginWidth=20;
    int PixelNbhd=10;
    int lastX,lastY,lastI;
    boolean maintainOrder=true;
    private static final boolean DEBUG=true;
    NumberFormat nf;
    /** Creates a new instance of FunctionDisplay */
    public FunctionDisplay(editableSampledFunction ff,int w,int h,double mx, double mmx, double my, double mmy) {
        sf=ff;
        ourWidth=w;
        ourHeight=h;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        maxX=mmx;
        minX=mx;
        maxY=mmy;
        minY=my;
        nf=NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        MakeImage();
    }
    public Dimension getPreferedSize(){return new Dimension(ourWidth,ourHeight);}
    public void paintComponent(Graphics g) {
        Graphics2D g2d=(Graphics2D) g;
        if(ourImage==null) MakeImage();
        g2d.drawImage(ourImage,0,0,null);
    }
    /**
     *We'll put the picture of the function in a smaller window with axes and labels on the left and below
     * THe indentaation for the axes and labels is MarginWidth
     **/
    public void MakeImage() {
        ourImage=new BufferedImage(ourWidth,ourHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d=ourImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0,0, ourWidth, ourHeight);
        
        //just draw lines for now
        xlow=0+MarginWidth;
        xhigh=ourWidth;
        ylow=ourHeight-MarginWidth;
        yhigh=0+MarginWidth;
        //to make more room, we should specify these in constructor
        // maxY=sf.maximum_value();
        // minY=sf.minimum_value();
        // maxX=sf.x[sf.x.length-1];
        // minX=sf.x[0];
        //loop to draw x0->x1,...,x(n-2)->x(n-1)
        // x goes linearly to a pixel so that minX->xlow, maxX->xhigh
        Xslope = ((double)(xhigh-xlow))/(maxX-minX);
        Yslope = ((double)(yhigh-ylow))/(maxY-minY);
        int xp0,yp0,xp1=0,yp1=0;
        g2d.setColor(Color.BLACK);
        for(int i=0;i<sf.x.length-1;i++) {
            xp0 = xlow +(int)Math.round(Xslope*(sf.x[i]-minX));
            yp0=  ylow +(int)Math.round(Yslope*(sf.y[i]-minY));
            xp1 = xlow +(int)Math.round(Xslope*(sf.x[i+1]-minX));
            yp1=  ylow +(int)Math.round(Yslope*(sf.y[i+1]-minY));
            g2d.drawLine(xp0, yp0,xp1, yp1);
            g2d.drawArc(xp0-2, yp0-2, 4, 4, 0, 360);
            
        }
        g2d.drawArc(xp1-2, yp1-2, 4, 4, 0, 360);
        //draw axes and labels
        g2d.setColor(Color.BLUE);
        g2d.drawLine(xlow,ylow,xhigh,ylow);
        g2d.drawLine(xlow,ylow,xlow,yhigh);
        
        g2d.drawString(nf.format(minX),xlow,ylow+MarginWidth/2);
        String t=nf.format(maxX);
        int len=t.getBytes().length;
        len *=10;
        g2d.drawString(nf.format(maxX),xhigh-len,ylow+MarginWidth/2);
        g2d.drawString(nf.format(minY),xlow-MarginWidth,ylow);
        g2d.drawString(nf.format(maxY),xlow-MarginWidth,yhigh+MarginWidth/2);
        g2d.dispose();
    }
    private void drawAxes()
    {
        Graphics2D g2d=ourImage.createGraphics();
        g2d.setColor(Color.blue);
        xlow=0+MarginWidth;
        xhigh=ourWidth;
        ylow=ourHeight-MarginWidth;
        yhigh=0+MarginWidth;
        g2d.drawLine(xlow,ylow,xhigh,ylow);
        g2d.drawLine(xlow,ylow,xlow,yhigh);
        
        g2d.drawString(nf.format(minX),xlow,ylow+MarginWidth/2);
        String t=nf.format(maxX);
        int len=t.getBytes().length;
        len *=10;
        g2d.drawString(nf.format(maxX),xhigh-len,ylow+MarginWidth/2);
        g2d.drawString(nf.format(minY),xlow-MarginWidth,ylow);
        g2d.drawString(nf.format(maxY),xlow-MarginWidth,yhigh+MarginWidth/2);
        g2d.dispose();
    }
    private int XtoScreen(double x){return xlow +(int)Math.round(Xslope*(x-minX));}
    private int YtoScreen(double y){return ylow +(int)Math.round(Yslope*(y-minY));}
    private double screenToX(int x) {
        return  ( (double)(x-xlow))/Xslope+minX;
    }
    private double screenToY(int y) {
        return  ( (double)(y-ylow))/Yslope+minY;
    }
    public void mouseClicked(MouseEvent e) {
        int where=-1,sample=-1;
        int myx=e.getX();
        int myy=e.getY();
        System.out.println("mouseClicked() ("+myx+","+myy+")" +" <-> ("+nf.format(screenToX(myx))+","+nf.format(screenToY(myy))+")");
        //if shift, insert a point provided we are near the graph
        //controlshift delete point provided we are near one
        
    }
    public void mouseDragged(MouseEvent e) {
        //if left buton and shift, we are dragging a cursor
        int myx=e.getX();
        int myy=e.getY();
        
       // System.out.println("mouseDragged() ("+myx+","+myy+")" +" <-> ("+nf.format(screenToX(myx))+","+nf.format(screenToY(myy))+")");
        //dont move the mouse if it will make a nonfunction
        
        /*
         *lastI is the index of the point we are dragging. If the new myxx corresponds to a sfxvalue smaller than lastI-1
         *or bigger than lastI+1's don't do anything
         */
         if(lastI==-1) return;
        if(maintainOrder) {
           
            if(lastI==0 && myx>=XtoScreen(sf.x[1])) 
            {
                if(DEBUG){System.out.println("Tried to move point 0 to right of point 1");}
                return;
            }
            else if(lastI==sf.x.length-1 && myx<=XtoScreen(sf.x[sf.x.length-2]))
            {
                    
             if(DEBUG){System.out.println("Tried to move last point to left of next to last point");}
                    return;
            }
            else if(lastI<sf.x.length-1 && lastI>0&&(myx>= XtoScreen(sf.x[lastI+1]) || myx<=XtoScreen(sf.x[lastI-1])))
            {
                 
              if(DEBUG){
                  System.out.println("Tried to move point "+lastI+" out of order");
                 // System.out.println("myx="+myx+"left x ="+XtoScreen(sf.x[lastI-1]));
              }
                 return;
            
            }
        }
        //MARCH 2014 Enforce Monoto condition if called for
        
        if(lastI==0 && sf.XLowFixed) myx=this.XtoScreen(sf.XLow);
        if(lastI==sf.x.length-1 && sf.XHighFixed) myx=this.XtoScreen(sf.XHigh);
        
        if(sf.YLowFixed && myy >this.YtoScreen(sf.YLow)) myy=this.YtoScreen(sf.YLow);
        if(sf.YHighFixed && myy < this.YtoScreen(sf.YHigh)) myy=YtoScreen(sf.YHigh);
        
        if(sf.MonotoneIncreasing)
        {
            if(lastI==0 && myy <YtoScreen(sf.y[lastI+1])) myy=YtoScreen(sf.y[lastI+1]);
            if(0<lastI && lastI < sf.x.length-1)
            {
                if(myy <YtoScreen(sf.y[lastI+1]))myy=YtoScreen(sf.y[lastI+1]);
                if(myy >YtoScreen(sf.y[lastI-1]))myy=YtoScreen(sf.y[lastI-1]);
            }
            if(lastI==sf.x.length-1)if(myy >YtoScreen(sf.y[lastI-1]))myy=YtoScreen(sf.y[lastI-1]);
        }
       
        //We have moved a control point.
       
        
        //if ti is valid, we are actually moving a point.
        //
        //Erase the old lines x(yt-1) -> x(ti) and x(ti)->x(ti+10
        Graphics2D g2d=ourImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.drawArc(lastX-2, lastY-2, 4, 4, 0, 360);
        if(lastI==0) {
            //just move line to our right
            g2d.drawLine(lastX, lastY, XtoScreen(sf.x[1]), YtoScreen(sf.y[1]));
            g2d.setColor(Color.BLACK);
            g2d.drawLine(myx, myy, XtoScreen(sf.x[1]), YtoScreen(sf.y[1]));
            g2d.drawArc(myx-2, myy-2, 4, 4, 0, 360);
        } else if(lastI==sf.x.length-1) {
            //just move line to our left
            int nn=sf.x.length-2;
            g2d.drawLine(lastX, lastY, XtoScreen(sf.x[nn]), YtoScreen(sf.y[nn]));
            g2d.setColor(Color.BLACK);
            g2d.drawLine(myx, myy, XtoScreen(sf.x[nn]), YtoScreen(sf.y[nn]));
            g2d.drawArc(myx-2, myy-2, 4, 4, 0, 360);
        } else {
            //move both lines
            int nn=sf.x.length-2;
            g2d.drawLine(lastX, lastY, XtoScreen(sf.x[lastI-1]), YtoScreen(sf.y[lastI-1]));
            g2d.drawLine(lastX, lastY, XtoScreen(sf.x[lastI+1]), YtoScreen(sf.y[lastI+1]));
            g2d.setColor(Color.BLACK);
            g2d.drawLine(myx, myy, XtoScreen(sf.x[lastI-1]), YtoScreen(sf.y[lastI-1]));
            g2d.drawLine(myx, myy, XtoScreen(sf.x[lastI+1]), YtoScreen(sf.y[lastI+1]));
            g2d.drawArc(myx-2, myy-2, 4, 4, 0, 360);
            
        }
        //update the values in sf DO THIS!
        
        // sf.x[lastI]=screenToX(myx);
        //    sf.y[lastI]=screenToY(myy);
        //and draw new lines
        lastX=myx;
        lastY=myy;
        drawAxes();
        repaint();
    }
    /**
     * Part of the mouse listening interface
     * @param e the MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        
    
    }
    
    /**
     * Part of the mouse listening interface
     * @param e the MouseEvent
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /**
     *
     * @param e
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     *
     * @param e
     */
    public void mousePressed(MouseEvent e) {
        //we are trying to drag a control point
        //if we're close to a real control point
        //find its index in sf store it in ti
        //store current values in tx, ty
        //select the active point and store its index
        int myx=e.getX();
        int myy=e.getY();
        double xx=screenToX(myx);
        double yy=screenToY(myy);
        System.out.println("mousePressed() ("+myx+","+myy+")" +" <-> ("+nf.format(xx)+","+nf.format(yy)+")");
        int nearestpoint=sf.nearestPoint(xx,yy);
        if((e.getModifiers()&InputEvent.SHIFT_MASK)==0) {
            double ourdist=Math.sqrt((XtoScreen(sf.x[nearestpoint])-myx)*(XtoScreen(sf.x[nearestpoint])-myx)+
                    (YtoScreen(sf.y[nearestpoint])-myy)*(YtoScreen(sf.y[nearestpoint])-myy));
            System.out.println("Nearest point is "+nearestpoint+" dist = "+ourdist);
            if( ourdist< PixelNbhd) {
                lastX=XtoScreen(sf.x[nearestpoint]);
                lastY=YtoScreen(sf.y[nearestpoint]);
                lastI=nearestpoint;
            } else{lastI=-1;}
        }
        //if SHIFT is down, we are inserting a new point.
        else if((e.getModifiers()&InputEvent.SHIFT_MASK)==1) {
            System.out.println("Shift is down");
            int i=0;
            sf.addPoint(xx,yy);
            MakeImage();
            repaint();
            
        }
    }
    
    /**
     *
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
        //update the control point in the function
        int myx=e.getX();
        int myy=e.getY();
        System.out.println("mouseReleased() ("+myx+","+myy+")" +" <-> ("+nf.format(screenToX(myx))+","+nf.format(screenToY(myy))+")");
        if(lastI!= -1) {
            sf.x[lastI]=screenToX(myx);
            if(sf.XLowFixed && sf.x[lastI]<sf.XLow)sf.x[lastI]=sf.XLow;
            if(sf.XHighFixed && sf.x[lastI]>sf.XHigh)sf.x[lastI]=sf.XHigh;
            
            sf.y[lastI]=screenToY(myy);
            if(sf.YLowFixed && sf.y[lastI]<sf.YLow)sf.y[lastI]=sf.YLow;
            if(sf.YHighFixed && sf.y[lastI]>sf.YHigh)sf.y[lastI]=sf.YHigh;
            
            
            if(sf.MonotoneIncreasing)
            {
                 if(lastI==0 && sf.y[lastI ] >sf.y[lastI+1]) sf.y[lastI ] =sf.y[lastI+1];
            if(0<lastI && lastI < sf.x.length-1)
            {
                if(sf.y[lastI] >sf.y[lastI+1])sf.y[lastI]=sf.y[lastI+1];
                if(sf.y[lastI]< sf.y[lastI-1])sf.y[lastI]=sf.y[lastI-1];
            }
            if(lastI==sf.x.length-1)
            {
                if(sf.y[lastI]<sf.y[lastI-1])sf.y[lastI]=sf.y[lastI-1];
            }
            }
             if(sf.Periodic)
        {
            if(lastI==0)
            {
                sf.y[sf.y.length-1]=sf.y[0];
                sf.x[sf.x.length-1]=sf.XHigh;
                sf.x[0]=sf.XLow;
            }
            if(lastI==sf.x.length-1)
            {
                sf.y[0]=sf.y[sf.y.length-1];
                sf.x[sf.x.length-1]=sf.XHigh;
                sf.x[0]=sf.XLow;
            }
             MakeImage();
            repaint();
            //REDRAW THE PICTURE
        }
            lastI=-1;
        }
        
    }
    public void actionPerformed(ActionEvent e){
        System.out.println(e.toString());
    }
}
