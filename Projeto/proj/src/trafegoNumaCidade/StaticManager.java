package trafegoNumaCidade;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.core.Agent;
import sajas.wrapper.ContainerController;
import trafegoNumaCidade.graph.Graph;
import trafegoNumaCidade.graph.Node;
import trafegoNumaCidade.streetlight.StreetlightCluster;

public class StaticManager extends Agent
{
	ContainerController _container;
	Context<Object> _context;
	
	public StaticManager(ContainerController container)
	{
		_container = container;
		
		try 
		{
			_container.acceptNewAgent("StaticManager", this).start();
		} 
		catch (StaleProxyException e) 
		{
			e.printStackTrace();
		}
	}
	
	@ScheduledMethod(start = 1)
	public void set()
	{
		_context = TrafegoCidadeBuilder.context;
		
		for(String nodeid : TrafegoCidadeBuilder.hasStreetlights)
		{
			Node node = TrafegoCidadeBuilder.idToNode.get(nodeid);
			new StreetlightCluster(node, _container, TrafegoCidadeBuilder.space);
		}
	}
	
	@ScheduledMethod(start = 1000, interval = 80000)
	public void addCar()
	{
		Graph graph = TrafegoCidadeBuilder.graph;
		ArrayList<Node> nodes = graph.getNodes();
		boolean valid = false;
		while(!valid)
		{
			Node start;
			Node end;
			int randomNum;
			if(TrafegoCidadeBuilder.forceSpawn == null)
			{
				randomNum = ThreadLocalRandom.current().nextInt(0, nodes.size());
				start = nodes.get(randomNum);
			}
			else
			{
				start = TrafegoCidadeBuilder.idToNode.get(TrafegoCidadeBuilder.forceSpawn);
			}
			
			if(TrafegoCidadeBuilder.forceGoal == null)
			{
				randomNum = ThreadLocalRandom.current().nextInt(0, nodes.size());
				end = nodes.get(randomNum);
			}
			else
			{
				end = TrafegoCidadeBuilder.idToNode.get(TrafegoCidadeBuilder.forceGoal);
			}
			
			if(start == end)
			{
				continue;
			}
			valid = true;
			
			try
			{
				new CarAgent(_container, TrafegoCidadeBuilder.space, graph, start, end);
			}
			catch(IllegalArgumentException e)
			{
				valid = false;
			}
		}
	}
}
