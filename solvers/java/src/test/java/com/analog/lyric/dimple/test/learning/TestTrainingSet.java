package com.analog.lyric.dimple.test.learning;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.learning.ITrainingSet;
import com.analog.lyric.dimple.learning.TrainingAssignment;
import com.analog.lyric.dimple.learning.TrainingAssignmentType;
import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.VariableBase;
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
			assertTrue(var.getDomain().containsValueWithRepresentation(value));
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
