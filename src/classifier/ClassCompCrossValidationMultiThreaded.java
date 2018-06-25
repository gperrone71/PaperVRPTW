package classifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import utils.*;
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
 * Class implementing a performance comparison between different classifiers using cross validation
 * Includes support for multithreading
 * 
 * 24/06/2018
 * 
 * @author gperr
 *
 */

public class ClassCompCrossValidationMultiThreaded {

		private final int nFolds = 6;
		private final int nIterations = 3;

		private ArrayList<ClassifierPerformance> lstClassifiersPerformances = new  ArrayList<ClassifierPerformance>();
		
		private ArrayList<BatchConfig> lstConfigObj = new ArrayList<BatchConfig>();
		private String strPath;
		
		/**
		 * class actually performing the training and evaluation
		 * 
		 * @author gperr
		 *
		 */
		
		public class TrainAndEvaluate implements Callable<ClassifierPerformance>{
			private Instances dataTrain;
			private Instances dataTest;
			private int iNumFold;
			private String strHeader;
			private String strTimeStamp;
			
			public TrainAndEvaluate(Instances train, Instances eval, int nFold, String strHdr,String strTimSt) {
				this.dataTrain = train;
				this.dataTest = eval;
				this.iNumFold = nFold;
				this.strHeader = strHdr;
				this.strTimeStamp = strTimSt;
			}
			
			public ClassifierPerformance call() {

				PerroUtils.print("T" + iNumFold + " | Start", true);

				// classifiers generation
			    AttributeSelectedClassifier clsJ48 = new AttributeSelectedClassifier();
			    NaiveBayes clsBayes = new NaiveBayes();
			    SMO clsSVM = new SMO();
			    RandomForest clsRndForest = new RandomForest();
			    	    			
				// create a temp element for the classifier performance list
				ClassifierPerformance tmpClPerf = new ClassifierPerformance();
				tmpClPerf.setStrTimeStampDay(strTimeStamp);
				tmpClPerf.setStrInstanceName(strPath);
				tmpClPerf.setStrDSType(strHeader);
				
				try {	
					PerroTimer timer1 = new PerroTimer();
					PerroUtils.print("T" + iNumFold + " | Building J48", true);
					clsJ48.buildClassifier(dataTrain);

					timer1.stop();
					PerroTimer timer2 = new PerroTimer();
					PerroUtils.print("T" + iNumFold + " | Building Bayes", true);
					clsBayes.buildClassifier(dataTrain);
		
					timer2.stop();
					PerroTimer timer3 = new PerroTimer();
					PerroUtils.print("T" + iNumFold + " | Building Random Forest", true);
					clsRndForest.buildClassifier(dataTrain);
		
					timer3.stop();
					PerroTimer timer4 = new PerroTimer();
					PerroUtils.print("T" + iNumFold + " | Building SVM", true);
					clsSVM.buildClassifier(dataTrain);
					timer4.stop();
					
					
					// start evaluation
					// J48
					Evaluation eval = new Evaluation(dataTrain);
					eval.evaluateModel(clsJ48, dataTest);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsJ48.getClass().getSimpleName(), eval, timer1.getElapsedS()));
		//			PerroUtils.print("\nJ48:\n" + eval.toClassDetailsString());
					
