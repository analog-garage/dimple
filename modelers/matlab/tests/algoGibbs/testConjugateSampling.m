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

function testConjugateSampling()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testComplexVariables');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);
test6(debugPrint, repeatable);
test7(debugPrint, repeatable);
test8(debugPrint, repeatable);
test9(debugPrint, repeatable);
test10(debugPrint, repeatable);
test11(debugPrint, repeatable);
test12(debugPrint, repeatable);
test13(debugPrint, repeatable);
test14(debugPrint, repeatable);
test15(debugPrint, repeatable);
test16(debugPrint, repeatable);
test17(debugPrint, repeatable);
test18(debugPrint, repeatable);
test19(debugPrint, repeatable);
test20(debugPrint, repeatable);
test21(debugPrint, repeatable);
test22(debugPrint, repeatable);
test23(debugPrint, repeatable);
test24(debugPrint, repeatable);
test25(debugPrint, repeatable);
test26(debugPrint, repeatable);

dtrace(debugPrint, '--testComplexVariables');

end

% Basic test, Normal sampler
function test1(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
x.FixedValue = data;

fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);

end



% Basic test, Normal sampler, constant data
function test2(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();

m.Input = FactorFunction('Normal',priorMean,priorPrecision);

dataTemp = num2cell(data);
fg.addFactor('Normal', m, dataPrecision, dataTemp{:});    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);

end




% Setting samplers
function test3(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
x.FixedValue = data;

fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);


% Try again with slice sampler

m.Solver.setSampler('SliceSampler');

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.setNumSamples(2000);
fg.solve();

mss = m.Solver.getAllSamples;

