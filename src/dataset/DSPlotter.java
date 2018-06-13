/**
 * 
 */
package dataset;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import objects.*;
import utils.PerroUtils;


/**
 * DSPlotter
 * This class is used to generate plots on display and disk of the datasets
 * 
 * @version 1.0 08/06/18
 * @author giovanni_perrone
 *
 */
public class DSPlotter {
	
	/**
	// private class used to store the tasks that are generated as clusters
	private class ClusteredTasks {
		private ArrayList<Task> lstTasksInCluster = new ArrayList<Task>();
	}
	*/
	
	// graphics objects
	private Graphics2D g;
	private BufferedImage bi;
	
	// private data members used for chart generation
	private double scaleX;
	private double scaleY;
	private int iTotClusteredTasks;
    private double maxX = -100000;
    private double maxY = -100000;
    private double minX = 100000;
    private double minY = 100000;
	
	// constant values
	private final float colorInc = 0.07f;			// steps of increment for the cluster legend
	private final float colorStart = 0.13f;			// starting value for the color for clusters and clusters legend
	private final int upperChartBorder = 130;			// height of the upper border of the chart area
	private final int lowerChartBorder = 70;				// height of the lower border
	private final int leftChartBorder = 30;
	private final int rightChartBorder = 30;
	private final int borderSize = 20;				// size of the space between axis and the grey boxes
	private final int numberOfTicks = 10;			// number of marker ticks for the two axis 
	private final int clusterLegendWidth = 400;		// width of the cluster legend
	private final int resourcesLegendWidth = 300;	// width of the resources legend

	// private data members used for configuration
	private int iWidth;
	private int iHeight;
	private String strTitle;																	// title of the plot
	private String strSubTitle;																	// sub-title
	private ArrayList<Task> lstRandomTasks = new ArrayList<Task>();								// list holding the tasks not generated in clusters
	private ArrayList<Resource> lstResources = new ArrayList<Resource>();						// list holding the resources
	private ArrayList<ClusteredTasks> lstClusteredTasks;										// list holding the tasks generated in clusters
	private VehicleRoutingProblemSolution solution;												// problem solution to be possibly plotted
	private BatchConfig configItem;																// object possibly holding the config parameters used to generate the ds
	private SolStats solutionStats;																// optional object storing results of solution
	private String strFileName;																	// String holding the name of the ds being plotted
	private String strPath;																		// String holding the path of the ds being plotted
	private String strSubFolder;																// optional string holding the subfolder where png has to be written
	
	// behaviour flags
	private boolean bWriteTasksText = true;														// specify if text on tasks has to be written
	private boolean bShowClusterRadius = true;													// specify if radius of the clusters has to be shown
	private boolean bUseDiffColorsPerCluster = true;											// specify if different colors have to be used per each cluster
	private boolean bUSeDiffColorsPerResources = true;											// specify if different colors have to be used per each resource when plotting a solution
	private boolean bShowAxisTicks = true;
	
	// constructor used to initialize the object
	public DSPlotter (int w, int h, String title) {
		
		// set some of the global variables
		iWidth = w;
		iHeight = h;
		strTitle = title;
		
		
		// create the image and store it in the Graphics2D object
		bi = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
		g = bi.createGraphics();

		// fill the background
		g.setColor(Color.white);
      	g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      	g.setColor(Color.black);

	}

