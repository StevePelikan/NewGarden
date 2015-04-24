/*
 * This standalone program is to read a dump file
 * and the SimData.xml file that generated the dump
 * and calculate all the usual summary statistics that the Newgarden
 * program produces. 
 * 
 * 6 March 2012 doPopulation now seems to work correctly. as does doHo and doHe.
 * 
 * 
 * As it stands 6 March 2012 the doOldReport() method will generated all 
 * the usual summary statistics for a  run broken down for each summary region
 * and also for the total population.
 * 
 * The results for subregions are reported in the order they're defined
 * in the SimData file with the total population reported as the last 
 * (highest numbered) subregion
 * 
 * 
 * Use this program like:
 * 
 * java -jar NewGarden statistics.DataReader SimData.xml output.txt
 *
 * 
 */
package statistics;

import biology.Region;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import parameters.SimData;
import parameters.SimDataReader;

/**
 *
 * @author sep
 */
public class DataReader {

    static String ProgramName="DataReader v1.12 (19 Dec 2014)";
    static String Intructions="use: >java -cp NG.jar statistics.DataReader <parameterfilename> <dumpfilename> locus_to_start loci_to_do\nSet reporting interval to 1 in simdata if using this program\n";
    static String Info="This program reads a NewGarden v2.77 SimData.xml and dumpfile and\nprints the summary statistics for it\n";
    
