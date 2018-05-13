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
 * Class to store configuration objects for the GeoRef data set generation
 * 
 * @author gperr
 *
 */
public class GeoRefConfig {

	private int nNumInstances;		// number of instances to be generated
	private String strCity;			// name of the city to be used as center
	private int iRadius;			// radius of the area to be considered
	private double dbScalingFactor;	// scaling factor for the population (i.e. customers = pop/scalingfactor)
	private double dbPopDensity;	// population density (i.e. max spread radius = pop / 
	private boolean blCustomersUniformSpread;		// true is clients have to be spread uniformly within the radius of the city or not
	private double dbResourcesToClientRatio;		// resources : clients ratio - if set to 0 then the fixed value (see below) is used
	private int iNumResources;		// fixed number of resources to be used (used only when dbResourcesToClientRatio is 0)
	

	// RESOURCES parameters
	// space
	private boolean bFullResourcesAvailability;		// forces all resources to have maximum availability window (NOT USED) 
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

		PerroUtils.print("Generating Geo Config template file");
		
		GeoRefConfig tmp = new GeoRefConfig();

		int numDM = tmp.getClass().getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = tmp.getClass().getDeclaredFields();
		
		try {

			// parse the fields and creates the return string accordingly
			for (int i = 0; i < numDM; i++ ) {
				
				PerroUtils.print(" " + fields[i].getType());

				if (fields[i].getType().toString().equals("class java.lang.String"))
					// field is a String
					fields[i].set(tmp, "na");

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
			Files.write(Paths.get("georef/resources/GeoRefConfigFileTemplate.xml") , xmlOut.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	// GETters and SETters

	public int getnNumInstances() {
		return nNumInstances;
	}

	public void setnNumInstances(int nNumInstances) {
		this.nNumInstances = nNumInstances;
	}

	public String getStrCity() {
		return strCity;
	}

	public void setStrCity(String strCity) {
		this.strCity = strCity;
	}

	public int getiRadius() {
		return iRadius;
	}

	public void setiRadius(int iRadius) {
		this.iRadius = iRadius;
	}

	public double getDbScalingFactor() {
		return dbScalingFactor;
	}

	public void setDbScalingFactor(double dbScalingFactor) {
		this.dbScalingFactor = dbScalingFactor;
	}

	public double getDbPopDensity() {
		return dbPopDensity;
	}

	public void setDbPopDensity(double dbPopDensity) {
		this.dbPopDensity = dbPopDensity;
	}

	public boolean isBlCustomersUniformSpread() {
		return blCustomersUniformSpread;
	}

	public void setBlCustomersUniformSpread(boolean blCustomersUniformSpread) {
		this.blCustomersUniformSpread = blCustomersUniformSpread;
	}

	public double getDbResourcesToClientRatio() {
		return dbResourcesToClientRatio;
	}

	public void setDbResourcesToClientRatio(double dbResourcesToClientRatio) {
		this.dbResourcesToClientRatio = dbResourcesToClientRatio;
	}

	public int getiNumResources() {
		return iNumResources;
	}

	public void setiNumResources(int iNumResources) {
		this.iNumResources = iNumResources;
	}

	public boolean isbFullResourcesAvailability() {
		return bFullResourcesAvailability;
	}

	public void setbFullResourcesAvailability(boolean bFullResourcesAvailability) {
		this.bFullResourcesAvailability = bFullResourcesAvailability;
	}

	public boolean isbResReturnToStart() {
		return bResReturnToStart;
	}

	public void setbResReturnToStart(boolean bResReturnToStart) {
		this.bResReturnToStart = bResReturnToStart;
	}

	public boolean isbAllResourcesHaveSamePos() {
		return bAllResourcesHaveSamePos;
	}

	public void setbAllResourcesHaveSamePos(boolean bAllResourcesHaveSamePos) {
		this.bAllResourcesHaveSamePos = bAllResourcesHaveSamePos;
	}

	public char getcResourcesStartingPosition() {
		return cResourcesStartingPosition;
	}

	public void setcResourcesStartingPosition(char cResourcesStartingPosition) {
		this.cResourcesStartingPosition = cResourcesStartingPosition;
	}

	public double getdStartingPositionsSpread() {
		return dStartingPositionsSpread;
	}

	public void setdStartingPositionsSpread(double dStartingPositionsSpread) {
		this.dStartingPositionsSpread = dStartingPositionsSpread;
	}

	public boolean isbExtendedTimeWin() {
		return bExtendedTimeWin;
	}

	public void setbExtendedTimeWin(boolean bExtendedTimeWin) {
		this.bExtendedTimeWin = bExtendedTimeWin;
	}

	public int getiResMinStartingTime() {
		return iResMinStartingTime;
	}

	public void setiResMinStartingTime(int iResMinStartingTime) {
		this.iResMinStartingTime = iResMinStartingTime;
	}

	public int getiResMinAvailabilityTime() {
		return iResMinAvailabilityTime;
	}

	public void setiResMinAvailabilityTime(int iResMinAvailabilityTime) {
		this.iResMinAvailabilityTime = iResMinAvailabilityTime;
	}

	public int getiTskMinStartingTime() {
		return iTskMinStartingTime;
	}

	public void setiTskMinStartingTime(int iTskMinStartingTime) {
		this.iTskMinStartingTime = iTskMinStartingTime;
	}

	public int getiTskMinTimeWindowWidth() {
		return iTskMinTimeWindowWidth;
	}

	public void setiTskMinTimeWindowWidth(int iTskMinTimeWindowWidth) {
		this.iTskMinTimeWindowWidth = iTskMinTimeWindowWidth;
	}

	public int getiTskMinServiceTimeDuration() {
		return iTskMinServiceTimeDuration;
	}

	public void setiTskMinServiceTimeDuration(int iTskMinServiceTimeDuration) {
		this.iTskMinServiceTimeDuration = iTskMinServiceTimeDuration;
	}

	public int getiTskMaxServiceTimeDuration() {
		return iTskMaxServiceTimeDuration;
	}

	public void setiTskMaxServiceTimeDuration(int iTskMaxServiceTimeDuration) {
		this.iTskMaxServiceTimeDuration = iTskMaxServiceTimeDuration;
	}

	public int getiTskServiceTimeSpread() {
		return iTskServiceTimeSpread;
	}

	public void setiTskServiceTimeSpread(int iTskServiceTimeSpread) {
		this.iTskServiceTimeSpread = iTskServiceTimeSpread;
	}
	
}


