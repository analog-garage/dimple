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

aDomain = 0:5;
bDomain = 1:4;
cDomain = 0:10;

a1 = Discrete(aDomain);
b1 = Discrete(bDomain);
c1 = Discrete(cDomain);
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

a2 = Discrete(aDomain);
b2 = Discrete(bDomain);
c2 = Discrete(cDomain);
d2 = Bit();
w2 = b2^a2;
x2 = w2 > c2;
y2 = x2 | d2;
z2 = ~y2;
z2.Input = 0.7;

graph2.solve();

a1Samples = a1.Solver.getAllSampleIndices();
b1Samples = b1.Solver.getAllSampleIndices();
c1Samples = c1.Solver.getAllSampleIndices();
z1Samples = z1.Solver.getAllSampleIndices();

assert(abs(mean(aDomain(a1Samples+1)) - a2.Belief*(aDomain')) < 0.05);
assert(abs(mean(bDomain(b1Samples+1)) - b2.Belief*(bDomain')) < 0.05);
assert(abs(mean(cDomain(c1Samples+1)) - c2.Belief*(cDomain')) < 0.05);
assert(abs(mean(z1Samples) - z2.Belief) < 0.01);

dtrace(debugPrint, '--testDeterministicDirectedFactors');

end


