/*
 * ProgressFrame.java
 *
 * Createdby sep  on May 9, 2006, 10:49 PM
 *
 */

package ProgressFrame;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author sep
 * 20feb2013 Add (in model.java) a 3rd bar that displays totalmemory and free 
 * memory as reported by Runtime. 
 */
public class ProgressFrame extends JFrame{
    JProgressBar [] progbars;
  
    /** Creates a new instance of ProgressFrame */
    public ProgressFrame(int n_bars, String []labels) {
        super(labels[0]);
        progbars=new JProgressBar[n_bars];
        this.getContentPane().setLayout(new GridLayout(n_bars,2));
        for(int i=0;i<n_bars;i++)
        {
            progbars[i]=new JProgressBar(0,100);
            progbars[i].setStringPainted(true);
            progbars[i].setPreferredSize(new Dimension(200,40));
            this.getContentPane().add(new JLabel(labels[i+1]));
            this.getContentPane().add(progbars[i]);
        }
        this.setSize(300,90);
        this.setVisible(true);
    }
    public int makePercent(double x, double y )
    {
        return (int)Math.floor(100*x/y);
    }
    public void setValue(int bar,int v)
    {
        progbars[bar].setValue(v);
    }

    public void done()
    {
        this.setVisible(false);
        this.dispose();
    }
   
}
