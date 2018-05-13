/**
 * 
 */
package objects;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utils.PerroUtils;
import utils.NumericUtils;

/**
 * Class used to store and manipulate statistics used in batch executions of the model
 * 
 * @author Giovanni
 *
 */
public class TaskStats {
	
	// REM: all metrics are expressed as percentages, i.e normalized versus the maximum possible value (for distances the max dist and for time the 24 hours period)
	private int Task_id;
	private double closestDist;		// distance between this task and the closest
	private double farestDist;		// distance between this task and the farest
	private double avgDist;			// average of the distances
	private double stvDist;			// standard variation of the distances
	private double mdnDist;			// moda of the distances
	private double timWind;			// time window duration
	private double srvTime;			// service time duration
	private ArrayList<Boolean> blTWB = new ArrayList<Boolean>();	// array list of 24 bins indicating if the time window of the tasks belong to bin #i
	private double perResUnav;		// % of resources in break (available) during task's execution window
	private double perResWSkills;	// % of the resources that possess the skills required to do the job
	private int blServiced;			// specifies if the task has been serviced by the solution or not 
	


	/**
	 * Returns an ArrayList of String with the data members names
	 * 
	 * @return	ArrayList String	ArrayList of String with the data members names
	 */
	public ArrayList<String> getFieldsNamesAsArray() {
		ArrayList<String> strHeader = new ArrayList<String>();
		
		// get total number of fields for this class
		int numDM = this.getClass().getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = this.getClass().getDeclaredFields();
		
		// parse the fields and creates the return string accordingly
		for (int i = 0; i < numDM; i++ ) {
			if 	(fields[i].getGenericType().toString().contains("java.util.ArrayList<java.lang.Boolean>")) { 
				// field is an array of booleans
				for (int k = 0; k < blTWB.size(); k++)
					strHeader.add(fields[i].getName() + "[" + k + "]");
			}
			else
				strHeader.add(fields[i].getName());		
		}
		return strHeader;
	
	}
	
	

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
			if 	(fields[i].getGenericType().toString().contains("java.util.ArrayList<java.lang.Boolean>")) { 
				// field is an array of booleans
				for (int k = 0; k < blTWB.size(); k++)
					str += (fields[i].getName() + "[" + k + "]" + ";");
			}
			else
				str += (fields[i].getName() + ";");
						
