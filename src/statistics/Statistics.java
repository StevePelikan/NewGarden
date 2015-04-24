package statistics;

/*
 * This is a class to do all the data summary and reporting.
 *
 * It currently keeps all the numbers in memory.
 *
 * It is called at the end of each year of each run of the simulation with
 * makeSummary() which reads through the populations and counts size, alleles,
 * heterozygotes etc. for the entire population and the current cohort; It
 * handles all the subregions and the total reagion (as the highst numbered
 * subregion)
 *
 */
/**
 *
 * @author sep 5 Jan 2012 Started this class and will make in available in
 * NewGarden with an optional flag
 *
 *
 *
 * To start each quantity will be calculated by a separate method with arguments
 * that give cohort (0/1) and that computes for the whole region and each
 * subregion
 *
 * Many of the things we calculate are not estimates since they are based on the
 * entire population. So Heterozygosity (observed H) and GeneDiversity (expected
 * H) are not estimates and don't need standard errors or such things (though
 * their means over multiple runs do need sd or se and it might be interesting
 * to investigate their distribution for normality).
 *
 * The previous statistics output was arranged by cohort(false, true), then by
 * subregion, and then by age=year
 *
 * 19 Jan 2012 This class now seems to perform that same way that the original
 * NewGarden did (although we calculate F by a "more correct" method.
 *
 * The one difference is that founders with age=5 (say) aren't tabulated in the
 * year 0 cohort summary, but they are in the total population summer. This
 * happens, at least, when the founders are generated with a Random Plant List
 *
 * I fixed this by testing (in year 0) if the plant is a founder and, if so,
 * counting it as part of cohort 0, which is what the original NewGarden did
 *
 * * 26 Jan 2012 It now seems that this class works the same as previous
 * NewGarden data reporting (except for the Fis formula) and I've commented out
 * the old stats routines and only use this one now.
 *
 * I added a field datainterval value to SimData that lets us call the summary
 * method every k years. This made changes in the DTD, SimData, SimDataReader
 * and in this class.
 *
 * 18 April 2012 I added a routing to count in how many runs a population goes
 * to 0 (runs_extinct). When the population goes to 0 in 1 or more runs, we
 * average "0" in to the other statistics we're reporting (population size,
 * number alleles, F for example)
 *
 * I modified the doFis function to fix the problem of a locus at which a single
 * allele is fixed. We now count it as 0 in averaging over loci to find F Before
 * it entered as NaN
 *
 * I made these changes in SmallStatistics as well.
 *
 * 19 April 2012 Started setting things up so that we can report stats
 * calculated only over runs that have a non-zero population. Very likely the
 * stats we want to treat this way are the Ho, He, and F stats. Averaging in a
 * "0" for population size or number of alleles if population=0 seems
 * reasonable.
 *
 */
import biology.Plant;
import biology.Region;
import java.text.NumberFormat;
import java.util.ArrayList;
import parameters.SimData;

public class Statistics implements Stats {

    static String[] labels = {"cohort", "subregion", "age", "mean(pop)",
        "sd(pop)", "mean(alleles)", "sd(alleles)",
        "mean(Ho)", "sd(Ho)",
        "mean(Ho)*", "sd(Ho)*",
        "mean(He)", "sd(He)",
        "mean(He)*", "sd(He)*",
        "mean(F)", "sd(F)",
        "mean(F)*", "sd(F)*",
        "mean(Seedbank proportion)", "sd(Seedbank proportion)",
        "runs_extinct"};
    NumberFormat nf;
    boolean DEBUG = true;
    SimData data;
    int number_subregions, number_observations;
    int[][][][] population_size;//year,run,cohort=0,1,subregion
    int[][][][] seedbankcounts;//year,run,cohort,subregion
    int[][][][][][] allelecounts;//year,run,cohort,subregion,locus,allele
    int[][][][][] heterozygouscounts;//year,run,cohost,subregion,locus

    /**
     *
     * @param args the xml file and dumpfile names this version reads in all the
     * data at once from a dumpfile rather than accumulating it gradually. We
     * read in data using a modification of the routine in Fst
     */
    public Statistics(String[] args) {
    }

