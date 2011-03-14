/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWindow.java
 *
 * Created on Mar 10, 2011, 3:27:46 PM
 */

package aim.GUI;

import java.io.IOException;

/**
 *
 * @author ut
 */
public class MainWindow extends javax.swing.JFrame {

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        File = new javax.swing.JMenu();
        Exit = new javax.swing.JMenuItem();
        View = new javax.swing.JMenu();
        Help = new javax.swing.JMenu();
        JavaDocs = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        File.setText("File");

        Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        Exit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aim/GUI/Images/exit.png"))); // NOI18N
        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        File.add(Exit);

        jMenuBar1.add(File);

        View.setText("View");
        jMenuBar1.add(View);

        Help.setText("Help");

        JavaDocs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        JavaDocs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aim/GUI/Images/java.png"))); // NOI18N
        JavaDocs.setText("JavaDocs");
        JavaDocs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JavaDocsActionPerformed(evt);
            }
        });
        Help.add(JavaDocs);

        jMenuBar1.add(Help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 459, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 215, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_ExitActionPerformed

    private void JavaDocsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JavaDocsActionPerformed
        String htmlFilePath = "home/ut/AIM/Skeleton_Code/AIM/dist/javadoc/index.html"; // path to your new file
        File htmlFile = new File(htmlFilePath);

        // open the default web browser for the HTML page
        Desktop.getDesktop().browse(htmlFile.toURI());

    }//GEN-LAST:event_JavaDocsActionPerformed

    /**
    * @param args the command line arguments
    */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenu File;
    private javax.swing.JMenu Help;
    private javax.swing.JMenuItem JavaDocs;
    private javax.swing.JMenu View;
    private javax.swing.JMenuBar jMenuBar1;
    // End of variables declaration//GEN-END:variables

}