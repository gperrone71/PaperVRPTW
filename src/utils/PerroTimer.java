/**
 * 
 */
package utils;

/**
 * Simple class to implement a timer
 * 24/06/2018
 * 
 * @author gperr
 *
 */
public class PerroTimer {

	private long lStartms;
	private long lEndms;
	
	// default constructor
	public PerroTimer() {
		lStartms = System.currentTimeMillis();
	}
	
	/**
	 * Initialize the time with the current time
	 */
	public void init() {
		lStartms = System.currentTimeMillis();
	}
	
	/**
	 * Stops the timer (useful to retrieve the values at a later time)
	 */
	public void stop() {
		lEndms = System.currentTimeMillis();
	}
	
	/**
	 * returns the elapsed amount of milliseconds
	 * 
	 * @return the amount of milliseconds
	 */
	public long getElapsedMs() {
		if (lEndms == 0)
			return (System.currentTimeMillis() - lStartms);
		else
			return (lEndms - lStartms);
	}
	
	/**
	 * returns the elapsed amount of seconds
	 * 
	 * @return the amount of seconds
	 */
	public long getElapsedS() {
		if (lEndms == 0)
			return (System.currentTimeMillis() - lStartms)/1000;
		else
			return (lEndms - lStartms)/1000;
	} 
	
	/**
	 * returns the elapsed amount of hours
	 * 
	 * @return double - the amount of hours
	 */
	public double getElapsedHr() {
		if (lEndms == 0)
			return (System.currentTimeMillis() - lStartms)/3600000;
		else
			return (lEndms - lStartms)/3600000;
	}
	
}
