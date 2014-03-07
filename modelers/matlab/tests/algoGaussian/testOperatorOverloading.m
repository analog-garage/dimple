%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013-2014 Analog Devices, Inc.
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

dtrace(debugPrint, '--testOperatorOverloading');

end

% Real variables
function test1(debugPrint, repeatable)

a = Real();
b = Real();

fg = FactorGraph();

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

fg2.addFactor('constmult', x2, b2, 2.7);
fg2.addFactor('add', y2, x2, a2);
fg2.addFactor('constmult', z2, y2, 17);

a2.Input = [mus(1) sigmas(1)];
b2.Input = [mus(2) sigmas(2)];

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(b.Belief,b2.Belief);
assertElementsAlmostEqual(b.Belief,b2.Belief);
assertElementsAlmostEqual(z.Belief,z2.Belief);
assertElementsAlmostEqual(z.Belief,z2.Belief);

end



% More Real variable cases
function test2(debugPrint, repeatable)

K = rand;

ainMu = rand;
ainVar = rand + 0.1;
ainStd = sqrt(ainVar);
ainPre = 1/ainVar;

binMu = rand;
binVar = rand + 0.1;
binStd = sqrt(binVar);
binPre = 1/binVar;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Operator overloaded graph
fg = FactorGraph();

a = Real();
b = Real();

% Can overload +/- and *-constant operators with Gaussian custom factors
aa = a + b;
bb = a - b;
cc = -a;
dd = a + K;
ee = K + b;
ff = a - K;
gg = K - b;
hh = a * K;
ii = K * b;

