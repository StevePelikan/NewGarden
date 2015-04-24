package functions;

/*
 * DiscreteProbabilityDistribution.java
 *
 * Created on July 15, 2005, 10:12 AM
 *
 *
 *
 *TO DO;
 *1) It'd be nice to read and write this class from Strings and perhaps from XML
 */

/**
 *
 * @author sep
 */
import java.util.*;
import java.text.*;
import parameters.MyFormat;

/**
 *class DiscreteProbabilityDistribution maintains the data for a finite probability distribution
 */
public class DiscreteProbabilityDistribution {

    /**
     * Turns on/off checks and messages that are potentially useful
     * for debugging
     */
    public static final boolean Debug = true;
    /**The probabilities of the values*/
    public float[] p;
    /**
     * The array of values
     */
    public float[] v;   //The values of the distribution
    /**
     * The cumulative (sum) of probabilities
     */
    float[] s;   //The cumulative distribution

    /**
     * Create a new DPD
     * @param d float []d is an array of probabilities
     * @param val float []val is the array of values the rv. assumes
     * We assume that the entries in p are non-negative and provide for renormalizing
     *them so their sum is 1. If they're all 0, we take them to all be equal to 1, 
     * making the outcomes of the rv equally likely
     */
    public DiscreteProbabilityDistribution(float[] d, float[] val) {
        p = d;
        v = val;
        normalize();
    }

    public DiscreteProbabilityDistribution(double[] d, double[] val) {
        p = new float[d.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = (float) d[i];
        }
        v = new float[val.length];
        for (int i = 0; i < p.length; i++) {
            v[i] = (float) val[i];
        }

        normalize();
    }

    /**
     * Simple creator for setting when we'll add points later
     */
    public DiscreteProbabilityDistribution() {
        p = null;
        v = null;
    }

    /**
     * Returns a printable String; primarily for debugging
     * @return A String version of the data contained in this class,
     * list of ordered pairs giving value and probability
     */
    public int number_points() {
        if (v == null) {
            return 0;
        }
        return v.length;
    }

    /**
     * Returns a string prepresentation of the DPD
     * @return String
     */
    public String toString() {
        if (v == null || v.length == 0) {
            return "No points in dpd.";
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        StringBuffer sb = new StringBuffer();
        sb.append("DiscreteProbabilityDistribution:" + p.length);
        for (int i = 0; i < p.length; i++) {
            sb.append(", (" + nf.format(v[i]) + "," + nf.format(p[i]) + ")");
        }
        sb.append("\n");
        return sb.toString();
    }
    

    /**
     * Generate an XML version of the DPD
     * @return A String that is an XML representation of the DPD.
     * TH\he SimData.dtd defines what this looks like
     */
    public String toXML() {
        if (v == null || v.length == 0) {
            return "No points in dpd.";
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < p.length; i++) {
            sb.append("<dpdpoint x=\"" + nf.format(p[i]) + "\" y=\"" + nf.format(v[i]) + "\"/>\n");
        }
        return sb.toString();
    }
    
      public String toXML(String name) {
        if (v == null || v.length == 0) {
            return "No points in dpd.";
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        StringBuffer sb = new StringBuffer("");
        MyFormat mf=new MyFormat();
        sb.append("<"+name+">\n");
        for (int i = 0; i < p.length; i++) {
            sb.append("<dpdpoint x=\"" + mf.format(p[i]) + "\" y=\"" + mf.format(v[i]) + "\"/>\n");
        }
        sb.append("</"+name+">\n");
        return sb.toString();
    }

    /**
     * addPoint lets us add probability/value pairs to a DPD one at a time.
     * When they've all bee added, call normalize() (we don't check for whether this has been done)
     * But don't call normalize() after each addPoint!
     * @param prob The probability with which the RV assumes the specified value
     * @param value The value assumed
     */
    public void addPoint(float prob, float value) {
        if (p == null || v == null) {
            p = new float[1];
            v = new float[1];
            p[0] = prob;
            v[0] = value;

        } else {
            float[] newd = new float[p.length + 1];
            float[] newv = new float[v.length + 1];
            for (int i = 0; i < p.length; i++) {
                newd[i] = p[i];
                newv[i] = v[i];
            }
            newd[p.length] = prob;
            newv[p.length] = value;
            p = newd;
            v = newv;
        }
    }

    /**
     * Normalize the probabilities in case they don't add up to 1.
     */
    public void normalize() {
        float sum = 0.0f;
        if (p == null || p.length <= 0) {
            return;
        }
        for (int i = 0; i < p.length; i++) {
            sum += p[i];
        }
        if (sum == 0.0f) {
            for (int j = 0; j < p.length; j++) {
                p[j] = 1.0f;
            }
            sum = p.length * 1.0f;
        }
        if (sum != 1.0f) {
            for (int i = 0; i < p.length; i++) {
                p[i] /= sum;
            }
        }
        s = new float[p.length];
        s[0] = p[0];
        for (int i = 1; i <= p.length - 1; i++) {
            s[i] = s[i - 1] + p[i];
        }
    }

    /**
     * Select a value at random from the DPD
     * @param r a uniform 0-1 rv used for the selection
     * @return The value selected
     */
    public float pickOne(double r) {
        if (Debug) {
            if (r < 0 || r > 1) {
                System.out.println("DPD.pickOne() called with a non 0-1 value.");
            }
        }
        for (int j = 0; j <= s.length - 1; j++) {
            if (r <= s[j]) {
                return (v[j]);
            }
        }
        return (v[p.length - 1]);
    }

    /**
     * Returns the expected value of the distribution
     * @return expected value
     */
    public float expected_value() {
        float e = 0.0f;
        for (int i = 0; i < p.length; i++) {
            e += p[i] * v[i];
        }
        return e;
    }

    /**
     * Returns the variance of the distribution
     * @return the variance
     */
    public float variance() {
        float e = 0.0f;
        for (int i = 0; i < p.length; i++) {
            e += p[i] * v[i];
        }
        float var = 0.0f;
        for (int i = 0; i < p.length; i++) {
            var += ((v[i] - e) * (v[i] - e) * p[i]);
        }
        return var;
    }

    /**
     * Just for testing purposes
     * @param args not used
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //pick up name for input and output files
        //System.out.println(args[0]);
        Random rnum;
//         double r;
        rnum = new Random();
        DiscreteProbabilityDistribution dpd = new DiscreteProbabilityDistribution();
        dpd.addPoint(.5f, 5);
        dpd.addPoint(0.5f, 10);
        dpd.addPoint(0.5f, 1);
        dpd.normalize();
        System.out.println(dpd.toString());
        System.out.println(dpd.expected_value() + "," + dpd.variance());
        for (int i = 0; i < 10; i++) {
            System.out.println(dpd.pickOne(rnum.nextDouble()));
        }
        System.out.append(dpd.toXML("myformat"));
    }
}
