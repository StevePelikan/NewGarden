/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

/**
 * A Task executes a command line (runs a shell script or .bat file say)
 * A Runner is intended to call enough Task's to populate all the processors
 * and threads allowed, requested, or available.
 * @author pelikan
 * 
 * "java -Xmx1G -Xms10M -jar NG.jar Model -x MASTERSimData.xml  -rresults.csv -s   -p -v --smallstats -doutput.txt"
 */
public class Task implements Callable{
 
    private Runner runner;
    private int seq;
    private String commandline;

    public Task() {}
    public Task(int i) { seq = i; }
    public Task(int i,String commandline) { this.seq=i;this.commandline=commandline; }
    public Object call() {
      // String str = "";
     //  long begTest = new java.util.Date().getTime();
     //  System.out.println("start - Task "+seq);
        System.out.println(""+seq+":"+commandline);
        String line;
       try {
          
         final Process p= Runtime.getRuntime().exec(commandline);
      BufferedReader  is = new BufferedReader(new InputStreamReader(p.getInputStream()));

    while ((line = is.readLine()) != null)
    {
      System.out.println(""+seq+":"+line);
    }
         p.waitFor();
         
       } catch (Exception e) {}

       runner.callBack(seq);

      // Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);
      // System.out.println("task -"+seq+" took " + secs + " secs");
       return seq;
    }
    public void setCommandLine(String s){this.commandline=s;}
    public void setCaller(Runner runner) {
       this.runner = runner;
    }

    public Runner getCaller() {
       return runner;
    }

}
