package Restoran;

import java.util.Iterator;
import java.util.function.BooleanSupplier;

import process.Actor;
import process.DispatcherFinishException;
import process.QueueForTransactions;
import rnd.Randomable;

public class Waiter extends Actor {
	private QueueForTransactions<Client> queueToOrder;
	private QueueForTransactions<Client> queueToWaiting;
	private double finishTime;
	private BooleanSupplier isWork;
	private QueueForTransactions<Integer> queueOfKitchen;
	private int order;
	private Client client;
	private QueueForTransactions<Integer> queueInKitchen;
	private Randomable rnd;
	
	public Waiter(String name, RestGui gui, RestModel model) {
		setNameForProtocol(name);
		queueToOrder = model.getQueueToOrder();
		queueToWaiting = model.getQueueToWaiting();
		queueOfKitchen = model.getQueueOfKitchen();
		queueInKitchen = model.getQueueInKitchen();
		rnd = gui.getRndWaiter();
		finishTime = gui.getChooseDataFinishTime().getDouble();
		
		setHistoForActorWaitingTime(model.getHistoWaiter());
	}
	@Override
	protected void rule() throws DispatcherFinishException {
		isWork = () -> (queueToOrder.size() > 0 || queueOfKitchen.size() > 0 );
		while(getDispatcher().getCurrentTime() <= finishTime) {
			waitForCondition(isWork, "Чекає на замовлення або видачу з кухні");
			if(queueOfKitchen.size() > 0) {
				order = queueOfKitchen.removeFirst();
				Iterator<Client> itr = queueToWaiting.iterator();
				while(itr.hasNext()) {
					client = itr.next();
					if(client.getOrder() == order) {
						queueToWaiting.remove(client);
						getDispatcher().printToProtocol(getNameForProtocol() + " віддав замовлення клієнту");
						holdForTime(rnd.next());
						client.setOrderDone(true);
						break;
					}					
				}
			}
			if(queueToOrder.size() > 0) {
				client = queueToOrder.removeFirst();
				queueInKitchen.addLast(client.getOrder());
				queueToWaiting.addLast(client);
				getDispatcher().printToProtocol(getNameForProtocol() + " віддав замолення клієнта на кухню");
				holdForTime(rnd.next());
			}
			
		}
		
		
	}
	
	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;

	}
}
