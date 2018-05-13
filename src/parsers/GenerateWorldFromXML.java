/**
 * 
 */
package parsers;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import objects.*;
import utils.*;

/**
 * GenerateWorldFromXML
 * 
 * This class reads the .xml file passed as argument and populates the arraylists containing the "world" for our model that can be read using getters.
 * Structure of the .xml has to follow the specifications used for this project.
 * 
 * @author Giovanni
 *
 */
public class GenerateWorldFromXML {

	// private data members that will be used to generate the world
	List<Task> lstTasks = new ArrayList<Task>();
	List<Resource> lstResources = new ArrayList<Resource>();
	List<Node> lstNodes = new ArrayList<Node>();

	int numTasks = 0;
	int numResources = 0;

	/**
	 * Reads the file whose filename is passed as parameter and populates the arraylists
	 * return true if everything went well, false otherwise
	 * 
	 * @param	fileName	Name of the .xml file to be parsed WITHOUT the path
	 * @return	boolean		Result of the parsing (true if world has been created successfully, false otherwise)
	 */
	public boolean ReadDatasetFile(String path, String fileName) {
	
		List<String> lstString= new ArrayList<String>();
		String strTemp;
		int i = 0;
		
		if (path == "")
			path = "resources/";
		
		// initializes XStream object (ie XML reader)
		XStream xstream = new XStream();

		// security permissions for XStream
		
		// clear out existing permissions and set own ones
		xstream.addPermission(NoTypePermission.NONE);
		xstream.allowTypeHierarchy(Task.class);
		xstream.allowTypeHierarchy(Node.class);
		xstream.allowTypeHierarchy(Resource.class);

		// all lines from files are read and put in an arraylist
		lstString = PerroUtils.getFileToList(path + fileName);
		// exit if something went wrong
		if (lstString == null)
			return false;

	
		// first pass: reads tasks list and populate relevant list

		PerroUtils.print("Parsing file   : " + fileName, true);
		
		int iStartRow = 0;
		String str = "";
		
		do {
			str = XMLUtils.returnNextXMLObject(lstString, Task.class, iStartRow);
//			PerroUtils.print(str);
			iStartRow = XMLUtils.getiEndRow();
			if (str != "") {
				// creates a new object and add it to the list		
				Task tmpCObj = (Task)xstream.fromXML(str);
				// e lo aggiungo alla lista
				lstTasks.add(tmpCObj);
			}
				
		} while ( (str != "") && (iStartRow < lstString.size()) );
		
		// second pass: reads resources list and populate relevant list
		iStartRow = 0;
		str = "";
		
		do {
			str = XMLUtils.returnNextXMLObject(lstString, Resource.class, iStartRow);
//			PerroUtils.print(str);
			iStartRow = XMLUtils.getiEndRow();
			if (str != "") {
				// creates a new object and add it to the list		
				Resource tmpCObj = (Resource)xstream.fromXML(str);
				// e lo aggiungo alla lista
				lstResources.add(tmpCObj);
			}
				
		} while ( (str != "") && (iStartRow < lstString.size()) );

		numTasks = lstTasks.size();
		numResources = lstResources.size();
		
		PerroUtils.print("Retrieved #" + numTasks + " tasks and #"+ numResources + " resources - complete", true);
		
		return true;	
	}


	/**
	 * @return the lstTasks
	 */
	public List<Task> getlstTasks() {
		return lstTasks;
	}

	/**
	 * @return the lstResources
	 */
	public List<Resource> getlstResources() {
		return lstResources;
	}

	/**
	 * @return the lstNodes
	 */
	public List<Node> getLstNodes() {
		return lstNodes;
	}


	public void setLstNodes(List<Node> lstNodes) {
		this.lstNodes = lstNodes;
	}


	/**
	 * @return the numTasks
	 */
	public int getNumTasks() {
		return numTasks;
	}


	/**
	 * @return the numResources
	 */
	public int getNumResources() {
		return numResources;
	}

	
	// example execution
	public static void main(String[] args) {
	
		GenerateWorldFromXML tmp = new GenerateWorldFromXML();
		if (tmp.ReadDatasetFile("resources/", "DS_5_1.xml"))
			PerroUtils.print("Parsing completed ok");
		else
			PerroUtils.print("Parsing failed");
		
	}
}

/**
 * 

		// First two lines are used to store number of tasks and number of resources
		// 1) number of tasks
		strTemp = lstString.get(0);
		i = strTemp.indexOf("</numTsk>");
		numTasks = Integer.parseInt(strTemp.substring(8,  i));
		// in case of problems set numTasks to -1 and return false 
		if (numTasks <= 0) {
			numTasks = -1;
			return false;
		}

		// 2) number of resources
		strTemp = lstString.get(1);
		i = strTemp.indexOf("</numRsc>");
		numResources = Integer.parseInt(strTemp.substring(8,  i));
		// in case of problems set numResources to -1 and return false 
		if (numTasks <= 0) {
			numTasks = -1;
			return false;
		}

		
// 3) Read the xml file and populates tasks and resources lists
PerroUtils.print("-Parsing XML File--------------------------------------------------------------------");
PerroUtils.print(" Parsing file   : " + fileName);
PerroUtils.print(" # of Tasks     : " + numTasks);
PerroUtils.print(" # of Resources : " + numResources);
PerroUtils.print("-Reading file and building lists-----------------------------------------------------");

// Start with task list: 
// retrieves total number of rows in the class Task (useful should the class change in the future) 
int numFields = XMLUtils.numRowsInXMLForClass(Task.class);
numFields+=2;		// I have to add first and last line in the xml file	
//PerroUtils.print(" "+ numFields);

for (i = 0; i < numTasks; i++) {
	strTemp = "";
	for (int j = 0; j < numFields; j++) {
//		PerroUtils.print(" i="+ i + " j=" + j);

		strTemp = strTemp + lstString.get((i * numFields) + j + 2);
	}
//	PerroUtils.print(strTemp);
	
	// creates a new object and add it to the list		
	Task tmpTask = (Task)xstream.fromXML(strTemp);
	// e lo aggiungo alla lista
	lstTasks.add(tmpTask);
}
PerroUtils.print("" + lstTasks.size() + " Tasks parsed ok");

// Same thing with resources: 
// retrieves total number of rows in the class Resource (useful should the class change in the future) 
int intStartRow = 2 + numTasks*numFields; 			// I have to skip the first lines in the list regarding tasks

numFields = XMLUtils.numRowsInXMLForClass(Resource.class);
numFields+=2;		// I have to add first and last line in the xml file
			
for (i = 0; i < numResources; i++) {
	strTemp = "";
	for (int j = 0; j < numFields; j++) { 
		strTemp = strTemp + lstString.get((i * numFields) + j + intStartRow);
	}
	// creates a new object and add it to the list
	Resource tmpRes = (Resource)xstream.fromXML(strTemp);
	// e lo aggiungo alla lista
	lstResources.add(tmpRes);
}
PerroUtils.print("" + lstResources.size()+ " resources parsed ok");
*/
