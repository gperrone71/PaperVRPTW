/**
 * 
 */
package classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.time.*;

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
 * Class implementing a batch series of training - evaluation jobs performed following configurations described in xml config file
 * 
 * @author gperr
 *
 */
public class BatchClassifier {

	private ArrayList<BatchConfig> lstConfigObj = new ArrayList<BatchConfig>();
	private String strPath;
	
	ArrayList<ClassifierStats> lstClassifierStats = new ArrayList<ClassifierStats>();			// list used to store the results of the solver's execution
	ArrayList<PruningCompareStats> lstPruningCompareStats = new ArrayList<PruningCompareStats>(); 	// list that will handle the results of the pruning comparison

	/**
	 * Parses an XML file containing a batch job and populates the list of jobs.
	 * 
	 * @param strConfigFileName	Name of the xml file that includes the list of the jobs to be added to the batch
	 * 
	 */
	public boolean NewBatchJob(String strConfigFileName) {

		strPath = BatchLauncher.returnBatchNameFromFileName(strConfigFileName);
		
		// List used to parse the xml file with the configuration items 
		List<String> lstString = new ArrayList<String>();
		
		// generates a new xml stream
		XStream xstream = new XStream();

		// security permissions for XStream
		
		// clear out existing permissions and set own ones
		xstream.addPermission(NoTypePermission.NONE);
		xstream.allowTypeHierarchy(Task.class);
		xstream.allowTypeHierarchy(Node.class);
		xstream.allowTypeHierarchy(Resource.class);
		xstream.allowTypeHierarchy(BatchConfig.class);

		// all lines from files are read and put in an arraylist
		lstString = PerroUtils.getFileToList("resources/" + strConfigFileName);
		// exit if something went wrong
		if (lstString == null)
			return false;

		// Read the xml configuration file and populates the list of the jobs to be executed
		PerroUtils.print("-BATCH LOADER for classifier-----------------------------------------------------");
		PerroUtils.print(" Config file   : " + strConfigFileName);
	
		int iStartRow = 0;
		String str = "";
		
		do {
			str = XMLUtils.returnNextXMLObject(lstString, BatchConfig.class, iStartRow);
			iStartRow = XMLUtils.getiEndRow();
			if (str != "") {
				// creates a new object and add it to the list		
				BatchConfig tmpCObj = (BatchConfig)xstream.fromXML(str);
				// e lo aggiungo alla lista
				lstConfigObj.add(tmpCObj);
			}
				
		} while ( (str != "") && (iStartRow < lstString.size()) ); 
			
		PerroUtils.print(" Loaded " + lstConfigObj.size() + " configuration items");
		PerroUtils.print("-** END of BATCH LOADING --------------------------------------------------------");
				
		return true;
	}
	
