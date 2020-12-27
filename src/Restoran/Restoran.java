package Restoran;

import java.util.function.BooleanSupplier;

import process.Actor;
import process.DispatcherFinishException;
import process.QueueForTransactions;
import rnd.Randomable;

public class Restoran extends Actor {
	private QueueForTransactions<Client> queueToRestoran;
	private Randomable rnd;
	private BooleanSupplier isClient;
	private double finishTime;
	private Client client;
	
	public Restoran(String name, RestGui gui, RestModel model) {
		setNameForProtocol(name);
		queueToRestoran = model.getQueueToRestoran();
		finishTime = gui.getChooseDataFinishTime().getDouble();
		rnd = gui.getRndClient();
	}
	@Override
	protected void rule() throws DispatcherFinishException {
		isClient = () -> queueToRestoran.size() > 0;
		while(getDispatcher().getCurrentTime() <= finishTime) {
			waitForCondition(isClient, "");
			client = queueToRestoran.removeFirst();
			getDispatcher().printToProtocol(getNameForProtocol() + " запускає клієнта");
			client.setEntry(true);
			holdForTime(rnd.next() * (100/(getDispatcher().getCurrentTime()+1)));
		}
		
	}
	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;

	}
}
