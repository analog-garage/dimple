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

package com.analog.lyric.dimple.test.solvers.lp;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.lp.IntegerEquation;
import com.analog.lyric.dimple.solvers.lp.LPFactorMarginalConstraint;
import com.analog.lyric.dimple.solvers.lp.LPVariableConstraint;
import com.analog.lyric.dimple.solvers.lp.MatlabConstraintTermIterator;
import com.analog.lyric.dimple.solvers.lp.LPSolverGraph;
import com.analog.lyric.dimple.solvers.lp.STableFactor;
import com.analog.lyric.dimple.solvers.lp.SVariable;
import com.analog.lyric.dimple.solvers.lp.Solver;
import com.analog.lyric.dimple.test.DimpleTestBase;
import org.eclipse.jdt.annotation.Nullable;
import com.analog.lyric.util.test.Unchecked;

public class LPSolverTestCase extends DimpleTestBase
{
	public final FactorGraph model;
	public @Nullable String[] expectedConstraints = null;

	public final LPSolverGraph solver;
	
	public LPSolverTestCase(FactorGraph model)
	{
		this.model = model;
		solver = new Solver().createFactorGraph(model);
		assertInitialState();
		
		assertNull(solver.getParentGraph());
		assertSame(solver, solver.getRootGraph());
		
		assertEquals("dimpleLPSolve", solver.getMatlabSolveWrapper());
		solver.setLPSolverName("Matlab");
		assertEquals("Matlab", solver.getLPSolverName());
		assertEquals("dimpleLPSolve", solver.getMatlabSolveWrapper());
		solver.setLPSolverName("foo");
		assertEquals("foo", solver.getLPSolverName());
		assertEquals(null, solver.getMatlabSolveWrapper());
		solver.setLPSolverName(null);
		
		// Test do nothing methods
		solver.setNumIterations(42);
		assertEquals(1, solver.getNumIterations());
		solver.update();
		solver.updateEdge(0);
		solver.initialize();
		solver.resetEdgeMessages(0);
		
		// Test unsupported methods
		try { solver.setInputMsg(0, "foo"); fail("exception expected"); } catch (DimpleException ex) {}
		try { solver.setInputMsgValues(0, "foo"); fail("exception expected"); } catch (DimpleException ex) {}
		try { solver.getInputMsg(0); fail("exception expected"); } catch (DimpleException ex) {}
		try { solver.setOutputMsg(0, "foo"); fail("exception expected"); } catch (DimpleException ex) {}
		try { solver.setOutputMsgValues(0, "foo"); fail("exception expected"); } catch (DimpleException ex) {}
		try { solver.getOutputMsg(0); fail("exception expected"); } catch (DimpleException ex) {}
		expectThrow(DimpleException.class, solver, "moveMessages", null, 0, 1);
		expectThrow(DimpleException.class, solver, "estimateParameters", null, 0, 0, 0.0);
		expectThrow(DimpleException.class, solver, "baumWelch", null, 0, 0);
	}
	
