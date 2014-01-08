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

function testSamplingCases()


debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testSamplingCases');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);


dtrace(debugPrint, '--testSamplingCases');

end

function test1(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(1000);
fg.Solver.setScansPerSample(1);

v = Discrete(0:9);
fg.addFactor(@myFactor1, v);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.saveAllSamples();
fg.solve();

if(debugPrint); disp(v.Solver.getAllSamples); end;

% Sampler should never sample from index 4, which has zero probability
assert(all(v.Solver.getAllSampleIndices ~= 4));

end

function y = myFactor1(x)
if (x == 4)
    y = 0;
else
    y = 1;
end
end


function test2(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(1000);
fg.Solver.setScansPerSample(1);

v = Discrete(0:9);
fg.addFactor(@myFactor2, v);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.saveAllSamples();
fg.solve();

if(debugPrint); disp(v.Solver.getAllSamples); end;

% Sampler should only sample from index 4, since all others have zero probability
assert(all(v.Solver.getAllSampleIndices == 4));

end

function y = myFactor2(x)
if (x == 4)
    y = 1;
else
    y = 0;
end
end


function test3(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(1000);
fg.Solver.setScansPerSample(1);

v = Discrete(0:9);
fg.addFactor(@myFactor3, v);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.saveAllSamples();
fg.solve();

if(debugPrint); disp(v.Solver.getAllSamples); end;

% Sampler should only sample from index 4, since the probability energy for
% all other values should round down to zero
assert(all(v.Solver.getAllSampleIndices == 4));

end

function y = myFactor3(x)
if (x == 4)
    y = realmax;
else
    y = 1;
end
end


function test4(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(1000);
fg.Solver.setScansPerSample(1);

v = Discrete(0:9);
fg.addFactor((0:9)', zeros(10,1), v);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

try
    fg.solve();
catch exception
    % Gibbs solver should throw an exception since all probabilities are zero
    assertTrue(~isempty(strfind(exception.message,'Energy value is NaN')));
end

end




function test5(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

v = Discrete(0:9);
fg.addFactor((0:9)', zeros(10,1), v);

% Directly check certain values of expApprox to make sure their correct
% expApprox is an approximate exponential function used by the sampler
assert(v.Solver.getSampler.expApprox(-Inf) == 0);
assert(v.Solver.getSampler.expApprox(-realmax) == 0);
assert(v.Solver.getSampler.expApprox(-log(realmax)) == 0);
assert(v.Solver.getSampler.expApprox(0) == 1);

end



