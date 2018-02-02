package trafegoNumaCidade;

import java.util.ArrayList;

import trafegoNumaCidade.graph.Edge;
import trafegoNumaCidade.graph.Graph;
import trafegoNumaCidade.graph.Node;

public class Map 
{
	private Graph _graph;
	private ArrayList<Road> _road = new ArrayList<>();
	private ArrayList<Node> _node = new ArrayList<>();
	private ArrayList<RoadWayIndicator> _indicator = new ArrayList<>();
	
	public Map(Graph graph)
	{
		_graph = graph;
		for(Node node : _graph.getNodes())
		{
			_node.add(node);
			for(Edge edge : node.getEdges())
			{
				Node target = edge.getTarget();
				MyPoint difference = target.getLocation().subtract(node.getLocation());
				MyPoint unitVector = difference.unitVector();
				MyPoint perpendicular = unitVector.clockwisePerpendicularVector().unitVector().multiply(0.5);
				MyPoint unitplusperp = unitVector.add(perpendicular);
				int magnitudeFloor = (int)Math.floor(difference.magnitude());
				MyPoint start = node.getLocation();
				_indicator.add(new RoadWayIndicator(start.add(unitplusperp)));
				for(int i = 0; i < magnitudeFloor; i++)
				{
					start = start.add(unitVector);
					_road.add(new Road(start));
				}
			}
		}
	}
	
	public ArrayList<Road> getRoad()
	{
		return _road;
	}
	
	public ArrayList<Node> getNode()
	{
		return _node;
	}
	
	public ArrayList<RoadWayIndicator> getIndicator()
	{
		return _indicator;
	}
}
