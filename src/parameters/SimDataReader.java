package parameters;

/*
 * SimDataReader.java based on ModelBuilder.java is part of the program
 * NewGarden
 *
 * Created on August 30, 2003 adapted to Biglist project Decemeber 29, 2003 this
 * version is for newgarden parameter files 2006-2007
 *
 * 29 July 2011 added startElement for RandomPlantList which only has to create
 * a RandomPlantList, parse attributes and add the RPL to the SimData class's
 * randomfounders list.
 */
/**
 *
 * @author sep
 */
//Model Builder - extends DefaultHandler for parsing the xml in the taxa.dtd data type
import biology.*;
import discretefunction.TwoDLatticePDF;
import functions.BilinearInterp;
import functions.CDF;
import functions.DiscreteProbabilityDistribution;
import functions.SampledFunction;
import java.util.ArrayList;
import java.util.Stack;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SimDataReader extends DefaultHandler {

  
    Stack stack;
    Object topguy;
    static final boolean DEBUG = false;

    public SimDataReader() {
        if (DEBUG) {
            System.out.println("SimDataReader()");
        }
        stack = new Stack();
    }

    public SimData getSD() {
        return (SimData) stack.pop();
    }

    //what to do when we get character data
    @Override
    public void characters(char[] ch, int start, int len) {
        //TempElement el=(TempElement)stack.peek();
        String s = new String(ch, start, len);
        System.out.println("Problem: asked to handle characters" + s);
    }
    //how to end an element

    @Override
    public void endElement(String ns, String ln, String qname) throws SAXException {

        if (stack.empty()) {
            System.out.println("Hey --- stack is empty!");
        }

        if (qname.equals("EasyPollen")) {
            ArrayList ourFrames = (ArrayList) stack.pop();
            SimData sd = (SimData) stack.peek();
            //create a sampledfunction that might have been read 
            //in as a PollenDistances
            SampledFunction sf = new SampledFunction();
            for (int i = 0; i < ourFrames.size() - 1; i++) {
                String framedata[] = (String[]) ourFrames.get(i);
                //add the point i,framedata[1] to the sampledfunction
                sf.addPoint(i, Double.parseDouble(framedata[1]));
            }
            //now do what /PollenDistance does with its sampledfunction

            sd.pollen_distances = new int[sf.y.length];
            //faster(?) is 
            for (int i = 0; i < sd.pollen_distances.length; i++) {
                sd.pollen_distances[i] = (int) Math.floor(sf.y[i]);
            }
            //better is this
            // for(int i=0;i<sd.pollen_distances.length;i++)
            // {
            //    sd.pollen_distances[i]=(int)Math.floor(sf.value(i));
            //}
            //Now do the same with the probabilities
            //First make a sampledfunection
            sf = new SampledFunction();
            for (int i = 0; i < ourFrames.size(); i++) {
                String[] framedata = (String[]) ourFrames.get(i);
                sf.addPoint(Double.parseDouble(framedata[2]), i);
            }
            sd.pollen_distance_probabilities = new DiscreteProbabilityDistribution();
            //better is probably this, which doesn't assume the xml elements are in the right order
            for (int i = 0; i < sf.x.length; i++) {
                sd.pollen_distance_probabilities.addPoint((float) sf.x[i], (float) sf.y[i]);
            }
            sd.pollen_distance_probabilities.normalize();

        }

        //insert this element into its parent in the correct way


        //If it is a locus, normalize the dpd
        //put it in the array of loci

        if (qname.equals("locus")) {

            //pop the locus
            //put it in sd.Loci
            Locus l = (Locus) stack.pop();
            l.dpd.normalize();
            SimData sd = (SimData) stack.peek();
            sd.loci.add(l);

        }
        //end LOCI by setting number_loci to size of loci and triptosize
        if (qname.equals("Reproduction_Rate")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.fruit_production = sf;

        }
        
        //NEW SEED STUFF
        if (qname.equals("SeedGerminationRate")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.seed_germination_rate= sf;

        }
        
        
        if (qname.equals("Dispersal_Distribution")) {

            //pop the the sampled function
            //put it in sd.R_R
            CDF cdf = (CDF) stack.pop();
            //SampledFunction sf=(SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.dispersal_distribution = cdf;

        }
         if (qname.equals("DispersalDirection")) {

            //pop the the sampled function
            //put it in sd.R_R
            CDF cdf = (CDF) stack.pop();
            //SampledFunction sf=(SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.DispersalDirection= cdf;

        }
        

        if (qname.equals("Mortality_Rate")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.mortality = sf;

        }
        if (qname.equals("PollenDirection")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.PollenDirection = sf;

        }
        
        
        //NEW SEED STUFF
        if (qname.equals("SeedMortalityRate")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.seedmortality = sf;

        }
        
        
        //new pollen stuff 7 april 06
        if (qname.equals("Pollen_Distances")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.pollen_distances = new int[sf.y.length];
            //faster(?) is 
            for (int i = 0; i < sd.pollen_distances.length; i++) {
                sd.pollen_distances[i] = (int) Math.floor(sf.y[i]);
            }
            //better is this
            for (int i = 0; i < sd.pollen_distances.length; i++) {
                sd.pollen_distances[i] = (int) Math.floor(sf.value(i));
            }
        }

        if (qname.equals("Pollen_Distance_Probabilities")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            //sd.pollen_distance_probabilities=new DiscreteProbabilityDistribution(sf.x,sf.y);
            sd.pollen_distance_probabilities = new DiscreteProbabilityDistribution();
            //better is probably this, which doesn't assume the xml elements are in the right order
            for (int i = 0; i < sf.x.length; i++) {
                sd.pollen_distance_probabilities.addPoint((float) sf.x[i], (float) sf.y[i]);
            }
            sd.pollen_distance_probabilities.normalize();

        }
        //end new pollen stuff


        if (qname.equals("Pollen_Rate")) {

            //pop the the sampled function
            //put it in sd.R_R
            SampledFunction sf = (SampledFunction) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.pollen_production = sf;

        }
        //Initial_Population --- we can trip sd.plants to size
        //region pop it and put it in sd
        if (qname.equals("Region")) {
            Region r = (Region) stack.pop();
            //SimData sd=(SimData)stack.peek();
            //sd.region=r;
            //when we allow summary subregions, we need
            //to determine whether the parent is
            //the SimData or a summaryregion
            //and cast accordingly
          //  if (stack.peek().getClass().getName().equals("parameters.SimData")) {
          //      SimData sd = (SimData) stack.peek();
              //  sd.region = r;
         //   } else {
                if (stack.peek().getClass().getName().equals("parameters.SummaryRegions")) {
                    SummaryRegions sr = (SummaryRegions) stack.peek();
                    sr.addRegion(r);
                }
                //region is one of the summary subregions
          //  }
        }
        
         if (qname.equals("NewRegion")) {
            NewRegion r = (NewRegion) stack.pop();
            //SimData sd=(SimData)stack.peek();
            //sd.region=r;
            //when we allow summary subregions, we need
            //to determine whether the parent is
            //the SimData or a summaryregion
            //and cast accordingly
            if (stack.peek().getClass().getName().equals("parameters.SimData")) {
                SimData sd = (SimData) stack.peek();
                sd.region = r;
            } 
            //else {
             //   if (stack.peek().getClass().getName().equals("parameters.SummaryRegions")) {
             //       SummaryRegions sr = (SummaryRegions) stack.peek();
             //       sr.addRegion(r);
              //  }
                //region is one of the summary subregions
            
        }
   
 if (qname.equals("NewRectangle")) {
     //pull of the 3 parts of DynamicRect and put them in the dymanic rect
     //onthe stack. Take the DR off and put it in SD.
     //ClustersPerYear
     //SitesPerCluster
     //SiteLifeTime
            //SummaryRegions sr = (SummaryRegions) stack.pop();
            //SimData sd = (SimData) stack.peek();
            //sd.summaryregions = sr;
     NewRectangle dr=(NewRectangle)stack.peek();
    // if(dr.ourType==RECTANGLETYPE.DYNAMIC)
    // {
         dr=(NewRectangle) stack.pop();
        NewRegion reg=(NewRegion) stack.peek();
        reg.addRectangle(dr);
    // }
     
        }          
   /*      
 if (qname.equals("DynamicRect")) {
     //pull of the 3 parts of DynamicRect and put them in the dymanic rect
     //onthe stack. Take the DR off and put it in SD.
     //ClustersPerYear
     //SitesPerCluster
     //SiteLifeTime
            //SummaryRegions sr = (SummaryRegions) stack.pop();
            //SimData sd = (SimData) stack.peek();
            //sd.summaryregions = sr;
     DynamicRect dr=(DynamicRect) stack.pop();
     Region reg=(Region) stack.peek();
     reg.addDRect(dr);
        }        
 */       
 if (qname.equals("ClustersPerYear")) {
     //Shoud have either a CDF or DynamicRect on the stack
     //if CDF, pop it and put it in DR
     if (stack.peek().getClass().getName().equals("functions.CDF")) 
     {
         CDF cdf=(CDF) stack.pop();
         NewRectangle dr= (NewRectangle)stack.peek();
         dr.clustersperyear=cdf;
     }
            
        }  
        
 if (qname.equals("SitesPerCluster")) {
     //Shoud have either a CDF or DynamicRect on the stack
     //if CDF, pop it and put it in DR
     if (stack.peek().getClass().getName().equals("functions.CDF")) 
     {
         CDF cdf=(CDF) stack.pop();
         //DynamicRect dr= (DynamicRect)stack.peek();
          NewRectangle dr= (NewRectangle)stack.peek();
         dr.clustersize=cdf;
     }
            
        }  
 
 if (qname.equals("SiteLifetime")) {
     //Shoud have either a CDF or DynamicRect on the stack
     //if CDF, pop it and put it in DR
     if (stack.peek().getClass().getName().equals("functions.CDF")) 
     {
         CDF cdf=(CDF) stack.pop();
        // DynamicRect dr= (DynamicRect)stack.peek();
          NewRectangle dr= (NewRectangle)stack.peek();
         dr.clusterlifetime=cdf;
     }
            
        }  
 
        if (qname.equals("SummaryRegions")) {
            SummaryRegions sr = (SummaryRegions) stack.pop();
            SimData sd = (SimData) stack.peek();
            sd.summaryregions = sr;
        }

        if (qname.equals("ConvexPolygon")) {
            ConvexPolygon cp = (ConvexPolygon) stack.pop();
            Region r = (Region) stack.peek();

            r.addPoly(cp);

        }
  if (qname.equals("Immigration")) {

            //pop the the sampled function
            //put it in sd.R_R
      
      
     
            SimData sd = (SimData) stack.peek();
              sd.Immigration.setUP();

        }
        /*
         * if(qname.equals("Rectangle")) { Rectangle r= (Rectangle)stack.pop();
         * Region reg=(Region)stack.peek(); reg.addRect(r); }
         *
         */
        /*
         * if(qname.equals("Region")) { SimData sd=(SimData)stack.peek();
         * sd.MakeFinal(); }
         */
    }

    @Override
    public void startElement(String ns, String ln, String qname, Attributes at) throws SAXException {
        //get these in the right order
        //Region
        if (qname.equals("SummaryRegions")) {
            SummaryRegions sr = new SummaryRegions();
            stack.push(sr);
        }
         if (qname.equals("Immigration")) {
             boolean use=false;
             float rate=0;
             float ipfemale=0.5f;
              for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                  if (s.equals("use")) {
                   if(t.equals("true")) use=true;
                }
                  if(s.equals("rate")) {rate=Float.parseFloat(t);}
                  if(s.equals("pfemale")){ipfemale=Float.parseFloat(t);}
                
              }
               SimData sd = (SimData) stack.peek();
               sd.ImmigrationUse=use;
               sd.ImmigrationRate=rate;
               sd.ImmigrationPFemale=ipfemale;
               sd.Immigration=new TwoDLatticePDF();
              /*top should be a SimData, set use and rate
               * END this Immigration by normalizing/checking the vectors setUP()
               * and moving the TwoDLattice to the SimData
               */
          
        }
        if (qname.equals("Region")) {
            int l = 0, r = 0, bot = 0, top = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                } //and now parse "old style" attributes
                else if (s.equals("TOP")) {
                    top = (int) Float.parseFloat(t);
                } else if (s.equals("BOTTOM")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("LEFT")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("RIGHT")) {
                    r = (int) Float.parseFloat(t);
                }
            }
            Region region = new Region(l, bot, r, top);
            stack.push(region);
        }
        
        if (qname.equals("NewRegion")) {
            int l = 0, r = 0, bot = 0, top = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                } //and now parse "old style" attributes
                else if (s.equals("TOP")) {
                    top = (int) Float.parseFloat(t);
                } else if (s.equals("BOTTOM")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("LEFT")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("RIGHT")) {
                    r = (int) Float.parseFloat(t);
                }
            }
            NewRegion region = new NewRegion(l, bot, r, top);
                stack.push(region);
            
        }
        
        
         if (qname.equals("NewRectangle")) {
            int l = 0, r = 0, bot = 0, top = 0;
            float density=0.0f;
            String ourtype="PLAIN";
            String ourname=null;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } 
                else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } 
                else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } 
                else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                }
                else if(s.equals("type")){ ourtype=t;
               
                }
                 else if(s.equals("name")){ ourname=t;
               
                }
                else if(s.equals("density"))
                {
                    density=Float.parseFloat(t);
                }
            }
            NewRectangle rect =null;
           if(ourname==null){
                 rect = new NewRectangle(l, bot, r, top,RECTANGLETYPE.valueOf(ourtype));}
           else
           {
               rect = new NewRectangle(l, bot, r, top,RECTANGLETYPE.valueOf(ourtype),ourname);
           }
            if(rect.ourType==RECTANGLETYPE.RANDOM) rect.density=density;
            stack.push(rect);
            /*
            if(RECTANGLETYPE.valueOf(ourtype)==RECTANGLETYPE.PLAIN|| RECTANGLETYPE.valueOf(ourtype)==RECTANGLETYPE.RANDOM)
            {NewRegion reg = (NewRegion) stack.peek();
            reg.addRectangle(rect);
            }
            if(RECTANGLETYPE.valueOf(ourtype)==RECTANGLETYPE.DYNAMIC)
            {
                stack.push(rect);
            }*/
        }
        
        // /*NEW TO PARSE RECTANGLES INSIDE A REGION
        if (qname.equals("Rectangle")) {
            int l = 0, r = 0, bot = 0, top = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                }
            }
            Rect rect = new Rect(l, bot, r, top);
            //stack.push(rect);
            Region reg = (Region) stack.peek();
            reg.addRect(rect);
        }
        
        
        
        //DYNAMIC RECT GOES HERE 29 DEC 2013
        /*
         if (qname.equals("DynamicRect")) {
             boolean use=false;
            int l = 0, r = 0, bot = 0, top = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("use")){
                    if(t.equals("true"))use=true;
                }
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                }
            }
            DynamicRect rect = new DynamicRect(l, bot, r, top);
            rect.use=use;
            stack.push(rect);
            //Region reg = (Region) stack.peek();
            //reg.addRect(rect);
        }
         */
        if(qname.equals("ClustersPerYear"))
        {
            //get attributes and put them in DynamicRect
            String distribution=null;
            String mean=null;
            String min=null;
            String max=null;
            String value=null;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("distribution")){distribution=t;}
                else if(s.equals("mean")){mean=t;}
                else if(s.equals("min")){min=t;}
                else if(s.equals("max")){max=t;}
                else if(s.equals("value")){value=t;}
            }
           // DynamicRect dr=(DynamicRect)stack.peek();
            NewRectangle dr=(NewRectangle)stack.peek();
            dr.clusternumberdistribution=DISTRIBUTION.valueOf(distribution.toUpperCase());
            if(mean != null) dr.cpy_mean=Float.parseFloat(mean);
            if(min != null) dr.cpy_min=Integer.parseInt(min);
            if(max != null) dr.cpy_max=Integer.parseInt(max);
             if(value != null) dr.cpy_value=Integer.parseInt(value);
            //if we're using CDF, put it on stack
             if(dr.clusternumberdistribution==DISTRIBUTION.CDF)
             {
                 stack.push(new CDF());
             }
            //then we end this by looking if there's a CDF on the stack
        }
        
        
        if(qname.equals("SitesPerCluster"))
        {
            //get attributes and put them in DynamicRect
            String distribution=null;
            String mean=null;
            String min=null;
            String max=null;
            String value=null;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("distribution")){distribution=t;}
                else if(s.equals("mean")){mean=t;}
                else if(s.equals("min")){min=t;}
                else if(s.equals("max")){max=t;}
                else if(s.equals("value")){value=t;}
            }
             NewRectangle dr=(NewRectangle)stack.peek();
            //DynamicRect dr=(DynamicRect)stack.peek();
            dr.clustersizedistribution=DISTRIBUTION.valueOf(distribution.toUpperCase());
            if(mean != null) dr.cs_mean=Float.parseFloat(mean);
            if(min != null) dr.cs_min=Integer.parseInt(min);
            if(max != null) dr.cs_max=Integer.parseInt(max);
             if(value != null) dr.cs_value=Integer.parseInt(value);
            //if we're using CDF, put it on stack
             if(dr.clustersizedistribution==DISTRIBUTION.CDF)
             {
                 stack.push(new CDF());
             }
            //then we end this by looking if there's a CDF on the stack
        }
        
        
            if(qname.equals("SiteLifetime"))
        {
            //get attributes and put them in DynamicRect
            String distribution=null;
            String mean=null;
            String min=null;
            String max=null;
            String value=null;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("distribution")){distribution=t;}
                else if(s.equals("mean")){mean=t;}
                else if(s.equals("min")){min=t;}
                else if(s.equals("max")){max=t;}
                else if(s.equals("value")){value=t;}
            }
             NewRectangle dr=(NewRectangle)stack.peek();
           // DynamicRect dr=(DynamicRect)stack.peek();
            dr.clusterlifedistribution=DISTRIBUTION.valueOf(distribution.toUpperCase());
            if(mean != null) dr.cl_mean=Float.parseFloat(mean);
            if(min != null) dr.cl_min=Integer.parseInt(min);
            if(max != null) dr.cl_max=Integer.parseInt(max);
             if(value != null) dr.cl_value=Integer.parseInt(value);
            //if we're using CDF, put it on stack
             if(dr.clusterlifedistribution==DISTRIBUTION.CDF)
             {
                 stack.push(new CDF());
             }
            //then we end this by looking if there's a CDF on the stack
        }
            
            
        /*
        
        if (qname.equals("SparseRectangle")) {
            int l = 0, r = 0, bot = 0, top = 0;
            float den=0.0f;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("XL")) {
                    l = (int) Float.parseFloat(t);
                } else if (s.equals("XH")) {
                    r = (int) Float.parseFloat(t);
                } else if (s.equals("YL")) {
                    bot = (int) Float.parseFloat(t);
                } else if (s.equals("YH")) {
                    top = (int) Float.parseFloat(t);
                }
                else if (s.equals("density")) {
                    den = Float.parseFloat(t);
                }
            }
            SparseRect rect = new SparseRect(l, bot, r, top,den,null);
            //stack.push(rect);
            Region reg = (Region) stack.peek();
            reg.addRect(rect);
        }
        */
        //If we read a vertex it must belong to a parent ConvexPolygon
        if (qname.equals("Vertex")) {
            float x, y;
            x = 0;
            y = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("x")) {
                    x = (int) Float.parseFloat(t);
                } else if (s.equals("y")) {
                    y = (int) Float.parseFloat(t);
                }
            }
            ConvexPolygon cp = (ConvexPolygon) stack.peek();
            cp.addVertex(x, y);
        }
        //if we get a convexpoly, create one and put it on the stack.
        //end it by adding it to the region
        if (qname.equals("ConvexPolygon")) {
            ConvexPolygon cp = new ConvexPolygon();
            stack.push(cp);
        } //    */
        //we no longer use this but leave it for
        //backward compat. 7 Dec 2007
        else if (qname.equals("regionpoint")) {
            int a = 0, b = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("a")) {
                    a = (int) Float.parseFloat(t);
                } else if (s.equals("b")) {
                    b = (int) Float.parseFloat(t);
                }
            }
            // Region r=(Region)stack.peek();
            //r.addRLEpoint(a,b);
        } else if (qname.equals("Simulation_Data")) {
            SimData element = new SimData();
            stack.push(element);
        } else if (qname.equals("LOCI")) {
            int number_automatic_loci = 0;
            int auto_alleles_per_locus = 0;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("number_loci")) {
                    //Parent is SimData
                    //
                    int number_loci = Integer.parseInt(t);
                    SimData sd = (SimData) stack.peek();
                    sd.number_loci = number_loci;
                }
                if (s.equals("number_automatic_loci")) {
                    //Parent is SimData
                    //
                    number_automatic_loci = Integer.parseInt(t);
                    SimData sd = (SimData) stack.peek();
                    //sd.number_loci += number_automatic_loci;
                    //put the attribute in sd
                    sd.number_automatic_loci=number_automatic_loci;
                }
                if (s.equals("auto_alleles_per_locus")) {
                    auto_alleles_per_locus = Integer.parseInt(t);
                     SimData sd = (SimData) stack.peek();
                     sd.auto_alleles_per_locus=auto_alleles_per_locus ;
                     //put the attribute in sd
                    
                }
                if (auto_alleles_per_locus > 0 && number_automatic_loci > 0) {
                    //create the requested loci
                    for (int ii = 0; ii < number_automatic_loci; ii++) {
                        float prob = 1.0f / auto_alleles_per_locus;
                        Locus l = new Locus();
                        //l.dpd.addPoint()
                        for (int allele = 0; allele < auto_alleles_per_locus; allele++) {
                            l.dpd.addPoint(prob, allele);
                        }
                        l.dpd.normalize();
                        SimData sd = (SimData) stack.peek();
                       
                        sd.loci.add(l);
                    }
                }
            }
        } else if (qname.equals("locus")) {
            Locus l = new Locus();
             for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("action")){l.setAction(t);}
             }
            stack.push(l);
        } else if (qname.equals("dpdpoint")) {
            Locus l = (Locus) stack.peek();
            float x = 0.0f;
            float y = 0.0f;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("x")) {
                    x = Float.parseFloat(t);
                } else if (s.equals("y")) {
                    y = Float.parseFloat(t);
                }
            }
            l.dpd.addPoint(x, y);
        } else if ((qname.equals("Reproduction_Rate"))
                //NEW SEED STUFF
                || (qname.equals("SeedGerminationRate"))
                
                || (qname.equals("Pollen_Rate"))
                || (qname.equals("Mortality_Rate"))
                
                //NEW SEED STUFF
                || (qname.equals("SeedMortalityRate"))
                //March 2014
                || (qname.equals("PollenDirection"))
                //new pollen stuff 7 april 06
                || (qname.equals("Pollen_Distances"))
                || (qname.equals("Pollen_Distance_Probabilities")) //end new pollen stuff
                ) {
            
             for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("use") && qname.equals("PollenDirection")) {
                    if(t.equals("true"))((SimData)stack.peek()).usePollenDirection=true;
                    else if(t.equals("flase"))((SimData)stack.peek()).usePollenDirection=false;
                } 
            }
            
            
            SampledFunction sf = new SampledFunction();
            stack.push(sf);

        } //NEW FOR EASYPOLLEN
        else if (qname.equals("EasyPollen")) {
            //put a list on the stack in which we'll accumulate pollenframes
            //when this element ends, pop it, parse it,
            //and create the Pollen_Distances
            // and Pollen_Distance_Probabilities structures
            stack.push(new ArrayList());
        } else if (qname.equals("pollenframe")) {
            //put the data into the list on the stack
            //get low high &prob
            //we only get apollenframe element inside an EasyPollen
            //element so there's an ArrayList on the stack waiting
            //for our data.
            String framedata[] = new String[3];
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("low")) {
                    framedata[0] = t;
                } else if (s.equals("high")) {
                    framedata[1] = t;
                } else if (s.equals("prob")) {
                    framedata[2] = t;
                }
            }
            ((ArrayList) stack.peek()).add(framedata);
        } else if ((qname.equals("Dispersal_Distribution"))) {
            CDF cdf = new CDF();
            stack.push(cdf);

        }
        

 else if ((qname.equals("DispersalDirection"))) {
     for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("use")) {
                    if(t.equals("true"))
                    {
                ((SimData)    stack.peek()).useDispersalDirection= true;
                    }
                   if(t.equals("false"))
                    {
                ((SimData)    stack.peek()).useDispersalDirection= false;
                    } 
                }
            }
            CDF cdf = new CDF();
            stack.push(cdf);
            
            

        }


