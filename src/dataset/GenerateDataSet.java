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
		List<Task> listTasks = new ArrayList<Task>(nTasks);
		List<Resource> listResources = new ArrayList<Resource>(nRes);
		int numNodes = 0;
		
			
		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);

		PerroUtils.print("Generating dataset for " + nTasks + " tasks and " + nRes + " resources.", true);
		
		// initialize rnd generator
		Random rnd = new Random();		
		
		// generate collections for the data to be created
		// Tasks list
		for (int i = 0; i < nTasks; i++) {
			Task tsk = new Task();
			
			Random rndNum = new Random();
			
			// set ID
			tsk.setId(i);
			
			// set type to T
			tsk.setType("T");
			
			// set Description
			tsk.setDescription("Task#"+i);
			
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
			pos.generateRndPosition(maxX, maxY);
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
		}

		numNodes = listTasks.size()-1;

		// calculates the center of gravity for the tasks
		double dCoGX = 0;
		double dCoGY = 0;
		for (Task tsk : listTasks) {
			dCoGX += tsk.getNode().getLatitude();
			dCoGY += tsk.getNode().getLongitude();
		}
		dCoGX = (dCoGX / listTasks.size());
		dCoGY = (dCoGY / listTasks.size());
			
		
		// Resources generations
		// preliminary activity: based on the type of positioning required, calculates in advance the center of the resources starting point
		double dResCentralPosX = 0;
		double dResCentralPosY = 0;
		

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
				Random numRnd = new Random();

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
			pos1.setId(++numNodes);  			// the node ID for the
			rsc.setDestination(pos1);
			
			// generate a random skill
			int iSkill = rnd.nextInt(10);
			rsc.setSkill(iSkill);
			
			// and copy the resource
			listResources.add(rsc);
		}

		// writes the generated dataset on an XML file
		WriteDataSetOnFile(listTasks, listResources, strPath, strFileSuff);
	
		PerroUtils.print("Generation completed successfully (" + numNodes +" nodes generated).", true);
	}

	
	/**
	 * Writes a dataset on file using the two lists of tasks and resources passed as parameters
	 *  
	 * @param listTasks	List of the tasks to be written on the XML file
	 * @param listResources	List of the resources to be written on the XML file
	 * @param strPath String containing the path where to write the file
	 * @param strFileSuff String containing the optional suffix to append to the filename
	 * @return file name of the generated file
	 */
	public String WriteDataSetOnFile(List<Task> listTasks, List<Resource> listResources, String strPath, String strFileSuff) {

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
			strDataSetPath = "resources/";
		else
			strDataSetPath = strPath;

		strDataSetFileName = "DS_"+ listTasks.size() + "_" + listResources.size() + strFileSuff + ".xml";
		
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