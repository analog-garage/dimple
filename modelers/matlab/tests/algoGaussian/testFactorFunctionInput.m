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

function testFactorFunctionInput()

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real();
b = Real();
c = Real();
d = Real(1,2);
e = Real;
f = Real(1,2);

amean = 2;
bmean = 3;
cmean = 4;
dmean = 5;
emean = 6;
fmean = 7;
astd = 1.5;
bstd = 2.5;
cstd = 3.5;
dstd = 4.5;
estd = 5.5;
fstd = 6.5;
aprecision = 1/astd^2;
bprecision = 1/bstd^2;
dprecision = 1/dstd^2;
eprecision = 1/estd^2;
fprecision = 1/fstd^2;

% Try various ways to set the Inputs
a.Input = FactorFunction('Normal', amean, aprecision);  % Input as factor function
b.Input = {'Normal', bmean, bprecision};                % Alternative syntax
c.Input = [cmean, cstd];                                % Input as [mean, std] array
d.Input = FactorFunction('Normal', dmean, dprecision);  % Input to whole array
e.Input = FactorFunction('Normal', emean, eprecision);
f(1).Input = FactorFunction('Normal', fmean, fprecision);
f(2).Input = FactorFunction('Normal', fmean, fprecision);

z = a + b + c + d(1) + d(2) + e + f(1) + f(2);

fg.solve;

zb = z.Belief;

zExpectedMean = amean + bmean + cmean + 2*dmean + emean + 2*fmean;
zExpectedStd = sqrt(astd^2 + bstd^2 + cstd^2 + 2*(dstd^2) + estd^2 + 2*(fstd^2));

assertElementsAlmostEqual(zb, [zExpectedMean; zExpectedStd]);

assertElementsAlmostEqual(amean, a.Input.getMean);
assertElementsAlmostEqual(aprecision, a.Input.getPrecision);
assertElementsAlmostEqual(bmean, b.Input.getMean);
assertElementsAlmostEqual(bprecision, b.Input.getPrecision);
assertElementsAlmostEqual(cmean, c.Input(1));
assertElementsAlmostEqual(cstd, c.Input(2));
assertElementsAlmostEqual(dmean, d.Input{1}.getMean);
assertElementsAlmostEqual(dprecision, d.Input{1}.getPrecision);
assertElementsAlmostEqual(dmean, d.Input{2}.getMean);
assertElementsAlmostEqual(dprecision, d.Input{2}.getPrecision);
assertElementsAlmostEqual(emean, e.Input.getMean);
assertElementsAlmostEqual(eprecision, e.Input.getPrecision);
assertElementsAlmostEqual(fmean, f.Input{1}.getMean);
assertElementsAlmostEqual(fprecision, f.Input{1}.getPrecision);
assertElementsAlmostEqual(fmean, f.Input{2}.getMean);
assertElementsAlmostEqual(fprecision, f.Input{2}.getPrecision);

end

