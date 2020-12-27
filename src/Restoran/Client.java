package Restoran;

import java.util.function.BooleanSupplier;

import process.Actor;
import process.DispatcherFinishException;
import process.QueueForTransactions;
import process.Store;
import rnd.Randomable;

public class Client extends Actor {

	private QueueForTransactions<Client> queueToOrder;
	private QueueForTransactions<Client> queueToRestoran;
	private Store tables;
	private Randomable rnd;
	private int order;
	private boolean orderDone;
	private boolean entry;
	private BooleanSupplier isOrder;
	private BooleanSupplier isEntry;
	private double finishTime;
	
	
	public Client(String name, RestGui gui, RestModel model) {
		setNameForProtocol(name);
		queueToOrder = model.getQueueToOrder();
		queueToRestoran = model.getQueueToRestoran();
		tables = model.getTables();
		rnd = gui.getRndClient();
		finishTime = gui.getChooseDataFinishTime().getDouble();
	}
	@Override
	protected void rule() throws DispatcherFinishException {
		isOrder = () -> orderDone;
		isEntry = () -> entry;
		while(getDispatcher().getCurrentTime() <= finishTime) {
			orderDone = false;
			entry = false;
			order = 0;
			queueToRestoran.addLast(this);
			waitForCondition(isEntry, "");
			if(tables.getSize() > 1) {
				tables.remove(1);
				order = ((int)(rnd.next()*100) % 3) + 1;
				holdForTime(rnd.next());
				queueToOrder.addLast(this);
				waitForCondition(isOrder, "Чекає заказ");
				holdForTime(rnd.next());
				tables.add(1);
			}
		}
	}
	
	
	public void setOrderDone(boolean orderDone) {
		this.orderDone = orderDone;
	}
	public void setEntry(boolean entry) {
		this.entry = entry;
	}
	public int getOrder() {
		return order;
	}
	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;

	}
}
