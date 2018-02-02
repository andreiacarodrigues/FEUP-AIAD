package trafegoNumaCidade;

public abstract class StaticComponent 
{
	private MyPoint _pos;
	
	public StaticComponent(MyPoint pos)
	{
		_pos = pos;
	}
	
	public MyPoint getPosition()
	{
		return _pos;
	}
}
