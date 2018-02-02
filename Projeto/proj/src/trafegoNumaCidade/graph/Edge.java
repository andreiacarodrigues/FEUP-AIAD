package trafegoNumaCidade.graph;

public class Edge 
{
	Node _target;
	double _cost;
	
	public Edge(Node target, double cost)
	{
		_target = target;
		_cost = cost;
	}
	
	public Node getTarget()
	{
		return _target;
	}
	
	public double getCost()
	{
		return _cost;
	}
}
