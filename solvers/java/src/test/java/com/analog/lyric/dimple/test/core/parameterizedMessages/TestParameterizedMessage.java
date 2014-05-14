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

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.util.test.SerializationTester;

/**
<<<<<<< HEAD
 * Base class for IParameterizedMessage tests
=======
>>>>>>> Tests and fixes for IParameterizedMessage implementations
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestParameterizedMessage
{
	private static class BogusParameters implements IParameterizedMessage
	{
<<<<<<< HEAD
		private static final long serialVersionUID = 1L;

=======
>>>>>>> Tests and fixes for IParameterizedMessage implementations
		@Override
		public BogusParameters clone()
		{
			return new BogusParameters();
		}
		
		@Override
		public double computeKLDivergence(IParameterizedMessage that)
		{
			return 0;
		}

		@Override
		public void setNull()
		{
		}
	}
	
	public void assertGenericInvariants(IParameterizedMessage message)
	{
		IParameterizedMessage clone1 = message.clone();
		assertNotSame(clone1, message);
		double divergence = message.computeKLDivergence(clone1);
		assertEquals(0.0, divergence, 1e-9);
		assertTrue(divergence >= 0.0);

		IParameterizedMessage clone2 = SerializationTester.clone(message);
		assertNotSame(clone2, message);
		divergence = message.computeKLDivergence(clone2);
		assertEquals(0.0, divergence, 1e-9);
		assertTrue(divergence >= 0.0);

		expectThrow(IllegalArgumentException.class, message, "computeKLDivergence", new BogusParameters());
	}
}
