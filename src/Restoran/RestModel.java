package Restoran;

import java.util.HashMap;
import java.util.Map;

import process.Dispatcher;
import process.MultiActor;
import process.QueueForTransactions;
import process.Store;
import stat.DiscretHisto;
import stat.Histo;
import stat.IHisto;
import widgets.Painter;
import widgets.experiments.IExperimentable;
import widgets.stat.IStatisticsable;
import widgets.trans.ITransMonitoring;
import widgets.trans.ITransProcesable;

public class RestModel implements IExperimentable, ITransProcesable,
IStatisticsable {

	private Dispatcher dispatcher;
	private RestGui gui;
	private Store tables;
 	private QueueForTransactions<Integer> queueInKitchen;
 	private QueueForTransactions<Integer> queueOfKitchen;
 	private QueueForTransactions<Client> queueToOrder;
 	private QueueForTransactions<Client> queueToWaiting;
 	private QueueForTransactions<Client> queueToRestoran;
	private Histo histoWaiter;
	private Histo histoCook;
	private Client client;
	private Cook cook;
	private Waiter waiter;
	private Restoran restoran;
	private MultiActor	multiClient;
	private MultiActor multiCook;
	private MultiActor multiWaiter;
	private Histo histoTable;
	private DiscretHisto histoForQueueOfKitchen;
	private DiscretHisto histoForQueueToOrder;
	
	
	
	public RestModel(Dispatcher d, RestGui restGui) {
		if (d == null || restGui == null) {
			System.out.println("Не визначено диспетчера або GUI для RgrModel");
			System.out.println("Подальша робота неможлива");
			System.exit(0);
		}
		dispatcher = d;
		gui = restGui;
		componentsToStartList();
	}
	
	private void componentsToStartList() {
		// TODO Auto-generated method stub
		dispatcher.addStartingActor(getMultiWaiter());
		dispatcher.addStartingActor(getMultiCook());
		dispatcher.addStartingActor(getMultiClient());
		dispatcher.addStartingActor(getRestoran());
	}

	public Client getClient() {
		if(client == null) {
			client = new Client("Клієнт", gui, this);
		}
		return client;
	}
	
	public Cook getCook() {
		if(cook == null) {
			cook = new Cook("Кухар", gui, this);
		}
		return cook;
	}
	
	public Waiter getWaiter() {
		if(waiter == null) {
			waiter = new Waiter("Офіціант", gui, this);
		}
		return waiter;
	}
	
	public Restoran getRestoran() {
		if(restoran == null) {
			restoran = new Restoran("Ресторан", gui, this);
		}
		return restoran;
	}
	
	public MultiActor getMultiClient() {
		if(multiClient == null) {
			multiClient = new MultiActor("Група клієнтів", getClient(), 20);
		}
		return multiClient;
	}
	
	public MultiActor getMultiCook() {
		if(multiCook == null) {
			multiCook = new MultiActor("Група кухарів", getCook(), gui.getChooseDataNCook().getInt());
		}
		return multiCook;
	}
	
	public MultiActor getMultiWaiter() {
		if(multiWaiter == null) {
			multiWaiter = new MultiActor("Група офіціантів", getWaiter(), gui.getChooseDataNWaiter().getInt());
		}
		return multiWaiter;
	}
	public Store getTables() {
		if(tables == null) {
			tables = new Store("Столи",dispatcher, getHistoTables());
			tables.add(gui.getChooseDataNTables().getDouble());
		}
		return tables;
	}

	public QueueForTransactions<Integer> getQueueInKitchen() {
		if(queueInKitchen == null) {
			queueInKitchen = new QueueForTransactions<Integer>("Закази на приготування", dispatcher);
		}
		return queueInKitchen;
	}
	
	

	public QueueForTransactions<Integer> getQueueOfKitchen() {
		if(queueOfKitchen == null) {
			queueOfKitchen = new QueueForTransactions<Integer>("Готові закази", dispatcher, getHistoForQueueOfKitchen());
		}
		return queueOfKitchen;
	}
	
	public QueueForTransactions<Client> getQueueToRestoran(){
		if(queueToRestoran == null) {
			queueToRestoran = new QueueForTransactions<Client>("",dispatcher);
		}
		return queueToRestoran;
	}
	
	public QueueForTransactions<Client> getQueueToOrder() {
		if(queueToOrder == null) {
			queueToOrder = new QueueForTransactions<Client>("Черга на обслуговування", dispatcher, getHistoForQueueToOrder());
		}
		return queueToOrder;
	}

	public QueueForTransactions<Client> getQueueToWaiting() {
		if(queueToWaiting == null) {
			queueToWaiting = new QueueForTransactions<Client>("Очікування заказу", dispatcher);
		}
		return queueToWaiting;
	}

	public void initForTest() {
		// TODO Auto-generated method stub
		getTables().setPainter(gui.getDiagramFreeTables().getPainter());
		getQueueToOrder().setPainter(gui.getDiagramQueueToOrder().getPainter());
		getQueueOfKitchen().setPainter(gui.getDiagramQueueOfKitchen().getPainter());
		if (gui.getJCheckBox().isSelected())
			dispatcher.setProtocolFileName("Console");
	}
	
	@Override
	public Map<String, IHisto> getStatistics() {
		Map<String, IHisto> map = new HashMap<>();
		map.put("Гістограма для вільних столів",
				getHistoTables());
		map.put("Гістограма для черги на обслуговування", getHistoForQueueToOrder());
		map.put("Гістограма для черги готових блюд", getHistoForQueueOfKitchen());
		map.put("Гістограма для часу простою кухара", getHistoCook());
		map.put("Гістограма для часу простою офіціанта", getHistoWaiter());
		return map;
	}

	@Override
	public void initForStatistics() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initForTrans(double finishTime) {
		getCook().setFinishTime(finishTime);
		getClient().setFinishTime(finishTime);
		getWaiter().setFinishTime(finishTime);
		getRestoran().setFinishTime(finishTime);
		gui.getChooseDataFinishTime().setDouble(finishTime);
	}

	@Override
	public Map<String, ITransMonitoring> getMonitoringObjects() {
		Map<String, ITransMonitoring> transMap = new HashMap<>();
		transMap.put("Вільні столи", getTables());
		transMap.put("Черга на обслуговування", getQueueToOrder());
		transMap.put("Черга на видачу блюд", getQueueOfKitchen());
		return transMap;
	}

	@Override
	public void initForExperiment(double factor) {
		multiClient.setNumberOfClones((int)factor);
	}

	@Override
	public Map<String, Double> getResultOfExperiment() {
		Map<String, Double> resultMap = new HashMap<>();
		resultMap.put("Час простою офіціантів від їх кількості", getHistoWaiter().average());
		resultMap.put("Кількість вільних столів від кількості офіціантів", getHistoTables().average());
		resultMap.put("Час простою кухіра від кількості офіціантів",	getHistoCook().average());
		return resultMap;
	}


	public Histo getHistoWaiter() {
		if(histoWaiter == null) {
			histoWaiter = new Histo();
		}
		return histoWaiter;
	}


	public Histo getHistoCook() {
		if(histoCook == null) {
			histoCook = new Histo();
		}
		return histoCook;
	}
	
	private Histo getHistoTables() {
		if(histoTable == null) {
			histoTable = new Histo();
		}
		return histoTable;
	}
	
	private DiscretHisto getHistoForQueueOfKitchen() {
		if(histoForQueueOfKitchen == null) {
			histoForQueueOfKitchen = new DiscretHisto();
		}
		return histoForQueueOfKitchen;
	}
	
	private DiscretHisto getHistoForQueueToOrder() {
		if(histoForQueueToOrder == null) {
			histoForQueueToOrder = new DiscretHisto();
		}
		return histoForQueueToOrder;
	}

}
