/**
 * 
 */

package dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;

import gnu.trove.map.TMap;

import java.nio.charset.StandardCharsets;

import objects.*;
import scala.collection.generic.BitOperations.Int;
import utils.*;

/**
 * This class is used to generate a dataset from georef files
 * 
 * @version 1.0 12/01/18
 * @author gperr
 *
 */
public class GenerateGeoRefDataSet {

	List<City> lstAllCities = new ArrayList<City>();
	List<City> lstSelectedCities = new ArrayList<City>();

	private String strDataSetFileName;
	private String strDataSetPath;

	
	/**
	 * Alternate constructor 
	 * 
	 * @param strCompleteFileName full filename inclusive of path in the format "dir/name"
	 *  
	 */
	public GenerateGeoRefDataSet(String strCompleteFileName) {

		this(strCompleteFileName.substring(0, strCompleteFileName.indexOf('/')), strCompleteFileName.substring(strCompleteFileName.indexOf('/'), strCompleteFileName.length()));
			
	}
	
	/**
	 * Loads the georef file into the internal ArrayList of objects
	 * 
	 * file has to be structured like that:
	 * 
	 * The main 'geoname' table has the following fields :
		---------------------------------------------------
		geonameid         : integer id of record in geonames database
		name              : name of geographical point (utf8) varchar(200)
		asciiname         : name of geographical point in plain ascii characters, varchar(200)
		alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
		latitude          : latitude in decimal degrees (wgs84)
		longitude         : longitude in decimal degrees (wgs84)
		feature class     : see http://www.geonames.org/export/codes.html, char(1)
		feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
		country code      : ISO-3166 2-letter country code, 2 characters
		cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
		admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
		admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80) 
		admin3 code       : code for third level administrative division, varchar(20)
		admin4 code       : code for fourth level administrative division, varchar(20)
		population        : bigint (8 byte int) 
		elevation         : in meters, integer
		dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
		timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
		modification date : date of last modification in yyyy-MM-dd format
	 * 
	 * 
	 * @param strPath path for the file
	 * @param strFileName name of the file to be loaded
	 */
	public GenerateGeoRefDataSet(String strPath, String strFileName) {
		int iCntRow = 0;
		int iCntWrongRows = 0;
		int iSkippedRows = 0;

		Set<String> stItemsToSkip = new HashSet<String>(PerroUtils.getFileToList(strPath + "Skip.txt"));
		
		// Hashset used to remove duplicated cities
		Set<String> stCities = new HashSet<String>(); 
		
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(strPath + strFileName));

			String tmp;
			
			try {
				while ((tmp = inFile.readLine()) != null) {

					String[] strArray;
					strArray = tmp.split("\t");		// create an array using tab as separator parsing the line just read
					
					if (strArray.length == 19) {
						City tmpCity = new City();

						// create a temporary City object and adds it to the list
						tmpCity.setName(strArray[1]);
						tmpCity.setLatitude(Double.parseDouble(strArray[4])); 
						tmpCity.setLongitude(Double.parseDouble(strArray[5]));
						tmpCity.setPopulation(Integer.parseInt(strArray[14])); 

						// skip all cities with: 
						// pop < 100 (removes also the POI)
						// whose name has been already included (will skip also towns with same name but who cares)
						// items that represent province of a town
						// items that are included into the "Skip" file
						if ( (tmpCity.getPopulation()>100) && 
								(!stCities.contains(tmpCity.getName().toUpperCase())) && 
								(! (tmpCity.getName().contains("Provincia di") || tmpCity.getName().contains("Province of")) ) &&
								(!stItemsToSkip.contains(tmpCity.getName())) ) {  
							lstAllCities.add(tmpCity);
							stCities.add(tmpCity.getName().toUpperCase());
						}
						else
							iSkippedRows++;
						
						iCntRow++;
						}
					else
						iCntWrongRows++;
				}
				inFile.close();

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lstAllCities.sort(null);		// sort the list using the default comparator
		PerroUtils.print("Parsed file " + strFileName + " with "+iCntRow + " read rows and retrieved " + lstAllCities.size() + " elements ("+ iCntWrongRows + " wrong rows - " + iSkippedRows + " skipped rows)", true);
	}
	
