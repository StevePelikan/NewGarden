package discretefunction;

/*
 * SampledFunction.java
 *
 * Created on April 23, 2005, 10:06 PM
 */
import java.text.*;

/**
 * SampledFunction does PL interpolation
 * @author sep
 */
public class SampledFunction {

    public static final boolean DEBUG = false;
    private double[] x;
    private double[] y;
    private boolean MonotoneIncreasing = false;
    private boolean MonotoneDecreasing = false;
    private boolean extrapolate=false;

    /*
    //some functions to declare, test, enforce monotonicity
    //not currently used of fully implemented

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
            System.out.println("SampledFunction(double[],double[]) called");
            assert (xx.length == yy.length) : "X and Y not the same length\n";

        }
        x = xx;
        y = yy;
        if (DEBUG) {
            assert (this.checkXMonotone()) : "X values aren't monotone\n";
        }
    }
public boolean getExtrapolate(){return this.extrapolate;}
public boolean setExtrapolate(boolean val){this.extrapolate=val; return val;}
    public final boolean checkXMonotone() {
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

    public boolean checkMonotoneIncreasing() {
        if (y == null) {
            return true;
        }
        boolean mono = true;
        int i = 0;
        while (i < y.length - 1 && mono) {
            if (y[i] >= y[i + 1]) {
                mono = false;
                break;
            }

            i++;
        }
        return mono;
    }

    public void setMonotoneIncreasing(boolean v) {
        MonotoneIncreasing = v;
        if (this.MonotoneIncreasing) {
            assert this.checkMonotoneIncreasing() : "SampledFunction is not monotone increasing in setMonotoneIncreasing";
        }
    }

    public boolean getMonotoneIncreasing() {
        //this.checkMonotoneIncreasing();
        return (MonotoneIncreasing);

    }

    public boolean checkMonotoneDecreasing() {
        if (y == null) {
            return true;
        }
        boolean mono = true;
        int i = 0;
        while (i < y.length - 1 && mono) {
            if (y[i] <= y[i + 1]) {
                mono = false;
                break;
            }
            i++;
        }
        return mono;
    }

    public void setMonotoneDecreasing(boolean v) {
        MonotoneDecreasing = v;
        if (MonotoneDecreasing) {
            assert this.checkMonotoneDecreasing() : "SampledFunction is not monotone decreasing in setMonotoneDecreasing";
        }
    }

    public boolean getMonotoneDecreasing() {
        //this.checkMonotoneDecreasing();
        return (MonotoneIncreasing);

    }

    public double [] getX(){return x;}
    public double [] getY(){return y;}
    public double value(double t) {
        if(!extrapolate) assert(t>= x[0] && t<= x[x.length-1]): "t = "+t+"out of range";
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
        if (this.MonotoneIncreasing) {
            return y[y.length - 1];
        }
        if (this.MonotoneDecreasing) {
            return y[0];
        } else {
            for (int i = 1; i < y.length; i++) {
                if (y[i] > m) {
                    m = y[i];
                }
            }
            return m;
        }
    }

    public double minimum_value() {
        double m = y[0];
        if (this.MonotoneIncreasing) {
            return y[0];
        }
        if (this.MonotoneDecreasing) {
            return y[y.length - 1];
        } else {
            for (int i = 1; i < y.length; i++) {
                if (y[i] < m) {
                    m = y[i];
                }
            }
            return m;
        }
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
        if (MonotoneIncreasing) {
            assert (checkMonotoneIncreasing()) : "addPoint() destroyed monotonicity";
        }
        if (MonotoneDecreasing) {
            assert (checkMonotoneDecreasing()) : "addPoint() destroyed monotonicity";
        }

    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder("Number points = " + x.length + "\n");
        //sb.append("Monotonicity:"+MonotoneDecreasing+" "+MonotoneIncreasing);
        for (int i = 0; i < x.length; i++) {
            sb.append("(").append(nf.format(x[i])).append(",").append(nf.format(y[i])).append(")\n");
        }
        return sb.toString();
    }

    public String toXML() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder("<SampledFunction>" + "\n");
        for (int i = 0; i < x.length; i++) {
            sb.append("<functionpoint x=\"").append(nf.format(x[i])).append("\" y=\"").append(nf.format(y[i])).append("\"/>\n");
        }
        sb.append("</SampledFunction>");
        return sb.toString();
    }

    public String toXML(String name) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        StringBuilder sb = new StringBuilder("<" + name);
        sb.append(" extrapolate = \"").append(extrapolate).append("\" ");
        sb.append(" monotoneincreasing = \"").append(MonotoneIncreasing).append("\" ");
        sb.append(" monotonedecreasing = \"").append(MonotoneDecreasing).append("\" ");
        sb.append(">" + "\n");
        for (int i = 0; i < x.length; i++) {
            sb.append("<functionpoint x=\"").append(nf.format(x[i])).append("\" y=\"").append(nf.format(y[i])).append("\"/>\n");
        }
        sb.append("</" + name + ">\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {3, 4, 5, 6, 7};
        SampledFunction f = new SampledFunction(x, y);
        f.addPoint(3.5,5.9 );
        f.setMonotoneIncreasing(true);
        f.setExtrapolate(true);
        System.out.println(f.toXML("testfunction") + f.value(5.5) + " " + f.maximum_value()+" "+f.getMonotoneIncreasing());
    }
}
