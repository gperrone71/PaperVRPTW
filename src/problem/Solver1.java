/**
 * 
 */
package problem;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import com.graphhopper.jsprit.core.util.Solutions;

import objects.*;
import parsers.GenerateWorldFromXML;
import utils.NumericUtils;
import utils.PerroUtils;
import utils.SimpleStats;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.netlib.arpack.Dmout;


/**
 * Parses one of the generated datasets, loads the objects and solves it using jsprit toolkit
 * Based on the jsprit MultipleTimeWindowExample(s) files 
 * 
 * @author gperr
 *
 */
/**
 * @author gperr
 *
 */
public class Solver1 {

	// sets and variables used for modeling
	private List<Task> lstTasks = new ArrayList<Task>();
	private List<Resource> lstResources = new ArrayList<Resource>();
	private List<Task> lstL = new ArrayList<Task>();
	private List<Break> lstBreak = new ArrayList<Break>();
	private HashSet<Node> setNodes = new HashSet<Node>();

	private int numTasks = 0;
	private int numResources = 0;
	
	private double dbMaxX = 0;
	private double dbMaxY = 0;
	private double dbTskDens = 0;
	
	private int sizeOfL = 0;

	private String strNameOfDS;
	
    VehicleRoutingProblemSolution solFound;			// solution found by the solver
	private List<Task> lstTskSrvc = new ArrayList<Task>();	// list of tasks serviced by the best solution found by the solver
	
	// private lists for statistics
	private List<TaskStats> lstTskStats = new ArrayList<TaskStats>();

    
    
	/**
	 * This constructor is used to initialize the solver by calling the parser and copying the lists of objects and resources into local variables
	 * 
	 * @param strDSPath the path of the dataset file to be used for initilization. If empty defaults to "resources/"
	 * @param strDSName the filename of the dataset to be used for initilization
	 * 
	 */
	
	public Solver1(String strDSPath, String strDSName) {

		if (strDSPath == "")
			strDSPath = "resources/";
		
		// First of all, launch the parser
		GenerateWorldFromXML tmp = new GenerateWorldFromXML();
		if (tmp.ReadDatasetFile(strDSPath, strDSName)) {
			PerroUtils.print("Parsing completed ok", true);
			strNameOfDS = strDSName;
		}
		else {
			PerroUtils.print("** FATAL: Parsing of file " + strDSPath + strDSName + " failed");	
			System.exit(1);			// exit with an error code
		}
		
		// Parsing successful: first of all, let's create the resources (vehicles for jsprit)
		lstTasks = tmp.getlstTasks();
		lstResources = tmp.getlstResources();
		numTasks = tmp.getNumTasks();
		numResources = tmp.getNumResources();	
	}


	/**
	 * Solves the loaded dataset using the jsprit toolkit. 
	 * Requires the method initSolver to be called first in order to choose the selected dataset to be solved
	 * 
	 * @author gperr
	 * 
	 * @return SolStats an object of SolStats type that includes stats data for this execution of the solver
	 * @param bVerbose specify if solver has to print extended data on console or not
	 * @param bResReturnToStart	specify is resources need to return to the starting point or go to the destination point
	 * @param strFullPath specifies the full path (including final "/") where outputs have to be stored
	 */
	
