/*
 * This standalone program is to read a dump file
 * and the SimData.xml that generated it and calculate F statistics
 * using some or all of the subregions specified in the SimData file.
 * 
 * You can run NewGarden to generate a dumpfile and NOT calculate
 * summary statistics for the run by including the option --skipstats
 * on the commandline. This would save some time and memory during the NewGarden 
 * run.
 * 
 * In running the Fst progam  you must mention on the command line the name of the SimData file
 * then the dumpfile
 * and then strings like "0,2,4,6" giving the subregions
 * to be used in an Fst calculation. The subregions are numbered
 * in the order they occur in the SimData.xml file and they appear
 * on the command line as comma separated strings.
 * 
 * So:
 * 
 * java -cp NewGarden.jar statistics.Fst SimData.xml output.txt 0,1
 * 
 * if you put quotes areound the subregion string you can have spaces in it
 * 
 * java -cp NewGarden.jar statistics.Fst SimData.xml output.txt  "0 , 2  , 4"
 * 
 * You can ask for multiple F calculations using different collections
 * of subregions, as in
 * 
 * java -cp NewGarden.jar statistics.Fst SimData.xml output.txt "0,1" "0,2,4"
 * 
 * Note, however, that to get numbers that have a chance of making sense
 * you must always ask for a calculation based on DISJOINT (non-overlapping)
 * subregions.
 *
 * 
 */
package statistics;

import biology.Region;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Arrays;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import parameters.SimData;
import parameters.SimDataReader;

/**
 *
 * @author sep
 */
public class Fst {

    NumberFormat nf;
    static SimData sd;
    static String dumpfilename;
    static String SimDataFileName;
    static String[] Calculations;
    static int number_subregions;
    static int[][][][] population_size;// year,run,cohort=0,1,subregion
    static int[][][][] seedbank_counts;
    static int[][][][][][] geneotypecounts;// year,run,cohort,subregion,locus,geneotype

    // geneotype is alleles (a,b) with each of a, b in [0,max_alleles-1]
    // but we can and do assume 0\le a \le b
    /**
     *
     * @param a
     * @param b
     * @return for indexing a triangle (plus diagonal) of an n\times n array
     * Actually, we don't enforce the col\le row condition and use this for
     * storing a symmetric n\times n matrix in a single array.
     *
     * in particular tri(a,b)=tri(b,a) Here $0\le row \le n-1$ and $0\le col \le
     * row$ 
     * 0 . . . 
     * 1 2 . . 
     * 3 4 5 . 
     * 6 7 8 9 etc. and the total number of entries
     * is 1 + 2 + \cdots + n = (n)(n+1)/2
     *
     * so the entry (row,col) gets stored at the flat array in location
     * tri(row,col)
     */
    
    /**
     *
     * @param a
     * @param b
     * @return the index storing m(a,b)=m(b,a) in a single array. here m(ij) is
     * a symmetric matrix and we want to store it in smaller space (plus benefit
     * from not needing to worry whether a\le b or not)
     */
    private static int tri(int a, int b) {
        if (b < a) {//b is col since col\le row
            return (a * (a + 1)) / 2 + b;
        } else {//a is the col
            return (b * (b + 1)) / 2 + a;
        }
    }
/**
     *
     * @param s a string of space and comma separated integers
     * @return an array of the integers.
     */
    private static int[] parseString(String s) {
        String[] vals = s.split("\\s*,\\s*");
        int[] ans = new int[vals.length];
        for (int i = 0; i < vals.length; i++) {
            ans[i] = Integer.parseInt(vals[i]);
        }
        return ans;

    }

    /**
     *
     * @param xmlfilename
     * @return the SimData obtained by parsing the xml file
     * 
     * This procedure is now available in SimData
     */
    /*
    private static SimData getSimData(String xmlfilename) {
        SimDataReader my_mb = null; // a model builder for parsing .xml
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            org.xml.sax.XMLReader parser = saxParser.getXMLReader();
            my_mb = new SimDataReader();
            parser.setContentHandler(my_mb);
            try {
                parser.parse(xmlfilename);
            } catch (Exception e) {
                System.out.println("Parsing error in SimData(xmlfilename) "
                        + e.toString());
            }
        } catch (Exception e) {
            System.out.println("Problem getting a parser in SimDataReader()\n"+e.toString());
        }
        sd = my_mb.getSD();
        sd.MakeFinal();
        return sd;
    }
    */

