package pharoslabut;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;


/*************************************************************************
**PAV GUI																**
**Author: Devin Murphy													**
**Date: 4/12/2011														**
**Version: 1.1															**
**Last Modified: 2/27/2011												**
**About:																**
**																		**
**																		**
*************************************************************************/
class GUI extends JPanel implements ActionListener, MouseListener, MouseMotionListener, ComponentListener
{
	private GUICanvas loadedMapCanvas;
	private JPanel top, bottom;
	private JTextField theXCoor;
	private JTextField theYCoor;
	private JTextField mouseAtX;
	private JTextField mouseAtY;
	private JTextField currentLocX;
	private JTextField currentLocY;
	private JTextField theMessage;
	private JLabel dCoords;
	private JLabel dCoordX;
	private JLabel dCoordY;
	private JLabel cCoords;
	private JLabel cCoordX;
	private JLabel cCoordY;
	private JButton calculatePathButton;
	private JButton startButton;
	private JButton abortButton;
	private JRadioButton selectDestination;
	private JRadioButton selectStartLocation;
	private BufferedImage defaultMap = null;
	private BufferedImage map = null;
	private PAV pav;
	private JFileChooser mapChooser;
	private Node[][] mapNodeArray;
	private LinkedList path;
	private boolean pleaseInitLoc;
	public boolean startMovement;
	public boolean abortMovement;
	private List <Double> commands;
	private Dimension sizeOfNewMap;
	private Thread movementThread;
	GUI thisGUI;
	testing small_test;
	threadAbortTest taTest;
	Runnable mThread;
	//private FocusTraversalPolicy ftPolicy;
	
/* GUI Constructor
 * Every instance of GUI first makes the objects to be placed inside the frame
 * then arranges them as specified.
 * "theMessage" will show messages to the user based on input.
 * "calculatePathButton" causes the search algorithm to execute and find the shortest path
 * to a destination.
 * "startButton" tells the PAV to begin movement.
 * "abortButton" tells the PAV to stop movement.
 * "loadedMapCanvas" will display the map of the area as well as current PAV location and
 * destination.
 */
	
	public GUI()
	{
		makeTheObjects();
		doTheLayout();
		theXCoor.setActionCommand("X Coordinate");
		theXCoor.setText("0");
		theXCoor.addActionListener(this);
		theYCoor.setActionCommand("Y Coordinate");
		theYCoor.setText("0");
		theYCoor.addActionListener(this);
		calculatePathButton.addActionListener(this);
		startButton.addActionListener(this);
		abortButton.addActionListener(this);
		loadedMapCanvas.addMouseListener(this);
		loadedMapCanvas.addMouseMotionListener(this);
		this.addComponentListener(this);
		if(map != null)
		{
			loadedMapCanvas.setDrawPath(false);
			loadedMapCanvas.setMap(map);
			mapNodeArray = interpretMap(map);
		}
		path = new LinkedList();
		thisGUI = this;
		mThread = new Runnable(){
			public void run(){
				//try{
					//if(startMovement)
				//	{
						small_test = new testing("10.11.12.31", 6665, "log.txt",false, commands, thisGUI);
						//taTest = new threadAbortTest(thisGUI);
				//	}
				/*} catch(InterruptedException exc){
					System.out.println("Call to movment thread interrupted");
					System.exit(1);
				}*/
			}
		};
		
		//movementThread = new Thread(mThread);
		//movementThread.start();
	}
	