	public SolStats launchSolver(boolean bVerbose, boolean bResReturnToStart, int iNumThreadsToUse, String strFullPath) {

		PerroUtils.print("Initializing the solver...", true);
		
		// creates a new instance of a VRP builder
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

		// instructs the builder that the fleet size is finite
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        
        // add resources (in jsprit called vehicles)       
		for (Resource res : lstResources) {
			
			/*
	         * get a vehicle type-builder and build a type with the typeId equals to the resource Id and one capacity dimension
			 */
	        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance(res.getDescription())
	        	.addCapacityDimension(0, 100)
//	            .addCapacityDimension(0, res.getAvailability().getDuration())		// use weight as limit and sets the weight based on the availability time window duration
//	            .setCostPerWaitingTime(0.8)			don't want to consider waiting time for now
	            ;
	        VehicleType vehicleType = vehicleTypeBuilder.build();

	        // create a vehiclebuilder for the resource being treated
	        Builder vehicleBuilder = Builder.newInstance(res.getDescription());
	        
	        // sets start and end location based on the origin and destinations and makes sure that the vehicle goes to the destination point
	        vehicleBuilder
	        	.setStartLocation(Location.newInstance(res.getOrigin().getLatitude(), res.getOrigin().getLongitude()));

	        if (bResReturnToStart)		// if resources have to return to the starting point then End Location = Origin
	        	vehicleBuilder.setEndLocation(Location.newInstance(res.getOrigin().getLatitude(), res.getOrigin().getLongitude()));
	        else						// otherwise End Location = Destination
	        	vehicleBuilder.setEndLocation(Location.newInstance(res.getDestination().getLatitude(), res.getDestination().getLongitude()));

	        vehicleBuilder
	        	.setReturnToDepot(true);			// activate return to the EndPoint  

	        vehicleBuilder.setType(vehicleType);
	        
	        // sets start and end availability time windows
	        vehicleBuilder
	        	.setEarliestStart(res.getAvailability().getStartTime())
	        	.setLatestArrival(res.getAvailability().getEndTime());
	        
	        // build the vehicle
	        VehicleImpl vehicle = vehicleBuilder.build();

	        if (bVerbose)
	        	PerroUtils.print("Created vehicle " + vehicle.getId(), true);
	        
	        // add the created vehicle to the builder
	        vrpBuilder.addVehicle(vehicle);
		}

        // now adds tasks (services for jsprit) to the problem
        for (Task tsk : lstTasks) {

        	// generates a service based on the information retrieved from the dataset
            Service service = Service.Builder.newInstance(tsk.getDescription())		// use task description as instance id
                    .addTimeWindow(tsk.getTimeint().getStartTime(), tsk.getTimeint().getEndTime())	
                    .addSizeDimension(0, 1)			// weight is set to 1
                    .setServiceTime(tsk.getServiceTime())					
                    .setLocation(Location.newInstance(tsk.getNode().getLatitude(), tsk.getNode().getLongitude()))	// sets position
                    .build();
                vrpBuilder.addJob(service);
        }

        PerroUtils.print("Added "+ vrpBuilder.getAddedJobs().size() + " #Tasks and " + vrpBuilder.getAddedVehicles().size() + " #Resources", true);
        PerroUtils.print("Launching the solver with " + iNumThreadsToUse + " thread(s)...", true);
        
        // generate a new SolStats object to handle the results
        SolStats solStats = new SolStats();
        solStats.setNumResources(numResources);
        solStats.setNumTasks(numTasks);
        
        // setup the timer
        double startTime = System.currentTimeMillis();
        
        // sets the number of threads and generates the problem
        final VehicleRoutingProblem problem = vrpBuilder.build();

        // sets the number of active threads and sets the algorithm using the default settings generated by the solver
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem).setProperty(Jsprit.Parameter.THREADS, String.valueOf(iNumThreadsToUse)).buildAlgorithm();
        solStats.setiNumThreads(iNumThreadsToUse);

        // and search a solution
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        // update the execution time and the number of executions in the solution stats object
        double dbExecTimeInSec = (System.currentTimeMillis() - startTime) / 1000; 
        solStats.setDblExecutionTime( dbExecTimeInSec );
        solStats.setNumSolutionsFound(solutions.size());

        // get the best solution
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        // and stores it in the data member
        solFound = bestSolution;

        if (bVerbose)
        	SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        // scans the solution to find which tasks have been serviced and populate the relevant list
        for (VehicleRoute vhcRt : bestSolution.getRoutes() )
        	for (TourActivity act : vhcRt.getActivities()) {
//        		PerroUtils.print("LOC " + act.getLocation().getId() + " Name " + act.getName() + " I:" + act.getIndex());

        		// the task index obtained from jsprit is the task id + 1 (because they are entered in sequence)
        		for (Task tsk : lstTasks)
//        			if ( (tsk.getId() +1 ) == act.getIndex() ) {
        			if ( (lstTasks.indexOf(tsk) +1 ) == act.getIndex() ) {
        				lstTskSrvc.add(tsk);
        				break;
        			}
        	}      	
        PerroUtils.print("*** Solver completed execution with " + lstTskSrvc.size() + " tasks serviced in " + dbExecTimeInSec +" seconds", true);

        // add the total number of tasks serviced to the list
		solStats.setiTotServiced(lstTskSrvc.size());
		solStats.setiTotUnserviced(bestSolution.getUnassignedJobs().size());

		/*
         * plot
		 */
        new Plotter(problem,bestSolution).setLabel(Plotter.Label.ID).plot(strFullPath + "plot" + strNameOfDS, strNameOfDS);

