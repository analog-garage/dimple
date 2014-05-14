/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.core.parameterizedMessages;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestMultivariateNormalParameters extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		MultivariateNormalParameters msg = new MultivariateNormalParameters();
		assertTrue(msg.isNull());
		assertFalse(msg.isInInformationForm());
		assertInvariants(msg);
		
		assertEquals(0, msg.getInformationVector().length);
		assertTrue(msg.isInInformationForm());
		assertInvariants(msg);
		
		double[] means = new double[2];
		double[][] covariance = new double[2][2];

		//   mean     covariance                  inverse
		// | 5.0 |   | 1.0  2.0 |   det == -3  | -1/3  2/3 |
		// | 6.0 |   | 2.0  1.0 |              |  2/3 -1/3 |
		//
		means[0] = 5.0;
		means[1] = 6.0;
		covariance[0][0] = 1.0;
		covariance[1][1] = 1.0;
		covariance[0][1] = 2.0;
		covariance[1][0] = 2.0;
		msg.setMeanAndCovariance(means, covariance);
		assertFalse(msg.isInInformationForm());
		assertInvariants(msg);
		assertNotSame(means, msg.getMean());
		assertArrayEquals(means, msg.getMean(), 0.0);
		
		//   mean     covariance                  inverse
		// | 7.0 |   | 2.0  4.0 |   det == -10  | -3/10  2/5 |
		// | 8.0 |   | 4.0  3.0 |               |  2/5  -1/5 |
		//
		means[0] = 7.0;
		means[1] = 8.0;
		covariance[0][0] = 2.0;
		covariance[1][1] = 3.0;
		covariance[0][1] = 4.0;
		covariance[1][0] = 4.0;
		MultivariateNormalParameters msg2 = new MultivariateNormalParameters(means, covariance);
		assertInvariants(msg2);
		
		// Computed these by hand in MATLAB
		assertEquals(.75198640216297, msg.computeKLDivergence(msg2), 1e-10);
		assertEquals(1.5646802645036, msg2.computeKLDivergence(msg), 1e-10);
		
		msg2.setNull();
		assertTrue(msg2.isNull());
		assertTrue(msg2.isInInformationForm());
		expectThrow(IllegalArgumentException.class, "Incompatible vector sizes.*", msg, "computeKLDivergence", msg2);
		
		msg2.setInformation(means, covariance);
		assertArrayEquals(means, msg2.getInformationVector(), 1e-14);
		assertTrue(msg2.isInInformationForm());
		assertInvariants(msg2);
		
		// TODO: test correctness of covariance/information transformation
	}
	
	private void assertInvariants(MultivariateNormalParameters msg)
	{
		assertGenericInvariants(msg);
		
		final int n = msg.getVectorLength();
		assertEquals(n == 0, msg.isNull());
		
		MultivariateNormalParameters msg2 = msg.clone();
		assertEquals(msg.isInInformationForm(), msg2.isInInformationForm());
		assertEquals(n, msg2.getVectorLength());
		
		if (msg.isInInformationForm())
		{
			assertArrayEquals(msg2.getInformationVector(), msg.getInformationVector(), 0.0);
		}
		else
		{
			assertArrayEquals(msg2.getMean(), msg.getMean(), 0.0);
		}

		MultivariateNormalParameters msg3 = SerializationTester.clone(msg);
		assertEquals(msg.isInInformationForm(), msg3.isInInformationForm());
		assertEquals(n, msg3.getVectorLength());
		
		if (msg.isInInformationForm())
		{
			assertArrayEquals(msg.getInformationVector(), msg3.getInformationVector(), 0.0);
		}
		else
		{
			assertArrayEquals(msg.getMean(), msg3.getMean(), 0.0);
		}

		
		double[] means = msg2.getMean();
		assertFalse(msg2.isInInformationForm());
		
		assertArrayEquals(msg2.getMeans(), means, 0.0);
		assertNotSame(means, msg2.getMean());
		assertEquals(n, means.length);
		
		double[] infoVector = msg2.getInformationVector();
		assertEquals(n, infoVector.length);
		
		double[][] covariance = msg2.getCovariance();
		assertFalse(msg2.isInInformationForm());

		assertEquals(n, covariance.length);
		for (int row = 0; row < n; ++row)
		{
			assertEquals(n, covariance[row].length);
			for (int col = 0; col < n; ++col)
			{
				assertEquals(covariance[row][col], covariance[col][row], 1e-14);
			}
		}
		
		double[][] infoMatrix = msg2.getInformationMatrix();
		assertTrue(msg2.isInInformationForm());

		assertEquals(n, infoMatrix.length);
		for (int row = 0; row < n; ++row)
		{
			assertEquals(n, infoMatrix[row].length);
			for (int col = 0; col < n; ++col)
			{
				assertEquals(infoMatrix[row][col], infoMatrix[col][row], 1e-14);
			}
		}
	}
}