	//Make all the objects
	private void makeTheObjects()
	{	
		//Try to open the default map file and store it as a BufferedImage
		try{
			defaultMap = ImageIO.read(new File("src/pharoslabut/7 by 7.bmp"));
			map = defaultMap;
		}
		catch (IOException e){
			
		}
		pav = new PAV("PAV 1");
		
		loadedMapCanvas = new GUICanvas();
		loadedMapCanvas.initGUICanvas();
		loadedMapCanvas.setBackground(Color.black);
		loadedMapCanvas.setPreferredSize(new Dimension(600, 600));
		
		theMessage = new JTextField(50);
		theMessage.setEditable(false);
		
		mouseAtX = new JTextField(3);
		mouseAtX.setEditable(false);
		
		mouseAtY = new JTextField(3);
		mouseAtY.setEditable(false);
		
		selectStartLocation = new JRadioButton("Select Starting Location (Blue Marker)");
		selectDestination = new JRadioButton("Select Destination (Green Marker)", true);
		ButtonGroup markerSelectMode = new ButtonGroup();
		markerSelectMode.add(selectStartLocation);
		markerSelectMode.add(selectDestination);
		selectStartLocation.setEnabled(true);
		selectDestination.setEnabled(true);
		
		theXCoor = new JTextField(3);
		theYCoor = new JTextField(3);
		
		currentLocX = new JTextField(3);
		currentLocX.setEditable(false);
		
		currentLocY = new JTextField(3);
		currentLocY.setEditable(false);
		
		calculatePathButton = new JButton("Calculate Path");
		startButton = new JButton("Start");
		abortButton = new JButton("Abort");
		
		dCoords = new JLabel("Destination Coordinates ");
		dCoordX = new JLabel("X: ");
		dCoordY = new JLabel("Y: ");
		
		cCoords = new JLabel("Current PAV Location ");
		cCoordX = new JLabel("X: ");
		cCoordY = new JLabel("Y: ");
		
		mapChooser = new JFileChooser();
	}
	
