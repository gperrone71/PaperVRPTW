package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.swing.JOptionPane;
import javax.xml.soap.Node;

import objects.TimeInterval;

/** 
 * Collezione di piccole utils o funzioni da usare alla bisogna per semplificarsi la vita
 * 
 * @author: G. Perrone
 * @version 1.0 26/08/2016
 * 
 */
public class PerroUtils {

	/**
	 * Scrive sulla console la stringa passata come argomento usando il metodo println 
	 * @param strPrint
	 */
		public static void print(String strPrint) {
			System.out.println(strPrint);
		}

	/**
	 * Scrive sulla console la stringa passata come argomento usando il metodo println 
	 * @param bPrintTime specifies if current time has to be printed on screen
	 * @param strPrint
	 */
		public static void print(String strPrint, boolean bPrintTime) {
			System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " : " + strPrint);
		}

/** 
 * Mostra un messaggio di dialogo di informazione con parametri passati come argomenti
 * @param infoMessage - il testo del messaggio principale
 * @param titleBar - il titolo della finestra
 */
	public static void infoBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, "I: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}
/**
 * Mostra un messaggio di dialogo di errore con parametri passati come argomenti
 * @param errMessage: il testo del messaggio principale
 * @param titleBar: il titolo della finestra
 */
	public static void errBox(String errMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, errMessage, "ERR: " + titleBar, JOptionPane.ERROR_MESSAGE);
	}
	
	public static int YNBox(String strMessage, String titleBar) {
		return JOptionPane.showConfirmDialog(null, strMessage , titleBar, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
		
	}

	/**
	 * Dumps to a CSV contents of a List of Objects. Used to dump contents of lists to CSV for later analysis  
	 * 
	 * @param lstObj
	 * @param strCSVFileName
	 * @return True if dump successfull, false otherwise
	 */
	public static boolean fromListToCSV(List lstObj, String strCSVFileName) {

		NumericUtils.setDefaultFormat();
		
		List<String> strCSVCont = new ArrayList<String>();
		
		String str = "";
		if (lstObj.size() == 0)
			return false;								// exit if the list is empty 

		Class clsObj = lstObj.get(0).getClass();		// get the class of the first object 
		
		// get total number of fields for this class
		int numFields = clsObj.getDeclaredFields().length;
		
		// gets the list of fields for this class
		Field[] fields = clsObj.getDeclaredFields();
		
		// sets accessibility to true for all fields to overcome access limitations due to fields being private
		for (int i = 0; i < numFields; i++)
			fields[i].setAccessible(true);
		
		// parse the fields and creates the return string accordingly
		for (int i = 0; i < numFields; i++ )
			if (fields[i].getType().isAssignableFrom(Node.class))
				str += (fields[i].getName() + "_Lat;" + fields[i].getName() + "_Lon;" );
			else if (fields[i].getType().isAssignableFrom(TimeInterval.class))  
				str += (fields[i].getName() + "_StartT;" + fields[i].getName() + "_EndT;" );
			else
				str += (fields[i].getName() + ";");
						
		// add the header to the list
		strCSVCont.add(str.substring(0, str.length()-1));

		PerroUtils.print(str);

		str = "";
		// now retrieve the contents of the list
		try {
			for (Object obj : lstObj) {
	
				// parse the fields and creates the return string accordingly
				for (int i = 0; i < numFields; i++ ) {
					if (fields[i].getType().isAssignableFrom(String.class))
						str += (fields[i].get(obj) + ";");
					else if( (fields[i].getType().isAssignableFrom(Node.class)) || (fields[i].getType().isAssignableFrom(TimeInterval.class)) ) 
						str += ((fields[i].get(obj).toString() + ";"));
					else
						str += (fields[i].get(obj) + ";").replace('.', ',');
				}
				strCSVCont.add(str.substring(0, str.length()-1));
				PerroUtils.print(str);
				str = "";
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return 	PerroUtils.writeCSV(strCSVFileName, strCSVCont);
		
	}
	
	
	/**
	 * Reads all lines from a file and returns the content in an arraylist
	 * 
	 * @param 	fileName	string containing name of the file to be read
	 * @return	ArrayList	ArrayList object containing the lines of the file read (one item per each line)
	 */
	public static List<String> getFileToList(String fileName) {
		
		List<String> lstString = new ArrayList<String>();
				
		// all lines from files are read and put in an arraylist
		try {
			lstString= Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return lstString;
	}
	
	/**
	 * Returns an ArrayList of Strings with the single items included in the line to be parsed from the csv
	 * Separator to be used in passed as parameter
	 *  
	 * @param strLine	The String to be parsed
	 * @param sep		The char used as separator (normally ";")
	 * @return			ArrayList of String with the items parsed from strLine
	 */
	public static ArrayList<String> parseCSVLine(String strLine, char sep) {
		ArrayList<String> Result = new ArrayList<String>();
		
		if (strLine != null) {
			String[] splitData = strLine.split("\\s*"+ sep + "\\s*");
			for (int i = 0; i < splitData.length; i++) {
				if (!(splitData[i] == null) || !(splitData[i].length() == 0)) {
					Result.add(splitData[i].trim());
				}
			}
		}
	return Result;
	}
	

	/**
	 * converts a string to an int
	 * 
	 * @param str
	 * @return
	 */
	public static int StringToInt(String str) {
		return ((Integer.valueOf(str)).intValue());
	}

	/**
	 * converts a string to a double
	 * 
	 * @param str
	 * @return double the contents of str converted to a double
	 */
	public static double StringToDbl(String str) {
		return ((Double.valueOf(str)).doubleValue());
	}

	/**
	 * Writes contents of a list of String objects into a file
	 * 
	 * @param strNomeCSV	Name of the .csv file to be creates (actually could be a generic text file) including full path
	 * @param strOutput		List of String objects that will be written to the file
	 */
	public static boolean writeCSV(String strNomeCSV, List<String> strOutput) {
		try {
			Files.write(Paths.get(strNomeCSV), strOutput, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Calculates the CRC32 for the first 1024 bytes of the file whose name and path is passed as argument
	 * 
	 * @author gperrone
	 * 
	 * @param String srcFile	FIle name with path of the file to be used for calculating CRC32
	 * @return String The CRC formatted as string
	 * @throws IOException
	 * 
	 */
	
	public static String CRC32Calc(String srcFileName) throws IOException {

	    final InputStream in = new FileInputStream(srcFileName);

        CRC32 checksum;
        
        checksum = new CRC32();
        checksum.reset();

	    try {

	        final byte[] buffer = new byte[1024];
	        
	        int bytesRead = in.read(buffer);
	        
	        while (bytesRead >= 0) {
	        	checksum.update(buffer, 0, bytesRead);
	            bytesRead = in.read(buffer);
	        }
	    } catch (IOException e) {
	        throw e;
	    } finally {
	        in.close();
	    }
	    
	   return String.format("%x", checksum.getValue());
	}
	 
	/**
	 * generates and return a Random generator initialized with the current system time in milliseconds
	 * 
	 * @return Random generator
	 */
	public static Random genRandomGeneratorSeeded() {
		Random numRnd = new Random();						// generate the Random generator
		numRnd.setSeed(System.currentTimeMillis());			// set the current seed
		return numRnd;
	}
	
}
