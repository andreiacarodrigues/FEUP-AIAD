package trafegoNumaCidade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import sajas.wrapper.ContainerController;
import trafegoNumaCidade.graph.Edge;
import trafegoNumaCidade.graph.Graph;
import trafegoNumaCidade.graph.Node;
import trafegoNumaCidade.streetlight.Streetlight;
import trafegoNumaCidade.streetlight.StreetlightCluster;

public class CarAgent extends Agent 
{

	private ContainerController _container;
	private TNCSpace spaceCont;
	private Graph _graph;
	private Node _goal;
	private Node _curr;
	private Node nextNode;
	private Edge[] _path;
	private boolean next = true;
	private int step = 0;
	private int stepCount = 0;
	private MyPoint stepVector;
	private double distance = 0;
	private double speedFactor = 0.1;
	private double distanceToNext;
	private boolean done = false;
	private TrafegoCidadeBuilder.Enum.CarAgentSpeed speedPersonality;
	private int expectedSteps;
	private ArrayList<Edge> ignoreEdges = new ArrayList<>();
	private int globalStepCount = 0;
	
	int sign = 1;
	
	static int nextID = 1;
	private int ID = nextID++;
	
	public CarAgent(ContainerController container, TNCSpace space, Graph graph, Node start, Node goal) 
	{
		_container = container;
		spaceCont = space;
		_goal = goal;
		_graph = graph;
		_curr = start;
		_path = _graph.aStar(start, goal);
		double randomNum = ThreadLocalRandom.current().nextDouble(1.0);
		double sumprob = 0;
		if(randomNum < TrafegoCidadeBuilder.Enum.CarAgentSpeed.Slow.Prob)
		{
			speedPersonality = TrafegoCidadeBuilder.Enum.CarAgentSpeed.Slow;
		}
		else
		{
			sumprob += TrafegoCidadeBuilder.Enum.CarAgentSpeed.Slow.Prob;
			if(randomNum < TrafegoCidadeBuilder.Enum.CarAgentSpeed.Normal.Prob + sumprob)
			{
				speedPersonality = TrafegoCidadeBuilder.Enum.CarAgentSpeed.Normal;
			}
			else
			{
				sumprob += TrafegoCidadeBuilder.Enum.CarAgentSpeed.Normal.Prob;
				if(randomNum < TrafegoCidadeBuilder.Enum.CarAgentSpeed.Fast.Prob + sumprob)
				{
					speedPersonality = TrafegoCidadeBuilder.Enum.CarAgentSpeed.Fast;
				}
				else
				{
					speedPersonality = TrafegoCidadeBuilder.Enum.CarAgentSpeed.VeryFast;
				}
			}
		}
		speedFactor = 0.1*speedPersonality.SpeedFactor;
		if(_path == null)
		{
			throw new IllegalArgumentException();
		}
		try 
		{
			_container.acceptNewAgent("Car" + ID, this).start();
		} 
		catch (StaleProxyException e) 
		{
			e.printStackTrace();
		}
		spaceCont.addCar(this);
		spaceCont.moveCar(this, _curr.getLocation().getCoord(0), _curr.getLocation().getCoord(1));
	}
	
	@ScheduledMethod(start=1 , interval=2000)
	public void updateCarsPosition()
	{
		if(done)
		{
			return;
		}
		
		if(next)
		{
			next = false;
			if(step >= _path.length)
			{
				dispose();
				done = true;
				return;
			}
			Edge nextEdge = _path[step];
			nextNode = nextEdge.getTarget();
			try
			{
				spaceCont.moveCar(this, _curr.getLocation().getCoord(0), _curr.getLocation().getCoord(1));
			}
			catch(IllegalArgumentException e)
			{
				return;
			}
			
			MyPoint vector = nextNode.getLocation().subtract(_curr.getLocation());
			distanceToNext = vector.magnitude();
			MyPoint unit = vector.unitVector();
			MyPoint perpendicular = unit.clockwisePerpendicularVector().multiply(0.5);
			spaceCont.moveCarByDisplacement(this, perpendicular.getCoord(0), perpendicular.getCoord(1));
			stepVector = unit.multiply(speedFactor);
			expectedSteps = (int) Math.ceil(distanceToNext/speedFactor);
			step++;
		}
		
		stepCount++;
		globalStepCount++;
		
		if(this.hasCarsInFront())
		{
			return;
		}
		
		double differenceToNext = distanceToNext - distance;
		if(differenceToNext <= 1 && differenceToNext > 0.5)
		{
			StreetlightCluster cluster = nextNode.getCluster();
			
			if(cluster != null)
			{
				if(step < _path.length)
				{
					Streetlight.State state = cluster.getState(_path[step]);
					if(state == Streetlight.State.STOP)
					{
						return;
					}
				}
			}
		}
		
		spaceCont.moveCarByDisplacement(this, stepVector.getCoord(0), stepVector.getCoord(1));
		distance += speedFactor;
		
		if(distance >= distanceToNext)
		{
			if(stepCount > expectedSteps*2)
			{
				sendMessageToOtherCars(_curr, nextNode);
			}
			_curr = nextNode;
			stepCount = 0;
			distance = 0;
			next = true;
		}
	}
	
