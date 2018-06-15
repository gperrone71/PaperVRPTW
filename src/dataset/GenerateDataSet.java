/**
 * 
 */
package dataset;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.nio.charset.StandardCharsets;

import objects.*;
import utils.*;


/**
 * This class generates a dataset and writes it on file
 * 
 * @author Giovanni
 *
 */
public class GenerateDataSet {

	private String strDataSetFileName;
	private String strDataSetPath;
	private String strDSFileNamePrefix = "";
	
	private ArrayList<Task> listTasks ;
	private ArrayList<Resource> listResources ;

	// data members used by DSPLotter
	private ArrayList<ClusteredTasks> lstClusteredTasks = new ArrayList<ClusteredTasks>();
	private ArrayList<Task> lstRandomTasks = new ArrayList<Task>();

	
	/**
	 * Generates an .xml file containing the world to be simulated using the parameters passed as arguments
	 * 
	 * @param configParam	object of the type BatchConfig that includes parameters for DS generation
	 * @param strPath		optional string that can be used to specify the path (including final "/") of the dataset file to be generated (if empty defaults to "resources\")
	 * @param strFileSuff	optional string that specifies a suffix to be appended to the dataset file name
	 */
	public void GenerateDS(String strPath, String strFileSuff, BatchConfig configParam) {
		
		// for convenience, values of the configuration parameters are copied in local variables	
		int nTasks = configParam.getnTasks();
		int nRes = configParam.getnResources();
		boolean bFullResourcesAvailability = configParam.isbFullResourcesAvailability();
		boolean bSameDestination = configParam.isbResReturnToStart();
		boolean bExtendedTimeWin = configParam.isbExtendedTimeWin();
		char cResStartingPositioning = configParam.getcResourcesStartingPosition();
		double dResStartingPosSpread = configParam.getdStartingPositionsSpread();
		boolean bAllResourcesHaveSameStartingPoint = configParam.isbAllResourcesHaveSamePos();
					
		int maxX = configParam.getMaxX();
		int maxY = configParam.getMaxY();

		// generates lists and other variables
		listTasks = new ArrayList<Task>(nTasks);
		listResources = new ArrayList<Resource>(nRes);
		int numNodes = 0;
		
		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);

		PerroUtils.print("Generating dataset for " + nTasks + " tasks and " + nRes + " resources.", true);

		// preliminary activity: based on the type of positioning required, calculates in advance the center of the resources starting point
		// these activities are carried out here in order to have available the resources starting position then we generate the tasks clusters 
		double dResCentralPosX = 0;
		double dResCentralPosY = 0;
		
		// calculates the center of gravity for the tasks
		double dCoGX = 0;
		double dCoGY = 0;
		for (Task tsk : listTasks) {
			dCoGX += tsk.getNode().getLatitude();
			dCoGY += tsk.getNode().getLongitude();
		}
		dCoGX = (dCoGX / listTasks.size());
		dCoGY = (dCoGY / listTasks.size());
		
		Node resStartingPos = new Node();
		
		switch (cResStartingPositioning) {
			case 'C': {						// resources start from center
				dResCentralPosX = (maxX / 2);
				dResCentralPosY = (maxY / 2);
				break;
			}
			case 'N': {						// resources start from North
				dResCentralPosX = (maxX / 2);
				dResCentralPosY = (maxY * 0.75);
				if (dResStartingPosSpread > (maxY * 0.25) ) 
					dResStartingPosSpread = (maxY * 0.2); 
				break;
			}

			case 'S': {						// resources start from South
				dResCentralPosX = (maxX / 2);
				dResCentralPosY = (maxY * 0.25);
				if (dResStartingPosSpread > (maxY * 0.25) ) 
					dResStartingPosSpread = (maxY * 0.2); 
				break;
			}

			case 'E': {						// resources start from East
				dResCentralPosX = (maxX * 0.75);
				dResCentralPosY = (maxY / 2);
				if (dResStartingPosSpread > (maxX * 0.25) ) 
					dResStartingPosSpread = (maxX * 0.2); 
				break;
			}

			case 'W': {						// resources start from West
				dResCentralPosX = (maxX * 0.25);
				dResCentralPosY = (maxY / 2);
				if (dResStartingPosSpread > (maxX * 0.25) ) 
					dResStartingPosSpread = (maxX * 0.2); 
				break;
			}

			case 'G': {						// resources start from tasks' center of gravity
				dResCentralPosX = dCoGX;
				dResCentralPosY = dCoGY;

				// checks on the radius for the spreading
				if ((dResCentralPosX + dResStartingPosSpread) > maxX )
					dResStartingPosSpread = maxX - dResCentralPosX; 
										
				if ((dResCentralPosX - dResStartingPosSpread) < 0 )
					dResStartingPosSpread = dResCentralPosX;
					
				if ((dResCentralPosY + dResStartingPosSpread) > maxY )
					dResStartingPosSpread = maxY - dResCentralPosY; 
										
				if ((dResCentralPosY - dResStartingPosSpread) < 0 )
					dResStartingPosSpread = dResCentralPosY;

				break;
			}
			
			default: {						// random starting position - activated if the setting is set to 'R' or to any other char not recognized
				cResStartingPositioning = 'R';	// sets to 'R' to ensure consistency with the code below
				Node pos = new Node();
				pos.generateRndPosition(maxX, maxY);
				dResCentralPosX = pos.getLatitude();
				dResCentralPosY = pos.getLongitude();
				break;
			}

		}
		// init the Node object used to store resources central starting position when generating clusters
		resStartingPos.setLatitude(dResCentralPosX);
		resStartingPos.setLongitude(dResCentralPosY);
		
