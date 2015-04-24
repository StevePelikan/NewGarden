/*
 * This class takes a SimData.xml file that was used to generate
 * multiple "smallstats" temp files and the files themselves
 * and merges the results as if all the runs had been part of
 * one simulation
 * 
 * The files are called
 * tempdata.binary1,
 * tempdata.binary2,... tempdata.binaryN
 * 
 * for file k number_runs goes n=0...nr-1
 * and we count this is k*nr+n
 * 
 * The --smallstats option to the NewGarden program instructs NewGarden to
 * save intermediate summary statistics in a file on disk rather than retaining
 * these in memry during the simulation. This option should reduce the memory 
 * (Jave Virtual Machine memory, computer RAM) needed to run a simulation.
 * 
 * Using this option, at the end of all the runs, NewGarden reads in the 
 * information from the disk file and computes and displays the summary 
 * statistics as usual.
 * 
 * The file with the intermediate information remains on the disk and is
 * called "tempdata.binary".
 * 
 * You can use the StatsMerge program to re-read this intermediate information
 * and redisplay the summary statistics for the runs.
 * 
 * More importantly, if you run NewGarden several times using the same
 * SimData.xml file you can generate multiple tempdata.binary files, say
 * tempdata.binary1, tempdata.binary2, tempdata.binary3
 * each with intermediate information for the number of runs specified
 * in the SimData.xml The StatsMerge program can read all 3 of these 
 * temporary files and combine the information to produce summary statistics
 * as if the runs had all been part of one simulation 
 * (so, 3 times the number of runs in this case).
 * 
 * This will let you run NewGarden on different machines at the same time, 
 * or on a computer with multiple processors, and combined the results into
 * a single output summary.
 * 
 * Remarks:
 * 
 * 1) You must use the identical SimData.xml file for the different runs
 * if you want to combine the data
 * 
 * 2) Everytime NewGarden runs with the --smallstats option it uses the
 * same name for the disk file --- "tempdata.binary" So you need have the different
 * runs of NewGarden in different directories (or on different computers) in order
 * to keep the temporary files distinct (and un-overwritten, corrupted as a guess)
 * 
 * 3) Then you must rename the files tempdata.binary1, tempdata.binary2, tempdata.binary3
 * etc. consecutively starting with "1" appended and move all these files
 * to a single directory, along with the SimData.xml used to generate them. Only
 * then can you run the StatsMerge program.
 * 
 * The command line you'd use to do this is
 * 
 * java -jar NewGarden.jar statistics.StatsMerge MySimDataFile.xml 3
 */
package statistics;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import parameters.SimData;
import parameters.SimDataReader;

/**
 *
 * @author sep
 */
public class StatsMerge {
    boolean DEBUG=true;

    SimData data;
    String StatFileBaseName = "tempdata.binary";
    //call with SimDatafilename, number of temp files
    int N = 1; /// number of files to merge
      String[] labels = {"cohort", "subregion", "age", "mean(pop)", "sd(pop)", "mean(alleles)", "sd(alleles)",
        "mean(Ho)", "sd(Ho)", "mean(Ho)*", "sd(Ho)*", "mean(He)", "sd(He)","mean(He)*", "sd(He)*", "mean(F)", "sd(F)","mean(F)*", "sd(F)*",
        "mean(Seedbank proportion)","sd(Seedbank proportion)","runs_extinct"};   
    NumberFormat nf;
    int[][][][] population_size;//year,run,cohort=0,1,subregion
    int[][][][] seedbankcounts;//year,run,cohort,subregion
    int[][][][][][] allelecounts;//year,run,cohort,subregion,locus,allele
    int[][][][][] heterozygouscounts;//year,run,cohost,subregion,locus
    int number_subregions, number_observations;
    int number_runs;
    int collection_interval;
    int number_loci;
    int max_alleles;
    //this.data = data;

    public StatsMerge(String[] args) {
        getSimData(args[0]);
        this.N = Integer.parseInt(args[1]);
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        number_observations = 1 + data.number_generations / data.datainterval;
        collection_interval = data.datainterval;
        number_subregions = data.summaryregions.Regions.size();
        number_runs = data.number_runs;
        number_loci = data.number_loci;
        max_alleles = data.max_alleles;
    }

    public static void main(String[] args) {
        StatsMerge sm = new StatsMerge(args);
        System.out.println(sm.fullReport());
    }
    //from Model

    private void getSimData(String xmlfilename) {

        // New 27 Jan 2012 make sure the xml file is valid
        //No need for this here since NewGarden already
        //ran with this xml file to generate the output
        //we're about to read.
        //Validator v = new Validator(xmlfilename);
        //String ans = v.validate();
        //if (ans.length() > 0) {
         //   System.err.println(ans);
          //  System.exit(0);

        //}
        // end new 27 Jan 2012

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
        data = my_mb.getSD();
        data.MakeFinal();
    }