assert(~isempty(strfind(aa.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(bb.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(cc.Factors{1}.Solver.toString, 'CustomGaussianNegate')));
assert(~isempty(strfind(dd.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ee.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ff.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(gg.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(hh.Factors{1}.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(ii.Factors{1}.Solver.toString, 'CustomGaussianProduct')));

a.Input = {'Normal', ainMu, ainPre};
b.Input = {'Normal', binMu, binPre};

fg.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare to expected results
assertElementsAlmostEqual(aa.Belief(1), ainMu + binMu);
assertElementsAlmostEqual(aa.Belief(2)^2, ainVar + binVar);
assertElementsAlmostEqual(bb.Belief(1), ainMu - binMu);
assertElementsAlmostEqual(bb.Belief(2)^2, ainVar + binVar);
assertElementsAlmostEqual(cc.Belief(1), -ainMu);
assertElementsAlmostEqual(cc.Belief(2)^2, ainVar);
assertElementsAlmostEqual(dd.Belief(1), ainMu + K);
assertElementsAlmostEqual(dd.Belief(2)^2, ainVar);
assertElementsAlmostEqual(ee.Belief(1), K + binMu);
assertElementsAlmostEqual(ee.Belief(2)^2, binVar);
assertElementsAlmostEqual(ff.Belief(1), ainMu - K);
assertElementsAlmostEqual(ff.Belief(2)^2, ainVar);
assertElementsAlmostEqual(gg.Belief(1), K - binMu);
assertElementsAlmostEqual(gg.Belief(2)^2, binVar);
assertElementsAlmostEqual(hh.Belief(1), K * ainMu);
assertElementsAlmostEqual(hh.Belief(2), K * ainStd);
assertElementsAlmostEqual(ii.Belief(1), K * binMu);
assertElementsAlmostEqual(ii.Belief(2), K * binStd);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded version

fg2 = FactorGraph();

a2 = Real();
b2 = Real();
aa2 = Real();
bb2 = Real();
cc2 = Real();
dd2 = Real();
ee2 = Real();
ff2 = Real();
gg2 = Real();
hh2 = Real();
ii2 = Real();

% Can overload +/- and *-constant operators with Gaussian custom factors
fg2.addFactor('Sum', aa2, a2, b2);
fg2.addFactor('Subtract', bb2, a2, b2);
fg2.addFactor('Negate', cc2, a2);
fg2.addFactor('Sum', dd2, a2, K);
fg2.addFactor('Sum', ee2, K, b2);
fg2.addFactor('Subtract', ff2, a2, K);
fg2.addFactor('Subtract', gg2, K, b2);
fg2.addFactor('Product', hh2, a2, K);
fg2.addFactor('Product', ii2, K, b2);

assert(~isempty(strfind(aa2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(bb2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(cc2.Factors{1}.Solver.toString, 'CustomGaussianNegate')));
assert(~isempty(strfind(dd2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ee2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ff2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(gg2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(hh2.Factors{1}.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(ii2.Factors{1}.Solver.toString, 'CustomGaussianProduct')));

a2.Input = {'Normal', ainMu, ainPre};
b2.Input = {'Normal', binMu, binPre};

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertElementsAlmostEqual(a.Belief(1), a2.Belief(1));
assertElementsAlmostEqual(a.Belief(2), a2.Belief(2));
assertElementsAlmostEqual(b.Belief(1), b2.Belief(1));
assertElementsAlmostEqual(b.Belief(2), b2.Belief(2));
assertElementsAlmostEqual(aa.Belief(1), aa2.Belief(1));
assertElementsAlmostEqual(aa.Belief(2), aa2.Belief(2));
assertElementsAlmostEqual(bb.Belief(1), bb2.Belief(1));
assertElementsAlmostEqual(bb.Belief(2), bb2.Belief(2));
assertElementsAlmostEqual(cc.Belief(1), cc2.Belief(1));
assertElementsAlmostEqual(cc.Belief(2), cc2.Belief(2));
assertElementsAlmostEqual(dd.Belief(1), dd2.Belief(1));
assertElementsAlmostEqual(dd.Belief(2), dd2.Belief(2));
assertElementsAlmostEqual(ee.Belief(1), ee2.Belief(1));
assertElementsAlmostEqual(ee.Belief(2), ee2.Belief(2));
assertElementsAlmostEqual(ff.Belief(1), ff2.Belief(1));
assertElementsAlmostEqual(ff.Belief(2), ff2.Belief(2));
assertElementsAlmostEqual(gg.Belief(1), gg2.Belief(1));
assertElementsAlmostEqual(gg.Belief(2), gg2.Belief(2));
assertElementsAlmostEqual(hh.Belief(1), hh2.Belief(1));
assertElementsAlmostEqual(hh.Belief(2), hh2.Belief(2));
assertElementsAlmostEqual(ii.Belief(1), ii2.Belief(1));
assertElementsAlmostEqual(ii.Belief(2), ii2.Belief(2));

end



% RealJoint variables
function test3(debugPrint, repeatable)

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

a = RealJoint(dim);
b = RealJoint(dim);

% Can overload +/- operators with Gaussian custom factors
aa = a + b;
bb = a - b;
cc = -a;
dd = a + K;
ee = K + b;
ff = a - K;
gg = K - b;
hh = a + k;
ii = k + b;
jj = a - k;
kk = k - b;

assert(~isempty(strfind(aa.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(bb.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(cc.Factors{1}.Solver.toString, 'CustomMultivariateGaussianNegate')));
assert(~isempty(strfind(dd.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ee.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ff.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(gg.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(hh.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ii.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(jj.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(kk.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));

a.Input = {'MultivariateNormal', ainMu, ainCo};
b.Input = {'MultivariateNormal', binMu, binCo};

fg.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare to expected results
assertElementsAlmostEqual(aa.Belief.Mean, ainMu + binMu);
assertElementsAlmostEqual(aa.Belief.Covariance, ainCo + binCo);
assertElementsAlmostEqual(bb.Belief.Mean, ainMu - binMu);
assertElementsAlmostEqual(bb.Belief.Covariance, ainCo + binCo);
assertElementsAlmostEqual(cc.Belief.Mean, -ainMu);
assertElementsAlmostEqual(cc.Belief.Covariance, ainCo);
assertElementsAlmostEqual(dd.Belief.Mean, ainMu + K);
assertElementsAlmostEqual(dd.Belief.Covariance, ainCo);
assertElementsAlmostEqual(ee.Belief.Mean, K + binMu);
assertElementsAlmostEqual(ee.Belief.Covariance, binCo);
assertElementsAlmostEqual(ff.Belief.Mean, ainMu - K);
assertElementsAlmostEqual(ff.Belief.Covariance, ainCo);
assertElementsAlmostEqual(gg.Belief.Mean, K - binMu);
assertElementsAlmostEqual(gg.Belief.Covariance, binCo);
assertElementsAlmostEqual(hh.Belief.Mean, ainMu + k);
assertElementsAlmostEqual(hh.Belief.Covariance, ainCo);
assertElementsAlmostEqual(ii.Belief.Mean, k + binMu);
assertElementsAlmostEqual(ii.Belief.Covariance, binCo);
assertElementsAlmostEqual(jj.Belief.Mean, ainMu - k);
assertElementsAlmostEqual(jj.Belief.Covariance, ainCo);
assertElementsAlmostEqual(kk.Belief.Mean, k - binMu);
assertElementsAlmostEqual(kk.Belief.Covariance, binCo);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded version

fg2 = FactorGraph();

a2 = RealJoint(dim);
b2 = RealJoint(dim);
aa2 = RealJoint(dim);
bb2 = RealJoint(dim);
cc2 = RealJoint(dim);
dd2 = RealJoint(dim);
ee2 = RealJoint(dim);
ff2 = RealJoint(dim);
gg2 = RealJoint(dim);
hh2 = RealJoint(dim);
ii2 = RealJoint(dim);
jj2 = RealJoint(dim);
kk2 = RealJoint(dim);

% Can overload +/- operators with Gaussian custom factors
fg2.addFactor('RealJointSum', aa2, a2, b2);
fg2.addFactor('RealJointSubtract', bb2, a2, b2);
fg2.addFactor('RealJointNegate', cc2, a2);
fg2.addFactor('RealJointSum', dd2, a2, K);
fg2.addFactor('RealJointSum', ee2, K, b2);
fg2.addFactor('RealJointSubtract', ff2, a2, K);
fg2.addFactor('RealJointSubtract', gg2, K, b2);
fg2.addFactor('RealJointSum', hh2, a2, repmat(k,1,dim));
fg2.addFactor('RealJointSum', ii2, repmat(k,1,dim), b2);
fg2.addFactor('RealJointSubtract', jj2, a2, repmat(k,1,dim));
fg2.addFactor('RealJointSubtract', kk2, repmat(k,1,dim), b2);

assert(~isempty(strfind(aa2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(bb2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(cc2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianNegate')));
assert(~isempty(strfind(dd2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ee2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ff2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(gg2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(hh2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ii2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(jj2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(kk2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));

a2.Input = {'MultivariateNormal', ainMu, ainCo};
b2.Input = {'MultivariateNormal', binMu, binCo};

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
assertElementsAlmostEqual(dd.Belief.Mean, dd2.Belief.Mean);
assertElementsAlmostEqual(dd.Belief.Covariance, dd2.Belief.Covariance);
assertElementsAlmostEqual(ee.Belief.Mean, ee2.Belief.Mean);
assertElementsAlmostEqual(ee.Belief.Covariance, ee2.Belief.Covariance);
assertElementsAlmostEqual(ff.Belief.Mean, ff2.Belief.Mean);
assertElementsAlmostEqual(ff.Belief.Covariance, ff2.Belief.Covariance);
assertElementsAlmostEqual(gg.Belief.Mean, gg2.Belief.Mean);
assertElementsAlmostEqual(gg.Belief.Covariance, gg2.Belief.Covariance);
assertElementsAlmostEqual(hh.Belief.Mean, hh2.Belief.Mean);
assertElementsAlmostEqual(hh.Belief.Covariance, hh2.Belief.Covariance);
assertElementsAlmostEqual(ii.Belief.Mean, ii2.Belief.Mean);
assertElementsAlmostEqual(ii.Belief.Covariance, ii2.Belief.Covariance);
assertElementsAlmostEqual(jj.Belief.Mean, jj2.Belief.Mean);
assertElementsAlmostEqual(jj.Belief.Covariance, jj2.Belief.Covariance);
assertElementsAlmostEqual(kk.Belief.Mean, kk2.Belief.Mean);
assertElementsAlmostEqual(kk.Belief.Covariance, kk2.Belief.Covariance);

end




% More Real variable cases with more factor inputs
function test4(debugPrint, repeatable)

N = 10;
xinMu = rand(1,N);
xinVar = rand(1,N) + 0.1;
xinPre = 1./xinVar;

k = randn(1,N) * 100;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Operator overloaded graph
fg = FactorGraph();

x = Real(1,N);

aa = x(1) + x(2) + x(3) + x(4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
bb = x(1) - x(2) - x(3) - x(4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
cc = k(1) + k(2) + k(3) + x(4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
dd = k(1) - k(2) - k(3) - x(4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
ee = x(1) + x(2) + x(3) + x(4) + x(5) + x(6) + x(7) + x(8) + k(9) + k(10);
ff = x(1) - x(2) - x(3) - x(4) - x(5) - x(6) - x(7) - x(8) - k(9) - k(10);
gg = x(1) + k(2) + k(3) + k(4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
hh = x(1) - k(2) - k(3) - k(4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
ii = x(1) + k(2) + x(3) + k(4) + x(5) + k(6) + x(7) + k(8) + x(9) + k(10);
jj = x(1) - k(2) - x(3) - k(4) - x(5) - k(6) - x(7) - k(8) - x(9) - k(10);
kk = k(1) + x(2) + k(3) + k(4) + x(5) + x(6) + k(7) + x(8) + k(9) + x(10);
ll = k(1) - x(2) - k(3) - k(4) - x(5) - x(6) - k(7) - x(8) - k(9) - x(10);

for i=1:N
    x(i).Input = {'Normal', xinMu(i), xinPre(i)};
end

fg.NumIterations = 11;
fg.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded version with a single factor for each sum

fg2 = FactorGraph();

x2 = Real(1,N);
aa2 = Real;
bb2 = Real;
cc2 = Real;
dd2 = Real;
ee2 = Real;
ff2 = Real;
gg2 = Real;
hh2 = Real;
ii2 = Real;
jj2 = Real;
kk2 = Real;
ll2 = Real;


fg2.addFactor('Sum', aa2, x2);
fg2.addFactor('Subtract', bb2, x2);
fg2.addFactor('Sum', cc2, k(1), k(2), k(3), x2(4:10));
fg2.addFactor('Subtract', dd2, k(1), k(2), k(3), x2(4:10));
fg2.addFactor('Sum', ee2, x2(1:8), k(9), k(10));
fg2.addFactor('Subtract', ff2, x2(1:8), k(9), k(10));
fg2.addFactor('Sum', gg2, x2(1), k(2), k(3), k(4), x2(5:10));
fg2.addFactor('Subtract', hh2, x2(1), k(2), k(3), k(4), x2(5:10));
fg2.addFactor('Sum', ii2, x2(1), k(2), x2(3), k(4), x2(5), k(6), x2(7), k(8), x2(9), k(10));
fg2.addFactor('Subtract', jj2, x2(1), k(2), x2(3), k(4), x2(5), k(6), x2(7), k(8), x2(9), k(10));
fg2.addFactor('Sum', kk2, k(1), x2(2), k(3), k(4), x2(5:6), k(7), x2(8), k(9), x2(10));
fg2.addFactor('Subtract', ll2, k(1), x2(2), k(3), k(4), x2(5:6), k(7), x2(8), k(9), x2(10));

assert(~isempty(strfind(aa2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(bb2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(cc2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(dd2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(ee2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ff2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(gg2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(hh2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(ii2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(jj2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));
assert(~isempty(strfind(kk2.Factors{1}.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(ll2.Factors{1}.Solver.toString, 'CustomGaussianSubtract')));

for i=1:N
    x2(i).Input = {'Normal', xinMu(i), xinPre(i)};
end

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertElementsAlmostEqual(x(1).Belief, x2(1).Belief);
assertElementsAlmostEqual(x(2).Belief, x2(2).Belief);
assertElementsAlmostEqual(x(3).Belief, x2(3).Belief);
assertElementsAlmostEqual(x(4).Belief, x2(4).Belief);
assertElementsAlmostEqual(x(5).Belief, x2(5).Belief);
assertElementsAlmostEqual(x(6).Belief, x2(6).Belief);
assertElementsAlmostEqual(x(7).Belief, x2(7).Belief);
assertElementsAlmostEqual(x(8).Belief, x2(8).Belief);
assertElementsAlmostEqual(x(9).Belief, x2(9).Belief);
assertElementsAlmostEqual(x(10).Belief, x2(10).Belief);
assertElementsAlmostEqual(aa.Belief, aa2.Belief);
assertElementsAlmostEqual(bb.Belief, bb2.Belief);
assertElementsAlmostEqual(cc.Belief, cc2.Belief);
assertElementsAlmostEqual(dd.Belief, dd2.Belief);
assertElementsAlmostEqual(ee.Belief, ee2.Belief);
assertElementsAlmostEqual(ff.Belief, ff2.Belief);
assertElementsAlmostEqual(gg.Belief, gg2.Belief);
assertElementsAlmostEqual(hh.Belief, hh2.Belief);
assertElementsAlmostEqual(ii.Belief, ii2.Belief);
assertElementsAlmostEqual(jj.Belief, jj2.Belief);
assertElementsAlmostEqual(kk.Belief, kk2.Belief);
assertElementsAlmostEqual(ll.Belief, ll2.Belief);

end



% RealJoint variables
function test5(debugPrint, repeatable)

dim = 7;
N = 10;

k = rand(dim,N);

xinMu = zeros(dim,N);
xinCo = zeros(dim,dim,N);
for i = 1:N
    xinMu(:,i) = rand(dim,1);
    xinCo(:,:,i) = diag(rand(dim,1)) + eye(dim)*0.1;
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Operator overloaded graph
fg = FactorGraph();

x = RealJoint(dim,1,N);

aa = x(1) + x(2) + x(3) + x(4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
bb = x(1) - x(2) - x(3) - x(4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
cc = k(:,1) + k(:,2) + k(:,3) + x(4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
dd = k(:,1) - k(:,2) - k(:,3) - x(4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
ee = x(1) + x(2) + x(3) + x(4) + x(5) + x(6) + x(7) + x(8) + k(:,9) + k(:,10);
ff = x(1) - x(2) - x(3) - x(4) - x(5) - x(6) - x(7) - x(8) - k(:,9) - k(:,10);
gg = x(1) + k(:,2) + k(:,3) + k(:,4) + x(5) + x(6) + x(7) + x(8) + x(9) + x(10);
hh = x(1) - k(:,2) - k(:,3) - k(:,4) - x(5) - x(6) - x(7) - x(8) - x(9) - x(10);
ii = x(1) + k(:,2) + x(3) + k(:,4) + x(5) + k(:,6) + x(7) + k(:,8) + x(9) + k(:,10);
jj = x(1) - k(:,2) - x(3) - k(:,4) - x(5) - k(:,6) - x(7) - k(:,8) - x(9) - k(:,10);
kk = k(:,1) + x(2) + k(:,3) + k(:,4) + x(5) + x(6) + k(:,7) + x(8) + k(:,9) + x(10);
ll = k(:,1) - x(2) - k(:,3) - k(:,4) - x(5) - x(6) - k(:,7) - x(8) - k(:,9) - x(10);

for i=1:N
    x(i).Input = {'MultivariateNormal', xinMu(:,i), xinCo(:,:,i)};
end

fg.NumIterations = 11;
fg.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded version

fg2 = FactorGraph();

x2 = RealJoint(dim,1,N);
aa2 = RealJoint(dim);
bb2 = RealJoint(dim);
cc2 = RealJoint(dim);
dd2 = RealJoint(dim);
ee2 = RealJoint(dim);
ff2 = RealJoint(dim);
gg2 = RealJoint(dim);
hh2 = RealJoint(dim);
ii2 = RealJoint(dim);
jj2 = RealJoint(dim);
kk2 = RealJoint(dim);
ll2 = RealJoint(dim);

% Can overload +/- operators with Gaussian custom factors
fg2.addFactor('RealJointSum', aa2, x2);
fg2.addFactor('RealJointSubtract', bb2, x2);
fg2.addFactor('RealJointSum', cc2, k(:,1), k(:,2), k(:,3), x2(4:10));
fg2.addFactor('RealJointSubtract', dd2, k(:,1), k(:,2), k(:,3), x2(4:10));
fg2.addFactor('RealJointSum', ee2, x2(1:8), k(:,9), k(:,10));
fg2.addFactor('RealJointSubtract', ff2, x2(1:8), k(:,9), k(:,10));
fg2.addFactor('RealJointSum', gg2, x2(1), k(:,2), k(:,3), k(:,4), x2(5:10));
fg2.addFactor('RealJointSubtract', hh2, x2(1), k(:,2), k(:,3), k(:,4), x2(5:10));
fg2.addFactor('RealJointSum', ii2, x2(1), k(:,2), x2(3), k(:,4), x2(5), k(:,6), x2(7), k(:,8), x2(9), k(:,10));
fg2.addFactor('RealJointSubtract', jj2, x2(1), k(:,2), x2(3), k(:,4), x2(5), k(:,6), x2(7), k(:,8), x2(9), k(:,10));
fg2.addFactor('RealJointSum', kk2, k(:,1), x2(2), k(:,3), k(:,4), x2(5:6), k(:,7), x2(8), k(:,9), x2(10));
fg2.addFactor('RealJointSubtract', ll2, k(:,1), x2(2), k(:,3), k(:,4), x2(5:6), k(:,7), x2(8), k(:,9), x2(10));

assert(~isempty(strfind(aa2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(bb2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(cc2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(dd2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(ee2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ff2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(gg2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(hh2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(ii2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(jj2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));
assert(~isempty(strfind(kk2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSum')));
assert(~isempty(strfind(ll2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianSubtract')));

for i=1:N
    x2(i).Input = {'MultivariateNormal', xinMu(:,i), xinCo(:,:,i)};
end

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
T = 1e-4;
assertElementsAlmostEqual(x(1).Belief.Mean, x2(1).Belief.Mean);
assertElementsAlmostEqual(x(2).Belief.Mean, x2(2).Belief.Mean);
assertElementsAlmostEqual(x(3).Belief.Mean, x2(3).Belief.Mean);
assertElementsAlmostEqual(x(4).Belief.Mean, x2(4).Belief.Mean);
assertElementsAlmostEqual(x(5).Belief.Mean, x2(5).Belief.Mean);
assertElementsAlmostEqual(x(6).Belief.Mean, x2(6).Belief.Mean);
assertElementsAlmostEqual(x(7).Belief.Mean, x2(7).Belief.Mean);
assertElementsAlmostEqual(x(8).Belief.Mean, x2(8).Belief.Mean);
assertElementsAlmostEqual(x(9).Belief.Mean, x2(9).Belief.Mean);
assertElementsAlmostEqual(x(10).Belief.Mean, x2(10).Belief.Mean);
assertElementsAlmostEqual(aa.Belief.Mean, aa2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(aa.Belief.Covariance, aa2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(bb.Belief.Mean, bb2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(bb.Belief.Covariance, bb2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(cc.Belief.Mean, cc2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(cc.Belief.Covariance, cc2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(dd.Belief.Mean, dd2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(dd.Belief.Covariance, dd2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(ee.Belief.Mean, ee2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(ee.Belief.Covariance, ee2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(ff.Belief.Mean, ff2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(ff.Belief.Covariance, ff2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(gg.Belief.Mean, gg2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(gg.Belief.Covariance, gg2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(hh.Belief.Mean, hh2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(hh.Belief.Covariance, hh2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(ii.Belief.Mean, ii2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(ii.Belief.Covariance, ii2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(jj.Belief.Mean, jj2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(jj.Belief.Covariance, jj2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(kk.Belief.Mean, kk2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(kk.Belief.Covariance, kk2.Belief.Covariance, 'absolute', T);
assertElementsAlmostEqual(ll.Belief.Mean, ll2.Belief.Mean, 'absolute', T);
assertElementsAlmostEqual(ll.Belief.Covariance, ll2.Belief.Covariance, 'absolute', T);

end


% Matrix vector product
function test6(debugPrint, repeatable)

Dx = 7;
Dy = 5;

xinMu = rand(Dx,1);
xinCo = diag(rand(Dx,1)) + eye(Dx)*0.1;


fg = FactorGraph();

x = RealJoint(Dx);
A = rand(Dy,Dx);
y = A * x;

assert(~isempty(strfind(y.Factors{1}.Solver.toString, 'CustomMultivariateGaussianProduct')));

x.Input = {'MultivariateNormal', xinMu, xinCo};

fg.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Now compare against non-overloaded

fg2 = FactorGraph();

x2 = RealJoint(Dx);
y2 = RealJoint(Dy);

fg2.addFactor('constmult', y2, A, x2);

assert(~isempty(strfind(y2.Factors{1}.Solver.toString, 'CustomMultivariateGaussianProduct')));

x2.Input = {'MultivariateNormal', xinMu, xinCo};

fg2.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
assertElementsAlmostEqual(x.Belief.Mean, x2.Belief.Mean);
assertElementsAlmostEqual(x.Belief.Covariance, x2.Belief.Covariance);
assertElementsAlmostEqual(y.Belief.Mean, y2.Belief.Mean);
assertElementsAlmostEqual(y.Belief.Covariance, y2.Belief.Covariance);

end

