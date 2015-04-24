/*
 * Copyright (C) 2014 pelikan
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
package statistics;

/*
 * Image.java
 *
 * Created on Dec 29, 2013 
 *
 */
/**
 * Map is a class for drawing maps. But unlike Picture, it is made to be called
 * at runtime. It can indicate the distribution of viable sites or the
 * distribution of viable and occupied sites. It creates a PostScript file for
 * generating the graphic.
 *
 * Region is illustrated as white vs gray for viable vs not viable Live plant is
 * illustrated as green Seed is illustrated as light/dark blue depending on
 * whether site is viable or not
 *
 * @author Steve
 */
import biology.Plant;
import functions.RandomValue;
import parameters.SimData;
import parameters.SimDataReader;
import javax.xml.parsers.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

public class Map {

    SimData ourSD;
    NumberFormat nf;
    //parameters that control what the pictures look like
    float PictureWidth, PictureHeight;
    float ScaleFactor;
    float maxRadius;
    int maxAge;
    float[] GridCorners;
    float GridWidth, GridHeight;
    ArrayList<Plant> population;
    ArrayList<Plant> seeds;
    //parameters that determine what we draw
    // boolean traceFounder = false;
    // int[] yearsToPlot;
    //currently we'll only do one run
    // int[] runsToPlot = {0};

    /**
     * Creates a new instance of Map
     */
    public Map() {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
    }

    /*
     public Map(String[] args) {
     //read in the simdata structure that generated the dump
     getSimData(args[0]);
     readPictureData(args[1]);
     setupParameters();
     drawPicture(args[2]);
     }
     */
    /**
     * Here's a version that can be created at run time and asked to do various
     * things
     *
     * @param sd
     * @param population
     */
    public Map(SimData sd, ArrayList<Plant> pop) {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        ourSD = sd;
        this.population = pop;
        setupParameters();

    }

    /**
     * And this version is called with all the information to generate 3 maps
     *
     */
    public Map(SimData sd, ArrayList<Plant> pop, ArrayList<Plant> seeds) {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        ourSD = sd;
        this.population = pop;
        this.seeds = seeds;
        setupParameters();

    }

