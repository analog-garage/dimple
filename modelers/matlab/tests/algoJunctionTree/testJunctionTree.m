%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function testJunctionTree()
    bLog = false;
    dtrace(bLog, '++testJunctionTree');

    setSolver('JunctionTree');
    
    % Build a random 2x2 factor table
    table = FactorTable(rand(2,2), DiscreteDomain(0:1), DiscreteDomain(0:1));

    % A 3x3 grid of bits
    fg = FactorGraph();
    
    vars = Discrete(0:1,3,3);
    fg.addFactorVectorized(table, vars(1:(end-1),:), vars(2:end,:));
    fg.addFactorVectorized(table, vars(:,1:end-1), vars(:,2:end));
    
    fg.solve();
    
    jtBeliefs = vars.Belief;
    
    % Try again using a single factor
    fg.setSolver('SumProduct');
    allFactors = fg.Factors;
    fg.join(allFactors{:});
    fg.solve();
    
    expectedBeliefs = vars.Belief;
    
    assertElementsAlmostEqual(expectedBeliefs, jtBeliefs);
    
    dtrace(bLog, '--testJunctionTree');
end