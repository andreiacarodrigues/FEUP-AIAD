package trafegoNumaCidade;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import trafegoNumaCidade.graph.Graph;
import trafegoNumaCidade.graph.Node;

public class TrafegoCidadeBuilder extends RepastSLauncher {
	
	public static class Enum
	{
		public enum CarAgentSpeed
		{
			Slow(0.2, 0.8),
			Normal(0.5, 1.0),
			Fast(0.2, 1.2),
			VeryFast(0.1, 1.5);
			
			public final double Prob;
			public final double SpeedFactor;
			
			private CarAgentSpeed(double prob, double speed)
			{
				Prob = prob;
				SpeedFactor = speed;
			}
		}
	}
	
	public static TNCSpace space;
	
	ArrayList<CarAgent> cars = new ArrayList<CarAgent>();
	
	ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
	
	public static Context<Object> context;
	
	private static Map map;
	
	private static ArrayList<String> nodeident = new ArrayList<>();
	public static ArrayList<String> hasStreetlights = new ArrayList<>();
	private static HashMap<String, MyPoint> locations = new HashMap<>();
	private static HashMap<String, ArrayList<String>> edges = new HashMap<>();
	
	public static String forceSpawn = null;
	public static String forceGoal = null;
	
	public static HashMap<String, Node> idToNode = new HashMap<>();
	public static HashMap<Node, String> nodeToID = new HashMap<>();
	
	static int counter = 0;
	
	public static Graph graph = new Graph();
	
	static
	{
		initFile();
		
		for(java.util.Map.Entry<String, MyPoint> entry : locations.entrySet())
		{
			Node newNode = new Node(entry.getValue().getCoord(0), entry.getValue().getCoord(1));
			idToNode.put(entry.getKey(), newNode);
			nodeToID.put(newNode, entry.getKey());
			graph.addNode(newNode);
		}
		
		for(java.util.Map.Entry<String, ArrayList<String>> edge : edges.entrySet())
		{
			Node keynode = idToNode.get(edge.getKey());
			if(keynode == null)
			{
				continue;
			}
			for(String target : edge.getValue())
			{
				Node targetnode = idToNode.get(target);
				if(targetnode == null)
				{
					continue;
				}
				keynode.addEdge(targetnode);
			}
		}
		
		map = new Map(graph);
	}
	public static ContainerController agentContainer;

	@Override
	public String getName() 
	{
		return "Street map -- SAJaS RepastS Test";
	}
	
	private static void initFile()
	{
		File f = new File("data");

	    try 
	    {
	        Scanner scanner = new Scanner(f);

	        while(scanner.hasNextLine())
	        {
	        	switch(scanner.next())
	        	{
	        	case "N":
	        		parseNode(scanner);
	        		break;
	        	case "E":
	        		parseEdge(scanner);
	        		break;
	        	case "S":
	        		parseStreetlight(scanner);
	        		break;
	        	case "FS":
	        		parseForceSpawn(scanner);
	        		break;
	        	case "FG":
	        		parseForceGoal(scanner);
	        		break;
	        	default:
	        		break;
	        	}
	        	scanner.nextLine();
	        }
	    } 
	    catch (FileNotFoundException ex) 
	    {
	        ex.printStackTrace();
	    }
	}
	
	private static void parseNode(Scanner scanner)
	{
		String nodeid = scanner.next();
		nodeident.add(nodeid);
		double first = scanner.nextDouble();
		double second = scanner.nextDouble();
		locations.put(nodeid, new MyPoint(first, second));
	}
	
	private static void parseEdge(Scanner scanner)
	{
		String keyNode = scanner.next();
		ArrayList<String> targets = edges.get(keyNode);
		if(targets == null)
		{
			targets = new ArrayList<String>();
			edges.put(keyNode, targets); 
		}
		String targetNode = scanner.next();
		targets.add(targetNode);
	}
	
	private static void parseStreetlight(Scanner scanner)
	{
		String node = scanner.next();
		hasStreetlights.add(node);
	}
	
	private static void parseForceSpawn(Scanner scanner)
	{
		String node = scanner.next();
		forceSpawn = node;
	}
	
	private static void parseForceGoal(Scanner scanner)
	{
		String node = scanner.next();
		forceGoal = node;
	}

	@Override
	protected void launchJADE() 
	{	
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		agentContainer = rt.createMainContainer(p1);
		
		launchAgents();
	}
	
	private void launchAgents() 
	{
		new StaticManager(agentContainer);
		new Radio();
	}
	
	@Override
	public Context<Object> build(Context<Object> context) 
	{		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> objectSpace = spaceFactory.createContinuousSpace(Constants.projectionName, context, new SimpleCartesianAdder<>(), new StrictBorders(), 50, 50);
		space = new TNCSpace(objectSpace);
		
		for(Road road : map.getRoad())
		{
			context.add(road);
			space.addStatic(road);
		}
		
		for(Node node : map.getNode())
		{
			context.add(node);
			space.addStatic(node);
		}
		
		for(RoadWayIndicator indi : map.getIndicator())
		{
			context.add(indi);
			space.addStatic(indi);
		}
		@SuppressWarnings("unchecked")
		Context<Object> contextt = super.build(context);
		TrafegoCidadeBuilder.context = contextt;
		return contextt;
	}

}
