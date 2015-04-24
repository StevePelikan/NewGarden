package biology;

import parameters.SimData;

/*
 * Plant.java
 *
 * Created on February 28, 2005, 6:50 PM
 * 29 July 2011 added a constructor that takse a boolean specifying femaleP
 * to be called from Founders when generating a RandomPlantList
 * 
 * 9Feb13 changed StringBuffers in output methods to StringBuilder.

29June2014 Need to calcuate expected homozygosity for alleles specified in SimDat
which is static and done only once we'd like

And a method to calcuate and stor each Plant's excesshomozygosity
 */

/**
 * A structure for containing all the information we need about a plant
 * @author sep
 */
public class Plant {
    /**
     * id_no is a class variable that lets us give each plant created a unique id*/
    public static int id_no=0;
    private static final boolean INBREEDING=true;
    /**my_id_no is the unique id for a given instance
     **/
    public int my_id_no;
    /**parent1 and parent2 are the id numbers of the parents of an instance
     parent1 is the id of the maternal parent*/
    public int parent1,parent2;
    /** Creates a new instance of Plant */
    public Plant() {
        my_id_no=(id_no++);
    }
    
    /**
     * whether plant is female or not
     */
    public boolean female;
    //public boolean seedbank;
    /**
     *whether a plant was one of the founders of the population
     */
    public boolean founder;
    /**
     * an array that gives the alleles this plant has at the diferent loci
     */
    public  int [][]allele;
    /**
     * date of birth (germination) ,death
     * date produced as seed
     */
    public int dob,dod=-1;
    public int doc;
    /**
     * location of the plant
     */
    public Location location;
    
