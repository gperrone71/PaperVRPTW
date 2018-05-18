package launchers;

/**
 * 
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;

import classifier.BatchClassifier;
import objects.*;
import dataset.*;
import problem.*;
import utils.*;

/**
 * Class used to manage batch executions of georef jobs: 
 * 1) generation of a georef dataset with possible modifiers
 * 2) execution of the solver
 * 3) generation of the statistics and relevant arff files 
 *
 * Based on BatchLauncher.java
 * 
 * Could be optimized to separate some of the common portions of code but for the moment I keep like it is :)
 * 
 * @author gperr
 *
 */
public class BatchGeoRefLauncher {

	private ArrayList<GeoRefConfig> lstConfigObj = new ArrayList<GeoRefConfig>();
	ArrayList<SolStats> lstSolStats = new ArrayList<SolStats>();			// list used to store the results of the solver's execution
	
	/**
	 * Executes a batch job using configuration parameters found in the XML file whose name is passed as an argument
	 * 
	 * Essentially:
	 * 1) based on the parameters in the batch file, create a dataset
	 * 2) solve the dataset
	 * 3) generate an ARFF file that includes the solution and that can be used with the classifier
	 * 
	 * @param strConfigFileName	Name of the xml file to be used for the execution of the batch jobs
	 */
	public boolean NewBatchJob(String strConfigFileName) {

		// List used to parse the xml file with the configuration items 
		List<String> lstString = new ArrayList<String>();	
		
		// first of all I check if there is a directory with the batch name or if I have to create it
		
		String strFullPath = returnBatchNameFromFileName(strConfigFileName);
			
		File dir = new File(strFullPath);
		// if the directory does not exist, create it
		if (!dir.exists()) {
			PerroUtils.print("creating directory " + strFullPath, true);
			boolean result = dir.mkdir();  
			if(result) 	PerroUtils.print(strFullPath + " created ok", true);  
		} else 
			// directory exists : wipe all contents
			try {
				FileUtils.cleanDirectory(new File(strFullPath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		strFullPath += "/";		// add a final "/" to ensure String represents a real path
		
		// generates a new xml stream
		XStream xstream = new XStream();

		// security permissions for XStream
		
		// clear out existing permissions and set own ones
		xstream.addPermission(NoTypePermission.NONE);
		xstream.allowTypeHierarchy(Task.class);
		xstream.allowTypeHierarchy(Node.class);
		xstream.allowTypeHierarchy(Resource.class);
		xstream.allowTypeHierarchy(GeoRefConfig.class);

		// all lines from files are read and put in an arraylist
		lstString = PerroUtils.getFileToList(strConfigFileName);
		// exit if something went wrong
		if (lstString == null)
			return false;

		// Read the xml configuration file and populates the list of the jobs to be executed
		PerroUtils.print("-BATCH LAUNCHER--------------------------------------------------------------------");
		PerroUtils.print(" Config file   : " + strConfigFileName);
	
		int iStartRow = 0;
		String str = "";
		
		do {
			str = XMLUtils.returnNextXMLObject(lstString, GeoRefConfig.class, iStartRow);
			iStartRow = XMLUtils.getiEndRow();
			if (str != "") {
				// creates a new object and add it to the list		
				GeoRefConfig tmpCObj = (GeoRefConfig)xstream.fromXML(str);
				// e lo aggiungo alla lista
				lstConfigObj.add(tmpCObj);
			}
				
		} while ( (str != "") && (iStartRow < lstString.size()) ); 
			
		PerroUtils.print(" Loaded " + lstConfigObj.size() + " configuration items");

		int iInstancesCounter = 0;
		
		// main loop		
		for (GeoRefConfig batchObj : lstConfigObj) {
			
			int iBatchObj = lstConfigObj.indexOf(batchObj);
			
			PerroUtils.print("** START OF BATCH # " + iBatchObj + " -------------------------------------------------------------");

			int iNumInstancesOfThisKind = batchObj.getnNumInstances();
			if (iNumInstancesOfThisKind == 0)
				iNumInstancesOfThisKind++;

			PerroUtils.print("Generating " + iNumInstancesOfThisKind + " instances for this item.");
			
			// repeat the loop per the number of repetitions specified in the xml file
			for (int iRep = 0; iRep < iNumInstancesOfThisKind; iRep++) {
				
				// first of all, let's generate a georef dataset using the current parameters based on the specified file
				GenerateGeoRefDataSet dsGenerator = new GenerateGeoRefDataSet("georef/resources/", "it.txt");		
				dsGenerator.GenerateDS(strFullPath, "_" + iInstancesCounter , batchObj, false);
				
				// then, let's apply a solver to it
				Solver1 problemSolver = new Solver1(dsGenerator.getStrDataSetPath(), dsGenerator.getStrDataSetFileName());
				
				// generate a temp SolStats object
				SolStats tmp = new SolStats();
				
				// launch the solver and stores the results (threads always set to 24 by default)
				tmp = problemSolver.launchSolver(false, true, batchObj.isbResReturnToStart(), 24, strFullPath);
				
				// sets the other variables by using the method to calc maxX and maxY in the solver
				problemSolver.calcMaxAndDensity();
				tmp.setDbMaxX(problemSolver.getDbMaxX());
				tmp.setDbMaxY(problemSolver.getDbMaxY());
				tmp.setDbTasksDensity(problemSolver.getDbTskDens());
				
				// and add the stats object to the list
				lstSolStats.add(tmp);
				
				// then generate statistics
				problemSolver.generateStats();
				// and the relevant files
				problemSolver.generateCSV(false);
				problemSolver.generateARFF(strFullPath, false, false);		// generate in any case the full arff file (including class labels)
				
				iInstancesCounter++;
			}
			
			PerroUtils.print("** BATCH # " + iBatchObj + " COMPLETED -----------------------------------------");
			
		}
		
		generateCSV(false, strConfigFileName.substring(strConfigFileName.lastIndexOf('/')));			// generate a CSV file with the stats for the solver's executions

		PerroUtils.print("** END OF BATCH ** -- generated " + (iInstancesCounter) + " instances --------------------------------");

		return true;
	}
	
	/**
	 * Generates a CSV file from the private SolStats list (containing statistics for solver's executions) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 */
	public void generateCSV(boolean prtOnScreen, String strNameOfBatch) {
		
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		SolStats tmp = lstSolStats.get(0);		// take the first object of the list in order to be sure that all fields are populated

		if (prtOnScreen)
			PerroUtils.print(tmp.getHeaderString());
		
		strList.add(tmp.getHeaderString());
		
		for (SolStats tmp1 : lstSolStats) {
			if (prtOnScreen) 
				PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV("output/"+strNameOfBatch+"_stats.csv", strList);
	}

	
	/**
	 * Returns the path for the output of the batch files from a String in the format path/file.xml by taking the filename and returning it without the extension
	 * 
	 * @param strFileName
	 * @return String with path in the format "output/"+name of the batch file without ".xml"
	 */
	public static String returnBatchNameFromFileName(String strFileName) {
		// get only the path by stripping the path preceding the file name - to ensure resilience if there is no path I need to add it
		String strPathToStrip = "";
		if (strFileName.indexOf('/') == -1)
			strPathToStrip = "/" + strFileName;
		else 
			strPathToStrip = strFileName;
		
		return "output/"+strFileName.substring(strPathToStrip.lastIndexOf('/')+1, strPathToStrip.indexOf('.'));
	}


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// attempt to launch a batch for all files in the "/batch" directory
		final File filObj = new File("georef/resources/batch");
		PerroUtils.print(filObj.getPath());
		
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("XML", "xml");

		// print list of batch jobs
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) 
					PerroUtils.print(fileInDir.getName());
		}

		// starts the main loop
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) {			
					String strBatchName = "georef/resources/batch/"+ fileInDir.getName();
					PerroUtils.print("Launching batch job " + strBatchName);
					BatchGeoRefLauncher tmp2 = new BatchGeoRefLauncher();		
					tmp2.NewBatchJob(strBatchName);
				}
		}

	}

}