//   else if(qname.equals("OffspringDistribution"))
        //   {
        //       //parent is sd
        //        SimData sd=(SimData)stack.peek();
        //   }
        else if (qname.equals("functionpoint")) {
            SampledFunction sf = (SampledFunction) stack.peek();
            float x = 0.0f;
            float y = 0.0f;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("x")) {
                    x = Float.parseFloat(t);
                } else if (s.equals("y")) {
                    y = Float.parseFloat(t);
                }
            }
            sf.addPoint(x, y);
        } else if (qname.equals("Dioecious")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    if (t.equals("false")) {
                        sd.dioeceous = false;
                    } else {
                        sd.dioeceous = true;
                    }
                }
            }
        } 
        
        else if(qname.equals("IBD_ReproductionEffect"))
        {
            double[] xx=null;
            double[] yy=null;
            SimData sd = (SimData) stack.peek();
             for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("x"))
                {
                    String[] xs=t.split(",");
                    xx=new double[xs.length];
                    for(int ii=0;ii<xs.length;ii++)
                    {
                        xx[ii]=Double.parseDouble(xs[ii]);
                    }
                }
                if(s.equals("y"))
                {
                     String[] ys=t.split(",");
                    yy=new double[ys.length];
                    for(int ii=0;ii<ys.length;ii++)
                    {
                        yy[ii]=Double.parseDouble(ys[ii]);
                    }
                }
             }
             assert (xx!= null && yy!= null): "Problem with ReproductionEffect";
             sd.IBD_ReproductionEffect=new SampledFunction(xx,yy);
        }
        else if(qname.equals("IBD_MortalityEffect"))
        {
            double[] xx=null;
            double[] yy=null;
            double[] ff=null;
            SimData sd = (SimData) stack.peek();
             for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("x"))
                {
                    String[] xs=t.split(",");
                    xx=new double[xs.length];
                    for(int ii=0;ii<xs.length;ii++)
                    {
                        xx[ii]=Double.parseDouble(xs[ii]);
                    }
                }
                if(s.equals("y"))
                {
                     String[] ys=t.split(",");
                    yy=new double[ys.length];
                    for(int ii=0;ii<ys.length;ii++)
                    {
                        yy[ii]=Double.parseDouble(ys[ii]);
                    }
                }
                
                 if(s.equals("f"))
                {
                     String[] fs=t.split(",");
                    ff=new double[fs.length];
                    for(int ii=0;ii<fs.length;ii++)
                    {
                        ff[ii]=Double.parseDouble(fs[ii]);
                    }
                }
             }
             assert (xx!= null && yy!= null && ff!=null): "Problem with MortalityEffect";
             double [][] fff=new double[xx.length][];
             for(int ii=0;ii<xx.length;ii++)
             {
                 fff[ii]=new double[yy.length];
                 for(int jj=0;jj<yy.length;jj++)
                
                 {fff[ii][jj]=ff[ii*yy.length+jj]; //xx.length
                         }
                         }
             
             
             sd.IBD_MortalityEffect=new BilinearInterp(xx,yy,fff);
        }
        
        
        
        
        
            else if(qname.equals("InbreedingDepression"))
        {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("use"))
                {
                    if(t.equals("true"))
                    {
                        sd.IBD_use=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.IBD_use=false;
                    }
                }
             
                if(s.equals("depression_reproduction"))
                {
                     if(t.equals("true"))
                    {
                        sd.IBD_use__reproduction=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.IBD_use__reproduction=false;
                    }
                }
                if(s.equals("depression_mortality"))
                {
                    if(t.equals("true"))
                    {
                        sd.IBD_use__mortality=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.IBD_use__mortality=false;
                    }
                }
                if(s.equals("depressionloci"))
                {
                    sd.IBD_loci=parseRLEList(t);
                }
            }
        }
        
        
              else if(qname.equals("Agamospermy"))
        {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("use"))
                {
                    if(t.equals("true"))
                    {
                        sd.Agamospermy_use=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.Agamospermy_use=false;
                    }
                }
             
                if(s.equals("method"))
                {
                    sd.Agamospermy_method=t;
                }
                if(s.equals("probability"))
                {
                   sd.Agamospermy_probability= Float.parseFloat(t);
                }
            }
        }
        
              else if(qname.equals("SeedCollection"))
        {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("use"))
                {
                    if(t.equals("true"))
                    {
                        sd.useSeedCollection=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.useSeedCollection=false;
                    }
                }
                if(s.equals("subregions_only"))
                {
                    if(t.equals("true"))
                    {
                        sd.SeedCollectionsubregions_only=true;
                    }
                    if(t.equals("false"))
                    {
                       sd.SeedCollectionsubregions_only =false;
                    }
                }
                if(s.equals("seed_dump_file_name"))
                {
                    sd.seeddumpfilename=t;
                }
                if(s.equals("last_r"))
                {
                   sd.SeedCollectionlast_r= Float.parseFloat(t);
                }
            }
        }
        else if(qname.equals("Compatibility"))
        {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("value"))
                {
                    if(t.equals("true"))
                    {
                        sd.compatibility=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.compatibility=false;
                    }
                }
                if(s.equals("compatibility_fraction"))
                {
                   sd.compatibility_fraction= Float.parseFloat(t);
                }
                if(s.equals("locus"))
                {
                   sd.compatibility_locus= Integer.parseInt(t);
                }
            }
        }
         else if(qname.equals("MakeMaps"))
        {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                 String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("use"))
                {
                    if(t.equals("true"))
                    {
                        sd.makeMap=true;
                    }
                    if(t.equals("false"))
                    {
                        sd.makeMap=false;
                    }
                }
                if(s.equals("pixelsPerSite"))
                {
                    sd.pixelsPerSite=Integer.parseInt(t);
                }
               
            }
        }
        
        
        
        else if (qname.equals("Offspring_Distribution")) {
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("method")) {
                    if (t.equals("round")) {
                        sd.off_dist_method = "round";
                    } else if (t.equals("bracket")) {
                        sd.off_dist_method = "bracket";
                    } else if (t.equals("poisson")) {
                        sd.off_dist_method = "poisson";
                    }
                }
            }
        } //NEW SEED STUFF
