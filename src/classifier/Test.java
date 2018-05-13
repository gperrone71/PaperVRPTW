package classifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import objects.EvaluationStats;
import utils.PerroUtils;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String strPathForTraining = "output/Batch200_3000_d001";
		String strPippo = "";
		
		strPippo = strPathForTraining.substring(strPathForTraining.indexOf('/')+1);
		PerroUtils.print(strPippo);
		
		int intNumDir = 0;
		
		// attempt to launch a batch for all files in the "/batch" directory
		final File filObj = new File("output");
		
		// print list of folders
		for (final File fileInDir : filObj.listFiles()) {
			if (fileInDir.isDirectory()) 
				if (!fileInDir.getName().equals(strPippo)) {
					PerroUtils.print(fileInDir.getName());
					intNumDir++;
				}
		}
		PerroUtils.print("Num Folders: " + intNumDir);
	}	
		
}