	//Lay out all the objects
	private void doTheLayout()
	{
		JPanel titles = new JPanel();
		top = new JPanel();
		JPanel center = new JPanel();
		bottom = new JPanel();
		JPanel messageBar = new JPanel();
		JPanel mouseStuff = new JPanel();
		JPanel destinationStuff = new JPanel();
		JPanel currentLocStuff = new JPanel();
		
		//This grid bag is for the overall layout
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		//This grid bag is for PAV current location
		GridBagLayout gridBagP = new GridBagLayout();
		GridBagConstraints p = new GridBagConstraints();
		
		//This grid bag is for the destination information
		GridBagLayout gridBagD = new GridBagLayout();
		GridBagConstraints d = new GridBagConstraints();
		
		//Lay out the titles
		titles.setLayout(new GridLayout(1, 1, 1, 1));
		titles.add(new JLabel("Area Map", SwingConstants.CENTER));
		
		//Lay out the top where the maps are
		top.setLayout(new FlowLayout());
		top.add(loadedMapCanvas, SwingConstants.CENTER);
		
		//Lay out the center by first laying out the mouse stuff
		mouseStuff.setLayout(new FlowLayout());
		mouseStuff.add(new JLabel("Mouse At X:"));
		mouseStuff.add(mouseAtX);
		mouseStuff.add(new JLabel("Y:"));
		mouseStuff.add(mouseAtY);
		center.setLayout(new FlowLayout(FlowLayout.LEFT));
		center.add(mouseStuff);
		center.add(selectStartLocation);
		center.add(selectDestination);
		
		//Lay out the current PAV location information
		currentLocStuff.setLayout(gridBagP);
		p.gridwidth = GridBagConstraints.REMAINDER;
		p.fill = GridBagConstraints.NONE;
		p.weightx = 1.0;
		gridBagP.setConstraints(cCoords, p);
		currentLocStuff.add(cCoords);
		
		p.gridwidth = GridBagConstraints.WEST;
		gridBagP.setConstraints(cCoordX, p);
		currentLocStuff.add(cCoordX);
		
		p.ipadx = 3;
		gridBagP.setConstraints(currentLocX, p);
		currentLocStuff.add(currentLocX);
		
		p.ipadx = 0;
		p.gridwidth = GridBagConstraints.RELATIVE;
		gridBagP.setConstraints(cCoordY, p);
		currentLocStuff.add(cCoordY);
		
		p.gridwidth = GridBagConstraints.REMAINDER;
		gridBagP.setConstraints(currentLocY, p);
		currentLocStuff.add(currentLocY);
		
		//Lay out the destination information
		destinationStuff.setLayout(gridBagD);
		d.gridwidth = GridBagConstraints.REMAINDER;
		d.fill = GridBagConstraints.NONE;
		d.weightx = 1.0;
		gridBagD.setConstraints(dCoords, d);
		destinationStuff.add(dCoords);
		
		d.gridwidth = GridBagConstraints.WEST;
		gridBagD.setConstraints(dCoordX, d);
		destinationStuff.add(dCoordX);
		
		d.ipadx = 3;
		gridBagD.setConstraints(theXCoor, d);
		destinationStuff.add(theXCoor);
		
		d.ipadx = 0;
		d.gridwidth = GridBagConstraints.RELATIVE;
		gridBagD.setConstraints(dCoordY, d);
		destinationStuff.add(dCoordY);
		
		d.gridwidth = GridBagConstraints.REMAINDER;
		gridBagD.setConstraints(theYCoor, d);
		destinationStuff.add(theYCoor);
		
		//Lay out the bottom
		bottom.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 5));
		bottom.add(currentLocStuff);
		bottom.add(destinationStuff);
		bottom.add(calculatePathButton);
		bottom.add(startButton);
		bottom.add(abortButton);
		
		//Lay out the message bar
		messageBar.setLayout(new FlowLayout());
		messageBar.add(theMessage);
		theMessage.setText("Welcome to the PAV User Panel.");
		
		//now lay out GUI
		setLayout(gridBag);
		//Set each panel as the last element in its own row
		c.gridwidth = GridBagConstraints.REMAINDER;
		//Allow the panels to be resized horizontally and vertically
		c.fill = GridBagConstraints.BOTH;
		//add each panel
		gridBag.setConstraints(titles, c);
		add(titles);
		gridBag.setConstraints(top, c);
		add(top);
		gridBag.setConstraints(center, c);
		add(center);
		gridBag.setConstraints(bottom, c);
		add(bottom);
		gridBag.setConstraints(messageBar, c);
		add(messageBar);
	}
	
	//Handle button pushes and text entered
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getActionCommand().equals("Calculate Path"))
		{
			int startX = 0;
			int startY = 0;
			int endX = Integer.parseInt(theXCoor.getText());
			int endY = Integer.parseInt(theYCoor.getText());
			boolean startCheck = false;
			boolean endCheck = false;
			theMessage.setText("Calculating path............");
			if(pav.getInitStatus()==false)
			{
				JOptionPane.showMessageDialog(null,"Please click the map to initialize the PAV location");
				selectStartLocation.setSelected(true);
				pleaseInitLoc = true;
				return;
			}
			loadedMapCanvas.setDrawPath(false);
			startX = pav.getCurrentX();
			startY = pav.getCurrentY();
			if(mapNodeArray[startX][startY].getPath()==1)
			{
				startCheck = true;
			}
			if(mapNodeArray[endX][endY].getPath()==1)
			{
				endCheck = true;
			}
			if(startCheck&&endCheck)
			{
				path = AStar.findPath(mapNodeArray,mapNodeArray[startX][startY],mapNodeArray[endX][endY]);
				loadedMapCanvas.setPath(path);
				if(LinkedList.listSize(path) == 1)
				{
					JOptionPane.showMessageDialog(null,"No path exists to the selected destination.");
					theMessage.setText("No path exists to the selected destination.");
				}
				else
				{
					theMessage.setText("Path Calculated.");
					loadedMapCanvas.setDrawPath(true, true);
				}
				
			}
			else
			{
				if((startCheck==false)&&(endCheck==false))
				{
					theMessage.setText("Unable to calculate path. The initial location and destination are invalid.");
				}
				else if(startCheck==false)
				{
					theMessage.setText("Unable to calculate path. The initial location is invalid.");
				}
				else if(endCheck==false)
				{
					theMessage.setText("Unable to calculate path. The destination is invalid.");
				}
			}
			this.transferFocusUpCycle();
		}
		else if(evt.getActionCommand().equals("Start"))
		{
			try
			{
				theMessage.setText("Navigation to destination at coordinate "+theXCoor.getText()+", "+theYCoor.getText()+" starting.");
			}
			catch(NumberFormatException e)
			{
				theMessage.setText("Incomplete input.");
			}
			if(loadedMapCanvas.getDrawPath())
			{
				commands = AStar.move_instruction(path, path.first().retrieve());
				movementThread = new Thread(mThread);
				this.abortMovement = false;
				movementThread.start();
			}
			//testing small_test = new testing("10.11.12.31", 6665, "log.txt",false, commands);
			this.transferFocusUpCycle();
		}
		else if(evt.getActionCommand().equals("Abort"))
		{
			try
			{
				theMessage.setText("Navigation halted.");
			}
			catch(NumberFormatException e)
			{
				theMessage.setText("Error.");
			}
			this.abortMovement = true;
			//small_test.abort = true;
			this.transferFocusUpCycle();
		}
		else if(evt.getActionCommand().equals("X Coordinate"))
		{
			int tempx;
			if((tempx = Integer.parseInt(theXCoor.getText()))<200)
			{
				loadedMapCanvas.setDestination(tempx, 'x', false, true);
				theMessage.setText("X coordinate of destination set to "+tempx);
			}
			else
			{
				theMessage.setText("Invalid Coordinate");
			}
			this.transferFocusUpCycle();
		}
		else if(evt.getActionCommand().equals("Y Coordinate"))
		{
			int tempy;
			if((tempy = Integer.parseInt(theYCoor.getText()))<200)
			{
				loadedMapCanvas.setDestination(tempy, 'y', false, true);
				theMessage.setText("Y coordinate of destination set to "+tempy);
			}
			else
			{
				theMessage.setText("Invalid Coordinate");
			}
			this.transferFocusUpCycle();
		}
	}
	public void mouseClicked(MouseEvent me)
	{
		if(selectDestination.isSelected())
		{
			theXCoor.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
			theYCoor.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
			int tempx = Integer.parseInt(theXCoor.getText());
			int tempy = Integer.parseInt(theYCoor.getText());
			loadedMapCanvas.setDestination(tempx, tempy, false, true);
			theMessage.setText("X, Y coordinates of destination set to "+tempx+", "+tempy);
		}
		else
		{
			currentLocX.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
			currentLocY.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
			int tempx = Integer.parseInt(currentLocX.getText());
			int tempy = Integer.parseInt(currentLocY.getText());
			pav.initializePAV(tempx, tempy);
			loadedMapCanvas.setCurrentLoc(tempx, tempy, false, true);
			theMessage.setText("X, Y coordinates of start location set to "+tempx+", "+tempy);
			if(pleaseInitLoc)
			{
				pleaseInitLoc = false;
				calculatePathButton.doClick();
			}
		}
		this.transferFocusUpCycle();
	}
	public void mouseEntered(MouseEvent me)
	{
		mouseAtX.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
		mouseAtY.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
	}
	public void mouseExited(MouseEvent me)
	{
		mouseAtX.setText("");
		mouseAtY.setText("");
	}
	public void mousePressed(MouseEvent me)
	{
		
	}
	public void mouseReleased(MouseEvent me)
	{
		if(selectDestination.isSelected())
		{
			theXCoor.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
			theYCoor.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
			int tempx = Integer.parseInt(theXCoor.getText());
			int tempy = Integer.parseInt(theYCoor.getText());
			loadedMapCanvas.setDestination(tempx, tempy, false, true);
			theMessage.setText("X, Y coordinates of destination set to "+tempx+", "+tempy);
		}
		else
		{
			currentLocX.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
			currentLocY.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
			int tempx = Integer.parseInt(currentLocX.getText());
			int tempy = Integer.parseInt(currentLocY.getText());
			pav.initializePAV(tempx, tempy);
			loadedMapCanvas.setCurrentLoc(tempx, tempy, false, true);
			theMessage.setText("X, Y coordinates of start location set to "+tempx+", "+tempy);
		}
		this.transferFocusUpCycle();
	}
	public void mouseMoved(MouseEvent me)
	{
		mouseAtX.setText(""+((int)Math.ceil((me.getX())/loadedMapCanvas.getFactor())));
		mouseAtY.setText(""+((int)Math.ceil((me.getY())/loadedMapCanvas.getFactor())));
	}
	public void mouseDragged(MouseEvent me)
	{
		
	}
	
	public void componentHidden(ComponentEvent ce)
	{
		
	}
	
	public void componentMoved(ComponentEvent ce)
	{
		
	}
	
	public void componentResized(ComponentEvent ce)
	{
		final int sizeX = ce.getComponent().getWidth();
		final int sizeY = ce.getComponent().getHeight();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				updateCanvasSize(sizeX, sizeY);
			}
		});
		//theMessage.setText("Window size is "+sizeX+" x "+sizeY);
	}
	
	public void componentShown(ComponentEvent ce)
	{
		
	}
	
	public boolean getAbortMovement()
	{
		return abortMovement;
	}
	
	public void setMessageDisplay(String m)
	{
		theMessage.setText(m);
	}
	
	public PAV getPAV()
	{
		return pav;
	}
	
	public void initializeLocation()
	{
		String initLocation = JOptionPane.showInputDialog("Enter initial location of the PAV in the form x, y");
		
		
		if(initLocation == null || initLocation.length()==0)
		{
			theMessage.setText("Location of PAV was not initialized.");
		}
		
		else
		{
			int index = initLocation.indexOf(',');
			int initx = Integer.parseInt(initLocation.substring(0, index));
			int inity = Integer.parseInt(initLocation.substring(index+2, initLocation.length()));
			pav.initializePAV(initx, inity);
			currentLocX.setText(""+pav.getCurrentX());
			currentLocY.setText(""+pav.getCurrentY());
			loadedMapCanvas.setCurrentLoc(pav.getCurrentX(), pav.getCurrentY(), false, true);
		}
				
	}
	
	public void selectMap()
	{
		int result = mapChooser.showDialog(null, "Select");
		if(result==JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = mapChooser.getSelectedFile();
			String fileName = selectedFile.getName();
			theMessage.setText("Selected map: "+fileName+".");
			try{
				map = ImageIO.read(selectedFile);
			}
			catch (IOException e){
				
			}
			int index = fileName.indexOf('.');
			String fileType = fileName.substring(index+1);
			if((map!=null)&&(fileType.equals("bmp")))
			{
				loadedMapCanvas.setMap(map);
				mapNodeArray = interpretMap(map);
				loadedMapCanvas.setDrawPath(false);
				pav.resetInitStatus();
				canvasMaxRestore(this.getWidth(), this.getHeight());
			}
			else
			{
				theMessage.setText("The selected file is not the right type. Please select a '.bmp' file.");
			}
		}
		else
		{
			theMessage.setText("No map was selected.");
		}
	}
	
	public void setDefaultMap()
	{
		loadedMapCanvas.setDrawPath(false);
		loadedMapCanvas.setMap(defaultMap);
		mapNodeArray = interpretMap(defaultMap);
		pav.resetInitStatus();
	}
	
	public Node[][] interpretMap(BufferedImage map)
	{
		int [][] map2 = MapReader.ReadImage(map);
		int height = map.getHeight();
		int width = map.getWidth();
		int baseMapSize = Math.max(height, width);
		if(baseMapSize <= 200)
		{
			sizeOfNewMap = new Dimension(750, baseMapSize+250);
		}
		else
			sizeOfNewMap = new Dimension((baseMapSize)+150, baseMapSize+250);
		loadedMapCanvas.setBaseMapSize(baseMapSize);
		loadedMapCanvas.setBaseMapHeight(height);
		loadedMapCanvas.setBaseMapWidth(width);
		loadedMapCanvas.setCurrMapHeight(height);
		loadedMapCanvas.setCurrMapWidth(width);
		Node [][] nodemap2 = MapReader.CreateNode(map2);
		//System.out.println("Map width: "+width+"   Map height: "+height);
		return nodemap2;
	}
	
	public GUICanvas getMapCanvas()
	{
		return loadedMapCanvas;
	}
	
	
	public JTextField getMessageBar()
	{
		return theMessage;
	}
	
	public void updateCanvasSize(int panelX, int panelY)
	{
		int minDimension = loadedMapCanvas.getBaseMapSize();
		int baseHeight = loadedMapCanvas.getBaseMapHeight();
		int baseWidth = loadedMapCanvas.getBaseMapWidth();
		int currWinHeight = this.getHeight();
		int currWinWidth = this.getWidth();
		int factor = loadedMapCanvas.getFactor();
		if((((factor*minDimension)+50) < currWinWidth) && (((factor*minDimension)+200) < currWinHeight))
		{
			if(((((factor+1)*minDimension)+50) < currWinWidth) && ((((factor+1)*minDimension)+200) < currWinHeight))
			{
				loadedMapCanvas.setCurrMapHeight((factor+1)*baseHeight);
				loadedMapCanvas.setCurrMapWidth((factor+1)*baseWidth);
				resetCanvasSize((factor+1)*minDimension);
				updateCanvas();
				repaintCanvases();
			}
		}
		else
		{
			if(factor != 1)
			{
				loadedMapCanvas.setCurrMapHeight((factor-1)*baseHeight);
				loadedMapCanvas.setCurrMapWidth((factor-1)*baseWidth);
				resetCanvasSize((factor-1)*minDimension);
				updateCanvas();
				repaintCanvases();
			}
		}
		//System.out.println("Factor is: "+factor);
		//System.out.println("currHeight:  "+loadedMapCanvas.getCurrMapHeight()+"   currWidth: "+loadedMapCanvas.getCurrMapWidth());
	}
	
	public void canvasMaxRestore(int panelX, int panelY)
	{
		int minDimension = loadedMapCanvas.getBaseMapSize();
		int currHeight = loadedMapCanvas.getCurrMapHeight();
		int currWidth = loadedMapCanvas.getCurrMapWidth();
		int baseHeight = loadedMapCanvas.getBaseMapHeight();
		int baseWidth = loadedMapCanvas.getBaseMapWidth();
		//for(int a = 0; a < 5; a++){
		int factor = 1;
		while((((factor*minDimension)+50) < panelX) && (((factor*minDimension)+200) < panelY))
		{
			factor = factor+1;
		}
		if(factor!=1)
		{
			factor = factor-1;
		}
		//System.out.println("Factor is: "+factor);
		//System.out.println("currHeight:  "+currHeight+"   currWidth: "+currWidth);
		loadedMapCanvas.setCurrMapHeight(factor*baseHeight);
		loadedMapCanvas.setCurrMapWidth(factor*baseWidth);
		resetCanvasSize(factor*minDimension);
		updateCanvas();
		repaintCanvases();
		//}
		
	}
	
	public void resetCanvasSize(int x)
	{
		Dimension csize = new Dimension(x,x);
		loadedMapCanvas.setPreferredSize(csize);		
	}
	
	public void updateCanvas()
	{
		loadedMapCanvas.revalidate();
		top.revalidate();
	}
	
	public void setCanvasFactors()
	{
		loadedMapCanvas.setFactor();
	}
	
	public int getCanvasFactors()
	{
		return loadedMapCanvas.getFactor();
	}
	
	public void setMessageText(String t)
	{
		theMessage.setText(t);
		repaint();
	}
	
	public void repaintCanvases()
	{
		loadedMapCanvas.repaint();
	}
	
	public void movePAVUp()
	{
		if(pav.getCurrentY()!=0)
		{
			pav.setCurrentY(pav.getCurrentY() - 1);
			loadedMapCanvas.setCurrentLoc(pav.getCurrentX(), pav.getCurrentY(), true, true);
			currentLocX.setText(""+pav.getCurrentX());
			currentLocY.setText(""+pav.getCurrentY());
		}
	}
	
	public void movePAVDown()
	{
		if(pav.getCurrentY()!=199)
		{
			pav.setCurrentY(pav.getCurrentY() + 1);
			loadedMapCanvas.setCurrentLoc(pav.getCurrentX(), pav.getCurrentY(), true, true);
			currentLocX.setText(""+pav.getCurrentX());
			currentLocY.setText(""+pav.getCurrentY());
		}
	}
	
	public void movePAVLeft()
	{
		if(pav.getCurrentX()!=0)
		{
			pav.setCurrentX(pav.getCurrentX() - 1);
			loadedMapCanvas.setCurrentLoc(pav.getCurrentX(), pav.getCurrentY(), true, true);
			currentLocX.setText(""+pav.getCurrentX());
			currentLocY.setText(""+pav.getCurrentY());
		}
	}
	
	public void movePAVRight()
	{
		if(pav.getCurrentX()!=199)
		{
			pav.setCurrentX(pav.getCurrentX() + 1);
			loadedMapCanvas.setCurrentLoc(pav.getCurrentX(), pav.getCurrentY(), true, true);
			currentLocX.setText(""+pav.getCurrentX());
			currentLocY.setText(""+pav.getCurrentY());
		}
	}
	
	public Dimension newMapSize()
	{
		return sizeOfNewMap;
	}
	
	public void incrementPosition()
	{
		loadedMapCanvas.incrementNode();
		if(loadedMapCanvas.getCurrentNode().isValid())
		{
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					loadedMapCanvas.repaint();
				}
			});
		}
	}
}

