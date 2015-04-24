/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

/**
 *
 * @author sep
 */
import biology.Plant;
import java.util.ArrayList;
import parameters.SimData;
public interface Stats {
    public void makeSummary(ArrayList<Plant> pop, SimData d);
    public double [] DoStats(double [] data);
    public String doOldReport();
    public String fullReport();
    
}
