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

function testBoundsAndInputs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testBoundsAndInputs');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);

dtrace(debugPrint, '--testBoundsAndInputs');

end

% Test bounds
function test1(debugPrint, repeatable)

fg = FactorGraph;
a = Real; a.Name = 'a';
b = Real([0 1]); b.Name = 'b';
fg.addFactor({'Normal',0,1}, a);
fg.addFactor('Negate',b,a);

fg.setOption('GibbsOptions.numSamples',100);
fg.setOption('GibbsOptions.saveAllSamples',true);

if (repeatable)
    fg.setOption('DimpleOptions.randomSeed',1);
end

fg.Solver = 'Gibbs';
fg.solve;
assert(all(b.Solver.getAllSamples >= 0));
assert(all(a.Solver.getAllSamples <= 0));
assert(all(b.Solver.getAllSamples <= 1));
assert(all(a.Solver.getAllSamples >= -1));

end

% Test Input
function test2(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';    % Bug requires this to be set first if non-Normal inputs
a = Real; a.Name = 'a';
b = Real; b.Name = 'b';
b.Input = {'Gamma',1,1};
fg.addFactor({'Normal',0,1}, a);
fg.addFactor('Negate',b,a);

fg.setOption('GibbsOptions.numSamples',100);
fg.setOption('GibbsOptions.saveAllSamples',true);

if (repeatable)
    fg.setOption('DimpleOptions.randomSeed',1);
end

fg.solve;
assert(all(b.Solver.getAllSamples >= 0));
assert(all(a.Solver.getAllSamples <= 0));

end