    private String fullReport() {

        //allocate the memory
        number_observations = 1 + data.number_generations / data.datainterval;
        int MergedNumberRuns = N * data.number_runs;

        number_subregions = data.summaryregions.Regions.size();
        population_size = new int[number_observations][MergedNumberRuns][2][number_subregions + 1];
        seedbankcounts = new int[number_observations][MergedNumberRuns][2][number_subregions + 1];
        allelecounts = new int[number_observations][MergedNumberRuns][2][number_subregions + 1][data.number_loci][data.max_alleles];
        heterozygouscounts = new int[number_observations][MergedNumberRuns][2][number_subregions + 1][data.number_loci];
        //now read in all the data
        //loop over the files to read
        for (int file_no = 1; file_no <= N; file_no++) {
            String ThisFileName = this.StatFileBaseName.concat(Integer.toString(file_no));
            ObjectInputStream objectinputstream = null;
            try {
                // outputstream.flush();
                //outputstream.close();
                FileInputStream fis = new FileInputStream(ThisFileName);
                objectinputstream = new ObjectInputStream(fis);


            } catch (Exception e) {
                System.out.println(e.toString());
            }
            //loop over objects in the file
            DataLine dl;
            
            while ((dl = readDataLine(objectinputstream)) != null) {
                int OurRun=(file_no-1)*data.number_runs+dl.run;
                for (int sr = 0; sr < number_subregions + 1; sr++) {
                    population_size[dl.year][OurRun][0][sr] = dl.population_size[0][sr];
                    population_size[dl.year][OurRun][1][sr] = dl.population_size[1][sr];
                    seedbankcounts[dl.year][OurRun][0][sr] = dl.seedbankcounts[0][sr];
                    seedbankcounts[dl.year][OurRun][1][sr] = dl.seedbankcounts[1][sr];
                    for (int locus = 0; locus < data.number_loci; locus++) {
                        heterozygouscounts[dl.year][OurRun][0][sr][locus] = dl.heterozygouscounts[0][sr][locus];
                        heterozygouscounts[dl.year][OurRun][1][sr][locus] = dl.heterozygouscounts[1][sr][locus];
                        for (int allele = 0; allele < max_alleles; allele++) {
                            allelecounts[dl.year][OurRun][0][sr][locus][allele] = dl.allelecounts[0][sr][locus][allele];
                            allelecounts[dl.year][OurRun][1][sr][locus][allele] = dl.allelecounts[1][sr][locus][allele];

                        }
                    }
                }
            }//lines in this file
            try{
            objectinputstream.close();}
            catch(Exception e)
            {
                System.out.println(e.toString());
            }

        }//loop over files

       //Now adjust data.number_runs so that summary stats get made
        //of all the stuff we've read.
        data.number_runs=MergedNumberRuns;

        //now do the oldReport
        StringBuilder sb = new StringBuilder();
        double[] ans;
        for (int i = 0; i < labels.length - 1; i++) {
            sb.append(labels[i]).append(",");
        }
        sb.append(labels[labels.length - 1]).append("\n");
        for (int cohort = 0; cohort <= 1; cohort++) {
            for (int subregion = 0; subregion <= data.summaryregions.Regions.size(); subregion++) {
                for (int year = 0; year < data.number_generations; year += data.datainterval) {

                    sb.append(cohort == 1).append(",").append(subregion).append(",").append(year).append(",");
                    ans = doPopulation(cohort, year, subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    ans = doAlleleCounts(cohort, year, subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    //ans = doHeterozygosity(cohort, year, subregion);
                    //sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");

                    ans = doHeterozygosity(cohort, year, subregion, false);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    ans = doHeterozygosity(cohort, year, subregion, true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");

                    ans = doGeneDiversity(cohort, year, subregion, false);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    ans = doGeneDiversity(cohort, year, subregion, true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");



                    ans = doFis(cohort, year, subregion, false);


                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    ans = doFis(cohort, year, subregion, true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");

                    ans = doSeedBank(cohort, year, subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    sb.append(doExtinct(cohort, year, subregion)).append("\n");

                }//year
            }//subregion
        }//cohort

        return sb.toString();

    }


   private double[] DoStats(double[] x) {
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
    private double[] DoStats(double[] x,boolean [] skip) {
        assert(x.length==skip.length);
        double s = 0, ss = 0;
        double[] results = new double[2];
        //if we have no or small samples
        if (x == null || x.length == 0) {
            results[0] = results[1] = 0.0;
        }
        if (x.length == 1 && !skip[0]) {
            results[0] = x[0];
            results[1] = 0.0;
        }
        //otherwise
        int ourN=0;
        for (int i = 0; i < x.length; i++) {
            if(!skip[i])
            {
                ourN++;
                s += x[i];
                ss += (x[i] * x[i]);
            }
        }
        if(ourN>0){results[0] = s / ourN;}
        else{results[0]= Double.NaN;}
        if(ourN>1)
        {
        results[1] = Math.sqrt((ss - ourN * results[0] * results[0]) / (ourN - 1));
        }
        else{
            results[1]=0;
        }
        return results;
    }


    /**
     * 
     * @param chrt
     * @param year
     * @param subreg
     * @return 
     */
    private double[] doPopulation(int chrt, int year, int subreg) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doPopulation( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year= year/data.datainterval;
        //StringBuilder sb = new StringBuilder();

        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            x[r] = population_size[year][r][chrt][subreg];
        }
        //sb.append("year = ").append(year).append(" subregion = ").append(subreg);
        double[] ans = DoStats(x);
        //sb.append(" mean population = ").append(ans[0]).append(" sd population = ").append(ans[1]).append("\n");
        return (ans);

        //return sb.toString();
    }
    
     private double[] doSeedBank(int chrt, int year, int subreg) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doSeedBank( int chrt,....) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        //StringBuilder sb = new StringBuilder();
        year= year/data.datainterval;
        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            if(population_size[year][r][chrt][subreg]>0)
            {
            x[r] = (1.0*seedbankcounts[year][r][chrt][subreg])/  population_size[year][r][chrt][subreg];
            }
        }
        //sb.append("year = ").append(year).append(" subregion = ").append(subreg);
        double[] ans = DoStats(x);
        //sb.append(" mean population = ").append(ans[0]).append(" sd population = ").append(ans[1]).append("\n");
        return (ans);

        //return sb.toString();
    }

    private double[] doAlleleCounts(int chrt, int year, int subreg) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doAlleleCounts( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
 year= year/data.datainterval;
        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            for (int locus = 0; locus < data.number_loci; locus++) {
                for (int allele = 0; allele < data.max_alleles; allele++) {
                    if (allelecounts[year][r][chrt][subreg][locus][allele] > 0) {
                        x[r]++;
                    }
                }
            }
        }
        double[] ans = DoStats(x);
        return ans;

    }
    
    private double[] doGeneDiversity(int chrt, int year, int subregion,boolean SkipNull) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doGeneDiversity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
         year= year/data.datainterval;
        double[] x = new double[data.number_runs];
        boolean[] skip=null;
        if(SkipNull)
        {
            skip=new boolean[data.number_runs];
            for(int i=0;i<data.number_runs;i++)skip[i]=false;
        }
        for (int r = 0; r < data.number_runs; r++) {
            if (this.population_size[year][r][chrt][subregion] > 0) {
            double locusdiversity = 0.0;
            for (int locus = 0; locus < data.number_loci; locus++) {
                double sumsq = 0.0;
                //if (this.population_size[year][r][chrt][subregion] > 0) {
                    for (int allele = 0; allele < data.max_alleles; allele++) {
                        double ss = 1.0 * this.allelecounts[year][r][chrt][subregion][locus][allele] / (2 * population_size[year][r][chrt][subregion]);
                        ss = ss * ss;
                        sumsq += ss;
                    }
                //}
                locusdiversity += (1 - sumsq);
            }
            
            x[r] = locusdiversity / data.number_loci;
            }
            else{if(SkipNull)skip[r]=true;}
        }
        double[] ans;
        if(!SkipNull){ans= DoStats(x);}
        else{ans=DoStats(x,skip);}
        
        return ans;
    }

  
    private double[] doHeterozygosity(int chrt, int year, int subregion,boolean SkipNull) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doHeterozygosity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
         year= year/data.datainterval;
        //StringBuilder sb = new StringBuilder();
        double[] x = new double[data.number_runs];
        boolean [] skip=null;
        
        if(SkipNull)
        {
             skip=new boolean[data.number_runs];
            for(int i=0;i<data.number_runs;i++)skip[i]=false;
           
        }
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subregion] > 0) {
                for (int locus = 0; locus < data.number_loci; locus++) {

                    x[r] += 1.0 * heterozygouscounts[year][r][chrt][subregion][locus] / population_size[year][r][chrt][subregion];
                }
            }
            else{if(SkipNull){skip[r]=true;}
            }
            x[r] /= data.number_loci;
        }
        double[] ans;
        if(!SkipNull){ans= DoStats(x);}
        else{ans=DoStats(x,skip);}
        return ans;
        //sb.append("year = ").append(year).append(" subregion = ").append(subregion);
        //sb.append(" mean observed heterozygosity = ").append(ans[0]).append(" sd observed heterozygosity = ").append(ans[1]).append("\n");
        //return sb.toString();
    }
   
  private double[] doHeterozygosity(int chrt, int year, int subregion) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doHeterozygosity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
         year= year/data.datainterval;
        //StringBuilder sb = new StringBuilder();
        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subregion] > 0) {
                for (int locus = 0; locus < data.number_loci; locus++) {

                    x[r] += 1.0 * heterozygouscounts[year][r][chrt][subregion][locus] / population_size[year][r][chrt][subregion];
                }
            }
            x[r] /= data.number_loci;
        }
        double[] ans = DoStats(x);
        return ans;
        //sb.append("year = ").append(year).append(" subregion = ").append(subregion);
        //sb.append(" mean observed heterozygosity = ").append(ans[0]).append(" sd observed heterozygosity = ").append(ans[1]).append("\n");
        //return sb.toString();
    }
  private double[] doFis(int chrt, int year, int subregion,boolean SkipNull) {
         // if(!SkipNull)
         // {
         //     return doFis(chrt,year,subregion);
         // }
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doFis( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
         year= year/data.datainterval;
        double[] x = new double[data.number_runs];
        boolean [] skip=null;
        if(SkipNull){
            skip=new boolean[data.number_runs];
        for(int i=0;i<data.number_runs;i++){skip[i]=false;}
        
        }
        
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subregion] > 0) {
                //for each locus
                double He , Ho ;
                for (int locus = 0; locus < data.number_loci; locus++) {
                    Ho = 1.0 * heterozygouscounts[year][r][chrt][subregion][locus] / population_size[year][r][chrt][subregion];
                    He = 1.0;
                    for (int allele = 0; allele < data.max_alleles; allele++) {
                        //h -= p_{uu}^2
                        double ss = 1.0 * this.allelecounts[year][r][chrt][subregion][locus][allele] / (2 * population_size[year][r][chrt][subregion]);
                        ss = ss * ss;
                        He -= ss;
                    }
                    double fix = Ho / population_size[year][r][chrt][subregion];
                    double fis;
                    if(He==0 && Ho==0) {fis=1.0;}
                    else{
                    fis= (He - Ho + fix) / (He + fix);}
                    x[r] += fis;
                }//locus
                x[r] /= data.number_loci;
            }
            else{if(SkipNull) skip[r]=true;}
            //output  
        }//loop on runs
        double[] ans;
        if(SkipNull){ans = DoStats(x,skip);}
        else{ans=DoStats(x);}
        return ans;
    }
    
    
   
