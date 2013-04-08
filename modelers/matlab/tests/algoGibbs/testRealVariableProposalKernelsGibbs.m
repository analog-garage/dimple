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

function testRealVariableProposalKernelsGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRealVariableProposalKernelsGibbs');

test1(debugPrint,repeatable);

dtrace(debugPrint, '--testRealVariableProposalKernelsGibbs');

end

function test1(debugPrint,repeatable)


% Test 1 - replace kernel with circular kernel

numSamples = 10000;
updatesPerSample = 10;
burnInUpdates = 1000;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setUpdatesPerSample(updatesPerSample);
fg.Solver.setBurnInUpdates(burnInUpdates);

a = Real();
fg.addFactor({'VonMises',0,1}, a);

a.Solver.setSampler('MHSampler');           % Test MH Sampler
a.Solver.getSampler.getProposalKernel.setParameters(1); % Proposal std-dev

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) > pi);
assert(min(as) < -pi);

% Try with circular kernel, values should never stray past +/-pi

a.Solver.setProposalKernel('CircularNormalProposalKernel');
a.Solver.getSampler.getProposalKernel.setParameters(1); % Proposal std-dev

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) <= pi);
assert(min(as) >= -pi);



% Try with circular kernel with different bounds, values should never stray past +/-pi

a.Solver.setProposalKernel('CircularNormalProposalKernel');
a.Solver.setProposalKernelParameters({1, -14, -2});

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) <= -2);
assert(min(as) >= -14);


end


