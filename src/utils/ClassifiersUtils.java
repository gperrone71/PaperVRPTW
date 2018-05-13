/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;


import objects.ClassifierStats;
import objects.PruningCompareStats;

/**
 * Class that contains methods used by the classes in the classifier package
 * 
 * @author gperr
 *
 */
public class ClassifiersUtils {
	
	/**
	 * Generates a CSV file from the private SolStats list (containing statistics for solver's executions) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 *  @param strFullPath full path (inclusive of final "/") where the csv file has to be written
	 *  @param strNameOfBatch name of the back configuration file for which the statistics have been generated
	 */
	public static void classifierStatsToCSV(boolean prtOnScreen, String strFullPath, String strNameOfBatch, ArrayList<ClassifierStats> lstInternalClassStatList) {
		
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		ClassifierStats tmp = lstInternalClassStatList.get(0);		// take the first object of the list in order to be sure that all fields are populated

		if (prtOnScreen)
			PerroUtils.print(tmp.getHeaderString());
		
		strList.add(tmp.getHeaderString());
		
		String strFullFileName = strFullPath+strNameOfBatch+"_stats.csv";
		
		if (prtOnScreen)
			PerroUtils.print("Writing to file: "+ strFullFileName);
		for (ClassifierStats tmp1 : lstInternalClassStatList) {
			if (prtOnScreen) 
				PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV(strFullFileName, strList);
	}

	
	/**
	 * Generates a CSV file from the private PruningCompareStats list (containing statistics to compare results on different pruning) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 *  @param strFullPath full path (inclusive of final "/") where the csv file has to be written
	 *  @param strNameOfBatch name of the back configuration file for which the statistics have been generated
	 */
	public static void pruningStatsToCSV(boolean prtOnScreen, String strFullPath, String strNameOfBatch, ArrayList<PruningCompareStats> lstInternalClassStatList) {
		
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		PruningCompareStats tmp = lstInternalClassStatList.get(0);		// take the first object of the list in order to be sure that all fields are populated

		if (prtOnScreen)
			PerroUtils.print(tmp.getHeaderString());
		
		strList.add(tmp.getHeaderString());
		
		String strFullFileName = strFullPath+strNameOfBatch+"_stats.csv";
		
		if (prtOnScreen)
			PerroUtils.print("Writing to file: "+ strFullFileName);
		for (PruningCompareStats tmp1 : lstInternalClassStatList) {
			if (prtOnScreen) 
				PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV(strFullFileName, strList);
	}

	
	
	/**
	 * Appends the statistics information contained in the ArrayList passed as parameter to an SQL database. SQL server has to be running locally on the deafault port
	 * Credentials and table to be used are hard-coded into the method (for the moment)
	 * 12/01/18
	 * 
	 * @param prtOnScreen specifies if the output has to be printed on screen or not (for debug purposes)
	 * @param lstInternalClassStatList ArrayList with the objects to be written on the db
	 * 
	 * @return boolean True if everything went fine, false otherwise
	 */
	public static boolean classifierStatsToDB(boolean prtOnScreen, ArrayList<ClassifierStats> lstInternalClassStatList ) {

		// jdbc driver and database URL
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost:3306/ClassifierResults";
		
		String User = "root";
		String Pwd = "pippo";
		/*	
		
		try { 
            Connection conn = DriverManager.getConnection(DB_URL, User, Pwd); 
            Statement st = conn.createStatement();
		  String query = "INSERT INTO Users ("
		    + " user_id,"
		    + " username,"
		    + " firstname,"
		    + " lastname,"
		    + " companyname,"
		    + " email_addr,"
		    + " want_privacy ) VALUES ("
		    + "null, ?, ?, ?, ?, ?, ?)";

		  try {
		    // set all the preparedstatement parameters
		    PreparedStatement st = conn.prepareStatement(query);
		    st.setString(1, user.getName());
		    st.setString(2, user.getFirstName());
		    st.setString(3, user.getLastName());
		    st.setString(4, user.getCompanyName());
		    st.setString(5, user.getEmail());
		    st.setString(6, user.getPrivacy());

		    // execute the preparedstatement insert
		    st.executeUpdate();
		    st.close();
		  } 
		  catch (SQLException se)
		  {
		    // log exception
		    throw se;
		  }
		}
	*/	
		return false;
		
	}

}