	/**
	 * Calculates the scaling factors and then plots the image with the different items loaded based on the config parameters
	 * 
	 */
	public void plot () {
		
        // retrieves the maximum values of X and Y
		// if the BatchConfig obj is loaded use it otherwise calculate the values based on the max coordinates of the tasks
		if (configItem == null) {
			if (lstRandomTasks != null)					// performs this loop only if the lstTasks is not empty
		        for (Task tsk : lstRandomTasks) {
		        	double lat = tsk.getNode().getLatitude();
		        	double lon = tsk.getNode().getLongitude();
		        	
		        	maxX = (maxX <= lat) ? lat : maxX;
		        	minX = (minX >= lat) ? lat : minX;
		        	
		        	maxY = (maxY <= lon) ? lon : maxY;       	
		        	minY = (minY >= lon) ? lon : minY;
		        }
			// placeholder for the cluster generated tasks list

		}
		else {
			maxX = configItem.getMaxX();
			minX = 0;
			maxY = configItem.getMaxY();
			minY = 0;
		}
		
		// calculates the scaling factors
        scaleX = (iWidth - (leftChartBorder + rightChartBorder))/(maxX - minX);
        scaleY = (iHeight - (upperChartBorder + lowerChartBorder))/(maxY - minY);
      
        // drawing nodes of unclustered tasks first
        if (lstRandomTasks != null) 
        	drawRandomTasks();
        
        // then draw nodes belonging to clusters
        iTotClusteredTasks = 0;
        if (lstClusteredTasks != null)
        	drawClusteredTasks();
         
        // draw axis, title, subtitle and grey borders
        drawGraphFrame();
        
        // if I have clustered tasks then write the legend
        if (lstClusteredTasks != null) 
        	drawClusterLegend();

	    // draw information taken from configItem
        drawConfigItemInfo();
        
        // drawing resources
        if (lstResources != null)
        	drawResources();
        
        // draw the solution (if loaded);
        if (solution != null) 
        	drawSolution();
              
        // plot information regarding the solution (if available)
        if (solutionStats != null)
        	drawSolutionStats();
               
        writeOnDisk();
	}

	/*
	 * Draws the tasks generated randomly
	 * 
	 */
	private void drawRandomTasks() {
		g.setColor(Color.GRAY);
		g.setFont(new Font("Arial", Font.BOLD, 8));
    	for (Task tsk : lstRandomTasks) {
        	int lat = getScaledLat(tsk.getNode().getLatitude());
        	int lon = getScaledLon(tsk.getNode().getLongitude());
        	g.drawRect(
        			lat-2, 
        			lon-3, 
        			4, 
        			4);
        	if (bWriteTasksText) 
            	drawCenteredString(tsk.getType()+" "+tsk.getId(), lat ,lon);
        }
	}

	/**
	 * Draws information from the configitem (if loaded)
	 * 
	 */
	private void drawConfigItemInfo() {
	    g.setColor(Color.white);
	    g.fillRect(10, 10, 200, 75);
	    g.setColor(Color.black);    
	    g.setFont(new Font("Courier", Font.PLAIN, 9));
        g.drawString("MaxX      = " + String.format("%.1f", maxX), 20, 20);
        g.drawString("MaxY      = " + String.format("%.1f", maxY), 20, 30);
        g.drawString("#Res      = " + lstResources.size(), 20, 40);
        
        if (configItem != null) {
            g.drawString("#RNDTasks = " + lstRandomTasks.size(), 20, 50);
        	g.drawString("#Clusters = " + configItem.getiNumClusters(), 20, 60);
        	g.drawString("#CluTasks = " + iTotClusteredTasks, 20, 70);
        	g.drawString("Spread f  = " + configItem.getdExpFactor(), 20, 80);
        }
        else
            g.drawString("#Tasks    = " + lstRandomTasks.size(), 20, 50);

	}
	