    public Fst(String[] args) {
        super();
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        SimDataFileName = args[0];
        dumpfilename = args[1];
        //
        //sd = getSimData(SimDataFileName);
        //Now SimData knows how to read a SimData file.
        sd=SimData.readXMLFile(SimDataFileName);
        number_subregions = sd.summaryregions.Regions.size();
        // number_observations=1+sd.number_generations/sd.datainterval;
        allocateMemory();
        readData();
        if (args.length > 2) {
            Calculations = new String[args.length - 2];
            for (int i = 0; i < Calculations.length; i++) {
                Calculations[i] = args[i + 2];
            }
        } else//use all the subregions
        {
            StringBuffer sb = new StringBuffer("0");
            for (int i = 1; i < number_subregions; i++) {
                sb.append("," + i);
            }
            Calculations = new String[1];
            Calculations[0] = sb.toString();

        }
    }

    private static void allocateMemory() {
        population_size = new int[sd.number_generations][sd.number_runs][2][number_subregions];
        seedbank_counts = new int[sd.number_generations][sd.number_runs][2][number_subregions];
        geneotypecounts = new int[sd.number_generations][sd.number_runs][2][number_subregions][sd.number_loci][(sd.max_alleles * (sd.max_alleles + 1)) / 2];
    }

    private void readData() {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(Fst.dumpfilename);
            InputStreamReader isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String line;
        try {
            ourLoop:
            while ((line = br.readLine()) != null) {
                String[] vals = line.split("\\s*,\\s*");
                int ourRun = Integer.parseInt(vals[0]);
                int ourDOB = Integer.parseInt(vals[4]);
                int ourDOD = Integer.parseInt(vals[5]);
                int ourX = Integer.parseInt(vals[9]);
                int ourY = Integer.parseInt(vals[10]);
                // float ourAge=ourYear-ourDOB;
                int yearLow = Math.max(0, ourDOB);
                // negative ages for founders still get assigned to year 0
                // ie founder cohort
                int yearHigh = ourDOD;
                if (ourDOD == -1) {// alive at end of simulation
                    yearHigh = sd.number_generations - 1;
                }
                // find years that was alive
                // find subregion was in
                int subregionnumber = -1;
                for (int sr = 0; sr < number_subregions; sr++) {
                    Region region = (Region) sd.summaryregions.Regions.get(sr);
                    if (region.inRegion(ourX, ourY)) {
                        subregionnumber = sr;
                        break;
                    }
                }
                if (subregionnumber == -1)// not of interest to us if not in a summary region
                {
                    continue ourLoop;
                }

                // DEBUG: print the line and our deductions from it
                //System.out.println(line);
                //System.out.println("" + yearLow + "," + yearHigh + ","
                //		+ subregionnumber);

                // alleles for loci are at 11,12; 13,14,; etc
                boolean seedbank = Boolean.parseBoolean(vals[8]);

                population_size[yearLow][ourRun][1][subregionnumber]++;
                if (seedbank) {
                    seedbank_counts[yearLow][ourRun][1][subregionnumber]++;
                }
                for (int year = Math.min(yearLow, yearHigh); year <= yearHigh; year++) {
                    population_size[year][ourRun][0][subregionnumber]++;
                    if (seedbank) {
                        seedbank_counts[year][ourRun][0][subregionnumber]++;
                    }
                }
                for (int locus = 0; locus < sd.number_loci; locus++) {
                    int allele1 = Integer.parseInt(vals[11 + 2 * locus]);
                    int allele2 = Integer.parseInt(vals[11 + 2 * locus + 1]);

                    int ourTempIndex = tri(allele1, allele2);
                    geneotypecounts[yearLow][ourRun][1][subregionnumber][locus][ourTempIndex]++;
                    for (int year = Math.min(yearLow, yearHigh); year <= yearHigh; year++) {

                        geneotypecounts[year][ourRun][0][subregionnumber][locus][ourTempIndex]++;
                    }
                }
                //seedbank tallies?? slot 8 is seedbank boolean
                // loop on years
                // loop on loci
                // loop on cohort
                // store the data

            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //System.out.println("Read the data");
    }
/*
    double[] doAOV(int allele, int locus, int run, int year, int cohort, int[] srs) {
        double n = 0.0; // total population in srs (subregions)
        double[] ni = new double[srs.length]; // size of ith population
        double nbar = 0.0;
        for (int i = 0; i < srs.length; i++) {
            ni[i] = population_size[year][run][cohort][srs[i]];
            n += ni[i];
            nbar += ni[i];

        }
        nbar /= srs.length;

    }
*/
    /**
     *
     * @param allele
     * @param locus
     * @param run
     * @param year
     * @param cohort
     * @return double [] S is an array containing S1,S2,S3
     *
     * For each locus and each allele at the locus we compute Weir's S1, S2, and
     * S3
     *
     */
    double[] doAllele(int allele, int locus, int run, int year, int cohort, int[] srs) {
        double n = 0.0; // total population in srs (subregions)
        double[] ni = new double[srs.length]; // size of ith population
        double ns = 0.0;// \sum n_i^2
        double nc;
        double r = srs.length;
        double[] p = new double[srs.length];// p_{Ai} freq of allele in population i
        double pAdot = 0.0;//average frequency in combined population;
        double sA2 = 0.0;//\sum n_i(p_i-pdot)^2(\frac{1}{(r-1)\bar{n}}
        double nbar;//\sum n_i/r == average size of subpopulations
        double Ha = 0.0;//2\sum n_i(p_i-pAA_i)/n
        for (int i = 0; i < srs.length; i++) {
            ni[i] = population_size[year][run][cohort][srs[i]];
            n += ni[i];
            ns += ni[i] * ni[i];
            for (int all2 = 0; all2 < sd.max_alleles; all2++) {
                p[i] += geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, all2)];
            }
            p[i] += geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, allele)];
            pAdot += p[i];
            if (ni[i] > 0) {
                p[i] /= (2.0 * ni[i]);
            }

        }
        // for (int i = 0; i < srs.length; i++) {
        //     pAdot += p[i];
        //     if (ni[i] > 0) {
        //      p[i] /= (2.0 * ni[i]);
        //    }

        //}
        pAdot /= (2.0 * n);
        nbar = n / r;
        nc = (n - ns / n) / (r - 1);
        for (int i = 0; i < srs.length; i++) {
            double t = (p[i] - pAdot);
            sA2 += ni[i] * t * t;
        }
        sA2 /= ((r - 1) * nbar);
        //Weir says Ha frequency over all samples of heterozygous individuals
        //that have the allele a 
        //not how I see this?
        //it looks the number with allele a take away the homozyous aa
        //so it is the fraction of those with an a allele that are heterozygotes

        for (int i = 0; i < srs.length; i++) {
            Ha += 2 * ni[i] * (p[i] - geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, allele)] / ni[i]);

        }
        Ha *= (1.0 / n);
