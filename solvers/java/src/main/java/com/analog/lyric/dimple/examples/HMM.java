/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.examples;

import cern.colt.Arrays;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.util.misc.NonNullByDefault;

public class HMM
{

	@NonNullByDefault
	public static class TransitionFactorFunction extends FactorFunction
	{
		public TransitionFactorFunction()
		{
			super("TransitionFactorFunction");
		}
		
		@Override
		public double eval(Object ... args)
		{
			String state1 = (String)args[0];
			String state2 = (String)args[1];
			
			if (state1.equals("sunny"))
			{
				if (state2.equals("sunny"))
				{
					return 0.8;
				}
				else
				{
					return 0.2;
				}
				
			}
			else
			{
				return 0.5;
			}
			
		}
	}
	
	@NonNullByDefault
	public static class ObservationFactorFunction extends FactorFunction
	{
		public ObservationFactorFunction()
		{
			super("ObservationFactorFunction");
		}
		
		@Override
		public double eval(Object ... args)
		{
			String state = (String)args[0];
			String observation = (String)args[1];
			
			if (state.equals("sunny"))
				if (observation.equals("walk"))
					return 0.7;
				else if (observation.equals("book"))
					return 0.1;
				else // cook
					return 0.2;
			else
				if (observation.equals("walk"))
					return 0.2;
				else if (observation.equals("book"))
					return 0.4;
				else // cook
					return 0.4;
		}
	}
	
	public static void main(String[] args)
	{
		FactorGraph HMM = new FactorGraph();
		
		DiscreteDomain domain = DiscreteDomain.create("sunny","rainy");
		Discrete MondayWeather = new Discrete(domain);
		Discrete TuesdayWeather = new Discrete(domain);
		Discrete WednesdayWeather = new Discrete(domain);
		Discrete ThursdayWeather = new Discrete(domain);
		Discrete FridayWeather = new Discrete(domain);
		Discrete SaturdayWeather = new Discrete(domain);
		Discrete SundayWeather = new Discrete(domain);

		TransitionFactorFunction trans = new TransitionFactorFunction();
		HMM.addFactor(trans, MondayWeather,TuesdayWeather);
		HMM.addFactor(trans, TuesdayWeather,WednesdayWeather);
		HMM.addFactor(trans, WednesdayWeather,ThursdayWeather);
		HMM.addFactor(trans, ThursdayWeather,FridayWeather);
		HMM.addFactor(trans, FridayWeather,SaturdayWeather);
		HMM.addFactor(trans, FridayWeather,SundayWeather);
		
		ObservationFactorFunction obs = new ObservationFactorFunction();
		HMM.addFactor(obs,MondayWeather,"walk");
		HMM.addFactor(obs,TuesdayWeather,"walk");
		HMM.addFactor(obs,WednesdayWeather,"cook");
		HMM.addFactor(obs,ThursdayWeather,"walk");
		HMM.addFactor(obs,FridayWeather,"cook");
		HMM.addFactor(obs,SaturdayWeather,"book");
		HMM.addFactor(obs,SundayWeather,"book");
		
		
		MondayWeather.setInput(0.7,0.3);
		
		HMM.getSolver().setNumIterations(20);
		HMM.solve();
		
		double [] belief = TuesdayWeather.getBelief();
		System.out.println(Arrays.toString(belief));
		HMM.getSolver().iterate();
		HMM.getSolver().iterate(5);
	}

}