class GUICanvas extends JPanel
{
	private int currentX;
	private int currentY;
	private int destinationX;
	private int destinationY;
	private int factor;
	private int baseMapSize;
	private int currMapHeight;
	private int currMapWidth;
	private int baseMapHeight;
	private int baseMapWidth;
	//private double preferredSizeX;
	//private double preferredSizeY;
	private BufferedImage theMap;
	private LinkedList path;
	private LinkedListIterator currentNode;
	private boolean drawPath = false;
	//private Dimension preferredPSize;
	
	public void initGUICanvas()
	{
		currentX = 0;
		currentY = 0;
		destinationX = 0;
		destinationY = 0;
		factor = 1;
		theMap = null;
		path = null;
	}
	
	public void incrementNode()
	{
		currentNode.advance();
	}
	
	public LinkedListIterator getCurrentNode()
	{
		return currentNode;
	}
	
	public void setCurrentLoc(int x, int y, boolean drawPath, boolean repaint)
	{
		currentX = x;
		currentY = y;
		this.setDrawPath(drawPath, repaint);
	}
	
	public int getCurrentX()
	{
		return currentX;
	}
	
	public int getCurrentY()
	{
		return currentY;
	}
	
	public void setDestination(int val, char cor, boolean drawPath, boolean repaint)
	{
		if(cor == 'x')
		{
			destinationX = val;
		}
		else if(cor == 'y')
		{
			destinationY = val;
		}
		this.setDrawPath(drawPath, repaint);
	}
	
	
	public void setDestination(int x, int y, boolean drawPath, boolean repaint)
	{
		destinationX = x;
		destinationY = y;
		this.setDrawPath(drawPath, repaint);
	}
	
