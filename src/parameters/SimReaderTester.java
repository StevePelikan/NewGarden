/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parameters;

import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author pelikan
 */
public class SimReaderTester {
    static String xmlfilename="MASTERSimData.xml";
    public SimReaderTester(String [] args)
    {
        //xmlfilename=args[0];
          // New 27 Jan 2012 make sure the xml file is valid
        Validator v = new Validator(xmlfilename);
        String ans = v.validate();
        if (ans.length() > 0) {
            System.err.println(ans);
            System.exit(0);

        }
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
        SimData sd = my_mb.getSD();
        
        System.out.println(sd.toString());
        
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
    public String writeRLEString(int[] x)
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
    public static void main(String[] args)
    {
     SimReaderTester srt=new SimReaderTester(args);   
     
     SimData sd=SimData.readXMLFile(xmlfilename);
     System.out.println(sd.toString());
     
   //  int [] ans=srt.parseRLEList("1,2,5-9,12-15");
    // System.out.println(Arrays.toString(ans));
     //System.out.println(srt.writeRLEString(ans));
    }
    
}
