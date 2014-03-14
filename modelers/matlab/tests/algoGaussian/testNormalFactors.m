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

function testNormalFactors()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testNormalFactors');

if (repeatable)
    seed = 14;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint);
test2(debugPrint);
test3(debugPrint);
test4(debugPrint);

dtrace(debugPrint, '--testNormalFactors');

end

% Real variables
function test1(debugPrint)

fg = FactorGraph();

m = 2;
p = 7;

a = Real;
b = Real;
c = Real;
a.Input = {'Normal', m, p};
fb = fg.addFactor({'Normal', m, p}, b);
fc = fg.addFactor('Normal', m, p, c);

d = a + (b + c);  % To get 'a' in the graph

fg.solve();

assert(~isempty(strfind(class(fb.VectorObject.getFactorFunction.getContainedFactorFunction), 'Normal')));
assert(~isempty(strfind(class(fc.VectorObject.getFactorFunction.getContainedFactorFunction), 'Normal')));
assert(~isempty(strfind(class(fb.Solver), 'CustomNormalConstantParameters')));
assert(~isempty(strfind(class(fc.Solver), 'CustomNormalConstantParameters')));

assertElementsAlmostEqual(a.Belief.Mean, m);
assertElementsAlmostEqual(b.Belief.Mean, m);
assertElementsAlmostEqual(c.Belief.Mean, m);
assertElementsAlmostEqual(d.Belief.Mean, 3*m);

end


% Real-joint variables
function test2(debugPrint)

fg = FactorGraph();

m = 2+3i;
mv = [real(m); imag(m)];
v = eye(2)*.3;

a = Complex;
b = Complex;
c = Complex;
a.Input = {'MultivariateNormal', mv, v};
fb = fg.addFactor({'MultivariateNormal', mv, v}, b);

d = a + b;  % To get 'a' in the graph

fg.solve();

assert(~isempty(strfind(class(fb.VectorObject.getFactorFunction.getContainedFactorFunction), 'MultivariateNormal')));
assert(~isempty(strfind(class(fb.Solver), 'CustomMultivariateNormalConstantParameters')));

assertElementsAlmostEqual(a.Belief.Mean, mv, 'absolute', 1e-6);
assertElementsAlmostEqual(b.Belief.Mean, mv, 'absolute', 1e-6);
assertElementsAlmostEqual(d.Belief.Mean, 2.*mv, 'absolute', 1e-6);

end



% Real variable arrays from the same factor
function test3(debugPrint)

fg = FactorGraph();

N = 4;

m = 2;
p = 7;

a = Real(1,N);
b = Real(1,N);
c = Real(1,N);
a.Input = {'Normal', m, p};
fb = fg.addFactor({'Normal', m, p}, b);
fc = fg.addFactor('Normal', m, p, c);

d = a + (b + c);  % To get 'a' in the graph

fg.NumIterations = 2;  % This is loopy
fg.solve();

assert(~isempty(strfind(class(fb.VectorObject.getFactorFunction.getContainedFactorFunction), 'Normal')));
assert(~isempty(strfind(class(fc.VectorObject.getFactorFunction.getContainedFactorFunction), 'Normal')));
assert(~isempty(strfind(class(fb.Solver), 'CustomNormalConstantParameters')));
assert(~isempty(strfind(class(fc.Solver), 'CustomNormalConstantParameters')));

for i=1:N
    assertElementsAlmostEqual(a(i).Belief.Mean, m);
    assertElementsAlmostEqual(b(i).Belief.Mean, m);
    assertElementsAlmostEqual(c(i).Belief.Mean, m);
    assertElementsAlmostEqual(d(i).Belief.Mean, 3*m);
end

end


% Real-joint variable arrays from the same factor
function test4(debugPrint)

fg = FactorGraph();

N = 4;

m = 2+3i;
mv = [real(m); imag(m)];
v = eye(2)*.3;

a = Complex(1,N);
b = Complex(1,N);
c = Complex(1,N);
a.Input = {'MultivariateNormal', mv, v};
fb = fg.addFactor({'MultivariateNormal', mv, v}, b);

d = a + b;  % To get 'a' in the graph

fg.NumIterations = 2;  % This is loopy
fg.solve();

assert(~isempty(strfind(class(fb.VectorObject.getFactorFunction.getContainedFactorFunction), 'MultivariateNormal')));
assert(~isempty(strfind(class(fb.Solver), 'CustomMultivariateNormalConstantParameters')));

for i=1:N
    assertElementsAlmostEqual(a(i).Belief.Mean, mv, 'absolute', 1e-6);
    assertElementsAlmostEqual(b(i).Belief.Mean, mv, 'absolute', 1e-6);
    assertElementsAlmostEqual(d(i).Belief.Mean, 2.*mv, 'absolute', 1e-6);
end

end


