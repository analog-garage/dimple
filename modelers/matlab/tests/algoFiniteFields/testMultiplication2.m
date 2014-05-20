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

function testMultiplication2()

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf'))
        dtrace(true, 'WARNING: testMultiplication2 was skipped because Communications Toolbox not installed');
        return;
    end
    
    rs=resetRandStream('mt19937ar');
    m = 2;
    
    numElements = 2^m;
    domain = 0:numElements-1;
    
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;
    real_domain = cell(length(tmp),1);
    for i = 1:length(tmp)
       real_domain{i} = tmp(i); 
    end
    
    
    
    x_slow = Variable(real_domain);
    y_slow = Variable(real_domain);
    z_slow = Variable(real_domain);
    
    fg_slow = FactorGraph();
    
    fg_slow.addFactor(@multiplyDelta,x_slow,y_slow,z_slow);
    
    x_input = rand(size(x_slow.Domain.Elements));
    y_input = rand(size(y_slow.Domain.Elements));
    z_input = rand(size(y_slow.Domain.Elements));
        
    x_slow.Input = x_input;
    y_slow.Input = y_input;
    z_slow.Input = z_input;
    
    fg_slow.Solver.setNumIterations(1);
    
    fg_slow.solve();
    
    
    fg_fast = FactorGraph();
    
    x_fast = FiniteFieldVariable(prim_poly);
    y_fast = FiniteFieldVariable(prim_poly);
    z_fast = FiniteFieldVariable(prim_poly);
    
    x_fast.Input = x_input;
    y_fast.Input = y_input;
    z_fast.Input = z_input;
    
    fg_fast.addFactor(@finiteFieldMult,x_fast,y_fast,z_fast);
    
    fg_fast.Solver.setNumIterations(1);
    
    fg_fast.solve();

    assertElementsAlmostEqual(x_slow.Belief,x_fast.Belief);
    assertElementsAlmostEqual(y_slow.Belief,y_fast.Belief);
    assertElementsAlmostEqual(z_slow.Belief,z_fast.Belief);
     
end
    
