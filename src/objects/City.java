/**
 * 
 */
package objects;

import java.util.*;

/**
 * This class is used to store cities in memory that are used for the generation of datasets based on georef data
 * 
 * @author gperr
 *
 */
public class City implements Comparator<City>, Comparable<City> {

	public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

	private String name;		// name of the city
	private int population;		// population
	private double latitude;
	private double longitude;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
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
	
	public int compare(City c1, City c2) {
		return String.CASE_INSENSITIVE_ORDER.compare(c1.getName(), c2.getName());
	}
	
	public int compareTo(City c1) {
		return String.CASE_INSENSITIVE_ORDER.compare(this.getName(), c1.getName());
	}
	
	
	/**
	 * Calculates distance between this object and another object of type City passed as parameter using the Haversine formula
	 * 
	 * @param othCity the city we want the distance from
	 * @return distance in km
	 * 
	 */
	public int calculateDistanceFromCity(City othCity) {

		double latDistance = Math.toRadians(this.getLatitude() - othCity.getLatitude());
	    double lngDistance = Math.toRadians(this.getLongitude() - othCity.getLongitude());

	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	      + Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(othCity.getLatitude()))
	      * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
	}
	
	/**
	 * Returns my latitude in cartesian coordinates
	 * 
	 * @return the X in cartesian coordinates
	 */
	public double getMyX() {
		return AVERAGE_RADIUS_OF_EARTH_KM * Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(this.getLongitude()));
	}

	/**
	 * Returns my latitude in cartesian coordinates
	 * 
	 * @return the X in cartesian coordinates
	 */
	public double getMyY() {
		return AVERAGE_RADIUS_OF_EARTH_KM * Math.cos(Math.toRadians(this.getLatitude())) * Math.sin(Math.toRadians(this.getLongitude()));
	}

}
