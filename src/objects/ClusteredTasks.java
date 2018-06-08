/**
 * 
 */
package objects;

import java.util.ArrayList;

/**
 * @author giovanni_perrone
 *
 */
public class ClusteredTasks {
	// private class used to store the tasks that are generated as clusters
		private ArrayList<Task> lstTasksInCluster = new ArrayList<Task>();

		
		public ArrayList<Task> getLstTasksInCluster() {
			return lstTasksInCluster;
		}

		public void setLstTasksInCluster(ArrayList<Task> lstTasksInCluster) {
			this.lstTasksInCluster = lstTasksInCluster;
		}

		
}
