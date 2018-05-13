/**
 * 
 */
package objects;

import java.lang.reflect.Field;

import utils.NumericUtils;

/**
 * Describes and contains the statistics for the batch classifier
 * 
 *  * 08/01/2018	Added timestamp, hash and instance name
 * 
 * @author gperr
 *
 */
public class ClassifierStats {

	// information on date of execution
	private String strFullTimeStamp;	// Full timestamp of the execution
	private String strTimeStampDay;		// Timestamp (short version w/ only dates) 

	// Information describing the instance
	private String strInstanceName;		// "name" of the instance, i.e. its file name
	private String strHash;				// hash code for the instance
	private int numResources;			// number of resources in this execution
	private double dbMaxX;				// maximum value for X
	private double dbMaxY;				// maximum value for Y

	// generic information
	private int iNumThreads;			// number of threads used 
	
	// Information on the unpruned solution
	private int numTasks_UP; 				// number of tasks in this execution
	private double dbTasksDensity_UP;		// density of the tasks 
	private int numSolutionsFound_UP;		// Number of solutions found by the solver
	private double dblExecutionTime_UP;		// Running time of the solver
	private int iTotServiced_UP;			// total number of tasks serviced
	private int iTotUnserviced_UP;			// total number of tasks not serviced
	private int iNumVehiclesUsed_UP;		// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_UP;	// time window violations

	private double dbOperationTime_UP;		// operation time for the best solution
	private double dbServiceTime_UP;		// service time for the best solutions
	private double dbWaitingTime_UP;		// waiting time for the best solution
	private double dbTransportTime_UP;		// transport time for the best solution

	private double dbTotalCosts_UP;			// total costs for the best solution
	private double dbTraveledDistance_UP;	// traveled distance for the best solution

	// Information on the PRUNED solution
	private int numTasks_P; 				// number of the pruned tasks
	private double dbTasksDensity_P;		// tasks density for the pruned set of tasks
	private int numSolutionsFound_P;		// Number of solutions found by the solver
	private double dblExecutionTime_P;		// Running time of the solver
	private int iTotServiced_P;				// total number of tasks serviced
	private int iTotUnserviced_P;			// total number of tasks not serviced
	private int iNumVehiclesUsed_P;			// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_P;	// time window violations

	private double dbOperationTime_P;		// operationtime for the best solution
	private double dbServiceTime_P;			// service time for the best solutions
	private double dbWaitingTime_P;			// waiting time for the best solution
	private double dbTransportTime_P;		// transport time for the best solution

	private double dbTotalCosts_P;			// total costs for the best solution
	private double dbTraveledDistance_P;	// traveled distance for the best solution
		
	
	// stats on differences between the two
	private double dbAbsExecTimeDiff;			// difference between the execution times (absolute)
	private double dbPerExecTimeDiff;			// difference between the execution times (%)
	private double dbAbsSrvcdTasksDiff;			// difference between the execution times (absolute)
	private double dbPerSrvcdTasksDiff;			// difference between the execution times (%)
	
	// information on the trained classifier
	private double dbPrecision;					// classifier precision
	private double dbRecall;					// classifier recall
	private double dbAbsCorrectlyClassified;		// number of correctly classified instances (absolute)
	private double dbPerCorrectlyClassified;		// number of correctly classified instances (%)	
	private double dbAbsUncorrectlyClassified;		// number of uncorrectly classified instances (absolute)
	private double dbPerUncorrectlyClassified;		// number of uncorrectly classified instances (%)	
	
	
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
			for (int i = 0; i < numDM; i++ ) {
				if (fields[i].getType().isAssignableFrom(String.class))
					str += (fields[i].get(this) + ";");
				else
					str += (fields[i].get(this) + ";").replace('.', ',');
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		str = str.replace('.', ',');		// replace dots with commas as decimal separators
		
		return str.substring(0, str.length()-1);
	}

	public String getStrFullTimeStamp() {
		return strFullTimeStamp;
	}

	public void setStrFullTimeStamp(String strFullTimeStamp) {
		this.strFullTimeStamp = strFullTimeStamp;
	}

	public String getStrTimeStampDay() {
		return strTimeStampDay;
	}

	public void setStrTimeStampDay(String strTimeStampDay) {
		this.strTimeStampDay = strTimeStampDay;
	}

	public String getStrInstanceName() {
		return strInstanceName;
	}

	public void setStrInstanceName(String strInstanceName) {
		this.strInstanceName = strInstanceName;
	}

	public String getStrHash() {
		return strHash;
	}

	public void setStrHash(String strHash) {
		this.strHash = strHash;
	}

	public int getNumResources() {
		return numResources;
	}