	public void setCoordinates(int cX, int cY, int dX, int dY)
	{
		currentX = cX;
		currentY = cY;
		destinationX = dX;
		destinationY = dY;
	}
	
	public int getDestX()
	{
		return destinationX;
	}
	
	public int getDestY()
	{
		return destinationY;
	}
	
	public void setMap(BufferedImage map)
	{
		theMap = map;
	}
	
	public void setBaseMapSize(int baseSize)
	{
		this.baseMapSize = baseSize;
	}
	
	public int getBaseMapSize()
	{
		return this.baseMapSize;
	}
	
	public void setCurrMapHeight(int height)
	{
		this.currMapHeight = height;
	}
	
	public int getCurrMapHeight()
	{
		return this.currMapHeight;
	}
	
	public void setCurrMapWidth(int Width)
	{
		this.currMapWidth = Width;
	}
	
	public int getCurrMapWidth()
	{
		return this.currMapWidth;
	}
	
	public void setBaseMapWidth(int Width)
	{
		this.baseMapWidth = Width;
	}
	
	public int getBaseMapWidth()
	{
		return this.baseMapWidth;
	}
	
	public void setBaseMapHeight(int Height)
	{
		this.baseMapHeight = Height;
	}
	
	public int getBaseMapHeight()
	{
		return this.baseMapHeight;
	}
	
