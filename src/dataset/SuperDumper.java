/**
 * 
 */
package dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import classifier.BatchClassifier;
import parsers.GenerateWorldFromXML;
import utils.PerroUtils;
import objects.*;


/**
 * Class used to dump contents of all xml files in a folder in CSV files in a separate folder + a single csv with all instances
 * 
 * @author gperr
 *
 */
public class SuperDumper {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		List<Task> lstTsk = new ArrayList<Task>();
		List<Resource> lstRes = new ArrayList<Resource>();
		
		String strMainPath = "output/";
		// main loop: find all directories
		File filObj = new File(strMainPath);
		
		// loops into all directories found into strMainPath
		for (final File fileInMainDir : filObj.listFiles()) {
			if (fileInMainDir.isDirectory()) {
				String strPath = fileInMainDir.toString();
				String strFullPath = strPath + "/CSV";
				
				lstRes.clear();
				lstTsk.clear();
				
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

				// attempt to launch a batch for all files in the "/batch" directory
				final File filInnerObject = new File(strPath);
				PerroUtils.print(filInnerObject.getPath());
				
				final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("XML", "xml");

				// print list of batch jobs
				for (final File fileInDir : filInnerObject.listFiles()) {
					if ( extensionFilter.accept(fileInDir) ) 
						if (!fileInDir.isDirectory()) 
							PerroUtils.print(fileInDir.getName());
				}

				String strFileName = "";
				// starts the main loop and creates the dumps
				for (final File fileInDir : filInnerObject.listFiles()) {
					if ( extensionFilter.accept(fileInDir) ) 
						if (!fileInDir.isDirectory() && !fileInDir.getName().contains("_PRUNED")) {
							GenerateWorldFromXML tmpPrs = new GenerateWorldFromXML();
							tmpPrs.ReadDatasetFile(strPath + "/", fileInDir.getName());
							strFileName = fileInDir.getName().substring(0, fileInDir.getName().indexOf('.'));
							PerroUtils.fromListToCSV(tmpPrs.getlstResources(), strFullPath + "RES_" + strFileName + ".csv");
							PerroUtils.fromListToCSV(tmpPrs.getlstTasks(), strFullPath + "TSK_" + strFileName + ".csv");
							// add in the global list all tasks and resources
							lstRes.addAll(tmpPrs.getlstResources());
							lstTsk.addAll(tmpPrs.getlstTasks());
						}
				}
				// dump the complete lists
				PerroUtils.fromListToCSV(lstRes, strFullPath + "RES_ALL.csv");
				PerroUtils.fromListToCSV(lstTsk, strFullPath + "TSK_ALL.csv");

			}
		}

	}

}
