package trafegoNumaCidade.graph;

import java.util.ArrayList;

import trafegoNumaCidade.MyPoint;
import trafegoNumaCidade.StaticComponent;
import trafegoNumaCidade.streetlight.StreetlightCluster;

public class Node extends StaticComponent
{
	static int nextID = 1;
	
	int ID;
	ArrayList<Edge> _edges = new ArrayList<>();
	StreetlightCluster _cluster = null;
	
	public Node(MyPoint location)
	{
		super(location);
		ID = nextID++;
	}
	
	public Node(int ID, MyPoint location)
	{
		super(location);
		this.ID = ID;
	}
	
	public Node(double x, double y)
	{
		this(new MyPoint(x, y));
	}
	
	public void addEdge(Node target)
	{
		MyPoint targetPos = target.getLocation();
		MyPoint difference = targetPos.subtract(this.getPosition());
		double distance = difference.magnitude();
		_edges.add(new Edge(target, distance));
	}
	
	public void removeEdge(Edge edge)
	{
		_edges.remove(edge);
	}
	
	public MyPoint getLocation()
	{
		return this.getPosition();
	}
	
	public Edge[] getEdges()
	{
		return _edges.toArray(new Edge[_edges.size()]);
	}
	
	@Override
	public int hashCode()
	{
		return ID;
	}
	
	public double costHeuristic(Node other)
	{
		assert(other.getPosition().dimensionCount() == 2 && this.getPosition().dimensionCount() == 2);
		
		return this.getPosition().calculateDistance(other.getPosition());
	}
	
	public StreetlightCluster getCluster()
	{
		return _cluster;
	}
	
	public void setCluster(StreetlightCluster cluster)
	{
		_cluster = cluster;
	}
}