assert(strcmp(m.Solver.getSamplerName,'SliceSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));
assertElementsAlmostEqual(mean(mss), expectedMean, 'absolute', 0.1);
assertElementsAlmostEqual(std(mss), 1/sqrt(expectedPrecision), 'absolute', 0.1);


% Try again with MH

m.Solver.setSampler('MHSampler');

if (repeatable)
    fg.Solver.setSeed(2);					% Make this repeatable
end
fg.Solver.setScansPerSample(10);
fg.Solver.setNumSamples(1000);
fg.solve();

msmh = m.Solver.getAllSamples;

assert(strcmp(m.Solver.getSamplerName,'MHSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));
assertElementsAlmostEqual(mean(msmh), expectedMean, 'absolute', 0.25);
assertElementsAlmostEqual(std(msmh), 1/sqrt(expectedPrecision), 'absolute', 0.25);


end


% Basic test; prior using factor instead of input
function test4(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();
x = Real(1,numDatapoints);

x.FixedValue = data;

fg.addFactor(FactorFunction('Normal',priorMean,priorPrecision), m);
fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);

end



% Gamma prior on precision
function test5(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 2;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedAlpha = priorAlpha + numDatapoints / 2;
expectedBeta = priorBeta + sum((data - dataMean).^2) / 2;

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();
x = Real(1,numDatapoints);

p.Input = FactorFunction('Gamma',priorAlpha,priorBeta);
x.FixedValue = data;

fg.addFactor('Normal', dataMean, p, x);

assert(strcmp(p.Solver.getSamplerName,'GammaSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples;

% Estimate the parameters (see Wikipedia page on Gamma distribution)
s = log(sum(ps)/numSamples) - sum(log(ps))/numSamples;
alphaEst = (3 - s + sqrt((s-3)^2 + 24*s)) / (12 * s);
betaEst = alphaEst * numSamples / sum(ps);

assertElementsAlmostEqual(alphaEst, expectedAlpha, 'absolute', 2.5);
assertElementsAlmostEqual(betaEst, expectedBeta, 'absolute', 0.1);

% Test repeatablibility with reset seed
fg.Solver.setSeed(1);
fg.solve();
ps2 = p.Solver.getAllSamples;

fg.Solver.setSeed(1);
fg.solve();
ps3 = p.Solver.getAllSamples;

assertEqual(ps2, ps3);      % All samples should be equal

% Test non-repeatabilibity without resetting the seed
fg.solve();
ps4 = p.Solver.getAllSamples;
assert(~any(ps3 == ps4));   % No samples should be exactly equal

end


% Gamma prior on precision, using separate factor
function test6(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 2;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedAlpha = priorAlpha + numDatapoints / 2;
expectedBeta = priorBeta + sum((data - dataMean).^2) / 2;

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();
x = Real(1,numDatapoints);

x.FixedValue = data;

fg.addFactor(FactorFunction('Gamma',priorAlpha,priorBeta), p);
fg.addFactor('Normal', dataMean, p, x);

assert(strcmp(p.Solver.getSamplerName,'GammaSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples;

% Estimate the parameters (see Wikipedia page on Gamma distribution)
s = log(sum(ps)/numSamples) - sum(log(ps))/numSamples;
alphaEst = (3 - s + sqrt((s-3)^2 + 24*s)) / (12 * s);
betaEst = alphaEst * numSamples / sum(ps);

assertElementsAlmostEqual(alphaEst, expectedAlpha, 'absolute', 2.5);
assertElementsAlmostEqual(betaEst, expectedBeta, 'absolute', 0.1);

% Test repeatablibility with reset seed
fg.Solver.setSeed(1);
fg.solve();
ps2 = p.Solver.getAllSamples;

fg.Solver.setSeed(1);
fg.solve();
ps3 = p.Solver.getAllSamples;

assertEqual(ps2, ps3);      % All samples should be equal

% Test non-repeatabilibity without resetting the seed
fg.solve();
ps4 = p.Solver.getAllSamples;
assert(~any(ps3 == ps4));   % No samples should be exactly equal

end



% Test the default sampler
function test7(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setDefaultRealSampler('MHSampler');   % Set a different default sampler

m = Real([-1000, Inf]); % Set bounds, so NormalSampler won't be used for m
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
x.FixedValue = data;

fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'MHSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(4000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.5);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.2);


end


% Test the slice sampler with poor initial sample
% THIS TEST IS ONLY REALLY NEEDED UNTIL RANDOMRESTART IS GIVES IMPROVED
% INITIAL SAMPLE
function test8(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real([-1000, 1000]); % Set bounds, so NormalSampler won't be used for m
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
x.FixedValue = data;

fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'SliceSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setBurnInScans(10);  % Initial samples are poor, so let it burn in a bit
fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.5);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.2);


end



% Log-normal test mean parameter
function test9(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = exp(dataMean + randn(1,numDatapoints) * dataPrecision);
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(log(data)) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
x.FixedValue = data;

fg.addFactor('LogNormal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);

end



% LogNormal test Gamma prior on precision
function test10(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 2;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = exp(dataMean + randn(1,numDatapoints) * dataPrecision);
expectedAlpha = priorAlpha + numDatapoints / 2;
expectedBeta = priorBeta + sum((log(data) - dataMean).^2) / 2;

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();
x = Real(1,numDatapoints);

p.Input = FactorFunction('Gamma',priorAlpha,priorBeta);
x.FixedValue = data;

fg.addFactor('LogNormal', dataMean, p, x);

assert(strcmp(p.Solver.getSamplerName,'GammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples;

% Estimate the parameters (see Wikipedia page on Gamma distribution)
s = log(sum(ps)/numSamples) - sum(log(ps))/numSamples;
alphaEst = (3 - s + sqrt((s-3)^2 + 24*s)) / (12 * s);
betaEst = alphaEst * numSamples / sum(ps);

assertElementsAlmostEqual(alphaEst, expectedAlpha, 'absolute', 2.5);
assertElementsAlmostEqual(betaEst, expectedBeta, 'absolute', 0.1);

end



% Gamma prior on Categorical distribution
function test11(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
discreteDomainSize = 10;
discreteDomain = 0:discreteDomainSize-1;
distribution = randSimplex(discreteDomainSize);
numDatapoints = 100;
data = discreteSample(distribution, numDatapoints);
expectedAlpha = priorAlpha + sum(bsxfun(@eq, data, discreteDomain'),2);
expectedBeta = priorBeta;

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real(1, discreteDomainSize);

p.Input = FactorFunction('Gamma',priorAlpha,priorBeta);

% Use the built-in constructor
x = Categorical(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p(1).Solver.getSamplerName,'GammaSampler'));
assert(strcmp(p(2).Solver.getSamplerName,'GammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.invokeSolverMethodWithReturnValue('getAllSamples');

% Estimate the parameters (see Wikipedia page on Gamma distribution)
for i=1:discreteDomainSize
    s = log(sum(ps{i})/numSamples) - sum(log(ps{i}))/numSamples;
    alphaEst = (3 - s + sqrt((s-3)^2 + 24*s)) / (12 * s);
    betaEst = alphaEst * numSamples / sum(ps{i});
    
    assertElementsAlmostEqual(alphaEst/expectedAlpha(i), 1, 'absolute', 0.10);
    assertElementsAlmostEqual(betaEst, expectedBeta, 'absolute', 0.1);
end


end


% Gamma prior on Categorical distribution, values constant
function test12(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
discreteDomainSize = 10;
discreteDomain = 0:discreteDomainSize-1;
distribution = randSimplex(discreteDomainSize);
numDatapoints = 100;
data = discreteSample(distribution, numDatapoints);
expectedAlpha = priorAlpha + sum(bsxfun(@eq, data, discreteDomain'),2);
expectedBeta = priorBeta;

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real(1, discreteDomainSize);

p.Input = FactorFunction('Gamma',priorAlpha,priorBeta);

dataTemp = num2cell(data);
fg.addFactor(FactorFunction('CategoricalUnnormalizedParameters',discreteDomainSize), p, dataTemp{:});

assert(strcmp(p(1).Solver.getSamplerName,'GammaSampler'));
assert(strcmp(p(2).Solver.getSamplerName,'GammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(6);					% Make this repeatable
end

fg.solve();

ps = p.invokeSolverMethodWithReturnValue('getAllSamples');

% Estimate the parameters (see Wikipedia page on Gamma distribution)
for i=1:discreteDomainSize
    s = log(sum(ps{i})/numSamples) - sum(log(ps{i}))/numSamples;
    alphaEst = (3 - s + sqrt((s-3)^2 + 24*s)) / (12 * s);
    betaEst = alphaEst * numSamples / sum(ps{i});
    
    assertElementsAlmostEqual(alphaEst/expectedAlpha(i), 1, 'absolute', 0.10);
    assertElementsAlmostEqual(betaEst, expectedBeta, 'absolute', 0.1);
end

end



% Dirichlet prior on Categorical distribution
function test13(debugPrint, repeatable)

discreteDomainSize = 10;
priorAlpha = ones(discreteDomainSize,1);
discreteDomain = 0:discreteDomainSize-1;
distribution = randSimplex(discreteDomainSize);
numDatapoints = 100;
data = discreteSample(distribution, numDatapoints);
expectedAlpha = priorAlpha + sum(bsxfun(@eq, data, discreteDomain'),2);
expectedAlpha = expectedAlpha/sum(expectedAlpha);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = RealJoint(discreteDomainSize);

p.Input = FactorFunction('Dirichlet',priorAlpha);

% Use built-in constructor
x = Categorical(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p.Solver.getSamplerName,'DirichletSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

alphaEst = sum(ps,1);
alphaEst = alphaEst/sum(alphaEst);
for i=1:discreteDomainSize
    assertElementsAlmostEqual(alphaEst(i)/expectedAlpha(i), 1, 'absolute', 0.02);
end

end



% Dirichlet prior on Categorical distribution, values constant
function test14(debugPrint, repeatable)

discreteDomainSize = 10;
priorAlpha = ones(discreteDomainSize,1);
discreteDomain = 0:discreteDomainSize-1;
distribution = randSimplex(discreteDomainSize);
numDatapoints = 100;
data = discreteSample(distribution, numDatapoints);
expectedAlpha = priorAlpha + sum(bsxfun(@eq, data, discreteDomain'),2);
expectedAlpha = expectedAlpha/sum(expectedAlpha);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = RealJoint(discreteDomainSize);

p.Input = FactorFunction('Dirichlet',priorAlpha);

dataTemp = num2cell(data);
fg.addFactor('Categorical', p, dataTemp{:});

assert(strcmp(p.Solver.getSamplerName,'DirichletSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

alphaEst = sum(ps,1);
alphaEst = alphaEst/sum(alphaEst);
for i=1:discreteDomainSize
    assertElementsAlmostEqual(alphaEst(i)/expectedAlpha(i), 1, 'absolute', 0.02);
end

end



% Beta prior on Bernoulli distribution
function test15(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
pData = rand();
numDatapoints = 100;
data = discreteSample([1-pData pData], numDatapoints);
expectedP = (priorAlpha + sum(data))/(priorAlpha + priorBeta + numDatapoints);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();

p.Input = FactorFunction('Beta',priorAlpha,priorBeta);

% Use built-in constructor
x = Bernoulli(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

pEst = mean(ps);
assertElementsAlmostEqual(pEst/expectedP, 1, 'absolute', 0.02);

end



% Beta prior on Bernoulli distribution, values constant
function test16(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
pData = rand();
numDatapoints = 100;
data = discreteSample([1-pData pData], numDatapoints);
expectedP = (priorAlpha + sum(data))/(priorAlpha + priorBeta + numDatapoints);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();

p.Input = FactorFunction('Beta',priorAlpha,priorBeta);

dataTemp = num2cell(data);
fg.addFactor('Bernoulli', p, dataTemp{:});

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

pEst = mean(ps);
assertElementsAlmostEqual(pEst/expectedP, 1, 'absolute', 0.02);

end



% Beta prior on Binomial distribution, fixed N parameter
function test17(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
pData = rand();
numDatapoints = 100;
data = discreteSample([1-pData pData], numDatapoints);
expectedP = (priorAlpha + sum(data))/(priorAlpha + priorBeta + numDatapoints);

fg = FactorGraph();
fg.Solver = 'Gibbs';

N = 100;
p = Real();

p.Input = FactorFunction('Beta',priorAlpha,priorBeta);

% Use built-in constructor
x = Binomial(N, p);
x.FixedValue = sum(data);

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

pEst = mean(ps);
assertElementsAlmostEqual(pEst/expectedP, 1, 'absolute', 0.01);

end


% ExchangeableDirichlet prior on Categorical distribution
function test18(debugPrint, repeatable)

discreteDomainSize = 10;
priorAlpha = 1;
discreteDomain = 0:discreteDomainSize-1;
distribution = randSimplex(discreteDomainSize);
numDatapoints = 100;
data = discreteSample(distribution, numDatapoints);
expectedAlpha = priorAlpha + sum(bsxfun(@eq, data, discreteDomain'),2);
expectedAlpha = expectedAlpha/sum(expectedAlpha);

fg = FactorGraph();
fg.Solver = 'Gibbs';


p = ExchangeableDirichlet(discreteDomainSize, priorAlpha);

% Use built-in constructor
x = Categorical(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p.Solver.getSamplerName,'DirichletSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(2);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

alphaEst = sum(ps,1);
alphaEst = alphaEst/sum(alphaEst);
for i=1:discreteDomainSize
    assertElementsAlmostEqual(alphaEst(i)/expectedAlpha(i), 1, 'absolute', 0.03);
end

end


% Multiple Beta priors on Bernoulli distribution
function test19(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
pData = rand();
numDatapoints = 100;
data = discreteSample([1-pData pData], numDatapoints);
expectedP = (priorAlpha + sum(data))/(priorAlpha + priorBeta + numDatapoints);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();

p.Input = FactorFunction('Beta',priorAlpha,priorBeta);
fg.addFactor(FactorFunction('Beta',priorAlpha,priorBeta), p);
fg.addFactor(FactorFunction('Beta',priorAlpha,priorBeta), p);
fg.addFactor(FactorFunction('Beta',priorAlpha,priorBeta), p);
fg.addFactor(FactorFunction('Beta',priorAlpha,priorBeta), p);

% Use built-in constructor
x = Bernoulli(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

pEst = mean(ps);
assertElementsAlmostEqual(pEst/expectedP, 1, 'absolute', 0.02);

end


% No Beta prior on Bernoulli distribution
function test20(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
pData = rand();
numDatapoints = 100;
data = discreteSample([1-pData pData], numDatapoints);
expectedP = (priorAlpha + sum(data))/(priorAlpha + priorBeta + numDatapoints);

fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();

% Use built-in constructor
x = Bernoulli(p, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();

pEst = mean(ps);
assertElementsAlmostEqual(pEst/expectedP, 1, 'absolute', 0.02);

end


% No Beta prior on Bernoulli distribution, data as likelihood function
function test21(debugPrint, repeatable)

pLikelihood = 0.95;
fg = FactorGraph();
fg.Solver = 'Gibbs';

p = Real();

% Use built-in constructor
x = Bernoulli(p);
x.Input = pLikelihood;

assert(strcmp(p.Solver.getSamplerName,'BetaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ps = p.Solver.getAllSamples();
px = x.Solver.getAllSampleIndices();
pEst = mean(ps);

% Expected value of 2(xp + (1-x)(1-p)), a weighted mixture for a single sample
pExpected = (pLikelihood+1)/3;

assertElementsAlmostEqual(pEst/pExpected, 1, 'absolute', 0.02);
assertElementsAlmostEqual(nnz(px)/length(px), pLikelihood, 'absolute', 0.01);

end



% Beta prior on Multinomial distribution, fixed N parameter
function test22(debugPrint, repeatable)

dim = 10;
priorAlpha = ones(1,dim);
xData = randi(100,1,dim)-1;
N = sum(xData);
expectedAlpha = xData/N;

fg = FactorGraph();
fg.Solver = 'Gibbs';

alpha = RealJoint(dim);
alpha.Input = FactorFunction('Dirichlet',priorAlpha);

% Use built-in constructor
x = Multinomial(N, alpha);
x.FixedValue = xData;

assert(strcmp(alpha.Solver.getSamplerName,'DirichletSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = alpha.Solver.getAllSamples();

aEst = mean(as,1);
assertElementsAlmostEqual(aEst/expectedAlpha, 1, 'absolute', 0.01);

end



% Beta prior on MultinomialUnnormalizedParameters, fixed N parameter
function test23(debugPrint, repeatable)

dim = 10;
priorAlpha = 1;
xData = randi(100,1,dim)-1;
N = sum(xData);
expectedAlpha = xData/N;

fg = FactorGraph();
fg.Solver = 'Gibbs';

alpha = Real(1,dim);
alpha.Input = FactorFunction('Gamma',priorAlpha,1);

% Use built-in constructor
x = Multinomial(N, alpha);
x.FixedValue = xData;

assert(strcmp(alpha(1).Solver.getSamplerName,'GammaSampler'));
assert(strcmp(alpha(dim).Solver.getSamplerName,'GammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = cell2mat(alpha.invokeSolverMethodWithReturnValue('getAllSamples'));

aEst = mean(as,1);
aEst = aEst/sum(aEst);  % Normalize
assertElementsAlmostEqual(aEst/expectedAlpha, 1, 'absolute', 0.01);

end


% Beta prior on MultinomialEnergyParameters, fixed N parameter
function test24(debugPrint, repeatable)

dim = 10;
priorAlpha = 1;
xData = randi(100,1,dim)-1;
N = sum(xData);
expectedAlpha = xData/N;

fg = FactorGraph();
fg.Solver = 'Gibbs';

alpha = Real(1,dim);
alpha.Input = FactorFunction('NegativeExpGamma',priorAlpha,1);

% Use built-in constructor
x = MultinomialEnergyParameters(N, alpha);
x.FixedValue = xData;

assert(strcmp(alpha(1).Solver.getSamplerName,'NegativeExpGammaSampler'));
assert(strcmp(alpha(dim).Solver.getSamplerName,'NegativeExpGammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = cell2mat(alpha.invokeSolverMethodWithReturnValue('getAllSamples'));

as = exp(-as);    % Convert from energy values
aEst = mean(as,1);
aEst = aEst/sum(aEst);  % Normalize
assertElementsAlmostEqual(aEst/expectedAlpha, 1, 'absolute', 0.01);

end



% Gamma prior on Poisson distribution
function test25(debugPrint, repeatable)

priorAlpha = 1;
priorBeta = 1;
maxK = 100;
lambdaData = 7;
numDatapoints = 1000;

% Sample from Poisson (see Wikipedia on Poisson distribution)
L = exp(-lambdaData);
data = zeros(1,numDatapoints);
for i = 1:numDatapoints
    k = 0;
    p = 1;
    while (p > L)
        k = k + 1;
        p = p * rand;
    end
    data(i) = k - 1;
end

% Create the graph
fg = FactorGraph();
fg.Solver = 'Gibbs';

% Use built-in constructors
lambda = Gamma(priorAlpha, priorBeta);
x = Poisson(lambda, maxK, [1, numDatapoints]);
x.FixedValue = data;

assert(strcmp(lambda.Solver.getSamplerName,'GammaSampler'));

numSamples = 1000;
fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ls = lambda.Solver.getAllSamples();

lambdaMean = mean(ls);
assertElementsAlmostEqual(lambdaMean/lambdaData, 1, 'absolute', 0.01);

end


% Normal sampler, variable data
function test26(debugPrint, repeatable)

priorMean = 3;
priorPrecision = 0.01;
dataMean = 10;
dataPrecision = .001;
numDatapoints = 100;
data = dataMean + randn(1,numDatapoints) * dataPrecision;
expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
expectedMean = (priorMean * priorPrecision + sum(data) * dataPrecision) / expectedPrecision;

fg = FactorGraph();
fg.Solver = 'Gibbs';

m = Real();
x = Real(1,numDatapoints);

m.Input = FactorFunction('Normal',priorMean,priorPrecision);
for i=1:numDatapoints
    x(i).Input = {'Normal',data(i),1e6};
end

fg.addFactor('Normal', m, dataPrecision, x);    % Fixed precision

assert(strcmp(m.Solver.getSamplerName,'NormalSampler'));
assert(strcmp(x(1).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(4);					% Make this repeatable
end

fg.solve();

ms = m.Solver.getAllSamples;

assertElementsAlmostEqual(mean(ms), expectedMean, 'absolute', 0.01);
assertElementsAlmostEqual(std(ms), 1/sqrt(expectedPrecision), 'absolute', 0.05);

end



%********* UTILITIES ***************************************

% Given a column vector x in R^k contained in the standard (k-1)-simplex,
% choose n in {0,...,k-1} according to the categorical distribution x
% Optional dimension arguments create an array of samples using the same
% distribution
function n = discreteSample(x,varargin)
	n = squeeze(1+sum(bsxfun(@lt, cumsum(x(:)), rand(1,varargin{:})),1)) - 1;
end

% Choose a column vector in R^k uniformly from the standard (k-1)-simplex
function x = randSimplex(k)
	x = normalize(randExp([k,1]));
end

% Standard exponential random variables
function x = randExp(varargin)
	x = -log(rand(varargin{:}));
end

% Normalize a vector
function out = normalize(in)
	out = in/sum(in);
end

% Count instances
function out = count(domain, in)
    in = reshape(in, 1, []);
    domain = reshape(domain, [], 1);
    out = sum(bsxfun(@eq, domain, in), 2);
end
