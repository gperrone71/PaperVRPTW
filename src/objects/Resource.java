/**
 * 
 */
package objects;

/**
 * 
 * Object OPERATING RESOURCE
 * 
 * Represents an atomic entity in charge of executing tasks
 * 
 * @author Giovanni
 *
 */
public class Resource {

	/**
	 * @param args
	 */
	
	private int id;
	private String description;
	private int region;
	private TimeInterval availability;
	private TimeInterval breakTime;
	private Node origin;
	private Node destination;
	private double capacity;
	private int skill;		// int 0 - 10
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TimeInterval getAvailability() {
		return availability;
	}

	public void setAvailability(TimeInterval availability) {
		this.availability = availability;
	}

	/**
	 * @return the breakTime
	 */
	public TimeInterval getBreakTime() {
		return breakTime;
	}

	/**
	 * @param breakTime the breakTime to set
	 */
	public void setBreakTime(TimeInterval breakTime) {
		this.breakTime = breakTime;
	}

	public Node getOrigin() {
		return origin;
	}



	public void setOrigin(Node origin) {
		this.origin = origin;
	}



	public Node getDestination() {
		return destination;
	}



	public void setDestination(Node destination) {
		this.destination = destination;
	}

	/**
	 * @return the skill
	 */
	public int getSkill() {
		return skill;
	}

	/**
	 * @param skill the skill to set
	 */
	public void setSkill(int skill) {
		this.skill = skill;
	}

}
