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

function testRealVariableGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRealVariableGibbs');

% Test 1 - only real variables

numSamples = 10000;
updatesPerSample = 10;
burnInUpdates = 1000;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setUpdatesPerSample(updatesPerSample);
graph1.Solver.setBurnInUpdates(burnInUpdates);

aPriorMean = 1;
aPriorSigma = 0.5;
aPriorR = 1/(aPriorSigma*aPriorSigma);
bPriorMean = -1;
bPriorSigma = 2.;
bPriorR = 1/(bPriorSigma*bPriorSigma);
a = Real();
b = Real();
a.Input = FactorFunction('Normal',aPriorMean,aPriorR);
b.Input = {'Normal',bPriorMean, bPriorR};	% Try setting the input differently, just to test a different path

abMean = 0;
abSigma = 1;
abR = 1/(abSigma*abSigma);
f = graph1.addFactor(FactorFunction('Normal',0,1), a, b);

sa = a.Solver;
sb = b.Solver;

if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end

graph1.Solver.saveAllSamples();
graph1.solve();


aSamples = sa.getAllSamples;
bSamples = sb.getAllSamples;
aSum = 0;
for i=1:length(aSamples); aSum = aSum + aSamples(i); end
aMean = aSum/length(aSamples);
dtrace(debugPrint, ['aSampleMean: ' num2str(aMean)]);
bSum = 0;
for i=1:length(bSamples); bSum = bSum + bSamples(i); end
bMean = bSum/length(bSamples);
dtrace(debugPrint, ['bSampleMean: ' num2str(bMean)]);


aExpectedMean = (aPriorMean*aPriorR + abMean*abR)/(aPriorR + abR);
bExpectedMean = (bPriorMean*bPriorR + abMean*abR)/(bPriorR + abR);
dtrace(debugPrint, ['aExpectedMean: ' num2str(aExpectedMean)]);
dtrace(debugPrint, ['bExpectedMean: ' num2str(bExpectedMean)]);

% Best should be the same as the mean in this case
dtrace(debugPrint, ['aBest: ' num2str(sa.getBestSample())]);
dtrace(debugPrint, ['bBest: ' num2str(sb.getBestSample())]);

assertElementsAlmostEqual(aMean, 0.805087522616858);
assertElementsAlmostEqual(bMean, -0.192131270223249);
assertElementsAlmostEqual(sa.getBestSample(), 0.804355066141338);
assertElementsAlmostEqual(sb.getBestSample(), -0.207004277346162);


% Test 2 - real and discrete variables

numSamples = 10000;
updatesPerSample = 10;
burnInUpdates = 1000;

graph2 = FactorGraph();
graph2.Solver = 'Gibbs';
graph2.Solver.setNumSamples(numSamples);
graph2.Solver.setUpdatesPerSample(updatesPerSample);
graph2.Solver.setBurnInUpdates(burnInUpdates);

aPriorMean = 0;
aPriorSigma = 5;
aPriorR = 1/(aPriorSigma*aPriorSigma);
bProb1 = 0.6;
bProb0 = 1 - bProb1;
a = Real();
b = Variable([0 1]);
a.Input = FactorFunction('Normal',aPriorMean,aPriorR);
b.Input = [bProb0 bProb1];

fMean0 = -1;
fSigma0 = 0.75;
fR0 = 1/fSigma0^2;
fMean1 = 1;
fSigma1 = 0.75;
fR1 = 1/fSigma1^2;
fR0 = 1/(fSigma0*fSigma0);
fR1 = 1/(fSigma1*fSigma1);
f = graph2.addFactor(FactorFunction('MixedNormal',fMean0, fR0, fMean1, fR1), a, b);

sa = a.Solver;
sb = b.Solver;

if (repeatable)
    graph2.Solver.setSeed(1);					% Make this repeatable
end
graph2.Solver.saveAllSamples();
graph2.solve();


aSamples = sa.getAllSamples;
bSamples = sb.getAllSamples;
aSum = 0;
for i=1:length(aSamples); aSum = aSum + aSamples(i); end
aMean = aSum/length(aSamples);
dtrace(debugPrint, ['aSampleMean: ' num2str(aMean)]);
bSum = 0;
for i=1:length(bSamples); bSum = bSum + bSamples(i); end
bMean = bSum/length(bSamples);
dtrace(debugPrint, ['bSampleMean: ' num2str(bMean)]);

if (debugPrint)
    hist(aSamples,256);
end


aExpectedMean = bProb0*(aPriorMean*aPriorR + fMean0*fR0)/(aPriorR + fR0) + bProb1*(aPriorMean*aPriorR + fMean1*fR1)/(aPriorR + fR1);
dtrace(debugPrint, ['aExpectedMean: ' num2str(aExpectedMean)]);
dtrace(debugPrint, ['bExpectedMean: ' num2str(bProb1)]);

dtrace(debugPrint, ['aBest: ' num2str(sa.getBestSample())]);
dtrace(debugPrint, ['bBest: ' num2str(sb.getBestSample())]);

assertElementsAlmostEqual(aMean, 0.208672165661859);
assertElementsAlmostEqual(bMean, 0.605500000000000);
assertElementsAlmostEqual(sa.getBestSample(), 0.977986266650138);
assert(sb.getBestSample() == 1);

dtrace(debugPrint, '--testRealVariableGibbs');

end


