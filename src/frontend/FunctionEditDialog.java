package frontend;

/*
 * FunctionEditDialog.java
 *
 * Created on April 24, 2005, 11:24 AM
 */
import functions.SampledFunction;
import javax.swing.*;
/**
 *
 * @author  Steve
 */
public class FunctionEditDialog extends javax.swing.JDialog {
    FunctionDisplay ourFD=null;
    editableSampledFunction backupfunction=null;
    /** Creates new form FunctionEditDialog */
    public FunctionEditDialog(java.awt.Frame parent, boolean modal,FunctionDisplay fd) {
        super(parent, modal);
        ourFD=fd;
        backupfunction=fd.sf.Copy();
        initComponents();
        ScrollPane.setViewportView(ourFD);
        this.validate();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        ScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        DoneMenuItem = new javax.swing.JMenuItem();
        QuitMenuItem = new javax.swing.JMenuItem();
        PrintMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Function editor");
        jPanel1.setPreferredSize(new java.awt.Dimension(600, 400));
        ScrollPane.setViewportView(jPanel1);

        getContentPane().add(ScrollPane, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Action");
        DoneMenuItem.setText("Done");
        DoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DoneMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(DoneMenuItem);

        QuitMenuItem.setText("Quit");
        QuitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QuitMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(QuitMenuItem);

        PrintMenuItem.setText("Print XML");
        PrintMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrintMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(PrintMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }//GEN-END:initComponents

    private void PrintMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrintMenuItemActionPerformed
        // TODO add your handling code here:
        System.out.println(ourFD.sf.toXML());
    }//GEN-LAST:event_PrintMenuItemActionPerformed

    private void QuitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QuitMenuItemActionPerformed
        // TODO add your handling code here:
        ourFD.sf=backupfunction;
        System.out.println("After quit sf is"+ourFD.sf.toXML());
        this.setVisible(false);
        dispose();
    }//GEN-LAST:event_QuitMenuItemActionPerformed

    private void DoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DoneMenuItemActionPerformed
        // TODO add your handling code here:
         this.setVisible(false);
        dispose();
    }//GEN-LAST:event_DoneMenuItemActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FunctionEditDialog(new javax.swing.JFrame(), true,null).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem DoneMenuItem;
    private javax.swing.JMenuItem PrintMenuItem;
    private javax.swing.JMenuItem QuitMenuItem;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}