    public Statistics(SimData data) {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        this.data = data;
        number_observations = 1 + data.number_generations / data.datainterval;

        number_subregions = data.summaryregions.Regions.size();
        population_size = new int[number_observations][data.number_runs][2][number_subregions + 1];
        seedbankcounts = new int[number_observations][data.number_runs][2][number_subregions + 1];
        allelecounts = new int[number_observations][data.number_runs][2][number_subregions + 1][data.number_loci][data.max_alleles];
        heterozygouscounts = new int[number_observations][data.number_runs][2][number_subregions + 1][data.number_loci];
    }

    @Override
    public void makeSummary(ArrayList<Plant> pop, SimData d) {
        int ourYear = d.current_year / d.datainterval;
        for (Plant p : pop) {

            //if (p.dob == d.current_year)//this cohort
            //19 Jan or if year=0 and plant is a founder (age <=0?)
            if (p.dob == d.current_year || (p.founder && d.current_year == 0)) {
                for (int sr = 0; sr < d.summaryregions.Regions.size(); sr++) {
                    //if plant is in summaryregion
                    Region rr = (Region) d.summaryregions.Regions.get(sr);
                    if (rr.inRegion(p.location.X, p.location.Y)) {
                        population_size[ourYear][d.run_number][1][sr]++;
//                        if (p.seedbank) {
//                            seedbankcounts[ourYear][d.run_number][1][sr]++;
//                        }
                        for (int locus = 0; locus < d.number_loci; locus++) {


                            allelecounts[ourYear][d.run_number][1][sr][locus][p.allele[locus][0]]++;
                            allelecounts[ourYear][d.run_number][1][sr][locus][p.allele[locus][1]]++;
                            if (p.allele[locus][0] != p.allele[locus][1]) {
                                heterozygouscounts[ourYear][d.run_number][1][sr][locus]++;
                            }

                        }
                    }
                }
                population_size[ourYear][d.run_number][1][d.summaryregions.Regions.size()]++;
//                if (p.seedbank) {
//                    seedbankcounts[ourYear][d.run_number][1][d.summaryregions.Regions.size()]++;
//                }
                for (int locus = 0; locus < d.number_loci; locus++) {

                    //year,run,cohort,subregion,locus,allele
                    allelecounts[ourYear][d.run_number][1][d.summaryregions.Regions.size()][locus][p.allele[locus][0]]++;
                    allelecounts[ourYear][d.run_number][1][d.summaryregions.Regions.size()][locus][p.allele[locus][1]]++;
                    if (p.allele[locus][0] != p.allele[locus][1]) {
                        heterozygouscounts[ourYear][d.run_number][1][d.summaryregions.Regions.size()][locus]++;
                    }

                }
            }
            //and now do it for the whole population
            {
                for (int sr = 0; sr < d.summaryregions.Regions.size(); sr++) {
                    Region rr = (Region) d.summaryregions.Regions.get(sr);
                    if (rr.inRegion(p.location.X, p.location.Y)) {
                        population_size[ourYear][d.run_number][0][sr]++;
//                        if (p.seedbank) {
//                            seedbankcounts[ourYear][d.run_number][0][sr]++;
//                        }
                        for (int locus = 0; locus < d.number_loci; locus++) {


                            allelecounts[ourYear][d.run_number][0][sr][locus][p.allele[locus][0]]++;
                            allelecounts[ourYear][d.run_number][0][sr][locus][p.allele[locus][1]]++;
                            if (p.allele[locus][0] != p.allele[locus][1]) {
                                heterozygouscounts[ourYear][d.run_number][0][sr][locus]++;
                            }

                        }
                    }
                }
                //whole region whole population
                population_size[ourYear][d.run_number][0][d.summaryregions.Regions.size()]++;
//                if (p.seedbank) {
//                    seedbankcounts[ourYear][d.run_number][0][d.summaryregions.Regions.size()]++;
//                }
                for (int locus = 0; locus < d.number_loci; locus++) {


                    allelecounts[ourYear][d.run_number][0][d.summaryregions.Regions.size()][locus][p.allele[locus][0]]++;
                    allelecounts[ourYear][d.run_number][0][d.summaryregions.Regions.size()][locus][p.allele[locus][1]]++;
                    if (p.allele[locus][0] != p.allele[locus][1]) {
                        heterozygouscounts[ourYear][d.run_number][0][d.summaryregions.Regions.size()][locus]++;
                    }

                }
            }
        }



    }

