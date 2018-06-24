/**
 * 
 */
package classifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;

import dataset.DSPlotter;
import dataset.GenerateDataSet;
import launchers.BatchLauncher;
import objects.*;
import problem.Solver1;
import utils.ClassifiersUtils;
import utils.PerroTimer;
import utils.PerroUtils;

import utils.XMLUtils;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;

/**
 * Class implementing a performance comparison between different classifiers
 * 20/06/2018
 * 
 * @author gperr
 *
 */
public class ClassifierCompare {
	
	private ArrayList<ClassifierPerformance> lstClassifiersPerformances = new  ArrayList<ClassifierPerformance>();
	
	private ArrayList<BatchConfig> lstConfigObj = new ArrayList<BatchConfig>();
	private String strPath;
	
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
		lstString = PerroUtils.getFileToList(FolderDefs.resourcesFolderName + strConfigFileName);
		// exit if something went wrong
		if (lstString == null)
			return false;

		// Read the xml configuration file and populates the list of the jobs to be executed
		PerroUtils.print("** CLASSIFIERS COMPARISON **");
		PerroUtils.print("-BATCH LOADER for classifier comparison -----------------------------------------");
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
	 * Executes the batch classification inclusive of cross-validation using the leave one out technique, that is:
	 * 1) starts setting the first dataset as to be used for evaluation and all the others for training
	 * 2) reads accordingly the instances
	 * 3) generates a model and train it on the loaded instances
	 * 4) loads the dataset to be used as test and evaluates performances of the classifier built so far on the evaluation dataset
	 * 5) generates the stats and iterates until the all the datasets have been used for evaluation
	 *  	
	 */
	public void executeBatchClassifier() {

		List<FileParse> lstFileToBeParsed = new ArrayList<FileParse>();

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		String strTimeStampDay = dateFormat.format(date);
				
		PerroUtils.print("\n-BATCH CLASSIFIER COMPARISON ----------------------------------------------------");
		PerroUtils.print("\nStarting training phase.");

		// outer loop: loop for all ARFF files present in the specified output directory
		final File filObj = new File(strPath);
		PerroUtils.print(filObj.getPath());
		
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("ARFF", "arff");

		// variables for email composition
		String strEmailBody = "Started batch classifier comparison job for file " + strPath ;	
		long timeNow = System.currentTimeMillis();
		
		// create a list of the .arff files that are available in the selected folder
		for (final File fileInDir : filObj.listFiles()) {
			if ( extensionFilter.accept(fileInDir) ) 
				if (!fileInDir.isDirectory()) {
					PerroUtils.print(fileInDir.getName());
					FileParse tmp = new FileParse();
					tmp.setStrFileName(fileInDir.getName());
					tmp.setbTestSet(false);
					lstFileToBeParsed.add(tmp);
					strEmailBody += "\n" + fileInDir.getName();		// add to the email body the name of the files
				}
		}
		
		strEmailBody += "\n#" + lstFileToBeParsed.size() + " files to be processed:";
		
		
		// prepare email object
		Email email = EmailBuilder.startingBlank()
			    .from("Giovanni Perrone", "gperrone71@yahoo.it")
			    .to("Me", "gperrone71@gmail.com")
			    .withSubject("PAPERVRPTW: Start Classifier comparison job " + strPath)
			    .withPlainText(strEmailBody)
			    .buildEmail();
		PerroUtils.emailSender(email);
		

		// outer loop: execute per each of the .arff file stored in the file list
		for (FileParse tmpObj : lstFileToBeParsed)
			tmpObj.setbTestSet(false);
			long lTimerForFirstIteration = System.currentTimeMillis();
		
		    try {
		    	
		    	for (FileParse objFileParse : lstFileToBeParsed) {
		    		// inner loop:
		    		
		    		// select the evaluation test set for the object currently loaded
		    		objFileParse.setbTestSet(true);
		    		String strDSFileNamePrefix = returnDSFileNamePrefix(objFileParse.getStrFileName());		// stores the type of DS in order to be able to use it later
					String strDSFileNameSuffix = returnFullFileNameWOExtension(objFileParse.getStrFileName());
					strDSFileNameSuffix = strDSFileNameSuffix.substring(strDSFileNameSuffix.lastIndexOf('_')+1);	// get the string from the last occurrence of "_" (without it) until the end of the string

		    		// generate the classifiers 
				    AttributeSelectedClassifier clsJ48 = new AttributeSelectedClassifier();
				    NaiveBayes clsBayes = new NaiveBayes();
				    SMO clsSVM = new SMO();
				    RandomForest clsRndForest = new RandomForest();
				    
				    // uses the first dataset to build the structure for the object that will contain the various instances
			    	ArffLoader initLoader = new ArffLoader();
			    	String strFirstDataSet = strPath + "/" + lstFileToBeParsed.get(0).getStrFileName();
			    	
			    	PerroUtils.print(" Using dataset #0 for structure generation ("+ strFirstDataSet + ")");
					initLoader.setFile(new File(strFirstDataSet));
					Instances dataTrain = initLoader.getStructure();
					dataTrain.setClassIndex(dataTrain.numAttributes() - 1);	
			
				    String fileNameTestSet = "";
				    
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
						else 
							fileNameTestSet = strDataSetFileName;
					}
					
					PerroUtils.print(" Finished loading datasets - added " + dataTrain.numInstances() + " instances.");
					
					// loads the test set
					PerroUtils.print("\nLoading " + fileNameTestSet + " as test set.");
					ArffLoader testLoader = new ArffLoader();
					testLoader.setFile(new File(fileNameTestSet));
					Instances testSet = testLoader.getDataSet();
					testSet.setClassIndex(testSet.numAttributes() - 1);

					// build the classifiers
					PerroUtils.print("Building classifiers", true);

					PerroTimer timer1 = new PerroTimer();					
					PerroUtils.print("           Building J48");

					timer1.stop();
					PerroTimer timer2 = new PerroTimer();
					clsJ48.buildClassifier(dataTrain);
					PerroUtils.print("           Building Bayes");
					clsBayes.buildClassifier(dataTrain);

					timer2.stop();
					PerroTimer timer3 = new PerroTimer();
					PerroUtils.print("           Building Random Forest");
					clsRndForest.buildClassifier(dataTrain);

					timer3.stop();
					PerroTimer timer4 = new PerroTimer();
					PerroUtils.print("           Building SVM");
					clsSVM.buildClassifier(dataTrain);
					timer4.stop();
					
					// create a temp element for the classifier performance list
					ClassifierPerformance tmpClPerf = new ClassifierPerformance();
					
					tmpClPerf.setStrInstanceName(fileNameTestSet);
					
					String strNumClusters = strDSFileNamePrefix.replaceAll("[^0-9]", "");
					if (strNumClusters.length() == 0)
						tmpClPerf.setNumClusters(0);
					else
						tmpClPerf.setNumClusters(Integer.parseInt(strNumClusters));
					
					tmpClPerf.setStrDSType(strDSFileNamePrefix.substring(0, strDSFileNamePrefix.length()-strNumClusters.length() ));

					tmpClPerf.setStrTimeStampDay(strTimeStampDay);
					
					// start evaluation
					// J48
					Evaluation eval = new Evaluation(dataTrain);
					eval.evaluateModel(clsJ48, testSet);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsJ48.getClass().getSimpleName(), eval, timer1.getElapsedS()));
