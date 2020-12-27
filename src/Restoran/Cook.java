package Restoran;

import java.util.function.BooleanSupplier;

import process.Actor;
import process.DispatcherFinishException;
import process.QueueForTransactions;
import rnd.Randomable;

public class Cook extends Actor{
	private QueueForTransactions<Integer> queueInKitchen;
	private QueueForTransactions<Integer> queueOfKitchen;
	private int order;
	private double finishTime;
	private BooleanSupplier isOrder;
	private Randomable rnd;
	
	public Cook(String name, RestGui gui, RestModel model) {
		setNameForProtocol(name);
		queueOfKitchen = model.getQueueOfKitchen();
		queueInKitchen = model.getQueueInKitchen();
		rnd = gui.getRndCook();
		finishTime = gui.getChooseDataFinishTime().getDouble();
		setHistoForActorWaitingTime(model.getHistoCook());
	}
	@Override
	protected void rule() throws DispatcherFinishException {
		isOrder = () -> queueInKitchen.size() >0;
		while(getDispatcher().getCurrentTime() <= finishTime) {
			waitForCondition(isOrder, "Чекає на замовлення");
			order = queueInKitchen.removeFirst();
			holdForTime(rnd.next());
			queueOfKitchen.addLast(order);
		}
		
	}
	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;

	}
}
