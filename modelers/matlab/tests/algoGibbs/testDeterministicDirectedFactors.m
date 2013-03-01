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

function testDeterministicDirectedFactors()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testDeterministicDirectedFactors');

numSamples = 10000;
scansPerSample = 1;
burnInScans = 10;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);

a1 = Discrete(0:5);
b1 = Discrete(1:4);
c1 = Discrete(0:10);
d1 = Bit();
w1 = b1^a1;
x1 = w1 > c1;
y1 = x1 | d1;
z1 = ~y1;
z1.Input = 0.7;

if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end
graph1.Solver.saveAllSamples();
graph1.solve();


graph2 = FactorGraph();
graph2.Solver ='SumProduct';

a2 = Discrete(0:5);
b2 = Discrete(1:4);
c2 = Discrete(0:10);
d2 = Bit();
w2 = b2^a2;
x2 = w2 > c2;
y2 = x2 | d2;
z2 = ~y2;
z2.Input = 0.7;

graph2.solve();

z1Samples = z1.Solver.getAllSampleIndices();

assert(abs(mean(z1Samples) - z2.Belief) < 0.01);

dtrace(debugPrint, '--testDeterministicDirectedFactors');

end


