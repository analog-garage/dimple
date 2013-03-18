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

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);

dtrace(debugPrint, '--testRealOperatorOverloading');

end


function test1(debugPrint, repeatable)

numSamples = 1000;
scansPerSample = 50;
burnInScans = 10;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);

a1 = Real(com.analog.lyric.dimple.FactorFunctions.Normal(3,12));
b1 = Real(com.analog.lyric.dimple.FactorFunctions.Normal(7,17));

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

a2 = Real(com.analog.lyric.dimple.FactorFunctions.Normal(3,12));
b2 = Real(com.analog.lyric.dimple.FactorFunctions.Normal(7,17));
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

end




function test2(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);

w1 = Real(com.analog.lyric.dimple.FactorFunctions.Gamma(1,1));
x1 = Real([-pi pi]);
y1 = Real(com.analog.lyric.dimple.FactorFunctions.Normal(0,10));
z1 = Real([-.99 .99]);

a1 = abs(z1);
b1 = exp(y1);
c1 = log(w1);
d1 = sqrt(w1);
e1 = sin(x1);
f1 = cos(x1);
g1 = tan(x1);
h1 = asin(z1);
i1 = acos(z1);
j1 = atan(z1);
k1 = sinh(y1);
l1 = cosh(y1);
m1 = tanh(y1);
n1 = a1 + b1 + c1 + d1 + e1 + f1 + g1 + h1 + i1 + j1 + k1 + l1 + m1;


if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end
graph1.Solver.saveAllSamples();
graph1.solve();

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
c1Samples = c1.Solver.getAllSamples();
d1Samples = d1.Solver.getAllSamples();
e1Samples = e1.Solver.getAllSamples();
f1Samples = f1.Solver.getAllSamples();
g1Samples = g1.Solver.getAllSamples();
h1Samples = h1.Solver.getAllSamples();
i1Samples = i1.Solver.getAllSamples();
j1Samples = j1.Solver.getAllSamples();
k1Samples = k1.Solver.getAllSamples();
l1Samples = l1.Solver.getAllSamples();
m1Samples = m1.Solver.getAllSamples();
n1Samples = n1.Solver.getAllSamples();

w1Samples = w1.Solver.getAllSamples();
x1Samples = x1.Solver.getAllSamples();
y1Samples = y1.Solver.getAllSamples();
z1Samples = z1.Solver.getAllSamples();


assertElementsAlmostEqual(a1Samples, abs(z1Samples), 'absolute');
assertElementsAlmostEqual(b1Samples, exp(y1Samples), 'absolute');
assertElementsAlmostEqual(c1Samples, log(w1Samples), 'absolute');
assertElementsAlmostEqual(d1Samples, sqrt(w1Samples), 'absolute');
assertElementsAlmostEqual(e1Samples, sin(x1Samples), 'absolute');
assertElementsAlmostEqual(f1Samples, cos(x1Samples), 'absolute');
assertElementsAlmostEqual(g1Samples, tan(x1Samples), 'absolute');
assertElementsAlmostEqual(h1Samples, asin(z1Samples), 'absolute');
assertElementsAlmostEqual(i1Samples, acos(z1Samples), 'absolute');
assertElementsAlmostEqual(j1Samples, atan(z1Samples), 'absolute');
assertElementsAlmostEqual(k1Samples, sinh(y1Samples), 'absolute');
assertElementsAlmostEqual(l1Samples, cosh(y1Samples), 'absolute');
assertElementsAlmostEqual(m1Samples, tanh(y1Samples), 'absolute');
assertElementsAlmostEqual(n1Samples, a1Samples + b1Samples + c1Samples + d1Samples + e1Samples + f1Samples + g1Samples + h1Samples + i1Samples + j1Samples + k1Samples + l1Samples + m1Samples, 'absolute');


end


