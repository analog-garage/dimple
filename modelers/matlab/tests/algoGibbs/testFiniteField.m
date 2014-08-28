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

function testFiniteField()

% Skip this test if the Communications Toolbox is unavailable.
if ~hasCommunicationToolbox('testFiniteField')
    return;
end

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testFiniteField');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
dtrace(debugPrint, '--testFiniteField');

end



function test1(debugPrint, repeatable)

m = 7;
numElements = 2^m;
domain = 0:numElements-1;
gfx = gf(domain,m);
prim_poly = gfx.prim_poly;

constant = 3;
gfConst = gf(constant,m,prim_poly);



% Create model
fg = FactorGraph();
fg.Solver = 'Gibbs';

v = FiniteFieldVariable(prim_poly, 1, 11);
b = Bit(1,m);

fg.addFactor('FiniteFieldAdd', v(1:3));
fg.addFactor('FiniteFieldAdd', v(4), constant, v(5));
fg.addFactor('FiniteFieldMult', v(6:8));
fg.addFactor('FiniteFieldMult', v(9), constant, v(10));
fg.addFactor('FiniteFieldProjection', v(11), m-1:-1:0, b(1:m));

% Repeat a few times with different random data
for n=1:5
    
    % Create data
    inputs = randi(numElements,1,11)-1;
    gfIn = gf(inputs,m,prim_poly);
    gfOut = gf(zeros(1,11),m,prim_poly);
    gfOut(1) = gfIn(2) + gfIn(3);
    gfOut(4) = gfConst + gfIn(5);
    gfOut(6) = gfIn(7) * gfIn(8);
    gfOut(9) = gfConst * gfIn(10);
    outputs = double(gfOut.x);
    bitsOut = dec2bin(inputs(11),m)-'0'; % Convert to bit array
    
    
    v(2).Input = deltaInput(inputs(2), numElements);
    v(3).Input = deltaInput(inputs(3), numElements);
    v(5).Input = deltaInput(inputs(5), numElements);
    v(7).Input = deltaInput(inputs(7), numElements);
    v(8).Input = deltaInput(inputs(8), numElements);
    v(10).Input = deltaInput(inputs(10), numElements);
    v(11).Input = deltaInput(inputs(11), numElements);
    
    if (repeatable)
        fg.Solver.setSeed(1);					% Make this repeatable
    end
    
    fg.solve();
    
    assertElementsAlmostEqual(v(1).Solver.getBestSampleIndex, outputs(1));
    assertElementsAlmostEqual(v(4).Solver.getBestSampleIndex, outputs(4));
    assertElementsAlmostEqual(v(6).Solver.getBestSampleIndex, outputs(6));
    assertElementsAlmostEqual(v(9).Solver.getBestSampleIndex, outputs(9));
    assertElementsAlmostEqual(cell2mat(b.invokeSolverMethodWithReturnValue('getBestSampleIndex')), bitsOut);
    
end

end


function x = deltaInput(i, n)
x = zeros(1,n);
x(i+1) = 1;
end

