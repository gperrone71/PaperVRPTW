/**
 * 
 */
package objects;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.thoughtworks.xstream.XStream;

import utils.PerroUtils;

/**
 * @author gperr
 * 
 * Class to hold configuration parameters for the batch launcher. 
 * Objects of this class will be read through an xml file in order to execute batch tasks that are made of:
 * 1) generation of a dataset with possible modifiers
 * 2) execution of the solver
 * 3) generation of the statistics and relevant arff files
 * 
 */
public class BatchConfig {
	
	// number of instances to be generated of this type
	private int nNumInstances;	// number of instances to be generated 
	
	// General Parameters
	private int nResources;		// number of resources to be used for dataset generation
	private int nTasks;			// number of tasks to be used for dataset generation
	
	private int maxX;			// maximum value for the X coordinates
	private int maxY; 			// maximum value for the Y coordinates
	
	private int iNumThreads;	// number of threads to be used for elaboration
	
	private boolean bGenerateTestSet;				// if true, a separate file w/ unlabeled instances is generated to be used as test set

	// RESOURCES parameters
	// space
	private boolean bFullResourcesAvailability;		// forces all resources to have maximum availability window 
	private boolean bResReturnToStart;				// forces origins and destinations for resources to be the same
	private boolean bAllResourcesHaveSamePos;		// forces all resources to have the same starting position
	private char cResourcesStartingPosition;		// specifies how the starting positions will be distributed (R = random, C = Center, N = North, S = South, E = East, W = West, G = tasks center of gravity) 
	private double dStartingPositionsSpread;			// specifies the spreading factor for the starting positions (must be < maxX and maxY)
	
	// time
	private boolean bExtendedTimeWin;				// forces extension of the time window duration for tasks
	private int iResMinStartingTime;				// minimum starting time for the resources time window
	private int iResMinAvailabilityTime;			// min duration of availability
	
	// TASKS Parameters
	private int iTskMinStartingTime;				// minimum starting time for the tasks time window
	private int iTskMinTimeWindowWidth;				// minimum width of the tasks time window
	private int iTskMinServiceTimeDuration;			// minimum service time duration
	private int iTskMaxServiceTimeDuration;			// maximum service time duration
	private int iTskServiceTimeSpread;				// spread for the task starting time (i.e. task start = min start + (0 -> spread) )
	
	
	/**
	 * Create a skeleton file useful for creating the config files
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);

		PerroUtils.print("Generating Batch Config template file");
		
		BatchConfig tmp = new BatchConfig();
/*	
		tmp.setnResources(50);
		tmp.setnTasks(10);
		tmp.setbExtendedTimeWin(true);
		tmp.setbFullResourcesAvailability(true);
		tmp.setbSameDestination(true);
*/		
		int numDM = tmp.getClass().getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = tmp.getClass().getDeclaredFields();
		
