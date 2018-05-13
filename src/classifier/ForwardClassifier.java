/**
 * 
 */
package classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTableHeaderUI;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;

import dataset.GenerateDataSet;
import launchers.BatchLauncher;
import objects.*;
import problem.Solver1;
import utils.ClassifiersUtils;
import utils.PerroUtils;

import utils.XMLUtils;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Class implementing a test on classifier built on a set of instances and then evaluated on larger instances
 * Updated version modified for batch processing: takes all instances in the output/ folder and evaluates the classifier against all of them in batch mode
 * 
 * @author gperr
 * @version 2.0
 */
public class ForwardClassifier {

	ArrayList<EvaluationStats> lstEvaluationStats = new ArrayList<EvaluationStats>();			// list used to store the results of the solver's execution
	ArrayList<ClassifierStats> lstClassificationStats = new ArrayList<ClassifierStats>();			// list used to store the results of the solver's execution

	
	/**
	 * Builds a classifier on a given set of instances with a certain difficulty and then evaluates it on larger ones
	 * 1) Takes all 5 100x1000 instances to train the classifier
	 * 2) Evaluate the classifier on 5 200x2000 instances (and stores the results)
	 * 3) Creates pruned datasets based on all 200x2000 instances using the classifier and then solves them 
	 *  	
	 */
	public void executeForwardClassifier() {

		String strPathForTraining = "output/Batch100_1000_d01";		// this is the set of instances to be used for training the classifier

//		String strPathToBeEvaluated = "output/Batch200_2000_d01";
		
		List<Task> lstTasks = new ArrayList<Task>();
		List<Resource> lstResources = new ArrayList<Resource>();

		
		PerroUtils.print("\n-FORWARD CLASSIFIER----------------------------------------------------------------");
		PerroUtils.print("\nStarting training phase on " + strPathForTraining + " d = 0.1 instances.");

		// Step 1) create a classifier and train it using all instances in the Batch100_1000_d01 folder
		
		// generate a new AttributeSelectedClassifier
	    AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
	    
	    try {
		    	
		    // generate a new ARFF loader to generate the structure of the instances set
	    	ArffLoader initLoader = new ArffLoader();   	
			initLoader.setFile(new File(strPathForTraining + "/DS_1000_100_batch0_stats.arff"));		// at least the first file must exist
			Instances dataTrain = initLoader.getStructure();
			dataTrain.setClassIndex(dataTrain.numAttributes() - 1);	
	
			// loads all the arff files in the strPathForTraining folder into the dataTrain set of instances
			final File filObj = new File(strPathForTraining);	
	
			// print list of batch jobs
			for (final File fileInDir : filObj.listFiles()) 
				if (fileInDir.getName().contains("_stats.arff")) {
	
					String strArffFileName = strPathForTraining + "/" + fileInDir.getName();
					
					PerroUtils.print("Adding " + strArffFileName);
					
			    	// loads the arff file corresponding to the current job
			    	ArffLoader loader = new ArffLoader();    	
					loader.setFile(new File(strArffFileName));
					
					Instances tmpInst = loader.getDataSet();			// loads the dataset
					tmpInst.setClassIndex(tmpInst.numAttributes()-1);
					dataTrain.addAll(tmpInst);							// and copies the instances in the main set of instances
					
				}
	
			PerroUtils.print(" Finished loading datasets - added " + dataTrain.numInstances() + " instances.");
	
			// end of main loop: I have all instances but the test set loaded and can build the classifier
			classifier.buildClassifier(dataTrain);
		
			// ***Step 2 and 3 outer loop ***
			
			final File filFolder = new File("output");
			String strFolderInEvaluation = "";
			
			// extracts list of folders in the "output" folder and per each folder launches steps 2 and 3, repeating evaluation
			for (final File filFolderCheck: filFolder.listFiles()) {
				if (filFolderCheck.isDirectory()) 
					// skip the folder used to train the classifier
					if (!filFolderCheck.getName().equals(strPathForTraining.substring(strPathForTraining.indexOf('/')+1))) {
						strFolderInEvaluation = "output/" + filFolderCheck.getName();
						PerroUtils.print("\n== OUTER LOOP =================================================");
						PerroUtils.print("Entering folder    : " + strFolderInEvaluation, true);
						
						
						// Step 2) evaluate the classifier on all instances stored in the strPathToBeEvaluated string variable					
						// loads all the arff files in the strPathForTraining folder into the dataTrain set of instances
						final File filObj2 = new File(strFolderInEvaluation);	
				
						// main loop
						int iNumFile = 0;
						for (final File fileInDir : filObj2.listFiles()) {
							if (fileInDir.getName().contains("_stats.arff")) {
				
								String strPathToBeEvaluated = strFolderInEvaluation;

								String fileNameTestSet = strPathToBeEvaluated + "/" + fileInDir.getName();
								PerroUtils.print("Evaluating against : " + fileNameTestSet, true);
				
								// loads the data set for evaluation
								ArffLoader testLoader = new ArffLoader();
								testLoader.setFile(new File(fileNameTestSet));
								Instances testSet = testLoader.getDataSet();
								testSet.setClassIndex(testSet.numAttributes() - 1);
								
								// start evaluation
								Evaluation eval = new Evaluation(dataTrain);
								eval.evaluateModel(classifier, testSet);
								
								// print statistics
								System.out.println(eval.toSummaryString("\nResults\n======\n", false));
					
								// Print some statistics on evaluation
								for (int i = 0; i <= 1; i++) {
									PerroUtils.print("Class " + i +": #of True Positives "+eval.numTruePositives(i) + " # of False Positives " + eval.numFalsePositives(i));
								}
								
								double confMatr[][] = eval.confusionMatrix();
								
								PerroUtils.print("\nConfusion matrix\n----------------");
								PerroUtils.print("           0     1");
								for (int i = 0; i <= 1; i++) {
									System.out.print("Class " + i + " : ");
									for (int j = 0; j <= 1; j++)
										System.out.print("["+ confMatr[i][j] + "]");
									System.out.println("");
								}
								
								PerroUtils.print("\n" + eval.toClassDetailsString());
								
								// stores the evaluation result in a list
							    // generate a temp ClassifierStats object and starts populating it
								EvaluationStats tmpEvalStat = new EvaluationStats();
							    
							    tmpEvalStat.setNumResources(2000);
				
							    tmpEvalStat.setStDSName(fileNameTestSet);
							    
							    // add the stats and information on the model
								tmpEvalStat.setDbPrecision(eval.weightedPrecision());
								tmpEvalStat.setDbRecall(eval.weightedRecall());
								tmpEvalStat.setDbAbsCorrectlyClassified(eval.correct());
								tmpEvalStat.setDbPerCorrectlyClassified(eval.pctCorrect());
								tmpEvalStat.setDbAbsUncorrectlyClassified(eval.incorrect());
								tmpEvalStat.setDbPerUncorrectlyClassified(eval.pctIncorrect());
								
								// add the temp object to the list
								lstEvaluationStats.add(tmpEvalStat);

								// STEP 3): using the model trained on 100x1000 dataset generate pruned versions of the xml file and solve them and then store the results 
								
								// now with the trained model I generate a new dataset for the test set to be solved separately
								// first of all I will load the non-classified version of the test set

						    	String strEmptyDSName = returnFullFileNameWOExtension(fileNameTestSet) + ".arff";
								PerroUtils.print("*** Loading empty test set with filename :" + strEmptyDSName, true);
						    	
								ArffLoader emptyDSLoader = new ArffLoader();
						    	emptyDSLoader.setFile(new File(strEmptyDSName));
						    	Instances unlabeledTS = emptyDSLoader.getDataSet();
								unlabeledTS.setClassIndex(unlabeledTS.numAttributes() - 1);
							
								
						    	// then I have to load in memory the dataset in xml format corresponding to the test set
						    	// first of all I have to extract paths and filenames of the xml dataset
						    	String strFullName = fileNameTestSet.substring(0,  fileNameTestSet.indexOf("_stats")) + ".xml";
						    	strPathToBeEvaluated = strFullName.substring(0, strFullName.lastIndexOf('/') + 1);
						    	String strXMLFileName = strFullName.substring(strFullName.lastIndexOf('/') + 1);
						    	
						    	// finally I can load the xml file and populate the lists
								Solver1 problemSolver = new Solver1(strPathToBeEvaluated, strXMLFileName);
								lstTasks = problemSolver.getLstTasks();
								lstResources = problemSolver.getLstResources();
						
								// init local variables
					    		List<Task> lstPrunedTasks = new ArrayList<Task>();
					    		List<Resource> lstPrunedResources = new ArrayList<Resource>();
					    		
								// copy the resources in the pruned version (it won't change)
								lstPrunedResources = lstResources;
					
								// populate the pruned tasks only with the tasks that are classified as schedulable
							    for (int i = 0; i < unlabeledTS.numInstances(); i++) {
							    	double clsLabel = classifier.classifyInstance(unlabeledTS.instance(i));
							    	if (clsLabel == 1)
							    		lstPrunedTasks.add(lstTasks.get(i));	    		
							    }
							    
							    PerroUtils.print("*** Created pruned dataset with " + lstPrunedTasks.size() + " tasks.", true);
							    
							    // write the XML file on disk
							    GenerateDataSet tmpGDS = new GenerateDataSet();
							    String strPrunedXMLFileName = tmpGDS.WriteDataSetOnFile(lstPrunedTasks, lstPrunedResources, "fwd_output/" , "_PRUNED_" + iNumFile++);
							    
							    // generate a temp ClassifierStats object and starts populating it
							    ClassifierStats tmpClassStat = new ClassifierStats();
							    tmpClassStat.setNumResources(lstResources.size());
								tmpClassStat.setiNumThreads(24);
						    
							    // before launching the solver calculates maxX, maxY and tasks densities and store the info into the stats object
							    problemSolver.calcMaxAndDensity();
							    tmpClassStat.setDbMaxX(problemSolver.getDbMaxX());
								tmpClassStat.setDbMaxY(problemSolver.getDbMaxY());
								tmpClassStat.setDbTasksDensity_UP(problemSolver.getDbTskDens());
							    
								// launch the solver and store the results into a temporary stats object
							    SolStats tmpSolStat = new SolStats();		    	    
								tmpSolStat = problemSolver.launchSolver(false, true, 24, "fwd_output/");

							    // generate the information on timestamp and hash
							    tmpClassStat.setStrFullTimeStamp(new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date()));
							    tmpClassStat.setStrTimeStampDay(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
							    
							    // store the file name of the instance
							    tmpClassStat.setStrInstanceName(strXMLFileName);
							    
							    // generate the hash for this instance and save it to the statistical object
							    tmpClassStat.setStrHash(PerroUtils.CRC32Calc(strFullName));
							    
							    // and copies the relevant information in the ClassifierStats object
								tmpClassStat.setNumTasks_UP(lstTasks.size());
								tmpClassStat.setNumSolutionsFound_UP(tmpSolStat.getNumSolutionsFound());
								tmpClassStat.setDblExecutionTime_UP(tmpSolStat.getDblExecutionTime());
								tmpClassStat.setiTotServiced_UP(tmpSolStat.getiTotServiced());
								tmpClassStat.setiTotUnserviced_UP(tmpSolStat.getiTotUnserviced());
								tmpClassStat.setDbTraveledDistance_UP(tmpSolStat.getDbTraveledDistance());
								tmpClassStat.setDbOperationTime_UP(tmpSolStat.getDbOperationTime());
								tmpClassStat.setDbWaitingTime_UP(tmpSolStat.getDbWaitingTime());
								tmpClassStat.setDbTotalCosts_UP(tmpSolStat.getDbTotalCosts());
								tmpClassStat.setDbTimeWinViolation_UP(tmpSolStat.getDbTimeWinViolation());
								tmpClassStat.setiNumVehiclesUsed_UP(tmpSolStat.getiNumVehiclesUsed());
								
								// generates another solver object using the pruned dataset
								Solver1 prunedProblemSolver = new Solver1("fwd_output/", strPrunedXMLFileName);
								
								// calculates density for the pruned dataset and stores it into the stats file 
								prunedProblemSolver.calcMaxAndDensity();
								tmpClassStat.setDbTasksDensity_P(prunedProblemSolver.getDbTskDens());

								// launch the solver on the pruned problem with 24 threads and stores the results
							    tmpSolStat = prunedProblemSolver.launchSolver(false, true, 24, "fwd_output/");

							    // and copies the relevant information in the ClassifierStats object in the section for the pruned dataset
								tmpClassStat.setNumTasks_P(lstPrunedTasks.size());
								tmpClassStat.setNumSolutionsFound_P(tmpSolStat.getNumSolutionsFound());
								tmpClassStat.setDblExecutionTime_P(tmpSolStat.getDblExecutionTime());
								tmpClassStat.setiTotServiced_P(tmpSolStat.getiTotServiced());
								tmpClassStat.setiTotUnserviced_P(tmpSolStat.getiTotUnserviced());
								tmpClassStat.setDbTraveledDistance_P(tmpSolStat.getDbTraveledDistance());
								tmpClassStat.setDbOperationTime_P(tmpSolStat.getDbOperationTime());
								tmpClassStat.setDbWaitingTime_P(tmpSolStat.getDbWaitingTime());
								tmpClassStat.setDbTotalCosts_P(tmpSolStat.getDbTotalCosts());
								tmpClassStat.setDbTimeWinViolation_P(tmpSolStat.getDbTimeWinViolation());
								tmpClassStat.setiNumVehiclesUsed_P(tmpSolStat.getiNumVehiclesUsed());
								
							    // add the stats and information on the model
								tmpClassStat.setDbPrecision(eval.weightedPrecision());
								tmpClassStat.setDbRecall(eval.weightedRecall());
								tmpClassStat.setDbAbsCorrectlyClassified(eval.correct());
								tmpClassStat.setDbPerCorrectlyClassified(eval.pctCorrect());
								tmpClassStat.setDbAbsUncorrectlyClassified(eval.incorrect());
								tmpClassStat.setDbPerUncorrectlyClassified(eval.pctIncorrect());
								
								// add the stats on the differences between the two executions
								double dbExecTimDiff = tmpClassStat.getDblExecutionTime_P()-tmpClassStat.getDblExecutionTime_UP();
								tmpClassStat.setDbAbsExecTimeDiff(dbExecTimDiff);
								tmpClassStat.setDbPerExecTimeDiff(dbExecTimDiff/tmpClassStat.getDblExecutionTime_UP());
								
								double dbServicedDiff = tmpClassStat.getiTotServiced_P()-tmpClassStat.getiTotServiced_UP();
								tmpClassStat.setDbAbsSrvcdTasksDiff(dbServicedDiff);
								tmpClassStat.setDbPerSrvcdTasksDiff(dbServicedDiff/tmpClassStat.getiTotServiced_UP());
								
								// add the temp object to the list
								lstClassificationStats.add(tmpClassStat);				
						} 
					}

					// writes the stats to disk
					
					evaluationStatsToCSV(false, "fwd_output/", "evaluation", lstEvaluationStats);
					ClassifiersUtils.classifierStatsToCSV(false, "fwd_output/", "forward_classification", lstClassificationStats);
					}
			}	    
	    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
					
	}				    

		

	
	/**
	 * Returns a string with the full path and file name without the extension - to be used to generate arff or xml file names
	 * 
	 * @param str String containing the full path and filename
	 * @return String string without extension
	 */
	private String returnFullFileNameWOExtension (String str) {
		return str.substring(0, str.indexOf('.'));
	}
	
	
	/**
	 * Generates a CSV file from the EvalutionStats list (containing statistics for model's evaluation statistics) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 *  @param strFullPath full path (inclusive of final "/") where the csv file has to be written
	 *  @param strNameOfBatch name of the back configuration file for which the statistics have been generated
	 */
	private static void evaluationStatsToCSV(boolean prtOnScreen, String strFullPath, String strNameOfBatch, ArrayList<EvaluationStats> lstInternalEvalClassStatList) {
		
		List<String> strList = new ArrayList<String>();
		
		// temp stats object
		EvaluationStats tmp = lstInternalEvalClassStatList.get(0);		// take the first object of the list in order to be sure that all fields are populated

		if (prtOnScreen)
			PerroUtils.print(tmp.getHeaderString());
		
		strList.add(tmp.getHeaderString());
		
		String strFullFileName = strFullPath+ strNameOfBatch+"_stats.csv";
		
		if (prtOnScreen)
			PerroUtils.print("Writing to file: "+ strFullFileName);
		for (EvaluationStats tmp1 : lstInternalEvalClassStatList) {
			if (prtOnScreen) 
				PerroUtils.print(tmp1.toString());
			strList.add(tmp1.toString());
		}
		
		PerroUtils.writeCSV(strFullFileName, strList);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ForwardClassifier tmp = new ForwardClassifier();
		
		tmp.executeForwardClassifier();

/**		
		// attempt to launch a batch for all files in the "/batch" directory
		final File filObj = new File("resources/batch");
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
					ForwardClassifier tmp = new ForwardClassifier();
					String strBatchName = "batch/"+ fileInDir.getName();
					PerroUtils.print("Launching batch job " + strBatchName);
					tmp.NewBatchJob(strBatchName);		//loads the jobs configuration
					tmp.executeBatchClassifier();				// launches the classifier
					lstFullStats.addAll(tmp.getLstClassifierStats());
				}
		}
		classifierStatsToCSV(false, "output/", "all_", lstFullStats);


		/*
		BatchClassifier tmp = new BatchClassifier();	
		tmp.NewBatchJob("batch/Batch100_1000_d01.xml");		//loads the jobs configuration
		tmp.executeBatchClassifier();				// launches the classifier
		
		BatchClassifier tmp1 = new BatchClassifier();	
		tmp1.NewBatchJob("batch/Batch100_1000_d001.xml");		//loads the jobs configuration
		tmp1.executeBatchClassifier();				// launches the classifier

		BatchClassifier tmp2 = new BatchClassifier();	
		tmp2.NewBatchJob("batch/Batch100_1000_d005.xml");		//loads the jobs configuration
		tmp2.executeBatchClassifier();				// launches the classifier
*/
	}


}
