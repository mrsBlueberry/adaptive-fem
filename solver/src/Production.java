

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class Production implements Runnable, Node {
	
	public CountDownLatch latch;
	public Vertex vertex;
	private List<Node> inNodes;
	
	
	
	public Production(Vertex vert){
		
		this.vertex = vert;
		
	}

	@Override
	public void run() {
		try{
			apply(vertex);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		getLatch().countDown();
		
	}
	
	public abstract void apply(Vertex vert);

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	@Override
	public Collection<Node> getInNodes() {
		return inNodes;
	}

	@Override
	public String getName() {
		return null;
	} 

}

