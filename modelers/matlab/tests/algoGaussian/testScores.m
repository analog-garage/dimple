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

function testScores()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testScores');

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

dtrace(debugPrint, '--testScores');

end

% Real variables
function test1(debugPrint)

fg = FactorGraph();

a = Real();
b = Real();
c = Real();
f = fg.addFactor('Sum', c, a, b);

a.Input = [3, 1];
b.Input = [2, 2];

fg.solve();

s = fg.Score;
as = a.Score;
bs = b.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + bs + cs + fs);
assert(~isinf(s));

a.Guess = 4;
b.Guess = 5;
c.Guess = 9;

s = fg.Score;
as = a.Score;
bs = b.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + bs + cs + fs);
assert(~isinf(s));

end



% RealJoint variables
function test2(debugPrint)

fg = FactorGraph();

a = Complex();
b = Complex();
c = Complex();
f = fg.addFactor('ComplexSum', c, a, b);

a.Input = MultivariateNormalParameters([1 2], eye(2));
b.Input = MultivariateNormalParameters([3 4], eye(2)*2);

fg.solve();

s = fg.Score;
as = a.Score;
bs = b.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + bs + cs + fs);
% In this case, fs is inf since solver is not exact
% assert(~isinf(s));

a.Guess = 4 + 2*1i;
b.Guess = 3 + 4*1i;
c.Guess = 7 + 6*1i;

s = fg.Score;
as = a.Score;
bs = b.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + bs + cs + fs);
assert(~isinf(s));

end


% Real variables, specified with factor functions rather than inputs
function test3(debugPrint)

fg = FactorGraph();

a = Normal(3, 1);
b = Normal(2, 0.25);
c = Real();
f = fg.addFactor('Sum', c, a, b);

fg.solve();

s = fg.Score;
as = a.Score;
afs = a.Factors{1}.Score;
bs = b.Score;
bfs = b.Factors{1}.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + afs + bs + bfs + cs + fs);
assert(~isinf(s));

a.Guess = 4;
b.Guess = 5;
c.Guess = 9;

s = fg.Score;
as = a.Score;
afs = a.Factors{1}.Score;
bs = b.Score;
bfs = b.Factors{1}.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + afs + bs + bfs + cs + fs);
assert(~isinf(s));

end


% RealJoint variables, specified with factor functions rather than inputs
function test4(debugPrint)

fg = FactorGraph();

a = Complex();
b = Complex();
c = Complex();
af = fg.addFactor(FactorFunction('MultivariateNormal', [1 2], eye(2)), a);
bf = fg.addFactor(FactorFunction('MultivariateNormal', [3 4], eye(2)*0.25), a);
f = fg.addFactor('ComplexSum', c, a, b);

fg.solve();

s = fg.Score;
as = a.Score;
afs = af.Score;
bs = b.Score;
bfs = bf.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + afs + bs + bfs + cs + fs);
% In this case, fs is inf since solver is not exact
% assert(~isinf(s));

a.Guess = 4 + 2*1i;
b.Guess = 3 + 4*1i;
c.Guess = 7 + 6*1i;

s = fg.Score;
as = a.Score;
afs = af.Score;
bs = b.Score;
bfs = bf.Score;
cs = c.Score;
fs = f.Score;

assertElementsAlmostEqual(s, as + afs + bs + bfs + cs + fs);
assert(~isinf(s));

end


