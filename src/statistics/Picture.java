package statistics;

/*
 * Picture.java
 *
 * Created on April 4, 2006, 9:48 PM
 *
 */

/**
 *Picture reads a data dump from a NewGarden run and makes one of more
 *postscript pictures showing the distribution of individuals and their ages
 *for selected years during the simulation.
 *
 *
 *This program is called with the ff arguments
 *1) simdata.xml filename
 *2) picturedata.txt
 *3) dump file name
 *
 *The simdata.xml file is the one used to generate the dump file
 *the dump file name is the output from the model run
 *the picturedata.txt file has just a few lines, csv, and tells
 *what is to be drawn:
 *The format is
 *
 *picturewidth,pictureheight (in inches)
 *years_to_plot (as csv)
 *maxAge
 *
 * @author Steve
 */
import biology.Plant;
import parameters.SimData;
import parameters.SimDataReader;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Picture {
    SimData ourSD;
    //parameters that control what the pictures look like
    float PictureWidth,PictureHeight;
    float ScaleFactor;
    float maxRadius;
    int maxAge;
    float [] GridCorners;
    float GridWidth,GridHeight;
    ArrayList<Plant> population;
    //parameters that determine what we draw
    boolean traceFounder=false;
    int [] yearsToPlot;
    //currently we'll only do one run
    int [] runsToPlot={0};
    /** Creates a new instance of Picture */
    public Picture(String [] args) {
        //read in the simdata structure that generated the dump
        getSimData(args[0]);
        readPictureData(args[1]);
        setupParameters();
        drawPicture(args[2]);
    }
    /**
     * Here's a version that can be created at run time and asked to do various things
     * @param sd
     * @param population 
     */
    public Picture(SimData sd, ArrayList<Plant> pop )
    {
        ourSD=sd;
        this.population=pop;
         setupParameters();
        
    }
    private void readPictureData(String filename) {
        BufferedReader br=null;
        try{
            FileInputStream fis=new FileInputStream(filename);
            InputStreamReader isr=new InputStreamReader(fis);
            br=new BufferedReader(isr);
        }catch(Exception e){System.out.println(e.toString());}
        int line_number=0;
        String line;
        
        try{
            line=br.readLine();
            String [] vals=line.split("\\s*,\\s*");
            PictureWidth=Integer.parseInt(vals[0]);
            PictureHeight=Integer.parseInt(vals[1]);
            line=br.readLine();
            vals=line.split("\\s*,\\s*");
            yearsToPlot=new int[vals.length];
            for(int v=0;v<vals.length;v++) {
                yearsToPlot[v]=Integer.parseInt(vals[v]);
            }
             line=br.readLine();
             maxAge=Integer.parseInt(line);
        }catch(Exception e){System.out.println(e.toString());}
        
    }
    private void getSimData(String xmlfilename) {
        SimDataReader my_mb=null;
        try {
            SAXParserFactory factory=SAXParserFactory.newInstance();
            SAXParser saxParser=factory.newSAXParser();
            org.xml.sax.XMLReader parser=saxParser.getXMLReader();
            my_mb=new SimDataReader();
            parser.setContentHandler(my_mb);
            try {
                parser.parse(xmlfilename);
            } catch(Exception e) {
                System.out.println("Parsing error in SimData(xmlfilename) "+e.toString());}
        } catch(Exception e) {
            System.out.println("Problem getting a parser in SimDataReader()");
        }
        ourSD=my_mb.getSD();
    }
    private void setupParameters() {
        GridCorners=new float[4];
        GridCorners[0]=ourSD.region.LEFT;
        GridCorners[1]=ourSD.region.BOTTOM;
        GridCorners[2]=ourSD.region.RIGHT;
        GridCorners[3]=ourSD.region.TOP;
        
        PictureWidth= PictureWidth*72; //inches times pt per inch
        PictureHeight=PictureHeight*72;
        GridWidth=GridCorners[2]-GridCorners[0];
        GridHeight=GridCorners[3]-GridCorners[1];
        float a=PictureWidth/GridWidth;
        float b=PictureHeight/GridHeight;
        ScaleFactor = (a>b)? a: b;
        maxRadius=ScaleFactor/2;
    }
    /**
     * runtime version. Specify the width and
     * height of the final picture in inches
     * @param s 
     */
    public void setPictureSize(float [] s)
    {
        PictureWidth=s[0];
        PictureHeight=s[1];
        setupParameters();
    }
    /**
     * A runtime command to generate a map of viable sites
     * especially useful when working with Sparse or Dynamic Rectangles
     * @return a String with PS commands to draw the map
     * 
     * default behaviour is viable sites are white, nonviable sites are gray.
     * 
     * The small rectangles we draw will have (in points) dimensions  so that
     * GridWidth (number sites)*RectWidth (points/site) = PictureWidth (points)
     * 
     * dx=PictureWidth/GridWidth;
     * The PS command should be
     * gray setgray
     * newpath xl yl moveto  dx 0 rlineto 0 dy rlineto
     * -dx 0 rlineto 0 -dy rlineto 
     * closepath
     * fill
     */
    public String makeMap()
    {
        StringBuilder sb=new StringBuilder( "%!PS\n");
        float gray; //1= white, 0=black
        float dx=PictureWidth/GridWidth;
        float dy=PictureHeight/GridHeight;
        //loop over all the sites in the Region
        //drawing a small rect for each
        sb.append("/dx{ "+dx+"} def\n");
        sb.append("/dy{ "+dy+"} def\n");
        sb.append("/box{ newpath  moveto\n" +
"dx 0 rlineto\n" +
"0 dy rlineto\n" +
"-1 dx mul 0 rlineto\n" +
"closepath\n" +
"setgray fill\n} def\n");

        for(int col=ourSD.region.LEFT;col<=ourSD.region.RIGHT;col++)
        {
            for(int row=ourSD.region.BOTTOM;row<=ourSD.region.TOP;row++)
            {
                //what color do we want?
                if(ourSD.region.inRegion(col, row))
                {
                    gray=1.0f;
                }
                else
                {
                    gray=0.5f;
                }
                //Where, in pt, does this site live on
                //our map?
                float x=ScaleFactor * (col-ourSD.region.LEFT);
                float y=ScaleFactor*(row-ourSD.region.BOTTOM);
                sb.append(""+gray+" "+x+" "+y+" box\n");
                
                
                
                
            }
        }
        sb.append("showpage\n");
        
        return sb.toString();
    }
    public void drawPicture(String filename) {
        //get the dump file for our data
        NumberFormat nf=NumberFormat.getInstance();
         StringBuilder sb=new StringBuilder();
         sb.append("0.5 setlinewidth\n");
        nf.setMaximumFractionDigits(3);
        File ourFile=new File(filename);
        if(! ourFile.exists()) {
            System.out.println("Can't open the dump file: "+filename);
        }
        //eventually include this all in a loop on runs
        //loop for each year
        
        for(int yi=0;yi<yearsToPlot.length;yi++) {
            int ourYear=yearsToPlot[yi];
            BufferedReader br=null;
            try{
                FileInputStream fis=new FileInputStream(filename);
                InputStreamReader isr=new InputStreamReader(fis);
                br=new BufferedReader(isr);
            }catch(Exception e){System.out.println(e.toString());}
            int line_number=0;
            String line;
            
            //get a file to write the PS to
            //get a StringBuilder to accumulate PS
           
            try {
                drawloop: while((line=br.readLine())!= null)
                {
                    String [] vals=line.split("\\s*,\\s*");
                    int ourRun=Integer.parseInt(vals[0]);
                    int ourDOB=Integer.parseInt(vals[4]);
                     int ourDOD=Integer.parseInt(vals[5]);
                     int ourX=Integer.parseInt(vals[8]);
                     int ourY=Integer.parseInt(vals[9]);
                     float ourAge=ourYear-ourDOB;
                   if(  (ourAge >=0) && ((ourDOD>= ourYear) || (ourDOD==-1)))
                   {
                     float x=ScaleFactor * (1+ourX);
                     float y=ScaleFactor*(1+ourY);
                     float rr= 1+((float)ourAge/maxAge)*maxRadius;
                     float xpr=x+rr;
                     sb.append("newpath "+nf.format(xpr)+" "+nf.format(y)+" moveto "+nf.format(x)+ " "+nf.format(y)+" "+nf.format(rr)+" 0 360 arc stroke\n");
                   }
                   else{ continue drawloop;}
                     
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
            
            File outfile= new File("testout.ps");
            FileWriter fw=new FileWriter(outfile);
            BufferedWriter bw=new BufferedWriter(fw);
            PrintWriter pw=new PrintWriter(bw,true);
            pw.print(sb.toString());
            pw.flush();
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Picture p=new Picture(args);
    }
    
}