	/**
	 * Executes the batch classification inclusive of cross-validation, that is:
	 * 1) starts setting the first dataset as to be used for evaluation and all the others for training
	 * 2) reads accordingly the instances
	 * 3) generates a model and train it on the loaded instances
	 * 4) loads the dataset to be used as test and evaluates performances of the classifier built so far on the evaluation dataset
	 * 5) generates the stats and iterates until the all the datasets have been used for evaluation
	 *  	
	 */
	public void executeBatchClassifier() {

		List<Task> lstTasks = new ArrayList<Task>();
		List<Resource> lstResources = new ArrayList<Resource>();
		List<FileParse> lstFileToBeParsed = new ArrayList<FileParse>();
		
		PerroUtils.print("\n-BATCH CLASSIFIER----------------------------------------------------------------");
		PerroUtils.print("\nStarting training phase.");

		// outer loop: loop for all ARFF files present in the specified output directory
		final File filObj = new File(strPath);
		PerroUtils.print(filObj.getPath());
		
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("ARFF", "arff");

		// create a list of the .arff files that are available in the selected folder
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) {
					PerroUtils.print(fileInDir.getName());
					FileParse tmp = new FileParse();
					tmp.setStrFileName(fileInDir.getName());
					tmp.setbTestSet(false);
					lstFileToBeParsed.add(tmp);
				}
		}
		
		// outer loop: execute per each of the .arff file stored in the file list
		for (FileParse tmpObj : lstFileToBeParsed)
			tmpObj.setbTestSet(false);		
		
		    try {
		    	
		    	for (FileParse objFileParse : lstFileToBeParsed) {
		    		// inner loop:
		    		// select the evaluation test set for the object currently loaded
		    		objFileParse.setbTestSet(true);
		    		String strDSFileNamePrefix = returnDSFileNamePrefix(objFileParse.getStrFileName());		// stores the type of DS in order to be able to use it later
					String strDSFileNameSuffix = returnFullFileNameWOExtension(objFileParse.getStrFileName());
					strDSFileNameSuffix = strDSFileNameSuffix.substring(strDSFileNameSuffix.lastIndexOf('_')+1);	// get the string from the last occurrence of "_" (without it) until the end of the string
					PerroUtils.print("** " + objFileParse.getStrFileName() + " --> " + strDSFileNameSuffix);
		    		// init variables
		    		List<Task> lstPrunedTasks = new ArrayList<Task>();
		    		List<Resource> lstPrunedResources = new ArrayList<Resource>();
	
		    		// generate a new AttributeSelectedClassifier
				    AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
				    			    
				    // uses the first dataset to build the structure for the object that will contain the various instances
			    	ArffLoader initLoader = new ArffLoader();
			    	String strFirstDataSet = strPath + "/" + lstFileToBeParsed.get(0).getStrFileName();
			    	
			    	PerroUtils.print(" Using dataset #0 for structure generation ("+ strFirstDataSet + ")");
					initLoader.setFile(new File(strFirstDataSet));
					Instances dataTrain = initLoader.getStructure();
					dataTrain.setClassIndex(dataTrain.numAttributes() - 1);	
			
				    String fileNameTestSet = "";
				    boolean bTestSetResourcesReturnToOrigin = false;
				    double maxX = 0;	    
				    double maxY = 0;
				    
					// loop for training phase 		
					for (FileParse tmpFP : lstFileToBeParsed) {
							
						// retrieve the file name of the arff corresponding to the job
						String strDataSetFileName = strPath + "/" + tmpFP.getStrFileName();					
						
						if (!tmpFP.isbTestSet()) {			// skip if the dataset has to be used for test and evaluation
							
							PerroUtils.print(" Loading #" + lstFileToBeParsed.indexOf(tmpFP) + " - " + strDataSetFileName);
				
					    	// loads the arff file corresponding to the current job
					    	ArffLoader loader = new ArffLoader();    	
							loader.setFile(new File(strDataSetFileName));
								
							PerroUtils.print(" +--- Adding instances...");
							Instances tmpInst = loader.getDataSet();
							tmpInst.setClassIndex(tmpInst.numAttributes()-1);
							dataTrain.addAll(tmpInst);
						} 
						else {
							fileNameTestSet = strDataSetFileName;
/*							bTestSetResourcesReturnToOrigin = batchObj.isbResReturnToStart();
							maxX = batchObj.getMaxX();
							maxY = batchObj.getMaxY(); */				
						}
					}
					
					PerroUtils.print(" Finished loading datasets - added " + dataTrain.numInstances() + " instances.");
					
					// end of main loop: I have all instances but the test set loaded and can build the classifier
					classifier.buildClassifier(dataTrain);
				
					// loads the test set
					PerroUtils.print("\nLoading " + fileNameTestSet + " as test set.");
					ArffLoader testLoader = new ArffLoader();
					testLoader.setFile(new File(fileNameTestSet));
					Instances testSet = testLoader.getDataSet();
					testSet.setClassIndex(testSet.numAttributes() - 1);
					
					// start evaluation
					Evaluation eval = new Evaluation(dataTrain);
					eval.evaluateModel(classifier, testSet);
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
		
					
					// now with the trained model I generate a new dataset for the test set to be solved separately
					// first of all I will load the non-classified version of the test set
			    	PerroUtils.print("*** Loading empty test set with filename :" + fileNameTestSet, true);
			    	
					ArffLoader emptyDSLoader = new ArffLoader();
			    	emptyDSLoader.setFile(new File(fileNameTestSet));
			    	Instances unlabeledTS = emptyDSLoader.getDataSet();
					unlabeledTS.setClassIndex(unlabeledTS.numAttributes() - 1);
				
					
			    	// then I have to load in memory the dataset in xml format corresponding to the test set
			    	// first of all I have to extract paths and filenames of the xml dataset
			    	String strFullName = fileNameTestSet.substring(0,  fileNameTestSet.indexOf(".")) + ".xml";
			    	String strPathTmp = strFullName.substring(0, strFullName.lastIndexOf('/') + 1);
			    	String strXMLFileName = strFullName.substring(strFullName.lastIndexOf('/') + 1);
			    	
			    	// finally I can load the xml file and populate the lists
					Solver1 problemSolver = new Solver1(strPathTmp, strXMLFileName);
					lstTasks = problemSolver.getLstTasks();
					lstResources = problemSolver.getLstResources();
		
					// and using the strings generated so far save on disk stats for the generated classifier
					// First check if the folder exists or not and if folder prep operation successful writes relevant stats
					if (PerroUtils.prepareFolder(strPathTmp + "model/", false)) {
						Charset chEnc = Charset.forName("utf-8");
						File tmpFile = new File(strPathTmp + "model/" + "model_stats_" + strDSFileNameSuffix + ".txt");
						FileUtils.writeStringToFile(tmpFile, eval.toSummaryString(), chEnc);
						FileUtils.writeStringToFile(tmpFile, eval.toClassDetailsString("\nClass Details\n"), chEnc, true);
					}
		
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
				    String strPrunedXMLFileName = tmpGDS.WriteDataSetOnFile(strDSFileNamePrefix, lstPrunedTasks, lstPrunedResources, strPathTmp , "_" + strDSFileNameSuffix + "_PRUNED" );
				    
				    // generate a temp ClassifierStats object and starts populating it
				    ClassifierStats tmpClassStat = new ClassifierStats();
				    tmpClassStat.setNumResources(lstResources.size());
					tmpClassStat.setiNumThreads(24);
	
					// launch the solver on the original problem with the specified number of threads and stores the results
					// for the original problem I want the results to be stored on disk
				    SolStats tmpSolStat = new SolStats();		    
				    tmpSolStat = problemSolver.launchSolver(false, true, bTestSetResourcesReturnToOrigin, 24, strPathTmp);

				    // calculates maxX and maxY and density
				    problemSolver.calcMaxAndDensity();
				    maxX = problemSolver.getDbMaxX();
				    maxY = problemSolver.getDbMaxY();
				    tmpClassStat.setDbMaxX(maxX);
					tmpClassStat.setDbMaxY(maxY);
				    
				    // generate the information on timestamp and hash
				    tmpClassStat.setStrFullTimeStamp(new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date()));
				    tmpClassStat.setStrTimeStampDay(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				    
				    // store the file name of the instance
				    tmpClassStat.setStrInstanceName(strPathTmp + strXMLFileName);
				    
				    // generate the hash for this instance and save it to the statistical object
				    tmpClassStat.setStrHash(PerroUtils.CRC32Calc(strPathTmp + strXMLFileName));
				    
				    // and copies the relevant information in the ClassifierStats object
					tmpClassStat.setNumTasks_UP(lstTasks.size());
					tmpClassStat.setDbTasksDensity_UP(lstTasks.size() / (maxX * maxY));
					tmpClassStat.setNumSolutionsFound_UP(tmpSolStat.getNumSolutionsFound());
					tmpClassStat.setDblExecutionTime_UP(tmpSolStat.getDblExecutionTime());
					tmpClassStat.setiTotServiced_UP(tmpSolStat.getiTotServiced());
					tmpClassStat.setiTotUnserviced_UP(tmpSolStat.getiTotUnserviced());
					tmpClassStat.setDbTraveledDistance_UP(tmpSolStat.getDbTraveledDistance());
					tmpClassStat.setDbTotalCosts_UP(tmpSolStat.getDbTotalCosts());
					tmpClassStat.setiNumVehiclesUsed_UP(tmpSolStat.getiNumVehiclesUsed());
					
					// times				
					tmpClassStat.setDbTimeWinViolation_UP(tmpSolStat.getDbTimeWinViolation());
					tmpClassStat.setDbOperationTime_UP(tmpSolStat.getDbOperationTime());
					tmpClassStat.setDbWaitingTime_UP(tmpSolStat.getDbWaitingTime());
					tmpClassStat.setDbServiceTime_UP(tmpSolStat.getDbServiceTime());
					tmpClassStat.setDbTransportTime_UP(tmpSolStat.getDbTransportTime());
					
					// generates another solver object using the pruned dataset 
					Solver1 prunedProblemSolver = new Solver1(strPathTmp, strPrunedXMLFileName);
					
					// launch the solver on the pruned problem with 24 threads and stores the results w/o storing solution results
				    tmpSolStat = prunedProblemSolver.launchSolver(false, false, bTestSetResourcesReturnToOrigin, 24, strPathTmp);
	
				    // and copies the relevant information in the ClassifierStats object in the section for the pruned dataset
					tmpClassStat.setNumTasks_P(lstPrunedTasks.size());
					tmpClassStat.setDbTasksDensity_P(lstPrunedTasks.size() / (maxX * maxY));
					tmpClassStat.setNumSolutionsFound_P(tmpSolStat.getNumSolutionsFound());
					tmpClassStat.setDblExecutionTime_P(tmpSolStat.getDblExecutionTime());
					tmpClassStat.setiTotServiced_P(tmpSolStat.getiTotServiced());
					tmpClassStat.setiTotUnserviced_P(tmpSolStat.getiTotUnserviced());
					tmpClassStat.setDbTraveledDistance_P(tmpSolStat.getDbTraveledDistance());
					tmpClassStat.setDbTotalCosts_P(tmpSolStat.getDbTotalCosts());
					tmpClassStat.setiNumVehiclesUsed_P(tmpSolStat.getiNumVehiclesUsed());
	
					// times				
					tmpClassStat.setDbTimeWinViolation_P(tmpSolStat.getDbTimeWinViolation());
					tmpClassStat.setDbOperationTime_P(tmpSolStat.getDbOperationTime());
					tmpClassStat.setDbWaitingTime_P(tmpSolStat.getDbWaitingTime());
					tmpClassStat.setDbServiceTime_P(tmpSolStat.getDbServiceTime());
					tmpClassStat.setDbTransportTime_P(tmpSolStat.getDbTransportTime());
	
					
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
					tmpClassStat.setDbPerExecTimeDiff(dbExecTimDiff/tmpClassStat.getDblExecutionTime_UP()*100);
					
					double dbServicedDiff = tmpClassStat.getiTotServiced_P()-tmpClassStat.getiTotServiced_UP();
					tmpClassStat.setDbAbsSrvcdTasksDiff(dbServicedDiff);
					tmpClassStat.setDbPerSrvcdTasksDiff(dbServicedDiff/tmpClassStat.getiTotServiced_UP()*100);
					
					// add the temp object to the list
					lstClassifierStats.add(tmpClassStat);
	
					// ** 11/05/2018 section of code to handle validation of pruning
					// this section of code does the following:
					// 1) create an object of the type PruningCompareStats
					// 2) populates it with the results obtained so far
					// 3) does a random pruning, solve the dataset and stores the results
					// 4) does a random pruning forcingly including the items that were NOT selected by the classifier and stores the results
					// 5) add the object to a list
					
					// first of all, create a new object and store the information already available
					PruningCompareStats tmpPrunCompareStats = new PruningCompareStats();
					
					tmpPrunCompareStats.setStrFullTimeStamp(tmpClassStat.getStrFullTimeStamp());
					tmpPrunCompareStats.setStrTimeStampDay(tmpClassStat.getStrTimeStampDay());
					tmpPrunCompareStats.setStrInstanceName(tmpClassStat.getStrInstanceName());
					tmpPrunCompareStats.setStrHash(tmpClassStat.getStrHash());
					tmpPrunCompareStats.setNumResources(tmpClassStat.getNumResources());
					tmpPrunCompareStats.setDbMaxX(tmpClassStat.getDbMaxX());
					tmpPrunCompareStats.setDbMaxY(tmpClassStat.getDbMaxY());
					
					tmpPrunCompareStats.setNumTasks_UP(tmpClassStat.getNumTasks_UP());
					tmpPrunCompareStats.setDblExecutionTime_UP(tmpClassStat.getDblExecutionTime_UP());
					tmpPrunCompareStats.setiTotServiced_UP(tmpClassStat.getiTotServiced_UP());
					tmpPrunCompareStats.setiTotUnserviced_UP(tmpClassStat.getiTotUnserviced_UP());
					tmpPrunCompareStats.setiNumVehiclesUsed_UP(tmpClassStat.getiNumVehiclesUsed_UP());
					tmpPrunCompareStats.setDbTimeWinViolation_UP(tmpClassStat.getDbTimeWinViolation_UP());
					tmpPrunCompareStats.setDbOperationTime_UP(tmpClassStat.getDbOperationTime_UP());
					tmpPrunCompareStats.setDbTotalCosts_UP(tmpClassStat.getDbTotalCosts_UP());
					tmpPrunCompareStats.setDbTraveledDistance_UP(tmpClassStat.getDbTraveledDistance_UP());

					tmpPrunCompareStats.setNumTasks_P(tmpClassStat.getNumTasks_P());
					tmpPrunCompareStats.setDblExecutionTime_P(tmpClassStat.getDblExecutionTime_P());
					tmpPrunCompareStats.setiTotServiced_P(tmpClassStat.getiTotServiced_P());
					tmpPrunCompareStats.setiTotUnserviced_P(tmpClassStat.getiTotUnserviced_P());
					tmpPrunCompareStats.setiNumVehiclesUsed_P(tmpClassStat.getiNumVehiclesUsed_P());
					tmpPrunCompareStats.setDbTimeWinViolation_P(tmpClassStat.getDbTimeWinViolation_P());
					tmpPrunCompareStats.setDbOperationTime_P(tmpClassStat.getDbOperationTime_P());
					tmpPrunCompareStats.setDbTotalCosts_P(tmpClassStat.getDbTotalCosts_P());
					tmpPrunCompareStats.setDbTraveledDistance_P(tmpClassStat.getDbTraveledDistance_P());

					tmpPrunCompareStats.setDbAbsExecTimeDiff(tmpClassStat.getDbAbsExecTimeDiff());
					tmpPrunCompareStats.setDbPerExecTimeDiff(tmpClassStat.getDbPerExecTimeDiff()*100);
					tmpPrunCompareStats.setDbAbsSrvcdTasksDiff(tmpClassStat.getDbAbsSrvcdTasksDiff());
					tmpPrunCompareStats.setDbPerSrvcdTasksDiff(tmpClassStat.getDbPerSrvcdTasksDiff()*100);
					
					tmpPrunCompareStats.setDbPrecision(tmpClassStat.getDbPrecision());
					tmpPrunCompareStats.setDbRecall(tmpClassStat.getDbRecall());
					tmpPrunCompareStats.setDbAbsCorrectlyClassified(tmpClassStat.getDbAbsCorrectlyClassified());
					tmpPrunCompareStats.setDbPerCorrectlyClassified(tmpClassStat.getDbPerCorrectlyClassified()*100);
					tmpPrunCompareStats.setDbAbsUncorrectlyClassified(tmpClassStat.getDbAbsUncorrectlyClassified());
					tmpPrunCompareStats.setDbPerUncorrectlyClassified(tmpClassStat.getDbAbsUncorrectlyClassified()*100);
					
					// *** RANDOM PRUNING ***
					class sortArray implements Comparator<sortArray> {
						private int index;
						private double rand;

						public int getIndex() {
							return index;
						}
						public void setIndex(int index) {
							this.index = index;
						}
						public double getRand() {
							return rand;
						}
						public void setRand(double rand) {
							this.rand = rand;
						}
					    
						public int compare(sortArray a1, sortArray a2) {
							if (a1.getRand() == a2.getRand())
								return 0;
							else
								return (a1.getRand() <= a2.getRand()) ? -1 : 1;
					    }
					}
					// create tasks lists for the rnd pruned dataset
					ArrayList<Task> lstPrRndTasks = new ArrayList<Task>();
		
					// generate a list that will be used to sort randomly the tasks
					ArrayList<sortArray> lstsrtAr = new ArrayList<sortArray>();
					for (int i = 0; i < lstTasks.size(); i++) {
						sortArray tmpsrt = new sortArray();
						tmpsrt.setIndex(i);
						tmpsrt.setRand(Math.random());
						lstsrtAr.add(tmpsrt);
					}
					
					// sort the list using the rand data type
					Collections.sort(lstsrtAr, new sortArray());
					
					// copy the tasks from the original tasks list in the order defined by the randomly sorted array until I have copied a number of tasks equal to the size of the classiier-pruned dataset
					for (int i = 0; i < lstPrunedTasks.size(); i++)
						lstPrRndTasks.add(lstTasks.get(lstsrtAr.get(i).getIndex()));
				    
				    PerroUtils.print("*** Created RND Pruned dataset with " + lstPrRndTasks.size() + " tasks.", true);
				    
				    // write the XML file on disk
				    GenerateDataSet tmpGDS_RND = new GenerateDataSet();
				    String strPrRNDXMLFileName = tmpGDS_RND.WriteDataSetOnFile(strDSFileNamePrefix, lstPrRndTasks, lstResources, strPathTmp , "_" + strDSFileNameSuffix + "_PRRND" );
				    
					// generates another solver object using the pruned dataset 
					Solver1 RNDPrunedProblemSolver = new Solver1(strPathTmp, strPrRNDXMLFileName);
					
					// launch the solver on the pruned problem with 7 threads and stores the results w/o storing solution results
				    tmpSolStat = RNDPrunedProblemSolver.launchSolver(false, false, bTestSetResourcesReturnToOrigin, 24, strPathTmp);

				    // and add the information on the execution to the stats object
				    // and copies the relevant information in the ClassifierStats object in the section for the pruned dataset
				    tmpPrunCompareStats.setNumTasks_PR1(lstPrunedTasks.size());
				    tmpPrunCompareStats.setDblExecutionTime_PR1(tmpSolStat.getDblExecutionTime());
				    tmpPrunCompareStats.setiTotServiced_PR1(tmpSolStat.getiTotServiced());
				    tmpPrunCompareStats.setiTotUnserviced_PR1(tmpSolStat.getiTotUnserviced());
				    tmpPrunCompareStats.setDbTraveledDistance_PR1(tmpSolStat.getDbTraveledDistance());
				    tmpPrunCompareStats.setDbTotalCosts_PR1(tmpSolStat.getDbTotalCosts());
				    tmpPrunCompareStats.setiNumVehiclesUsed_PR1(tmpSolStat.getiNumVehiclesUsed());
				    tmpPrunCompareStats.setDbTimeWinViolation_PR1(tmpSolStat.getDbTimeWinViolation());
				    tmpPrunCompareStats.setDbOperationTime_PR1(tmpSolStat.getDbOperationTime());
					// add the stats on the differences between the two executions
					dbExecTimDiff = tmpPrunCompareStats.getDblExecutionTime_PR1()-tmpPrunCompareStats.getDblExecutionTime_UP();
					tmpPrunCompareStats.setDbAbsExecTimeDiff_PR1(dbExecTimDiff);
					tmpPrunCompareStats.setDbPerExecTimeDiff_PR1(dbExecTimDiff/tmpPrunCompareStats.getDblExecutionTime_UP()*100);
					
					dbServicedDiff = tmpPrunCompareStats.getiTotServiced_PR1()-tmpPrunCompareStats.getiTotServiced_UP();
					tmpPrunCompareStats.setDbAbsSrvcdTasksDiff_PR1(dbServicedDiff);
					tmpPrunCompareStats.setDbPerSrvcdTasksDiff_PR1(dbServicedDiff/tmpPrunCompareStats.getiTotServiced_UP()*100);

					// *** RANDOM PRUNING FROM NON SELECTED TASKS ***
					// create tasks lists for the rnd pruned dataset
					ArrayList<Task> lstPrRnd2Tasks = new ArrayList<Task>();
		
					// first of all, clear the array for the sorting 
					lstsrtAr.clear();
					
					// now generate a new one where the tasks that have NOT selected by the classifier have higher priority
					for (int i = 0; i < lstTasks.size(); i++) {
						sortArray tmpsrt = new sortArray();
						tmpsrt.setIndex(i);
						tmpsrt.setRand(Math.random());

						double clsLabel = classifier.classifyInstance(unlabeledTS.instance(i));
				    	if (clsLabel == 1)
				    		tmpsrt.setRand(tmpsrt.getRand()*2);	    		

						lstsrtAr.add(tmpsrt);
					}
					
					// sort the list using the rand data type
					Collections.sort(lstsrtAr, new sortArray());
					
					// copy the tasks from the original tasks list in the order defined by the randomly sorted array until I have copied a number of tasks equal to the size of the classiier-pruned dataset
					for (int i = 0; i < lstPrunedTasks.size(); i++)
						lstPrRnd2Tasks.add(lstTasks.get(lstsrtAr.get(i).getIndex()));
				    
				    PerroUtils.print("*** Created RND 2 Pruned dataset with " + lstPrRnd2Tasks.size() + " tasks.", true);
	
				    // write the XML file on disk
				    GenerateDataSet tmpGDS_RND2 = new GenerateDataSet();
				    String strPrRND2XMLFileName = tmpGDS_RND2.WriteDataSetOnFile(strDSFileNamePrefix, lstPrRnd2Tasks, lstResources, strPathTmp , "_" + strDSFileNameSuffix + "_PRRND2" );
				    
					// generates another solver object using the pruned dataset 
					Solver1 RND2PrunedProblemSolver = new Solver1(strPathTmp, strPrRND2XMLFileName);
					
					// launch the solver on the pruned problem with 7 threads and stores the results w/o storing solution results
				    tmpSolStat = RND2PrunedProblemSolver.launchSolver(false, false, bTestSetResourcesReturnToOrigin, 24, strPathTmp);

				    // and add the information on the execution to the stats object
				    // and copies the relevant information in the ClassifierStats object in the section for the pruned dataset
				    tmpPrunCompareStats.setNumTasks_PR2(lstPrunedTasks.size());
				    tmpPrunCompareStats.setDblExecutionTime_PR2(tmpSolStat.getDblExecutionTime());
				    tmpPrunCompareStats.setiTotServiced_PR2(tmpSolStat.getiTotServiced());
				    tmpPrunCompareStats.setiTotUnserviced_PR2(tmpSolStat.getiTotUnserviced());
				    tmpPrunCompareStats.setDbTraveledDistance_PR2(tmpSolStat.getDbTraveledDistance());
				    tmpPrunCompareStats.setDbTotalCosts_PR2(tmpSolStat.getDbTotalCosts());
				    tmpPrunCompareStats.setiNumVehiclesUsed_PR2(tmpSolStat.getiNumVehiclesUsed());
				    tmpPrunCompareStats.setDbTimeWinViolation_PR2(tmpSolStat.getDbTimeWinViolation());
				    tmpPrunCompareStats.setDbOperationTime_PR2(tmpSolStat.getDbOperationTime());
					// add the stats on the differences between the two executions
					dbExecTimDiff = tmpPrunCompareStats.getDblExecutionTime_PR2()-tmpPrunCompareStats.getDblExecutionTime_UP();
					tmpPrunCompareStats.setDbAbsExecTimeDiff_PR2(dbExecTimDiff);
					tmpPrunCompareStats.setDbPerExecTimeDiff_PR2(dbExecTimDiff/tmpPrunCompareStats.getDblExecutionTime_UP()*100);
					
					dbServicedDiff = tmpPrunCompareStats.getiTotServiced_PR2()-tmpPrunCompareStats.getiTotServiced_UP();
					tmpPrunCompareStats.setDbAbsSrvcdTasksDiff_PR2(dbServicedDiff);
					tmpPrunCompareStats.setDbPerSrvcdTasksDiff_PR2(dbServicedDiff/tmpPrunCompareStats.getiTotServiced_UP()*100);

					lstPruningCompareStats.add(tmpPrunCompareStats);
					
					// ** END Of the random pruning code section
					
					
		    		// de-select the evaluation test set for the object currently loaded dataset (there must be only one dataset set for evaluation per loop iteration)
		    		objFileParse.setbTestSet(false);
	
		    	}			
	
				// writes the stats to disk
				ClassifiersUtils.classifierStatsToCSV(false, strPath, "_full", lstClassifierStats);
				
				// REM this line if pruning comparison is switched off
				ClassifiersUtils.pruningStatsToCSV(false, strPath, "_PRCOMP", lstPruningCompareStats);
	
	/*	
				 String[] options = new String[2];
				 options[0] = "-t";
				 options[1] = fileNameTestSet;
				 System.out.println(Evaluation.evaluateModel(classifier, options));
				 
				for (String str : eval.getMetricsToDisplay()) {
					PerroUtils.print(str);
				}
	*/			
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					    
	}
		

	/**
	 * Generates the file name of the ARFF file corresponding to the batch object being read
	 *  
	 * @param bcObj		the current batch object
	 * @param lstObj	the list containing all the batch objects (to retrieve the index)
	 * @return			the string containing the full path and filename of the ARFF file
	 */
	private String generateARFFFileName(BatchConfig bcObj, ArrayList<BatchConfig> lstObj) {
		return strPath + "/DS_"+ bcObj.getnTasks() +"_"+ bcObj.getnResources() +lstObj.indexOf(bcObj)+".arff";

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
	 * Returns a string with the prefix of the ds file name (essentially all chars before the first "_")
	 * 
	 * @param str String containing the full path and filename
	 * @return String string without extension
	 */
	private String returnDSFileNamePrefix (String str) {
		return str.substring(0, str.indexOf('_'));
	}
	
	
	/**
	 * Generates a CSV file from the private SolStats list (containing statistics for solver's executions) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 *  @param strFullPath full path (inclusive of final "/") where the csv file has to be written
	 *  @param strNameOfBatch name of the back configuration file for which the statistics have been generated
	 */
	/*
	private static void classifierStatsToCSV(boolean prtOnScreen, String strFullPath, String strNameOfBatch, ArrayList<ClassifierStats> lstInternalClassStatList) {
		
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

	*/
	
	/**
	 * @return the lstClassifierStats
	 */
	public ArrayList<ClassifierStats> getLstClassifierStats() {
		return lstClassifierStats;
	}

	/**
	 * @param lstClassifierStats the lstClassifierStats to set
	 */
	public void setLstClassifierStats(ArrayList<ClassifierStats> lstClassifierStats) {
		this.lstClassifierStats = lstClassifierStats;
	}

	public ArrayList<PruningCompareStats> getLstPruningCompareStats() {
		return lstPruningCompareStats;
	}

	public void setLstPruningCompareStats(ArrayList<PruningCompareStats> lstPruningCompareStats) {
		this.lstPruningCompareStats = lstPruningCompareStats;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ArrayList<ClassifierStats> lstFullStats = new ArrayList<ClassifierStats>();
		ArrayList<PruningCompareStats> lstFullPruningCompareStats = new ArrayList<PruningCompareStats>();
/*		
		BatchClassifier tmp = new BatchClassifier();	
		tmp.NewBatchJob("batch/Batch10_100_d01.xml");		//loads the jobs configuration
		tmp.executeBatchClassifier();				// launches the classifier
		lstFullStats.addAll(tmp.getLstClassifierStats());
		
		BatchClassifier tmp1 = new BatchClassifier();	
		tmp1.NewBatchJob("batch/Batch10_100_d001.xml");		//loads the jobs configuration
		tmp1.executeBatchClassifier();				// launches the classifier
		lstFullStats.addAll(tmp1.getLstClassifierStats());

		BatchClassifier tmp2 = new BatchClassifier();	
		tmp2.NewBatchJob("batch/Batch10_100_d005.xml");		//loads the jobs configuration
		tmp2.executeBatchClassifier();				// launches the classifier
		lstFullStats.addAll(tmp2.getLstClassifierStats());
*/
		
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
					BatchClassifier tmp = new BatchClassifier();
					String strBatchName = "batch/"+ fileInDir.getName();
					PerroUtils.print("Launching batch job " + strBatchName);
					tmp.NewBatchJob(strBatchName);		//loads the jobs configuration
					tmp.executeBatchClassifier();				// launches the classifier
					lstFullStats.addAll(tmp.getLstClassifierStats());
					lstFullPruningCompareStats.addAll(tmp.getLstPruningCompareStats());
				}
		}

		ClassifiersUtils.classifierStatsToCSV(false, "output/", "all_", lstFullStats);
		ClassifiersUtils.pruningStatsToCSV(false, "output/", "PrComp_all_", lstFullPruningCompareStats);


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