//        else if (qname.equals("SeedDistribution")) {
//            SimData sd = (SimData) stack.peek();
//            for (int i = 0; i < at.getLength(); i++) {
//                String s = at.getQName(i);
//                String t = at.getValue(i);
//                if (s.equals("method")) {
//                    if (t.equals("round")) {
//                        sd.seed_dist_method = "round";
//                    } else if (t.equals("bracket")) {
//                        sd.seed_dist_method = "bracket";
//                    } else if (t.equals("poisson")) {
//                        sd.seed_dist_method = "poisson";
//                    }
//                }
//            }
//        } 
        else if (qname.equals("Number_Generations")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    sd.number_generations = (int) Math.round(Float.parseFloat(t));
                }
            }
        } else if (qname.equals("Selfing_Rate")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    sd.selfing_rate = Float.parseFloat(t);
                }
            }
        } else if (qname.equals("FoundersF")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    sd.foundersf = Float.parseFloat(t);

                }
            }
        } 
     //   else if (qname.equals("SeedBankMixInRate")) {
     //       //parent is SimData
     //       SimData sd = (SimData) stack.peek();
     //       for (int i = 0; i < at.getLength(); i++) {
     //           String s = at.getQName(i);
     //           String t = at.getValue(i);
     //           if (s.equals("value")) {
     //               sd.seedbankmixinrate= Float.parseFloat(t);

     //           }
     //       }
     //   }
         else if (qname.equals("CreateAllFounders")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    if (t.equals("true")) {
                        sd.CreateAllFounders = true;
                    } else {
                        sd.CreateAllFounders = false;
                    }
                    //sd.selfing_rate=Float.parseFloat(t);
                }
            }
        }
        else if (qname.equals("RandomMating")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    if (t.equals("true")) {
                        sd.random_mating = true;
                    } else {
                        sd.random_mating = false;
                    }
                    //sd.selfing_rate=Float.parseFloat(t);
                }
            }
        } else if (qname.equals("Number_Runs")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    sd.number_runs = (int) Math.round(Float.parseFloat(t));
                }
            }
        } 
        
        else if (qname.equals("DataInterval")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("value")) {
                    sd.datainterval = (int) Math.round(Float.parseFloat(t));
                }
            }
        } 
        