    //30 June 2014 Inbreeding
    public float excesshomozygosity;
    /**
     * create a plant with space of n loci
     * @param number_loci the number of loci we're tracking
     */
    public Plant(int number_loci)
    {  my_id_no=(id_no++);allele=new int[number_loci][2];founder=false;
    //seedbank=false;
    }
    /**
     * create a plant
     * @param number_loci number of loci
     * @param db date of birth
     * @param l location
     */
    public Plant(int number_loci,int db,Location l) {
        my_id_no=(id_no++);
        allele=new int[number_loci][2];
        dob=db;
        location=l;
        founder=false;
       // seedbank=false;
    }
    public Plant(int number_loci,int db,Location l,boolean s,boolean f) {
        my_id_no=(id_no++);
        allele=new int[number_loci][2];
        dob=db;
        location=l;
        founder=false;
        female=s;
        founder=f;
        //seedbank=false;
    }
    /**
     *toXML() returns a String with data that can be used in the Simulation_Data .dtd
     *to describe the plant. The assignment of alleles isn't preserved but 
     * location, sex, and age are
     * @return a String in XML for this structure
     */
    public String toXML() {
        StringBuilder sb=new StringBuilder("");
        sb.append("<Plant age=\"").append(dob).append("\" femaleP=\"")
                .append(female).append("\" X=\"").append(location.X).
                append("\" Y=\"").append(location.Y).append("\"/>\n");
        return sb.toString();
    }
    /**
     * a printable string describing this instance
     * @return String description of this instance of the class
     */
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("id_no=").append(my_id_no).append("parent1 =").append(parent1).append(" parent2 =").append(parent2).append("\ndob = ").append(dob).append("\ndod = ").append(dod).append("\n" + "female = ").append(female).append("\n");
        sb.append("founder =").append(founder).append("\n");
        sb.append(location.toString()).append("\nalleles\n");
        for(int i=0;i<allele.length;i++) {
            sb.append("locus ").append(i).append(":" + "(").append(allele[i][0]).append(",").append(allele[i][1]).append(")");
            sb.append("\n");
        }
        return sb.toString();
    }
    /**
     * apomixis creates a new plant that is a clone of this plant.
     * 18 May 2014 to be used with Agamospermy
     * @param sd
     * @return 
    */
    public Plant apomixis(SimData sd)
    {
        Plant p=new Plant(sd.number_loci);
        p.dob=-1;
        p.doc=sd.current_year;
        p.dod=-1;
        p.female=this.female;
        p.founder=false;
        p.parent1=this.my_id_no;
        p.parent2=this.my_id_no;
        for(int l=0;l< this.allele.length;l++)
        {
            for(int a=0;a<2;a++)
            {
                p.allele[l][a]=this.allele[l][a];
            }
        }
        //29June2014 InbreedingDepression
        //copy excesshomozygosity
        
        return p;
    }   
    /**
     * 
     * @param sd SimData that gives the alleles to consider in finding homozygosity
     * 
     * 30 June 2014 This method introduced as part of InbreedingDepression
     * 
     * SimData.foundershomozygosity is the expected number of homozygous loci
     * (among those used for IBD)
     * among the founders
     * 
     */
    public void calculateExcessHomozygosity(SimData sd)
    {
        if(INBREEDING)
        {
        int count=0;
        for(int i: sd.IBD_loci)
        {
            if(allele[i][0]==allele[i][1]) count++;
        }
        double us=Math.max(0, count-SimData.foundershomozygosity);
        
        excesshomozygosity = (float) (us/(sd.IBD_loci.length-SimData.foundershomozygosity));
        }
        else
        {
            excesshomozygosity =0;
        }
    }
    /**
     * Read in a .csv string that describes a Plant and
     * create one with those properties.
     * @param csv The string
     * @param sd a SimData describing the context
     * @param offset how many entries at start of string to skip (we prepend these
     * strings with things like run number and summary region.
     * @return 
     * 
     * 
     * additional data can be pre-pended to st strings
     * offset is the index of the ID field (starting with 0)
     * 18 May 2014
     */
    static public Plant fromCSVString(String csv,SimData sd,int offset)
    {
        Plant p=new Plant(sd.number_loci);
        //split the string
        String[] values=csv.split(",");
        //int t=Integer.valueOf(values[1]);
        //int tt=Integer.getInteger(values[1]);
        p.my_id_no=Integer.valueOf(values[offset]);
        p.parent1=Integer.valueOf(values[offset+1]);
        p.parent2=Integer.valueOf(values[offset+2]);
        p.doc=Integer.valueOf(values[offset+3]);
        p.dob=Integer.valueOf(values[offset+4]);
        p.dod=Integer.valueOf(values[offset+5]);
        if( (values[offset+6]) .equals("true") )
        { p.female=true;}
        else {p.female=false;}
        
        if((values[offset+7]) .equals("true") )
        { p.founder=true;}
        else {p.founder=false;}
        //29June2014 InbrredingDepression excesshomozygosity
        p.location=new Location(Integer.valueOf(values[offset+8]), Integer.valueOf(values[offset+9]));
        for(int l=0;l<sd.number_loci;l++)
        {
            for(int a=0;a<2;a++)
            {
                p.allele[l][a]=Integer.valueOf(values[offset+10+2*l+a]);
            }
        }
        //IF INBREEDING DEPRESSION calculate excesshomozygosity
        if(INBREEDING)p.calculateExcessHomozygosity(sd);
        return p;
    }
    /**
     *toCSVString generates a single line of csv data about this plant
     *it is intended to be imported into a statistical package
     *The field names are printed in the order
     *ID,p1,p2,dob,female,locationX,locationY,loc_0_a_0,l_0_a_1,...locus_k_allele_m,...
     * 13Feb13 made this just a call to the version with integer argument
     * @return 
     */
    public String toCSVString()
    {
        return toCSVString(0);
    
    }

    /**
     *
     * @param run
     * @return
     */
    public String toCSVString(int run)
    {
        StringBuilder sb=new StringBuilder("");
        sb.append(run).append(",");
        sb.append(my_id_no).append(",");
        sb.append(parent1).append(",");
        sb.append(parent2).append(",");
        sb.append(doc).append(",");
        sb.append(dob).append(",");
        sb.append(dod).append(",");
        sb.append(female).append(",");
        sb.append(founder).append(",");
       
      //  sb.append(seedbank).append(",");
        sb.append(location.X).append(",");
        sb.append(location.Y).append(",");
        for(int loc=0;loc<allele.length;loc++)
        {
            for(int a=0;a<2;a++)
            {
                sb.append("").append(allele[loc][a]).append(",");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
    public String CSVHeader()
    {
         StringBuilder sb=new StringBuilder("");
         sb.append("run,ID,p1,p2,doc,dob,dod,female,founder,X,Y,");
         //29June2014 excesshomozygosity
          for(int loc=0;loc<allele.length;loc++)
        {
            for(int a=0;a<2;a++)
            {
                sb.append("l").append(loc).append("a").append(a).append(",");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
    public static void main(String [] args)
    {
        String csv="-1,0,230,11,94,4,0,-1,true,false,15,86,7,16,5,6,18,2,4,16,9,7,14,4,2,8,15,2,2,3,6,15,1,2,";
        SimData sd=SimData.readXMLFile("MasterSimData.xml");
        Plant p=Plant.fromCSVString(csv, sd,2);
        System.out.println(p.toCSVString());
       
    }
}


