package trafegoNumaCidade.streetlight;

import java.util.HashMap;
import java.util.Map;

import sajas.wrapper.ContainerController;
import trafegoNumaCidade.MyPoint;
import trafegoNumaCidade.TNCSpace;
import trafegoNumaCidade.graph.Edge;
import trafegoNumaCidade.graph.Node;

public class StreetlightCluster 
{	
	private Map<Edge, Streetlight> _states = new HashMap<>();
	
	public StreetlightCluster(Node node, ContainerController container, TNCSpace space)
	{
		for(Edge edge : node.getEdges())
		{
			Node targetNode = edge.getTarget();
			MyPoint unitDifference = targetNode.getLocation().subtract(node.getLocation()).unitVector();
			MyPoint multiply = unitDifference.multiply(0.5);
			MyPoint position = node.getLocation().add(multiply);
			_states.put(edge, new Streetlight(position, container));
		}
		
		node.setCluster(this);
	}
	
	public Streetlight.State getState(Edge edge)
	{
		Streetlight streetlight = _states.get(edge);
		
		if(streetlight == null)
		{
			return null;
		}
		else
		{
			return streetlight.getState();
		}
	}
}
