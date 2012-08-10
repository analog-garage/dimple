package com.analog.lyric.dimple.model;



public class FiniteFieldVariable extends Discrete 
{
	private static Double [] _getDomainFromPoly(int poly)
	{
		double [] dpoly = _convert2poly(poly);
				
        //domain = 0:2^(length(poly)-1)-1;
		int max = (int)Math.pow(2,dpoly.length-1)-1;
		Double [] domain = new Double[max+1];
		for (int i = 0; i < domain.length; i++)
			domain[i] = new Double(i);
		
		return domain;
	}
	
	public FiniteFieldVariable(int poly)  
	{
//		 * TODO /
			super((Object[])_getDomainFromPoly(poly));
			/*
			com.lyricsemi.dimple.model.Discrete impl 
			= (com.lyricsemi.dimple.model.Discrete) getModelerObject();
			double [] dpoly = _convert2poly(poly);
  		setProperty("primitivePolynomial", dpoly);
  		*/
	}
	
	private static double [] _convert2poly(int poly)
	{
		
		double log2 = Math.log(poly)/Math.log(2);
		int polySize = (int)Math.floor(log2) + 1;
		
		double [] retval = new double[polySize];
		
		for (int i = 0; i < retval.length; i++)
		{
			if (((1 << i) & poly) != 0)
				retval[retval.length-i-1] = 1;
			
		}
		
		return retval;
	}
}
