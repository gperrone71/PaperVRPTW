/**
 * 
 */
package objects;

import java.lang.reflect.Field;

import utils.NumericUtils;

/**
 * Describes and contains the evaluation statistics for the forward classifier
 * 
 * @author gperr
 *
 */
public class EvaluationStats {

	// Information describing the instance
	private int numResources;			// number of resources in this execution
	private double dbMaxX;				// maximum value for X
	private double dbMaxY;				// maximum value for Y
	
	// information on the trained classifier
	private String stDSName;					// Dataset name
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
	 * @return the dbMaxX
	 */
	public double getDbMaxX() {
		return dbMaxX;
	}

	/**
	 * @param dbMaxX the dbMaxX to set
	 */
	public void setDbMaxX(double dbMaxX) {
		this.dbMaxX = dbMaxX;
	}

	/**
	 * @return the dbMaxY
	 */
	public double getDbMaxY() {
		return dbMaxY;
	}

	/**
	 * @param dbMaxY the dbMaxY to set
	 */
	public void setDbMaxY(double dbMaxY) {
		this.dbMaxY = dbMaxY;
	}

	/**
	 * @return the dbPrecision
	 */
	public double getDbPrecision() {
		return dbPrecision;
	}

	/**
	 * @param dbPrecision the dbPrecision to set
	 */
	public void setDbPrecision(double dbPrecision) {
		this.dbPrecision = dbPrecision;
	}

	/**
	 * @return the dbRecall
	 */
	public double getDbRecall() {
		return dbRecall;
	}

	/**
	 * @param dbRecall the dbRecall to set
	 */
	public void setDbRecall(double dbRecall) {
		this.dbRecall = dbRecall;
	}

	/**
	 * @return the dbAbsCorrectlyClassified
	 */
	public double getDbAbsCorrectlyClassified() {
		return dbAbsCorrectlyClassified;
	}

	/**
	 * @param dbAbsCorrectlyClassified the dbAbsCorrectlyClassified to set
	 */
	public void setDbAbsCorrectlyClassified(double dbAbsCorrectlyClassified) {
		this.dbAbsCorrectlyClassified = dbAbsCorrectlyClassified;
	}

	/**
	 * @return the dbPerCorrectlyClassified
	 */
	public double getDbPerCorrectlyClassified() {
		return dbPerCorrectlyClassified;
	}

	/**
	 * @param dbPerCorrectlyClassified the dbPerCorrectlyClassified to set
	 */
	public void setDbPerCorrectlyClassified(double dbPerCorrectlyClassified) {
		this.dbPerCorrectlyClassified = dbPerCorrectlyClassified;
	}

	/**
	 * @return the dbAbsUncorrectlyClassified
	 */
	public double getDbAbsUncorrectlyClassified() {
		return dbAbsUncorrectlyClassified;
	}

	/**
	 * @param dbAbsUncorrectlyClassified the dbAbsUncorrectlyClassified to set
	 */
	public void setDbAbsUncorrectlyClassified(double dbAbsUncorrectlyClassified) {
		this.dbAbsUncorrectlyClassified = dbAbsUncorrectlyClassified;
	}

	/**
	 * @return the dbPerUncorrectlyClassified
	 */
	public double getDbPerUncorrectlyClassified() {
		return dbPerUncorrectlyClassified;
	}

	/**
	 * @param dbPerUncorrectlyClassified the dbPerUncorrectlyClassified to set
	 */
	public void setDbPerUncorrectlyClassified(double dbPerUncorrectlyClassified) {
		this.dbPerUncorrectlyClassified = dbPerUncorrectlyClassified;
	}

	/**
	 * @return the stDSName
	 */
	public String getStDSName() {
		return stDSName;
	}

	/**
	 * @param stDSName the stDSName to set
	 */
	public void setStDSName(String stDSName) {
		this.stDSName = stDSName;
	}



}
