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
x = Discrete(discreteDomain, 1, numDatapoints);

p.Input = FactorFunction('Gamma',priorAlpha,priorBeta);
x.FixedValue = data;

fg.addFactor(FactorFunction('Categorical',discreteDomainSize), p, x);

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
fg.addFactor(FactorFunction('Categorical',discreteDomainSize), p, dataTemp{:});

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



% Given a column vector x in R^k contained in the standard (k-1)-simplex,
% choose n in {0,...,k-1} according to the categorical distribution x
% Optional dimension arguments create an array of samples using the same
% distribution
function n = discreteSample(x,varargin)
	n = squeeze(1+sum(bsxfun(@lt, cumsum(vec(x)), rand(1,varargin{:})),1)) - 1;
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
