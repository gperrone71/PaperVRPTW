/**
 * ARFF Merger 
 */
package utils;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import classifier.BatchClassifier;
import parsers.GenerateWorldFromXML;
import utils.PerroUtils;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import objects.*;


/**
 * Class used to merge all the .arff files for a given dataset in a single arff file
 * 
 * @author gperr
 *
 */
public class ArffMerger {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		String strMainPath = "output/";
		// main loop: find all directories
		final File filObj = new File(strMainPath);
		
		// loops into all directories found into strMainPath
		for (final File fileInMainDir : filObj.listFiles()) {
			if (fileInMainDir.isDirectory()) {
				String strPath = fileInMainDir.toString();
				PerroUtils.print("Checking folder " + strPath);
				List<String> lstFileToBeParsed = new ArrayList<String>();
				
				final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("ARFF", "arff");

				// create a list of the .arff files that are available in the selected folder
				for (final File fileInDir : fileInMainDir.listFiles()) {
					PerroUtils.print(fileInDir.getName());
					if ( extensionFilter.accept(fileInDir) ) 
						if (!fileInDir.isDirectory()) {
							PerroUtils.print(fileInDir.getName());
							lstFileToBeParsed.add(fileInDir.getName());
						}
				}


			    // uses the first dataset to build the structure for the object that will contain the various instances
		    	ArffLoader initLoader = new ArffLoader();
		    	String strFirstDataSet = strPath + "/" + lstFileToBeParsed.get(0);
		    	
		    	PerroUtils.print(" Using dataset #0 for structure generation ("+ strFirstDataSet + ")");
				try {
					initLoader.setFile(new File(strFirstDataSet));
					Instances dataTrain = initLoader.getStructure();
					dataTrain.setClassIndex(dataTrain.numAttributes() - 1);	

					// outer loop: execute per each of the .arff file stored in the file list
					for (String strObj : lstFileToBeParsed) {
					
								// retrieve the file name of the arff corresponding to the job
								String strDataSetFileName = strPath + "/" + strObj;					
								
								PerroUtils.print(" Loading #" + lstFileToBeParsed.indexOf(strObj) + " - " + strDataSetFileName);
						
						    	// loads the arff file corresponding to the current job
						    	ArffLoader loader = new ArffLoader();    	
								loader.setFile(new File(strDataSetFileName));
									
								PerroUtils.print(" +--- Adding instances...");
								Instances tmpInst = loader.getDataSet();
								tmpInst.setClassIndex(tmpInst.numAttributes()-1);
								dataTrain.addAll(tmpInst);
					}
					PerroUtils.print(" Finished loading datasets - added " + dataTrain.numInstances() + " instances.");
					ArffSaver saver = new ArffSaver();
					saver.setInstances(dataTrain);
					saver.setFile(new File(strPath+"/All_DS.arff"));
					saver.writeBatch();
					
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}

	}

}



