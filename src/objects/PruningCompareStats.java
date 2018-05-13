/**
 * 
 */
package objects;

import java.lang.reflect.Field;

import utils.NumericUtils;

/**
 * Objects of this class are used to make comparison between different pruning techniques. In particular, an object of this class can be used to compare results from 4 different types of pruning.
 * 
 * @author gperr
 *
 */
public class PruningCompareStats {

	// information on date of execution
	private String strFullTimeStamp;	// Full timestamp of the execution
	private String strTimeStampDay;		// Timestamp (short version w/ only dates) 

	// Information describing the instance
	private String strInstanceName;		// "name" of the instance, i.e. its file name
	private String strHash;				// hash code for the instance
	private int numResources;			// number of resources in this execution
	private double dbMaxX;				// maximum value for X
	private double dbMaxY;				// maximum value for Y


	// Information on the unpruned solution
	private int numTasks_UP; 				// number of tasks in this execution
	private double dblExecutionTime_UP;		// Running time of the solver
	private int iTotServiced_UP;			// total number of tasks serviced
	private int iTotUnserviced_UP;			// total number of tasks not serviced
	private int iNumVehiclesUsed_UP;		// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_UP;	// time window violations

	private double dbOperationTime_UP;		// operation time for the best solution
	private double dbTotalCosts_UP;			// total costs for the best solution
	private double dbTraveledDistance_UP;	// traveled distance for the best solution

	// Information on the PRUNED 1 solution
	private int numTasks_P; 				// number of the pruned tasks
	private double dblExecutionTime_P;		// Running time of the solver
	private int iTotServiced_P;				// total number of tasks serviced
	private int iTotUnserviced_P;			// total number of tasks not serviced
	private int iNumVehiclesUsed_P;			// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_P;	// time window violations

	private double dbOperationTime_P;		// operation time for the best solution
	private double dbTotalCosts_P;			// total costs for the best solution
	private double dbTraveledDistance_P;	// traveled distance for the best solution
		
	// Information on the PRUNED RND 1 solution
	private int numTasks_PR1; 				// number of the pruned tasks
	private double dblExecutionTime_PR1;		// Running time of the solver
	private int iTotServiced_PR1;				// total number of tasks serviced
	private int iTotUnserviced_PR1;			// total number of tasks not serviced
	private int iNumVehiclesUsed_PR1;			// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_PR1;	// time window violations

	private double dbOperationTime_PR1;		// operation time for the best solution
	private double dbTotalCosts_PR1;			// total costs for the best solution
	private double dbTraveledDistance_PR1;	// traveled distance for the best solution

	// Information on the PRUNED RND2 solution
	private int numTasks_PR2; 				// number of the pruned tasks
	private double dblExecutionTime_PR2;		// Running time of the solver
	private int iTotServiced_PR2;				// total number of tasks serviced
	private int iTotUnserviced_PR2;			// total number of tasks not serviced
	private int iNumVehiclesUsed_PR2;			// number of vehicles (i.e. routes) used
	private double dbTimeWinViolation_PR2;	// time window violations

	private double dbOperationTime_PR2;		// operation time for the best solution
	private double dbTotalCosts_PR2;			// total costs for the best solution
	private double dbTraveledDistance_PR2;	// traveled distance for the best solution

	
	// stats on differences 
	// differences between unpruned and pruned
	private double dbAbsExecTimeDiff;			// difference between the execution times (absolute)
	private double dbPerExecTimeDiff;			// difference between the execution times (%)
	private double dbAbsSrvcdTasksDiff;			// difference between the execution times (absolute)
	private double dbPerSrvcdTasksDiff;			// difference between the execution times (%)
	// differences between unpruned and rnd
	private double dbAbsExecTimeDiff_PR1;			// difference between the execution times (absolute)
	private double dbPerExecTimeDiff_PR1;			// difference between the execution times (%)
	private double dbAbsSrvcdTasksDiff_PR1;			// difference between the execution times (absolute)
	private double dbPerSrvcdTasksDiff_PR1;			// difference between the execution times (%)
	// differences between unpruned and rnd2
	private double dbAbsExecTimeDiff_PR2;			// difference between the execution times (absolute)
	private double dbPerExecTimeDiff_PR2;			// difference between the execution times (%)
	private double dbAbsSrvcdTasksDiff_PR2;			// difference between the execution times (absolute)
	private double dbPerSrvcdTasksDiff_PR2;			// difference between the execution times (%)
	
	
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

	public int getNumTasks_UP() {
		return numTasks_UP;
	}

