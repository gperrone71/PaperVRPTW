/**
 * 
 */
package dataset;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

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
	double scaleX;
	double scaleY;

	// private data members used for configuration
	private int iWidth;
	private int iHeight;
	private String strTitle;																	// title of the plot
	private String strSubTitle;																	// sub-title
	private ArrayList<Task> lstTasks = new ArrayList<Task>();									// list holding the tasks not generated in clusters
	private ArrayList<Resource> lstResources = new ArrayList<Resource>();						// list holding the resources
	private ArrayList<ClusteredTasks> lstClusteredTasks = new ArrayList<ClusteredTasks>();		// list holding the tasks generated in clusters
	private BatchConfig configItem = new BatchConfig();											// object possibly holding the config parameters used to generate the ds
	
	// behaviour flags
	private boolean bWriteTasksText = true;														// specify if text on tasks has to be written
	private boolean bShowClusterRadius = true;													// specify if radius of the clusters has to be shown
	private boolean bUseDiffColorsPerCluster = true;											// specify if different colors have to be used per each cluster
	
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
			if (lstTasks != null)					// performs this loop only if the lstTasks is not empty
		        for (Task tsk : lstTasks) {
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
		// 100 and 150 are fixed numbers (50 pixels of border on X, 100 on top part of Y and 50 on lower side)
        scaleX = (iWidth - 100)/(maxX - minX);
        scaleY = (iHeight - 150)/(maxY - minY);

        // starts drawing
		Font font1 = new Font("Arial", Font.BOLD, 8);
		g.setFont(font1);
        
        // drawing nodes of unclustered tasks first
        if (lstTasks != null) 
        	for (Task tsk : lstTasks) {
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
        // draw 4 boxes to fill the borders leaving a 10 pixels border to be able to see points drawn on the axes       
        // lower part
	    g.fillRect(0, iHeight - 40, iWidth, iHeight);
	    // upper part
	    g.fillRect(0, 0, iWidth, 90);
	    // left
	    g.fillRect(0,  0,  40, iHeight);
	    // right
	    g.fillRect(iWidth-40, 0, iWidth, iHeight);
	    
	    // draw axes
	    g.setColor(Color.black);
	    g.drawRect(50, 100, iWidth-100, iHeight-150);

	    // plots origin and border coordinates
	    FontMetrics fontMetrics = g.getFontMetrics();
	    g.setFont(new Font("Arial", Font.BOLD, 12));
	    g.drawString("(0;0)", 30, iHeight - 30);
	    String strTmp = "("+String.format("%.1f", maxX) + ";"+ String.format("%.1f", maxY) + ")";
	    int stringWidth = fontMetrics.stringWidth(strTitle);
	    int stringHeight = fontMetrics.getAscent();    
	    g.drawString(strTmp, iWidth-stringWidth/2-50, 80);        

	    // draws Title and subTitle
	    g.setFont(new Font("Arial", Font.BOLD, 20));
	    stringWidth = fontMetrics.stringWidth(strTitle);
	    stringHeight = fontMetrics.getAscent();    
	    g.drawString(strTitle, (iWidth - stringWidth) / 2, 20 + stringHeight / 4);

	    g.setFont(new Font("Arial", Font.BOLD, 16));
	    stringWidth = fontMetrics.stringWidth(strSubTitle);
	    stringHeight = fontMetrics.getAscent();
	    g.drawString(strSubTitle, (iWidth - stringWidth) / 2, 50 + stringHeight / 4);
	    
	    // draw information taken from configItme
	    g.setColor(Color.white);
	    g.fillRect(10, 10, 200, 70);
	    g.setColor(Color.black);    
	    g.setFont(new Font("Courier", Font.BOLD, 9));
        g.drawString("MaxX      = " + String.format("%.1f", maxX), 20, 20);
        g.drawString("MaxY      = " + String.format("%.1f", maxY), 20, 30);
        g.drawString("#Tasks    = " + lstTasks.size(), 20, 40);
        g.drawString("#Res      = " + lstResources.size(), 20, 50);
        g.drawString("#Clusters = " + configItem.getiNumClusters(), 20, 60);
        g.drawString("Spread f  = " + configItem.getdExpFactor(), 20, 70);
        
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
		return (int) (50 + lat * scaleX);
	}

	/*
	 * returns the longitude scaled accordingly to the scaling factor for Y axis
	 * assumes scaleY is already calculated
	 */
	private int getScaledLon(double lon) {
		return (int) ( (iHeight - 50) - (lon * scaleY));
	}

	/**
	 * writes on disk the plot generated
	 */
	public void writeOnDisk () {
		try {
			ImageIO.write(bi, "PNG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\pippo.png"));
			ImageIO.write(bi, "JPG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\pippo.jpg"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public ArrayList<Task> getLstTasks() {
		return lstTasks;
	}

	public void setLstTasks(ArrayList<Task> lstTasks) {
		this.lstTasks = lstTasks;
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

	public void setbWriteTasksText(boolean bWriteTasksText) {
		this.bWriteTasksText = bWriteTasksText;
	}

	public void setbShowClusterRadius(boolean bShowClusterRadius) {
		this.bShowClusterRadius = bShowClusterRadius;
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

}
