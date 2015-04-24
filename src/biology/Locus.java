package biology;

/*
 * Locus.java
 *
 * Created on February 28, 2005, 6:42 PM
31Jan 2014 Introduced the variable "action"
 */

/**
 * represents and stores all the data we need about a locus
 * @author sep
 */
import functions.DiscreteProbabilityDistribution;
import functions.DPD;
import java.util.*;

public class Locus {

    public DiscreteProbabilityDistribution dpd; //probs of the alleles
    DPD genotypeDPD; //probs of genotypes if F >0
    float F = 0.0f;
    private static final boolean Debug = false;
    private String action=null;
    /** Creates a new instance of Locus */
    public Locus() {
        dpd = new DiscreteProbabilityDistribution();
        genotypeDPD = null;
    }

 /**
  * 
  * @return $\sum p_i^2$ where the sum is over all allele
  * frequencies
  * 
  * 22 June 2014 to be used in modeling inbreeding depression
  * and heterozygote advantage.
  * 
  * 
  * 
  * Later we can ensure that this info is only used if the action of
  * the locus is specified as "inbreeding" or "fitness" or something
  * like that
  * 
  *
  */   
public float expectedHomozygosity()   
{
    float ans= 0.0f;
    if(F==0.0f)
    {
    for(float x:dpd.p){ans += x*x;}
    
    }
    else
    {
        assert F>0.0f : "Problem in expectedHomozygosity() F ="+F;
        for(int gt=0;gt<genotypeDPD.number_points();gt++)
        {
            ArrayList al=(ArrayList) genotypeDPD.values.get(gt);
            int [] genotype=new int[2];
            for(int i=0;i<2;i++)genotype[i]=(Integer) al.get(i);
            if(genotype[0]==genotype[1]){
                float temp= genotypeDPD.ps.get(gt);
                ans += temp;}
        }
    }
    return ans;
}


public void setAction(String a){action=a;}
public String getAction(){return action;}

    /**
     * 
     * @param ibc the value of F "inbreeding coefficient" for this locus
     *   to be used in generating random plants
     */
    public void setF(float ibc) {
        F = ibc;
        if (F > 0.0f) {
            genotypeDPD = new DPD();
            //we store the genotypes as Lists of alleles
            for (int a = 0; a < dpd.number_points(); a++) {
                for (int b = a; b < dpd.number_points(); b++) {
                    ArrayList genotype = new ArrayList();
                    genotype.add(new Integer(a));
                    genotype.add(new Integer(b));
                    float p, pa, pb;
                    pa = dpd.p[a];
                    pb = dpd.p[b];
                    if (a == b) {
                        p = (1 - F) * pa * pa + F * pa;
                    } else {
                        p = 2 * (1 - F) * pa * pb;
                    }
                    genotypeDPD.addPoint(p, genotype);
                }

            }
            genotypeDPD.normalize();
        } else {
            genotypeDPD = null;
        }
    }

    public Locus(float ibc) {
        dpd = new DiscreteProbabilityDistribution();
        F = ibc;
        if (F > 0.0f) {
            genotypeDPD = new DPD();
            //we store the genotypes as Lists of alleles
            for (int a = 0; a < dpd.number_points(); a++) {
                for (int b = a; b < dpd.number_points(); b++) {
                    ArrayList genotype = new ArrayList();
                    genotype.add(new Integer(a));
                    genotype.add(new Integer(b));
                    float p, pa, pb;
                    pa = dpd.p[a];
                    pb = dpd.p[b];
                    if (a == b) {
                        p = (1 - F) * pa * pa + F * pa;
                    } else {
                        p = 2 * (1 - F) * pa * pb;
                    }
                    genotypeDPD.addPoint(p, genotype);
                }
                genotypeDPD.normalize();
            }
        } else {
            genotypeDPD = null;
        }
    }

    /**
     * the number of alleles at this locus
     * @return number of alleles at this locus
     */
    public int number_alleles() {
        return dpd.number_points();
    }

    /**
     *       pickAllele(double r) uses the uniform 0-1 random number r
     *       to select one of the alleles from this locus according to the
     *       frequency distribution []p.
     * @param r a random number to be used in selecting one of the alleles at this locus
     * @return the allele selected
     */
    public int pickAllele(double r) {
        return (int) Math.round((double) dpd.pickOne(r));
    }

    /**
     * 
     * @param rnum
     * @return a list of the indices for the two alleles at this locus
     */
    public int[] pickGenotype(Random rnum) {
        int[] ans = new int[2];
        if (F == 0.0f) {
            // if(Debug) System.out.println("You probably shouldn't be calling this function with F=0");
            for (int i = 0; i < 2; i++) {
                ans[i] = (int) Math.round((double) dpd.pickOne(rnum.nextDouble()));
            }
        } else {
            ArrayList gt = (ArrayList) genotypeDPD.pickOne(rnum.nextDouble());

            for (int i = 0; i < 2; i++) {
                ans[i] = ((Integer) gt.get(i)).intValue();
            }

        }
        return ans;
    }

    /**
     * @return a string giving XML markup for this locus
     * 31jan2014 included printing of action attribute if non-null
     */
    public String toXML() {
        StringBuilder sb = new StringBuilder("");
        if(action==null)
        {
        sb.append("<locus>\n");
        }
        else
        {
            sb.append("<locus action=\""+action+"\">\n");
        }
        sb.append(dpd.toXML());
        sb.append("</locus>\n");
        return sb.toString();
    }
    public static void main(String[] args)
    {
        Locus l=new Locus();
        l.dpd.addPoint(0.25f, 0);
        l.dpd.addPoint(0.25f, 1);
        l.dpd.addPoint(0.25f, 2);
        l.dpd.addPoint(0.25f, 3);
        System.out.println(l.toXML());
        System.out.println("ExpectedHomozygosity = "+l.expectedHomozygosity());
       l.setF(1.0f);
       System.out.println("ExpectedHomozygosity = "+l.expectedHomozygosity());
    }
}