//Let's just try counting heterozygous individuals and the ones of them that have the allele a


        double hh = pAdot * (1 - pAdot);
        double[] S = new double[3];
        S[0] = sA2 - (hh - sA2 * ((r - 1) / r) - Ha / 4) / (nbar - 1);
        S[2] = Ha * nc / nbar;
        S[1] = hh - (hh * r * (nbar - nc) / nbar - Ha * (nbar - nc) / (4 * nc * nc) - (sA2 / nbar) * (nbar - 1 + (r - 1) * (nbar - nc))) * nbar / (r * (nbar - 1));
        //S[1] = hh - (nbar / (r * (nbar - 1))) * ((r * (nbar - nc) / nbar) * hh - (sA2 / nbar) * ((nbar - 1 + (r - 1) * (nbar - nc))) - Ha * (nbar - nc) / (4 * nc * nc));

        double a1 = 1.0 - Ha / hh;
        double a2 = sA2 / hh;
        System.err.println("Fit= " + a1 + ",Fst= " + a2);
        //looks like Fit and Ha are the problem!
        return S;
    }

    /**
     *
     * @param run
     * @param year
     * @param cohort
     * @return an array with F estimates per Weir's pg 155
     */
    public double[] sumSs(int run, int year, int cohort, int[] srs) {
        assert (run < sd.number_runs && year < sd.number_generations);
        // System.out.println("run=" + run + " year=" + year + " cohort=" + cohort);

        double[] sums = new double[3];
        for (int locus = 0; locus < sd.number_loci; locus++) {
            for (int allele = 0; allele < sd.max_alleles; allele++) {
                // System.out.println("locus= " + locus + " allele=" + allele);
                double[] temp = doAllele(allele, locus, run, year, cohort, srs);
                sums[0] += temp[0];
                sums[1] += temp[1];
                sums[2] += temp[2];
                //double t1,t2,t3;
                //t1=1.0-temp[2]/temp[1];
                //t2=temp[0]/temp[1];
                //t3=(temp[0]-temp[1])/(1.0-temp[1]);
                //System.out.println(" "+t1+","+t2+","+t3+"\n");

            }
        }
        double[] ans = new double[3];
        ans[0] = 1.0 - sums[2] / sums[1];//$F_{it}$
        ans[1] = sums[0] / sums[1];//$F_{st}$
        //
        //so F_{is}=(F_{it}-F_{st})/(1-F_{st})

        //We were using
        ans[2] = (ans[0] - ans[1]) / (1.0 - ans[1]);
        //(1-Fis)(1-Fst)=(1-Fit)
        //so Fis =1- (1-Fit)/(1-Fst)
        //ans[2]=1.0-(1-ans[0])/(1-ans[1]);

        return ans;
    }

    /**
     *
     * @param run
     * @param year
     * @param cohort
     * @param srs
     * @return calculates the F statistics for the specified run, year, cohort
     * and subregions The calculation is reported for each locus
     *
     * H_I is the heterozygosity of an individual in a subregion. We calculate
     * it by looking at all the individuals in a subregion and counting the
     * fraction of that total that are heterozygous. Then average these results
     * over the subregions.
     *
     * H_S is the expected heterozygosity assuming random mating in the
     * subpopulation computed according to HW using the allele frequencies
     * observed in the subpop
     *
     * H_T is the same as H_S but computed using allele frequencies in the
     * entire population
     *
     * Then F_{IS} = \frac{H_S-H_I}{H_S}
     *
     * F_{ST}= \frac{H_T-H_S}{H_T}
     *
     * F_{IT}=\frac{H_T-H_I}{H_T}
     *
     *
     */
    double[] HartlFs(int run, int year, int cohort, int[] srs) {
        double[] ans = new double[3 * sd.number_loci];
        //loop on loci
        for (int locus = 0; locus < sd.number_loci; locus++) {
            //For each of the subregions and the union of the subregions
            //get frequency of the alleles
            double[] hss = new double[srs.length];
            for (int sr = 0; sr < srs.length; sr++) {
                int[] subregion = new int[1];
                subregion[0] = sr;
                double[] fs = countAlleles(cohort, year, locus, run, subregion);
                hss[sr] = 1.0;
                for (int a = 0; a < sd.max_alleles; a++) {
                    hss[sr] -= (fs[a] * fs[a]);
                }

            }
            double Hs = (DoStats(hss))[0];
            //expected H in union of subregions
            double[] fs = countAlleles(cohort, year, locus, run, srs);
            double Ht = 1.0;
            for (int a = 0; a < sd.max_alleles; a++) {
                Ht -= (fs[a] * fs[a]);
            }

            //get the observed heterozygosity 
            double[] hos = new double[srs.length];
            for (int sr = 0; sr < srs.length; sr++) {
                int[] subregion = new int[1];
                subregion[0] = sr;
                hos[sr] = countHeterozygotes(cohort, year, locus, run, subregion);

            }
            double Hi = (DoStats(hos)[0]);
            ans[3 * locus] = 1.0 - Hi / Hs;
            ans[3 * locus + 1] = 1.0 - Hs / Ht;
            ans[3 * locus + 2] = 1.0 - Hi / Ht;

        }

        return ans;
    }

    /**
     *
     * @param cohort
     * @param year
     * @param subreg
     * @param locus
     * @param run
     * @param srs
     * @param frequencies
     * @return a double [] giving the frequencies of the alleles in the
     * indicated subregion
     */
    double[] countAlleles(int cohort, int year, int locus, int run, int[] srs) {
        double[] allelefreqs = new double[sd.max_alleles];
        for (int i = 0; i < srs.length; i++) {
            for (int allele = 0; allele < sd.max_alleles; allele++) {
                for (int other = 0; other < sd.max_alleles; other++) {
                    if (other == allele) {//homozygote
                        allelefreqs[allele] += 2 * geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, other)];
                    } else {//heterozygote

                        allelefreqs[allele] += geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, other)];
                    }
                }
            }
        }
        //now normalize
        double pop = 0.0;
        for (int i = 0; i < srs.length; i++) {
            pop += population_size[year][run][cohort][srs[i]];
        }
        if (pop > 0) {
            for (int allele = 0; allele < sd.max_alleles; allele++) {


                allelefreqs[allele] =
                        allelefreqs[allele] / (2.0 * pop);
            }

        }


        return allelefreqs;
    }

    /**
     *
     * @param cohort
     * @param year
     * @param locus
     * @param run
     * @param srs
     * @param frequencies
     * @return calculates the fraction of the population in the indicated
     * subregions that are heterozygous at the given locus.
     */
    double countHeterozygotes(int cohort, int year, int locus, int run, int[] srs) {
        double het = 0.0;
        double pop = 0.0;
        for (int i = 0; i < srs.length; i++) {
            for (int allele = 0; allele < sd.max_alleles; allele++) {

                het += geneotypecounts[year][run][cohort][srs[i]][locus][tri(allele, allele)];


            }
            pop += population_size[year][run][cohort][srs[i]];
        }
        if (pop > 0) {
            het = (pop - het) / pop;
        }
        return het;

    }

    /**
     *
     * @param x array to calculate mean and sd of;
     * @return an array with mean and standard deviation
     */
    public double[] DoStats(double[] x) {
        double s = 0, ss = 0;
        double[] results = new double[2];
        //if we have no or smal samples
        if (x == null || x.length == 0) {
            results[0] = results[1] = 0.0;
        }
        if (x.length == 1) {
            results[0] = x[0];
            results[1] = 0.0;
        }
        //otherwise
        for (int i = 0; i < x.length; i++) {
            s += x[i];
            ss += (x[i] * x[i]);
        }
        results[0] = s / x.length;
        results[1] = Math.sqrt((ss - x.length * results[0] * results[0]) / (x.length - 1));
        return results;
    }