		return str.substring(0, str.length()-1);
	
	}
	
	
	/**
	 * Returns an ArrayList of String with the values of all the fields for the instance
	 * 
	 * @param blUsePoint	Specifies if doubles are to be formatted with "." instead of ","
	 * @param bDoNotAddClassLabel	If set, forces to set to "?" the last data member (that actually is the class)
	 * @return ArrayList ArrayList of String with all data members values 
	 */
	public ArrayList<String> getDataMembersAsArray(boolean blUsePoint, boolean bDoNotAddClassLabel) {
		
		ArrayList<String> strData = new ArrayList<String>();
		
		if (blUsePoint)
			NumericUtils.setPointAsSep();
		else
			NumericUtils.setDefaultFormat();
		
		// get total number of fields for this class
		int numDM = this.getClass().getDeclaredFields().length;
		
		if (bDoNotAddClassLabel)			// If I have to skip the last data member then ignores last field
			numDM --;
		
		// gets the list of fields for this class
		Field[] fields = this.getClass().getDeclaredFields();
		
		try {

			// parse the fields and creates the return string accordingly
			for (int i = 0; i < numDM; i++ ) {
				
//				PerroUtils.print(" " + fields[i].getGenericType());
//				PerroUtils.print(" " + fields[i].getType());
				
				if (fields[i].getType().toString().equals("double")) 
					// field is a double
					strData.add(NumericUtils.Double2String((double) fields[i].get(this)));
					
				else if (fields[i].getGenericType().toString().contains("java.util.ArrayList<java.lang.Boolean>")) {
					// field is an array of booleans
					// tried to use reflection but it doesn't work - I will assume that I am working on blTimeWindowBins

					for (Boolean bl : blTWB) 
						if (bl.booleanValue()) 
							strData.add("1"); 							
						else
							strData.add("0");			

				} else			
					strData.add(fields[i].get(this).toString());
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bDoNotAddClassLabel)
			strData.add("?");
		
		return strData;
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
				
//				PerroUtils.print(" " + fields[i].getGenericType());
//				PerroUtils.print(" " + fields[i].getType());
				
				if (fields[i].getType().toString().equals("double")) {
					// field is a double
					str += (NumericUtils.Double2String((double) fields[i].get(this)) + ";");
					
				} else if (fields[i].getGenericType().toString().contains("java.util.ArrayList<java.lang.Boolean>")) {
					// field is an array of booleans
					// tried to use reflection but it doesn't work - I will assume that I am working on blTimeWindowBins

					for (Boolean bl : blTWB) 
						if (bl.booleanValue()) 
							str += "1" + ";"; 							
						else
							str += "0" + ";";
					
					
/*
//					Object arInst = Array.newInstance(Boolean.class, 24);
					
//					Object arInst = new (this.getBlTimeWindowBins().getClass());
					
					ArrayList<Boolean> arInst = new ArrayList<Boolean>();
											
					fields[i].setAccessible(true);

					Object value = fields[i].get(arInst);

					for (int j = 0; j < Array.getLength(value); j++) 
						if (Array.getBoolean(value, j)) 
							str += "1" + ";"; 							
						else
							str += "0" + ";";
*/							

				} else			
					str += (fields[i].get(this) + ";");			
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return str.substring(0, str.length()-1);
	}

	/**
	 * @return the task_id
	 */
	public int getTask_id() {
		return Task_id;
	}

	/**
	 * @param task_id the task_id to set
	 */
	public void setTask_id(int task_id) {
		Task_id = task_id;
	}

	/**
	 * @return the closestDist
	 */
	public double getClosestDist() {
		return closestDist;
	}

	/**
	 * @param closestDist the closestDist to set
	 */
	public void setClosestDist(double closestDist) {
		this.closestDist = closestDist;
	}

	/**
	 * @return the farestDist
	 */
	public double getFarestDist() {
		return farestDist;
	}

	/**
	 * @param farestDist the farestDist to set
	 */
	public void setFarestDist(double farestDist) {
		this.farestDist = farestDist;
	}

	/**
	 * @return the avgDist
	 */
	public double getAvgDist() {
		return avgDist;
	}

	/**
	 * @param avgDist the avgDist to set
	 */
	public void setAvgDist(double avgDist) {
		this.avgDist = avgDist;
	}

	/**
	 * @return the stvDist
	 */
	public double getStvDist() {
		return stvDist;
	}

	/**
	 * @param stvDist the stvDist to set
	 */
	public void setStvDist(double stvDist) {
		this.stvDist = stvDist;
	}

	/**
	 * @return the mdnDist
	 */
	public double getMdnDist() {
		return mdnDist;
	}

	/**
	 * @param mdnDist the mdnDist to set
	 */
	public void setMdnDist(double mdnDist) {
		this.mdnDist = mdnDist;
	}

	/**
	 * @return the timWind
	 */
	public double getTimWind() {
		return timWind;
	}

	/**
	 * @param timWind the timWind to set
	 */
	public void setTimWind(double timWind) {
		this.timWind = timWind;
	}

	/*

	public boolean[] getBlTimeWind() {
		return blTimeWind;
	}

	public void setBlTimeWind(boolean[] blTimeWind) {
		this.blTimeWind = blTimeWind;
	}
*/
	/**
	 * @return the perResAv
	 */
	public double getPerResAv() {
		return perResUnav;
	}

	/**
	 * @param perResAv the perResAv to set
	 */
	public void setPerResAv(double perResAv) {
		this.perResUnav = perResAv;
	}

	/**
	 * @return the blTimeWindowBins
	 */
	public ArrayList<Boolean> getBlTimeWindowBins() {
		return blTWB;
	}

	/**
	 * @param blTimeWindowBins the blTimeWindowBins to set
	 */
	public void setBlTimeWindowBins(ArrayList<Boolean> blTimeWindowBins) {
		this.blTWB = blTimeWindowBins;
	}

	/**
	 * @return the blTWB
	 */
	public ArrayList<Boolean> getBlTWB() {
		return blTWB;
	}

	/**
	 * @param blTWB the blTWB to set
	 */
	public void setBlTWB(ArrayList<Boolean> blTWB) {
		this.blTWB = blTWB;
	}

	/**
	 * @return the perResUnav
	 */
	public double getPerResUnav() {
		return perResUnav;
	}

	/**
	 * @param perResUnav the perResUnav to set
	 */
	public void setPerResUnav(double perResUnav) {
		this.perResUnav = perResUnav;
	}

	/**
	 * @return the perResWSkills
	 */
	public double getPerResWSkills() {
		return perResWSkills;
	}

	/**
	 * @param perResWSkills the perResWSkills to set
	 */
	public void setPerResWSkills(double perResWSkills) {
		this.perResWSkills = perResWSkills;
	}

	/**
	 * @return the blServiced
	 */
	public int getBlServiced() {
		return blServiced;
	}

	/**
	 * @param blServiced the blServiced to set
	 */
	public void setBlServiced(int blServiced) {
		this.blServiced = blServiced;
	}



	/**
	 * @return the srvTime
	 */
	public double getSrvTime() {
		return srvTime;
	}



	/**
	 * @param srvTime the srvTime to set
	 */
	public void setSrvTime(double srvTime) {
		this.srvTime = srvTime;
	}


	
	

}
