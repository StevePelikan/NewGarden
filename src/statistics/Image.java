/*
 * Copyright (C) 2014 s pelikan
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

/**
 *
 * @author sep
 */
import biology.Location;
import biology.Plant;
import functions.RandomValue;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import parameters.SimData;
import parameters.SimDataReader;

/**
 *
 * @author sep
 */

/*
 Here we assume each viable site (a,b) is represented by a square of side 1
 centered at the site so (a-0.5,b-0.5) to (a+0.5,b+0.5) are SW and NE corners.


 */
interface AgeColorConverter {

    public Color getColor(int age);
}
/*
age is
*/
public class Image {

    SimData ourSimData;
    int pixelWidth, pixelHeight;
    int pixelsPerSite;
    BufferedImage image;
    Graphics2D g2d;

    public Image(SimData sd) {
        ourSimData = sd;

    }

    public Image() {

    }

    public BufferedImage getBufferedImage() {
        return image;
    }

    public void setPixelsPerSite(int pps) {
        pixelsPerSite = pps;
        pixelWidth = pps * (ourSimData.region.XH - ourSimData.region.XL + 1);
        pixelHeight = pps * (ourSimData.region.YH - ourSimData.region.YL + 1);
        image = new BufferedImage(this.pixelWidth, this.pixelHeight, BufferedImage.TYPE_INT_RGB);
        g2d = image.createGraphics();

    }

    /**
     *
     * @param a
     * @param b
     * @param color Draw a box corresponding to the site /Location (a,b)
     * 
     * The graphic is pixelHeight=pps*(YH-YL+1) pixels high extending from 0 to
     * pixelHeight-1.
     * 
     */
    public void makeBox(int a, int b, Color color) {
        int Xin = pixelsPerSite * (a - ourSimData.region.XL);
        //int Yup = this.pixelHeight-pixelsPerSite*((b-ourSimData.region.YL));
        //int Yup = pixelsPerSite * ((b - ourSimData.region.YL));
        int Yup=pixelsPerSite*(ourSimData.region.YH-b);
        g2d.setColor(color);
        g2d.fillRect(Xin, Yup, pixelsPerSite, pixelsPerSite);
    }

    /**
     *
     * @param population
     * @param color Green or Blue depending on whether population is the actual
     * population or the seedbank
     */
    public void plotPlants(ArrayList<Plant> population, Color color) {
        for (Plant p : population) {
            int x = p.location.X;
            makeBox(p.location.X, p.location.Y, color);
        }
    }

    public void plotPlants(ArrayList<Plant> population, AgeColorConverter acc) {
        for (Plant p : population) {
            int age=ourSimData.current_year-p.dob;
            if(age>ourSimData.number_runs) age=ourSimData.number_runs;
            if(age<0) age=0;
            age= (int)Math.floor(255*age/ourSimData.number_runs);
            Color color = acc.getColor(age);
            int x = p.location.X;
            makeBox(p.location.X, p.location.Y, color);
        }
    }

    public void drawMap() {
        Color white = Color.WHITE;
        Color gray = Color.DARK_GRAY;
        Color col = white;
        for (int x = ourSimData.region.XL; x <= ourSimData.region.XH; x++) {
            for (int y = ourSimData.region.YL; y <= ourSimData.region.YH; y++) {
                if (ourSimData.region.inRegion(x, y)) {
                    col = white;
                } else {
                    col = gray;
                }
                makeBox(x, y, col);

            }
        }
    }

    /**
     *
     * @param xmlfilename
     *
     * THis is for debugging an development. In practice Image is created by
     * handing it a reference to a SimData.
     *
     * In future this can be replaced by the static SimData.readXMLFile()
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
        ourSimData = my_mb.getSD();
    }

    public void writeImage(String filename, String filetype) {
        try {
    // retrieve image

            File outputfile = new File(filename + "." + filetype);
            ImageIO.write(image, filetype, outputfile);

        } catch (IOException e) {
            System.out.println(e.toString());
        }

    }

    public void doAllImages(ArrayList<Plant> pop, ArrayList<Plant> seed) {
        StringBuilder sb = new StringBuilder();
        String Region = sb.append("Region-" + ourSimData.current_year).toString();
        sb = new StringBuilder();
        String Population = sb.append("Population-" + ourSimData.current_year).toString();
        sb = new StringBuilder();
        String Seedbank = sb.append("Seedbank-" + ourSimData.current_year).toString();
        //basic map
        this.setPixelsPerSite(ourSimData.pixelsPerSite);
        this.drawMap();
        this.writeImage(Region, "png");

        this.setPixelsPerSite(4);
        this.drawMap();
        //
        this.plotPlants(pop, new AgeColorConverter() {
            public Color getColor(int age) {
                return (Color.BLUE);
            }
        });
        // this.plotPlants(pop, Color.GREEN);
        this.writeImage(Population, "png");

        this.setPixelsPerSite(4);
        this.drawMap();
        this.plotPlants(seed, Color.RED);
        this.writeImage(Seedbank, "png");
    }

    public static void main(String[] args) {
        Image im = new Image();
        im.getSimData("MASTERSimData.xml");
        im.ourSimData.region.initialize();
        im.ourSimData.region.update(0, new RandomValue());
        im.setPixelsPerSite(4);
        im.drawMap();
        ArrayList<Plant> pop = new ArrayList<Plant>();
        pop.add(new Plant(2, -1, new Location(1, 1)));
        pop.add(new Plant(2, -5, new Location(1, 50)));
        pop.add(new Plant(2, 2, new Location(0, 100)));
        pop.add(new Plant(2, 0, new Location(0, 0)));
        im.plotPlants(pop, new AgeColorConverter() {
            public Color getColor(int age) {
                return new Color(0,age,0);
                //if(age >0) return (Color.green);
               // else return(Color.blue);
            }
        });
        im.writeImage("test", "png");
    }
}
