package parameters;


/*
 * 29 Juy 2011 added randomfounders (a list of RandomPlantList s
 * for automatic generation of random founders.
 * 
 * 29 Juy 2011 I still haven't managed to generate EasyPollen xml
 * from the pollen distribution data. 20Feb2013 This was finally done.
 * 
 * 23 May 2013 I added Immigration parameters and modified the parsing and
 * printing of SimData to handle them. Still to do: ImmigrationPFemale
 * 
 * 25 May 2013 CreateAllFounders is a new flag that idicates whether
 * we should keep trying to create random founders if, at first, some
 * of them fall outside the Region because it includes SparseRectangles (random
 * regions) or because part of the RandomPlantList rectangle lies outside the Region.
 * THis has th epotential to be very slow or even (with bad inputs) make an infinte loop
 * in Founders.calculate
 */
import biology.Locus;
import biology.NewRegion;
import biology.Plant;
import biology.Region;
import discretefunction.TwoDLatticePDF;
import functions.BilinearInterp;
import functions.CDF;
import functions.DiscreteProbabilityDistribution;
import functions.SampledFunction;
import java.io.File;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SimData {
    private static final boolean GENEACTION=true;
    public boolean dioeceous;
    public boolean random_mating;
    public ArrayList<Locus> loci;
    public int number_automatic_loci = 0;
    public int auto_alleles_per_locus = 0;
    public ArrayList<Plant> plants;
    public ArrayList<RandomPlantList> randomfounders;
    public boolean CreateAllFounders = false;
    public int max_alleles, number_loci;
    public SampledFunction fruit_production;

    //NEW SEED STUFF
    //public boolean use_seedbank;
    public SampledFunction seed_germination_rate;
    //public float seedbankmixinrate;

    public SampledFunction pollen_production;
    public SampledFunction PollenDirection;
    public boolean usePollenDirection = false;
    public SampledFunction mortality;

    //NEW SEED STUFF
    public SampledFunction seedmortality;

    public float selfing_rate;
    public boolean Agamospermy_use;
    public float Agamospermy_probability;
    public String Agamospermy_method;
    public float foundersf;
    //  public float fruit_distance, pollen_distance;
    public CDF dispersal_distribution;
    //new ,arch 2014 DispersalDirection
    public CDF DispersalDirection;
    public boolean useDispersalDirection = false;

    //Comaptibility 14 Dec 2013
    public boolean compatibility = false;
    public float compatibility_fraction = 0.75f;
    public int compatibility_locus = 0;
    //Region now may have SparseRect's that change with each run

    //NEW Dec 2013 for dynamic regions
    //public boolean use_dyanamic_regions=false;
    //public Region region;
    public NewRegion region;

    public SummaryRegions summaryregions;
    public String off_dist_method;

    public TwoDLatticePDF Immigration;
    public boolean ImmigrationUse = false;
    public float ImmigrationRate;
    public float ImmigrationPFemale = 0.5f;

    //NEW SEED STUFF
    //public String seed_dist_method;
    public int number_generations, number_runs;
    public int datainterval = 1;
    public int current_year, run_number;
    //new pollen stuff 7 april 06
    public int[] pollen_distances;
    public DiscreteProbabilityDistribution pollen_distance_probabilities;
    //end new pollen stuff

    public boolean makeMap;
    public int[] mapYears;
    public int pixelsPerSite=4;

    /*17 May 2014 SeedCollection*/
    public boolean useSeedCollection = false;
    public boolean SeedCollectionsubregions_only = true;
    public String seeddumpfilename = "seeddump.csv";
    public float SeedCollectionlast_r;

    
    //29 June 2014 InbreedingDepression parameters
 public    boolean IBD_use;
 public    int[] IBD_loci;
 public    boolean IBD_use__reproduction;
 public    boolean IBD_use__mortality;
 public    SampledFunction IBD_ReproductionEffect;
 public    BilinearInterp IBD_MortalityEffect;
 public    static double foundershomozygosity;
    /*
     //1 Feb 2014 New stuff for making maps
     public boolean makeMap=false;
     public boolean mapRegion=false;
     public boolean mapPopulation=false;
     public boolean mapSeedbank=false;
     public float[] mapSize={5,5};
     */
    /*
     New in April 2014. Why not keep the code for reading in this class?
     */
    public static SimData readXMLFile(String xmlfilename) {
        File input = new File(xmlfilename);
        if (!input.exists()) {
            System.out.println("Can't find the file " + xmlfilename);
        }

        SimData sd = null;
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
        if (my_mb == null) {
            System.out.println("Problem getting  SimDataReader() in SimData.readXMLFile()");
            sd = null;
        } else {
            sd = my_mb.getSD();
            sd.MakeFinal();

        }
        return sd;
    }

    public SimData() {
        loci = new ArrayList<Locus>();
        plants = new ArrayList<Plant>();
        randomfounders = new ArrayList<RandomPlantList>();

        fruit_production = new SampledFunction();

        Agamospermy_use = false;
        Agamospermy_probability = 0.0f;
        Agamospermy_method = "all";

        //NEW SEED STUFF
        seed_germination_rate = new SampledFunction();
        //use_seedbank=true;

        pollen_production = new SampledFunction();
        PollenDirection = new SampledFunction();
        usePollenDirection = false;
        mortality = new SampledFunction();

        //NEW SEED STUFF
        seedmortality = new SampledFunction();
        //seedbankmixinrate=0.0f;

        dispersal_distribution = new CDF();
        DispersalDirection = new CDF();
        useDispersalDirection = false;

        current_year = 0;
        run_number = 0;
        //default values that should be over written by reading paramter file
        off_dist_method = "round";

        //NEW SEED STUFF
//        this.seed_dist_method="round";
        dioeceous = false;
        summaryregions = new SummaryRegions();
        datainterval = 1;
        makeMap = false;
        mapYears = null;
    }
    /*
     * 20feb2013 reconstructs an EasyPollen statement from
     * the pollen_distances and .. probabilities we
     * store in SimData.
     */

    private String OldToEasyPollen() {
        MyFormat myformat = new MyFormat();
        String[] lows = new String[pollen_distances.length + 1];
        lows[0] = Integer.toString(0);
        for (int i = 1; i < lows.length; i++) {
            lows[i] = Integer.toString(pollen_distances[i - 1] + 1);
        }
        String[] highs = new String[pollen_distances.length + 1];
        highs[highs.length - 1] = "Inf";
        for (int i = 0; i < highs.length - 1; i++) {
            highs[i] = Integer.toString(pollen_distances[i]);
        }
        String[] probs = new String[pollen_distances.length + 1];
        for (int i = 0; i < pollen_distances.length + 1; i++) {
            for (int j = 0; j < pollen_distance_probabilities.v.length; j++) {
                if (pollen_distance_probabilities.v[j] == i) {
                    probs[i] = myformat.format("%5.3g", pollen_distance_probabilities.p[j]);
                    continue;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<EasyPollen>\n");
        for (int i = 0; i < lows.length; i++) {
            sb.append("<pollenframe low=\"").append(lows[i]).append("\" high =\"").append(highs[i]);
            sb.append("\" prob=\"").append(probs[i]).append("\"/>\n");
        }
        sb.append("</EasyPollen>\n");
        return (sb.toString());
    }

    public void MakeFinal() {
        loci.trimToSize();
        //31 Jan 2014 if compatibility value is true
        //assert locus[compatibility_locus].getAction().equals("compatibility"): "somethings wrong with compatibility";
        plants.trimToSize();
        randomfounders.trimToSize();
        number_loci = loci.size();
        Iterator<Locus> it = loci.iterator();
        max_alleles = 0;
        while (it.hasNext()) {
            Locus l = (Locus) it.next();
            // l.setF(this.foundersf);
            if (max_alleles < l.number_alleles()) {
                max_alleles = l.number_alleles();
            }
        }
        for (Locus l : loci) {
            l.setF(foundersf);
        }
//29 June 2014 InbreedingDepression calculate foundersexpectedhomozygosity
        if(IBD_use && SimData.GENEACTION)
        {
            for(int i:this.IBD_loci)
            {
                SimData.foundershomozygosity+= loci.get(i).expectedHomozygosity();
            }
        } else {
        }
        //4 JUNE 2014
        //A FEW SANITY CHECKS ON CDFS
        assert this.dispersal_distribution.monotoneIncreasing() == true : "Dispersal CDF not monotone";
        assert this.dispersal_distribution.isOkay() == true : "Dispersal CDF not defined on 0 to 1";
        assert this.DispersalDirection.monotoneIncreasing() == true : "DispersalDirection CDF not monotone";
        assert this.DispersalDirection.isOkay() == true : "DispersalDirection CDF not defined on 0 to 1";
    }

    @Override
    /**
     * Returns a string that is an XML version of the values stored in this
     * object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append("<LOCI  number_loci =\"").append(loci.size());

        if (this.number_automatic_loci > 0) {
            sb.append("\" number_automatic_loci = \"").append(number_automatic_loci).append("\" auto_alleles_per_locus = \"");
            sb.append(auto_alleles_per_locus).append("\"");
        }
        sb.append(">\n");
        //write out xml for the non-automatic loci
        for (int i = number_automatic_loci; i < this.number_loci; i++) {
            sb.append(loci.get(i).toXML());
        }

        sb.append("</LOCI>\n");
        
          //29 June 2014 Inbreeding GENEACTION
        if(GENEACTION)
        {
            sb.append("<InbreedingDepression ");
            sb.append(" use = \""+this.IBD_use+"\" ");
            sb.append("depressionloci = \""+writeRLEString(IBD_loci)+"\"\n");
            sb.append("depression_reproduction = \""+IBD_use__reproduction+"\"\n");
            sb.append("depression_mortality =\""+IBD_use__mortality+"\"\t>\n");
            String[] names1={"x","y"};
            sb.append("<IBD_ReproductionEffect "+IBD_ReproductionEffect.toAttributes(names1)+"\t/>\n");
            String []names2={"x","y","f"};
            sb.append("<IBD_MortalityEffect "+this.IBD_MortalityEffect.toAttributes(names2)+"\t/>\n");
            sb.append("</InbreedingDepression>\n");
        }
        
        
        sb.append("<Dioecious value =\"").append(dioeceous).append("\"/>\n");

        sb.append("\n").append(fruit_production.toXML("Reproduction_Rate"));
        sb.append("\n<Offspring_Distribution method=\"").append(off_dist_method).append("\"/>\n");

        //NEW SEED STUFF
        sb.append("\n").append(seed_germination_rate.toXML("SeedGerminationRate"));

        sb.append("\n").append(pollen_production.toXML("Pollen_Rate"));

        sb.append("\n").append(mortality.toXML("Mortality_Rate"));

        //NEW SEED STUFF
        sb.append("\n").append(this.seedmortality.toXML("SeedMortalityRate"));

        
      
        sb.append("\n<Selfing_Rate value =\"").append(selfing_rate).append("\"/>\n");
        sb.append("\n<RandomMating value=\"").append(random_mating).append("\"/>\n");

        sb.append("\n<Agamospermy use = \"" + this.Agamospermy_use + "\" ");
        sb.append("method = \"" + this.Agamospermy_method + "\" ");
        sb.append("probability =\"" + this.Agamospermy_probability + "\"/>\n");

        sb.append("\n").append(dispersal_distribution.toXML("Dispersal_Distribution"));
        //march 2014
        if (this.useDispersalDirection) {
            sb.append("\n").append(DispersalDirection.toXML("DispersalDirection", "use = \"true\""));
        } else {
            sb.append("\n").append(DispersalDirection.toXML("DispersalDirection", "use = \"false\""));
        }

        sb.append(OldToEasyPollen());

//MARH 2013 PollenDirection
        sb.append("\n").append(this.PollenDirection.toXML("PollenDirection", "use =\"" + this.usePollenDirection + "\""));

//compatibility 14 Dec 2013
        sb.append("<Compatibility locus = \"").append(compatibility_locus).append("\" value =\"").append(compatibility).append("\" fraction = \"").append(compatibility_fraction).append("\"/>\n");
//Region
        sb.append(region.toXML());
        sb.append(this.summaryregions.toXML());

        sb.append("<MakeMaps use =\"").append(makeMap).append("\" pixelsPerSite = \"").append(this.pixelsPerSite).append("\">\n");
        sb.append("<Vector name=\"maps\"");
        sb.append(" length =\"").append(mapYears.length).append("\" ");
        sb.append(" values = \"");
        for (int i = 0; i < mapYears.length - 1; i++) {
            sb.append("").append(mapYears[i]).append(",");
        }
        sb.append("").append(mapYears[mapYears.length - 1]).append("\"/>\n");
        sb.append("</MakeMaps>\n");
        String[] atts = new String[6];
        atts[0] = "use";
        atts[1] = "false";
        if (ImmigrationUse) {
            atts[1] = "true";
        }
        atts[2] = "rate";
        atts[3] = Float.toString(ImmigrationRate);
        atts[4] = "pfemale";
        atts[5] = Float.toString(ImmigrationPFemale);
        sb.append(this.Immigration.toXML("Immigration", atts)); //ALSO PASS ATTRIBUTES

        sb.append("<Number_Generations value =\"").append(number_generations).append("\"/>\n");
        sb.append("<Number_Runs value =\"").append(number_runs).append("\"/>\n");
        sb.append("<DataInterval value =\"").append(datainterval).append("\"/>\n");

        sb.append("<SeedCollection use =\"" + this.useSeedCollection + "\" ");
        sb.append("subregions_only =\"" + this.SeedCollectionsubregions_only + "\" ");
        sb.append("seed_dump_file_name = \"" + this.seeddumpfilename + "\" ");
        sb.append("last_r = \"" + this.SeedCollectionlast_r + "\"/>\n");

        sb.append("<Initial_Population>\n");

        for (Plant p : plants) {
            sb.append(p.toXML());
        }
        Iterator<RandomPlantList> rit = randomfounders.iterator();
        while (rit.hasNext()) {
            RandomPlantList rpl = (RandomPlantList) rit.next();
            sb.append(rpl.toXML());
        }

        sb.append("</Initial_Population>\n");
        sb.append("<CreateAllFounders value =\"").append(CreateAllFounders).append("\"/>\n");
        sb.append("<FoundersF value =\"").append(foundersf).append("\"/>\n\n\n");

        sb.append("\n=============================================\n");
        sb.append("Here's R code to plot some of the parameters:\n");
        String[] labels = new String[4];
        labels[0] = "Seedgermination";
        labels[1] = "Age";
        labels[2] = "Germination";
        labels[3] = "Age dependent germination rate";
        sb.append(seed_germination_rate.toRcode(labels)).append("\n");

        labels[0] = "Seedmortality";
        labels[1] = "Age";
        labels[2] = "Mortality";
        labels[3] = "Age dependent seed mortality rate";
        sb.append(seedmortality.toRcode(labels)).append("\n");

        labels[0] = "pollenproduction";
        labels[1] = "Age";
        labels[2] = "Relative pollen production";
        labels[3] = "Age dependent pollen production rate";
        sb.append(pollen_production.toRcode(labels)).append("\n");

        labels[0] = "fruitproduction";
        labels[1] = "Age";
        labels[2] = "Reproduction rate";
        labels[3] = "Age dependent reproduction rate";
        sb.append(fruit_production.toRcode(labels)).append("\n");

        labels[0] = "mortality";
        labels[1] = "Age";
        labels[2] = "Mortality rate";
        labels[3] = "Age dependent mortality rate";
        sb.append(mortality.toRcode(labels)).append("\n");

        labels[0] = "dispersal";
        labels[1] = "distance";
        labels[2] = "Prob < distance";
        labels[3] = "Cumulative distribution of dispersal distances";
        sb.append(dispersal_distribution.toRcode(labels)).append("\n");

        labels[0] = "dispersaldirection";
        labels[1] = "direction(angle)";
        labels[2] = "Prob < angle";
        labels[3] = "Cumulative distribution of dispersal direction";
        sb.append(DispersalDirection.toRcode(labels)).append("\n");
        sb.append("\n=============================================\n");
        return sb.toString();
    }
    private String writeRLEString(int[] x)
    {
        StringBuilder sb=new StringBuilder();
       int where=0;
       while(where<x.length-1)
       {
           sb.append(""+x[where]);
           
           if(x[where+1]==x[where]+1)
           {
              sb.append("-");
              while( where<x.length-1&&x[where+1]==x[where]+1 ) where++;
           }
           else{
               sb.append(",");
           where++;
           }
       }
       sb.append(x[x.length-1]);
       
        
        return sb.toString();
    }
}
