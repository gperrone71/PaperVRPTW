/**
 * 
 */
package utils;

import java.lang.reflect.Field;
import java.util.List;

import objects.BatchConfig;

/**
 * Set of small tools for managing XML files using XStream
 * 
 * @author gperr
 *
 */
public class XMLUtils {

	private static int iStartRow; 
	private static int iEndRow;
	
	/**
	 * Returns in a String object the next object of type objToBeFound found in the list lstXMLtoBeParsed starting from position rowStartNumber
	 * Comments are skipped 
	 * 
	 * @param lstXMLtoBeParsed	List or ArrayList containing the rows of the XML file to be parsed  
	 * @param clsToBeFound		Class to be found in the XML stream
	 * @param rowStartNumber	row number to begin the search from
	 * @return Empty String if no occurrences have been found, otherwise the set of the rows retrieved
	 *  
	 */
	public static String returnNextXMLObject(List<String> lstXMLtoBeParsed, Class clsToBeFound, int rowStartNumber) {
		
		String str = "";
		String strXMLObj = "";
		String strStartTag = "<"+clsToBeFound.getName()+">";
		String strEndTag = "</"+clsToBeFound.getName()+">";
		
		iStartRow = rowStartNumber;
		// find the first occurrence of the tag starting from the rowStartNumber position
		do 
			str = lstXMLtoBeParsed.get(iStartRow++);
		while (( iStartRow < lstXMLtoBeParsed.size() ) && !(str.contains(strStartTag) ) );
		
		if ( iStartRow == lstXMLtoBeParsed.size())		// if no occurrences found the return an empty string
			return "";
		
		iEndRow = iStartRow;
		strXMLObj = str;
		
		// find the end tag and retrieve the string
		
		do { 
			str = lstXMLtoBeParsed.get(iEndRow++);
			strXMLObj += str;
		} while (( iEndRow < lstXMLtoBeParsed.size() ) && !(str.contains(strEndTag) ) );
		
		if ( (iEndRow == lstXMLtoBeParsed.size()) && !(str.contains(strEndTag) ) )	// no end tag found -> return an empty string
			return "";
		
//		PerroUtils.print(strXMLObj);
		
		return strXMLObj;
	}
		
	/**
	 * 
	 * Calculates the total number of rows that will be used in the .xml file for a given class
	 * the class passed as parameter is parsed and its components (non-primitives) are analyzed to add the correct number of fields 
	 * 
	 * @param objClass	the class to be parsed
	 * @return int		the total number of fields (i.e. the number of rows for the xml file) 
	 */
	public static int numRowsInXMLForClass(Class objClass) {
		
		int numFields = 0;
		int numDM = objClass.getDeclaredFields().length;
		
		// gets the list of fields for the class passed as parameter
		Field[] fields = objClass.getDeclaredFields();

		// parse the fields and increase the numFields variable accordingly (+1 if it's a primitive or string or +#fields if it's a derived type)
		for (int i = 0; i < numDM; i++ ) 
			if (fields[i].getType().isPrimitive() || (fields[i].getType().getName().contentEquals("java.lang.String")))
				numFields++;
			else
				// the +2 copes with start and end tags for derived types  
				numFields += fields[i].getType().getDeclaredFields().length+2;

		return numFields;
	
	}

	/**
	 * Return the last value of the starting row
	 * 
	 * @return iStartRow
	 */
	public static int getiStartRow() {
		return iStartRow;
	}

	/**
	 * Returns the last value of the end row (obtained at the end of the parsing sequence)
	 * 
	 * @return iEndRow
	 */
	public static int getiEndRow() {
		return iEndRow;
	}

}