    public void doAllMaps() {
        //generate file names
        //Year_n_Region Year_n_Population Year_n_Seedbank
        StringBuilder sb=new StringBuilder();
        String Region=sb.append("Region-"+ourSD.current_year+".ps").toString();
        sb=new StringBuilder();
        String Population=sb.append("Population-"+ourSD.current_year+".ps").toString();
        sb=new StringBuilder();
        String Seedbank=sb.append("Seedbank-"+ourSD.current_year+".ps").toString();
       
        PrintWriter writer = null;
       
        setPictureSize(new float[]{6, 4});
        try {
            writer = new PrintWriter(Region, "UTF-8");
            String map = makeMap(true);
            writer.println(map);

            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
   
 
        try {
            writer = new PrintWriter(Population, "UTF-8");
            String map = plotPopulation(0, 0.5f, 0);
            writer.println(map);

            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        setPopulation(seeds);
        try {
            writer = new PrintWriter(Seedbank, "UTF-8");
            String map = plotPopulation(0, 0.15f, 0.5f);
            writer.println(map);

            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    /*
     private void readPictureData(String filename) {
     BufferedReader br = null;
     try {
     FileInputStream fis = new FileInputStream(filename);
     InputStreamReader isr = new InputStreamReader(fis);
     br = new BufferedReader(isr);
     } catch (FileNotFoundException e) {
     System.out.println(e.toString());
     }
     int line_number = 0;
     String line;

     try {
     line = br.readLine();
     String[] vals = line.split("\\s*,\\s*");
     PictureWidth = Integer.parseInt(vals[0]);
     PictureHeight = Integer.parseInt(vals[1]);
     line = br.readLine();
     vals = line.split("\\s*,\\s*");
     yearsToPlot = new int[vals.length];
     for (int v = 0; v < vals.length; v++) {
     yearsToPlot[v] = Integer.parseInt(vals[v]);
     }
     line = br.readLine();
     maxAge = Integer.parseInt(line);
     } catch (IOException e) {
     System.out.println(e.toString());
     } catch (NumberFormatException e) {
     System.out.println(e.toString());
     }

     }
     */

    /**
     * Read SimData form an .xml file name
     *
     * This is primarily for testing. In practice we'd just use setSimData() to
     * provide the sd.
     *
     * @param xmlfilename
     */

    private void getSimData(String xmlfilename) {
        SimDataReader my_mb = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            org.xml.sax.XMLReader parser = saxParser.getXMLReader();
            my_mb = new SimDataReader();
            parser.setContentHandler(my_mb);
            try {
                parser.parse(xmlfilename);
            } catch (IOException e) {
                System.out.println("Parsing error in SimData(xmlfilename) " + e.toString());
            } catch (SAXException e) {
                System.out.println("Parsing error in SimData(xmlfilename) " + e.toString());
            }
        } catch (ParserConfigurationException e) {
            System.out.println("Problem getting a parser in SimDataReader()");
        } catch (SAXException e) {
            System.out.println("Problem getting a parser in SimDataReader()");
        }
        ourSD = my_mb.getSD();
    }

    private void setupParameters() {
        GridCorners = new float[4];
        GridCorners[0] = ourSD.region.LEFT;
        GridCorners[1] = ourSD.region.BOTTOM;
        GridCorners[2] = ourSD.region.RIGHT;
        GridCorners[3] = ourSD.region.TOP;

        PictureWidth = PictureWidth * 72; //inches times pt per inch
        PictureHeight = PictureHeight * 72;
        GridWidth = GridCorners[2] - GridCorners[0];
        GridHeight = GridCorners[3] - GridCorners[1];
        float a = PictureWidth / GridWidth;
        float b = PictureHeight / GridHeight;
        ScaleFactor = (a > b) ? a : b;
        maxRadius = ScaleFactor / 2;
    }

    /**
     * runtime version. Specify the width and height of the final picture in
     * inches
     *
     * @param s
     */
    public void setPictureSize(float[] s) {
        PictureWidth = s[0];
        PictureHeight = s[1];
        setupParameters();
    }

    /**
     * By handing different lists we can plot both the population and the
     * seedbank
     *
     * @param pop
     */
    public void setPopulation(ArrayList<Plant> pop) {
        this.population = pop;
    }

    /**
     * Read the population from a binary file extracting a List<Plant> for the
     * specified year and run store this in our variable population
     *
     * @param binaryfile
     * @param run
     * @param year
     */
    public void readPopulation(String binaryfile, int run, int year) {

    }

    public void setSimData(SimData sd) {
        ourSD = sd;
    }

    public String plotPopulation(float R, float G, float B) {
        //plot the viable sites
        StringBuilder sb = new StringBuilder(makeMap(false));
        float dx = PictureWidth / GridWidth;
        float dy = PictureHeight / GridHeight;
        float green = G;
        float red = R;
        float blue = B;

        for (Plant p : population) {
            float x = ScaleFactor * (p.location.X - ourSD.region.LEFT);
            float y = ScaleFactor * (p.location.Y - ourSD.region.BOTTOM);
            float age = ourSD.current_year - p.dob;
            //draw a greenish box with darkness age/maxage
            sb.append("").append(red).append(" ").append(green).append(" ").append(blue).append(" ").append(nf.format(x)).append(" ").append(nf.format(y)).append(" cbox\n");

        }

        return sb.toString();
    }

    public String BoundingBox() {
        StringBuilder sb = new StringBuilder("0 setgray 1 setlinewidth\n");
        float dx = PictureWidth / GridWidth;
        float dy = PictureHeight / GridHeight;
        float x = ScaleFactor * (ourSD.region.LEFT - ourSD.region.LEFT);
        float y = ScaleFactor * (ourSD.region.BOTTOM - ourSD.region.BOTTOM);
        sb.append("" + x + " " + y + " newpath moveto\n");
        x = ScaleFactor * (ourSD.region.RIGHT - ourSD.region.LEFT) + dx;
        //y= ScaleFactor*(ourSD.region.BOTTOM-ourSD.region.BOTTOM);
        sb.append("" + x + " " + y + " lineto\n");
        y = ScaleFactor * (ourSD.region.TOP - ourSD.region.BOTTOM) + dy;
        sb.append("" + x + " " + y + " lineto\n");
        x = ScaleFactor * (ourSD.region.LEFT - ourSD.region.LEFT);
        sb.append("" + x + " " + y + " lineto\n");
        sb.append(" closepath stroke");
        return sb.toString();
    }

    /**
     * A runtime command to generate a map of viable sites especially useful
     * when working with Sparse or Dynamic Rectangles
     *
     * @return a String with PS commands to draw the map
     *
     * default behaviour is viable sites are white, nonviable sites are gray.
     *
     * The small rectangles we draw will have (in points) dimensions so that
     * GridWidth (number sites)*RectWidth (points/site) = PictureWidth (points)
     *
     * dx=PictureWidth/GridWidth; The PS command should be gray setgray newpath
     * xl yl moveto dx 0 rlineto 0 dy rlineto -dx 0 rlineto 0 -dy rlineto
     * closepath fill
     */
    public String makeMap(boolean show) {
        StringBuilder sb = new StringBuilder("%!PS\n");
        sb.append("20 20 translate\n");
        sb.append(BoundingBox());
        float gray; //1= white, 0=black
        float dx = PictureWidth / GridWidth;
        float dy = PictureHeight / GridHeight;
        //loop over all the sites in the Region
        //drawing a small rect for each
        sb.append("/dx{ " + nf.format(dx) + "} def\n");
        sb.append("/dy{ " + nf.format(dy) + "} def\n");
        sb.append("/box{ newpath  moveto\n"
                + "dx 0 rlineto\n"
                + "0 dy rlineto\n"
                + "-1 dx mul 0 rlineto\n"
                + "closepath\n"
                + "setgray fill\n} def\n");
        sb.append("/cbox{ newpath  moveto\n"
                + "dx 0 rlineto\n"
                + "0 dy rlineto\n"
                + "-1 dx mul 0 rlineto\n"
                + "closepath\n"
                + "setrgbcolor fill\n} def\n");
        // sb.append("0 0  newpath  moveto\n"+PictureWidth+" rlineto"+"0 "+PictureHeight+" rlineto"
        // +"-1 "+PictureWidth+" mul 0 rlineto closepath 0 setgray stroke\n");

        for (int col = ourSD.region.LEFT; col <= ourSD.region.RIGHT; col++) {
            for (int row = ourSD.region.BOTTOM; row <= ourSD.region.TOP; row++) {
                //what color do we want?
                if (ourSD.region.inRegion(col, row)) {
                    gray = 1.0f;
                } else {
                    gray = 0.5f;
                }
                //Where, in pt, does this site live on
                //our map?
                float x = ScaleFactor * (col - ourSD.region.LEFT);
                float y = ScaleFactor * (row - ourSD.region.BOTTOM);
                sb.append("" + gray + " " + nf.format(x) + " " + nf.format(y) + " box\n");

            }
        }
        if (show) {
            sb.append("showpage\n");
        }

        return sb.toString();
    }
    /*
     public void drawPicture(String filename) {
     //get the dump file for our data
     NumberFormat nf = NumberFormat.getInstance();
     StringBuilder sb = new StringBuilder();
     sb.append("0.5 setlinewidth\n");
     nf.setMaximumFractionDigits(3);
     File ourFile = new File(filename);
     if (!ourFile.exists()) {
     System.out.println("Can't open the dump file: " + filename);
     }
     //eventually include this all in a loop on runs
     //loop for each year

     for (int yi = 0; yi < yearsToPlot.length; yi++) {
     int ourYear = yearsToPlot[yi];
     BufferedReader br = null;
     try {
     FileInputStream fis = new FileInputStream(filename);
     InputStreamReader isr = new InputStreamReader(fis);
     br = new BufferedReader(isr);
     } catch (Exception e) {
     System.out.println(e.toString());
     }
     int line_number = 0;
     String line;

     //get a file to write the PS to
     //get a StringBuilder to accumulate PS
     try {
     drawloop:
     while ((line = br.readLine()) != null) {
     String[] vals = line.split("\\s*,\\s*");
     int ourRun = Integer.parseInt(vals[0]);
     int ourDOB = Integer.parseInt(vals[4]);
     int ourDOD = Integer.parseInt(vals[5]);
     int ourX = Integer.parseInt(vals[8]);
     int ourY = Integer.parseInt(vals[9]);
     float ourAge = ourYear - ourDOB;
     if ((ourAge >= 0) && ((ourDOD >= ourYear) || (ourDOD == -1))) {
     float x = ScaleFactor * (1 + ourX);
     float y = ScaleFactor * (1 + ourY);
     float rr = 1 + ((float) ourAge / maxAge) * maxRadius;
     float xpr = x + rr;
     sb.append("newpath " + nf.format(xpr) + " " + nf.format(y) + " moveto " + nf.format(x) + " " + nf.format(y) + " " + nf.format(rr) + " 0 360 arc stroke\n");
     } else {
     continue drawloop;
     }

     }
     } catch (NumberFormatException ex) {
     ex.printStackTrace();
     } catch (IOException ex) {
     ex.printStackTrace();
     }
     sb.append("showpage\n");
     sb.append("0.5 setlinewidth\n");

     }
     try {

     File outfile = new File("testout.ps");
     FileWriter fw = new FileWriter(outfile);
     BufferedWriter bw = new BufferedWriter(fw);
     PrintWriter pw = new PrintWriter(bw, true);
     pw.print(sb.toString());
     pw.flush();
     pw.close();
     } catch (IOException ex) {
     ex.printStackTrace();
     }
     }
     */

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        PrintWriter writer = null;
        Map m = new Map();
        m.getSimData("MASTERSimData.xml");
        RandomValue rv = new RandomValue();

        //test the onMap() and inDynamic() methods
        System.out.println(m.ourSD.region.onMap(55, 50));
        System.out.println(m.ourSD.region.inRegion(55, 50));
        //   System.out.println(m.ourSD.region.inDynamicRect(55, 50));

        m.ourSD.region.initialize();
        m.ourSD.region.update(1, rv);
        // m.ourSD.region.makeInstances(rv);
        //  m.ourSD.region.updateDynamics(m.ourSD,rv);
        m.setPictureSize(new float[]{6, 4});
        try {
            writer = new PrintWriter("mapout0.ps", "UTF-8");
            String map = m.makeMap(true);
            writer.println(map);

            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }

        m.ourSD.current_year = 1;
        m.ourSD.region.update(m.ourSD.current_year, rv);

        // m.ourSD.region.updateDynamics(m.ourSD,rv);
        m.setPictureSize(new float[]{6, 4});
        //m.setupParameters();

        try {
            writer = new PrintWriter("mapout1.ps", "UTF-8");
            String map = m.makeMap(true);
            writer.println(map);

            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(m.BoundingBox());
    }

}