	/**
	 * Tests construction of linear programming version of factor
	 * graph.
	 */
	public void testLPState()
	{
		solver.buildLPState();
		assertTrue(solver.hasLPState());
		
		final int nLPVars = solver.getNumberOfLPVariables();
		assertTrue(nLPVars >= 0);
		
		final double[] objective = requireNonNull(solver.getObjectiveFunction());
		assertEquals(nLPVars, objective.length);
		
		int nVarsUsed = 0;
		
		for (Variable var : model.getVariables())
		{
			SVariable svar = requireNonNull(solver.getSolverVariable(var));
			assertSame(svar, solver.createVariable(var));
			
			Discrete mvar = svar.getModelObject();
			assertSame(var, mvar);
			assertSame(solver, svar.getParentGraph());
			
			// Test do-nothing methods
			svar.resetEdgeMessages(0);
			svar.updateEdge(0);
			svar.moveMessages(svar, 0, 1);
			assertNull(svar.getInputMsg(0));
			assertNull(svar.getOutputMsg(0));
			assertNull(svar.createMessages(Unchecked.nullValue(ISolverFactor.class)));
			assertNull(svar.resetInputMessage(""));
			
			int lpVar = svar.getLPVarIndex();
			int nValidAssignments = svar.getNumberOfValidAssignments();
			
			if (var.hasFixedValue())
			{
				// Currently the converse is not true because model variables
				// do not currently check to see if there is only one non-zero input weight.
				assertTrue(svar.hasFixedValue());
			}
			if (svar.hasFixedValue())
			{
				assertFalse(svar.hasLPVariable());
			}
			if (svar.hasLPVariable())
			{
				assertTrue(lpVar >= 0);
				assertTrue(nValidAssignments > 1);
				++nVarsUsed;
			}
			else
			{
				assertEquals(-1, lpVar);
				assertTrue(nValidAssignments <= 1);
			}
			
		
			
			for (int i = 0, end = svar.getModelObject().getDomain().size(); i < end; ++i)
			{
				double w = mvar.getInput()[i];
				int lpVarForValue = svar.domainIndexToLPVar(i);
				int i2 = svar.lpVarToDomainIndex(lpVarForValue);
				if (lpVarForValue >= 0)
				{
					assertEquals(i, i2);
					assertEquals(Math.log(w), objective[lpVarForValue], 1e-6);
				}
				if (!svar.hasLPVariable() || mvar.getInput()[i] == 0.0)
				{
					assertEquals(-1, lpVarForValue);
				}
			}
		}
		
		for (Factor factor : model.getFactors())
		{
			STableFactor sfactor = requireNonNull(solver.getSolverFactor(factor));
			assertSame(sfactor, solver.createFactor(factor));
			assertSame(factor, sfactor.getModelObject());
			assertSame(solver, sfactor.getParentGraph());
			
			// Test do nothing methods
			sfactor.createMessages();
			sfactor.updateEdge(0);
			sfactor.resetEdgeMessages(0);
			sfactor.moveMessages(sfactor, 0 , 1);
			assertNull(sfactor.getInputMsg(0));
			assertNull(sfactor.getOutputMsg(0));
		}
		
		final List<IntegerEquation> constraints = solver.getConstraints();
		assertNotNull(constraints);

		int nConstraints = solver.getNumberOfConstraints();
		int nVarConstraints = solver.getNumberOfVariableConstraints();
		int nMarginalConstraints = solver.getNumberOfMarginalConstraints();
		assertEquals(nConstraints, constraints.size());
		assertEquals(nConstraints, nVarConstraints + nMarginalConstraints);
		assertEquals(nVarsUsed, nVarConstraints);
		
		{
			MatlabConstraintTermIterator termIter = solver.getMatlabSparseConstraints();
			List<Integer> constraintTerms = new ArrayList<Integer>(termIter.size() * 3);
			
			Iterator<IntegerEquation> constraintIter = constraints.iterator();
			for (int row = 1; constraintIter.hasNext(); ++ row)
			{
				IntegerEquation constraint = constraintIter.next();
				
				int nExpectedTerms = -1;
				int lpVar = -1;
				
				if (row <= nVarConstraints)
				{
					LPVariableConstraint varConstraint = constraint.asVariableConstraint();
					assertNotNull(varConstraint);
					assertNull(constraint.asFactorConstraint());
					
					SVariable svar = varConstraint.getSolverVariable();
					assertTrue(svar.hasLPVariable());
					
					assertEquals(1, varConstraint.getRHS());
					nExpectedTerms = svar.getNumberOfValidAssignments();
					
					lpVar = svar.getLPVarIndex();
				}
				else
				{
					LPFactorMarginalConstraint factorConstraint = constraint.asFactorConstraint();
					assertNotNull(factorConstraint);
					assertNull(constraint.asVariableConstraint());
					
					STableFactor sfactor = factorConstraint.getSolverFactor();
					lpVar = sfactor.getLPVarIndex();
					
					assertEquals(0, factorConstraint.getRHS());
					nExpectedTerms = sfactor.getNumberOfValidAssignments();
				}
				
				int[] lpvars = constraint.getVariables();
				assertEquals(constraint.size(), lpvars.length);
				assertEquals(0, constraint.getCoefficient(-1));
				assertEquals(0, constraint.getCoefficient(lpVar + nExpectedTerms));
				assertFalse(constraint.hasCoefficient(-1));
				
				assertTrue(lpVar >= 0);

				IntegerEquation.TermIterator constraintTermIter = constraint.getTerms();
				for (int i = 0; constraintTermIter.advance(); ++i)
				{
					assertEquals(lpvars[i], constraintTermIter.getVariable());
					assertEquals(constraintTermIter.getCoefficient(), constraint.getCoefficient(lpvars[i]));
					assertTrue(constraint.hasCoefficient(lpvars[i]));
					constraintTerms.add(row);
					constraintTerms.add(constraintTermIter.getVariable());
					constraintTerms.add(constraintTermIter.getCoefficient());
				}
				assertFalse(constraintTermIter.advance());
			}
			
			for (int i = 0; termIter.advance(); i += 3)
			{
				assertEquals((int)constraintTerms.get(i), termIter.getRow());
				assertEquals(constraintTerms.get(i+1) + 1, termIter.getVariable());
				assertEquals((int)constraintTerms.get(i+2), termIter.getCoefficient());
			}
			assertFalse(termIter.advance());
		}
		
		final String[] expectedConstraints2 = expectedConstraints;
		if (expectedConstraints2 != null)
		{
			Iterator<IntegerEquation> constraintIter = constraints.iterator();
			assertEquals(expectedConstraints2.length, solver.getNumberOfConstraints());
			for (int i = 0, end = expectedConstraints2.length; i < end; ++i)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IntegerEquation constraint = constraintIter.next();
				constraint.print(new PrintStream(out));
				String actual = out.toString().trim();

				String expected = expectedConstraints2[i].trim();

				if (!expected.equals(actual))
				{
					System.out.format("Constraint %d mismatch:\n", i);
					System.out.format("Expected: %s\n", expected);
					System.out.format("  Actual: %s\n", actual);
				}
				assertEquals(expected, actual);
			}
		}
		
