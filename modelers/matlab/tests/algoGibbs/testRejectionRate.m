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

function testRejectionRate()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRejectionRate');

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);

dtrace(debugPrint, '--testRejectionRate');

end

% Basic test
function test1(debugPrint, repeatable)

[rejectionRate1, numScores1] = runTestCase(debugPrint, repeatable);
assert(rejectionRate1 == 0);
[rejectionRate2, numScores2] = runTestCase(debugPrint, repeatable, 'MHSampler');
assertElementsAlmostEqual(rejectionRate2, 0.3, 'absolute', 0.01);
assert(all(numScores1([1:2 5]) >= 6));
assert(all(numScores1([3:4 6:8])==0));
assert(all(numScores2([1:2 5]) == 2));
assert(all(numScores2([3:4 6:8])==0));

end

function [rejectionRate, numScores] = runTestCase(debugPrint, repeatable, varargin)

fg = FactorGraph;
fg.Solver = 'Gibbs';

if (nargin > 2)
    fg.Solver.setDefaultRealSampler(varargin{1});
end

x = (Normal(0,1) + Gamma(1,1)) ^ 2;
y = x + log(LogNormal(2,7)) ^ 2;
y.FixedValue = 5;

if (repeatable)
    fg.Solver.setSeed(1);
end
fg.Solver.setNumSamples(1000);
fg.Solver.setBurnInScans(100);
fg.solve();

rejectionRate = fg.Solver.getRejectionRate();
numScores = cellfun(@(x)x.Solver.getNumScoresPerUpdate, fg.Variables);

end

% Block MH
function test2(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

% Multinomial creates a BlockMHSampler to sample its output variables
% which should be counted in the rejection rate
dim = 4;
N = 10;
alpha = RealJoint(dim);
M = Discrete(0:N, 1, dim);
fg.addFactor({'ExchangeableDirichlet', dim, 1.5}, alpha);
fg.addFactor({'Multinomial', N}, alpha, M);
M.Input = rand(1, dim, N+1);

if (repeatable)
    fg.Solver.setSeed(1);
end
fg.Solver.setNumSamples(100);
fg.Solver.setBurnInScans(10);
fg.solve();

assert(fg.Solver.getRejectionRate() > 0);
assert(alpha.Solver.getRejectionRate() == 0);   % Conjugate sampled

end


% Reset statistics
function test3(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';
fg.Solver.setDefaultRealSampler('MHSampler');

a = Normal(0,1);
b = Gamma(1,1);
x = (a + b) ^ 2;
y = x + log(LogNormal(2,7)) ^ 2;
y.FixedValue = 5;

dim = 4;
N = 10;
alpha = RealJoint(dim);
M = Discrete(0:N, 1, dim);
fg.addFactor({'ExchangeableDirichlet', dim}, x, alpha);
fg.addFactor({'Multinomial', N}, alpha, M);
M.Input = rand(1, dim, N+1);

if (repeatable)
    fg.Solver.setSeed(1);
end
fg.Solver.setNumSamples(100);
fg.Solver.setBurnInScans(10);
fg.solve();

assert(fg.Solver.getRejectionRate() > 0);
assert(a.Solver.getRejectionRate() > 0);
assert(b.Solver.getRejectionRate() > 0);
assert(x.Solver.getRejectionRate() == 0);  % Deterministic depenent
assert(alpha.Solver.getRejectionRate() == 0);   % Conjugate sampled

a.Solver.resetRejectionRateStats();
assert(a.Solver.getRejectionRate() == 0);
assert(b.Solver.getRejectionRate() > 0);
assert(fg.Solver.getRejectionRate() > 0);

fg.Solver.resetRejectionRateStats();
assert(fg.Solver.getRejectionRate() == 0);
assert(a.Solver.getRejectionRate() == 0);
assert(b.Solver.getRejectionRate() == 0);
assert(x.Solver.getRejectionRate() == 0);
assert(alpha.Solver.getRejectionRate() == 0);

fg.solve();

assert(fg.Solver.getRejectionRate() > 0);
assert(a.Solver.getRejectionRate() > 0);
assert(b.Solver.getRejectionRate() > 0);
assert(x.Solver.getRejectionRate() == 0);  % Deterministic depenent
assert(alpha.Solver.getRejectionRate() == 0);   % Conjugate sampled

end



