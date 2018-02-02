package trafegoNumaCidade;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.core.Agent;
import sajas.domain.DFService;
import trafegoNumaCidade.graph.Edge;
import trafegoNumaCidade.graph.Graph;
import trafegoNumaCidade.graph.Node;

public class Radio extends Agent
{
	static public Radio radio;
	
	public Radio()
	{
		try 
		{
			TrafegoCidadeBuilder.agentContainer.acceptNewAgent("Radio", this).start();
			Radio.radio = this;
		} catch (StaleProxyException e) 
		{
			e.printStackTrace();
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1000000)
	public void sendRandomEdge()
	{
		double prob = ThreadLocalRandom.current().nextGaussian();
		if(prob <= 0.2)
		{
			relayMessage(getRandomEdgeString());
		}
	}
	
	private String getRandomEdgeString()
	{
		Graph graph = TrafegoCidadeBuilder.graph;
		String string = null;
		boolean valid = false;
		while(!valid)
		{
			ArrayList<Node> nodes = graph.getNodes();
			int index = ThreadLocalRandom.current().nextInt(0, nodes.size());
			Node node = nodes.get(index);
			if(node.getEdges().length == 0)
			{
				continue;
			}
			index = ThreadLocalRandom.current().nextInt(0, node.getEdges().length);
			Edge edge = node.getEdges()[index];
			Node node2 = edge.getTarget();
			string = new String(TrafegoCidadeBuilder.nodeToID.get(node) + " " + TrafegoCidadeBuilder.nodeToID.get(node2));
			valid = true;
		}
		return string;
	}
	
	private void relayMessage(String message)
	{
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
	
	public void setup()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Radio");
		dfd.addServices(sd);
		try
		{
			DFService.register(this, dfd);
		}
		catch(FIPAException e)
		{
			e.printStackTrace();
		}
	}
}