/**
 * 
 * @param calc specified which subregions to use in calculating F stats
 * 
 * This method will, for each cohort=0/1, year
 * find the average of the F stats for the specified collection of summary regions
 * over all the loci and 
 * and report the average and sd of these F's
 * cohort,year,Fis,sd(Fis),Fst,sd(Fst),Fit,sd(Fit)
 * 
 */
    public String doHartl(int[] calc) {
        String labels1="F stats for subregions"+Arrays.toString(calc)+"\n";
        String labels2="cohort,year,Fis,sd(Fis),Fst,sd(Fst),Fit,sd(Fit)\n";
        StringBuilder sb=new StringBuilder(labels1+labels2);
        for(int cohort=0;cohort<2;cohort++)
        {
            for(int year=0;year<sd.number_generations;year++)
            {
                double []fis=new double[sd.number_runs];
                double []fst=new double[sd.number_runs];
                double []fit=new double[sd.number_runs];
                for(int run=0;run<sd.number_runs;run++)
                {
                    //HartlFs
                    double [] temp=HartlFs(run,year,cohort,calc);
                    for(int locus =0;locus<sd.number_loci;locus++)
                    {
                        fis[run]+=temp[3*locus];
                        fst[run]+=temp[3*locus+1];
                        fit[run]+=temp[3*locus+2];
                    }
                    fis[run]/= sd.number_loci;
                    fst[run]/= sd.number_loci;
                    fit[run]/= sd.number_loci;
                    //average over loci
                }
                sb.append(""+cohort+","+year+",");
                double [] ans=DoStats(fis);
                sb.append(nf.format(ans[0])+","+nf.format(ans[1])+",");
                ans=DoStats(fst);
                sb.append(nf.format(ans[0])+","+nf.format(ans[1])+",");
                ans=DoStats(fit);
                sb.append(nf.format(ans[0])+","+nf.format(ans[1])+"\n");
                
            }
            
        }
       return sb.toString();

    }

    public static void main(String[] args) {
        Fst fst = new Fst(args);
        for (int calc = 0; calc < Calculations.length; calc++) {
            int[] srs = parseString(Calculations[calc]);
            System.out.println(fst.doHartl(srs));
//            int[] srs = parseString(Calculations[calc]);
//            fst.doHartl(srs);
//            System.out.println("F stats calculated with subregions " + Calculations[calc]);
//            System.out.println("cohort,year,Fit,sd,Fst,sd,Fis,sd");
//            for (int cohort = 0; cohort <= 1; cohort++) {
//                for (int year = 0; year < sd.number_generations; year++) {
//                    double[] stats = new double[3];
//                    double[] FIT, FST, FIS;
//                    FIT = new double[sd.number_runs];
//                    FST = new double[sd.number_runs];
//                    FIS = new double[sd.number_runs];
//                    for (int run = 0; run < sd.number_runs; run++) {
//                        double[] test = fst.sumSs(run, year, cohort, srs);
//                        FIT[run] = test[0];
//                        FST[run] = test[1];
//                        FIS[run] = test[2];
//                    }//run
//                    System.out.print("" + cohort + "," + year + ",");
//                    double[] ans = fst.DoStats(FIT);
//                    System.out.print("" + fst.nf.format(ans[0]) + "," + fst.nf.format(ans[1]) + ",");
//                    ans = fst.DoStats(FST);
//                    System.out.print("" + fst.nf.format(ans[0]) + "," + fst.nf.format(ans[1]) + ",");
//                    ans = fst.DoStats(FIS);
//                    System.out.print("" + fst.nf.format(ans[0]) + "," + fst.nf.format(ans[1]) + "\n");
//                }//year
//            }//cohort
        }//calculation
    }
}