	/**
	 * Generates the dataset using the configuration parameters found in the object passed as parameter.
	 * Essentially this method:
	 * 1) finds the city to be used as center
	 * 2) extracts all the cities within the specified radius
	 * 3) generates the dataset using the specified scaling factor
	 * 
 	 * @param strPath		optional string that can be used to specify the path (including final "/") of the dataset file to be generated (if empty defaults to "resources\")
	 * @param strFileSuff	optional string that specifies a suffix to be appended to the dataset file name
 	 * @param configParam   object of the type GeoRefConfig that stores the configuration parameter
 	 * @param bVerbose		specifies verbose output or not
	 */
	public void GenerateDS(String strPath, String strFileSuff, GeoRefConfig configParam, boolean bVerbose) {

		// generates lists and other variables
		List<Task> listTasks = new ArrayList<Task>();
		List<Resource> listResources = new ArrayList<Resource>();
		int iNumResources;
		int iNumNodes = 0;

		PerroUtils.print("Generating data set for city " + configParam.getStrCity(), true);

		City cityRef = new City();
		boolean bFound = false;
		
		for (City tmp : lstAllCities) 
			if (tmp.getName().equals(configParam.getStrCity())) {
					cityRef = tmp;
					bFound = true;
					break;
			}
		
		// if ref city wasn't found then exit
		if (!bFound) {
			PerroUtils.print("** FATAL: city " + configParam.getStrCity() + " not found in database", true);
			return;
		}

		// copies the object found in the loop in a final variable
		final City cityRefFinal  = cityRef;
		
		// generates list of selected cities
		lstSelectedCities.clear();
		lstSelectedCities = lstAllCities.stream().filter(c -> c.calculateDistanceFromCity(cityRefFinal) <= configParam.getiRadius()).collect(Collectors.toList());
		PerroUtils.print("+--- " + lstSelectedCities.size() + " cities within the selected radius of " + configParam.getiRadius() + " km");

		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);

		// preliminary calculations on main city that has to be in the center of the map
		// coordinates conversion factors
		double dbLatConvFactor = configParam.getiRadius() - cityRefFinal.getMyX();
		double dbLongConvFactor = configParam.getiRadius() - cityRefFinal.getMyY(); 
		
		// 1) per each city in the selected cities list generate the relevant nodes		

		Random rndNum = new Random();
		
		for (City tmp : lstSelectedCities) {

			// calculate number of customers for the city being processed
			int iNumCustomers = (int) (tmp.getPopulation()/configParam.getDbScalingFactor());

			if (iNumCustomers > 0) {		// proceeds only if number of customers > 0
		
				// calculate the coordinates for the city; assumes refCity stays in the middle of the map
				double dbCityLat = tmp.getMyX() + dbLatConvFactor;
				double dbCityLon = tmp.getMyY() + dbLongConvFactor;
				
				// calculates the customers spreading radius for the city ( r = sqrt(pop/(dens*pi)) )
				double dbCustSpreadRadius = dbSpreadRadius(tmp, configParam); 
	
				if (bVerbose)
					PerroUtils.print("     Generating nodes for " + tmp.getName() + " (X=" + dbCityLat + "; Y="+ dbCityLon +"; pop="+tmp.getPopulation()+"; cust = " + iNumCustomers + "; dist = " + tmp.calculateDistanceFromCity(cityRefFinal)+ ")");
	
				// random generators
				Random rndPos = PerroUtils.genRandomGeneratorSeeded();
				Random rndTime = PerroUtils.genRandomGeneratorSeeded();
	
				// loop per each city
				for (int i = 0; i < iNumCustomers; i++ ) {
	
					Task tsk = new Task();
					
					// set ID
					tsk.setId(iNumNodes++);
					
					// set type to T
					tsk.setType("T");
					
					// set Description
					tsk.setDescription("Task#"+iNumNodes);
					
					// set type id to 1
					tsk.setType_id(1);
					
					// set resId to 0;
					tsk.setResId(0);
				
					// set Priority to a random number (1 - 5)
					tsk.setPriority(rndNum.nextInt(4)+1);
					
					// generate random capacity required
					tsk.setCapacity(rndNum.nextInt(10));
		
					// generate random position centered on the city center
					Node pos = new Node();
					pos.setId(i);
					pos.setLatitude(dbCityLat + rndPos.nextDouble()*dbCustSpreadRadius);
					pos.setLongitude(dbCityLon + rndPos.nextDouble()*dbCustSpreadRadius);			
					tsk.setNode(pos);
					
					// set region to 1
					tsk.setRegion(1);
					
					// generate random time interval for availability
					TimeInterval timint = new TimeInterval();
					timint.generateRandomTimeInterval(configParam.getiTskMinStartingTime(), configParam.getiTskServiceTimeSpread(), configParam.getiTskMinTimeWindowWidth());
					tsk.setTimeint(timint);
		
					tsk.setServiceTime(configParam.getiTskMinServiceTimeDuration() + rndTime.nextInt(configParam.getiTskMaxServiceTimeDuration()-configParam.getiTskMinServiceTimeDuration()) );		
		
					// skills currently not implemented
		
					listTasks.add(tsk);
				}
			}
		}

