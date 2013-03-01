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

function testRealOperatorOverloading()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRealOperatorOverloading');

numSamples = 1000;
scansPerSample = 50;
burnInScans = 10;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);

a1 = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(3,12));
b1 = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(7,17));

z1 = a1 + b1;

if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end
graph1.Solver.saveAllSamples();
graph1.solve();


graph2 = FactorGraph();
graph2.Solver = 'Gibbs';
graph2.Solver.setNumSamples(numSamples);
graph2.Solver.setScansPerSample(scansPerSample);
graph2.Solver.setBurnInScans(burnInScans);

a2 = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(3,12));
b2 = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(7,17));
z2 = Real();
graph2.addFactor(com.analog.lyric.dimple.FactorFunctions.Sum,z2,a2,b2);

if (repeatable)
    graph2.Solver.setSeed(1);					% Make this repeatable
end
graph2.Solver.saveAllSamples();
graph2.solve();

z1Samples = z1.Solver.getAllSamples();
z2Samples = z2.Solver.getAllSamples();

assertElementsAlmostEqual(z1Samples, z2Samples);

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
assertElementsAlmostEqual(a1Samples + b1Samples, z1Samples);

dtrace(debugPrint, '--testRealOperatorOverloading');

end


