/**
 * 
 */
package problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jfree.data.statistics.Statistics;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;

import objects.Break;
import objects.Node;
import objects.Resource;
import objects.Task;
import objects.TaskStats;

import parsers.GenerateWorldFromXML;
import utils.*;

/**
 * Generates statistics for later analysis using DM tools - output stats to a file
 * 
 * @author gperr
 *
 */
public class StatsGenerator {
	
	// sets and variables used for modeling
	private List<Task> lstTasks = new ArrayList<Task>();
	private List<Resource> lstResources = new ArrayList<Resource>();
	private List<Task> lstL = new ArrayList<Task>();
	private List<Break> lstBreak = new ArrayList<Break>();
	private HashSet<Node> setNodes = new HashSet<Node>();

	private int numTasks = 0;
	private int numResources = 0;
	
	private int sizeOfL = 0;
	
	// private lists for statistics
	private List<TaskStats> lstTskStats = new ArrayList<TaskStats>();

	
	/**
	 * This constructor is used to initialize the object by calling the parser and copying the lists of objects and resources into local variables
	 * 
	 * @param strDSPath the path of filename of the dataset to be used for initilization (if empty defaults to "resources/")
	 * @param strDSName the filename of the dataset to be used for initilization
	 * 
	 */

	public StatsGenerator(String strDSPath, String strDSName) {

		PerroUtils.print("\nReading the dataset...\n");

		if (strDSPath == "")
			strDSPath = "resources/";
		
		// First of all, launch the parser
		GenerateWorldFromXML tmp = new GenerateWorldFromXML();
		if (tmp.ReadDatasetFile(strDSPath, strDSName))
			PerroUtils.print("Parsing completed ok");
		else {
			PerroUtils.print("** FATAL: Parsing failed");
		}
		
		// Parsing successful: first of all, let's create the resources (vehicles for jsprit)
		lstTasks = tmp.getlstTasks();
		lstResources = tmp.getlstResources();
		numTasks = tmp.getNumTasks();
		numResources = tmp.getNumResources();	
	}

	/**
	 * Generates statistics based on the information stored in the lists (populated when the object has been generated)
	 * 
	 * @param args
	 */
	public void generateStats() {
		
		double absMaxD = Double.MIN_VALUE;
		
		// clear the list of the stats
		lstTskStats.clear();
				
		// first of all, calculate the max absolute distance between two tasks
		for (Task tski : lstTasks){
			for (Task tskj : lstTasks) {
				double distance = dblDistance(tski.getNode(), tskj.getNode()); 
			
				if (absMaxD < distance)
					absMaxD = distance;		// absolute maximum distance (used later for normalization)
			}
		}
		
		// now per each task calculate the values of the various statistics
		for (Task tski : lstTasks) {
			double[] dist = new double[lstTasks.size()];
			
			for (Task tskj : lstTasks) {
				double distance = dblDistance(tski.getNode(), tskj.getNode()); 
						
				dist[lstTasks.indexOf(tskj)] = distance;

			}
			
			// generates a dummy stats object and populate the list for the statistics
			// dummy object for simple statistics calculation
			SimpleStats tmp2 = new SimpleStats(dist);

			// temp stats object
			TaskStats tmp = new TaskStats();

			// first of all, generate spatial carachteristics
			tmp.setTask_id(tski.getId());	
			
			tmp.setAvgDist(tmp2.getMean()/absMaxD);
			tmp.setClosestDist(tmp2.getMinIgnoresZero()/absMaxD);		// use the minimum distance w/o taking in account zero (distance to itself)
			tmp.setFarestDist(tmp2.getMax()/absMaxD);
			tmp.setStvDist(tmp2.getStdDev()/absMaxD);
			tmp.setMdnDist(tmp2.median()/absMaxD);

			// then, proceed with time-based ones
			tmp.setTimWind(tski.getDuration()/(24*60));					// width of the task's time window vs. the 24 hrs period
			// calculates the % of the resources that will be unavailable (even partially) during task's execution window
			double cntResUnav = 0;
			for (Resource res : lstResources) 
				if ( ( tski.getTimeint().getStartTime() >= res.getBreakTime().getStartTime() ) && ( tski.getTimeint().getStartTime() <= res.getBreakTime().getEndTime() ) ) 				
					cntResUnav++;		// if task start time falls between start and end time of the break then increase counter 
			tmp.setPerResAv(cntResUnav/lstResources.size());

			// calculates the % of the resources that possess the skill required to do the job
			double cntResSkilled = 0;
			for (Resource res : lstResources) 
				if ( res.getSkill() >= tski.getCapacity() ) 				
					cntResSkilled++;		// if task start time falls between start and end time of the break then increase counter 
			tmp.setPerResWSkills(cntResSkilled/lstResources.size());
			
			// populates the boolean array with the 24 bins corresponding to the mapping of the task vs the hours of the day
			ArrayList<Boolean> blBins = new ArrayList<Boolean>();	// array list of 24 bins indicating if the time window of the tasks belong to bin #i

			for (int i = 0; i < 24; i++) {
				int iStartHour = i * 60;
				if ( ( tski.getTimeint().getStartTime() <= iStartHour ) && ( tski.getTimeint().getEndTime() >= iStartHour ) )
					blBins.add(Boolean.TRUE);
				else
					blBins.add(Boolean.FALSE);
			}
			tmp.setBlTimeWindowBins(blBins);
			lstTskStats.add(tmp);
//			tmp.setBlTimeWind(blBin);
			
		}
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		TaskStats tmp = lstTskStats.get(0);		// take the first object of the list in order to be sure that all fields are populated
		PerroUtils.print(tmp.getHeaderString());
		strList.add(tmp.getHeaderString());
		
		for (TaskStats tmp1 : lstTskStats) {
			PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV("output/DS_"+lstTskStats.size()+"_stats.csv", strList);
	}
	
	/**
	 * Returns the Euclidean distance between the two nodes passed as parameters
	 * 
	 * @param nd1, nd2 the two nodes to be used to calculate the distance
	 */
	private double dblDistance(Node nd1, Node nd2) {
	    double xcoord = Math.abs (nd1.getLatitude()- nd2.getLatitude());
	    double ycoord = Math.abs (nd1.getLongitude()- nd2.getLongitude());
		return Math.sqrt(ycoord*ycoord + xcoord*xcoord); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Sample code to test the class
		
		StatsGenerator tmp = new StatsGenerator("resources/", "DS_50_5.xml");

		tmp.generateStats();	
		
    }
}
