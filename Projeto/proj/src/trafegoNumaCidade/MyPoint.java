package trafegoNumaCidade;

import repast.simphony.space.continuous.NdPoint;

public class MyPoint extends NdPoint
{
	public MyPoint(double... point) 
	{
		super(point);
	}
	
	public double magnitude()
	{
		double magsq = 0;
		
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			magsq += Math.pow(this.getCoord(i), 2);
		}
		
		return Math.sqrt(magsq);
	}

	public double calculateDistanceSquared(NdPoint other)
	{
		double sum = 0;
		if(this.dimensionCount() != other.dimensionCount())
		{
			throw new IllegalArgumentException();
		}
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			double term = Math.pow(this.getCoord(i) - other.getCoord(i), 2);
			sum += term;
		}
			
		return sum;
	}
	
	public double calculateDistance(NdPoint other)
	{
		return Math.sqrt(calculateDistanceSquared(other));
	}
	
	public MyPoint subtract(NdPoint other)
	{
		MyPoint ret;
		
		double[] newValues = new double[this.dimensionCount()];
		
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			newValues[i] = this.getCoord(i) - other.getCoord(i);
		}
		
		ret = new MyPoint(newValues);
		return ret;
	}
	
	public MyPoint add(NdPoint other)
	{
		MyPoint ret;
		
		double[] newValues = new double[this.dimensionCount()];
		
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			newValues[i] = this.getCoord(i) + other.getCoord(i);
		}
		
		ret = new MyPoint(newValues);
		return ret;
	}
	
	public MyPoint multiply(double factor)
	{
		MyPoint ret;
		
		double[] newValues = new double[this.dimensionCount()];
		
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			newValues[i] = this.getCoord(i) * factor;
		}
		
		ret = new MyPoint(newValues);
		return ret;
	}
	
	public MyPoint rotate2D(double radians)
	{
		if(this.dimensionCount() != 2)
		{
			throw new IllegalArgumentException();
		}
		
		double[] newValues = new double[2];
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		
		newValues[0] = this.getCoord(0)*cos - this.getCoord(1)*sin;
		newValues[1] = this.getCoord(0)*sin + this.getCoord(1)*cos;
		
		return new MyPoint(newValues);
	}
	
	private double dotProduct(MyPoint other)
	{
		return this.getCoord(0)*other.getCoord(0) + this.getCoord(1)*other.getCoord(1);
	}
	
	public MyPoint unitVector()
	{
		MyPoint ret;
		
		double mag = this.magnitude();
		
		double[] newValues = new double[this.dimensionCount()];
		
		for(int i = 0; i < this.dimensionCount(); i++)
		{
			newValues[i] = this.getCoord(i)/mag;
		}
		
		ret = new MyPoint(newValues);
		return ret;
	}
	
	public MyPoint clockwisePerpendicularVector()
	{
		if(this.dimensionCount() != 2)
		{
			throw new IllegalArgumentException();
		}
		
		MyPoint ret;
		ret = new MyPoint(this.getCoord(1), -this.getCoord(0));
		return ret;
	}
	
	public boolean isClockwiseTo(MyPoint other)
	{
		MyPoint clockwiseNormal = this.clockwisePerpendicularVector();
		
		return clockwiseNormal.dotProduct(other) < 0;
	}
}
