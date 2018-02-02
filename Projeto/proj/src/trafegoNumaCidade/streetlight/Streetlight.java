package trafegoNumaCidade.streetlight;

import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.core.Agent;
import sajas.wrapper.ContainerController;
import trafegoNumaCidade.MyPoint;
import trafegoNumaCidade.TNCSpace;
import trafegoNumaCidade.TrafegoCidadeBuilder;

public class Streetlight extends Agent
{
	public enum State
	{
		GO, STOP
	};

	private MyPoint _location;
	private int _interval;
	private int _timeLeft;
	private State _state;
	private StreetlightStatic _static;
	private Context<Object> _context;
	private TNCSpace _space = TrafegoCidadeBuilder.space;

	public Streetlight(MyPoint location, ContainerController container)
	{
		this._location = location;
		this._interval = ThreadLocalRandom.current().nextInt(1, 10);
		this._timeLeft = _interval;
		this._state = State.GO;
		this._static = null;
		_context = TrafegoCidadeBuilder.context;
		_context.add(this);
		setStatic();
	}
	
	public State getState()
	{
		return _state;
	}

	@ScheduledMethod(start = 1000, interval = 200000)
	public void updateStreetlight()
	{
		this._timeLeft--;
		if (_timeLeft <= 0)
		{
			this.toggleStreetlight();
		}
	}

	private void toggleStreetlight()
	{
		if (this._state == State.GO)
		{
			this._state = State.STOP;
		} else if (_state == State.STOP)
		{
			this._state = State.GO;
		} else
		{
			return;
		}
		
		this.deleteStatic();
		this.setStatic();
	}
	
	private void setStatic()
	{
		if(_state == State.GO)
		{
			_static = new GoStreetlight(_location);
		}
		else if(_state == State.STOP)
		{
			_static = new StopStreetlight(_location);
		}
		else
		{
			return;
		}
		_context.add(_static);
		_space.addStatic(_static);
	}
	
	private void deleteStatic()
	{
		if(_static != null)
		{
			_context.remove(_static);
			_static = null;
		}
	}
}
