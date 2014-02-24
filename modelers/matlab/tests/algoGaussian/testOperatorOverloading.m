%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

function testOperatorOverloading()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testOperatorOverloading');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);

dtrace(debugPrint, '--testOperatorOverloading');

end

% Real variables
function test1(debugPrint, repeatable)

a = Real();
b = Real();

fg = FactorGraph();
fg.Solver = 'Gaussian';

% Can overload + and *-constant operators with Gaussian custom factors
z = (a + 2.7 * b) * 17;

mus = [8 10];
sigmas = [1 2];
a.Input = [mus(1) sigmas(1)];
b.Input = [mus(2) sigmas(2)];

fg.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded

a2 = Real();
b2 = Real();
x2 = Real();
y2 = Real();
z2 = Real();

fg2 = FactorGraph();
fg2.Solver = 'Gaussian';

fg2.addFactor('constmult', x2, b2, 2.7);
fg2.addFactor('add', y2, x2, a2);
fg2.addFactor('constmult', z2, y2, 17);

a2.Input = [mus(1) sigmas(1)];
b2.Input = [mus(2) sigmas(2)];

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertEqual(a.Belief(1),a2.Belief(1));
assertEqual(a.Belief(2),a2.Belief(2));
assertEqual(b.Belief(1),b2.Belief(1));
assertEqual(b.Belief(2),b2.Belief(2));
assertEqual(z.Belief(1),z2.Belief(1));
assertEqual(z.Belief(2),z2.Belief(2));

end


% RealJoint variables
function test2(debugPrint, repeatable)

dim = 7;
K = rand(dim,1);
k = rand;

ainMu = rand(dim,1);
ainCo = diag(rand(dim,1)) + eye(dim)*0.1;

binMu = rand(dim,1);
binCo = diag(rand(dim,1)) + eye(dim)*0.1;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Operator overloaded graph
fg = FactorGraph();
fg.Solver = 'Gaussian';

a = RealJoint(dim);
b = RealJoint(dim);

% Can overload + and *-constant operators with Gaussian custom factors
aa = a + b;
bb = a - b;
cc = -a;
% dd = a + K;   % FIXME: Custom Gaussian factors don't yet support constants
% ee = K + b;
% ff = a - K;
% gg = K - b;
% hh = a + k;
% ii = k + b;
% jj = a - k;
% kk = k - b;

a.Input = MultivariateNormalParameters(ainMu, ainCo);
b.Input = MultivariateNormalParameters(binMu, binCo);

fg.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare to expected results
assertElementsAlmostEqual(aa.Belief.Mean, ainMu + binMu);
assertElementsAlmostEqual(aa.Belief.Covariance, ainCo + binCo);
assertElementsAlmostEqual(bb.Belief.Mean, ainMu - binMu);
assertElementsAlmostEqual(bb.Belief.Covariance, ainCo + binCo);
assertElementsAlmostEqual(cc.Belief.Mean, -ainMu);
assertElementsAlmostEqual(cc.Belief.Covariance, ainCo);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded version

fg2 = FactorGraph();
fg2.Solver = 'Gaussian';

a2 = RealJoint(dim);
b2 = RealJoint(dim);
aa2 = RealJoint(dim);
bb2 = RealJoint(dim);
cc2 = RealJoint(dim);
% dd2 = RealJoint(dim);
% ee2 = RealJoint(dim);
% ff2 = RealJoint(dim);
% gg2 = RealJoint(dim);
% hh2 = RealJoint(dim);
% ii2 = RealJoint(dim);
% jj2 = RealJoint(dim);
% kk2 = RealJoint(dim);

% Can overload + and *-constant operators with Gaussian custom factors
fg2.addFactor('RealJointSum', aa2, a2, b2);
fg2.addFactor('RealJointSubtract', bb2, a2, b2);
fg2.addFactor('RealJointNegate', cc2, a2);
% fg2.addFactor('RealJointSum', dd2, a2, K);
% fg2.addFactor('RealJointSum', ee2, K, b2);
% fg2.addFactor('RealJointSubtract', ff2, a2, K);
% fg2.addFactor('RealJointSubtract', gg2, K, b2);
% fg2.addFactor('RealJointSum', hh2, a2, repmat(k,1,dim));
% fg2.addFactor('RealJointSum', ii2, repmat(k,1,dim), b2);
% fg2.addFactor('RealJointSubtract', jj2, a2, repmat(k,1,dim));
% fg2.addFactor('RealJointSubtract', kk2, repmat(k,1,dim), b2);

a2.Input = MultivariateNormalParameters(ainMu, ainCo);
b2.Input = MultivariateNormalParameters(binMu, binCo);

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertElementsAlmostEqual(a.Belief.Mean, a2.Belief.Mean);
assertElementsAlmostEqual(a.Belief.Covariance, a2.Belief.Covariance);
assertElementsAlmostEqual(b.Belief.Mean, b2.Belief.Mean);
assertElementsAlmostEqual(b.Belief.Covariance, b2.Belief.Covariance);
assertElementsAlmostEqual(aa.Belief.Mean, aa2.Belief.Mean);
assertElementsAlmostEqual(aa.Belief.Covariance, aa2.Belief.Covariance);
assertElementsAlmostEqual(bb.Belief.Mean, bb2.Belief.Mean);
assertElementsAlmostEqual(bb.Belief.Covariance, bb2.Belief.Covariance);
assertElementsAlmostEqual(cc.Belief.Mean, cc2.Belief.Mean);
assertElementsAlmostEqual(cc.Belief.Covariance, cc2.Belief.Covariance);
% assertElementsAlmostEqual(dd.Belief.Mean, dd2.Belief.Mean);
% assertElementsAlmostEqual(dd.Belief.Covariance, dd2.Belief.Covariance);
% assertElementsAlmostEqual(ee.Belief.Mean, ee2.Belief.Mean);
% assertElementsAlmostEqual(ee.Belief.Covariance, ee2.Belief.Covariance);
% assertElementsAlmostEqual(ff.Belief.Mean, ff2.Belief.Mean);
% assertElementsAlmostEqual(ff.Belief.Covariance, ff2.Belief.Covariance);
% assertElementsAlmostEqual(gg.Belief.Mean, gg2.Belief.Mean);
% assertElementsAlmostEqual(gg.Belief.Covariance, gg2.Belief.Covariance);
% assertElementsAlmostEqual(hh.Belief.Mean, hh2.Belief.Mean);
% assertElementsAlmostEqual(hh.Belief.Covariance, hh2.Belief.Covariance);
% assertElementsAlmostEqual(ii.Belief.Mean, ii2.Belief.Mean);
% assertElementsAlmostEqual(ii.Belief.Covariance, ii2.Belief.Covariance);
% assertElementsAlmostEqual(jj.Belief.Mean, jj2.Belief.Mean);
% assertElementsAlmostEqual(jj.Belief.Covariance, jj2.Belief.Covariance);
% assertElementsAlmostEqual(kk.Belief.Mean, kk2.Belief.Mean);
% assertElementsAlmostEqual(kk.Belief.Covariance, kk2.Belief.Covariance);

end

