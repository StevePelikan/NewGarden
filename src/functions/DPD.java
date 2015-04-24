package functions;

import java.util.*;

/**
 * DPD is a Discrete Probability Distribution that picks Objects at
 * random rather than integers.
 * DPD.java
 *
 * Created on April 8, 2006, 10:41 AM
 * @author sep
 * 
 */
public class DPD{

    /**
     * Turns on/off checks and messages that are potentially useful
     * for debug process
     */
    public static final boolean Debug = false;
   
    /**The probabilities*/
    public ArrayList<Float> ps;

    /**The array of values (Objects)*/
    public ArrayList values;   
    
    /**The cumulative (sum) of probabilities   */
    float[] s;

    /** Creates a new instance of DPD */
    public DPD() {
        ps = new ArrayList<Float>();
        values = new ArrayList();
    }

    /**
     * A simple constructor for when the probabilities and
     * Objects are all available at one time
     * @param d the probabilities
     * @param val the objects
     */
    public DPD(float[] d, ArrayList val) {
        assert d.length == val.size();
        if (Debug) {
            if (d.length != val.size()) {
                System.out.println("Problem in DPD(float [] d, ArrayList val)\n Size mismatch");
            }
        }
        for (int i = 0; i < d.length; i++) {
            ps.add(d[i]);
        }
        values = val;
        normalize();
    }

    /**
     * For building the DPD incrementally, as when parsing
     * an XML file.
     *
     * Probably need to call normalize() when you're done adding points
     * @param prob the probability
     * @param value the object
     */
    public void addPoint(float prob, Object value) {

        ps.add(prob);
        values.add(value);

    }

    /**
     * Select a value at random from the DPD
     * @param r a uniform 0-1 rv used for the selection
     * @return The value selected
     */
    public Object pickOne(double r) {
        if (Debug) {
            if (r < 0 || r > 1) {
                System.out.println("DPD.pickOne() called with a non 0-1 value.");
            }
            assert ps.size() == values.size();
        }
        for (int j = 0; j <= s.length - 1; j++) {
            if (r <= s[j]) {
                return (values.get(j));
            }
        }
        return (values.get(values.size() - 1));
    }

    /**
     * The number of objects
     * @return the number objects
     */
    public int number_points() {
        return values.size();
    }
    /**
     * Normalize the probabilities in case they don't add up to 1.
     */

    public void normalize() {
        float sum = 0.0f;
        assert ps.size() == values.size() : "DPD normalize bad data sizes" + ps.size() + " " + values.size();
        for (int i = 0; i < ps.size(); i++) {

            sum += (float) ps.get(i);
        }
        assert sum > 0.0f : "Sum of probabilities is not positive in normalize()";
        if (sum != 1.0f) {
            for (int i = 0; i < ps.size(); i++) {
                ps.set(i, ps.get(i) / sum);
            }
        }
        s = new float[ps.size()];
        s[0] = ps.get(0);
        for (int i = 1; i <= ps.size() - 1; i++) {
            s[i] = s[i - 1] + ps.get(i);
        }
    }

    /**
     * for testing
     * @param args not used
     * When run you should see the numbers 1,5, and 10
     * but 80% of them should be 5
     */
    public static void main(String[] args) {

        Random rnum;
        double r;
        rnum = new Random();
        DPD dpd = new DPD();
        dpd.addPoint(.5f, Integer.valueOf(5));
        dpd.addPoint(0.1f, Integer.valueOf(10));
        dpd.addPoint(0.1f, Integer.valueOf(1));
        dpd.normalize();

        System.out.println("Size of dpd is " + dpd.number_points());
        for (int i = 0; i < 10; i++) {
            System.out.println(((Integer) dpd.pickOne(rnum.nextDouble())).intValue());
        }
    }
}