        SolutionAnalyser a = new SolutionAnalyser(problem, bestSolution, new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return problem.getTransportCosts().getTransportTime(from,to,0.,null,null);
            }
        });

        PerroUtils.print(" \n");

        PerroUtils.print("++++++   TIME WINDOW VIOLATION : " + a.getTimeWindowViolation());
        PerroUtils.print("++++++   Number of pickups	: " + a.getNumberOfPickups());
        PerroUtils.print("++++++   Number of jobs unass  : " + bestSolution.getUnassignedJobs().size());      
        PerroUtils.print("++++++   Number of vehicles    : " + bestSolution.getRoutes().size());
        PerroUtils.print("++++++   Operation Time        : " + a.getOperationTime());
        PerroUtils.print("++++++   Service time          : " + a.getServiceTime());
        PerroUtils.print("++++++   Transportation Time   : " + a.getTransportTime());
        PerroUtils.print("++++++   Waiting Time          : " + a.getWaitingTime());

        PerroUtils.print(" \n");

        // update solution stats
        solStats.setDbTotalCosts(a.getTotalCosts());
        solStats.setDbTraveledDistance(a.getDistance());;

        solStats.setDbTimeWinViolation(a.getTimeWindowViolation());
        solStats.setDbOperationTime(a.getOperationTime());
        solStats.setDbTransportTime(a.getTransportTime());
        solStats.setDbServiceTime(a.getServiceTime());
        solStats.setDbWaitingTime(a.getWaitingTime());

        solStats.setiNumVehiclesUsed(bestSolution.getRoutes().size());

        
        if (bVerbose) {
	        PerroUtils.print(" \n");

	        PerroUtils.print("++++++   TIME WINDOW VIOLATION : " + a.getTimeWindowViolation());
	        PerroUtils.print("++++++   Number of pickups	 : " + a.getNumberOfPickups());
	        PerroUtils.print("++++++   Number of jobs unass  : " + bestSolution.getUnassignedJobs().size());      
	        PerroUtils.print("++++++   Number of vehicles    : " + bestSolution.getRoutes().size());
	        PerroUtils.print("++++++   Operation Time        : " + a.getOperationTime());
	        PerroUtils.print("++++++   Service time          : " + a.getServiceTime());
	        PerroUtils.print("++++++   Transportation Time   : " + a.getTransportTime());
	        PerroUtils.print("++++++   Waiting Time          : " + a.getWaitingTime());

	        PerroUtils.print(" \n");

        }

