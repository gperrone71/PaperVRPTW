/**
 * 
 */
package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import launchers.BatchLauncher;

import java.util.ArrayList;
import java.util.List;

import objects.*;
import problem.Solver1;

/**
 * Very rude class for displaying thw world and the results of the simulation
 * 
 * @author Giovanni
 *
 */

public class DisplayDS extends JComponent {
       
    class NodesConnection {
    	private Resource res;
    	private Task tskO;
    	private Task tskE;
		public Resource getRes() {
			return res;
		}
		public void setRes(Resource res) {
			this.res = res;
		}
		public Task getTskO() {
			return tskO;
		}
		public void setTskO(Task tskO) {
			this.tskO = tskO;
		}
		public Task getTskE() {
			return tskE;
		}
		public void setTskE(Task tskE) {
			this.tskE = tskE;
		}
    	
    }
    
    private List<Task> lstTasks;
    private List<NodesConnection> lstConn;
    
    public DisplayDS(int width, int height) {
        super();
        setPreferredSize(new Dimension(width,height));
        lstTasks = new ArrayList<Task>();
        lstConn = new ArrayList<NodesConnection>();
    }

    /**
     * addNode
     * 
     * Public method to add nodes to be drawn 
     * 
     * @param tsk	the node to be added
     */
    public void addNode(Task tsk) {
    	lstTasks.add(tsk);
//    	PerroUtils.print("Task added with " +tsk.getNode().getLatitude() + "lat and " + tsk.getNode().getLongitude() + " lon");
//    	repaint();
    }
    