		PerroUtils.print("     Generated " +listTasks.size() + " customers.");
		
		// 2) generates resources
		// calculate number of resources available
		if (configParam.getDbResourcesToClientRatio() != 0)
			iNumResources = (int) (listTasks.size()/configParam.getDbResourcesToClientRatio());
		else
			iNumResources = configParam.getiNumResources();

		// Random number generator for the resources spread and for the skills generator
		Random numRnd = PerroUtils.genRandomGeneratorSeeded();
		Random rndSkill = PerroUtils.genRandomGeneratorSeeded();
		
		// main loop to generate the resources
		for (int i = 0; i < iNumResources; i++) {
			Resource rsc = new Resource();
			
			// NOTE: resources ID start from 1 since 0 is reserved for resId field in task nodes
			rsc.setId(i+1);
			rsc.setDescription("Technician"+ (i+1) );
			rsc.setRegion(1);
			
			// generate random time interval for availability (flag Full availability not implemented)
			TimeInterval timint = new TimeInterval();
			timint.generateRandomTimeInterval( configParam.getiResMinStartingTime(), 0, configParam.getiResMinAvailabilityTime() );							
			rsc.setAvailability(timint);
						
			// generate random time interval for unavailability period
			TimeInterval timint1 = new TimeInterval();
			timint1.generateRandomTimeInterval(rsc.getAvailability().getStartTime()+4*60, 0, (int) (timint.getDuration() * 0.112) );		// break cannot be == 0 nor > 11,2% of the availability window
			rsc.setBreakTime(timint1);	

			// generate position for the Origin
			Node pos = new Node();
			pos.setId(++iNumNodes);    	// the node ID for the origin has to continue from the node ID generated for the tasks
			
			// set default starting position as the coordinates of the reference city that are, by definition, in the middle of the quadrant
			pos.setLatitude(configParam.getiRadius());
			pos.setLongitude(configParam.getiRadius());			
			if (!configParam.isbAllResourcesHaveSamePos()) {				
				// if need to apply a spreading apply it on top of the coordinates already set
				pos.setLatitude(cityRef.getLatitude() + (numRnd.nextDouble() - 0.5)*2* configParam.getdStartingPositionsSpread());
				pos.setLongitude(cityRef.getLongitude() + (numRnd.nextDouble() - 0.5)*2*configParam.getdStartingPositionsSpread());
			}
			rsc.setOrigin(pos);
			
			// set Destination (always return to base)
			Node pos1 = new Node();
			pos1 = rsc.getOrigin();
			pos1.setId(++iNumNodes);  			// the node ID for the
			rsc.setDestination(pos1);
			
			// generate a random skill
			int iSkill = rndSkill.nextInt(10);
			rsc.setSkill(iSkill);
			
			// and copy the resource
			listResources.add(rsc);
		}
		PerroUtils.print("     Generated " + listResources.size() + " resources.");

