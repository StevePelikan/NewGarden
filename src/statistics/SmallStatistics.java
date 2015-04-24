/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import biology.Plant;
import biology.Region;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import parameters.SimData;

class DataLine implements Serializable {

    int year, run;
    /**
     * cohort,subregion*
     */
    int[][] population_size;//cohort=0,1,subregion
    /**
     * cohort, subregion*
     */
    int[][] seedbankcounts;//cohort,subregion
    /**
     * cohort, subregion, locus, allele*
     */
    int[][][][] allelecounts;//cohort,subregion,locus,allele
    /**
     * cohort,subregion,locus*
     */
    int[][][] heterozygouscounts;//cohort,subregion,locus

//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("run =" + run + " year =" + year + "\n");
//        sb.append("\tcohort = false" + Arrays.toString(population_size[0]));
//        sb.append("\n\tcohort = true" + Arrays.toString(population_size[1]));
//        sb.append("\n");
//        return (sb.toString());
//
//    }

    public DataLine(SimData data) {
        int number_observations, number_subregions;
        //maybe not the +1 here:
        number_observations = 1 + data.number_generations / data.datainterval;

        number_subregions = data.summaryregions.Regions.size();
        population_size = new int[2][number_subregions + 1];

        seedbankcounts = new int[2][number_subregions + 1];

        allelecounts = new int[2][number_subregions + 1][data.number_loci][data.max_alleles];
        heterozygouscounts = new int[2][number_subregions + 1][data.number_loci];
    }
}

/**
 *
 * @author sep
 */
public class SmallStatistics implements Stats {
    SimData data;
    NumberFormat nf;
    boolean DEBUG = true;
    //SimData data;
    int number_subregions, number_observations;
    int number_runs;
    int collection_interval;
    int number_loci;
    int max_alleles;
    ObjectOutputStream outputstream;
    ObjectInputStream objectinputstream = null;
    String filename = "tempdata.binary";
    int lineswritten = 0;
    String[] labels = {"cohort", "subregion", "age", "mean.pop", "sd.pop", "mean.alleles", "sd.alleles",
        "mean.Ho", "sd.Ho", "mean.Ho*", "sd.Ho*", "mean.He", "sd.He","mean.He*", "sd.He*", "mean.F", "sd.F.","mean.F*", "sd.F*",
        "mean.Seedbank.proportion","sd.Seedbank.proportion.","runs_extinct"};   
    
    int[][][][] population_size;//year,run,cohort=0,1,subregion
    int[][][][] seedbankcounts;//year,run,cohort,subregion
    int[][][][][][] allelecounts;//year,run,cohort,subregion,locus,allele
    int[][][][][] heterozygouscounts;//year,run,cohost,subregion,locus

