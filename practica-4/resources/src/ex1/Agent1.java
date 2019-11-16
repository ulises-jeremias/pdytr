import jade.core.*;
import jade.util.leap.Serializable;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.Iterator;

public class Agent1 extends Agent
{
	private Location origin;
	private long startTime;
	private ArrayList<String> containers = new ArrayList<String>();
	private int index;
	private ArrayList<ContainerInfo> info = new ArrayList<ContainerInfo>();
	
	public void printAgentPresentation() {
		System.out.println("Hi agent with local name: " + getLocalName());
		System.out.println("and full name: " + getName());
	}

	public void setup() {
		this.origin = here();
                
		this.printAgentPresentation();
		System.out.println("in location: " + this.origin.getID());

		for (int i = 0; i < 10; i++) {
			String containerName = "Container-" + i;
                        
			if (!containerName.equals(origin.getName())) {
				createContainer(containerName);
				containers.add(containerName);
			}
		}
                
		containers.add(this.origin.getName());

		try {
			index = 0;

			ContainerID destination = new ContainerID(containers.get(index++), null);
			System.out.println("Migrating agent to " + destination.getID());
			startTime = System.currentTimeMillis();
			doMove(destination);
		} catch (Exception e) {
			System.out.println("Error: It was not posible to migrate the agent!");
		}
	}

	protected void afterMove() {
		Location actual = here();
                
		if (actual.getName().equals(origin.getName())) {
			this.originAction();
		} else {
			this.remoteAction(actual);
		}
	}

	private void remoteAction(Location actual) {
		long startContainerTime = System.currentTimeMillis();
		ContainerInfo currentContainerInfo = new ContainerInfo();
		this.printAgentPresentation();
		currentContainerInfo.setFreeMemory(java.lang.Runtime.getRuntime().freeMemory());
		currentContainerInfo.setName(actual.getName());
                
		try {
			ContainerID destination = new ContainerID(containers.get(index++), null);
			System.out.println("Migratin agent to " + destination.getID());
			info.add(currentContainerInfo);
			long finishContainerTime = System.currentTimeMillis() - startContainerTime;
			currentContainerInfo.setProcessingTime(finishContainerTime);
			doMove(destination);
		} catch (Exception e) {
			System.out.println("Error: It was not posible to migrate the agent!");
			System.out.println(e);
		}
	}

	private void originAction() {
		System.out.println("Finished! Total time: " + (System.currentTimeMillis() - startTime) + "ms");

		for (ContainerInfo container : info) System.out.println(container.getInfoAsString());
	}

	protected ContainerController createContainer(String name) {
		// Get the JADE runtime interface (singleton)
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, name);
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		// create a non-main agent container
		return runtime.createAgentContainer(profile);
	}

	public class ContainerInfo implements Serializable {
		private long freeMemory;
		private String name;
		private long processingTime;

		public long getFreeMemory() {
			return freeMemory;
		}

		public void setFreeMemory(long freeMemory) {
			this.freeMemory = freeMemory;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getProcessingTime() {
			return processingTime;
		}

		public void setProcessingTime(long processingTime) {
			this.processingTime = processingTime;
		}
                
		public String getInfoAsString() {
			float processingPercentage = (getProcessingTime() * 100) / (System.currentTimeMillis() - startTime);
			String str = "";

			str += "Container " + getName() + " information:";
			str += "\n\tFree memory: " + (getFreeMemory() / 1024) / 1024 + "Mb";
			str += "\n\tProcessing time: " + getProcessingTime() + "ms";
			str += "\n\tProcessing load: " + processingPercentage + "%";
                        
			return str;
		}
	}
}
