/**
 * 
 */
package dataset;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

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
	
	// constant values
	private final float colorInc = 0.07f;			// steps of increment for the cluster legend
	private final float colorStart = 0.13f;			// starting value for the color for clusters and clusters legend
	private final int upperChartBorder = 120;			// height of the upper border of the chart area
	private final int lowerChartBorder = 70;				// height of the lower border
	private final int leftChartBorder = 30;
	private final int rightChartBorder = 30;
	private final int borderSize = 20;				// size of the space between axis and the grey boxes
	private final int numberOfTicks = 10;			// number of marker ticks for the two axis 

	// private data members used for configuration
	private int iWidth;
	private int iHeight;
	private String strTitle;																	// title of the plot
	private String strSubTitle;																	// sub-title
	private ArrayList<Task> lstRandomTasks = new ArrayList<Task>();								// list holding the tasks not generated in clusters
	private ArrayList<Resource> lstResources = new ArrayList<Resource>();						// list holding the resources
	private ArrayList<ClusteredTasks> lstClusteredTasks = new ArrayList<ClusteredTasks>();		// list holding the tasks generated in clusters
	private VehicleRoutingProblemSolution solution;												// problem solution to be possibly plotted
	private BatchConfig configItem = new BatchConfig();											// object possibly holding the config parameters used to generate the ds
	private String strFileName;																	// String holding the name of the ds being plotted
	private String strPath;																		// String holding the path of the ds being plotted
	
	// behaviour flags
	private boolean bWriteTasksText = true;														// specify if text on tasks has to be written
	private boolean bShowClusterRadius = true;													// specify if radius of the clusters has to be shown
	private boolean bUseDiffColorsPerCluster = true;											// specify if different colors have to be used per each cluster
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
	 * Plots the image with the currently loaded lists of tasks and resources
	 * 
	 */
	public void plot () {
		
        double maxX = -100000;
        double maxY = -100000;
        double minX = 100000;
        double minY = 100000;
 
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

        // starts drawing
		Font font1 = new Font("Arial", Font.BOLD, 8);
		g.setFont(font1);
        
        // drawing nodes of unclustered tasks first
        if (lstRandomTasks != null) 
        	for (Task tsk : lstRandomTasks) {
	        	int lat = getScaledLat(tsk.getNode().getLatitude());
	        	int lon = getScaledLon(tsk.getNode().getLongitude());
	        	g.drawRect(
	        			lat-2, 
	        			lon-3, 
	        			4, 
	        			4);
	            
//	        	PerroUtils.print(tsk.getId() + " - old coord: " + tsk.getNode().getLatitude() + "; " + tsk.getNode().getLongitude() + " -> "+ lat + "; "+ lon);

	        	if (bWriteTasksText) 
	            	drawCenteredString(tsk.getType()+" "+tsk.getId(), lat ,lon);
	        }
        // then draw nodes belonging to clusters
        iTotClusteredTasks = 0;
        
        if (lstClusteredTasks != null) {
            Color curColor = g.getColor();
            float hue = colorStart;
            
        	for (ClusteredTasks cluster : lstClusteredTasks) {
        		
        		int clusterIndex = lstClusteredTasks.indexOf(cluster);
        		
        		if (bUseDiffColorsPerCluster) {
        			Color tmpCol = Color.getHSBColor(hue, 1.0f, 1.0f);
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
//            		PerroUtils.print("C: " + clusterCenterLat + ";" + clusterCenterLon + " - R = "+ cluster.getiClusterRadius() + " -> " + iRadiusLat + ";" + iRadiusLon);
            	}
            	         	
            	iTotClusteredTasks += cluster.getLstTasksInCluster().size();
        	}
            g.setColor(curColor);
        }

        		
        
        // drawing resources
        g.setColor(Color.red);
        if (lstResources != null)
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
	    
	    // draw information taken from configItem
	    g.setColor(Color.white);
	    g.fillRect(10, 10, 200, 75);
	    g.setColor(Color.black);    
	    g.setFont(new Font("Courier", Font.BOLD, 9));
        g.drawString("MaxX      = " + String.format("%.1f", maxX), 20, 20);
        g.drawString("MaxY      = " + String.format("%.1f", maxY), 20, 30);
        g.drawString("#Res      = " + lstResources.size(), 20, 40);
        g.drawString("#RNDTasks = " + lstRandomTasks.size(), 20, 50);
        g.drawString("#Clusters = " + configItem.getiNumClusters(), 20, 60);
        g.drawString("#CluTasks = " + iTotClusteredTasks, 20, 70);
        g.drawString("Spread f  = " + configItem.getdExpFactor(), 20, 80);
        
        // if I have clustered tasks then write the legend
        // REM: I have to paint the legend here because AFTER I have created the chart I draw the grey boxes at the borders
        // and therefore if I paint the legend when I am drawing the chart the legend will be gone :)
        if (lstClusteredTasks != null) {

        	Color curColor = g.getColor();
            float hue = colorStart;
            
            int iClusterLegendLongitude = iHeight - lowerChartBorder + borderSize + 5;		// starting Y coordinate for legend and for legend box

            g.setColor(Color.white);
    	    g.fillRect(leftChartBorder + 50, iClusterLegendLongitude, iWidth - (leftChartBorder + rightChartBorder + 100), 31);
    	    g.setColor(Color.black);    

        	for (ClusteredTasks cluster : lstClusteredTasks) {
        		
        		int clusterIndex = lstClusteredTasks.indexOf(cluster);
        		
        		if (bUseDiffColorsPerCluster) {
        			Color tmpCol = Color.getHSBColor(hue, 1.0f, 1.0f);
        			hue += colorInc;
        			if (hue >= 1) 
        				hue = colorStart;
        			g.setColor(tmpCol);
        		}
       			g.drawString("Cluster " + clusterIndex, (leftChartBorder + 55) + ( (int) (clusterIndex / 3) ) * 55, iClusterLegendLongitude + 8 + (clusterIndex % 3) * 10);
        	}
        	g.setColor(curColor);
        }
		writeOnDisk();
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
		
		String sPath = strPath + "/DSPlot";
		PerroUtils.prepareFolder(sPath, false);
		
		try {
			ImageIO.write(bi, "PNG", new File(sPath + "/" + strFileName + "_PLOT.png"));
//			ImageIO.write(bi, "JPG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\pippo.jpg"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