private double[] doFis(int chrt, int year, int subregion) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doFis( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
         year= year/data.datainterval;
       


        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subregion] > 0) {
                //for each locus
                double He , Ho ;
                for (int locus = 0; locus < data.number_loci; locus++) {
                    Ho = 1.0 * heterozygouscounts[year][r][chrt][subregion][locus] / population_size[year][r][chrt][subregion];
                    He = 1.0;
                    for (int allele = 0; allele < data.max_alleles; allele++) {
                        //h -= p_{uu}^2
                        double ss = 1.0 * this.allelecounts[year][r][chrt][subregion][locus][allele] / (2 * population_size[year][r][chrt][subregion]);
                        ss = ss * ss;
                        He -= ss;
                    }
                    double fix = Ho / population_size[year][r][chrt][subregion];
                     double fis;
                    if(He==0 && Ho==0) {fis=1.0;}
                    else{
                    fis= (He - Ho + fix) / (He + fix);}
                   
                    x[r] += fis;
                }
                x[r] /= data.number_loci;

            }
            //output  
        }//loop on runs
        double[] ans = DoStats(x);
        return ans;

    }
    // cohort(false, true), then by subregion, and then by age=year
 /**
     * 
     * @param chrt
     * @param year
     * @param subregion
     * @return the number of runs that had 0 population
     */
    private int doExtinct(int chrt, int year, int subregion)
    {
        int answer=0;
        year= year/data.datainterval;
        for(int run=0;run<data.number_runs;run++)
        {
            if(population_size[year][run][chrt][subregion]==0) answer++;
        }
        return answer;
    }
// public String doOldReport()
 //{
  //   return(fullReport());
// }
   
    private DataLine readDataLine(ObjectInputStream ois) {
        DataLine dataline = null;
        try {
            dataline = (DataLine) ois.readObject();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        //lineswritten--;
        return (dataline);
    }
}