		// Test setting solution. A real solution will only use ones and zeros,
		// but we will use random values to make sure they are assigned correctly.
		double[] solution = new double[nLPVars];
		Random rand = new Random();
		for (int i = solution.length; --i>=0;)
		{
			solution[i] = rand.nextDouble();
		}
		solver.setSolution(solution);
		for (Variable var : model.getVariables())
		{
			SVariable svar = requireNonNull(solver.getSolverVariable(var));
			double[] belief = svar.getBelief();
			if (svar.hasFixedValue())
			{
				int vali = svar.getValueIndex();
				for (int i = belief.length; --i>=0;)
				{
					if (i == vali)
					{
						assertEquals(1.0, belief[i], 1e-6);
					}
					else
					{
						assertEquals(0.0, belief[i], 1e-6);
					}
				}
			}
			else
			{
				for (int i = svar.getModelObject().getDomain().size(); --i>=0;)
				{
					int lpVar = svar.domainIndexToLPVar(i);
					if (lpVar < 0)
					{
						assertEquals(0, belief[i], 1e-6);
					}
					else
					{
						assertEquals(solution[lpVar], belief[i], 1e-6);
					}
				}
			}
		}
		
		solver.clearLPState();
		assertInitialState();
	}
	
	private void assertInitialState()
	{
		assertFalse(solver.hasLPState());
		assertNull(solver.getObjectiveFunction());
		assertNull(solver.getConstraints());
		assertEquals(-1, solver.getNumberOfConstraints());
		assertEquals(-1, solver.getNumberOfLPVariables());
		assertEquals(-1, solver.getNumberOfVariableConstraints());
		assertEquals(-1, solver.getNumberOfMarginalConstraints());
		
		for (Variable var : model.getVariables())
		{
			assertNull(solver.getSolverVariable(var));
		}
		
		MatlabConstraintTermIterator terms = solver.getMatlabSparseConstraints();
		assertEquals(0, terms.size());
		assertFalse(terms.advance());
		assertEquals(-1, terms.getVariable());
		assertEquals(0, terms.getCoefficient());
		assertEquals(-1, terms.getRow());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		solver.printConstraints(new PrintStream(out));
		assertEquals("Constraints not yet computed.", out.toString().trim());

		assertEquals("", solver.getLPSolverName());
	}
}
