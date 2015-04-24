/*
 * Copyright (C) 2005-2014 s pelikan and s rogstad
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package model;

/*
 * Model.java
 *
 * Created on July 16, 2005, 12:39 AM Last modified 19 Jans 2012
 *
 */
/*
 * Changes
 *
 * April 2006 Introduced the Gnu GetOpt class and made command line options to
 * put turn lifetable on/off and to turn dump or whole population on/off with
 * options to put this output into text files.
 *
 *
 * ??? Changed distribution mechanism from FruitDistance to DispersalDistance
 * line which we can specify the CDF of dispersal distances Changed the DTD and
 * the sample SimData.xml
 *
 * April 2009 Changed "Pollen_Distance" to a mechanism when we specifiy the
 * probabilities that pollen donor lies in different distance classes. Changed
 * DTD and sample SimData.xml
 *
 *
 * 9 May 2006. Added ProgressBar and command line switches to turn it on
 *
 * 9 Jan 2008. I made sure that OffspringDistribution is read from the xml file,
 * changed to the new mechanism for regions and subregions, provided for summary
 * stats used on subregions, and made sure the FoundersF is read from the xml
 * file.
 *
 * 15 Dec 2010 Wrote a simple command line options parser so we don't need to
 * use the getopt() library since I can't determine exactly whether it is LGPG
 * or GPL'd or what.
 *
 * 29July2011 changed makeinitialpopuation to generate random founders
 *
 * 22 Sept 2011 Started changes to let us specify random founders in a rectangle
 * with several RandomPlantList statements so that we can set up desired age/sex
 * structure in the region. This means that we have to treat RandomPlantLists in
 * groups when we generate founders for a given rectangle so we don't have
 * multiple cals and perhaps generate multiple fuonders at the same location
 *
 *
 * 5 Jan 2012 Introduced Statistics class to start to modify how we do summaries
 * and print data All changes are marked between //begin 5 Jan 2010 and //end 5
 * Jan 2010
 *
 * Eventually we can eliminate the bookkeeping variables in the class and do
 * away with expopulation
 *
 * 19 Jan 2012. More changes to the new Statistics class. Plus I moved
 * CommandLineParameters to its own file and made it public Changed data to
 * today and version to 2.5
 *
 * 5 Feb 2012. Will dump expopulation at the end of every year rather than
 * saving up a big list of stuff.
 *
 * 16 March 2012 added a new commandline option --skipstats that keeps the
 * program from accumulating data to print summary stats at the end of the run.
 * This is to be used when we're dumping the raw data to a file anyway since
 * then DataReader can calculate the stats later.
 *
 * 12 April 2012 --smallstats commandline option now uses a Stats method that
 * writes out intermediate statistics to a data file and then reads them in at
 * the end.
 *
 * 
 * 22 June 2012 I'll do some tracing, profiling, debugging to see how 
 * much memory is getting used and whether we can reduce how much uis needed.
 * Yamini has some 600 year runs for her rainforest model that end for lack 
 * of memory or stack eve though her populations never get much above 1-^4.
 * 
 * 30 Feb 2013 I added an extra bar in the ProgressFrame that reports free memory
 * and explicitly called GC. This might give us some idea about possib;e memory problems.
 * 
 * 30 April 2013 I added some prints to trace generation of new plants under the new,
 * seed=bank model. There's a new variable int []plantcounts=new int[5]; in nextYear()
 * that gets updated as that method executes.

 *FEB 2014 changed runModel() to update dynamic regions every year of a run
 and update random regions at the start of every run

 Incuded a call in runModel() to generate maps if requested in the MakeMaps SimData element.

April 2014 Directional Pollen and Dispersal

22 May 14 Agamospermy
27 May 2014 SeedCollection
 */
/**
 *
 * @author sep
 */
import Leslie.Leslie;
import ProgressFrame.ProgressFrame;
import biology.Location;
import biology.Locus;
import biology.NewRectangle;
import biology.Plant;
import biology.RECTANGLETYPE;
import biology.Region;
import functions.DPD;
import functions.RandomValue;
import getopt.Getopt;
import getopt.LongOpt;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import parameters.Founders;
import parameters.RandomPlantList;
import parameters.SimData;
import parameters.SimDataReader;
import parameters.Validator;
import statistics.Image;
import statistics.Map;
import statistics.SmallStatistics;
import statistics.Statistics;
import statistics.Stats;

public final class Model {

    static final double SQRT2O2 = 0.707106781;
    static final double PIO180 = 0.017453293;
    static final boolean INBREEDING=true;
    Random rnum;
    RandomValue rv;
    Leslie les;
    SimData sd;
    // NEW SEED STUFF (seedbank)
    ArrayList<Plant> population, expopulation, seedbank;
    NumberFormat nf;
    //boolean dump_pop = false;
    boolean do_life_table = false;
    boolean save_all_data = false;
    static final boolean DEBUG = false;
    static final boolean DEBUGMEMORY = false;
    // 5 Jan 2012
    //Statistics statistics;
    Stats ourStats;
    private CommandLineParameters params;
    private  LongOpt[] longopts = new LongOpt[8];

    private void initializeLongOpts() {
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("xmlfile", LongOpt.REQUIRED_ARGUMENT, null,
                'x');
        longopts[2] = new LongOpt("dumpfile", LongOpt.OPTIONAL_ARGUMENT, null,
                'd');
        longopts[3] = new LongOpt("progressbar", LongOpt.OPTIONAL_ARGUMENT,
                null, 'p');
        longopts[4] = new LongOpt("resultsfile", LongOpt.OPTIONAL_ARGUMENT,
                null, 'r');
        longopts[5] = new LongOpt("showNe", LongOpt.NO_ARGUMENT, null, 'n');
        longopts[6] = new LongOpt("skipstats", LongOpt.NO_ARGUMENT, null, 'k');
        longopts[7] = new LongOpt("smallstats", LongOpt.NO_ARGUMENT, null, 'z');
    }

