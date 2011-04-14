package pharoslabut;


import playerclient.NoNewDataException;
import playerclient.Position2DListener;
import playerclient.structures.position2d.PlayerPosition2dData;
import java.awt.*;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import pharoslabut.RobotMover;
import playerclient.structures.PlayerPoint2d;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import java.util.Timer;

import javax.imageio.ImageIO;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewJFrame.java
 *
 * Created on Mar 6, 2011, 10:19:07 PM
 */
/**
 *
 * @author Tad M
 */
public class NewJFrame extends javax.swing.JFrame implements Position2DListener  {
    /** Creates new form NewJFrame */
    private static RobotMover XueHua, XueHuaPos;
    protected  double XueHuaXf = 0, XueHuaYf = 0 ;
    protected long Half =2, Full = 4;
    public Timer timer = new Timer(); 
    protected String Time;
    BufferedImage image ;
    public String mapfile;
    public int src_x, src_y, dest_x, dest_y;
    private final static String newline = "\n";
    static StopWatch s = new StopWatch();
    public NewJFrame() {
        initComponents();
      XueHua =  new RobotMover("10.11.12.32", 6665, "log.txt",  false);
      XueHuaPos = new RobotMover("10.11.12.32", 6666, "log.txt", false);
  
         
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();



        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextField1.setText("Xpos");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setText("Ypos");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField3.setText("Orientation");

        jTextField4.setText("Distance Traveled");

        jTextField5.setText("Time");

        jButton1.setText("NW");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("N");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("NE");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 332, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        jLabel1.setText("Relative Xpos");

        jLabel2.setText("Relative Ypos");

        jLabel3.setText("Relative Orientation");

        jLabel4.setText("Mode");

        jLabel5.setText("Time Elapsed");

        jTextField6.setText("Blocked/Open");

        jLabel6.setText("Status");

        jButton10.setText("Pathfind");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
             try {
				jButton10ActionPerformed(evt);
			} catch (NoNewDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
        });

        jButton4.setText("W");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Manual Stop");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("E");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("SW");