	public void setNumResources(int numResources) {
		this.numResources = numResources;
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

	public int getiNumThreads() {
		return iNumThreads;
	}

	public void setiNumThreads(int iNumThreads) {
		this.iNumThreads = iNumThreads;
	}

	public int getNumTasks_UP() {
		return numTasks_UP;
	}

	public void setNumTasks_UP(int numTasks_UP) {
		this.numTasks_UP = numTasks_UP;
	}

	public double getDbTasksDensity_UP() {
		return dbTasksDensity_UP;
	}

	public void setDbTasksDensity_UP(double dbTasksDensity_UP) {
		this.dbTasksDensity_UP = dbTasksDensity_UP;
	}

	public int getNumSolutionsFound_UP() {
		return numSolutionsFound_UP;
	}

	public void setNumSolutionsFound_UP(int numSolutionsFound_UP) {
		this.numSolutionsFound_UP = numSolutionsFound_UP;
	}

	public double getDblExecutionTime_UP() {
		return dblExecutionTime_UP;
	}

	public void setDblExecutionTime_UP(double dblExecutionTime_UP) {
		this.dblExecutionTime_UP = dblExecutionTime_UP;
	}

	public int getiTotServiced_UP() {
		return iTotServiced_UP;
	}

	public void setiTotServiced_UP(int iTotServiced_UP) {
		this.iTotServiced_UP = iTotServiced_UP;
	}

	public int getiTotUnserviced_UP() {
		return iTotUnserviced_UP;
	}

	public void setiTotUnserviced_UP(int iTotUnserviced_UP) {
		this.iTotUnserviced_UP = iTotUnserviced_UP;
	}

	public int getiNumVehiclesUsed_UP() {
		return iNumVehiclesUsed_UP;
	}

	public void setiNumVehiclesUsed_UP(int iNumVehiclesUsed_UP) {
		this.iNumVehiclesUsed_UP = iNumVehiclesUsed_UP;
	}

	public double getDbTraveledDistance_UP() {
		return dbTraveledDistance_UP;
	}

	public void setDbTraveledDistance_UP(double dbTraveledDistance_UP) {
		this.dbTraveledDistance_UP = dbTraveledDistance_UP;
	}

	public double getDbOperationTime_UP() {
		return dbOperationTime_UP;
	}

	public void setDbOperationTime_UP(double dbOperationTime_UP) {
		this.dbOperationTime_UP = dbOperationTime_UP;
	}

	public double getDbServiceTime_UP() {
		return dbServiceTime_UP;
	}

	public void setDbServiceTime_UP(double dbServiceTime_UP) {
		this.dbServiceTime_UP = dbServiceTime_UP;
	}

	public double getDbWaitingTime_UP() {
		return dbWaitingTime_UP;
	}

	public void setDbWaitingTime_UP(double dbWaitingTime_UP) {
		this.dbWaitingTime_UP = dbWaitingTime_UP;
	}

	public double getDbTotalCosts_UP() {
		return dbTotalCosts_UP;
	}

	public void setDbTotalCosts_UP(double dbTotalCosts_UP) {
		this.dbTotalCosts_UP = dbTotalCosts_UP;
	}

	public double getDbTimeWinViolation_UP() {
		return dbTimeWinViolation_UP;
	}

	public void setDbTimeWinViolation_UP(double dbTimeWinViolation_UP) {
		this.dbTimeWinViolation_UP = dbTimeWinViolation_UP;
	}

	public int getNumTasks_P() {
		return numTasks_P;
	}

	public void setNumTasks_P(int numTasks_P) {
		this.numTasks_P = numTasks_P;
	}

	public double getDbTasksDensity_P() {
		return dbTasksDensity_P;
	}

	public void setDbTasksDensity_P(double dbTasksDensity_P) {
		this.dbTasksDensity_P = dbTasksDensity_P;
	}

	public int getNumSolutionsFound_P() {
		return numSolutionsFound_P;
	}

	public void setNumSolutionsFound_P(int numSolutionsFound_P) {
		this.numSolutionsFound_P = numSolutionsFound_P;
	}

	public double getDblExecutionTime_P() {
		return dblExecutionTime_P;
	}

	public void setDblExecutionTime_P(double dblExecutionTime_P) {
		this.dblExecutionTime_P = dblExecutionTime_P;
	}

	public int getiTotServiced_P() {
		return iTotServiced_P;
	}

	public void setiTotServiced_P(int iTotServiced_P) {
		this.iTotServiced_P = iTotServiced_P;
	}

	public int getiTotUnserviced_P() {
		return iTotUnserviced_P;
	}

	public void setiTotUnserviced_P(int iTotUnserviced_P) {
		this.iTotUnserviced_P = iTotUnserviced_P;
	}

	public int getiNumVehiclesUsed_P() {
		return iNumVehiclesUsed_P;
	}

	public void setiNumVehiclesUsed_P(int iNumVehiclesUsed_P) {
		this.iNumVehiclesUsed_P = iNumVehiclesUsed_P;
	}

	public double getDbTraveledDistance_P() {
		return dbTraveledDistance_P;
	}

	public void setDbTraveledDistance_P(double dbTraveledDistance_P) {
		this.dbTraveledDistance_P = dbTraveledDistance_P;
	}

	public double getDbOperationTime_P() {
		return dbOperationTime_P;
	}

	public void setDbOperationTime_P(double dbOperationTime_P) {
		this.dbOperationTime_P = dbOperationTime_P;
	}

	public double getDbServiceTime_P() {
		return dbServiceTime_P;
	}

	public void setDbServiceTime_P(double dbServiceTime_P) {
		this.dbServiceTime_P = dbServiceTime_P;
	}

	public double getDbWaitingTime_P() {
		return dbWaitingTime_P;
	}

	public void setDbWaitingTime_P(double dbWaitingTime_P) {
		this.dbWaitingTime_P = dbWaitingTime_P;
	}

	public double getDbTotalCosts_P() {
		return dbTotalCosts_P;
	}

	public void setDbTotalCosts_P(double dbTotalCosts_P) {
		this.dbTotalCosts_P = dbTotalCosts_P;
	}

	public double getDbTimeWinViolation_P() {
		return dbTimeWinViolation_P;
	}

	public void setDbTimeWinViolation_P(double dbTimeWinViolation_P) {
		this.dbTimeWinViolation_P = dbTimeWinViolation_P;
	}

	public double getDbAbsExecTimeDiff() {
		return dbAbsExecTimeDiff;
	}

	public void setDbAbsExecTimeDiff(double dbAbsExecTimeDiff) {
		this.dbAbsExecTimeDiff = dbAbsExecTimeDiff;
	}

	public double getDbPerExecTimeDiff() {
		return dbPerExecTimeDiff;
	}

	public void setDbPerExecTimeDiff(double dbPerExecTimeDiff) {
		this.dbPerExecTimeDiff = dbPerExecTimeDiff;
	}

	public double getDbAbsSrvcdTasksDiff() {
		return dbAbsSrvcdTasksDiff;
	}

	public void setDbAbsSrvcdTasksDiff(double dbAbsSrvcdTasksDiff) {
		this.dbAbsSrvcdTasksDiff = dbAbsSrvcdTasksDiff;
	}

	public double getDbPerSrvcdTasksDiff() {
		return dbPerSrvcdTasksDiff;
	}

	public void setDbPerSrvcdTasksDiff(double dbPerSrvcdTasksDiff) {
		this.dbPerSrvcdTasksDiff = dbPerSrvcdTasksDiff;
	}

	public double getDbPrecision() {
		return dbPrecision;
	}

	public void setDbPrecision(double dbPrecision) {
		this.dbPrecision = dbPrecision;
	}

	public double getDbRecall() {
		return dbRecall;
	}

	public void setDbRecall(double dbRecall) {
		this.dbRecall = dbRecall;
	}

	public double getDbAbsCorrectlyClassified() {
		return dbAbsCorrectlyClassified;
	}

	public void setDbAbsCorrectlyClassified(double dbAbsCorrectlyClassified) {
		this.dbAbsCorrectlyClassified = dbAbsCorrectlyClassified;
	}

	public double getDbPerCorrectlyClassified() {
		return dbPerCorrectlyClassified;
	}

	public void setDbPerCorrectlyClassified(double dbPerCorrectlyClassified) {
		this.dbPerCorrectlyClassified = dbPerCorrectlyClassified;
	}

	public double getDbAbsUncorrectlyClassified() {
		return dbAbsUncorrectlyClassified;
	}

	public void setDbAbsUncorrectlyClassified(double dbAbsUncorrectlyClassified) {
		this.dbAbsUncorrectlyClassified = dbAbsUncorrectlyClassified;
	}

	public double getDbPerUncorrectlyClassified() {
		return dbPerUncorrectlyClassified;
	}

	public void setDbPerUncorrectlyClassified(double dbPerUncorrectlyClassified) {
		this.dbPerUncorrectlyClassified = dbPerUncorrectlyClassified;
	}

	public double getDbTransportTime_UP() {
		return dbTransportTime_UP;
	}

	public void setDbTransportTime_UP(double dbTransportTime_UP) {
		this.dbTransportTime_UP = dbTransportTime_UP;
	}

	public double getDbTransportTime_P() {
		return dbTransportTime_P;
	}

	public void setDbTransportTime_P(double dbTransportTime_P) {
		this.dbTransportTime_P = dbTransportTime_P;
	}



}
