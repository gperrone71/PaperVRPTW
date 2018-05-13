/**
 * 
 */
package classifier;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utils.PerroUtils;

/**
 * @author gperr
 *
 */
public class ClassifierTest1 {

	public void IncrementalClassifier(String strTrain, String strTest) {

		  /**
		   * Expects an ARFF file as first argument (class attribute is assumed
		   * to be the last attribute).
		   *
		   * @param args        the commandline arguments
		   * @throws Exception  if something goes wrong
		   */
		    // load data
		    ArffLoader loader = new ArffLoader();
		    ArffLoader loader1 = new ArffLoader();
		    ArffLoader loader2 = new ArffLoader();
		    
		    try {
				loader.setFile(new File(strTrain));
				loader1.setFile(new File(strTest));
				loader2.setFile(new File("output/DS_100_10_batch1_stats.arff"));

				//			    Instances structure = loader.getStructure();
				Instances dataTrain = loader.getDataSet();
				Instances unlabeled = loader1.getDataSet();			
				Instances dsLabeled = loader2.getDataSet();
				
//				testData.addAll(data);
				
			    dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
			    unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
			    dsLabeled.setClassIndex(dsLabeled.numAttributes() - 1);

			    AttributeSelectedClassifier nb = new AttributeSelectedClassifier(); 
			    
			    nb.buildClassifier(dataTrain);

			    // output generated model
			    System.out.println(nb);

			    double iChodato = 0;
			    for (int i = 0; i < unlabeled.numInstances(); i++) {
			    	double clsLabel = nb.classifyInstance(unlabeled.instance(i));
			    	unlabeled.instance(i).setClassValue(clsLabel);
			    	boolean bTest = (dsLabeled.instance(i).classValue() == unlabeled.instance(i).classValue());
			    	PerroUtils.print(" " + unlabeled.instance(i).classValue() + " - " + dsLabeled.instance(i).classValue() + ": " + ((bTest) ? "OK" : "NOK"));
			    	if (bTest)
			    		iChodato++;
			    }
			    
			    PerroUtils.print(" C'ho dato per il " + (iChodato/dsLabeled.numInstances() * 100));
			    // save labeled data
			    BufferedWriter writer = new BufferedWriter(new FileWriter("output/labeled.arff"));
			    writer.write(dsLabeled.toString());
			    writer.newLine();
			    writer.flush();
			    writer.close();
			    
			    Evaluation eval = new Evaluation(dataTrain);
			    eval.evaluateModel(nb, unlabeled);
			    System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			    
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ClassifierTest1 tmp = new ClassifierTest1();
		tmp.IncrementalClassifier("output/DS_100_10_batch0_stats.arff", "output/DS_100_10_batch1_stats_TS.arff");
		
	}

}
