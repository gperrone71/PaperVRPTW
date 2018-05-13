/**
 * 
 */
package objects;

import java.util.Random;

/**
 * 
 * Object TIMEINTERVAL
 * 
 * Represents a generic pair of time units <Start;End>
 * This version handles time units as integers (no more as lodaldatetime)
 * 
 * @author Giovanni
 *
 */
public class TimeInterval {

	private int startTime;
	private int endTime;
	
	/**
	 * overrides normal method toString and generate a readable string
	 * 
	 */
	public String toString() {
		return (this.getStartTime() + ";" + this.getEndTime());
	}
	
	public String getName() {
		return ("StartT;EndT");
	}
	
	/**
	 * Generate a random time interval using current date and set the interval for this object
	 * 
	 * @param iStartingTime		minimum value that the start time can be (in minutes)
	 * @param iStartTimeSpread	max spread between the starting time minimum value set with iStartingTime and the Starting Time
	 * @param iMinWidth			minimum width of the time interval (in minutes) 
	 */
	public void generateRandomTimeInterval (int iStartingTime, int iStartTimeSpread, int iMinWidth) {
 
		Random numRnd = new Random();
		int Start, End;

		if (iMinWidth < 60)
			iMinWidth = 60;												// time window must be >= 60 mins
		
		if (iStartTimeSpread < 240)
			iStartTimeSpread = 240;
		
	    Start = iStartingTime + numRnd.nextInt(iStartTimeSpread);		// Start time always starts from iStartingTime and generated as a random number 
		
		do {
			End = Start + numRnd.nextInt(600);				// End time between Start time and St+10 hours
		} while ( (End - Start) < iMinWidth ) ;				// ensures that time window span is at least = iMinWidth
		
		this.startTime = Start;
		this.endTime = End;
	}
	
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	

	/**
	 * Returns an integer containing the value of End - Start for this object
	 * 
	 */

	public int getDuration() {
		return (this.getEndTime() - this.getStartTime());
	}
	
	
}