//        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();

        return solStats;       
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
			tmp.setTimWind( ( (double) tski.getTimeint().getDuration()) / (24*60) );					// width of the task's time window vs. the 24 hrs period
			tmp.setSrvTime( ( (double) tski.getServiceTime() )/ tski.getTimeint().getDuration());	// sets the service time normalized vs the time window width
			
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

			// sets the "has been serviced flag" only if a solution has been found
			if (solFound != null) 
				tmp.setBlServiced(lstTskSrvc.contains(tski) ? 1: 0);
			
			// and finally add the line to the stats array
			lstTskStats.add(tmp);
			

		}
		
	}

	
	/**
	 * Generates a CSV file and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 */
	public void generateCSV(boolean prtOnScreen) {
		
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		TaskStats tmp = lstTskStats.get(0);		// take the first object of the list in order to be sure that all fields are populated

		strList.add(tmp.getHeaderString());
		
		for (TaskStats tmp1 : lstTskStats) {
			if (prtOnScreen) 
				PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV("output/DS_"+lstTskStats.size()+"_stats.csv", strList);
	}
	
	
	/**
	 * Generates an ARFF file and optionally prints on console its contents
	 * 
	 *  @param strFullPath String containing the full path where the .arff has to be written to
	 *  @param boolean prtOnScreen specifies if the ARFF output has to be printed on console or not
	 *  @param bGenerateAsTestSet specifies if the ARFF output has to include or not values for the last attribute (which is the class). If not, .arff is saved w/ "_TS" suffix 
	 */
	public void generateARFF(String strFullPath, boolean prtOnScreen, boolean bGenerateAsTestSet) {
		
		List<String> strList = new ArrayList<String>();
		
		// Header
		strList.add("%\n% ARFF file generated automagically\n% Dataset name :" + strNameOfDS+"\n%");
		strList.add("@RELATION "+strNameOfDS+ "\n");
		
		// Using reflections generate the ATTRIBUTE section
		// one row is generated per each attribute and the attribute type is always NUMERIC
		
		// get list of fields as an ArrayList and populate accordingly the list that will generate the ARFF file
		TaskStats tmp = lstTskStats.get(0);
		for (String str : tmp.getFieldsNamesAsArray())
			if (str != "blServiced")
				strList.add("@ATTRIBUTE "+str+" NUMERIC");
			else
				strList.add("@ATTRIBUTE "+str+" {0, 1}");
			
		// @DATA Section
		// output is generated parsing the list containing the tasks stats
		strList.add("\n@DATA");
		
		for (TaskStats tskS : lstTskStats) {
			String str = "";
			
			for (String strData : tskS.getDataMembersAsArray(true, bGenerateAsTestSet))
				str += strData + ",";
			strList.add(str.substring(0, str.length()-1));				// remove the last ","
		}
				
		// if print on console activated then prints contents of the arraylist
		for (String strConsole : strList)
			if (prtOnScreen)
				PerroUtils.print(strConsole);
		
		String strFileName = strFullPath + strNameOfDS.substring(0, strNameOfDS.indexOf(".xml")) + ( (bGenerateAsTestSet) ? "_TS" : "" ) + ".arff";
		PerroUtils.writeCSV(strFileName, strList);
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
	 * Calculates the maximum values for maxX, maxY and the value for the tasks density
	 * 
	 */

	public void calcMaxAndDensity() {
		double dbTmpX = 0;
		double dbTmpY = 0;
		
		double tmpX = 0;
		double tmpY = 0;
		
		for (Task tsk : lstTasks) {
			tmpX = tsk.getNode().getLatitude();
			tmpY = tsk.getNode().getLongitude();

			if ( tmpX > dbTmpX)
				dbTmpX = tmpX;

			if ( tmpY > dbTmpY)
				dbTmpY = tmpY;

		}
		
		dbMaxX = dbTmpX;
		dbMaxY = dbTmpY;
		dbTskDens = (double) lstTasks.size() / (dbTmpX*dbTmpY);
				
		return;
	}
	
	/**
	 * @return the lstTasks
	 */
	public List<Task> getLstTasks() {
		return lstTasks;
	}


	/**
	 * @param lstTasks the lstTasks to set
	 */
	public void setLstTasks(List<Task> lstTasks) {
		this.lstTasks = lstTasks;
	}


	/**
	 * @return the lstResources
	 */
	public List<Resource> getLstResources() {
		return lstResources;
	}


	/**
	 * @param lstResources the lstResources to set
	 */
	public void setLstResources(List<Resource> lstResources) {
		this.lstResources = lstResources;
	}


	/**
	 * @return the lstL
	 */
	public List<Task> getLstL() {
		return lstL;
	}


	/**
	 * @param lstL the lstL to set
	 */
	public void setLstL(List<Task> lstL) {
		this.lstL = lstL;
	}


	/**
	 * @return the lstBreak
	 */
	public List<Break> getLstBreak() {
		return lstBreak;
	}


	/**
	 * @param lstBreak the lstBreak to set
	 */
	public void setLstBreak(List<Break> lstBreak) {
		this.lstBreak = lstBreak;
	}


	/**
	 * @return the setNodes
	 */
	public HashSet<Node> getSetNodes() {
		return setNodes;
	}


	/**
	 * @param setNodes the setNodes to set
	 */
	public void setSetNodes(HashSet<Node> setNodes) {
		this.setNodes = setNodes;
	}


	/**
	 * @return the numTasks
	 */
	public int getNumTasks() {
		return numTasks;
	}


	/**
	 * @param numTasks the numTasks to set
	 */
	public void setNumTasks(int numTasks) {
		this.numTasks = numTasks;
	}


	/**
	 * @return the numResources
	 */
	public int getNumResources() {
		return numResources;
	}


	/**
	 * @param numResources the numResources to set
	 */
	public void setNumResources(int numResources) {
		this.numResources = numResources;
	}


	/**
	 * @return the sizeOfL
	 */
	public int getSizeOfL() {
		return sizeOfL;
	}


	/**
	 * @param sizeOfL the sizeOfL to set
	 */
	public void setSizeOfL(int sizeOfL) {
		this.sizeOfL = sizeOfL;
	}


	/**
	 * @return the lstTskSrvc
	 */
	public List<Task> getLstTskSrvc() {
		return lstTskSrvc;
	}


	/**
	 * @param lstTskSrvc the lstTskSrvc to set
	 */
	public void setLstTskSrvc(List<Task> lstTskSrvc) {
		this.lstTskSrvc = lstTskSrvc;
	}

	/**
	 * @return the strNameOfDS
	 */
	public String getStrNameOfDS() {
		return strNameOfDS;
	}


	/**
	 * @param strNameOfDS the strNameOfDS to set
	 */
	public void setStrNameOfDS(String strNameOfDS) {
		this.strNameOfDS = strNameOfDS;
	}


	/**
	 * @return the solFound
	 */
	public VehicleRoutingProblemSolution getSolFound() {
		return solFound;
	}


	/**
	 * @param solFound the solFound to set
	 */
	public void setSolFound(VehicleRoutingProblemSolution solFound) {
		this.solFound = solFound;
	}


	/**
	 * @return the lstTskStats
	 */
	public List<TaskStats> getLstTskStats() {
		return lstTskStats;
	}


	/**
	 * @param lstTskStats the lstTskStats to set
	 */
	public void setLstTskStats(List<TaskStats> lstTskStats) {
		this.lstTskStats = lstTskStats;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Solver1 tmp = new Solver1("", "DS_50_5.xml");
		
		tmp.launchSolver(true, true, 6, "output/");
		
		tmp.generateStats();
		
		tmp.generateCSV(false);
		
		tmp.generateARFF("output/", true, false);
			
		
    }


	public double getDbMaxX() {
		return dbMaxX;
	}


	public double getDbMaxY() {
		return dbMaxY;
	}


	public double getDbTskDens() {
		return dbTskDens;
	}


}