		// writes the generated dataset on an XML file
		PerroUtils.print("Dataset "+WriteDataSetOnFile(listTasks, listResources, cityRefFinal, configParam, strPath, strFileSuff) + " generated", true);
	
		PerroUtils.print("Generation completed successfully (" + iNumNodes +" nodes generated).", true);

	}
	
/**
 * Writes a dataset on file using the two lists of tasks and resources passed as parameters
 * File name of the dataset is generates using the name of the reference city and some configuration parameters
 * 
 * @param listTasks		ArrayList with the list of tasks generated
 * @param listResources	ArrayList with the list of resources generated
 * @param cityRef		City object of the central city
 * @param cfgPar		GeoRefConfig object being processed
 * @param strPath		String that contains the output path of the file to be generated
 * @param strSuff		String with a possible suffix to be appended to the filename
 * 
 * @return String containing name of the dataset generated
 */
	public String WriteDataSetOnFile(List<Task> listTasks, List<Resource> listResources, City cityRef, GeoRefConfig cfgPar, String strPath, String strSuff) {
		
		// instantiate xstream object and set it to absolute references (i.e. do not use references at all)
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		// XML file generation
		List<String> lstString = new ArrayList<String>();

		// populate the list of strings with the two .xml outputs generated before
		for (Task tsk : listTasks) {
			String xmlOut = xstream.toXML(tsk);
			lstString.add(xmlOut);
		}
		
		for (Resource rsc : listResources) {
			String xmlOut = xstream.toXML(rsc);
			lstString.add(xmlOut);
		}

		// write the file on disk in the path specified by the arguments
		if (strPath == "")
			strDataSetPath = "output/";
		else
			strDataSetPath = strPath;

		strDataSetFileName = cityRef.getName()+"_"+cfgPar.getiRadius()+"_"+( (int) cfgPar.getDbScalingFactor()) + strSuff + ".xml";
		
		PerroUtils.print("Attempting to write file " + strDataSetPath + strDataSetFileName + " on disk....", true);
		
		try {
			Files.write(Paths.get(strDataSetPath + strDataSetFileName ), lstString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return strDataSetFileName;
		
	}
	
	/**
	 * @return the strDataSetFileName
	 */
	public String getStrDataSetFileName() {
		return strDataSetFileName;
	}

	/**
	 * @return the strDataSetPath
	 */
	public String getStrDataSetPath() {
		return strDataSetPath;
	}

	// calculates the customers spreading radius for the city ( r = sqrt(pop/(dens*pi)) )
	private double dbSpreadRadius (City tmp, GeoRefConfig configParam) {
		return Math.sqrt( (((double) tmp.getPopulation()) / configParam.getDbPopDensity()) * 1/Math.PI); 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub


		GeoRefConfig tmpCfg = new GeoRefConfig();
		tmpCfg.setStrCity("Florence");
		tmpCfg.setiRadius(50);
		tmpCfg.setDbScalingFactor(1000);
		tmpCfg.setDbPopDensity(1500);
		tmpCfg.setBlCustomersUniformSpread(true);
		tmpCfg.setDbResourcesToClientRatio(0);
		tmpCfg.setiNumResources(100);
		tmpCfg.setbFullResourcesAvailability(false);
		tmpCfg.setbResReturnToStart(true);
		tmpCfg.setbAllResourcesHaveSamePos(false);
		tmpCfg.setcResourcesStartingPosition('C');
		tmpCfg.setdStartingPositionsSpread(10);
		tmpCfg.setbExtendedTimeWin(false);
		tmpCfg.setiResMinAvailabilityTime(480);
		tmpCfg.setiResMinStartingTime(480);
		tmpCfg.setiTskMinStartingTime(480);
		tmpCfg.setiTskMinTimeWindowWidth(240);

		tmpCfg.setiTskMinServiceTimeDuration(30);
		tmpCfg.setiTskMaxServiceTimeDuration(90);
		tmpCfg.setiTskServiceTimeSpread(30);
		
		GenerateGeoRefDataSet tmp = new GenerateGeoRefDataSet("georef/resources/", "it.txt");
		tmp.GenerateDS("", "", tmpCfg, true);
		
	}

}