		// tasks generation
		switch (configParam.getcTaskDistribution()) {

			// cluster based generation
			case 'C': {
				listTasks = GenerateClusterTasksList(nTasks, 0, configParam, resStartingPos);	
				break;
			}
			// mixed configuration
			case 'R': {
				int iNumClusteredTasks = (int) (nTasks*(configParam.getdRDSClusteredTasksRatio()));
				listTasks = GenerateClusterTasksList(iNumClusteredTasks, 0, configParam, resStartingPos);
				listTasks.addAll(GenerateRandomTasksList(nTasks-listTasks.size(), listTasks.size(), configParam));
				
				String tmp = "R" + strDSFileNamePrefix;
				strDSFileNamePrefix = tmp;
				
				break;
			}
			default: {				// default value - switch here if no value is selected and generates a completely uniform rnd distribution
				configParam.setcTaskDistribution('U');
				listTasks = GenerateRandomTasksList(nTasks, 0, configParam);
				strDSFileNamePrefix = "RU";
				break;
			}
		}
		numNodes = listTasks.size()-1;

		// Resources generations
		Random rnd = new Random();
		Random numRnd = new Random();

		for (int i = 0; i < nRes ; i++) {
			Resource rsc = new Resource();
			
			// NOTE: resources ID start from 1 since 0 is reserved for resId field in task nodes
			rsc.setId(i+1);
			rsc.setDescription("Technician"+ (i+1) );
			rsc.setRegion(1);
			
			// generate random time interval for availability based on the flag
			TimeInterval timint = new TimeInterval();
			if (bFullResourcesAvailability) {
				int Start = 0;
				timint.setStartTime(Start);
				timint.setEndTime(Start+24*60);			// sets 24 hours of availability 
			} else  
				timint.generateRandomTimeInterval( configParam.getiResMinStartingTime(), 0, configParam.getiResMinAvailabilityTime() );							
			rsc.setAvailability(timint);
						
			// generate random time interval for unavailability period
			
			TimeInterval timint1 = new TimeInterval();
			timint1.generateRandomTimeInterval(rsc.getAvailability().getStartTime()+4*60, 0 , (int) (timint.getDuration() * 0.112) );		// break cannot be == 0 nor > 11,2% of the availability window
			rsc.setBreakTime(timint1);	

			// generate random position for the Origin
			Node pos = new Node();
			pos.setId(++numNodes);    	// the node ID for the origin has to continue from the node ID generated for the tasks
			
			if (bAllResourcesHaveSameStartingPoint) {
				// if all resources have same starting position then set starting position to the coordinates defined before
				pos.setLatitude(dResCentralPosX);
				pos.setLongitude(dResCentralPosY);
			} else if (cResStartingPositioning == 'R') {
				// if resources coordinated have to be defined randomically then do so
				pos.generateRndPosition(maxX, maxX);
				
			} else {
				// need to generate a spreading factor around the starting position
				pos.setLatitude(dResCentralPosX + (numRnd.nextDouble() - 0.5)*2*dResStartingPosSpread);
				pos.setLongitude(dResCentralPosY + (numRnd.nextDouble() - 0.5)*2*dResStartingPosSpread);
			}
			
			rsc.setOrigin(pos);
			
			// generate random position for the Destination based on the flag
			Node pos1 = new Node();
			if (bSameDestination) 
				pos1 = rsc.getOrigin();
			else 
				pos1.generateRndPosition(maxX, maxY);
			pos1.setId(++numNodes);  			// the node ID for the destination
			rsc.setDestination(pos1);
			
			// generate a random skill
			int iSkill = rnd.nextInt(10);
			rsc.setSkill(iSkill);
			
			// and copy the resource
			listResources.add(rsc);
		}