    public SmallStatistics(SimData data) {
        this.data=data;
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        //this.data = data;
        number_observations = 1 + data.number_generations / data.datainterval;
        collection_interval = data.datainterval;
        number_subregions = data.summaryregions.Regions.size();
        number_runs = data.number_runs;
        number_loci = data.number_loci;
        max_alleles = data.max_alleles;
        
        //New Dec 2013 make a temporary file name
        // we might have multiple processes creating temp files in
        //the same directory (once we start using Runner.
         String workingdir=System.getProperty("user.dir");
         File tempfile=null;
        try {
            tempfile=File.createTempFile("temporary", ".binary", new File(workingdir));
            
        } catch (IOException ex) {
            Logger.getLogger(SmallStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(tempfile != null)
        {try {
           // FileOutputStream fos = new FileOutputStream(filename);
            filename=tempfile.getName();
            FileOutputStream fos = new FileOutputStream(tempfile);
            outputstream = new ObjectOutputStream(fos);

        } catch (IOException ex) {
            System.out.println(ex.toString());
        }}
        else
        {
            try{
             FileOutputStream fos = new FileOutputStream(filename);
            //filename=tempfile.getName();
            //FileOutputStream fos = new FileOutputStream(tempfile);
            outputstream = new ObjectOutputStream(fos);}
            catch(Exception ex)
            {
                System.out.println(ex.toString()); 
            }
        }

    }

    private void writeDataLine(DataLine dataline) {
        //run generation subregion cohortpopsize cohortallelcounts[locus][allele] cohortheterolocicounts
        //totalpopulationsize totalallelecounts[locus][allele] totalheterolocicounts
        try {
            outputstream.writeObject(dataline);

        } catch (IOException ex) {
            System.out.println(ex.toString());

        }
        lineswritten++;

    }

    public DataLine readDataLine() {
        DataLine dataline = null;
        try {
            dataline = (DataLine) objectinputstream.readObject();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        //lineswritten--;
        return (dataline);
    }

    @Override
    public void makeSummary(ArrayList<Plant> pop, SimData d) {
        //tabulate data into a DataLine then write this out 
        //to disk
        //throw new UnsupportedOperationException("Not supported yet.");
        DataLine dl = new DataLine(d);

        int ourYear = d.current_year / d.datainterval;
        dl.year = ourYear;
        dl.run = d.run_number;
        for (Plant p : pop) {

            //if (p.dob == d.current_year)//this cohort
            //19 Jan or if year=0 and plant is a founder (age <=0?)
            if (p.dob == d.current_year || (p.founder && d.current_year == 0)) {
                for (int sr = 0; sr < d.summaryregions.Regions.size(); sr++) {
                    //if plant is in summaryregion
                    Region rr = (Region) d.summaryregions.Regions.get(sr);
                    if (rr.inRegion(p.location.X, p.location.Y)) {
                        dl.population_size[1][sr]++;

//                        if (p.seedbank) {
//                            dl.seedbankcounts[1][sr]++;
//                        }

                        for (int locus = 0; locus < d.number_loci; locus++) {


                            dl.allelecounts[1][sr][locus][p.allele[locus][0]]++;
                            dl.allelecounts[1][sr][locus][p.allele[locus][1]]++;
                            if (p.allele[locus][0] != p.allele[locus][1]) {
                                dl.heterozygouscounts[1][sr][locus]++;
                            }

                        }
                    }
                }
                dl.population_size[1][d.summaryregions.Regions.size()]++;

//                if (p.seedbank) {
//                    dl.seedbankcounts[1][d.summaryregions.Regions.size()]++;
//                }

                for (int locus = 0; locus < d.number_loci; locus++) {

                    //year,run,cohort,subregion,locus,allele
                    dl.allelecounts[1][d.summaryregions.Regions.size()][locus][p.allele[locus][0]]++;
                    dl.allelecounts[1][d.summaryregions.Regions.size()][locus][p.allele[locus][1]]++;
                    if (p.allele[locus][0] != p.allele[locus][1]) {
                        dl.heterozygouscounts[1][d.summaryregions.Regions.size()][locus]++;
                    }

                }
            }
            //and now do it for the whole population cohort=0=false
            {
                for (int sr = 0; sr < d.summaryregions.Regions.size(); sr++) {
                    Region rr = (Region) d.summaryregions.Regions.get(sr);
                    if (rr.inRegion(p.location.X, p.location.Y)) {
                        dl.population_size[0][sr]++;

//                        if (p.seedbank) {
//                            dl.seedbankcounts[0][sr]++;
//                        }

                        for (int locus = 0; locus < d.number_loci; locus++) {


                            dl.allelecounts[0][sr][locus][p.allele[locus][0]]++;
                            dl.allelecounts[0][sr][locus][p.allele[locus][1]]++;
                            if (p.allele[locus][0] != p.allele[locus][1]) {
                                dl.heterozygouscounts[0][sr][locus]++;
                            }

                        }
                    }
                }
                //whole region whole population
                dl.population_size[0][d.summaryregions.Regions.size()]++;

//                if (p.seedbank) {
//                    dl.seedbankcounts[0][d.summaryregions.Regions.size()]++;
//                }

                for (int locus = 0; locus < d.number_loci; locus++) {


                    dl.allelecounts[0][d.summaryregions.Regions.size()][locus][p.allele[locus][0]]++;
                    dl.allelecounts[0][d.summaryregions.Regions.size()][locus][p.allele[locus][1]]++;
                    if (p.allele[locus][0] != p.allele[locus][1]) {
                        dl.heterozygouscounts[0][d.summaryregions.Regions.size()][locus]++;
                    }

                }
            }
        }
        //System.out.println(dl.toString());
        writeDataLine(dl);

    }

    @Override
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
     public double[] DoStats(double[] x,boolean [] skip) {
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
    public double[] doPopulation(int chrt, int year, int subreg) {
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
    
     public double[] doSeedBank(int chrt, int year, int subreg) {
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

    public double[] doAlleleCounts(int chrt, int year, int subreg) {
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
    
    public double[] doGeneDiversity(int chrt, int year, int subregion,boolean SkipNull) {
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

  
    public double[] doHeterozygosity(int chrt, int year, int subregion,boolean SkipNull) {
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
   
  public double[] doHeterozygosity(int chrt, int year, int subregion) {
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
   public double[] doFis(int chrt, int year, int subregion,boolean SkipNull) {
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
                double He = 0, Ho = 0;
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
    
    
   
 public double[] doFis(int chrt, int year, int subregion) {
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
                double He = 0, Ho = 0;
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
    public int doExtinct(int chrt, int year, int subregion)
    {
        int answer=0;
        year= year/data.datainterval;
        for(int run=0;run<data.number_runs;run++)
        {
            if(population_size[year][run][chrt][subregion]==0) answer++;
        }
        return answer;
    }
 public String doOldReport()
 {
     return(fullReport());
 }
    public String fullReport()
    {
        
        //allocate the memory
            number_observations=1+data.number_generations/data.datainterval;
        
        number_subregions = data.summaryregions.Regions.size();
        population_size = new int[number_observations][data.number_runs][2][number_subregions + 1];
        seedbankcounts = new int[number_observations][data.number_runs][2][number_subregions + 1];
        allelecounts = new int[number_observations][data.number_runs][2][number_subregions + 1][data.number_loci][data.max_alleles];
        heterozygouscounts = new int[number_observations][data.number_runs][2][number_subregions + 1][data.number_loci];
 //now read in all the data
         try {
            outputstream.flush();
            outputstream.close();
            FileInputStream fis = new FileInputStream(filename);
            objectinputstream = new ObjectInputStream(fis);


        } catch (Exception e) {
            System.out.println(e.toString());
        }
         
         int count = lineswritten;
        while (count > 0) {
            count--;
            DataLine dl = readDataLine();
            //System.out.println(dl.toString());
            for(int sr=0;sr<number_subregions+1;sr++)
            {
            population_size[dl.year][dl.run][0][sr]=dl.population_size[0][sr];
            population_size[dl.year][dl.run][1][sr]=dl.population_size[1][sr];
            seedbankcounts[dl.year][dl.run][0][sr]=dl.seedbankcounts[0][sr];
            seedbankcounts[dl.year][dl.run][1][sr]=dl.seedbankcounts[1][sr];
            for(int locus=0;locus<data.number_loci;locus++)
            {
                heterozygouscounts[dl.year][dl.run][0][sr][locus]=dl.heterozygouscounts[0][sr][locus];
                heterozygouscounts[dl.year][dl.run][1][sr][locus]=dl.heterozygouscounts[1][sr][locus];
                for(int allele=0;allele<max_alleles;allele++)
                {
                    allelecounts[dl.year][dl.run][0][sr][locus][allele]=dl.allelecounts[0][sr][locus][allele];
               allelecounts[dl.year][dl.run][1][sr][locus][allele]=dl.allelecounts[1][sr][locus][allele];
               
                }
            }
            }

        } 
         
        //now do the oldReport
        StringBuilder sb = new StringBuilder();
        double[] ans;
        for (int i = 0; i < labels.length - 1; i++) {
            sb.append(labels[i]).append(",");
        }
        sb.append(labels[labels.length - 1]).append("\n");
        for (int cohort = 0; cohort <= 1; cohort++) {
            for (int subregion = 0; subregion <= data.summaryregions.Regions.size(); subregion++) {
                for (int year = 0; year < data.number_generations; year+=data.datainterval) {
                    
                    sb.append(cohort==1).append(",").append(subregion).append(",").append(year).append(",");
                    ans = doPopulation(cohort, year, subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    ans = doAlleleCounts(cohort, year, subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    //ans = doHeterozygosity(cohort, year, subregion);
                    //sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                    
                     ans = doHeterozygosity(cohort, year, subregion,false);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   ans = doHeterozygosity(cohort, year, subregion,true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   
                    ans = doGeneDiversity(cohort, year, subregion,false);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   ans = doGeneDiversity(cohort, year, subregion,true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   
                    
                    
                    ans = doFis(cohort, year, subregion,false);
                    
                    
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   ans = doFis(cohort, year, subregion,true);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                   
                    ans=doSeedBank(cohort,year,subregion);
                    sb.append(nf.format(ans[0])).append(",").append(nf.format(ans[1])).append(",");
                     sb.append(doExtinct(cohort,year,subregion)+"\n");

                }//year
            }//subregion
        }//cohort

        return sb.toString();
    
    }

    /**
     * doOldReport reads in the data that was written to disk and calculates
     * some summary statistics. To keep things small it does this by repeatedly
     * reading the data and using just the part it needs for a given calculation
     * rather than allocating lots of memory and doing it all at once.
     *
     * @return
     */
    
}