	/**
	 * Draws the clustered tasks into the graphics object
	 * 
	 */
	private void drawClusteredTasks() {
        Color curColor = g.getColor();
        float hue = colorStart;
        
    	for (ClusteredTasks cluster : lstClusteredTasks) {
    		
    		int clusterIndex = lstClusteredTasks.indexOf(cluster);
    		
    		if (bUseDiffColorsPerCluster) {
    			Color tmpCol = Color.getHSBColor(hue, 0.4f, 0.6f);
    			hue += colorInc;
    			if (hue >= 1) 
    				hue = colorStart;
    			g.setColor(tmpCol);
    		}
    		
    		// first of all draw the cluster center
    		int clusterCenterLat =getScaledLat(cluster.getNdClusterCenter().getLatitude());
    		int clusterCenterLon =getScaledLon(cluster.getNdClusterCenter().getLongitude());
    		
        	g.fillOval(
        			clusterCenterLat - 2, 
        			clusterCenterLon - 3,
        			4, 
        			4);
        	if (bWriteTasksText) 
            	drawCenteredString("C" + clusterIndex, clusterCenterLat ,clusterCenterLon);
        	        		
    		// then draw the tasks
        	ArrayList<Task> tmpLstTasks = new ArrayList<Task>(); 
        	tmpLstTasks = cluster.getLstTasksInCluster();			// create a temp list for sake of simplicity and speed of access
        	for (Task tsk : tmpLstTasks) {
	        	int lat = getScaledLat(tsk.getNode().getLatitude());
	        	int lon = getScaledLon(tsk.getNode().getLongitude());
	        	g.drawRect(
	        			lat-2, 
	        			lon-3, 
	        			4, 
	        			4);
	            
	        	if (bWriteTasksText) 
	            	drawCenteredString(tsk.getType()+" "+tsk.getId(), lat ,lon);
	        }
        	
        	// then draw the radius
        	if (bShowClusterRadius) {				// need to calculate (x,y) of the upper left corner of the radius since drawOval doesn't work in polar coord
        		int iRadiusLat = (int) (cluster.getiClusterRadius() * scaleX);
        		int iRadiusLon = (int) (cluster.getiClusterRadius() * scaleY);
        		
        		g.drawOval(
        				clusterCenterLat - iRadiusLat - 2, 
        				clusterCenterLon - iRadiusLon - 3,
	        			2 * iRadiusLat,
	        			2 * iRadiusLon);
//	            		PerroUtils.print("C: " + clusterCenterLat + ";" + clusterCenterLon + " - R = "+ cluster.getiClusterRadius() + " -> " + iRadiusLat + ";" + iRadiusLon);
        	}
        	         	
        	iTotClusteredTasks += cluster.getLstTasksInCluster().size();
    	}
        g.setColor(curColor);
    }       		

	
	/**
	 * Draws the box with the information taken from the solution stats object
	 */
	private void drawSolutionStats() {
    	int startLat = iWidth - 210;
	    g.setColor(Color.white);
	    g.fillRect(startLat, 10, 200, 85);		// box has same size of the configItem box but displayed on the right
	    startLat += 10;
	    g.setColor(Color.black);    
	    g.setFont(new Font("Courier", Font.PLAIN, 9));
        g.drawString("Serviced tasks  = " + solutionStats.getiTotServiced(), startLat, 20);
        g.drawString("Unsrvcd tasks   = " + solutionStats.getiTotUnserviced(), startLat, 30);
        g.drawString("Time violations = " + String.format("%.1f", solutionStats.getDbTimeWinViolation()), startLat, 40);
        g.drawString("#Vehicles used  = " + solutionStats.getiNumVehiclesUsed(), startLat, 50);
        g.drawString("Execution time  = " + String.format("%.1f", solutionStats.getDblExecutionTime()), startLat, 60);

        g.drawString("Total costs     = " + String.format("%.1f", solutionStats.getDbTotalCosts()), startLat, 70);
        g.drawString("Traveled dist   = " + String.format("%.1f", solutionStats.getDbTraveledDistance()), startLat, 80);
        g.drawString("Operation time  = " + String.format("%.1f", solutionStats.getDbOperationTime()), startLat, 90);
	}
	
	/**
	 * Draws the legend for the clusters
	 * 
	 * REM: I have to paint the legend here because AFTER I have created the chart I draw the grey boxes at the borders
	 * and therefore if I paint the legend when I am drawing the chart the legend will be gone :)
	 */
	private void drawClusterLegend() {

    	Color curColor = g.getColor();
        float hue = colorStart;
        
        int iClusterLegendLongitude = iHeight - lowerChartBorder + borderSize + 5;		// starting Y coordinate for legend and for legend box

        g.setColor(Color.white);
	    g.fillRect(leftChartBorder, iClusterLegendLongitude, iWidth - (leftChartBorder + rightChartBorder + clusterLegendWidth), 31);
	    g.setColor(Color.black);    

	    g.setFont(new Font("Arial", Font.BOLD, 9));

    	for (ClusteredTasks cluster : lstClusteredTasks) {
    		
    		int clusterIndex = lstClusteredTasks.indexOf(cluster);
    		
    		if (bUseDiffColorsPerCluster) {
    			Color tmpCol = Color.getHSBColor(hue, 0.4f, 0.6f);
    			hue += colorInc;
    			if (hue >= 1) 
    				hue = colorStart;
    			g.setColor(tmpCol);
    		}
   			g.drawString("Cluster " + clusterIndex, (leftChartBorder) + ( (int) (clusterIndex / 3) ) * 55, iClusterLegendLongitude + 8 + (clusterIndex % 3) * 10);
    	}
    	g.setColor(curColor);
    }

	
	/**
	 * Draws the resources on the chart
	 * 
	 * No params
	 */
	private void drawResources() {
    	
	    g.setFont(new Font("Arial", Font.BOLD, 9));
        g.setColor(Color.red);
       	for (Resource rsc : lstResources) {
        	int lat = getScaledLat(rsc.getOrigin().getLatitude());
        	int lon = getScaledLon(rsc.getOrigin().getLongitude());
        	g.drawOval(
        			lat-1, 
        			lon-2, 
        			3, 
        			3);

        	if (bWriteTasksText) 
        		drawCenteredString("R"+rsc.getId(), lat ,lon);
        	}
	}
	
