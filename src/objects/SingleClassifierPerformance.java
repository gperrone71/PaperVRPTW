/**
 * 
 */
package objects;

import java.lang.reflect.Field;

import utils.NumericUtils;

/**
 * Class used to store results of the comparison between classifiers
 * 
 * @author giovanni_perrone
 *
 */
public class SingleClassifierPerformance {
	
	private String strClassifierName;
	private long lTimeForTraining;		// time required for the training phase
	private double dbPrecision;
	private double dbRecall;
	private double dbAccuracy;
	private double[][] dbConfMatrix;
	
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
			if (fields[i].getType().isArray())
				// this is the array for the confusion matrix
				for (int row = 0; row <= 1; row++)
					for (int col = 0; col <= 1; col++)
						str += this.strClassifierName + "." + fields[i].getName() + "["+row+"]["+col+"];";
			else
				str += this.strClassifierName + "." + (fields[i].getName() + ";");
						
		return  str.substring(0, str.length()-1);
	
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
				
				else if (fields[i].getType().isArray()) {		// the field is the array of the confusion matrix
				
					double[][] tmpAr = (double[][]) fields[i].get(this);
					
					for (int row = 0; row <= 1; row++)
						for (int col = 0; col <= 1; col++)
							str += (tmpAr[row][col] + ";").replace('.', ',');

				}
				else		// for all other cases just write the value and replace all commas with dots 
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

	public double getDbAccuracy() {
		return dbAccuracy;
	}

	public void setDbAccuracy(double dbAccuracy) {
		this.dbAccuracy = dbAccuracy;
	}

	public double[][] getDbConfMatrix() {
		return dbConfMatrix;
	}

	public void setDbConfMatrix(double[][] dbConfMatrix) {
		this.dbConfMatrix = dbConfMatrix;
	}

	public String getStrClassifierName() {
		return strClassifierName;
	}

	public void setStrClassifierName(String strClassifierName) {
		this.strClassifierName = strClassifierName;
	}

	public long getlTimeForTraining() {
		return lTimeForTraining;
	}

	public void setlTimeForTraining(long lTimeForTraining) {
		this.lTimeForTraining = lTimeForTraining;
	}


	

}
