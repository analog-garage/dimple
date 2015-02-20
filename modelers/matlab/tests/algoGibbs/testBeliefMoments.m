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

function testBeliefMoments()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testBeliefMoments');

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);

dtrace(debugPrint, '--testBeliefMoments');

end

% Basic test, Real variables
function test1(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

a = Normal(0,1);
b = Gamma(1,1);
x = (a + b) ^ 2;
y = x + log(LogNormal(2,7)) ^ 2;
y.FixedValue = 5;

% Run the solver without saving all samples
fg.Solver.setSeed(1);
fg.Solver.setNumSamples(100);
fg.Solver.setBurnInScans(10);
fg.solve();

aMean = a.Solver.getSampleMean();
aVar = a.Solver.getSampleVariance();
bMean = b.Solver.getSampleMean();
bVar = b.Solver.getSampleVariance();
xMean = x.Solver.getSampleMean();
xVar = x.Solver.getSampleVariance();
yMean = y.Solver.getSampleMean();
yVar = y.Solver.getSampleVariance();

% Run the solver again, this time saving all samples
fg.Solver.setSeed(1);
fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples();
bs = b.Solver.getAllSamples();
xs = x.Solver.getAllSamples();
ys = y.Solver.getAllSamples();

assertElementsAlmostEqual(aMean, mean(as));
assertElementsAlmostEqual(aVar, var(as));
assertElementsAlmostEqual(bMean, mean(bs));
assertElementsAlmostEqual(bVar, var(bs));
assertElementsAlmostEqual(xMean, mean(xs));
assertElementsAlmostEqual(xVar, var(xs));
assertElementsAlmostEqual(yMean, mean(ys));
assertElementsAlmostEqual(yVar, var(ys));
assert(yMean == y.FixedValue);
assert(yVar == 0);

% Make sure moments are the same the next time
aMean2 = a.Solver.getSampleMean();
aVar2 = a.Solver.getSampleVariance();
assertElementsAlmostEqual(aMean2, mean(as));
assertElementsAlmostEqual(aVar2, var(as));

end


% Basic test, RealJoint variables
function test2(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

dim = 10;
a = ExchangeableDirichlet(dim, 2);
b = ExchangeableDirichlet(dim, 2);
x = (a + b);
y = x - 4;

% Run the solver without saving all samples
fg.Solver.setSeed(1);
fg.Solver.setNumSamples(100);
fg.Solver.setBurnInScans(10);
fg.setOption('GibbsOptions.computeRealJointBeliefMoments', true);
fg.solve();

aMean = a.Solver.getSampleMean();
aVar = a.Solver.getSampleCovariance();
bMean = b.Solver.getSampleMean();
bVar = b.Solver.getSampleCovariance();
xMean = x.Solver.getSampleMean();
xVar = x.Solver.getSampleCovariance();
yMean = y.Solver.getSampleMean();
yVar = y.Solver.getSampleCovariance();

% Run the solver again, this time saving all samples
fg.Solver.setSeed(1);
fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples();
bs = b.Solver.getAllSamples();
xs = x.Solver.getAllSamples();
ys = y.Solver.getAllSamples();

assertElementsAlmostEqual(aMean, mean(as,1)');
assertElementsAlmostEqual(aVar, cov(as));
assertElementsAlmostEqual(bMean, mean(bs,1)');
assertElementsAlmostEqual(bVar, cov(bs));
assertElementsAlmostEqual(xMean, mean(xs,1)');
assertElementsAlmostEqual(xVar, cov(xs));
assertElementsAlmostEqual(yMean, mean(ys,1)');
assertElementsAlmostEqual(yVar, cov(ys));

% Make sure moments are the same the next time
aMean2 = a.Solver.getSampleMean();
aVar2 = a.Solver.getSampleCovariance();
assertElementsAlmostEqual(aMean2, mean(as,1)');
assertElementsAlmostEqual(aVar2, cov(as));

end



% Basic test, Rolled-up graph
function test3(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

numDataPoints = 10;
dataPrecision = 1e4;
transitionPrecision = 10;

y = Real();
x = Real();
ng = FactorGraph(x,y);
ng.addFactor('Normal',x * 1.1, transitionPrecision, y);

vars = RealStream();
fg.addFactor(ng,vars,vars.getSlice(2));

data = ones(1,numDataPoints);
dataSource = FactorFunctionDataSource();
for i=1:numDataPoints
    dataSource.add(FactorFunction('Normal', data(i), dataPrecision));
end
vars.DataSource = dataSource;

if (repeatable)
    fg.Solver.setSeed(1);
end
fg.Solver.setNumSamples(4000);
fg.Solver.setBurnInScans(10);
fg.initialize();

fg2 = FactorGraph();
fg2.Solver = 'Gibbs';
if (repeatable)
    fg2.Solver.setSeed(1);
end
fg2.Solver.setNumSamples(4000);
fg2.Solver.setBurnInScans(10);
r = Real(numDataPoints,1);
for i = 1:(numDataPoints-1)
    fg2.addFactor('Normal',r(i) * 1.1, transitionPrecision, r(i+1));
end


i = 1;
fg.NumSteps = 0;
while fg.hasNext()
    r(i).Input = FactorFunction('Normal', data(i), dataPrecision);
    r(i+1).Input = FactorFunction('Normal', data(i+1), dataPrecision);
    
    fg.solveOneStep();
    fg2.solve();
    
    assertElementsAlmostEqual(vars.get(1).Solver.getSampleMean, r(i).Solver.getSampleMean, 'absolute', 0.01);
    assertElementsAlmostEqual(vars.get(1).Solver.getSampleVariance/r(i).Solver.getSampleVariance, 1, 'absolute', 0.1);
    assertElementsAlmostEqual(vars.get(2).Solver.getSampleMean, r(i+1).Solver.getSampleMean, 'absolute', 0.01);
    assertElementsAlmostEqual(vars.get(2).Solver.getSampleVariance/r(i+1).Solver.getSampleVariance, 1, 'absolute', 0.1);
    
    fg.advance();
    i = i + 1;
end

end