	/**
	 * Draws borders, title, subtitle, axis and ticks
	 * 
	 * No params
	 */
	private void drawGraphFrame() {
        // fill borders
        g.setColor(Color.gray);
        // draw 4 boxes to fill the borders leaving a border to be able to see points drawn on the axes       
        // lower part
	    g.fillRect(0, iHeight - (lowerChartBorder - borderSize), iWidth, iHeight);
	    // upper part
	    g.fillRect(0, 0, iWidth, (upperChartBorder - borderSize));
	    // left
	    g.fillRect(0,  0,  (leftChartBorder - borderSize), iHeight);
	    // right
	    g.fillRect(iWidth - (rightChartBorder - borderSize) , 0, iWidth, iHeight);
	    
	    // draw axes
	    g.setColor(Color.black);
	    g.drawRect(leftChartBorder, upperChartBorder, iWidth-(leftChartBorder + rightChartBorder), iHeight-(upperChartBorder + lowerChartBorder));
	    
	    // draw axis ticks
	    if (bShowAxisTicks) {
	    	g.setFont(new Font("Arial", Font.PLAIN, 10));

	    	int step = (int) ( (iWidth-(leftChartBorder + rightChartBorder)) / numberOfTicks );
	    	for (int i = 0 ; i <= numberOfTicks; i++) {
	    		int lat = leftChartBorder + i*step;
	    		int lon = iHeight - lowerChartBorder;
	    		g.drawRect(lat, lon, 1, 5);
	    		drawCenteredString(String.format("%.1f", (maxX / numberOfTicks) * i), lat, lon + 20);
	    	}

	    	AffineTransform fontTransform = new AffineTransform();
    		fontTransform.rotate(Math.toRadians(90));
    		Font rotatedFont = g.getFont().deriveFont(fontTransform);    		
    		g.setFont(rotatedFont);

	    	step = (int) ( (iHeight-(upperChartBorder + lowerChartBorder)) / numberOfTicks );    	
	    	for (int i = 0; i <= numberOfTicks; i++) {
	    		int lat = leftChartBorder;
	    		int lon = iHeight - lowerChartBorder - i*step;
	    		g.drawRect(lat-5, lon, 5, 1);
	    		String str = String.format("%.1f", (maxX / numberOfTicks) * i);

	    		g.drawString(str,  lat - 13, lon - 10 );		// since the font is rotated cannot use the drawCenteredString method
	    	}
	    }

	    // draws Title and subTitle
	    g.setFont(new Font("Arial", Font.BOLD, 20));
	    g.setColor(Color.yellow);
	    drawCenteredString(strTitle, iWidth/2, 40);

	    g.setFont(new Font("Arial", Font.BOLD, 16));
	    g.setColor(Color.BLUE);
	    drawCenteredString(strSubTitle, iWidth / 2, 60);

	}
	