        jButton8.setText("S");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("SE");
        jButton11.setText("jButton11");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }

			private void jButton11ActionPerformed(ActionEvent evt) {
				// TODO Auto-generated method stub
				
			}
        }
        
       
 
        
        
        );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 197, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(25, 25, 25))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(75, 75, 75)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)
                            .addComponent(jButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jButton6))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton7)
                            .addComponent(jButton8)
                            .addComponent(jButton9)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)))
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(31, 31, 31)
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(92, 92, 92))
        )
        ;

        pack();
    }
    
   
    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    		

    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        //turn 45 degrees CCW move
    	XueHua.turnLeft();
    	try {
			XueHua.wait(Half);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	XueHua.moveForward();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    			XueHua.turnLeft();
    			try {
					XueHua.wait(Full);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				XueHua.stop();
    			XueHua.moveForward();
    			jTextField4.setText("30");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
          
                     XueHua.turnRight();
                     try {
     					XueHua.wait(Full);
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
     				try {
    					XueHua.wait(Full);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                     XueHua.moveForward();
                     System.out.println("Button 8 pushed");// Add code here        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    				 jTextField4.setText("Manual");
                     XueHua.stop();// Add code here 
                     // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    			{	 
                     XueHua.moveForward(); 
                      
                     XueHuaPos.INS_UpdateX();
                     XueHuaPos.INS_UpdateY();
                     
                     String XueHuaX = Double.toString(XueHuaPos.Xpos);
                     String XueHuaY = Double.toString(XueHuaPos.Ypos);
                     
                     jTextField1.setText(XueHuaX); 
                     jTextField2.setText(XueHuaY);
                     
                   double XueHuaTime = s.getElapsedTimeSecs();
                     Time = Double.toString(XueHuaTime);
                     jTextField5.setText(Time);

    			}// TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         
                     XueHua.turnRight();
                     try {
     					XueHua.wait(Full);
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
     				XueHua.stop();
                     XueHua.moveForward();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

   private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) throws NoNewDataException, IOException {//GEN-FIRST:event_jButton10ActionPerformed
        JFrame Oldinputs = new JFrame();
        String inputs = JOptionPane.showInputDialog(Oldinputs, "Previous File name is "+mapfile);
	    JFrame File = new JFrame();
	    String mapfile = JOptionPane.showInputDialog(File, "Input Map File name");

	    System.out.println(mapfile);
	    
	 /*   ImageIcon Display = new ImageIcon(mapfile);
	    jButton11.setIcon(Display);		
					// display map 8 */
	    JFrame frame = new JFrame();
	    String result = JOptionPane.showInputDialog(frame, "Input Initial Coordinates X  Y separate with space");
		Scanner Scan = new Scanner(result);
		
		src_x = Scan.nextInt();
		XueHua.BaseX = src_x;
		src_y = Scan.nextInt();
		XueHua.BaseY = src_y;
	    //TODO: call RobotMover.ManUpdateINS(x,y,yaw)
	    System.out.println(result);
	    
	    JFrame Dest = new JFrame();
	    String Desresult = JOptionPane.showInputDialog(Dest, "Destination Coordinates in X Y separate with space");
	    Scan = new Scanner(Desresult);
	    dest_x =Scan.nextInt();
	    dest_y =Scan.nextInt();
	    
	    System.out.println(Desresult);
	    
	    File ascii_map = new File(mapfile);	
		Mapping map  = new Mapping();
		map.parse(ascii_map);
		map.createBuffer();
		map.printBuffer(this);
		//map.printMap();
		
		PathFind pf  = new PathFind(map,1);
		pf.A_path(src_x, src_y, dest_x, dest_y, 0);
		pf.result.printPath();
		pf.result.printMov();
		map.printBuffer(this);
	    //TODO: call PathFind.A_path
	    
	    
	    //TODO: Handle movement list
	    
	    
	    
	    // while new command 
	    //move
	    // update
	    // check for new command
             
	    	//  XueHuaPos.INS_UpdateX();
            //  XueHuaPos.INS_UpdateY();
              
	    XueHuaPos.INS_UpdateX();
        XueHuaPos.INS_UpdateY();
        
        String XueHuaX = Double.toString(XueHuaPos.Xpos);
        String XueHuaY = Double.toString(XueHuaPos.Ypos);
        
        jTextField1.setText(XueHuaX); 
        jTextField2.setText(XueHuaY);
        
      double XueHuaTime = s.getElapsedTimeSecs();
        Time = Double.toString(XueHuaTime);
        jTextField5.setText(Time);

     //         private void jTextField7ActionPerformed(java.awt.event.ActionEvent ) {
                  // TODO add your handling code here:
         //     }     

// TODO add your handling code here: this is a temp button for manual updates from INS for nowT
    }//GEN-LAST:event_jButton10ActionPerformed
   private ImageDisplay ImageDisplay(String mapfile) {
	// TODO Auto-generated method stub
	return null;
}

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {                                         
       // TODO add your handling code here:
       // Turn NW
       XueHua.moveForward();
   }                                        

   private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {                                         
       // TODO add your handling code here:
	   XueHua.turnLeft();
	   XueHua.moveForward();
	   //turn 225 degrees CCW and move straight
   }                                        

   private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {                                         
      XueHua.turnRight();
      XueHua.moveForward();// TODO add your handling code here:
   }               // turn 315 degrees CCW and move 
    /**
    * @param args the command line arguments
    */
   
   public void UpdateMap(String s)
   {
	   
	   jTextArea1.append(s);
	   
   }
   
   // Note that all degree movements are relative to the current heading of the robot, not absolute heading.
    public static void main(String args[]) {
    	
    	s.start();
      
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            
            public void run() {
                new NewJFrame().setVisible(true);
               
                
                //code you want to time goes here
              
                
            }
  // Add Automatic Control Components here
        });
        
    }

    private static void addPos2DListener(RobotMover xueHuaPos2) {
		// TODO Auto-generated method stub
		
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JLabel Map;
    private javax.swing.JButton jButton11;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    

    // End of variables declaration//GEN-END:variables
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		// TODO Auto-generated method stub
		
	}

}
