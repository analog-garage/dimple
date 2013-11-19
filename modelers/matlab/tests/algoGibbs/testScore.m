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

function testScore()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testScore');

if (repeatable)
    seed = 14;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testScore');

end

% Test Guess and Score for various variable types
% Bit, Discrete, Real, Complex, and RealJoint variables and arrays
function test1(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Bit;
aa = Bit(2,3);
b = Discrete(0:9);
ba = Discrete(0:9,2,3);
c = Real;
ca = Real(2,3);
d = Complex;
da = Complex(2,3);
e = RealJoint(4);
ea = RealJoint(4,2,1);

a.Input = rand;
aa.Input = rand(2,3);
b.Input = rand(10,1);
ba.Input = rand(2,3,10);
c.Input = FactorFunction('Normal',0,1);
ca.Input = FactorFunction('Normal',0,1);
d.Input = {FactorFunction('Normal',0,1), FactorFunction('Normal',1,2)};
da.Input = {FactorFunction('Normal',0,1), FactorFunction('Normal',1,2)};
e.Input = {FactorFunction('Normal',0,1), FactorFunction('Normal',1,2), FactorFunction('Normal',3,4), FactorFunction('Normal',5,6)};
ea.Input = {FactorFunction('Normal',0,1), FactorFunction('Normal',1,2), FactorFunction('Normal',3,4), FactorFunction('Normal',5,6)};

f1 = fg.addFactor({'ComplexSum', 1}, [31,15], a, aa, b, ba, c, ca, d, da);
f2 = fg.addFactor({'VectorInnerProduct', 1}, 5, e, ea(1));
f3 = fg.addFactor({'VectorInnerProduct', 1}, -4, e, ea(2));

fg.initialize();

a.Guess = 1;
aa.Guess = [1 0 1; 0 1 0];
b.Guess = 4;
ba.Guess = [2 7 1; 0 1 9];
c.Guess = 3.33;
ca.Guess = [2.4 7.1 1; -8.2 1.73 -.01];
d.Guess = 3.33 + 0.2*1i;
da.Guess = [2.4+3*1i 7.1*1i -1; -8.2+3.2*1i 1.73*1i -.01];
e.Guess = [4.6 7.9 9.1 -21.1];
ea.Guess = rand(2,4);

% Score for factor 1
scoref1 = f1.Score;
sm = cell2mat(a.Guess) + sum(sum(cell2mat(aa.Guess))) + ...
     cell2mat(b.Guess) + sum(sum(cell2mat(ba.Guess))) + ...
     cell2mat(c.Guess) + sum(sum(cell2mat(ca.Guess))) + ...
     cell2mat(d.Guess) + sum(sum(cell2mat(da.Guess)));
diff1 = [31 15] - [real(sm) imag(sm)];
expectedScoref1 = diff1 * diff1';
assertElementsAlmostEqual(scoref1, expectedScoref1);

% Score for factor 2
scoref2 = f2.Score;
p2 = cell2mat(e.Guess)' * cell2mat(ea(1).Guess);
diff2 = 5 - p2;
expectedScoref2 = diff2 * diff2;
assertElementsAlmostEqual(scoref2, expectedScoref2);

% Score for factor 3
scoref3 = f3.Score;
p3 = cell2mat(e.Guess)' * cell2mat(ea(2).Guess);
diff3 = (-4) - p3;
expectedScoref3 = diff3 * diff3;
assertElementsAlmostEqual(scoref3, expectedScoref3);

% Check that graph score equals the total of all factor and variable scores
fgScore = fg.Score;
totalScore = scoref1 + scoref2 + scoref3 + ...
    a.Score + aa.Score + b.Score + ba.Score + c.Score + ca.Score + ...
    d.Score + da.Score + e.Score + ea.Score;
assertElementsAlmostEqual(fgScore, totalScore);
 
end


