/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Stack;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author pelikan
 */
public class Runner {
       //private int NUM_OF_TASKS = 50;
    private static int seq=1;
       private static int numberRunning=0;
        int nrOfProcessors=0;
       int ourTaskLimit=8;
       private String [] ourArgs;
       Stack<String> commands;
       ExecutorService es;
   Object result;
   int cnt = 0;
   long startTime, stopTime;

   public Runner(String [] args) {
       ourArgs=args;
      startTime= new java.util.Date().getTime();
      nrOfProcessors = Runtime.getRuntime().availableProcessors();
       es = Executors.newFixedThreadPool(nrOfProcessors);
       
   }

   public void callBack(Object result) {
       //a process is done. Start another one if
       //there are still things to do
       numberRunning--;
       if(commands.size()>0)
       {
            Task task = new Task(seq++);
         task.setCommandLine(commands.pop());
         task.setCaller(this);
         es.submit(task);
         numberRunning++;
       }
      System.out.println("result "+result);
      this.result = result;
      if(numberRunning==0) {
        
         System.exit(0);
      }
   }
/**
 * 
 */
   public void run() {
       //we need 1 or 2 command line parameters
       //the file with the commands to execute and
       //optionally, the number of simultaneous processes to run
      readCommands(ourArgs[0]);
      int numberToStart=commands.size();
      if(ourTaskLimit < numberToStart) numberToStart=ourTaskLimit;
       if(nrOfProcessors < numberToStart) numberToStart=nrOfProcessors;
        
      for(int i = 0;  i < numberToStart; i++) { //i<min(number teaks, commands.size()
          numberRunning++;
         Task task = new Task(seq++);
         task.setCommandLine(commands.pop());
         task.setCaller(this);
         es.submit(task);
         // at this point after submitting the tasks the
         // main thread is free to perform other work.
      }
   }

    private void readCommands(String filename)
    {
         commands=new Stack<String>();
       //our argument is the name of a text file with command lines in it
      //int nrOfProcessors = Runtime.getRuntime().availableProcessors();
   //   es = Executors.newFixedThreadPool(nrOfProcessors);
      //read the provided file and extract the commandlines in it
      //loop to execute up to numberProcesses at a time
           try {
               BufferedReader reader = new BufferedReader(new FileReader(filename));
               String line;
               while((line=reader.readLine())!=null)
               {
                   commands.push(line);
               }
           } catch (Exception ex) {
               Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, null, ex);
           }
     
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if(args.length<1)
        {
            System.out.println("java -cp NG.jar Runner.Runner <filename>");
            System.out.println("where <filename> lists commands to run, one per line");
            System.exit(0);
        }
          new Runner(args).run();
    }
}