	/**
	 * Draws the solution and the resources legend in the current graphics object
	 * 
	 * No parameters as inputs
	 */
	private void drawSolution() {
        Color curColor = g.getColor();
        float hue = colorStart + 0.03f;
	    g.setFont(new Font("Arial", Font.BOLD, 9));

        int iResourceLegendLongitude = iHeight - lowerChartBorder + borderSize + 5;		// starting Y coordinate for legend and for legend box
        int iResourceLegendLatitude = iWidth - (resourcesLegendWidth + leftChartBorder + rightChartBorder);
        		
        // draw the legend box
        g.setColor(Color.white);
	    g.fillRect(iResourceLegendLatitude, iResourceLegendLongitude, iWidth - (leftChartBorder + rightChartBorder + iResourceLegendLatitude), 31);
	    g.setColor(Color.black);
	    
    	// arrowhead
    	Polygon arrowHead = new Polygon();  
    	arrowHead.addPoint( 0,5);
    	arrowHead.addPoint( -5, -5);
    	arrowHead.addPoint( 5,-5);
    	
        // scans the solution to find which tasks have been serviced and populate the relevant list
        for (VehicleRoute vhcRt : solution.getRoutes() ) {
        	
       		if (bUSeDiffColorsPerResources) {
    			Color tmpCol = Color.getHSBColor(hue, 0.6f, 0.8f);
    			hue += colorInc;
    			if (hue >= 1) 
    				hue = colorStart;
    			g.setColor(tmpCol);
    		}
       		// draw the resource name in the legend
       		int resIndex = vhcRt.getVehicle().getIndex() - 1;
       		g.drawString("Res " + resIndex, (iResourceLegendLatitude) + ( (int) (resIndex / 3) ) * 55, 
       										iResourceLegendLongitude + 8 + (resIndex % 3) * 10);

        	// per each vehicle create a list of nodes visited
        	
        	ArrayList<Node> routeNodes = new ArrayList<Node>();
        	// add starting location
        	routeNodes.add(new Node(vhcRt.getVehicle().getStartLocation().getCoordinate().getX(), 
        							vhcRt.getVehicle().getStartLocation().getCoordinate().getY()));				
        	
        	// add coordinates per each task visited
        	for (TourActivity act : vhcRt.getActivities()) 
        		routeNodes.add(new Node(act.getLocation().getCoordinate().getX(),
        								act.getLocation().getCoordinate().getY()));
        	
        	// add end location
        	routeNodes.add(new Node(vhcRt.getVehicle().getEndLocation().getCoordinate().getX(), 
        							vhcRt.getVehicle().getEndLocation().getCoordinate().getY()));
        	
        	
        	// and then draw the route
    		Node tmp1 = new Node();
    		Node tmp2 = new Node();
        	for (int i = 0; i < (routeNodes.size() - 1); i++) {
        		tmp1 = routeNodes.get(i);
        		tmp2 = routeNodes.get(i+1);

        		g.drawLine(
        				getScaledLat(tmp1.getLatitude()), 
        				getScaledLon(tmp1.getLongitude()),
        				getScaledLat(tmp2.getLatitude()),
        				getScaledLon(tmp2.getLongitude()) );
        		
        		if (i == 0) {
        			// I am drawing the first connection -> draw the arrowhead
        			AffineTransform tx = new AffineTransform();
        			tx.setToIdentity();
        			double angle = Math.atan2(getScaledLon(tmp2.getLongitude())-getScaledLon(tmp1.getLongitude()), 
        									  getScaledLat(tmp2.getLatitude())-getScaledLat(tmp1.getLatitude()));
        			tx.translate(getScaledLat(tmp2.getLatitude()), getScaledLon(tmp2.getLongitude()));
        			tx.rotate(angle-Math.PI/2d);
        			g.setTransform(tx);
        			g.fill(arrowHead);
        			tx.setToIdentity();
        			g.setTransform(tx);
        		}
        	}
        }
	g.setColor(curColor);
	}
	
	/**
	 * Draws the string passed as parameter centered above the coordinates passed as parameter
	 * 
	 * @param str	String to be plotted
	 * @param lat	X coordinate (for the starting point)
	 * @param lon	Y coordinate (for the starting point)
	 */
	private void drawCenteredString (String str, int lat, int lon) {
	    FontMetrics fontMetrics = g.getFontMetrics();
		int stringWidth = fontMetrics.stringWidth(str);
	    int stringHeight = fontMetrics.getAscent();    
		g.drawString(str, lat - (stringWidth/2) ,lon - (stringHeight / 2 ));
	}
	
	/*
	 * returns the latitude scaled accordingly to the scaling factor for X axis
	 * assumes scaleX is already calculated
	 */
	private int getScaledLat(double lat) {
		return (int) (leftChartBorder + lat * scaleX);
	}

	/*
	 * returns the longitude scaled accordingly to the scaling factor for Y axis
	 * assumes scaleY is already calculated
	 */
	private int getScaledLon(double lon) {
//		return (int) ( (iHeight - 50) - (lon * scaleY));
		return (int) ( (iHeight - lowerChartBorder ) - (lon * scaleY));
	}