	private void dispose()
	{
		ResultsManager.addResult(globalStepCount);
		System.out.println(ResultsManager.getCurrentValue() + " for " + ResultsManager.getCurrentCount() + " vehicles.");
		_container.removeLocalAgent(this);
		spaceCont.removeCar(this);
	}
	
	private Predicate<CarAgent> isNotWithinAngleVar()
	{
		return 
		c -> 
		{
			MyPoint counterClock = this.stepVector.rotate2D(Math.toRadians(2));
			MyPoint clock = this.stepVector.rotate2D(Math.toRadians(-2));
			
			MyPoint ref = this.spaceCont.getCarLocation(c).subtract(this.spaceCont.getCarLocation(this));
			
			boolean isClock = ref.isClockwiseTo(counterClock);
			boolean isCounterClock = clock.isClockwiseTo(ref);
			
			return !(isClock && isCounterClock);
		};
	}
	
	private void sendMessageToOtherCars(Node origin, Node target)
	{
		String originID = TrafegoCidadeBuilder.nodeToID.get(origin);
		String targetID = TrafegoCidadeBuilder.nodeToID.get(target);
		String message = new String(originID + " " + targetID);
		Context<Object> context = TrafegoCidadeBuilder.context;
		Iterable<Object> carList = context.getRandomObjects(CarAgent.class, 10);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		for(Object obj : carList)
		{
			CarAgent car = (CarAgent)obj;
			msg.addReceiver(car.getAID());
		}
		msg.setContent(message);
		send(msg);
	}
	
	private boolean hasCarsInFront()
	{
		Collection<CarAgent> carsInOuterRadius = spaceCont.getCarsInRadiusOf(this, 2);
		carsInOuterRadius.removeIf(this.isNotWithinAngleVar());
		return !carsInOuterRadius.isEmpty();
	}
	
	public void avoidEdge(String originID, String targetID)
	{
		Node originNode = null;
		Node targetNode = null;
		
		for(Map.Entry<String, Node> entry : TrafegoCidadeBuilder.idToNode.entrySet())
		{
			if(entry.getKey().equals(originID))
			{
				originNode = entry.getValue();
			}
			else if(entry.getKey().equals(targetID))
			{
				targetNode = entry.getValue();
			}
			
		}
		
		if(originNode == null || targetNode == null)
		{
			return;
		}
		
		for(Node node : _graph.getNodes())
		{
			if(node == originNode)
			{
				for(Edge edge : node.getEdges())
				{
					if(edge.getTarget() == targetNode)
					{
						this.ignoreEdges.add(edge);
						Edge[] newPath = _graph.aStar(nextNode, _goal, ignoreEdges);
						if(newPath != null)
						{
							_path = newPath;
							step = 0;
						}
					}
				}
			}
		}
	}
	
	class ReactRadio extends SimpleBehaviour
	{
		private static final long serialVersionUID = 1L;
		
		public ReactRadio(CarAgent car)
		{
			super(car);
		}
		
		@Override
		public void action() 
		{
			ACLMessage msg = receive();
			if(msg == null)
			{
				return;
			}
			if(msg.getPerformative() == ACLMessage.INFORM)
			{
				Scanner scanner = new Scanner(msg.getContent());
				
				System.out.println(getLocalName() + ":" + msg.getContent());
				
				String node1 = scanner.next();
				String node2 = scanner.next();
				
				scanner.close();
				
				avoidEdge(node1, node2);
			}
		}

		@Override
		public boolean done()
		{
			return false;
		}
	}
	
	@Override
	protected void setup()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Car");
		dfd.addServices(sd);
		try
		{
			DFService.register(this, dfd);
		}
		catch(FIPAException e)
		{
			e.printStackTrace();
		}
		ReactRadio rr = new ReactRadio(this);
		addBehaviour(rr);
	}
	
	@Override
	public int hashCode()
	{
		return ID;
	}
}
