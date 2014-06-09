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

package com.analog.lyric.dimple.test.learning;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.learning.ITrainingSet;
import com.analog.lyric.dimple.learning.TrainingAssignment;
import com.analog.lyric.dimple.learning.TrainingAssignmentType;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.test.dummySolver.DummySolver;
import com.analog.lyric.util.test.SerializationTester;

/**
 * Tests for {@link ITrainingSet} and related classes.
 */
public class TestTrainingSet
{
	@Test
	public void testTrainingAssignment()
	{
		
		FactorGraph model = makeModel();
		
		for (VariableBase var : model.getVariables())
		{
			TrainingAssignment missing = TrainingAssignment.create(var, TrainingAssignmentType.MISSING, null);
			assertEquals(TrainingAssignmentType.MISSING, missing.getAssignmentType());
			assertNull(missing.getValue());
			assertSame(var, missing.getVariable(model));
			testTrainingAssignment(model, missing);
			
			TrainingAssignment fixed = TrainingAssignment.create(var.getUUID(), TrainingAssignmentType.FIXED, 0);
			assertEquals(TrainingAssignmentType.FIXED, fixed.getAssignmentType());
			assertEquals(0, fixed.getValue());
			assertSame(var, fixed.getVariable(model));
			testTrainingAssignment(model, fixed);
		}
	}

	private FactorGraph makeModel()
	{
		FactorGraph model = new FactorGraph();
		model.setSolverFactory(new DummySolver());
		model.addVariables(new Bit(), new Real());
		return model;
	}
	
	private void testTrainingAssignment(FactorGraph model, TrainingAssignment assignment)
	{
		assertTrainingAssignmentInvariants(model, assignment);
	}
	
	private void assertTrainingAssignmentInvariants(FactorGraph model, TrainingAssignment assignment)
	{
		VariableBase var = assignment.getVariable(model);
		TrainingAssignmentType type = assignment.getAssignmentType();
		Object value = assignment.getValue();
		
		assertSame(var.getSolver(), assignment.getSolverVariable(model.getSolver()));
		assertNull(assignment.getSolverVariable(null));
		
		switch (type)
		{
		case MISSING:
			assertNull(value);
			break;
		case FIXED:
		case VALUE:
			assertTrue(value != null && var.getDomain().containsValueWithRepresentation(value));
			break;
		case INPUTS:
			break;
		}
		
		// Test serialization
		// FIXME: handle case where underlying value is not serializable.
		TrainingAssignment assignment2 = SerializationTester.clone(assignment);
		assertNotSame(assignment, assignment2);
		assertEquals(type, assignment2.getAssignmentType());
		assertEquals(assignment.getValue(), assignment2.getValue());
		assertEquals(var, assignment2.getVariable(model));
		
		
	}
}