	public void setNumTasks_UP(int numTasks_UP) {
		this.numTasks_UP = numTasks_UP;
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

	public double getDbTimeWinViolation_UP() {
		return dbTimeWinViolation_UP;
	}

	public void setDbTimeWinViolation_UP(double dbTimeWinViolation_UP) {
		this.dbTimeWinViolation_UP = dbTimeWinViolation_UP;
	}

	public double getDbOperationTime_UP() {
		return dbOperationTime_UP;
	}

	public void setDbOperationTime_UP(double dbOperationTime_UP) {
		this.dbOperationTime_UP = dbOperationTime_UP;
	}

	public double getDbTotalCosts_UP() {
		return dbTotalCosts_UP;
	}

	public void setDbTotalCosts_UP(double dbTotalCosts_UP) {
		this.dbTotalCosts_UP = dbTotalCosts_UP;
	}

	public double getDbTraveledDistance_UP() {
		return dbTraveledDistance_UP;
	}

	public void setDbTraveledDistance_UP(double dbTraveledDistance_UP) {
		this.dbTraveledDistance_UP = dbTraveledDistance_UP;
	}

	public int getNumTasks_P() {
		return numTasks_P;
	}

	public void setNumTasks_P(int numTasks_P) {
		this.numTasks_P = numTasks_P;
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

	public double getDbTimeWinViolation_P() {
		return dbTimeWinViolation_P;
	}

	public void setDbTimeWinViolation_P(double dbTimeWinViolation_P) {
		this.dbTimeWinViolation_P = dbTimeWinViolation_P;
	}

	public double getDbOperationTime_P() {
		return dbOperationTime_P;
	}

	public void setDbOperationTime_P(double dbOperationTime_P) {
		this.dbOperationTime_P = dbOperationTime_P;
	}

	public double getDbTotalCosts_P() {
		return dbTotalCosts_P;
	}

	public void setDbTotalCosts_P(double dbTotalCosts_P) {
		this.dbTotalCosts_P = dbTotalCosts_P;
	}

	public double getDbTraveledDistance_P() {
		return dbTraveledDistance_P;
	}

	public void setDbTraveledDistance_P(double dbTraveledDistance_P) {
		this.dbTraveledDistance_P = dbTraveledDistance_P;
	}

	public int getNumTasks_PR1() {
		return numTasks_PR1;
	}

	public void setNumTasks_PR1(int numTasks_PR1) {
		this.numTasks_PR1 = numTasks_PR1;
	}

	public double getDblExecutionTime_PR1() {
		return dblExecutionTime_PR1;
	}

	public void setDblExecutionTime_PR1(double dblExecutionTime_PR1) {
		this.dblExecutionTime_PR1 = dblExecutionTime_PR1;
	}

	public int getiTotServiced_PR1() {
		return iTotServiced_PR1;
	}

	public void setiTotServiced_PR1(int iTotServiced_PR1) {
		this.iTotServiced_PR1 = iTotServiced_PR1;
	}

	public int getiTotUnserviced_PR1() {
		return iTotUnserviced_PR1;
	}

	public void setiTotUnserviced_PR1(int iTotUnserviced_PR1) {
		this.iTotUnserviced_PR1 = iTotUnserviced_PR1;
	}

	public int getiNumVehiclesUsed_PR1() {
		return iNumVehiclesUsed_PR1;
	}

	public void setiNumVehiclesUsed_PR1(int iNumVehiclesUsed_PR1) {
		this.iNumVehiclesUsed_PR1 = iNumVehiclesUsed_PR1;
	}

	public double getDbTimeWinViolation_PR1() {
		return dbTimeWinViolation_PR1;
	}

	public void setDbTimeWinViolation_PR1(double dbTimeWinViolation_PR1) {
		this.dbTimeWinViolation_PR1 = dbTimeWinViolation_PR1;
	}

	public double getDbOperationTime_PR1() {
		return dbOperationTime_PR1;
	}

	public void setDbOperationTime_PR1(double dbOperationTime_PR1) {
		this.dbOperationTime_PR1 = dbOperationTime_PR1;
	}

	public double getDbTotalCosts_PR1() {
		return dbTotalCosts_PR1;
	}

	public void setDbTotalCosts_PR1(double dbTotalCosts_PR1) {
		this.dbTotalCosts_PR1 = dbTotalCosts_PR1;
	}

	public double getDbTraveledDistance_PR1() {
		return dbTraveledDistance_PR1;
	}

	public void setDbTraveledDistance_PR1(double dbTraveledDistance_PR1) {
		this.dbTraveledDistance_PR1 = dbTraveledDistance_PR1;
	}

	public int getNumTasks_PR2() {
		return numTasks_PR2;
	}

	public void setNumTasks_PR2(int numTasks_PR2) {
		this.numTasks_PR2 = numTasks_PR2;
	}

	public double getDblExecutionTime_PR2() {
		return dblExecutionTime_PR2;
	}

	public void setDblExecutionTime_PR2(double dblExecutionTime_PR2) {
		this.dblExecutionTime_PR2 = dblExecutionTime_PR2;
	}

	public int getiTotServiced_PR2() {
		return iTotServiced_PR2;
	}

	public void setiTotServiced_PR2(int iTotServiced_PR2) {
		this.iTotServiced_PR2 = iTotServiced_PR2;
	}

	public int getiTotUnserviced_PR2() {
		return iTotUnserviced_PR2;
	}

	public void setiTotUnserviced_PR2(int iTotUnserviced_PR2) {
		this.iTotUnserviced_PR2 = iTotUnserviced_PR2;
	}

	public int getiNumVehiclesUsed_PR2() {
		return iNumVehiclesUsed_PR2;
	}

	public void setiNumVehiclesUsed_PR2(int iNumVehiclesUsed_PR2) {
		this.iNumVehiclesUsed_PR2 = iNumVehiclesUsed_PR2;
	}

	public double getDbTimeWinViolation_PR2() {
		return dbTimeWinViolation_PR2;
	}

	public void setDbTimeWinViolation_PR2(double dbTimeWinViolation_PR2) {
		this.dbTimeWinViolation_PR2 = dbTimeWinViolation_PR2;
	}

	public double getDbOperationTime_PR2() {
		return dbOperationTime_PR2;
	}

	public void setDbOperationTime_PR2(double dbOperationTime_PR2) {
		this.dbOperationTime_PR2 = dbOperationTime_PR2;
	}

	public double getDbTotalCosts_PR2() {
		return dbTotalCosts_PR2;
	}

	public void setDbTotalCosts_PR2(double dbTotalCosts_PR2) {
		this.dbTotalCosts_PR2 = dbTotalCosts_PR2;
	}

	public double getDbTraveledDistance_PR2() {
		return dbTraveledDistance_PR2;
	}

	public void setDbTraveledDistance_PR2(double dbTraveledDistance_PR2) {
		this.dbTraveledDistance_PR2 = dbTraveledDistance_PR2;
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

	public double getDbAbsExecTimeDiff_PR1() {
		return dbAbsExecTimeDiff_PR1;
	}

	public void setDbAbsExecTimeDiff_PR1(double dbAbsExecTimeDiff_PR1) {
		this.dbAbsExecTimeDiff_PR1 = dbAbsExecTimeDiff_PR1;
	}

	public double getDbPerExecTimeDiff_PR1() {
		return dbPerExecTimeDiff_PR1;
	}

	public void setDbPerExecTimeDiff_PR1(double dbPerExecTimeDiff_PR1) {
		this.dbPerExecTimeDiff_PR1 = dbPerExecTimeDiff_PR1;
	}

	public double getDbAbsSrvcdTasksDiff_PR1() {
		return dbAbsSrvcdTasksDiff_PR1;
	}

	public void setDbAbsSrvcdTasksDiff_PR1(double dbAbsSrvcdTasksDiff_PR1) {
		this.dbAbsSrvcdTasksDiff_PR1 = dbAbsSrvcdTasksDiff_PR1;
	}

	public double getDbPerSrvcdTasksDiff_PR1() {
		return dbPerSrvcdTasksDiff_PR1;
	}

	public void setDbPerSrvcdTasksDiff_PR1(double dbPerSrvcdTasksDiff_PR1) {
		this.dbPerSrvcdTasksDiff_PR1 = dbPerSrvcdTasksDiff_PR1;
	}

	public double getDbAbsExecTimeDiff_PR2() {
		return dbAbsExecTimeDiff_PR2;
	}

	public void setDbAbsExecTimeDiff_PR2(double dbAbsExecTimeDiff_PR2) {
		this.dbAbsExecTimeDiff_PR2 = dbAbsExecTimeDiff_PR2;
	}

	public double getDbPerExecTimeDiff_PR2() {
		return dbPerExecTimeDiff_PR2;
	}

	public void setDbPerExecTimeDiff_PR2(double dbPerExecTimeDiff_PR2) {
		this.dbPerExecTimeDiff_PR2 = dbPerExecTimeDiff_PR2;
	}

	public double getDbAbsSrvcdTasksDiff_PR2() {
		return dbAbsSrvcdTasksDiff_PR2;
	}

	public void setDbAbsSrvcdTasksDiff_PR2(double dbAbsSrvcdTasksDiff_PR2) {
		this.dbAbsSrvcdTasksDiff_PR2 = dbAbsSrvcdTasksDiff_PR2;
	}

	public double getDbPerSrvcdTasksDiff_PR2() {
		return dbPerSrvcdTasksDiff_PR2;
	}

	public void setDbPerSrvcdTasksDiff_PR2(double dbPerSrvcdTasksDiff_PR2) {
		this.dbPerSrvcdTasksDiff_PR2 = dbPerSrvcdTasksDiff_PR2;
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



}
