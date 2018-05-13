/**
 * 
 */
package objects;

/**
 * 
 * TASK Object
 * 
 * Represents the atomic workload (ref. 2.1)
 * 
 * @author Giovanni
 *
 */
public class Task {

	/**
	 * Data members as defined in the specifications
	 * 
	 * @param args
	 */
	
	private int id;
	private String Description;
	private String type;		// "T" for tasks, "O" for Origin nodes and "D" for Destination nodes
	private int type_id;	// used for task performance calculations
	private int priority;		// priority of the task (increasing)
	private int resId;			// ID of the resource - used only when the node is used to represent origins or destinations - set to 0 for tasks
	private int serviceTime;		// tasks duration in minutes
	private Node node;
	private int region;			// currently always set to 1
	private TimeInterval timeint;
	private double capacity;	// capacity (skill) required to complete the task 0 - 10

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getType_id() {
		return type_id;
	}

	public void setType_id(int task_type) {
		this.type_id = task_type;
	}

	/**
	 * @return the resId
	 */
	public int getResId() {
		return resId;
	}

	/**
	 * @param resId the resId to set
	 */
	public void setResId(int resId) {
		this.resId = resId;
	}


	public Node getNode() {
		return node;
	}

	public void setNode(Node position) {
		this.node = position;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public TimeInterval getTimeint() {
		return timeint;
	}

	public void setTimeint(TimeInterval timeint) {
		this.timeint = timeint;
	}
	

	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}

	
	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	// public methods used to establish if a task (node) is a Task, Origin or Destination
	public boolean isTask() {
		return (this.type.equals("T"));
	}

	public boolean isOrigin() {
		return (this.type.equals("O"));
	}
	
	public boolean isDestination() {
		return (this.type.equals("D"));
	}
	
	/**
	 * blNodeIsMine() 
	 * public method to determine if a node if my destination or origin
	 * return true if this.resId == res.getId()
	 * 
	 *  @param res	resource
	 */
	public boolean blNodeIsMine(Resource res) {
		return (this.resId == res.getId());
	}
	
	/**
	 * isOriginFor() 
	 * public method to determine if a node if an Origin for a given resource
	 * return true if the node is an Origin and this.resId == res.getId()
	 * 
	 *  @param res	resource
	 */
	public boolean isOriginFor(Resource res) {
		return ( this.isOrigin() && (this.resId == res.getId()) );
	}
	
	/**
	 * blIsDestinationFor() 
	 * public method to determine if a node is a Destination for a given resource
	 * return true if the node is a Destination and this.resId == res.getId()
	 * 
	 *  @param res	resource
	 */
	public boolean isDestinationFor(Resource res) {
		return ( this.isDestination() && (this.resId == res.getId()) );
	}

	/**
	 * @return the serviceTime
	 */
	public int getServiceTime() {
		return serviceTime;
	}

	/**
	 * @param serviceTime the serviceTime to set
	 */
	public void setServiceTime(int serviceTime) {
		this.serviceTime = serviceTime;
	}

}