//        else if (qname.equals("Fruit_Distance")) {
//            //parent is SimData
//            SimData sd = (SimData) stack.peek();
//            for (int i = 0; i < at.getLength(); i++) {
//                String s = at.getQName(i);
//                String t = at.getValue(i);
//                if (s.equals("value")) {
//                    sd.fruit_distance = Float.parseFloat(t);
//                }
//            }
//        }
//        else if (qname.equals("Pollen_Distance")) {
//            //parent is SimData
//            SimData sd = (SimData) stack.peek();
//            for (int i = 0; i < at.getLength(); i++) {
//                String s = at.getQName(i);
//                String t = at.getValue(i);
//                if (s.equals("value")) {
//                    sd.pollen_distance = Float.parseFloat(t);
//                }
//            }
//        } 
        else if (qname.equals("Vector")){
            String ourname=null, ourlength=null,ourvalues=null;
             for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if(s.equals("name")) ourname=t;
                if(s.equals("length")) ourlength=t;
                if(s.equals("values")) ourvalues=t;
             }
             if(ourname.equals("maps"))
             {
                 SimData sd = (SimData) stack.peek();
                 int len=Integer.parseInt(ourlength);
                 int []ourvect=new int[len];
                 String [] vals=ourvalues.split(",");
                 for(int i=0;i<len;i++) {
                     ourvect[i]=Integer.parseInt(vals[i]);
                         }
            
            sd.mapYears=ourvect;   
             }
             
             //Immigration data
             if(ourname.equals("x") )
             {
                 SimData sd = (SimData) stack.peek();
                 int len=Integer.parseInt(ourlength);
                 int []ourvect=new int[len];
                 String [] vals=ourvalues.split(",");
                 for(int i=0;i<len;i++) {
                     ourvect[i]=Integer.parseInt(vals[i]);
                         }
            
            sd.Immigration.setX(ourvect);   
             }
             if(ourname.equals("y") )
             {
                 int len=Integer.parseInt(ourlength);
                 int []ourvect=new int[len];
                 String [] vals=ourvalues.split(",");
                 for(int i=0;i<len;i++) {
                     ourvect[i]=Integer.parseInt(vals[i]);
                 }
                 
            SimData sd = (SimData) stack.peek();
            sd.Immigration.setY(ourvect);
            
                 
             }
             if(ourname.equals("f"))
             {
                 int len=Integer.parseInt(ourlength);
                  float []ourvect=new float[len];
                 String [] vals=ourvalues.split(",");
                 for(int i=0;i<len;i++) {
                     ourvect[i]=Float.parseFloat(vals[i]);
                 }
                 
               SimData sd = (SimData) stack.peek();
            sd.Immigration.setF(ourvect);
             }
             //END oF Immigration Data
             //NOW CONSIDER OTHER USES OF Vectors
             //
        } 
        else if (qname.equals("Plant")) {
            //parent is SimData
            SimData sd = (SimData) stack.peek();
            int age = 0, X = 0, Y = 0;
            boolean femaleP = true;
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                //age,X,Y,femaleP
                if (s.equals("age")) {
                    age = (int) Math.round((double) Float.parseFloat(t));
                } else if (s.equals("X")) {
                    X = (int) Math.round((double) Float.parseFloat(t));
                } else if (s.equals("Y")) {
                    Y = (int) Math.round((double) Float.parseFloat(t));
                } else if (s.equals("femaleP")) {
                    if (t.equals("true")) {
                        femaleP = true;
                    } else {
                        femaleP = false;
                    }
                }
            }
            //make a Plant
            Location l = new Location(X, Y);
            //dump it if not in our Region
            //number_loci=loci.size();
            Plant ourplant = new Plant(sd.loci.size(), age, l);
            ourplant.female = femaleP;
            ourplant.parent1 = -1;
            ourplant.parent2 = -1;
            sd.plants.add(ourplant);

        }   else if (qname.equals("RandomPlantList")) {
            RandomPlantList rpl = new RandomPlantList();
            for (int i = 0; i < at.getLength(); i++) {
                String s = at.getQName(i);
                String t = at.getValue(i);
                if (s.equals("number")) {
                    rpl.number = Integer.parseInt(t);

                }
                if (s.equals("XL")) {
                    rpl.XL = Integer.parseInt(t);

                }
                if (s.equals("XH")) {
                    rpl.XH = Integer.parseInt(t);

                }
                if (s.equals("YL")) {
                    rpl.YL = Integer.parseInt(t);

                }
                if (s.equals("YH")) {
                    rpl.YH = Integer.parseInt(t);

                }
                if (s.equals("agelow")) {
                    rpl.agelo = Integer.parseInt(t);

                }
                if (s.equals("agehi")) {
                    rpl.agehi = Integer.parseInt(t);

                }
                if (s.equals("probfemale")) {
                    rpl.probfemale = Float.parseFloat(t);

                }

                if (s.equals("numberfemale")) {
                    rpl.numberfemale = Integer.parseInt(t);

                }


            }
            SimData sd = (SimData) stack.peek();
            sd.randomfounders.add(rpl);
        }



    }
     private int [] parseRLEList(String s)
    {
        int c=0;
        ArrayList<Integer> ans=new ArrayList<Integer>();
        String []temp=s.split(",");
        for(String t:temp)
        {
            if(t.contains("-"))
            {
                String [] parts=t.split("-");
                int low,high;
                low=Integer.parseInt(parts[0]);
                high=Integer.parseInt(parts[1]);
                for(int i=low;i<=high;i++)ans.add(i);
            }
            else
            {
                ans.add(Integer.parseInt(t));
            }
        }
        
        int[] ret=new int[ans.size()];
       
        for(int i:ans) ret[c++]=i;
        return ret;
    }
}
