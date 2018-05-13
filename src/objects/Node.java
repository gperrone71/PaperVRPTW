/**
 * 
 */
package objects;

import java.util.Random;

/**
 * 
 * Object NODE
 * 
 * Represents a node, that is a physical position represented with <lat;long> representing an actual location in space
 * Each node has therefore a unique id identifier as well as members for its position inside the "world"
 *  
 * @author Giovanni
 * @version 1.1
 * 
 */
public class Node {

	private long id;
	private double latitude;
	private double longitude;
	
	/**
	 * overrides normal method toString and generate a readable string
	 * 
	 */
	public String toString() {
		return (this.getLatitude() + ";" + this.getLongitude());
	}
	
	public String getName() {
		return "Lat;Lon";
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * generateRndPosition
	 * 
	 * generates a random position (expressed in lat; lon coordinates in double format) for the object within the boundaries specified by maxX and maxY
	 * @param maxX maximum value for latitude
	 * @param maxY maximum value for longitude
	 * 
	 */
	public void generateRndPosition(int iMaxX, int iMaxY) {

		/*
		if (iMaxX < 100)
			iMaxX = 100;

		if (iMaxY < 100)
			iMaxY = 100;
		*/
		
		Random numRnd = new Random();
		
		this.latitude = numRnd.nextDouble()*iMaxX;
		this.longitude = numRnd.nextDouble()*iMaxY;
		
	}
	
}
