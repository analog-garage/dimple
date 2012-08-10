function testRealVariableGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRealVariableGibbs');

% Test 1 - only real variables

numSamples = 100000;
updatesPerSample = 10;
burnInUpdates = 1000;

setSolver(com.analog.lyric.dimple.solvers.gibbs.Solver());
graph1 = FactorGraph();
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setUpdatesPerSample(updatesPerSample);
graph1.Solver.setBurnInUpdates(burnInUpdates);

aPriorMean = 1;
aPriorSigma = 0.5;
aPriorR = 1/(aPriorSigma*aPriorSigma);
bPriorMean = -1;
bPriorSigma = 2.;
bPriorR = 1/(bPriorSigma*bPriorSigma);
a = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(aPriorMean,aPriorSigma));
b = Real();
b.Input = com.analog.lyric.dimple.FactorFunctions.SimpleNormal(bPriorMean, bPriorSigma);	% Try setting the input differently, just to test a different path

abMean = 0;
abSigma = 1;
abR = 1/(abSigma*abSigma);
f = graph1.addFactor(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1), a, b);

sa = a.Solver;
sb = b.Solver;

sa.setProposalStandardDeviation(0.1);
sb.setProposalStandardDeviation(0.1);


if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end

graph1.Solver.saveAllSamples();
graph1.solve();


aSamples = sa.AllSamples;
bSamples = sb.AllSamples;
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

assertElementsAlmostEqual(aMean, 0.7964189428291455);
assertElementsAlmostEqual(bMean, -0.2035763225037135);
assertElementsAlmostEqual(sa.getBestSample(), 0.800418664253919);
assertElementsAlmostEqual(sb.getBestSample(), -0.19842890611838168);


% Test 2 - real and discrete variables

numSamples = 10000;
updatesPerSample = 10;
burnInUpdates = 1000;

setSolver(com.analog.lyric.dimple.solvers.gibbs.Solver());
graph2 = FactorGraph();
graph2.Solver.setNumSamples(numSamples);
graph2.Solver.setUpdatesPerSample(updatesPerSample);
graph2.Solver.setBurnInUpdates(burnInUpdates);

aPriorMean = 0;
aPriorSigma = 5;
aPriorR = 1/(aPriorSigma*aPriorSigma);
bProb1 = 0.6;
bProb0 = 1 - bProb1;
a = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(aPriorMean,aPriorSigma));
b = Variable([0 1]);
b.Input = [bProb0 bProb1];

fMean0 = -1;
fSigma0 = 0.75;
fMean1 = 1;
fSigma1 = 0.75;
fR0 = 1/(fSigma0*fSigma0);
fR1 = 1/(fSigma1*fSigma1);
f = graph2.addFactor(com.analog.lyric.dimple.FactorFunctions.MixedNormal(fMean0, fSigma0, fMean1, fSigma1), a, b);

sa = a.Solver;
sb = b.Solver;

sa.setProposalStandardDeviation(1.0);


if (repeatable)
    graph2.Solver.setSeed(1);					% Make this repeatable
end
graph2.Solver.saveAllSamples();
graph2.solve();


aSamples = sa.AllSamples;
bSamples = sb.AllSamples;
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

assertElementsAlmostEqual(aMean, 0.21862681232341438);
assertElementsAlmostEqual(bMean, 0.6079);
assertElementsAlmostEqual(sa.getBestSample(), 0.977907866169981);
assert(sb.getBestSample() == 1);

dtrace(debugPrint, '--testRealVariableGibbs');

end


