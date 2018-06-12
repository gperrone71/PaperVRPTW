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
	
		private int iClusterRadius;												// radius of the cluster
		private Node ndClusterCenter;											// center of the cluster (not necessarily the position of a task)
		private ArrayList<Task> lstTasksInCluster = new ArrayList<Task>();		// ArrayList containing the list of the Tasks for the cluster

		// getters and setters
		public ArrayList<Task> getLstTasksInCluster() {
			return lstTasksInCluster;
		}

		public void setLstTasksInCluster(ArrayList<Task> lstTasksInCluster) {
			this.lstTasksInCluster = lstTasksInCluster;
		}

		public int getiClusterRadius() {
			return iClusterRadius;
		}

		public void setiClusterRadius(int iClusterRadius) {
			this.iClusterRadius = iClusterRadius;
		}

		public Node getNdClusterCenter() {
			return ndClusterCenter;
		}

		public void setNdClusterCenter(Node ndClusterCenter) {
			this.ndClusterCenter = ndClusterCenter;
		}

		
}
