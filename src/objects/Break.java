/**
 * 
 */
package objects;

/**
 * Class used to store breaks interval. Each resource can have multiple breaks with a start time "start", end time "nd" and a duration "duration"
 * 
 * @author Giovanni
 *
 */
public class Break {

	private int resId;		// Id of the resource
	private TimeInterval tiBreak;
	
	
	public int getResId() {
		return resId;
	}
	public void setResId(int resId) {
		this.resId = resId;
	}
	public TimeInterval getTiBreak() {
		return tiBreak;
	}
	public void setTiBreak(TimeInterval tiBreak) {
		this.tiBreak = tiBreak;
	}
	
	
}