		try {

			// parse the fields and creates the return string accordingly
			for (int i = 0; i < numDM; i++ ) {
				
				PerroUtils.print(" " + fields[i].getType());
				
				if (fields[i].getType().toString().equals("int"))
					// field is an int
					fields[i].set(tmp, 0);
					
				if (fields[i].getType().toString().equals("long"))
					// field is a long
					fields[i].set(tmp, 0);

				else if (fields[i].getType().toString().equals("float")) 
					// field is a double
					fields[i].set(tmp, 0.0);

				else if (fields[i].getType().toString().equals("double")) 
					// field is a double
					fields[i].set(tmp, 0.0);

				else if (fields[i].getType().toString().equals("boolean")) 
					// field is a booleam
					fields[i].set(tmp, true);

				else if (fields[i].getType().toString().equals("char")) 
					// field is a booleam
					fields[i].set(tmp, 'C');

				} 					

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

		
		String xmlOut = xstream.toXML(tmp);
		
		// write the file on disk in the \resources folder
		try {
			Files.write(Paths.get("resources/batch/BatchConfigFileTemplate.xml") , xmlOut.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	/**
	 * @return the nResources
	 */
	public int getnResources() {
		return nResources;
	}

	/**
	 * @param nResources the nResources to set
	 */
	public void setnResources(int nResources) {
		this.nResources = nResources;
	}

	/**
	 * @return the nTasks
	 */
	public int getnTasks() {
		return nTasks;
	}

	/**
	 * @param nTasks the nTasks to set
	 */
	public void setnTasks(int nTasks) {
		this.nTasks = nTasks;
	}


	/**
	 * @return the bFullResourcesAvailability
	 */
	public boolean isbFullResourcesAvailability() {
		return bFullResourcesAvailability;
	}


	/**
	 * @param bFullResourcesAvailability the bFullResourcesAvailability to set
	 */
	public void setbFullResourcesAvailability(boolean bFullResourcesAvailability) {
		this.bFullResourcesAvailability = bFullResourcesAvailability;
	}


	/**
	 * @return the bExtendedTimeWin
	 */
	public boolean isbExtendedTimeWin() {
		return bExtendedTimeWin;
	}


	/**
	 * @param bExtendedTimeWin the bExtendedTimeWin to set
	 */
	public void setbExtendedTimeWin(boolean bExtendedTimeWin) {
		this.bExtendedTimeWin = bExtendedTimeWin;
	}


	/**
	 * @return the maxX
	 */
	public int getMaxX() {
		return maxX;
	}


	/**
	 * @param maxX the maxX to set
	 */
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}


	/**
	 * @return the maxY
	 */
	public int getMaxY() {
		return maxY;
	}


	/**
	 * @param maxY the maxY to set
	 */
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}


	/**
	 * @return the bAllResourcesHaveSamePos
	 */
	public boolean isbAllResourcesHaveSamePos() {
		return bAllResourcesHaveSamePos;
	}


	/**
	 * @param bAllResourcesHaveSamePos the bAllResourcesHaveSamePos to set
	 */
	public void setbAllResourcesHaveSamePos(boolean bAllResourcesHaveSamePos) {
		this.bAllResourcesHaveSamePos = bAllResourcesHaveSamePos;
	}


	/**
	 * @return the cResourcesStartingPosition
	 */
	public char getcResourcesStartingPosition() {
		return cResourcesStartingPosition;
	}


	/**
	 * @param cResourcesStartingPosition the cResourcesStartingPosition to set
	 */
	public void setcResourcesStartingPosition(char cResourcesStartingPosition) {
		this.cResourcesStartingPosition = cResourcesStartingPosition;
	}


	/**
	 * @return the dStartingPositionsSpread
	 */
	public double getdStartingPositionsSpread() {
		return dStartingPositionsSpread;
	}


	/**
	 * @param dStartingPositionsSpread the dStartingPositionsSpread to set
	 */
	public void setdStartingPositionsSpread(double dStartingPositionsSpread) {
		this.dStartingPositionsSpread = dStartingPositionsSpread;
	}


	/**
	 * @return the iTskMinStartingTime
	 */
	public int getiTskMinStartingTime() {
		return iTskMinStartingTime;
	}


	/**
	 * @return the iTskMinTimeWindowWidth
	 */
	public int getiTskMinTimeWindowWidth() {
		return iTskMinTimeWindowWidth;
	}


	/**
	 * @return the iTskMinServiceTimeDuration
	 */
	public int getiTskMinServiceTimeDuration() {
		return iTskMinServiceTimeDuration;
	}


	/**
	 * @return the iTskMaxServiceTimeDuration
	 */
	public int getiTskMaxServiceTimeDuration() {
		return iTskMaxServiceTimeDuration;
	}


	/**
	 * @return the iTskServiceTimeSpread
	 */
	public int getiTskServiceTimeSpread() {
		return iTskServiceTimeSpread;
	}


	/**
	 * @return the iResMinStartingTime
	 */
	public int getiResMinStartingTime() {
		return iResMinStartingTime;
	}


	/**
	 * @return the iResMinAvailabilityTime
	 */
	public int getiResMinAvailabilityTime() {
		return iResMinAvailabilityTime;
	}


	/**
	 * @return the bResReturnToStart
	 */
	public boolean isbResReturnToStart() {
		return bResReturnToStart;
	}


	/**
	 * @param bResReturnToStart the bResReturnToStart to set
	 */
	public void setbResReturnToStart(boolean bResReturnToStart) {
		this.bResReturnToStart = bResReturnToStart;
	}


	/**
	 * @return the bGenerateTestSet
	 */
	public boolean isbGenerateTestSet() {
		return bGenerateTestSet;
	}


	/**
	 * @param bGenerateTestSet the bGenerateTestSet to set
	 */
	public void setbGenerateTestSet(boolean bGenerateTestSet) {
		this.bGenerateTestSet = bGenerateTestSet;
	}


	/**
	 * @return the iNumThreads
	 */
	public int getiNumThreads() {
		return iNumThreads;
	}


	/**
	 * @param iNumThreads the iNumThreads to set
	 */
	public void setiNumThreads(int iNumThreads) {
		this.iNumThreads = iNumThreads;
	}


	public int getnNumInstances() {
		return nNumInstances;
	}


	public void setnNumInstances(int nNumInstances) {
		this.nNumInstances = nNumInstances;
	}



}