    /**
     *
     * @param 0 or 1 indicating whether to include whole population of just
     * current cohort
     * @return String labeling and giving mean and SD of population in each
     * subregion and in total region. (total region is highest numbered
     * subregion)
     */
    public double[] doPopulation(int chrt, int year, int subreg) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doPopulation( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
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
        year = year / data.datainterval;
        double[] x = new double[data.number_runs];
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subreg] > 0) {
                x[r] = (1.0 * seedbankcounts[year][r][chrt][subreg]) / population_size[year][r][chrt][subreg];
            }
        }
        //sb.append("year = ").append(year).append(" subregion = ").append(subreg);
        double[] ans = DoStats(x);
        //sb.append(" mean population = ").append(ans[0]).append(" sd population = ").append(ans[1]).append("\n");
        return (ans);

        //return sb.toString();
    }

    /**
     *
     * @param chrt 0 or 1 according to whether only current cohort is to be
     * tabulated
     * @return a String giving the mean and standard deviation, these results
     * arranged by subregion number (the highest numbered one is the total
     * region)
     *
     * The total number of distinct alleles at each of the loci are tallied
     */
    public double[] doAlleleCounts(int chrt, int year, int subreg) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doAlleleCounts( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
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

    /**
     *
     * @param chrt 0 or 1 according to whether the standing population or just
     * the current cohort are to be considered
     * @return a String
     *
     * The diversity as a locus is 1- \sum p_i^2 where the p_i are the
     * frequencies of the alleles
     *
     * The overall GeneDiversity is the average over all the loci of the
     * diversity at each locus
     *
     * The calculation only makes sense if the population size is nonzero in a
     * given subregion. Otherwise the value 1.0 is returned
     *
     * Under HWE 1- \sum p_i^2 is the sum of the (expected) frequencies of all
     * the heterozygous genotypes so at a locus this is the "expected
     * heterozygosity" assuming HWE
     */
    public double[] doGeneDiversity(int chrt, int year, int subregion, boolean SkipNull) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doGeneDiversity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
        double[] x = new double[data.number_runs];
        boolean[] skip = null;
        if (SkipNull) {
            skip = new boolean[data.number_runs];
            for (int i = 0; i < data.number_runs; i++) {
                skip[i] = false;
            }
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
            } else {
                if (SkipNull) {
                    skip[r] = true;
                }
            }
        }
        double[] ans;
        if (!SkipNull) {
            ans = DoStats(x);
        } else {
            ans = DoStats(x, skip);
        }

        return ans;
    }

    /**
     *
     * @param chrt 0 or 1 according to whether total population or current
     * cohort is needed
     * @return a String
     *
     * The heterozygosity at a locus is the fraction of all individuals that are
     * heterozygous at that locus.
     *
     * The overall heterozygosity is average over all the loci.
     */
    /**
     *
     * @param chrt
     * @param year
     * @param subregion
     * @return
     */
    public double[] doHeterozygosity(int chrt, int year, int subregion) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doHeterozygosity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
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

    /**
     *
     * @param chrt
     * @param year
     * @param subregion
     * @param SkipNull whether to avoid averaging in H estimates if population
     * is 0
     * @return
     */
    public double[] doHeterozygosity(int chrt, int year, int subregion, boolean SkipNull) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doHeterozygosity( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
        //StringBuilder sb = new StringBuilder();
        double[] x = new double[data.number_runs];
        boolean[] skip = null;

        if (SkipNull) {
            skip = new boolean[data.number_runs];
            for (int i = 0; i < data.number_runs; i++) {
                skip[i] = false;
            }

        }
        for (int r = 0; r < data.number_runs; r++) {
            if (population_size[year][r][chrt][subregion] > 0) {
                for (int locus = 0; locus < data.number_loci; locus++) {

                    x[r] += 1.0 * heterozygouscounts[year][r][chrt][subregion][locus] / population_size[year][r][chrt][subregion];
                }
            } else {
                if (SkipNull) {
                    skip[r] = true;
                }
            }
            x[r] /= data.number_loci;
        }
        double[] ans;
        if (!SkipNull) {
            ans = DoStats(x);
        } else {
            ans = DoStats(x, skip);
        }
        return ans;
        //sb.append("year = ").append(year).append(" subregion = ").append(subregion);
        //sb.append(" mean observed heterozygosity = ").append(ans[0]).append(" sd observed heterozygosity = ").append(ans[1]).append("\n");
        //return sb.toString();
    }

    /**
     *
     * @param chrt
     * @param year
     * @param subregion
     * @return the number of runs that had 0 population
     */
    public int doExtinct(int chrt, int year, int subregion) {
        int answer = 0;
        year = year / data.datainterval;
        for (int run = 0; run < data.number_runs; run++) {
            if (population_size[year][run][chrt][subregion] == 0) {
                answer++;
            }
        }
        return answer;
    }

    /**
     *
     * @param chrt 0 or 1 to indicate whole population or cohort
     * @param year
     * @param subregion
     * @param SkipNull
     * @return a String
     *
     * We make calculations one locus at a time h=expected heterozygosity
     * H=observed heterozygosity f= (h-H- (1/n)H)/(h+(1/n)H)
     *
     * Then we report the mean over all the loci of this estimate
     *
     * If SkipNull is true the average F we report only includes runs with
     * nonzero populations. Otherwise we might get to low a number for how
     * inbred things are based on calculating with a 0 when there's no
     * population at all.
     */
    public double[] doFis(int chrt, int year, int subregion) {
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doFis( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
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
                    if (He == 0 && Ho == 0) {
                        fis = 1.0;
                    } else {
                        fis = (He - Ho + fix) / (He + fix);
                    }
                    x[r] += fis;
                }//locus
                x[r] /= data.number_loci;
            }
            //output  
        }//loop on runs
        double[] ans = DoStats(x);
        return ans;
    }
    // cohort(false, true), then by subregion, and then by age=year

    public double[] doFis(int chrt, int year, int subregion, boolean SkipNull) {
//          if(!SkipNull)
//          {
//              return doFis(chrt,year,subregion);
//          }
        if (DEBUG) {
            if (!(chrt == 0 || chrt == 1)) {
                System.out.println("Statistics: doFis( int chrt) has chrt =" + chrt + "which makes no sense\n");
            }
        }
        year = year / data.datainterval;
        double[] x = new double[data.number_runs];
        boolean[] skip = null;
        if (SkipNull) {
            skip = new boolean[data.number_runs];
            for (int i = 0; i < data.number_runs; i++) {
                skip[i] = false;
            }
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
                    if (He == 0 && Ho == 0) {
                        fis = 1.0;
                    } else {
                        fis = (He - Ho + fix) / (He + fix);
                    }
                    x[r] += fis;
                }//locus
                x[r] /= data.number_loci;
            } else {
                if (SkipNull) {
                    skip[r] = true;
                }
            }
            //output  
        }//loop on runs
        double[] ans;
        if (SkipNull) {
            ans = DoStats(x, skip);
        } else {
            ans = DoStats(x);
        }
        return ans;
    }

    public String doOldReport() {
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
                    sb.append(doExtinct(cohort, year, subregion) + "\n");

                }//year
            }//subregion
        }//cohort

        return sb.toString();
    }

    /**
     *
     * @param x array to calculate mean and sd of;
     * @param skip array of t/f indicating which values to use in calculating
     * the mean and sd.
     * @return an array with mean and standard deviation
     */
    @Override
    public double[] DoStats(double[] x) {
        double s = 0, ss = 0;
        double[] results = new double[2];
        //if we have no or small samples
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

    public double[] DoStats(double[] x, boolean[] skip) {
        assert (x.length == skip.length);
        if(skip==null){return DoStats(x);}
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
        int ourN = 0;
        for (int i = 0; i < x.length; i++) {
            if (!skip[i]) {
                ourN++;
                s += x[i];
                ss += (x[i] * x[i]);
            }
        }
        if (ourN > 0) {
            results[0] = s / ourN;
        } else {
            results[0] = Double.NaN;
        }
        if (ourN > 1) {
            results[1] = Math.sqrt((ss - ourN * results[0] * results[0]) / (ourN - 1));
        } else {
            results[1] = 0;
        }
        return results;
    }

    @Override
    public String fullReport() {
        return (doOldReport());
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