//					PerroUtils.print("\nJ48:\n" + eval.toClassDetailsString());
					
					// Bayes
					eval.evaluateModel(clsBayes, testSet);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsBayes.getClass().getSimpleName(), eval, timer2.getElapsedS()));
//					PerroUtils.print("\nBayes\n" + eval.toClassDetailsString());
					
					// Random Forest
					eval.evaluateModel(clsRndForest, testSet);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsRndForest.getClass().getSimpleName(), eval, timer3.getElapsedS()));
//					PerroUtils.print("\nRnd Forest\n" + eval.toClassDetailsString());

					// SVM
					eval.evaluateModel(clsSVM, testSet);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsSVM.getClass().getSimpleName(), eval, timer4.getElapsedS()));
//					PerroUtils.print("\nSVM\n" + eval.toClassDetailsString());
	
					// and then add to the list
					lstClassifiersPerformances.add(tmpClPerf);
					
		    		// de-select the evaluation test set for the object currently loaded dataset (there must be only one dataset set for evaluation per loop iteration)
		    		objFileParse.setbTestSet(false);
	
		    	}
			    writeClassifierPerformanceStats(false, strPath);
				
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	 
		// prepare email object
		long elapsedTime = (System.currentTimeMillis() - timeNow)/1000;
		Email finalEmail = EmailBuilder.startingBlank()
			    .from("Giovanni Perrone", "gperrone71@yahoo.it")
			    .to("Me", "gperrone71@gmail.com")
			    .withSubject("PAPERVRPTW: END Classifier job " + strPath)
			    .withPlainText("Processing complete after " + elapsedTime + " s (" + elapsedTime/3600 + " hours)")
			    .buildEmail();
		PerroUtils.emailSender(finalEmail);
					    
	}
	

		
	/**
	 * Parses the values contained in the Evaluation object and returns a ClassifierPerformance obj that can be stored
	 * 
	 * @param eval	The Evaluation object to be parsed
	 * @return 		The ClassifierPerformance object populated
	 */
	private SingleClassifierPerformance getClassifierPerformanceValues(String str, Evaluation eval, long timeForTraining) {
		SingleClassifierPerformance tmp = new SingleClassifierPerformance();

		tmp.setStrClassifierName(str);
		tmp.setDbPrecision(eval.weightedPrecision());
		tmp.setDbRecall(eval.weightedRecall());
		tmp.setDbConfMatrix(eval.confusionMatrix());
		tmp.setDbAccuracy(eval.pctCorrect());
		tmp.setlTimeForTraining(timeForTraining);
		
		return tmp;
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
	 * Generates a CSV file from the private PruningCompareStats list (containing statistics to compare results on different pruning) and optionally prints on console its contents 
	 * 
	 *  @param boolean prtOnScreen specifies if the CSV output has to be printed on console or not
	 *  @param strFullPath full path (inclusive of final "/") where the csv file has to be written
	 *  @param strNameOfBatch name of the back configuration file for which the statistics have been generated
	 */
	private void writeClassifierPerformanceStats(boolean prtOnScreen, String strFullPath) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String strFullFileName = strFullPath+ "/" + dateFormat.format(date) + "_Classifiers_compare.csv";
		
		List<String> strList = new ArrayList<String>();

		strList.add(lstClassifiersPerformances.get(0).getHeaderString());

		// create the header string
		if (prtOnScreen)
			PerroUtils.print(""+ lstClassifiersPerformances.get(0).getHeaderString());

		for (ClassifierPerformance tmp : lstClassifiersPerformances) {
			String str = tmp.toString();
			if (prtOnScreen) 
				PerroUtils.print(str);
			strList.add(str);
		}
			
		PerroUtils.writeCSV(strFullFileName, strList);
	}

	/**
	 * @return the lstClassifiersPerformances
	 */
	public ArrayList<ClassifierPerformance> getLstClassifiersPerformances() {
		return lstClassifiersPerformances;
	}
	
	/**
	 * @param lstClassifiersPerformances the lstClassifiersPerformances to set
	 */
	public void setLstClassifiersPerformances(ArrayList<ClassifierPerformance> lstClassifiersPerformances) {
		this.lstClassifiersPerformances = lstClassifiersPerformances;
	}

	/**
	 * @return the lstConfigObj
	 */
	public ArrayList<BatchConfig> getLstConfigObj() {
		return lstConfigObj;
	}

	/**
	 * @return the strPath
	 */
	public String getStrPath() {
		return strPath;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ArrayList<ClassifierPerformance> lstClsPerf = new ArrayList<>();

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
					ClassifierCompare tmp = new ClassifierCompare();
					String strBatchName = "batch/"+ fileInDir.getName();
					PerroUtils.print("Launching batch job " + strBatchName);
					tmp.NewBatchJob(strBatchName);		//loads the jobs configuration
					tmp.executeBatchClassifier();				// launches the classifier
					lstClsPerf.addAll(tmp.getLstClassifiersPerformances());
				}
			}
		ClassifierCompare tmp = new ClassifierCompare();
		tmp.setLstClassifiersPerformances(lstClsPerf);
		tmp.writeClassifierPerformanceStats(false, "output");
		
		}
}

