package trafegoNumaCidade.graph;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import trafegoNumaCidade.MyPoint;

public class Graph implements Cloneable
{
	ArrayList<Node> _nodes = new ArrayList<>();
	public Map<Node, Node> oldToNew = null;
	
	public Graph()
	{
		
	}
	
	@SuppressWarnings("unchecked")
	public Graph(Graph copy)
	{
		_nodes = (ArrayList<Node>)(copy._nodes.clone());
	}
	
	public void addNode(Node node)
	{
		_nodes.add(node);
	}
	
	public ArrayList<Node> getNodes()
	{
		return _nodes;
	}
	
	private Edge[] rebuildPath(Map<Node, SimpleEntry<Edge, Node>> from, Node curr)
	{
		LinkedList<Edge> path = new LinkedList<>();
		while(from.containsKey(curr))
		{
			SimpleEntry<Edge, Node> entry = from.get(curr);
			curr = entry.getValue();
			path.push(entry.getKey());
		}
		return path.toArray(new Edge[path.size()]);
	}
	
	public Edge[] aStar(Node start, Node goal)
	{
		return aStar(start, goal, null);
	}
	
	public Edge[] aStar(Node start, Node goal, Collection<Edge> ignoreEdges)
	{	
		Map<Node, Double> gValue = new HashMap<>();
		gValue.put(start, 0.0);
		
		Map<Node, Double> fValue = new HashMap<>();
		fValue.put(start, start.costHeuristic(goal));
		
		Comparator<Node> nodeCompare = new Comparator<Node>()
		{
			public int compare(Node left, Node right)
			{
				if(fValue.get(left) < fValue.get(right))
				{
					return -1;
				}
				else if(fValue.get(left) > fValue.get(right))
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}
		};
		
		Set<Node> closedSet = new HashSet<>();
		TreeSet<Node> openSet = new TreeSet<>(nodeCompare);
		openSet.add(start);
		Map<Node, SimpleEntry<Edge, Node>> from = new HashMap<>();
		
		while(!openSet.isEmpty())
		{
			Node curr = openSet.pollFirst();
			if(curr == goal)
			{
				return rebuildPath(from, curr);
			}
			
			openSet.remove(curr);
			closedSet.add(curr);
			
			for(Edge neighborEdge : curr._edges)
			{
				if(ignoreEdges != null && ignoreEdges.contains(neighborEdge))
				{
					continue;
				}
				
				Node neighbor = neighborEdge._target;
				
				if(!fValue.containsKey(neighbor))
				{
					fValue.put(neighbor, Double.POSITIVE_INFINITY);
				}
				
				if(!gValue.containsKey(neighbor))
				{
					gValue.put(neighbor, Double.POSITIVE_INFINITY);
				}
				
				if(closedSet.contains(neighbor))
				{
					continue;
				}
				
				if(!openSet.contains(neighbor))
				{
					openSet.add(neighbor);
				}
				
				double possibleGValue = gValue.get(curr) + neighborEdge._cost;
				if(possibleGValue >= gValue.get(neighbor))
				{
					continue;
				}
				
				from.put(neighbor, new SimpleEntry<Edge, Node>(neighborEdge, curr));
				gValue.put(neighbor, possibleGValue);
				fValue.put(neighbor, gValue.get(neighbor) + neighbor.costHeuristic(goal));
			}
		}
		
		return null;
	}
	
	public Node[] getNodesWithinDistance(MyPoint start, double radius)
	{
		LinkedList<Node> nodes = new LinkedList<>();
		
		for(Node node : _nodes)
		{
			if(start.calculateDistanceSquared(node.getLocation()) < Math.pow(radius, 2))
			{
				nodes.add(node);
			}
		}
		
		return nodes.toArray(new Node[nodes.size()]);
	}
}
