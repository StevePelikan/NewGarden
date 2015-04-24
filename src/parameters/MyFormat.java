/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parameters;

import java.util.Formatter;
import java.util.Locale;

/**
 *
 * @author pelikan
 */
/*
 * MyFormat is a silly class so that we can use the same default formats
 * for output in different places. It is easy to use it to replace NumberFormat
 * int the methods like toXML() and toString() for all the parameters in SimData
 * 19feb2013 modified to include defaults overloading the format method
 * and allowing specification of defaults at creation.
 */
public class MyFormat {
     String[] defaults={"%6.6g","%d"};
    public MyFormat()
    {
        
    }
    public MyFormat(String[] def)
    {
        defaults=def;
    }
    public String format(String proto,double x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(proto,x).toString();

    }
     public String format(double x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(defaults[0],x).toString();

    }
    public String format(String proto,long x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(proto,x).toString();

    }
    
    public String format(long x)
    {
        Formatter formatter;
        StringBuilder sb=new StringBuilder();
        formatter=new Formatter(sb,Locale.US);
        return formatter.format(defaults[1],x).toString();

    }
}
