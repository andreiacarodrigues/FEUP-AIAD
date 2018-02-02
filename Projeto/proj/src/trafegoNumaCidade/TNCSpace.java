package trafegoNumaCidade;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class TNCSpace
{
	private ContinuousSpace<Object> _space;
	private ConcurrentHashMap<CarAgent, MyPoint> _cars = new ConcurrentHashMap<>();
	
	public TNCSpace(ContinuousSpace<Object> space)
	{
		_space = space;
	}
	
	public void addCar(CarAgent car)
	{
		_space.getAdder().add(_space, car);
		_cars.put(car, new MyPoint(0, 0));
	}
	
	public void removeCar(CarAgent car)
	{
		_cars.remove(car);
	}
	
	public void addStatic(StaticComponent comp)
	{
		_space.getAdder().add(_space, comp);
		_space.moveTo(comp, comp.getPosition().toDoubleArray(null));
	}
	
	public void moveCar(CarAgent car, double x, double y)
	{
		_space.moveTo(car, x, y);
		_cars.put(car, new MyPoint(x, y));
	}
	
	public void moveCarByDisplacement(CarAgent car, double x, double y)
	{
		NdPoint newLocation = _space.moveByDisplacement(car, x, y);
		MyPoint newPoint = new MyPoint(newLocation.toDoubleArray(null));
		_cars.put(car, newPoint);
	}
	
	public LinkedList<CarAgent> getCarsInRadiusOf(CarAgent car, double radius)
	{
		LinkedList<CarAgent> cars = new LinkedList<>();
		MyPoint carPos = _cars.get(car);
		
		for(Entry<CarAgent, MyPoint> currCar : _cars.entrySet())
		{
			if(currCar.getKey() == car)
			{
				continue;
			}
			
			if(carPos.calculateDistanceSquared(currCar.getValue()) < Math.pow(radius, carPos.dimensionCount()))
			{
				cars.add(currCar.getKey());
			}
		}
		
		return cars;
	}
	
	public MyPoint getCarLocation(CarAgent car)
	{
		return _cars.get(car);
	}
}