    /**
     * addLine
     * 
     * Public method to add lines to be drawn 
     * 
     * @param res	the resource for which we want to draw the line
     * @param tskO	the origin node
     * @param tskE	the end node
     */
    public void addLine(Resource res, Task tskO, Task tskE) {
    	NodesConnection tmpConn = new NodesConnection();
    	tmpConn.setRes(res);
    	tmpConn.setTskO(tskO);
    	tmpConn.setTskE(tskE);
    	lstConn.add(tmpConn);
    }

    
    public void paintComponent(Graphics2D g) {
/*        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
*/        
        // find scaling factor for x and y
        double maxX = -100000;
        double maxY = -100000;
        double minX = 100000;
        double minY = 100000;
        
        for (Task tsk : lstTasks) {
        	double lat = tsk.getNode().getLatitude();
        	double lon = tsk.getNode().getLongitude();
        	
        	maxX = (maxX <= lat) ? lat : maxX;
        	minX = (minX >= lat) ? lat : minX;
        	
        	maxY = (maxY <= lon) ? lon : maxY;       	
        	minY = (minY >= lon) ? lon : minY;
        }
        
		Font font1 = new Font("Arial", Font.BOLD, 8);
		g.setFont(font1);
		
        g.drawString("MaxX = " + maxX, 50, 20);
        g.drawString("MaxY = " + maxY, 50, 30);

        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("(0;0)", 50, 950);
        g.drawString("("+String.format("%.2f", maxX) + ";"+ String.format("%.2f", maxY) + ")", 900, 50);        
        g.setFont(font1);
        
        double scaleX = 800/(maxX - minX);
        double scaleY = 800/(maxY - minY);

        ArrayList<Task> lstNodes_tmp = new ArrayList<Task>();

        // create a new list with with all nodes with the scaling factors
        for (Task tsk : lstTasks) {
           	double lat = tsk.getNode().getLatitude();
        	double lon = tsk.getNode().getLongitude();
 
        	Node nd = new Node();
        	nd = tsk.getNode();
        	
        	/*
           	nd.setLatitude((nd.getLatitude() - minX) * scaleX);
//        	nd.setLongitude( (nd.getLongitude() - minY) * scaleY);
        	nd.setLongitude( (nd.getLongitude() + minY) * scaleY);
        	tsk.setNode(nd);
        	*/

           	nd.setLatitude(	100 + nd.getLatitude()* scaleX);
        	nd.setLongitude( 900 - (nd.getLongitude()  * scaleY) );

        	PerroUtils.print("old coord: " + lat + "; " + lon + " -> "+ nd.getLatitude() + "; "+ nd.getLongitude());

        	lstNodes_tmp.add(tsk);       	
        }
        
        PerroUtils.print("---");
        
        // drawing nodes first
        for (Task tsk : lstNodes_tmp) {
        	int lat = 0;
        	int lon = 0;
        	
        	lat = (int) tsk.getNode().getLatitude();
        	lon = (int) tsk.getNode().getLongitude();
        	
        	PerroUtils.print("drawing :lat "+ lat + " lon "+ lon);
        	g.drawOval(
        			lat, 
        			lon, 
        			3, 
        			3);
            g.drawString(tsk.getType()+" "+tsk.getId(), lat ,lon);
        }
        
        // drawing connections
/*        
        g.setColor(Color.red);
        for (NodesConnection cnt : lstConn) {
            double xPos = (cnt.getTskO().getNode().getLatitude() + cnt.getTskE().getNode().getLatitude() )/ 2;
            double yPos = (cnt.getTskO().getNode().getLongitude() + cnt.getTskE().getNode().getLongitude() ) / 2;

            g.drawLine(
                    (int)cnt.getTskO().getNode().getLatitude()*10,
                    (int)cnt.getTskO().getNode().getLongitude()*10,
                    (int)cnt.getTskE().getNode().getLatitude()*10,
                    (int)cnt.getTskE().getNode().getLongitude()*10 );
            
            g.drawString("R"+cnt.res.getId(),
            		(int)xPos*10,
                    (int)yPos*10);
        }
  */      

    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

    	// finally I can load the xml file and populate the lists
		Solver1 problemSolver = new Solver1("output/C_Batch1_10/", "CG8_10_1_0.xml");
		DisplayDS tmpDS = new DisplayDS(1000,1000);
		tmpDS.setLstTasks((ArrayList<Task>) problemSolver.getLstTasks());
		
		BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		
		Font font = new Font("Arial", Font.BOLD, 20);
	      g.setFont(font);
	      String message = "www.java2s.com!";
	      FontMetrics fontMetrics = g.getFontMetrics();
	      int stringWidth = fontMetrics.stringWidth(message);
	      int stringHeight = fontMetrics.getAscent();
	      
	      g.setColor(Color.white);
	      g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
	      g.setColor(Color.black);

	      g.drawString(message, (1000- stringWidth) / 2, 100 / 2 + stringHeight / 4);

	    // draw axes
		g.drawRect(100, 100, 800, 800);

		Font font1 = new Font("Arial", Font.BOLD, 8);
		g.setFont(font1);

		tmpDS.paintComponent(g);
//		tmpDS.repaint();
		
		
		try {
			ImageIO.write(bi, "PNG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\C_Batch1_10\\pippo.png"));
			ImageIO.write(bi, "JPG", new File("C:\\Users\\giovanni_perrone.COMMPROVE\\Documents\\Paper\\PaperVRPTW\\output\\C_Batch1_10\\pippo.jpg"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//lstResources = problemSolver.getLstResources();

		/*
		// attempt to launch a batch for all files in the "/batch" directory
		final File filObj = new File("resources/batch");
		PerroUtils.print(filObj.getPath());
		
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("XML", "xml");

		// print list of batch jobs
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) 
					PerroUtils.print(fileInDir.getName());
		}

		// starts the main loop
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) {			
					String strBatchName = "batch/"+ fileInDir.getName();
					PerroUtils.print("Launching batch job " + strBatchName);
					BatchLauncher tmp2 = new BatchLauncher();		
					tmp2.NewBatchJob(strBatchName);
				}
		}
		*/

	}

	public List<Task> getLstTasks() {
		return lstTasks;
	}

	public void setLstTasks(ArrayList<Task> lstTasks) {
		this.lstTasks = lstTasks;
	}

	public List<NodesConnection> getLstConn() {
		return lstConn;
	}

	public void setLstConn(ArrayList<NodesConnection> lstConn) {
		this.lstConn = lstConn;
	}
    
    
}