    private void doOptions(Getopt g, String[] args) {

        int c;
        String arg;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                // options without arguments
                case 'h':
                    System.out.println("You asked for help");
                    System.out.println("The command line options are\n"
                            + "-h or --help: show this help message\n"
                            + "-x or --xmlfile <filename>: specify the XML parameter file\n"
                            + "-d or --dumpfile <filename>: put all the population data into the filename\n"
                            + "\t\t or to stdout if <filename> is not given\n"
                            + "-l or --lifetable <filename>: write Leslie lifetable to the file filename\n"
                            + "\t\t or to stdout if <filename> is not given.\n"
                            + "-p or --progressbar : show a progress bar on screen\n"
                            + "-v to label all bits of output with date-time and program version\n"
                            + "-r or --resultsfile <filename> to send the main results to the file filename\n"
                            + "-s to include a copy of the XML specified parameters in the results file\n"
                            //+ "-n or --showNe to show estimtates of Ne and their sd\n"
                            + "--skipstats to simply dump data and not bother to accumulate\n"
                            + "\t\t and report stats during the run\n"
                            + "--smallstats to use a method that writes intermediate data\n"
                            + "\t\t to disk rather than storing it in memory.");
                    System.exit(0);
                    break;
                case 'd':
                    arg = g.getOptarg();
                    if (arg != null) {
                        // System.out.println("You asked for a dump to file "+arg);
                        params.DUMPfilename = arg;
                        params.ShowDump = true;
                    }
                    if (arg == null) {
                        // System.out.println("You asked for a dump to stdout ");
                        params.DUMPfilename = null;
                        params.ShowDump = true;
                    }
                    break;
                case 'k': // --skipstats

                    params.SkipStats = true;

                    break;
                case 'z': // --smallstats

                    params.SmallStats = true;

                    break;
                case 'r':
                    arg = g.getOptarg();
                    if (arg != null) {
                        // System.out.println("You asked results written to "+arg);
                        params.RESULTSfilename = arg;
                    }
                    if (arg == null) {
                        System.out.println("If you use the '-r' commandline flag, you need to specify a filename for results file.\n");
                    }
                    break;
                case 's':
                    params.ShowXML = true;

                    break;
                case 'n':
                    //this no longer causes us to do anything
                    params.ShowNe = true;
                    break;
                case 'p':
                    // show_progress=true;
                    params.ShowProgress = true;
                    break;
                case 'x':
                    arg = g.getOptarg();
                    if (arg != null) {
                        // System.out.println("You asked for XML parameters from file "+arg);
                        params.XMLfilename = arg;
                    }
                    if (arg == null) {
                        System.out.println("You need to specify a filename for this option");
                    }
                    break;
                case 'l':
                    arg = g.getOptarg();
                    if (arg != null) {
                        // System.out.println("You asked for lifetable info to go to file "+arg);
                        params.ShowLifetable = true;
                        params.LIFETABLEfilename = arg;
                    }
                    if (arg == null) {
                        // System.out.println("You asked for lifetable info in stdout ");
                        params.ShowLifetable = true;
                        params.LIFETABLEfilename = null;
                    }
                    break;

                // options with arguments
                case 'v':
                    // System.out.println("You asked for version information to be printed");
                    params.ShowVersion = true;
                    break;

                //
                case '?':
                    break; // getopt() already printed an error
                //
                default:
                // System.out.print("getopt() returned " + c + "\n");
            }
        }
        // IF XMLFILE isn't defined, it must be given on the command line after
        // the options
        for (int i = g.getOptind(); i < args.length; i++) {
            System.out.println("Non option args element: " + args[i]);
            // Pick out the XML filename here.
            // params.XMLfilename=args[(g.getOptind())];
        }
    }

    /**
     * Creates a new instance of Model
     */
    /**
     * This version parses the command line via getopts
     *
     */
    public Model(String[] args) {

        // HERE IS THE NEW COMMAND LINE PARSING
        //params is global and could be final I think 29 May 2014
        params = new CommandLineParameters();
        initializeLongOpts();
        Getopt g = new Getopt("NewGarden", args, "snr:hpvl:x:d:", longopts);
        doOptions(g, args);

        // newdoOptions(args);
        rnum = new Random();
        rv = new RandomValue();
        rnum.setSeed(System.currentTimeMillis());
        //may 2014 we can now do 
        //sd=SimData.readXMLFile(params.XMLfilename);
        //
        getSimData(params.XMLfilename);

        sd.MakeFinal();

        //dump_pop = params.ShowDump;
        do_life_table = params.ShowLifetable;
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        //Select a Stats implementation if we are to output statistics
        if (!params.SkipStats) {
            if (!params.SmallStats) {
                ourStats = new Statistics(sd);
            } //new smallstats 11 April
            else {
                ourStats = new SmallStatistics(sd);
            }
        }
        //runModel();
    }

    /**
     *
     * @param xmlfilename filename to read the SimData from
     *
     * 27 Jan 2012 added a call to a Validator and possible exit that will
     * enforce the .xml file being valid
     */
    public void getSimData(String xmlfilename) {

        // New 27 Jan 2012 make sure the xml file is valid
        Validator v = new Validator(xmlfilename);
        String ans = v.validate();
        if (ans.length() > 0) {
            System.err.println(ans);
            System.exit(0);

        }
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
        sd = my_mb.getSD();
        sd.MakeFinal();
    }

    /**
     * do life table calculations with the leslie model put the results to
     * stdout or a file depending on what the commandline asked for
     * 
     * This has been changed to doing a simple power method approximation to
     * largest lambda and equilibrium distribution because doing "calculate()"
     * on huge matrices needed to Tropical Forest models took too long.
     *
     */
    void doLifeTable() {
        PrintStream ourPS;
        if (params.LIFETABLEfilename == null) {
            ourPS = System.out;
        } else {
            try {
                ourPS = new PrintStream(new File(params.LIFETABLEfilename));
            } catch (Exception e) {
                System.out.println(e.toString());
                ourPS = System.out;
            }
        }
        if (params.ShowVersion && params.LIFETABLEfilename != null) {
            ourPS.println(params.toString(true));
        }
        ourPS.println("LIFE TABLE CALCULATIONS (power method)");
        if (sd == null) {
            System.out.println("Cant do lifetable calculations without a SimData");
        }
        // build an array of reproduction rates
        // first, how many age classes --- what is largest age
        // for which mortality and reproduction is defined.
        double x1 = sd.fruit_production.x[sd.fruit_production.x.length - 1];
        double x2 = sd.mortality.x[sd.mortality.x.length - 1];
        double maxage = x1 < x2 ? x2 : x1;
        int ourdim = (int) maxage;
        double[] rrate = new double[(int) maxage + 1];
        // get reproduction rates according to current method
        for (int i = 0; i <= maxage; i++) {
            rrate[i] = sd.fruit_production.value(i);
            int n_offspring;
            if (sd.off_dist_method.equals("bracket")) {
                double fp = sd.fruit_production.value(i);
                // we pick between a=floor(fp) and b=floor(fp)+1 so we
                // end up with the correct average
                // p a +(1-p)b =fp
                // so p(a-b)=fp-b
                // p =(b-fp)/(b-a)=b-fp
                int a = (int) Math.floor(fp);
                if ((double) a == fp) {
                    n_offspring = a;
                } else {
                    int b = a + 1;
                    if (rnum.nextDouble() < (double) b - fp) {
                        n_offspring = a;
                    } else {
                        n_offspring = b;
                    }
                }
            } else if (sd.off_dist_method.equals("poisson")) {
                // System.out.println("poisson reproduction isn't implemented yet!");
                // System.exit(0);
                double fp = sd.fruit_production.value(i);
                n_offspring = (int) rv.Poisson(fp, 1)[0];

            } else // i.e. "round" or unspecified
            {
                n_offspring = (int) Math.round(sd.fruit_production.value(i));
            }
            rrate[i] = n_offspring;
        }
        double[] mrate = new double[(int) maxage];
        for (int i = 0; i < maxage; i++) {
            mrate[i] = sd.mortality.value((double) i);
        }

        les = new Leslie(rrate, mrate);
        if (DEBUG) {
            ourPS.println("Here are the reproduction rates:");
            for (int i = 0; i < rrate.length; i++) {
                ourPS.print(" " + nf.format(rrate[i]));
            }
            ourPS.println();

            ourPS.println("Here are the mortality nfrates:");
            for (int i = 0; i < mrate.length; i++) {
                ourPS.print(" " + nf.format(mrate[i]));
            }
            ourPS.println();

        }
        ourPS.println(les.quickCalculate());
        // now the approximate calculation
        // we begin by getting initial population age distribution.
//        double[] n0 = new double[rrate.length];
//        Iterator it = sd.plants.iterator();
//        while (it.hasNext()) {
//            Plant pl = (Plant) it.next();
//            n0[pl.dob]++;
//        }
//        ourPS.println(les.approximate(n0, 5));
    }

    
    
    /*THIS IS NG2 makeInitialPopulation() and
    gas better handling of RPL's
    
    void makeInitialPopulation() {
        // loop over sd.plants creating a real plant, giving it alleles
        Plant.id_no = sd.plants.size();
        Iterator it = sd.plants.iterator();
        mainloop:
        while (it.hasNext()) {
            Plant pl = (Plant) it.next();
            Plant ourplant = new Plant(pl.allele.length, -1 * pl.dob,
                    pl.location);
            ourplant.parent1 = ourplant.parent2 = pl.my_id_no;
            ourplant.female = pl.female;
            ourplant.founder = true;
            // NEW on 2 April 2006
            // If this location is already occupied, ignore this founder
            // and print a message for the user
            Iterator popit = population.iterator();
            while (popit.hasNext()) {
                Plant pp = (Plant) popit.next();
                if (pp.location.isequal(ourplant.location)) {
                    System.out.println("You've got two founders at one location.\nI'll ignore the second one. location ="
                            + ourplant.location.toString() + "\n");
                    continue mainloop;
                }
            }
            // pick the alleles for our plant
            // loop to tell each locus the value of F
            // 29july2009 should this have already been done? Hmmm
            Iterator li = sd.loci.iterator();
            while (li.hasNext()) {
                Locus l = (Locus) li.next();
                l.setF(sd.foundersf);
            }
            for (int loc = 0; loc < sd.loci.size(); loc++) {
                int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                for (int a = 0; a < 2; a++) {
                    ourplant.allele[loc][a] = genes[a];
                }
            }

            population.add(ourplant);
        }
        // now juy2011 loop over random founders
        // getting a list of plants for each RPL in the randomfounders
        // give them alleles and
        // add them to the population
        // new java notation too, you'll note.
        // 29july2009 should this have already been done? Hmmm
        Iterator li = sd.loci.iterator();
        while (li.hasNext()) {
            Locus l = (Locus) li.next();
            l.setF(sd.foundersf);
        }
       
        // new
        ArrayList<RandomPlantList> runsfounders = (ArrayList<RandomPlantList>) sd.randomfounders.clone();
        while (runsfounders.size() > 0) {
            ArrayList<RandomPlantList> temp = new ArrayList<RandomPlantList>();
            it = runsfounders.iterator();

            RandomPlantList current = (RandomPlantList) it.next();
            temp.add(current);
            it.remove();
            while (it.hasNext()) {
                RandomPlantList tl = (RandomPlantList) it.next();
                if (current.sameRect(tl)) {
                    temp.add(tl);
                    it.remove();
                }
            }
            // process temp that now has all the requests for
            //a given regtangle
            int numberplants = 0;
            Iterator us = temp.iterator();
            while (us.hasNext()) {
                RandomPlantList tl = (RandomPlantList) us.next();
                numberplants += tl.number;
            }
            int tempnum = current.number;
            current.number = numberplants;
            Founders f = new Founders(sd, current);
            //request total number of plants needed in this rectangle
    
            ArrayList<Plant> recruits = f.calculate();
            current.number = tempnum;
            int recnumber = 0;
            us = temp.iterator();
            while (us.hasNext()) {
                RandomPlantList tl = (RandomPlantList) us.next();
                // now fix up enought plants to fill the order in tl
                if (tl.numberfemale >= 0) {
                    for (int count = 0; count < tl.numberfemale; count++) {
                        // make females
                        Plant p = recruits.get(recnumber++);
                        p.founder = true;
                        // select alleles
                        for (int loc = 0; loc < sd.loci.size(); loc++) {
                            int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                            for (int a = 0; a < 2; a++) {
                                p.allele[loc][a] = genes[a];
                            }
                        }
                        p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);

                        p.female = true;
                        population.add(p);

                    }
                    for (int count = tl.numberfemale; count < tl.number; count++) {
                        // make males
                        Plant p = recruits.get(recnumber++);
                        p.founder = true;
                        // select alleles
                        for (int loc = 0; loc < sd.loci.size(); loc++) {
                            int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                            for (int a = 0; a < 2; a++) {
                                p.allele[loc][a] = genes[a];
                            }
                        }
                        p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);
                        p.female = false;
                        population.add(p);

                    }
                } else {
                    for (int count = 0; count < tl.number; count++) {
                        // make random sex
                        Plant p = recruits.get(recnumber++);
                        p.founder = true;
                        // select alleles
                        for (int loc = 0; loc < sd.loci.size(); loc++) {
                            int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                            for (int a = 0; a < 2; a++) {
                                p.allele[loc][a] = genes[a];
                            }
                        }
                        p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);
                        boolean oursex = false;
                        if (rv.Uniform(0, 1, 1)[0] < tl.probfemale) {
                            oursex = true;
                        }
                        p.female = oursex;
                        population.add(p);

                    }
                }

            }

        }


    }

    */
    
    
    void makeInitialPopulation() {

        // loop over sd.plants creating a real plant, giving it alleles
        Plant.id_no = sd.plants.size();
        Iterator<Plant> it = sd.plants.iterator();
        mainloop:
        while (it.hasNext()) {
            Plant pl = (Plant) it.next();
            Plant ourplant = new Plant(pl.allele.length, -1 * pl.dob,
                    pl.location);
            ourplant.parent1 = ourplant.parent2 = pl.my_id_no;
            ourplant.female = pl.female;
            ourplant.founder = true;
            // NEW on 2 April 2006
            // If this location is already occupied, ignore this founder
            // and print a message for the user
            Iterator popit = population.iterator();
            while (popit.hasNext()) {
                Plant pp = (Plant) popit.next();
                if (pp.location.isequal(ourplant.location)) {
                    System.out.println("You've got two non-random founders at one location.\nI'll ignore the second one. location ="
                            + ourplant.location.toString() + "\n");
                    continue mainloop;
                }
            }
            // pick the alleles for our plant
            // loop to tell each locus the value of F
            // 29july2009 should this have already been done? Hmmm
            // Iterator li = sd.loci.iterator();
            // while (li.hasNext()) {
            //     Locus l = (Locus) li.next();
            //     l.setF(sd.foundersf);
            // }
            for (int loc = 0; loc < sd.loci.size(); loc++) {
                int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                for (int a = 0; a < 2; a++) {
                    ourplant.allele[loc][a] = genes[a];
                }
            }
            //29 June 2014 InbreedinDepression calculate excesshomozygosity
            if(INBREEDING) ourplant.calculateExcessHomozygosity(sd);
            
            if (sd.region.inRegion(ourplant.location.X, ourplant.location.Y)) {
                population.add(ourplant);
            } //FEB 2014 else if onMap but not InRegion find the DYNAMIC that
            //contains the location make it viable add to the population as well
            //we'll let user force founders into non-viable sites
            //in a random rectangle?
            else {
                if (sd.region.onMap(ourplant.location)) {
                    NewRectangle r = sd.region.NewRectangleContaining(ourplant.location);
                    assert r != null : "null rectangle returned from NewRegion.NewRectangleContaining()";
                    assert r.ourType == RECTANGLETYPE.DYNAMIC : "Non DYNAMIC rectangle from NewRectangleContaining()";
                    r.addLocation(ourplant.location, 0);
                    population.add(ourplant);
                }

            }
        }

        /*
         * plants generated this way are marked as founders but dont have
         * identifiable parents (as InitialPopulation specified Plant s
         *
         * 22 Sept 2011 now we treat RPL's in groups according to whether they
         * have identical rectangles. If not, they better be disjoint!!!
         */
        // new 2013 to allow for SparseRects
        /*
         * for each RandomPlantList
         * for each produced by founders.calculate
         *   give it alleles
         * FEB 2014 RANDOM PLANT LISTS IN DYNAMIC RECTANGLES AREN'T ALLOWED
         SINCE THEY WOn"T WORK RIGHT AND WE HAVEN'T YET ARRANGED TO PICK RANDOM
         LOCATIONS FOR FOUNDERS FROM A DYNAMIC RECTANGLE
        
        
        6 June 2014 Need to return to the previous (NG 2) version for
        RPL's where we assemble all the requests for RPL's with the same
        rectangle and handle them alll at once to avoid clashes.
         */
        /****WAS RUNNING UNTIL JUNE 7****
        for (RandomPlantList rpl : sd.randomfounders) {
            Founders f = new Founders(sd, rpl);
            ArrayList<Plant> recruits = f.calculate();
            boolean uniquelocation = true;
            for (Plant plant : recruits) {
                plant.founder = true;
                // select alleles
                for (int loc = 0; loc < sd.loci.size(); loc++) {
                    int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                    for (int a = 0; a < 2; a++) {
                        plant.allele[loc][a] = genes[a];
                    }
                }
                plant.dob = -1 * rv.RandomInt(rpl.agelo, rpl.agehi);

                //test to see if this location is occupied
                uniquelocation = true;
                for (Plant olderplant : population) {

                    if (olderplant.location.isequal(plant.location)) {
                        System.out.println("You've got two founders at one location.\nI'll ignore the second one. location ="
                                + plant.location.toString() + "\n");
                        uniquelocation = false;

                    }
                }

                if (uniquelocation) {
                    population.add(plant);
                }

            }
        }
        * *******/
        
         ArrayList<RandomPlantList> runsfounders = (ArrayList<RandomPlantList>) sd.randomfounders.clone();
         while (runsfounders.size() > 0) {//keep working until RPL's have been handled
            ArrayList<RandomPlantList> temp = new ArrayList<RandomPlantList>();
            Iterator<RandomPlantList> RPit = runsfounders.iterator();

            //Take the next RPL move it to temp
            RandomPlantList current = (RandomPlantList) RPit.next();
            
            
            temp.add(current);
            RPit.remove();
            //Read through runsfounders and move all other RPL's 
            //with the same rectangle to temp as well
            while (RPit.hasNext()) {
                RandomPlantList tl = (RandomPlantList) RPit.next();
                if (current.sameRect(tl)) {
                    temp.add(tl);
                    RPit.remove();
                }
            }
            
            
              
         
         // process temp
         //determine the number of plants requested in temp
         //all of which lie in the same rectangle   
         int numberplants = 0;
         Iterator<RandomPlantList> us = temp.iterator();
         while (us.hasNext()) {
            RandomPlantList tl = (RandomPlantList) us.next();
            numberplants += tl.number;
         }
         //save the number requested in current
         int tempnum = current.number;
         
         //and adjust current to request the total number we need
         current.number = numberplants;
         Founders f = new Founders(sd, current);
//FOunders actually does more than we need, generating sex, age etc
         //which we'll need to do again for each RPL
         //BETTER would be to use a NewRectangle and get a list
         //of Locations, then create the Plants once here.
         ArrayList<Plant> recruits = f.calculate();
         if(recruits.size()< current.number)
         {
             System.out.println("Problem in run number "+sd.run_number+" ");
             System.out.println("Can not create all founders requested in this RandomPlantList");
             System.out.println(current.toXML());
             continue;
         }
         //restore the details of current
         current.number = tempnum;
         
         
         int recnumber = 0;
         us = temp.iterator();
         //now process recruits fill orders for all the RPLS in temp
         while (us.hasNext()) {
            RandomPlantList tl = (RandomPlantList) us.next();
            // now fix up enought plants to fill the order in tl
            if (tl.numberfemale >= 0) {//specific number females
                for (int count = 0; count < tl.numberfemale; count++) {
                    // make females
                    Plant p = recruits.get(recnumber++);
                    p.founder = true;
                    // select alleles
                    for (int loc = 0; loc < sd.loci.size(); loc++) {
                        int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                        for (int a = 0; a < 2; a++) {
                            p.allele[loc][a] = genes[a];
                        }
                    }
                    //29 June 2014 InbreedinDepression calculate excesshomozygosity
                     if(INBREEDING) p.calculateExcessHomozygosity(sd);
                  
                     p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);

                    p.female = true;
                    population.add(p);

            }//done making females
                for (int count = tl.numberfemale; count < tl.number; count++) {
                    // make males
                    Plant p = recruits.get(recnumber++);
                    p.founder = true;
                    // select alleles
                    for (int loc = 0; loc < sd.loci.size(); loc++) {
                        int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                        for (int a = 0; a < 2; a++) {
                            p.allele[loc][a] = genes[a];
                        }
                    }
                    //29 June 2014 InbreedinDepression calculate excesshomozygosity
                      if(INBREEDING) p.calculateExcessHomozygosity(sd);
                      
                    p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);
                    p.female = false;
                    population.add(p);

                }
         } 
            else 
            {//flip coin for sex of plants
                for (int count = 0; count < tl.number; count++) {
                // make random sex
                Plant p = recruits.get(recnumber++);
                p.founder = true;
                // select alleles
                for (int loc = 0; loc < sd.loci.size(); loc++) {
                    int[] genes = ((Locus) sd.loci.get(loc)).pickGenotype(rnum);
                    for (int a = 0; a < 2; a++) {
                        p.allele[loc][a] = genes[a];
                    }
                }
                //29 June 2014 InbreedinDepression calculate excesshomozygosity
                  if(INBREEDING) p.calculateExcessHomozygosity(sd);
                  
                p.dob = -1 * rv.RandomInt(tl.agelo, tl.agehi);
                boolean oursex = false;
                if (rv.Uniform(0, 1, 1)[0] < tl.probfemale) {
                    oursex = true;
                }
                p.female = oursex;
                population.add(p);

                }
         }//flipping coin for sex

         }//processing recruits to fill the order od temp's RPLs

         }//random plant list runsfounders
         

    }//makeInitialPopulation

    /**
     * Create a string to print list the whole population
     *
     * @return the string
     */
    public String showPopulation() {
        Iterator<Plant> it;
        StringBuffer sb = null;
        if (population.size() > 0) {
            Plant temp = population.get(0);
            sb = new StringBuffer(temp.CSVHeader());
            it = population.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                sb.append(p.toCSVString(sd.run_number));
            }
        }

        if (expopulation.size() > 0) {
            if (sb == null) {
                Plant temp = expopulation.get(0);
                sb = new StringBuffer(temp.CSVHeader());
            }
            it = expopulation.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                sb.append(p.toCSVString(sd.run_number));
            }
        }
        if (sb != null) {
            return sb.toString();
        } else {
            return "";
        }
    }

    public void showPopulation(PrintWriter pw) {
        Iterator<Plant> it;
        StringBuffer sb = null;
        if (population.size() > 0) {
            Plant temp = population.get(0);
            pw.print(temp.CSVHeader());
            // sb = new StringBuffer(temp.CSVHeader());
            it = population.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                pw.print(p.toCSVString(sd.run_number));
                // sb.append(p.toCSVString(sd.run_number));
            }
        }
        if (expopulation.size() > 0) {
            if (sb == null) {
                Plant temp = expopulation.get(0);
                // sb = new StringBuffer(temp.CSVHeader());
                pw.print(temp.CSVHeader());
            }
            it = expopulation.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                // sb.append(p.toCSVString(sd.run_number));
                pw.print(p.toCSVString(sd.run_number));
            }
        }

    }

    public void showPopulation(PrintStream pw) {
        Iterator<Plant> it;
        StringBuffer sb = null;
        if (population.size() > 0) {
            Plant temp = population.get(0);
            pw.print(temp.CSVHeader());
            // sb = new StringBuffer(temp.CSVHeader());
            it = population.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                pw.print(p.toCSVString(sd.run_number));
                // sb.append(p.toCSVString(sd.run_number));
            }
        }
        if (expopulation.size() > 0) {
            if (sb == null) {
                Plant temp = expopulation.get(0);
                // sb = new StringBuffer(temp.CSVHeader());
                pw.print(temp.CSVHeader());
            }
            it = expopulation.iterator();
            while (it.hasNext()) {
                Plant p = it.next();
                // sb.append(p.toCSVString(sd.run_number));
                pw.print(p.toCSVString(sd.run_number));
            }
        }

    }

    /**
     *
     * @param a --- and array
     * @return --- an integer indexing a random member of the array SHould be
     * inline now
     */
    //  int random_member(ArrayList a) {
    //      return (int) Math.floor((a.size()) * rnum.nextDouble());
    //  }
    /**
     * lastYear() implements the next round of breeding
     *
     */
    ArrayList<Plant> lastYear() {
        ArrayList<Plant> maternal = new ArrayList<Plant>();
        //int[] plantcounts = new int[6];
        //seeds to generate,non-pollination,off-plot,added to seed bank
        //out competed,immigrants
        Iterator<Plant> it;
        // loop over everyone remaining to generate tentative list of offspring
        ArrayList<Plant> templist = new ArrayList<Plant>();
        //ArrayList<Plant> tempseedlist = new ArrayList<Plant>();
        it = population.iterator();

        int loopcounter = 0;
        loop:
        while (it.hasNext()) {
            Plant fparent = it.next(); // pick a female parent

            if (sd.SeedCollectionsubregions_only) {
                //if fparent isn't in a summary region

                int sr = sd.summaryregions.whichSummaryRegion(fparent.location.X, fparent.location.Y);

                if (sr == -1) {
                    continue loop;
                }
                if(sd.dioeceous==true && fparent.female==false) continue loop;
            }

            Plant mparent = null;
            /* if (DEBUG) {
             loopcounter++;
             if (loopcounter % 1000 == 0) {
             System.out.println("nextYear() loop: pop="
             + population.size() + " seedbank=" + seedbank.size()
             + "loopcounter=" + loopcounter);
             }
             }*/
            // if we are dioecious, only look at females
            if (sd.dioeceous && !fparent.female) {
                continue loop;
            }
            /*
             * DETERMINE HOW MANY SEEDS TO PRODUCE
             */
            // How many offspring (fruit) should this parent produce?
            // The number of offspring is determined by the age of the
            // (female) parent
            int age = sd.current_year - fparent.dob;

            int n_offspring; 

                 if (sd.off_dist_method.equals("bracket")) {
                double fp = sd.SeedCollectionlast_r*sd.fruit_production.value(age);
                int a = (int) Math.floor(fp);
                if ((double) a == fp) {
                    n_offspring = a;
                } else {
                    int b = a + 1;
                    if (rnum.nextDouble() < (double) b - fp) {
                        n_offspring = a;
                    } else {
                        n_offspring = b;
                    }
                }
            } else if (sd.off_dist_method.equals("poisson")) {
                double fp = sd.SeedCollectionlast_r*sd.fruit_production.value(age);
                n_offspring = (int) rv.Poisson(fp, 1)[0];

            } else // i.e. "round" or unspecified
            {
                n_offspring = (int) Math.round(sd.SeedCollectionlast_r*sd.fruit_production.value(age));
            }
              
            //DEBUG TRACE
            // plantcounts[0] += n_offspring;
            /*
             * END DETERMINE HOW MANY SEEDS TO PRODUCE
             */
            // NEW POLLEN PROCEDURES 7 Apr 2006
/*
             * SET UP MECHANISM FOR CHOOSING POLLEN SOURCES
             */
            int number_distance_ranges = sd.pollen_distances.length + 1;
            DPD[] DPDs = new DPD[number_distance_ranges];
            for (int i = 0; i < DPDs.length; i++) {
                DPDs[i] = new DPD();
            }

            // loop over population to put each potential pollen source in the
            // correct DPD
            Iterator<Plant> popit = population.iterator();
            pollensourceloop:
            while (popit.hasNext()) {
                Plant pp = (Plant) popit.next();

                // CHECK IF WE WANT TO CONSIDER THIS PP BASED ON SEX, RANDOM
                // MATING ETC
                // find to which distance range this pp belongs
                float ourDist = pp.location.dist(fparent.location);
                int distance_class = locateClass(ourDist, sd.pollen_distances);
                // continue loop if distance_class is too big
                // look up the age-dependent (relative) chance of being
                // a pollen donor for thie potential parent pp

                //MARCH 2014 POLLEN DIRECTION
                if (sd.usePollenDirection) {
                    //find direction from pp to maternal parent 
                    float bearing = fparent.location.bearingTo(pp.location);

                    //if the chance of pollen moving FROM this bearing small..
                    if (rv.Uniform(0.0, 1.0, 1)[0] > sd.PollenDirection.value(bearing)) {
                        //don't include pp in the lottery
                        continue pollensourceloop;
                    }
                }
                //End MARCH 2014 POLLEN DIRECTION
                float prob = (float) sd.pollen_production.value(sd.current_year
                        - pp.dob);
                if (prob > 0) // add to correct DPD
                {
                    if (pp.my_id_no == fparent.my_id_no && sd.random_mating) {
                        DPDs[distance_class].addPoint(prob, pp);
                    }
                    if (pp.my_id_no != fparent.my_id_no)// enforce outcrossing
                    {
                        if (sd.dioeceous && !pp.female) {
                            DPDs[distance_class].addPoint(prob, pp);
                        } else {
                            DPDs[distance_class].addPoint(prob, pp);
                        }
                    }
                }
            }
            // NORMALIZE THE DPDs that are nonempty
            for (int i = 0; i < DPDs.length; i++) {
                // new
                // didn't test before we didn't test before, just made DPD
                // do some specific normalization if it was empty
                if (DPDs[i].number_points() > 0) {
                    DPDs[i].normalize();
                }
            }
            /*
             * END SET UP MECHANISM FOR CHOOSING POLLEN SOURCES
             */

            // ACTUALLY GENERATE POTENTIAL OFFSPRING, RESELECTING A POLLEN
            // SOURCE FOR EACH seed generated
            //THOUGH WE COOULD REUSE POLLEN SOURCES TO MODEL NONRANDOMNESS
           /* if (DEBUG) {
             if (loopcounter % 1000 == 0) {
             System.out.println("nextYear() offloop: population="
             + population.size());
             }
             //System.out.print(""+loopcounter +","+n_offspring+",");
             }*/
            //maternal.add(fparent);
            int offspringcount=0;
            offloop:
            for (int offsp = 0; offsp < n_offspring; offsp++) {
                // System.out.print(""+offsp+"of"+n_offspring+" to generate. ID="+fparent.my_id_no+",");
                mparent = null;
                 if(sd.Agamospermy_method.equals("all") && rnum.nextDouble() < sd.Agamospermy_probability)
                {
                    System.out.println("all agamospermy");
                     Plant off = fparent.apomixis(sd);
                            //pic k a location
                            off.location = chooseLocation(fparent.location,
                                    (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                            if (!sd.region.onMap(off.location)) {
                              

                                // System.out.print("OffPlot in facultative agamospermy\n");
                                //plantcounts[2]++;
                                continue offloop;
                            }
                            seedbank.add(off);
                            offspringcount++;
                }
                // outcross or selfing --- pick alleles for offspring

                // HERE WE SELECT THE POLLEN SOURCE ***NEW***
                if (sd.dioeceous || (rnum.nextDouble() > sd.selfing_rate)) // force  an outcross
                {
                    // DETERMINE A DISTANCE CLASS AND THEN PICK A mparent
                    int dist_class = (int) Math.floor(sd.pollen_distance_probabilities.pickOne(rnum.nextDouble()));
                    if (DEBUG) {
                        assert DPDs[dist_class] != null : "null prob dist in nextYear()!";
                    }
                    if (DPDs[dist_class].number_points() <= 0) {
                         //System.out.print("pollination failed\n");
//INCLUDE AGAMOSPERMY  i
                         if(sd.Agamospermy_method.equals("faculatative") && rnum.nextDouble() < sd.Agamospermy_probability) {
                            System.out.println("facultative agamospermy");
                            Plant off = fparent.apomixis(sd);
                            //pic k a location
                            off.location = chooseLocation(fparent.location,
                                    (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                            if (!sd.region.onMap(off.location)) {
                              

                                // System.out.print("OffPlot in facultative agamospermy\n");
                                //plantcounts[2]++;
                                continue offloop;
                            }
                            seedbank.add(off);
                            offspringcount++;
                        }
                        
                        continue offloop;
                    }
                    mparent = (Plant) DPDs[dist_class].pickOne(rnum.nextDouble());
                    // END NEW POLLEN PROCEDURE
                } else// self
                {
                    mparent = fparent;
                }
                // DONE SELECTING mparent ***END NEW***
                if (DEBUG) {
                    assert mparent != null : "mparent == null in nextYear()";
                }
                if (mparent == null) {
                    continue offloop; // shouldn't ever need to do this
                }
                //CONSTRUCT AN OFFSPRING
                Plant offspring = new Plant(sd.loci.size());
                offspring.parent1 = fparent.my_id_no;
                offspring.parent2 = mparent.my_id_no;

                offspring.doc = sd.current_year;
                offspring.female = rnum.nextBoolean();

                // NEW LOCATION SELECTION
                offspring.location = chooseLocation(fparent.location,
                        (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                //System.out.println("offspring ID="+offspring.my_id_no);
                /*if (!sd.region.onMap(offspring.location)) {
                 ///if (!sd.region.inRegion((float) offspring.location.X,
                 //       (float) offspring.location.Y)) {

                 // System.out.print("OffPlot\n");
                   
                 continue offloop;
                 }
                 */
                // pick alleles for offspring
                // loop over loci
                for (int loc = 0; loc < sd.loci.size(); loc++) {

                    boolean choice = rnum.nextBoolean();
                    int ac;// fparent allele to choose
                    if (choice) {
                        ac = 0;
                    } else {
                        ac = 1;
                    }
                    offspring.allele[loc][0] = fparent.allele[loc][ac];

                    choice = rnum.nextBoolean();

                    if (choice) {
                        ac = 0;
                    } else {
                        ac = 1;
                    }
                    offspring.allele[loc][1] = mparent.allele[loc][ac];
                }
                /* DEC 14  2013 HERE IS THE COMPATIBILITY CHECK
                 * If male mparent has a compatibility_locus allele that is also
                 * in fparent we flip a coin to see if we return
                 * to the top of the loop
                
                 SHOULDN'T WE JUST LOOK AT THE ALLEL IN THE GAMETE? WE
                 DO!
                 */
                if (sd.compatibility) {
                    int cl = sd.compatibility_locus;
                    //offspring.allele[cl][1] is from mparent
                    if (offspring.allele[cl][1] == fparent.allele[cl][0]
                            || offspring.allele[cl][1] == fparent.allele[cl][1]) { //flip a coin
                        // if random(0,1)<= sd.compatibility fraction continue offloop 
                        if (rnum.nextDouble() <= sd.compatibility_fraction) {
                            continue offloop;
                        }
                    }
                }

                // add the offspring to the seedbank
                seedbank.add(offspring);
                offspringcount++;
                // System.out.println("Seedbank: "+seedbank.size());

            }// offspring loop
            if(offspringcount>0)maternal.add(fparent);
            /*if (DEBUG) {
             if (loopcounter % 1000 == 0) {
             System.out.println("end of offloop pop="
             + population.size() + "temp=" + templist.size());
             }
             }*/
        }// loop over all parents
        //NO IMMIGRATION

        /*
         * THIS IS THE PLACE FOR MORTALITY CALCULATIONS
         * seeds from this year have dob=current year and
         * all others have age=current-year-dob>=1
         * 
         * Plants that resulted from recruitment last year will have
         * age==currentyear-dob == 0
         * and won't have had a change to reporduce yet
         */
        // THIS iS THE NEW LOCATION FOR MORTALITY OF PARENTAL GENERATION
        // Everyone currently in the population
        // gets killed at age specific rates
        // NEW SEED STUFF
        // DO MORTALITY IN THE SEED BANK
//NOW PICK POTENTIAL RECRUITS FROM THE SEEBANK
        //make templist empty
        //iterate over seedbank
        //compare to germination rate
        //if germinate, remove from seedbank and put in templist
        /**
         * NEW METHOD for implementing competition We only consider potential
         * recruits that aren't at an occupied location. a little faster in a
         * test with high population density
         */
        // Loop over all new plants to remove any near a parent
        // for each offspring we iterate through population
        // perhaps ending early
        /**
         * *******************************************
         */
        // Randomize the order of the offspring
        //AND NOW ONLY SELECT NEW RECRUITS THAT ARE AT
        //UNOCCUPIED LOCATIONS
        // Remove the first offspring to the population
        // (if we don't kill it) NOW WE DON'T KILL OFF age==0 HERE
        // and then remove any offspring close to it
        return maternal;

    }

    void nextYear() {
        int[] plantcounts = new int[6];
        //seeds to generate,non-pollination,off-plot,added to seed bank
        //out competed,immigrants
        Iterator<Plant> it;
        // loop over everyone remaining to generate tentative list of offspring
        ArrayList<Plant> templist = new ArrayList<Plant>();
        //ArrayList<Plant> tempseedlist = new ArrayList<Plant>();
        it = population.iterator();

        int loopcounter = 0;
        loop:
        while (it.hasNext()) {
            Plant fparent = it.next(); // pick a female parent
            Plant mparent = null;
            /* if (DEBUG) {
             loopcounter++;
             if (loopcounter % 1000 == 0) {
             System.out.println("nextYear() loop: pop="
             + population.size() + " seedbank=" + seedbank.size()
             + "loopcounter=" + loopcounter);
             }
             }*/
            // if we are dioecious, only look at females
            if (sd.dioeceous && !fparent.female) {
                continue loop;
            }
            /*
             * DETERMINE HOW MANY SEEDS TO PRODUCE
             */
            // How many offspring (fruit) should this parent produce?
            // The number of offspring is determined by the age of the
            // (female) parent
            int age = sd.current_year - fparent.dob;

            int n_offspring = 0;
            double fp = sd.fruit_production.value(age);
            
            
             if(INBREEDING)
            {
                if(sd.IBD_use && sd.IBD_use__reproduction)
                {
                    fp*= sd.IBD_ReproductionEffect.value(fparent.excesshomozygosity);
                }
            }
            
            
            // THE NEXT LINES ALLOW NEW METHODS
            if (sd.off_dist_method.equals("bracket")) {
               // double fp = sd.fruit_production.value(age);
                int a = (int) Math.floor(fp);
                if ((double) a == fp) {
                    n_offspring = a;
                } else {
                    int b = a + 1;
                    if (rnum.nextDouble() < (double) b - fp) {
                        n_offspring = a;
                    } else {
                        n_offspring = b;
                    }
                }
            } else if (sd.off_dist_method.equals("poisson")) {
               // double fp = sd.fruit_production.value(age);
                n_offspring = (int) rv.Poisson(fp, 1)[0];

            } else // i.e. "round" or unspecified
            {
                n_offspring = (int) Math.round(fp);
            }
            
           
            
            //DEBUG TRACE
            plantcounts[0] += n_offspring;
            /*
             * END DETERMINE HOW MANY SEEDS TO PRODUCE
             */

            // NEW POLLEN PROCEDURES 7 Apr 2006
/*
             * SET UP MECHANISM FOR CHOOSING POLLEN SOURCES
             */
            int number_distance_ranges = sd.pollen_distances.length + 1;
            DPD[] DPDs = new DPD[number_distance_ranges];
            for (int i = 0; i < DPDs.length; i++) {
                DPDs[i] = new DPD();
            }

            // loop over population to put each potential pollen source in the
            // correct DPD
            Iterator<Plant> popit = population.iterator();
            pollensourceloop:
            while (popit.hasNext()) {
                Plant pp = (Plant) popit.next();

                // CHECK IF WE WANT TO CONSIDER THIS PP BASED ON SEX, RANDOM
                // MATING ETC
                // find to which distance range this pp belongs
                float ourDist = pp.location.dist(fparent.location);
                int distance_class = locateClass(ourDist, sd.pollen_distances);
                // continue loop if distance_class is too big
                // look up the age-dependent (relative) chance of being
                // a pollen donor for thie potential parent pp

                //MARCH 2014 POLLEN DIRECTION
                if (sd.usePollenDirection) {
                    //find direction from pp to maternal parent 
                    float bearing = fparent.location.bearingTo(pp.location);

                    //if the chance of pollen moving FROM this bearing small..
                    if (rv.Uniform(0.0, 1.0, 1)[0] > sd.PollenDirection.value(bearing)) {
                        //don't include pp in the lottery
                        continue pollensourceloop;
                    }
                }
                //End MARCH 2014 POLLEN DIRECTION
                float prob = (float) sd.pollen_production.value(sd.current_year
                        - pp.dob);
                if (prob > 0) // add to correct DPD
                {
                    if (pp.my_id_no == fparent.my_id_no && sd.random_mating) {
                        DPDs[distance_class].addPoint(prob, pp);
                    }
                    if (pp.my_id_no != fparent.my_id_no)// enforce outcrossing
                    {
                        if (sd.dioeceous && !pp.female) {
                            DPDs[distance_class].addPoint(prob, pp);
                        } else {
                            DPDs[distance_class].addPoint(prob, pp);
                        }
                    }
                }
            }
            // NORMALIZE THE DPDs that are nonempty
            for (int i = 0; i < DPDs.length; i++) {
                // new
                // didn't test before we didn't test before, just made DPD
                // do some specific normalization if it was empty
                if (DPDs[i].number_points() > 0) {
                    DPDs[i].normalize();
                }
            }
            /*
             * END SET UP MECHANISM FOR CHOOSING POLLEN SOURCES
             */

            // ACTUALLY GENERATE POTENTIAL OFFSPRING, RESELECTING A POLLEN
            // SOURCE FOR EACH seed generated
            //THOUGH WE COOULD REUSE POLLEN SOURCES TO MODEL NONRANDOMNESS
           /* if (DEBUG) {
             if (loopcounter % 1000 == 0) {
             System.out.println("nextYear() offloop: population="
             + population.size());
             }
             //System.out.print(""+loopcounter +","+n_offspring+",");
             }*/
            offloop:
            for (int offsp = 0; offsp < n_offspring; offsp++) {
                // System.out.print("parent "+loopcounter +" off to generate,"+n_offspring+"this try "+offsp+",");
                mparent = null;
                // outcross or selfing --- pick alleles for offspring
               
                //if we're doing all agamospermy, we can test here and do it if needed
                if(sd.Agamospermy_method.equals("all") && rnum.nextDouble() < sd.Agamospermy_probability)
                {
                    System.out.println("all agamospermy");
                     Plant off = fparent.apomixis(sd);
                            //pic k a location
                            off.location = chooseLocation(fparent.location,
                                    (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                            if (!sd.region.onMap(off.location)) {
                              

                                // System.out.print("OffPlot in facultative agamospermy\n");
                                plantcounts[2]++;
                                continue offloop;
                            }
                            seedbank.add(off);
                }
                
                 // outcross or selfing --- pick alleles for offspring
                // HERE WE SELECT THE POLLEN SOURCE ***NEW***
                if (sd.dioeceous || (rnum.nextDouble() > sd.selfing_rate)) // force  an outcross
                {
                    // DETERMINE A DISTANCE CLASS AND THEN PICK A mparent
                    int dist_class = (int) Math.floor(sd.pollen_distance_probabilities.pickOne(rnum.nextDouble()));
                    if (DEBUG) {
                        assert DPDs[dist_class] != null : "null prob dist in nextYear()!";
                    }
                    if (DPDs[dist_class].number_points() <= 0) {
                        // System.out.print("pollination failed, trying facultative Agamospermy\n");
                        /*18 May consider Agamospermy, do it if needed
                         oer continue the offloop*/
                        if (sd.Agamospermy_method.equals("faculatative") && rnum.nextDouble() < sd.Agamospermy_probability) {
                            System.out.println("facultative agamospermy");
                            Plant off = fparent.apomixis(sd);
                            //pic k a location
                            off.location = chooseLocation(fparent.location,
                                    (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                            if (!sd.region.onMap(off.location)) {
                              

                                // System.out.print("OffPlot in facultative agamospermy\n");
                                plantcounts[2]++;
                                continue offloop;
                            }
                            seedbank.add(off);
                        }
                      
                        continue offloop;
                    }
                    mparent = (Plant) DPDs[dist_class].pickOne(rnum.nextDouble());
                    // END NEW POLLEN PROCEDURE
                } else// selfing
                {
                    mparent = fparent;
                }
                // DONE SELECTING mparent ***END NEW***
                if (DEBUG) {
                    assert mparent != null : "mparent == null in nextYear()";
                }
                if (mparent == null) {
                    continue offloop; // shouldn't ever need to do this
                }
                //CONSTRUCT AN OFFSPRING
                Plant offspring = new Plant(sd.loci.size());
                offspring.parent1 = fparent.my_id_no;
                offspring.parent2 = mparent.my_id_no;

                offspring.doc = sd.current_year;
                offspring.female = rnum.nextBoolean();

                // NEW LOCATION SELECTION
                offspring.location = chooseLocation(fparent.location,
                        (int) Math.floor(sd.dispersal_distribution.value(rnum.nextDouble())));

                if (!sd.region.onMap(offspring.location)) {
                ///if (!sd.region.inRegion((float) offspring.location.X,
                    //       (float) offspring.location.Y)) {

                    // System.out.print("OffPlot\n");
                    plantcounts[2]++;
                    continue offloop;
                }
                // pick alleles for offspring
                // loop over loci
                for (int loc = 0; loc < sd.loci.size(); loc++) {

                    boolean choice = rnum.nextBoolean();
                    int ac;// fparent allele to choose
                    if (choice) {
                        ac = 0;
                    } else {
                        ac = 1;
                    }
                    offspring.allele[loc][0] = fparent.allele[loc][ac];

                    choice = rnum.nextBoolean();

                    if (choice) {
                        ac = 0;
                    } else {
                        ac = 1;
                    }
                    offspring.allele[loc][1] = mparent.allele[loc][ac];
                }
                if(INBREEDING) offspring.calculateExcessHomozygosity(sd);
                /* DEC 14  2013 HERE IS THE COMPATIBILITY CHECK
                 * If male mparent has a compatibility_locus allele that is also
                 * in fparent we flip a coin to see if we return
                 * to the top of the loop
                
                 SHOULDN'T WE JUST LOOK AT THE ALLELE IN THE GAMETE? WE
                 DO!
                 */
                if (sd.compatibility) {
                    int cl = sd.compatibility_locus;
                    //offspring.allele[cl][1] is from mparent
                    if (offspring.allele[cl][1] == fparent.allele[cl][0]
                            || offspring.allele[cl][1] == fparent.allele[cl][1]) { //flip a coin
                        // if random(0,1)<= sd.compatibility fraction continue offloop 
                        if (rnum.nextDouble() <= sd.compatibility_fraction) {
                            continue offloop;
                        }
                    }
                }

                // add the offspring to the seedbank
                seedbank.add(offspring);
                // System.out.println("Seedbank: "+seedbank.size());
                plantcounts[3]++;
            }// offspring loop

            /*if (DEBUG) {
             if (loopcounter % 1000 == 0) {
             System.out.println("end of offloop pop="
             + population.size() + "temp=" + templist.size());
             }
             }*/
        }// loop over all parents
        //IMMIGRATION
        if (sd.ImmigrationUse) {
            //how many immigrants
            int number_immigrants = (int) this.rv.Poisson(sd.ImmigrationRate, 1)[0];
            for (int im = 0; im < number_immigrants; im++) {
                //create a Plant
                Plant p = new Plant(sd.number_loci);
                p.parent1 = -1;
                p.parent2 = -1;
                p.doc = sd.current_year;
                //get genotype
                Iterator<Locus> li = sd.loci.iterator();
                while (li.hasNext()) {
                    Locus l = li.next();
                    l.setF(sd.foundersf);
                }
                for (int loc = 0; loc < sd.loci.size(); loc++) {
                    int[] genes = (sd.loci.get(loc)).pickGenotype(rnum);
                    for (int a = 0; a < 2; a++) {
                        p.allele[loc][a] = genes[a];
                    }
                }
                  if(INBREEDING) p.calculateExcessHomozygosity(sd);
                //get a sex
                if (rv.Uniform(0, 1, 1)[0] < sd.ImmigrationPFemale) {
                    p.female = true;
                } else {
                    p.female = false;
                }
                //assign a location
                int[] coords = sd.Immigration.randomPoint(rv);
                p.location = new Location(coords[0], coords[1]);
                //add to seedbank if there's a chance it will be viable
                //i.e. in region or on a dynamic rectangle
                if (sd.region.onMap(p.location)) {
                    seedbank.add(p);
                    plantcounts[5]++;
                }
            }
        }
        /*
         * THIS IS THE PLACE FOR MORTALITY CALCULATIONS
         * seeds from this year have dob=current year and
         * all others have age=current-year-dob>=1
         * 
         * Plants that resulted from recruitment last year will have
         * age==currentyear-dob == 0
         * and won't have had a change to reporduce yet
         */
        // THIS iS THE NEW LOCATION FOR MORTALITY OF PARENTAL GENERATION
        // Everyone currently in the population
        // gets killed at age specific rates
        if (DEBUG) {
            System.out.println("START mortality section: population="
                    + population.size() + " expopulation="
                    + expopulation.size() + " seedbank =" + seedbank.size());
        }
        Iterator<Plant> ourit = population.iterator();
        while (ourit.hasNext()) {
            Plant p = ourit.next();
            int age = sd.current_year - p.dob;
            double mortality = sd.mortality.value(age);
            
            if(INBREEDING)
            {
                if(sd.IBD_use &&sd.IBD_use__mortality)
                {
                    mortality=sd.IBD_MortalityEffect.value(p.excesshomozygosity,age);
                }
            }
            
            if (rnum.nextDouble() < mortality) {
                ourit.remove();
                p.dod = sd.current_year;
                expopulation.add(p);
            }
        }
        // NEW SEED STUFF
        // DO MORTALITY IN THE SEED BANK
        Iterator<Plant> ourseedit = seedbank.iterator();
        while (ourseedit.hasNext()) {
            Plant p = ourseedit.next();
            //this years seed is age 0
            //has dob=currentyear
            //we don't kill them off
            int age = sd.current_year - p.doc;
            if (age > 0) {
                double mortality = sd.seedmortality.value(age);
                if (rnum.nextDouble() < mortality) {
                    ourseedit.remove();
                }
            }
        }
        if (DEBUG) {
            System.out.println("END mortality section: population="
                    + population.size() + " expopulation="
                    + expopulation.size() + " seedbank =" + seedbank.size());
        }

//NOW PICK POTENTIAL RECRUITS FROM THE SEEBANK
        //make templist empty
        //iterate over seedbank
        //compare to germination rate
        //if germinate, remove from seedbank and put in templist
        templist.clear();
        Iterator<Plant> seedit = seedbank.iterator();
        while (seedit.hasNext()) {
            Plant seed = seedit.next();
            int age = sd.current_year - seed.doc;
            //if it germinates this year, remove it form the seed bank
            double germination = sd.seed_germination_rate.value(age);
            if (rnum.nextDouble() < germination) {
                seedit.remove();
                //and recruit if the seed is now in a viable location
                if (sd.region.inRegion(seed.location.X, seed.location.Y)) {
                    templist.add(seed);
                }
            }
        }

        /**
         * NEW METHOD for implementing competition We only consider potential
         * recruits that aren't at an occupied location. a little faster in a
         * test with high population density
         */
        // Loop over all new plants to remove any near a parent
        // for each offspring we iterate through population
        // perhaps ending early
        /**
         * *******************************************
         */
        if (DEBUG) {
            System.out.println("Starting recruitment. pop=" + population.size()
                    + " templist=" + templist.size() + " seedbank =" + seedbank.size());
        }
        Iterator<Plant> ourTLI = templist.iterator();
        while (ourTLI.hasNext()) {
            Plant ourOff = ourTLI.next();
            Iterator<Plant> ourPLI = population.iterator();
            while (ourPLI.hasNext()) {
                Plant ourPar = ourPLI.next();
                if (ourOff.location.isequal(ourPar.location)) {
                    ourTLI.remove();
                    plantcounts[4]++;
                    break; // iteration over parent list
                }
            }
        }
        // Randomize the order of the offspring
        //AND NOW ONLY SELECT NEW RECRUITS THAT ARE AT
        //UNOCCUPIED LOCATIONS

        ArrayList newtemplist = (ArrayList) templist.clone();
        int[] permutedIndices = rv.pickKfromM(templist.size(), templist.size());
        for (int ii = 0; ii < permutedIndices.length; ii++) {
            newtemplist.set(ii, templist.get(permutedIndices[ii]));
        }

        // Remove the first offspring to the population
        // (if we don't kill it) NOW WE DON'T KILL OFF age==0 HERE
        // and then remove any offspring close to it
        templist = newtemplist; // frees old templist memory
        while (templist.size() > 0) {
            Plant ourOff = (Plant) templist.get(0);
            // eliminated the mortality loop Feb 2009
            // double mortality = sd.mortality.value(0);
            // if (rnum.nextDouble() > mortality) {
            //if (ourOff.seedbank) {

            //now dob means date of germination
            //so next year will be exposed to age=0 mortality
            ourOff.dob = sd.current_year + 1;
            //}
            population.add(ourOff);
            // }
            ourTLI = templist.iterator();
            while (ourTLI.hasNext()) {
                Plant tplant = (Plant) ourTLI.next();
                if (ourOff.location.isequal(tplant.location)) {
                    if (ourOff != tplant) {
                        plantcounts[4]++;
                    }
                    ourTLI.remove();
                }

            }
        }
        if (DEBUG) {
            System.out.println("Ending recruitment. pop=" + population.size()
                    + " templist=" + templist.size() + " seedbank=" + seedbank.size());

            System.out.println("\n\n END nextYear()\nSeeds generated= " + plantcounts[0]
                    + "\nNonpollination = " + plantcounts[1]
                    + "\nOffplot = " + plantcounts[2]
                    + "\nadded to seed bank = " + plantcounts[3]
                    + "\ncrowded out = " + plantcounts[4]
                    + "\nimmigrants = " + plantcounts[5]);
        }
        /**
         * ***************************************
         */
    }

    private Location chooseLocation(Location parent_location, int d) {
        // There are (2d+1)^2 points at distance \le d
        // and (2d-1)^2 at distance \le d-1 and therefore
        // 4d^2+4d+1 -(4d^2-4d+1) = 8d points at distance d
        // numbering them clockwise around the square from 0=upper left the
        // corners are at 0, 2d, 4d, and 6d
        // x= uniformrandomint(0\le x \le 8d-1) RandomInt(0,8d-1)
        // if 0\le x \le 2d point<- (px-d+x, py+d)
        // if 2d\le x \le 4d point <- (px+d, py+d - (x-2d))
        // if 4d\le x \le 6d point<= (px+d-(x-4d) ,py-d)
        // if 6d \le x \le 8d point <- (px-d,py-d+(x-6d))
        if (!sd.useDispersalDirection)//old method
        {
            int x;
            if (d == 0) {
                x = 0;
            } else {
                x = rv.RandomInt(0, 8 * d - 1);
            }

        // if(DEBUG)
            // {
            // System.out.println("d = "+d+" x = "+x);
            // }
            if (0 <= x && x <= 2 * d) {
                return parent_location.add(new Location(x - d, d));
            } else if (2 * d <= x && x <= 4 * d) {
                return parent_location.add(new Location(d, 3 * d - x));
            } else if (4 * d <= x && x <= 6 * d) {
                return parent_location.add(new Location(5 * d - x, -d));
            } else if (6 * d <= x && x <= 8 * d) {
                return parent_location.add(new Location(-d, x - 7 * d));
            } else {
                if (DEBUG) {
                    System.out.println("chooseLocation(): This shouldn't happen");
                }
                return null;
            }
        } else {
            int u1, u2;
            //pick t
            double t;
            t = sd.DispersalDirection.value(rv.Uniform(0, 1, 1)[0]);
		//t is [0,360) convert to radians
            //System.out.print("Dispersal direction ="+ t+" ");
            //
            //SQRT2O2 is a constant \sqrt{2}/2
            //static final double SQRT2O2=0.707106781;
            //static final double PIO180=0.017453293;
            double z1 = Math.cos(t);
            double z2 = Math.sin(t);
            if (z1 >= SQRT2O2) {
                u1 = d;
                u2 = (int) Math.round(d * z2 / z1);
            } else if (z1 <= SQRT2O2) {
                u1 = -1 * d;
                u2 = (int) Math.round(d * z2 / (-1 * z1));
            } else if (z2 >= SQRT2O2) {
                u2 = d;
                u1 = (int) Math.round(d * z1 / z2);
            } else {
                u2 = -1 * d;
                u1 = (int) Math.round(d * z1 / (-1 * z2));
            }
           // System.out.println("new locations = "+new Location(u1,u2));
            return parent_location.add(new Location(u1, u2));
        }

    }

    public void runModel() {
        //Do a lifetable calculation 
        if (params.ShowLifetable) {
            doLifeTable();
        }
        //Start a progress window

        ProgressFrame pf = null;
        if (params.ShowProgress) {
            String[] labels = {"NewGarden Progress", "Run of simulation ",
                "Generation of run ", "Memory use "};
            pf = new ProgressFrame(3, labels);
        }
        //Do the requested number of simulations
        for (sd.run_number = 0; sd.run_number < sd.number_runs; sd.run_number++) {
            //update progress dialog
            if (params.ShowProgress) {
                //pf is not null if we enter this block of code
                System.gc();
                Runtime runtime = Runtime.getRuntime();
                long tm = runtime.totalMemory();
                long fm = runtime.freeMemory();
                pf.setValue(0,
                        pf.makePercent(sd.run_number, sd.number_runs - 1));
                pf.setValue(1, 0);
                pf.setValue(2, pf.makePercent(tm - fm, tm));

            }
            if (DEBUG) {
                System.out.println("Starting run number " + sd.run_number);
            }
            //each run starts with empty populations and expopulation
            population = new ArrayList<Plant>();
            expopulation = new ArrayList<Plant>();
            seedbank = new ArrayList<Plant>();

            //MAKE RANDOM REGION IF USING RANDOM REGIONS
            //Read through sd.Region and if any rects are Sparse, call makeInstance()
            //Update DynamicRects if any
            //sd.region.makeInstances(rv);
            //sd.region.updateDynamics(sd,rv);
            sd.region.initialize();
            sd.region.update(0, rv);
            //preIterate() the DYNAMIC NewRectangles
            makeInitialPopulation();

            //loop for each year/generation of the current simulation
            for (sd.current_year = 0; sd.current_year < sd.number_generations - 1; sd.current_year++) {
                //record stats on population
                //(gets founders as generation 0)
                if (sd.current_year % sd.datainterval == 0 && !params.SkipStats) {
                    ourStats.makeSummary(population, sd);
                    //new 11 apr
                    //sstats.makeSummary(population, sd);
                }
                if (DEBUG) {
                    System.out.println("In year number " + sd.current_year);
                }
                if (DEBUGMEMORY) {
                    System.out.print("current year=" + sd.current_year + " ");
                    System.out.println("pop=" + population.size() + "ex=" + expopulation.size() + "seed=" + seedbank.size());
                }
                //do a round of breeding, competition, mortality
                //DEC 2013 DynamicRects
                //if there are dynamic rects in the region
                //update them and, optionally, remove
                //non-founders? that are no longer on viable sites
                nextYear();
                if (params.ShowProgress) {
                    pf.setValue(1, pf.makePercent(sd.current_year,
                            sd.number_generations - 2));
                }
                //if requested, write dead plants to output file
                //and clear out the expopulation list
                showOurPop(expopulation, false, true);
                //ANYWAY***** clear the expop list NEW June 29 2013????
                expopulation = new ArrayList<Plant>();

                 // end 5 Jan 2012
                //FEB 2014 Make MAPS at the end of run=0
                if (sd.makeMap && sd.run_number == 0) {
                    //is this a mapYear?
                    boolean doit = false;
                    for (int i = 0; i < sd.mapYears.length; i++) {
                        if (sd.mapYears[i] == sd.current_year) {
                            doit = true;
                        }
                    }
                    if (doit) {
                         Image im = new Image(sd);
                    im.doAllImages(population, seedbank);
                    //Map produces .ps files
                    //Image produces .png files
                      //  Map m = new Map(sd, population, seedbank);
                      //  m.doAllMaps();
                    }
                }

                //1 FEB 2014 IF REQUESTED WRITE MAPS
                //TO START WE WILL DO EVERY MAP FPR EVERY YEAR IN THE FIRST RUN
                //LATER THIS WILL BE CONTROLED FROM SimData
                sd.region.update(sd.current_year + 1, rv);

            }//end of current year look
            // this will get the last generation as year number_generations-1
            if (sd.current_year % sd.datainterval == 0 && !params.SkipStats) {
                ourStats.makeSummary(population, sd);

            }
            //we're at the end of the run
            //write out the entire living population
            showOurPop(population, false, false);

            if (sd.makeMap && sd.run_number == 0) {
                //is this a mapYear?
                boolean doit = false;
                for (int i = 0; i < sd.mapYears.length; i++) {
                    if (sd.mapYears[i] == sd.current_year) {
                        doit = true;
                    }
                }
                if (doit) {
                    /*Map m=new Map(sd,population,seedbank);
                     m.doAllMaps();
                     */
                    Image im = new Image(sd);
                    im.doAllImages(population, seedbank);
                }
            }
            /*17 May 2014 SeedCollection*/
            if (sd.useSeedCollection) {
                seedbank.clear();
                ArrayList<Plant> maternal = lastYear();
                /*dump the seeds by appending to file*/
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(sd.seeddumpfilename, true)));
                    if (sd.run_number == 0)//provide .csv header
                    {
                        Plant p = new Plant(sd.number_loci);
                        out.print("SubRegion," + p.CSVHeader());
                    }
                    for (Plant p : seedbank) {
                        int sr = sd.summaryregions.whichSummaryRegion(p.location.X, p.location.Y);
                        //EVENTIUALLY ONLY WRITE THE SEED IF ITS PARENT
                        //LIES IN A summary region
                        out.print("" + sr + "," + p.toCSVString(sd.run_number));
                    }
                    out.close();
                } catch (IOException e) {
                    //exception handling left as an exercise for the reader
                }
                /*Now dump materal parents so we know their locations*/
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("parental-" + sd.seeddumpfilename, true)));
                    if (sd.run_number == 0)//provide .csv header
                    {
                        Plant p = new Plant(sd.number_loci);
                        out.print("SubRegion," + p.CSVHeader());
                    }
                    for (Plant p : maternal) {
                        int sr = sd.summaryregions.whichSummaryRegion(p.location.X, p.location.Y);
                        //if summaryregions only and sr!= -1 write the parent out
                        out.print("" + sr + "," + p.toCSVString(sd.run_number));
                    }
                    out.close();
                } catch (IOException e) {
                    //exception handling left as an exercise for the reader
                }
            }

            /*END 17 May 2014 SeedCollection*/
        }// runs
        //Now decide where and how information is to be written       
        if (!params.SkipStats) {
            //if we're given a results file name
            if (params.RESULTSfilename != null) {
                File resultsfile = new File(params.RESULTSfilename);
                try {
                    FileWriter fw = new FileWriter(resultsfile);
                    BufferedWriter br = new BufferedWriter(fw);
                    PrintWriter pw = new PrintWriter(br, true);
                    if (params.ShowVersion) {
                        pw.println(params.toString(false));
                    }
                    if (params.ShowXML) {
                        pw.println(sd.toString());
                        // pw.println(statistics.doOldReport());
                    }
                    pw.println(ourStats.fullReport());
                    fw.close();
                } catch (Exception e) {
                    System.out.println(e.toString());

                }
            } //otherwise write to out
            else {
                if (params.ShowVersion) {
                    System.out.println(params.toString(true));
                }
                if (params.ShowXML) {
                    System.out.println(sd.toString());
                }
                System.out.println(ourStats.fullReport());

            }
        }
        if (params.ShowProgress) {
            //pf != null sice we created it based on params.ShowProgress==true
            pf.done();
        }

        System.exit(0);
    }

    /**
     * makeSummary is called after each run It reads through the entire
     * population (and expopulation) to accumulate information on plants that
     * were alive during each year of the simulation and also by cohort ---
     * counting only those plants produced in a given year (and therefore of a
     * given age =1,2,3...) The only plants that need to be treated specially
     * are the founders whose dob is fudged to give them the desired age at the
     * start of the run. This will appear as "age=0" in the summary, but the
     * represent the founders not age=0
     *
     */
    /**
     * int locateClass(float x, float[] d) is given a value $x$ and an array of
     * values d[0],d[1],...,d[d.length-1] these inputs are constants and not
     * altered by this procedure
     *
     * The routine returns an integer i so that exactly one of 3 cases holds
     *
     * 1) i==0 and x \le d[0] 2) 0<i<d.length and d[i-1]< x \le d[i] 3)
     * i=d.length and x > d[d.length-1]
     *
     *
     * This should, of course, just be inline but I've separated it out to
     * include all the assertions and for purposes of profiling
     */
    int locateClass(double x, float[] d) {
        int i = 0;
        while (i < d.length && x > d[i]) {
            i++;
        }

        if (DEBUG)// we can turn assertions off at runtime, but all this sort
        // of stuff I'll put in DEBUGs
        {
            assert (i == 0 && x <= d[0])
                    || (0 < i && i < d.length && d[i - 1] < x && x <= d[i])
                    || (i == d.length && x > d[d.length - 1]) : "locateClass failed: x= "
                    + x + " i = " + i;

        }

        return i;
    }

    int locateClass(double x, int[] d) {
        int i = 0;
        while (i < d.length && x > d[i]) {
            i++;
        }

        if (DEBUG)// we can turn assertions off at runtime, but all this sort
        // of stuff I'll put in DEBUGs
        {
            assert (i == 0 && x <= d[0])
                    || (0 < i && i < d.length && d[i - 1] < x && x <= d[i])
                    || (i == d.length && x > d[d.length - 1]) : "locateClass failed: x= "
                    + x + " i = " + i;

        }

        return i;
    }

    // 5 Feb 2012 stuff to dump every year rather than at the end
    //
    // private void showExPop(BufferedWriter pw, ArrayList source) throws
    // IOException {
    // Iterator it = source.iterator();
    // while (it.hasNext()) {
    // Plant p = (Plant) it.next();
    // pw.append(p.toCSVString(sd.current_year));
    // it.remove();
    // }
    // pw.flush();
    //
    // }
    // private void showExPop(PrintStream pw, ArrayList source) {
    // Iterator it = source.iterator();
    // while (it.hasNext()) {
    // Plant p = (Plant) it.next();
    // pw.append(p.toCSVString(sd.current_year));
    // it.remove();
    // }
    // }
    /**
     *
     * @param source display an optional header, get the run number correct and
     * append the csv strings for all the plants in source optionally removing
     * them as we go.
     */
    private void showOurPop(ArrayList<Plant> source, boolean needHeader, boolean remove) {

        if (params.ShowDump)// should we do this dump?
        {
            if (params.DUMPfilename == null)// to System.out?
            {
                Iterator<Plant> it = source.iterator();
                if (needHeader && it.hasNext()) {
                    Plant temp = source.get(0);
                    System.out.println(temp.CSVHeader());
                }
                while (it.hasNext()) {
                    Plant p = it.next();

                    System.out.println(p.toCSVString(sd.run_number));
                    if (remove) {
                        it.remove();
                    }
                }

            } else// or to a user specified file?
            {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(params.DUMPfilename,
                            true));

                    Iterator<Plant> it = source.iterator();
                    if (needHeader && it.hasNext()) {
                        Plant temp = source.get(0);
                        bw.append(temp.CSVHeader());
                    }
                    while (it.hasNext()) {
                        Plant p = it.next();

                        bw.append(p.toCSVString(sd.run_number));
                        if (remove) {
                            it.remove();
                        }
                    }
                    bw.flush();
                } catch (IOException e) {
                    System.err.println(e.toString());
                }

            }
        }

    }

    // 5 Feb 2012
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("This program needs input parameters. Provide the name\n"
                    + "of a SimData.dtd XML file as an argument on the command line\n");
        }
        Model t = new Model(args);
        t.runModel();
    }
}