		// writes the generated dataset on an XML file
		WriteDataSetOnFile(strDSFileNamePrefix, listTasks, listResources, strPath, strFileSuff);
	
		// writes tasks definition on disk
		writeTasksDefinitionOnDisk(lstRandomTasks, lstClusteredTasks);
		
		PerroUtils.print("Generation completed successfully (" + numNodes +" nodes generated).", true);
	}

	/**
	 * Dumps on a folder on disk the definitions of the tasks in order to be retrieved later by the DSPlotter if needed
	 *  
	 * @param lstRandomTasks2			List of the tasks randomly generated 
	 * @param lstClusteredTasks2		List of the clusters
	 */
	private void writeTasksDefinitionOnDisk(ArrayList<Task> lstRandomTasks2, ArrayList<ClusteredTasks> lstClusteredTasks2) {

		String strTasksDefPath = strDataSetPath + FolderDefs.tasksFolderName;
		if (!PerroUtils.prepareFolder(strTasksDefPath, false))
			return;

		String strClusterFileName = strTasksDefPath + FilePrefixes.clusterDefinitionPrefix + PerroUtils.returnFullFileNameWOExtension(strDataSetFileName)+ ".xml";
		String strRndTaskFileName = strTasksDefPath + FilePrefixes.rndTaskDefinitionPrefix + PerroUtils.returnFullFileNameWOExtension(strDataSetFileName)+ ".xml";
		
		XStream xstream = new XStream();
		
		String strXML = "";
		
		strXML = xstream.toXML(lstRandomTasks2);
		
        List<String> tmp = new ArrayList<String>();
        tmp.add(strXML);
		
		try {
			Files.write(Paths.get(strRndTaskFileName), tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		strXML = xstream.toXML(lstClusteredTasks2);
		tmp.clear();
        tmp.add(strXML);
		
		try {
			Files.write(Paths.get(strClusterFileName), tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * generates a list of tasks based on uniform random distribution
	 * 
	 * @param nTasks	number of tasks to be created
	 * @param nStartingNodeID	ID of the first node that will be generated by the method
	 * @param configParam	BatchConfig object with the configuration parameters of the batch object being processed
	 * @return lstTasks	ArrayList of Task objects
	 */
	private ArrayList<Task> GenerateRandomTasksList(int nTasks, int nStartingNodeID, BatchConfig configParam) {

		ArrayList<Task> listTasks = new ArrayList<Task>(nTasks);
		
		PerroUtils.print("++ Generating " + nTasks + " randomly distributed tasks", true);
		
		// initialize rnd generator
		Random rnd = new Random();		
		
		// generate collections for the data to be created
		int iNumTsk = nStartingNodeID;

		// Tasks list
		for (int i = 0; i < nTasks; i++) {
			Task tsk = new Task();
			
			Random rndNum = new Random();
			
			// set ID
			tsk.setId(iNumTsk);
			
			// set type to T
			tsk.setType("T");
			
			// set Description
			tsk.setDescription("Task#"+iNumTsk);
			
			// set type id to 1
			tsk.setType_id(1);
			
			// set resId to 0;
			tsk.setResId(0);
		
			// set Priority to a random number (1 - 5)
			tsk.setPriority(rndNum.nextInt(4)+1);
			
			// generate random capacity required
			tsk.setCapacity(rnd.nextInt(10));

			// generate random position
			Node pos = new Node();
			pos.setId(i);
			pos.generateRndPosition(configParam.getMaxX(), configParam.getMaxY());
			tsk.setNode(pos);
			
			// set region to 1
			tsk.setRegion(1);
			
			// generate random time interval for availability
			TimeInterval timint = new TimeInterval();
			timint.generateRandomTimeInterval(configParam.getiTskMinStartingTime(), configParam.getiTskServiceTimeSpread(), configParam.getiTskMinTimeWindowWidth());
			tsk.setTimeint(timint);

			Random numRnd = new Random();
			tsk.setServiceTime(configParam.getiTskMinServiceTimeDuration() + numRnd.nextInt(configParam.getiTskMaxServiceTimeDuration()-configParam.getiTskMinServiceTimeDuration()) );		

			// skills currently not implemented

			listTasks.add(tsk);
			iNumTsk++;
		}
		
		lstRandomTasks = listTasks;
		return listTasks;
		
	}
	
	/**
	 * generates a list of tasks based on clustered distribution
	 * number of clusters is either specified in the configparam passed as argument or assumed by default to be equal to nTask / 50
	 * number of tasks per clusters can be either defined (using a flag in the configparam) to be uniform or derived from a gaussian distribution
	 * 
	 * Updated 11/06/18 -> changes in the way the number of tasks/cluster is generated and added compatibility with the new class required by DSPlotter
	 *   
	 * @param nTasks	number of tasks to be created
	 * @param nStartingNodeID	ID of the first node that will be generated by the method
	 * @param configParam	BatchConfig object with the configuration parameters of the batch object being processed
	 * @param resStartingPosition	Node object storing the coordinates of the resources starting position
	 * @return lstTasks	ArrayList of Task objects
	 */
	private ArrayList<Task> GenerateClusterTasksList(int nTasks, int nStartingNodeID, BatchConfig configParam, Node resStartingPosition) {

		ArrayList<Task> listTasks = new ArrayList<Task>(nTasks);
		
		int iNumClusters;
		
		if (configParam.getiNumClusters() != 0)
			iNumClusters = configParam.getiNumClusters();
		else {
			iNumClusters = nTasks / 50;
			if (iNumClusters == 0)
				iNumClusters = 1;
		}
				
		int iNumNodesPerCluster = 0;
		int iNumTasksCluster[] = new int[iNumClusters];
		int iTasksSum = 0;
		
		if ((configParam.getcClusterType() == 0) || (configParam.getcClusterType() == 'U')) { 
			iNumNodesPerCluster = nTasks / iNumClusters;				// num of nodes per cluster is fixed only for U type of clustered ds
			PerroUtils.print("++ Generating " + nTasks + " tasks clustered on " + iNumClusters + " clusters (" + iNumNodesPerCluster + " nodes per cluster)", true);
			strDSFileNamePrefix = "CU";
			for (int i = 0; i < iNumClusters; i++) {
				iNumTasksCluster[i] = iNumNodesPerCluster;
				iTasksSum += iNumTasksCluster[i];
			}
			if (iTasksSum < nTasks)
				iNumTasksCluster[iNumClusters - 1] += (nTasks - iTasksSum);		// if I am still missing nodes then add them to the last cluster
		}	
		else {
			PerroUtils.print("++ Generating " + nTasks + " tasks clustered on " + iNumClusters + " clusters with gaussian distribution of nodes", true);
			strDSFileNamePrefix = "CG";
			
			// generation of the number of tasks per cluster
			// initialize rnd generators
			Random rndGauss = new Random();		// used only for Gaussian type clusters
			Random rnd = new Random();		
			
			do {
				for (int i = 0; i < iNumClusters; i++) {
					if (iNumTasksCluster[i] == 0)
							if ( (i + 1) == iNumClusters) { 			// if I am creating the last cluster then adjust the number of nodes 
								iNumTasksCluster[i] = nTasks - iTasksSum;		// then the number of tasks for the last cluster is given by what is missing to reach the total number of tasks
							if (iNumTasksCluster[i] < 0) 
								iNumTasksCluster[i] = 0;			// if I am exceeding the maximum number of tasks then reset value for last cluster
							iTasksSum += iNumTasksCluster[i];		// and update the total number of tasks
						}
						else { 									//  otherwise generate the number of tasks based on a gaussian generation
							do 
								iNumTasksCluster[i] = (int) (rndGauss.nextGaussian()*(nTasks/(2*iNumClusters)) + (nTasks/iNumClusters));		// gaussian distr w/ avg = n/c and sigma = n/2*c
							while (iNumTasksCluster[i] <= 0);				// discard all values of the normal distribution that are < 0
							iTasksSum += iNumTasksCluster[i];
						}
				}

				while (iTasksSum > nTasks) {					// if the total number of tasks > nTasks then cancel one of the clusters choosen randomly
					int tmpRnd = rnd.nextInt(iNumClusters);
					iTasksSum -= iNumTasksCluster[tmpRnd];
					iNumTasksCluster[tmpRnd] = 0;
				} 
				
			} while (iTasksSum != nTasks);		// repeat until the sum is correct
			
		}
		
		strDSFileNamePrefix += iNumClusters;
			
		int iNumTsk = nStartingNodeID;
		Random rnd = new Random();		
		
		// main loop (one iteration per each cluster)
		for (int i = 0; i < iNumClusters; i++) {

			ClusteredTasks tmpCluster = new ClusteredTasks();
			ArrayList<Task> tmpClusterTskList = new ArrayList<Task>();
			
			// generate the center of the cluster
			Node clusterPos = new Node();
			clusterPos.generateRndPosition(configParam.getMaxX(), configParam.getMaxY());
			tmpCluster.setNdClusterCenter(clusterPos);
					
			PerroUtils.print(" +--- Cluster " + i + " has " + iNumTasksCluster[i] + " nodes.", true);
			
			// generate collections for the data to be created
			// Tasks list
			for (int j = 0; j < iNumTasksCluster[i]; j++) {
							
				Task tsk = new Task();
				
				Random rndNum = new Random();
				
				// set ID
				tsk.setId(iNumTsk);			// note: task ID must be univocal and therefore is assigned from iNumTsk (not j)
				
				// set type to T
				tsk.setType("T");
				
				// set Description
				tsk.setDescription("Task#"+iNumTsk);
				
				// set type id to 1
				tsk.setType_id(1);
				
				// set resId to 0;
				tsk.setResId(0);
			
				// set Priority to a random number (1 - 5)
				tsk.setPriority(rndNum.nextInt(4)+1);
				
				// generate random capacity required
				tsk.setCapacity(rnd.nextInt(10));
	
				// generate random position
				// dist holds the distance calculated as exp distribution
				double dist = Math.log(1-rnd.nextDouble())/(-configParam.getdExpFactor());
				double radius = rnd.nextDouble()*2*Math.PI;
				double lat = clusterPos.getLatitude() + (dist * Math.cos(radius));
				if (lat < 0) 
					lat = 0;
				if (lat > configParam.getMaxX())
					lat = configParam.getMaxX();

				double lon = clusterPos.getLongitude() + (dist * Math.sin(radius));
				if (lon < 0) 
					lon = 0;
				if (lon > configParam.getMaxY())
					lon = configParam.getMaxY();

				Node pos = new Node();
				pos.setId(iNumTsk);
				pos.setLatitude(lat);
				pos.setLongitude(lon);
				
				tsk.setNode(pos);
				
				// set region to 1
				tsk.setRegion(1);
				
				// generate random time interval for availability
				TimeInterval timint = new TimeInterval();
				// 07/06/2018 : modified time interval generation in such a way that the start window time is always equal to the minimum arrival time of a resource to the cluster center
				//original version -> timint.generateRandomTimeInterval(configParam.getiTskMinStartingTime(), configParam.getiTskServiceTimeSpread(), configParam.getiTskMinTimeWindowWidth());
				
				timint.generateRandomTimeInterval(configParam.getiResMinStartingTime() + (int) pos.getDistanceFromNode(resStartingPosition), configParam.getiTskServiceTimeSpread(), configParam.getiTskMinTimeWindowWidth());
				tsk.setTimeint(timint);
	
				Random numRnd = new Random();
				tsk.setServiceTime(configParam.getiTskMinServiceTimeDuration() + numRnd.nextInt(configParam.getiTskMaxServiceTimeDuration()-configParam.getiTskMinServiceTimeDuration()) );		
	
				// skills currently not implemented
	
				listTasks.add(tsk);

				tmpClusterTskList.add(tsk);
				
				iNumTsk++;
			}
			
			// add the list just created to the list of clustered tasks
			tmpCluster.setLstTasksInCluster(tmpClusterTskList);
			
			// calculate the radius
			int radius = 0;
			int curRadius = 0;
			for (Task tsk : tmpClusterTskList) {
				curRadius = (int) tsk.getNode().getDistanceFromNode(clusterPos);
				radius = ( radius < curRadius ) ? (curRadius) : (radius); 
			}
			tmpCluster.setiClusterRadius(radius);

			// and finally store everything in the list
			lstClusteredTasks.add(tmpCluster);
			
		}
		
		return listTasks;
		
	}
	
	
	/**
	 * Writes a dataset on file using the two lists of tasks and resources passed as parameters
	 *  
	 * @param strPrefix String that holds a description of the ds being written
	 * @param listTasks	List of the tasks to be written on the XML file
	 * @param listResources	List of the resources to be written on the XML file
	 * @param strPath String containing the path where to write the file
	 * @param strFileSuff String containing the optional suffix to append to the filename
	 * @return file name of the generated file
	 */
	public String WriteDataSetOnFile(String strPrefix, List<Task> listTasks, List<Resource> listResources, String strPath, String strFileSuff) {

		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		// XML file generation
		List<String> lstString = new ArrayList<String>();

		// populate the list of strings with the two .xml outputs generated before
		for (Task tsk : listTasks) {
			String xmlOut = xstream.toXML(tsk);
			lstString.add(xmlOut);
		}
		
		for (Resource rsc : listResources) {
			String xmlOut = xstream.toXML(rsc);
			lstString.add(xmlOut);
		}

		// write the file on disk in the path specified by the arguments
		if (strPath == "")
			strDataSetPath = FolderDefs.resourcesFolderName;
		else
			strDataSetPath = strPath;

		strDataSetFileName = strPrefix+ "_"+ listTasks.size() + "_" + listResources.size() + strFileSuff + ".xml";
		
		PerroUtils.print("Attempting to write file " + strPath + strDataSetFileName + " on disk....", true);
		
		try {
			Files.write(Paths.get(strPath + strDataSetFileName ), lstString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return strDataSetFileName;
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BatchConfig tmp = new BatchConfig();
		GenerateDataSet tmp1 = new GenerateDataSet();  
		
		for (int i = 0; i < 10; i++) {
			tmp.setnTasks(i*10+5);
			tmp.setnResources(i+1);
			tmp.setbExtendedTimeWin(true);
			tmp.setbFullResourcesAvailability(false);
			tmp.setbResReturnToStart(true);
			tmp1.GenerateDS("", "", tmp);
		}
		
		/*
		  
		GenerateDS(5, 1, true, false, true);
		GenerateDS(10, 2, true, false, true);
		GenerateDS(10, 5, true, false, true);
		GenerateDS(20, 4, true, false, true);
		GenerateDS(30, 5, true, false, true);
		GenerateDS(50, 5, true, false, true);
		GenerateDS(100, 10, true, false, true);

		 */		

	}

	/**
	 * @return the strDataSetFileName
	 */
	public String getStrDataSetFileName() {
		return strDataSetFileName;
	}

	/**
	 * @return the strDataSetPath
	 */
	public String getStrDataSetPath() {
		return strDataSetPath;
	}

	public ArrayList<Task> getListTasks() {
		return listTasks;
	}

	public void setListTasks(ArrayList<Task> listTasks) {
		this.listTasks = listTasks;
	}

	public ArrayList<Resource> getListResources() {
		return listResources;
	}

	public void setListResources(ArrayList<Resource> listResources) {
		this.listResources = listResources;
	}

	public ArrayList<ClusteredTasks> getLstClusteredTasks() {
		return lstClusteredTasks;
	}

	public void setLstClusteredTasks(ArrayList<ClusteredTasks> lstClusteredTasks) {
		this.lstClusteredTasks = lstClusteredTasks;
	}

	public ArrayList<Task> getLstRandomTasks() {
		return lstRandomTasks;
	}

	public void setLstRandomTasks(ArrayList<Task> lstRandomTasks) {
		this.lstRandomTasks = lstRandomTasks;
	}

}


/**
// First two tags used for the number of elements
// numTsk to identify number of tasks
// numRsc to identify number of resources
lstString.add(0, "<numTsk>"+nTasks+"</numTsk>");
lstString.add(1, "<numRsc>"+nRes+"</numRsc>");
 */


//PerroUtils.print("E" + tsk.getTimeint().getEndTime() +" S "+ tsk.getTimeint().getStartTime());

// set Duration 
// NOTES:
// 1) duration is always < 90 min and cannot be smaller than time window
// 2) the "+1" is used to avoid durations = 0
/*
int newRnd = 0;

int Start = tsk.getTimeint().getStartTime();
int End = tsk.getTimeint().getEndTime();

do {
	newRnd = rndNum.nextInt(End-Start);
} while (newRnd > 90);

tsk.setDuration(newRnd);
*/