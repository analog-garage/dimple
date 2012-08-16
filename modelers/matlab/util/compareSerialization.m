%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function compareSerialization( fg, iterations, ExpectedVars, filename, directory, dbg)
    dtrace(dbg, '++compareSerialization');
    SerializedName = serializeToXML(fg, filename, directory);
    [fg2, Variables, Functions, jVariables, jFunctions] = deserializeFromXML(SerializedName);
    fg2.Solver.setNumIterations(iterations);
    fg2.solve();
    ExpectedBeliefs = {};
    for idx = 1:length(ExpectedVars)
        ExpectedBeliefs{length(ExpectedBeliefs) + 1} = ExpectedVars(idx).Belief;
    end
    
    solver = getSolver();
    jfg = solver.getGraph(fg2.Id());
    jVariablesSolved = jfg.getVariableVector();
    jVariablesSolvedCells = cell(jVariablesSolved);

     array = jVariablesSolvedCells{1}.toArray();
     SolvedBeliefs = {};
     SolvedBeliefs1 = {};
     for idx = 1:length(ExpectedBeliefs)
         v = array(idx);
         SolvedBeliefs{idx} = v.getBelief();
          SolvedBeliefs1{idx} = SolvedBeliefs{idx}(1);
     end
%          length(ExpectedBeliefs)    
%          length(SolvedBeliefs)
%          length(SolvedBeliefs1)
    total_diff = 0 ; 
    for idx = 1:length(ExpectedBeliefs)
         diff = ExpectedBeliefs{idx} - SolvedBeliefs1{idx};
         total_diff = total_diff + diff;
         dtrace(dbg, 'exp:%d  solved:%d  diff:%d', ExpectedBeliefs{idx}, SolvedBeliefs1{idx}, diff);
    end
    assertEqual(ExpectedBeliefs, SolvedBeliefs1);
    dtrace(dbg, '--compareSerialization  total diff:%d over %d beliefs', total_diff, length(ExpectedBeliefs));
end

