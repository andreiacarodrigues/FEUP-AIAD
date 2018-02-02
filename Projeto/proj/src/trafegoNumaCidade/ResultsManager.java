package trafegoNumaCidade;

import java.util.ArrayList;

public class ResultsManager 
{
	static private ArrayList<Integer> values = new ArrayList<>();
	
	static public void addResult(int newCount)
	{
		values.add(newCount);
		values.sort(null);
	}
	
	static public int getCurrentValue()
	{
		return values.get(values.size()/2);
	}
	
	static public int getCurrentCount()
	{
		return values.size();
	}
}
