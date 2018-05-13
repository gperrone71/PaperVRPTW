/**
 * 
 */
package objects;

import java.lang.reflect.Field;

import utils.NumericUtils;

/**
 * Describes and contains the statistics for the solution
 * 
 * @author gperr
 *
 */
public class SolStats {

	private int numResources;			// number of resources in this execution
	private int numTasks; 				// number of tasks in this execution
	private double dbMaxX;				// maximum value for X
	private double dbMaxY;				// maximum value for Y
	private double dbTasksDensity;		// density of the tasks 
	
	private int numSolutionsFound;		// Number of solutions found by the solver
	private double dblExecutionTime;		// Running time of the solver
	private int iNumThreads;			// number of threads used 
	
	// information on the best solution found
	private int iTotServiced;			// total number of tasks serviced
	private int iTotUnserviced;			// total number of tasks NOT serviced
	private double dbTraveledDistance;	// traveled distance for the best solution
	private double dbTotalCosts;		// total costs for the best solution
	private double dbTimeWinViolation;	// time window violations
	private int iNumVehiclesUsed;		// number of vehicles (i.e. routes) used
    
	// times for the best solution found
	private double dbOperationTime;		// operation time for the best solution
	private double dbTransportTime;		// transport time for the best solution
	private double dbServiceTime;		// service time for the best solutions
	private double dbWaitingTime;		// waiting time for the best solution
	
	
	/**
	 * Returns a string formatted as .csv with the data members names
	 * 
	 * @return	String	string with all data members names separated by a ";"
	 */
	public String getHeaderString() {
		String str = "";

		// get total number of fields for this class
		int numDM = this.getClass().getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = this.getClass().getDeclaredFields();
		
		// parse the fields and creates the return string accordingly
		for (int i = 0; i < numDM; i++ )
				str += (fields[i].getName() + ";");
						
		return str.substring(0, str.length()-1);
	
	}

	/**
	 * Returns a string formatted as .csv with contents of the data members for the instance
	 * 
	 * @return	String	string with all data members values separated by a ";"
	 */
	public String toString() {
		
		NumericUtils.setDefaultFormat();
		
		String str = "";
		
		// get total number of fields for this class
		int numDM = this.getClass().getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = this.getClass().getDeclaredFields();
		
		try {

			// parse the fields and creates the return string accordingly
			for (int i = 0; i < numDM; i++ )			
					str += (fields[i].get(this) + ";");			
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		str = str.replace('.', ',');		// replace dots with commas as decimal separators
		
		return str.substring(0, str.length()-1);
	}

	public int getNumResources() {
		return numResources;
	}

	public void setNumResources(int numResources) {
		this.numResources = numResources;
	}

	public int getNumTasks() {
		return numTasks;
	}

	public void setNumTasks(int numTasks) {
		this.numTasks = numTasks;
	}

	public double getDbMaxX() {
		return dbMaxX;
	}

	public void setDbMaxX(double dbMaxX) {
		this.dbMaxX = dbMaxX;
	}

	public double getDbMaxY() {
		return dbMaxY;
	}

	public void setDbMaxY(double dbMaxY) {
		this.dbMaxY = dbMaxY;
	}

	public double getDbTasksDensity() {
		return dbTasksDensity;
	}

	public void setDbTasksDensity(double dbTasksDensity) {
		this.dbTasksDensity = dbTasksDensity;
	}

	public int getNumSolutionsFound() {
		return numSolutionsFound;
	}

	public void setNumSolutionsFound(int numSolutionsFound) {
		this.numSolutionsFound = numSolutionsFound;
	}

	public double getDblExecutionTime() {
		return dblExecutionTime;
	}

	public void setDblExecutionTime(double dblExecutionTime) {
		this.dblExecutionTime = dblExecutionTime;
	}

	public int getiNumThreads() {
		return iNumThreads;
	}

	public void setiNumThreads(int iNumThreads) {
		this.iNumThreads = iNumThreads;
	}

	public int getiTotServiced() {
		return iTotServiced;
	}

	public void setiTotServiced(int iTotServiced) {
		this.iTotServiced = iTotServiced;
	}

	public int getiTotUnserviced() {
		return iTotUnserviced;
	}

	public void setiTotUnserviced(int iTotUnserviced) {
		this.iTotUnserviced = iTotUnserviced;
	}

	public double getDbTraveledDistance() {
		return dbTraveledDistance;
	}

	public void setDbTraveledDistance(double dbTraveledDistance) {
		this.dbTraveledDistance = dbTraveledDistance;
	}

	public double getDbTotalCosts() {
		return dbTotalCosts;
	}

	public void setDbTotalCosts(double dbTotalCosts) {
		this.dbTotalCosts = dbTotalCosts;
	}

	public double getDbTimeWinViolation() {
		return dbTimeWinViolation;
	}

	public void setDbTimeWinViolation(double dbTimeWinViolation) {
		this.dbTimeWinViolation = dbTimeWinViolation;
	}

	public int getiNumVehiclesUsed() {
		return iNumVehiclesUsed;
	}

	public void setiNumVehiclesUsed(int iNumVehiclesUsed) {
		this.iNumVehiclesUsed = iNumVehiclesUsed;
	}

	public double getDbOperationTime() {
		return dbOperationTime;
	}

	public void setDbOperationTime(double dbOperationTime) {
		this.dbOperationTime = dbOperationTime;
	}

	public double getDbTransportTime() {
		return dbTransportTime;
	}

	public void setDbTransportTime(double dbTransportTime) {
		this.dbTransportTime = dbTransportTime;
	}

	public double getDbServiceTime() {
		return dbServiceTime;
	}

	public void setDbServiceTime(double dbServiceTime) {
		this.dbServiceTime = dbServiceTime;
	}

	public double getDbWaitingTime() {
		return dbWaitingTime;
	}

	public void setDbWaitingTime(double dbWaitingTime) {
		this.dbWaitingTime = dbWaitingTime;
	}


	
}