    NumberFormat nf;
    static SimData sd;
    int Loci_to_do=1;
    int Locus_to_start=1;
    static String dumpfilename;
    static String SimDataFileName;
    static int number_subregions;
    static int[][][][] population_size;// year,run,cohort=0,1,subregion
    static int [][][][] seedbank_counts;
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
     * row$ 0 . . . 1 2 . . 3 4 5 . 6 7 8 9 etc. and the total number of entries
     * is 1 + 2 + \cdots + n = (n)(n+1)/2
     *
     * so the entry (row,col) gets stored at the flat array in location
     * tri(row,col)
     */
    private static int tri(int a, int b) {
        if (b < a) {//b is col since col\le row
            return (a * (a + 1)) / 2 + b;
        } else {//a is the col
            return (b * (b + 1)) / 2 + a;
        }
    }

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
            System.out.println("Problem getting a parser in SimDataReader()");
        }
        sd = my_mb.getSD();
        sd.MakeFinal();
        return sd;
    }

    public DataReader(String[] args) {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        SimDataFileName = args[0];
        dumpfilename = args[1];
        Locus_to_start=Integer.parseInt(args[2]);
        Loci_to_do=Integer.parseInt(args[3]);
        
        File f=new File(SimDataFileName);
        if( ! f.exists())
        {
            System.out.println("Can't find the papameter file: "+SimDataFileName);
            System.exit(0);
        }
        f=new File(dumpfilename);
        if( ! f.exists())
        {
            System.out.println("Can't find the dump file: "+dumpfilename);
            System.exit(0);
        }
        //sd = getSimData(SimDataFileName);
        sd=SimData.readXMLFile(SimDataFileName);
        number_subregions = sd.summaryregions.Regions.size();
        
        //assert sd.number_loci>Locus_to_start+Loci_to_do: "Too many loci asked for";
        
        allocateMemory();
        readData();
    }

    private static void allocateMemory() {
        population_size = new int[sd.number_generations][sd.number_runs][2][number_subregions+1];
        seedbank_counts=new int[sd.number_generations][sd.number_runs][2][number_subregions+1];
        geneotypecounts = new int[sd.number_generations][sd.number_runs][2][number_subregions+1][sd.number_loci][(sd.max_alleles * (sd.max_alleles + 1)) / 2];
    }

    private void readData() {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(dumpfilename);
            InputStreamReader isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String line;
        try {
     
            while ((line = br.readLine()) != null) {
                String[] vals = line.split("\\s*,\\s*");
                int ourRun = Integer.parseInt(vals[0]);
                int ourDOB = Integer.parseInt(vals[5]);//4
                int ourDOD = Integer.parseInt(vals[6]);//5
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
                    //continue ourLoop;
                }

                // alleles for loci are at 11,12; 13,14,; etc
                boolean seedbank=Boolean.parseBoolean(vals[8]);
                population_size[yearLow][ourRun][1][number_subregions]++;
                if(subregionnumber!= -1) population_size[yearLow][ourRun][1][subregionnumber]++;
                
                if(seedbank) 
                {
                    if(subregionnumber!= -1) seedbank_counts[yearLow][ourRun][1][subregionnumber]++;
                    seedbank_counts[yearLow][ourRun][1][number_subregions]++;
                }
                for (int year = Math.min(yearLow, yearHigh); year <= yearHigh; year++) {
                    if(subregionnumber!= -1)population_size[year][ourRun][0][subregionnumber]++;
                    population_size[year][ourRun][0][number_subregions]++;
                     if(seedbank)
                     {
                         if(subregionnumber!= -1)seedbank_counts[year][ourRun][0][subregionnumber]++;
                         seedbank_counts[year][ourRun][0][number_subregions]++;
                     }
                }
                for (int locus = 0; locus < sd.number_loci; locus++) {
                    int allele1 = Integer.parseInt(vals[11 + 2 * locus]);
                    int allele2 = Integer.parseInt(vals[11 + 2 * locus + 1]);

                    int ourTempIndex = tri(allele1, allele2);
                    if(subregionnumber!= -1)geneotypecounts[yearLow][ourRun][1][subregionnumber][locus][ourTempIndex]++;
                    geneotypecounts[yearLow][ourRun][1][number_subregions][locus][ourTempIndex]++;
                    for (int year = Math.min(yearLow, yearHigh); year <= yearHigh; year++) {

                        if(subregionnumber!= -1)geneotypecounts[year][ourRun][0][subregionnumber][locus][ourTempIndex]++;
                        geneotypecounts[year][ourRun][0][number_subregions][locus][ourTempIndex]++;
                    }
                }
                

            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    public String showData() {
        StringBuilder sb = new StringBuilder("run,year,n1,n2,total\n");
        for (int run = 0; run < sd.number_runs; run++) {
            for (int year = 0; year < sd.number_generations; year++) {
                sb.append("").append(run).append(",").append(year).append(",");
                double s = 0;
                for (int sr = 0; sr < number_subregions; sr++) {
                    sb.append("").append(population_size[year][run][0][sr]).append(",");
                    s += population_size[year][run][0][sr];
                }
                sb.append("").append(s).append("\n");

            }
        }
        //now lets report genotype counts for first locus
        //first just show the genotypes:
        for (int all1 = 0; all1 < sd.max_alleles; all1++) {
            for (int all2 = 0; all2 < sd.max_alleles; all2++) {
                // year=0,run,cohort=0,subregion,locus,geneotype
                sb.append("" + all1 + "," + all2 + ",tri=" + tri(all1, all2) + "," + geneotypecounts[0][0][0][0][0][tri(all1, all2)]);

                sb.append("\n");
            }
        }
        return sb.toString();
    }
    double [] doSeedbank(int cohort, int year, int subreg)
    {
        double []x=new double[sd.number_runs];
        if(subreg>=0 && subreg<=number_subregions)
        {
            for(int run=0;run<sd.number_runs;run++)
            {
                if(population_size[year][run][cohort][subreg]>0 )
                {
                    x[run]=(1.0*seedbank_counts[year][run][cohort][subreg])
                    /population_size[year][run][cohort][subreg];
                }
            }
        }
        else//UNION of summaryregions
        {
           //problem!
        }
        
        return DoStats(x);
    }
    double[] doHo(int cohort, int year, int subreg) {
        double[] x = new double[sd.number_runs];
        for (int r = 0; r < sd.number_runs; r++) {
            x[r] = observedHeterozygosity(cohort, year, subreg, r);
        }
        return DoStats(x);
    }

    double[] doHe(int cohort, int year, int subreg) {
        double[] x = new double[sd.number_runs];
        for (int run = 0; run < sd.number_runs; run++) {
            double locusscore = 0.0;
            for (int locus = 0; locus < sd.number_loci; locus++) {
                double[] allelefrequencies = countAlleles(cohort, year, subreg, locus, run, true);
                double ss = 1.0;
                for (int a = 0; a < sd.max_alleles; a++) {
                    ss -= allelefrequencies[a] * allelefrequencies[a];
                }
                locusscore += ss;

            }
            x[run] = locusscore / sd.number_loci;
        }



        return DoStats(x);
    }

    double observedHeterozygosity(int cohort, int year, int subreg, int run) {
        double answer = 0.0;
        if (subreg >= 0 && subreg <= number_subregions) {
            if (population_size[year][run][cohort][subreg] > 0)//year,run,cohort=0,1,subregion
            {
                for (int locus = 0; locus < sd.number_loci; locus++) {
                    for (int allele1 = 1; allele1 < sd.max_alleles; allele1++) {
                        for (int allele2 = 0; allele2 < allele1; allele2++) {
                            answer += geneotypecounts[year][run][cohort][subreg][locus][tri(allele1, allele2)];
                        }
                    }
                }
                answer /= sd.number_loci;
                answer /= population_size[year][run][cohort][subreg];

            }
        } else//UNION of subregions
        {
            //problem!
        }
        return answer;
    }

    /**
     *
     * @param cohort
     * @param year
     * @param subreg
     * @return
     *
     * We calculate F one locus at a time and then report the average over all
     * the loci.
     *
     * f= (h-H- (1/n)H)/(h+(1/n)H)
     */
    double[] doF(int cohort, int year, int subreg) {
        double[] x = new double[sd.number_runs];


        if (subreg >= 0 && subreg <= number_subregions) {
            for (int run = 0; run < sd.number_runs; run++) {
                double Floc = 0.0;
                for (int locus = 0; locus < sd.number_loci; locus++) {
                    //observed heterozygous at this locus
                    double Ho = 0.0;
                    //year,run,cohort=0,1,subregion
                    if (population_size[year][run][cohort][subreg] > 0) {
                        for (int allele1 = 1; allele1 < sd.max_alleles; allele1++) {
                            for (int allele2 = 0; allele2 < allele1; allele2++) {
                                Ho += geneotypecounts[year][run][cohort][subreg][locus][tri(allele1, allele2)];
                            }
                        }
                    }
                    Ho /= population_size[year][run][cohort][subreg];
                    //expected heterozygous at this locus
                    double[] allelefrequencies = countAlleles(cohort, year, subreg, locus, run, true);
                    double He = 1.0;
                    for (int a = 0; a < sd.max_alleles; a++) {
                        He -= allelefrequencies[a] * allelefrequencies[a];
                    }
                    double fix = Ho / population_size[year][run][cohort][subreg];
                    Floc += (He - Ho + fix) / (He + fix);


                }
                x[run] = Floc / sd.number_loci;

            }
        } else//UNION of summary regions
        {


           //problem !

        }

        return DoStats(x);
    }
    /**
     * 
     * @param cohort
     * @param year
     * @param subreg
     * @return a count of the number of alleles (with positive frequency)
     * at all loci
     */

    double[] doAlleleCounts(int cohort, int year, int subreg) {
        //add up over loci and 
        //then do stats over the runs
        double[] x = new double[sd.number_runs];

        for (int run = 0; run < sd.number_runs; run++) {
            for (int locus = 0; locus < sd.number_loci; locus++) {
                x[run] += countAlleles(cohort, year, subreg, locus, run, false)[0];
            }
        }

        return DoStats(x);
    }

    /**
     * 
     * @param cohort
     * @param year
     * @param subreg
     * @return summary stats if the frequency of allele 0 at the indicated locus
     */
    double [] doAlleleFrequencies(int cohort, int year, int subreg,int locus)
    {
        double[] x = new double[sd.number_runs];
        for (int run = 0; run < sd.number_runs; run++) {
            
                x[run] = countAlleles(cohort, year, subreg, locus, run, true)[0];
            
        }
        return DoStats(x);
    }
    /**
     *
     * @param cohort
     * @param year
     * @param subreg the subregion to use or -1 for total over subregions
     * @param locus
     * @param run
     * @param frequencies -- return frequencies of all the alleles or just a
     * count of distinct alleles
     * @return
     */
    double[] countAlleles(int cohort, int year, int subreg, int locus, int run, boolean frequencies) {
        double[] allelecounts = new double[sd.max_alleles];

        if (subreg >= 0 && subreg <=number_subregions) {
            for (int allele = 0; allele < sd.max_alleles; allele++) {
                for (int other = 0; other < sd.max_alleles; other++) {
                    if (other == allele) {//homozygote
                        allelecounts[allele] += 2 * geneotypecounts[year][run][cohort][subreg][locus][tri(allele, other)];
                    } else {//heterozygote
                        allelecounts[allele] += geneotypecounts[year][run][cohort][subreg][locus][tri(allele, other)];
                    }
                }
            }
        } else {
            //problem!
        }


        double[] answer = new double[1];
        //we can get frequencies by dividing by population size
        if (frequencies) {
            answer = new double[sd.max_alleles];
            if (subreg >= 0 && subreg <= sd.summaryregions.Regions.size()) {
                for (int allele = 0; allele < sd.max_alleles; allele++) {
                    if (population_size[year][run][cohort][subreg] > 0) {
                        answer[allele] =
                                allelecounts[allele] / (2.0 * population_size[year][run][cohort][subreg]);
                    }

                }
            } else {
                //problem!

            }

        } //or we simply count the number of different alleles
        else {
            //answer=new double[1];
            for (int i = 0; i < allelecounts.length; i++) {
                if (allelecounts[i] > 0) {
                    answer[0]++;
                }
            }


        }
        return answer;
    }

    /**
     *
     * @param cohort 0 or 1 according to which is wanted
     * @param year
     * @param subreg
     * @return the average and std dev of the population in specified subregion
     * and year computed over all the runs or (if subreg is negative or not a
     * valid subregion number ) in the union of the subregions. 6 March 2012 The
     * output here agrees with that produced during the run by Statistics
     */
    double[] doPopulation(int cohort, int year, int subreg) {
        double[] x = new double[sd.number_runs];
        if (subreg >= 0 && subreg <= number_subregions) {
            for (int r = 0; r < sd.number_runs; r++) {
                //year,run,cohort=0,1,subregion
                x[r] = population_size[year][r][cohort][subreg];
            }
        } else//do the total population
        {
           
        }
        return DoStats(x);

    }

    String doOldReport() {
        String labels = "cohort,subregion,age,mean(pop),sd(pop),mean(alleles),sd(alleles),mean(Ho),sd(Ho),mean(He),sd(He),mean(F),sd(F)";
              //  + "mean(Seedbank proportion),sd(Seedbank proportion";
        StringBuilder sbl=new StringBuilder(labels);
        for(int i=0;i<this.Loci_to_do;i++)
        {
            int ll=Locus_to_start+i;
           sbl.append(",L"+ll+"A0,sd(L"+ll+"A0)");
           sbl.append(",L"+ll+"A0min,L"+ll+"A0max");
        }
        sbl.append("\n");
        String []cohortlabels={"false","true"};
        StringBuilder sb = new StringBuilder(sbl);
        for (int cohort = 0; cohort <= 1; cohort++) {
            for (int subr = 0; subr <= sd.summaryregions.Regions.size(); subr++) {
                for (int year = 0; year < sd.number_generations; year++) {
                    sb.append("" + cohortlabels[cohort] + "," + subr + "," + year + ",");
                    double[] ans = doPopulation(cohort, year, subr);
                    sb.append("" + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    ans = doAlleleCounts(cohort, year, subr);
                    sb.append("," + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    ans = doHo(cohort, year, subr);
                    sb.append("," + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    ans = doHe(cohort, year, subr);
                    sb.append("," + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    ans = doF(cohort, year, subr);
                    sb.append("," + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    //ans = doSeedbank(cohort, year, subr);
                    //sb.append("," + nf.format(ans[0]) + "," + nf.format(ans[1]));
                    for(int loc=0;loc<this.Loci_to_do;loc++)
                    {
                            int ll=Locus_to_start+loc;
                        ans=doAlleleFrequencies(cohort,year,subr,ll);
                    sb.append(","+nf.format(ans[0])+","+nf.format(ans[1]));
                    sb.append(","+nf.format(ans[2])+","+nf.format(ans[3]));
                    }
                    
                    
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    
   
    
    /**
     *
     * @param x array to calculate mean and sd of;
     * @return an array with mean and standard deviation min and max
     */
    public double[] DoStats(double[] x) {
        double s = 0, ss = 0;
        double[] results = new double[4];
        //if we have no or smal samples
        if (x == null || x.length == 0) {
            results[0] = results[1] = 0.0;
        }
        if (x.length == 1) {
            results[0] = x[0];
            results[1] = 0.0;
        }
        //otherwise
        double ourmin=x[0];
        double ourmax=x[0];
        
        for (int i = 0; i < x.length; i++) {
            if(x[i]<ourmin) ourmin=x[i];
            if(x[i]>ourmax) ourmax=x[i];
            s += x[i];
            ss += (x[i] * x[i]);
        }
        results[0] = s / x.length;
        results[1] = Math.sqrt((ss - x.length * results[0] * results[0]) / (x.length - 1));
        results[2]=ourmin;
        results[3]=ourmax;
        return results;
    }

    public static void main(String[] args) {
        
        System.out.println(DataReader.ProgramName);
        
        if(args.length<4)
        {
            System.out.println(DataReader.Intructions);
            System.out.println(DataReader.Info);
            System.exit(0);
        }
        System.out.println("Working with these files: "+args[0]+" that generated "+args[1]+"\n");
        DataReader dr = new DataReader(args);
        System.out.println(dr.doOldReport());
       
    }
}
