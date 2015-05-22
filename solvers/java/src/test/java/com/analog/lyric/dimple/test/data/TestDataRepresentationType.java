/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.data;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataRepresentationType;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link DataRepresentationType}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDataRepresentationType extends DimpleTestBase
{
	@Test
	public void test()
	{
		assertArrayEquals(new Object[]
			{ DataRepresentationType.FUNCTION,DataRepresentationType.MESSAGE, DataRepresentationType.VALUE },
			DataRepresentationType.values());
		
		assertSame(DataRepresentationType.FUNCTION, new Normal().representationType());
		assertSame(DataRepresentationType.MESSAGE, new NormalParameters().representationType());
		assertSame(DataRepresentationType.VALUE, RealValue.create(4.2).representationType());
	}
}