	/**
	 * writes on disk the plot generated
	 */
	public void writeOnDisk () {
		
		String sPath = strPath + "DSPlot";
		PerroUtils.prepareFolder(sPath, false);

		if (strSubFolder != null) {
			sPath += "/" + strSubFolder;
			PerroUtils.prepareFolder(sPath, false);
		}
		
		try {
			ImageIO.write(bi, "PNG", new File(sPath + "/" + PerroUtils.returnFullFileNameWOExtension(strFileName) + ".png"));
//			ImageIO.write(bi, "JPG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\pippo.jpg"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * erase the graphics
	 */
	public void clear() {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, iWidth, iHeight);
	}
	

	public Graphics2D getG() {
		return g;
	}

	public void setG(Graphics2D g) {
		this.g = g;
	}

	public BufferedImage getBi() {
		return bi;
	}

	public void setBi(BufferedImage bi) {
		this.bi = bi;
	}

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public int getiWidth() {
		return iWidth;
	}

	public void setiWidth(int iWidth) {
		this.iWidth = iWidth;
	}

	public int getiHeight() {
		return iHeight;
	}

	public void setiHeight(int iHeight) {
		this.iHeight = iHeight;
	}

	public String getStrTitle() {
		return strTitle;
	}

	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}

	public String getStrSubTitle() {
		return strSubTitle;
	}

	public void setStrSubTitle(String strSubTitle) {
		this.strSubTitle = strSubTitle;
	}

	public ArrayList<Task> getLstRandomTasks() {
		return lstRandomTasks;
	}

	public void setLstRandomTasks(ArrayList<Task> lstRandomTasks) {
		this.lstRandomTasks = lstRandomTasks;
	}

	public ArrayList<Resource> getLstResources() {
		return lstResources;
	}

	public void setLstResources(ArrayList<Resource> lstResources) {
		this.lstResources = lstResources;
	}

	public ArrayList<ClusteredTasks> getLstClusteredTasks() {
		return lstClusteredTasks;
	}

	public void setLstClusteredTasks(ArrayList<ClusteredTasks> lstClusteredTasks) {
		this.lstClusteredTasks = lstClusteredTasks;
	}

	public BatchConfig getConfigItem() {
		return configItem;
	}

	public void setConfigItem(BatchConfig configItem) {
		this.configItem = configItem;
	}

	public boolean isbWriteTasksText() {
		return bWriteTasksText;
	}

	public void setbWriteTasksText(boolean bWriteTasksText) {
		this.bWriteTasksText = bWriteTasksText;
	}

	public boolean isbShowClusterRadius() {
		return bShowClusterRadius;
	}

	public void setbShowClusterRadius(boolean bShowClusterRadius) {
		this.bShowClusterRadius = bShowClusterRadius;
	}

	public boolean isbUseDiffColorsPerCluster() {
		return bUseDiffColorsPerCluster;
	}

	public void setbUseDiffColorsPerCluster(boolean bUseDiffColorsPerCluster) {
		this.bUseDiffColorsPerCluster = bUseDiffColorsPerCluster;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int getiTotClusteredTasks() {
		return iTotClusteredTasks;
	}

	public void setiTotClusteredTasks(int iTotClusteredTasks) {
		this.iTotClusteredTasks = iTotClusteredTasks;
	}

	public String getStrFileName() {
		return strFileName;
	}

	public void setStrFileName(String strFileName) {
		this.strFileName = strFileName;
	}

	public String getStrPath() {
		return strPath;
	}

	public void setStrPath(String strPath) {
		this.strPath = strPath;
	}

	public boolean isbShowAxisTicks() {
		return bShowAxisTicks;
	}

	public void setbShowAxisTicks(boolean bShowAxisTicks) {
		this.bShowAxisTicks = bShowAxisTicks;
	}

	public VehicleRoutingProblemSolution getSolution() {
		return solution;
	}

	public void setSolution(VehicleRoutingProblemSolution solution) {
		this.solution = solution;
	}

	public SolStats getSolutionStats() {
		return solutionStats;
	}

	public void setSolutionStats(SolStats solutionStats) {
		this.solutionStats = solutionStats;
	}

	public boolean isbUSeDiffColorsPerResources() {
		return bUSeDiffColorsPerResources;
	}

	public void setbUSeDiffColorsPerResources(boolean bUSeDiffColorsPerResources) {
		this.bUSeDiffColorsPerResources = bUSeDiffColorsPerResources;
	}

	public String getStrSubFolder() {
		return strSubFolder;
	}

	public void setStrSubFolder(String strSubFolder) {
		this.strSubFolder = strSubFolder;
	}

}
