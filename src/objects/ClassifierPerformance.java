/**
 * 
 */
package objects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import utils.NumericUtils;

/**
 * @author giovanni_perrone
 *
 */
public class ClassifierPerformance {
	
	// information on date of execution
	private String strTimeStampDay;		// Timestamp (short version w/ only dates) 

	// Information describing the instance
	private String strInstanceName;		// "name" of the instance, i.e. its file name
	private String strDSType;			// Type of the dataset (RC, RU, C)
	private int numClusters;			// number of clusters in the ds

	// ArrayList with the information on the classifiers
	ArrayList<SingleClassifierPerformance> lstClsPerf = new ArrayList<SingleClassifierPerformance>();

	
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
			if ((Collection.class.isAssignableFrom(fields[i].getType())))
				try {	// it's a list
					@SuppressWarnings("unchecked")
					ArrayList<SingleClassifierPerformance> tmp = (ArrayList<SingleClassifierPerformance>) fields[i].get(this);
					for (SingleClassifierPerformance tmpSCPel : tmp)
						str += tmpSCPel.getHeaderString() + ";";
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else	
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
				
				else if ((Collection.class.isAssignableFrom(fields[i].getType())))
					try {	// it's a list
						@SuppressWarnings("unchecked")
						ArrayList<SingleClassifierPerformance> tmp = (ArrayList<SingleClassifierPerformance>) fields[i].get(this);
						for (SingleClassifierPerformance tmpSCPel : tmp)
							str += tmpSCPel.toString() + ";";
						
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	public String getStrDSType() {
		return strDSType;
	}

	public void setStrDSType(String strDSType) {
		this.strDSType = strDSType;
	}

	public ArrayList<SingleClassifierPerformance> getLstClsPerf() {
		return lstClsPerf;
	}

	public void setLstClsPerf(ArrayList<SingleClassifierPerformance> lstClsPerf) {
		this.lstClsPerf = lstClsPerf;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

	
}