	public void setPath(LinkedList p)
	{
		path = p;
		currentNode = path.first();
	}
	
	public void setDrawPath(boolean set)
	{
		drawPath = set;
	}
	
	public void setDrawPath(boolean set, boolean redraw)
	{
		drawPath = set;
		if(redraw&&(theMap!=null))
		{
			repaint();
		}
	}
	
	public boolean getDrawPath()
	{
		return drawPath;
	}
	
	public void setFactor()
	{
		factor = this.getWidth()/this.baseMapSize;
	}
	
	public int getFactor()
	{
		return this.factor;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		int dcX, dcY, ddX, ddY;
		this.factor = (this.getWidth())/this.baseMapSize;
		//System.out.println("Factor from paint is: "+factor);
		ddX = factor*destinationX;
		/*
		if(ddX > (factor))
		{
			ddX = ddX - (factor);
		}
		*/
		ddY = factor*destinationY;
		/*
		if(ddY > (factor))
		{
			ddY = ddY - (factor);
		}
		*/
		dcX = factor*currentX;
		/*
		if(dcX > (factor))
		{
			dcX = dcX - (factor);
		}
		*/
		dcY = factor*currentY;
		/*
		if(dcY > (factor))
		{
			dcY = dcY - (factor);
		}
		*/
		//System.out.println("Map Width: "+currMapWidth+"   Map Height: "+currMapHeight);
		g.drawImage(theMap, 0, 0, this.currMapWidth, this.currMapHeight, null);
		if(drawPath && (path!=null))
		{
			if(path.isEmpty()==false)
			{
				LinkedListIterator itr = path.first();
				g.setColor(Color.YELLOW);
	            for(;itr.isValid();itr.advance())
	            {
	            	int tempx = ((int)itr.retrieve().getX())*factor;
	            	int tempy = ((int)itr.retrieve().getY())*factor;
	            	g.fillRect(tempx, tempy, factor, factor);
	            }
	            dcX = factor*((int)currentNode.retrieve().getX());
	            dcY = factor*((int)currentNode.retrieve().getY());
			}
		}
		g.setColor(Color.GREEN);
		g.fillOval(ddX, ddY, factor, factor);
		g.setColor(Color.BLUE);
		g.fillOval(dcX, dcY, factor, factor);
		g.setPaintMode();
	}
}
