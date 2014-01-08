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
a.Solver.getSampler.getProposalKernel.setStandardDeviation(2); % Proposal std-dev
assert(~isempty(strcmp(a.Solver.getSampler.getProposalKernelName, 'NormalPropoalKernel')));
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getStandardDeviation, 2);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) > pi-.01);
assert(max(as) <= pi);
assert(min(as) < -pi+.01);
assert(min(as) >= -pi);

% Try with circular kernel, by default, bounds are +/-pi

a.Solver.setProposalKernel('CircularNormalProposalKernel');
a.Solver.getSampler.getProposalKernel.setStandardDeviation(1.5); % Proposal std-dev
assert(~isempty(strcmp(a.Solver.getSampler.getProposalKernelName, 'CircularNormalProposalKernel')));
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getStandardDeviation, 1.5);
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getLowerBound, -pi);
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getUpperBound, pi);

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) > pi-.01);
assert(max(as) <= pi);
assert(min(as) < -pi+.01);
assert(min(as) >= -pi);



% Try with circular kernel with different bounds, values should never stray
% past +/-pi/2

a.Solver.setProposalKernel('CircularNormalProposalKernel');
a.Solver.getSampler.getProposalKernel.setStandardDeviation(1);
a.Solver.getSampler.getProposalKernel.setCircularBounds(-pi/2, pi/2);
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getStandardDeviation, 1);
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getLowerBound, -pi/2);
assertElementsAlmostEqual(a.Solver.getSampler.getProposalKernel.getUpperBound, pi/2);

fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples;
assert(max(as) > pi/2-.01);
assert(max(as) <= pi/2);
assert(min(as) < -pi/2+.01);
assert(min(as) >= -pi/2);


end