					// Bayes
					eval.evaluateModel(clsBayes, dataTest);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsBayes.getClass().getSimpleName(), eval, timer2.getElapsedS()));
		//			PerroUtils.print("\nBayes\n" + eval.toClassDetailsString());
					
					// Random Forest
					eval.evaluateModel(clsRndForest, dataTest);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsRndForest.getClass().getSimpleName(), eval, timer3.getElapsedS()));
		//			PerroUtils.print("\nRnd Forest\n" + eval.toClassDetailsString());
		
					// SVM
					eval.evaluateModel(clsSVM, dataTest);
					tmpClPerf.getLstClsPerf().add(getClassifierPerformanceValues(clsSVM.getClass().getSimpleName(), eval, timer4.getElapsedS()));
		//			PerroUtils.print("\nSVM\n" + eval.toClassDetailsString());
					
					PerroUtils.print("T" + iNumFold + " | done", true);			
					
			    } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			

				return tmpClPerf;
			}
			
		}
		
		
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
		 * Executes the batch classification using cross-validation, that is:
		 * 
		 * 1) loads all the instances in a single Instances objects
		 * 2) use the weka API to generate the folds
		 * 3) generates the models and train it on the train set 
		 * 4) performs the evaluation against the test set
		 * 5) generates the stats and iterates until the all the datasets have been used for evaluation
		 *  	
		 */
		public void executeBatchClassifier() {

			// executor service
			ExecutorService exec = Executors.newCachedThreadPool();

			PerroTimer timer0 = new PerroTimer();
			
			List<String> lstFileToBeParsed = new ArrayList<String>();

			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date date = new Date();
			String strTimeStampDay = dateFormat.format(date);
					
			PerroUtils.print("\n-BATCH CLASSIFIER COMPARISON WITH CROSS-VALIDATION-------------------------------");

			// outer loop: loop for all ARFF files present in the specified output directory
			final File filObj = new File(strPath);
			PerroUtils.print(filObj.getPath());
			
			final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("ARFF", "arff");

			// variables for email composition
			String strEmailBody = "Started batch classifier comparison job for folder " + strPath ;	
			
			// loop in the folder specified to generate the full set of instances 
			// create a list of the .arff files that are available in the selected folder
			for (final File fileInDir : filObj.listFiles()) 
				if ( extensionFilter.accept(fileInDir) ) 
					if (!fileInDir.isDirectory()) {
						String strFileName =fileInDir.getName(); 
						lstFileToBeParsed.add(strFileName);
						strEmailBody += "\n" + strFileName;		// add to the email body the name of the files
					}
			
		    // uses the first dataset to build the structure for the object that will contain the various instances
	    	ArffLoader initLoader = new ArffLoader();
	    	String strFirstDataSet = strPath + "/" + lstFileToBeParsed.get(0);
	    	
	    	PerroUtils.print(" Using dataset #0 for structure generation ("+ strFirstDataSet + ")", true);
			try {
				initLoader.setFile(new File(strFirstDataSet));
				Instances dataset = initLoader.getStructure();
				dataset.setClassIndex(dataset.numAttributes() - 1);	

				for (final File fileInDir : filObj.listFiles()) {
					if ( extensionFilter.accept(fileInDir) ) 
						if (!fileInDir.isDirectory()) {
							String strFileName =  strPath + "/" + fileInDir.getName();
								
					    	ArffLoader loader = new ArffLoader();
							loader.setFile(new File(strFileName));

							Instances tmpInst = loader.getDataSet();
							tmpInst.setClassIndex(tmpInst.numAttributes()-1);
							dataset.addAll(tmpInst);
						}
					}

			
				strEmailBody += "\n#" + lstFileToBeParsed.size() + " files to be processed:";
				PerroUtils.print(" Finished loading datasets - added " + dataset.numInstances() + " instances.", true);
				
				// prepare email object
				Email email = EmailBuilder.startingBlank()
					    .from("Giovanni Perrone", "gperrone71@yahoo.it")
					    .to("Me", "gperrone71@gmail.com")
					    .withSubject("PAPERVRPTW: Start Classifier comparison job " + strPath)
					    .withPlainText(strEmailBody)
					    .buildEmail();
				PerroUtils.emailSender(email);
				
				// main loop
				for (int nIte = 0; nIte < nIterations; nIte++) {
					
					ArrayList<Future<ClassifierPerformance>> resultsFromThreads = new ArrayList<Future<ClassifierPerformance>>();
					
					PerroUtils.print("Iteration " + nIte + " out of " + nIterations,true);

				    // randomize the original dataset
				    Random rand = new Random(System.currentTimeMillis());   // create seeded number generator
				    Instances randData = new Instances(dataset);   			// create copy of original data
				    randData.randomize(rand);         						// randomize data with number generator
				    	    
				    // loop per single iteration:
				    // A) launch the threads
				    // B) collect the values
				    for (int i = 0; i < nFolds; i++) {
						// create a temp element for the classifier performance list
				    	String strTmp = "Run:"+ nIte + "- Fold:"+i;
					
					    // generate the training and test set using cross-validation
					    Instances dataTrain = randData.trainCV(nFolds, i);
					    Instances dataTest = randData.testCV(nFolds, i);

					    // launch all folds in parallel
					    resultsFromThreads.add(exec.submit(new TrainAndEvaluate(dataTrain, dataTest, i, strTmp, strTimeStampDay)));
				    }
				    
				    // wait until all threads are complete
				    do {
					    boolean bAllDone = true;
				    	for (Future<ClassifierPerformance> tmp : resultsFromThreads)
				    		bAllDone &= tmp.isDone();		// final result is true only if all threads have completed execution
				    	if (bAllDone)
				    		break;
				    } while (true);
				    	
				    PerroUtils.print("All threads completed",true);
				    
			    	for (Future<ClassifierPerformance> tmp : resultsFromThreads)
			    			lstClassifiersPerformances.add(tmp.get());

				}
				    
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					    
			writeClassifierPerformanceStats(false, strPath);

		    // prepare email object
			Email finalEmail = EmailBuilder.startingBlank()
				    .from("Giovanni Perrone", "gperrone71@yahoo.it")
				    .to("Me", "gperrone71@gmail.com")
				    .withSubject("PAPERVRPTW: END Classifier job " + strPath)
				    .withPlainText("Processing complete after " + timer0.getElapsedS() + " s (" + timer0.getElapsedHr() + " hours)")
				    .buildEmail();
			PerroUtils.emailSender(finalEmail);
			
			PerroUtils.print("Execution complete.",true);
						    
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

			strList.add((lstClassifiersPerformances.get(0)).getHeaderString());

			// create the header string
			if (prtOnScreen)
				PerroUtils.print(""+ (lstClassifiersPerformances.get(0)).getHeaderString());

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
						ClassCompCrossValidationMultiThreaded tmp = new ClassCompCrossValidationMultiThreaded();
						String strBatchName = "batch/"+ fileInDir.getName();
						PerroUtils.print("Launching batch job " + strBatchName);
						tmp.NewBatchJob(strBatchName);		//loads the jobs configuration
						tmp.executeBatchClassifier();				// launches the classifier
						lstClsPerf.addAll(tmp.getLstClassifiersPerformances());
					}
				}
			ClassCompCrossValidationMultiThreaded tmp = new ClassCompCrossValidationMultiThreaded();
			tmp.setLstClassifiersPerformances(lstClsPerf);
			tmp.writeClassifierPerformanceStats(false, "output");
			
			}
	}

