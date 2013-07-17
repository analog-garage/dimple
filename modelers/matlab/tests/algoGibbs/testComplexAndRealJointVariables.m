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

function testComplexAndRealJointVariables()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testOperatorOverloadingGibbs');

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testOperatorOverloadingGibbs');

end

function test1(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Complex();
b = Complex();
c = Complex();
a.Name = 'a';
b.Name = 'b';
c.Name = 'c';

fg.addFactor('CSum', c, a, b);

a.Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
b.Input = {FactorFunction('Normal',4,1), FactorFunction('Normal',6,2)};

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

cs = c.Solver.getAllSamples;

assertElementsAlmostEqual(mean(cs), [7,5], 'absolute', 0.05);


end


