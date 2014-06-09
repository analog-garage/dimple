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

function testBlockScheduleEntries()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testBlockScheduleEntries');

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

dtrace(debugPrint, '--testBlockScheduleEntries');

end



function test1(debugPrint, repeatable)

import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.test.solvers.gibbs.TrivialUniformBlockProposer;

table1 = zeros(2,2,2);
table1(:,:,1) = [2 5; 1 0];
table1(:,:,2) = [4 1; 3 7];

table2 = [1 2; 3 4];

fg = FactorGraph();
fg.Solver = 'SumProduct';
a = Bit;
b = Bit;
c = Bit;
d = Bit;

a.Input = 0.2;
fg.addFactor([0.7 0.3], b);   % For variety, do prior for b differently

fg.addFactor(table1, a, b, c);
fg.addFactor(table2, c, d);

fg.solve();
aB = a.Belief;
bB = b.Belief;
cB = c.Belief;
dB = d.Belief;


fg.Solver = 'Gibbs';
fg.Schedule = {{BlockMHSampler(TrivialUniformBlockProposer),a,b,c}, d};

if repeatable
    fg.Solver.setSeed(1);
end

fg.Solver.setNumSamples(20000);
fg.Solver.saveAllSamples();

fg.initialize();
assert(fg.Solver.getUpdatesPerSample() == 2);

fg.solve();

assertElementsAlmostEqual(aB/a.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(bB/b.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(cB/c.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(dB/d.Belief, 1, 'absolute', 0.04);

as = a.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();
cs = c.Solver.getAllSampleIndices();

assertEqual(nnz(arrayfun(@(a,b,c)a==1 && b==1 && c==0, as,bs,cs)), 0);

end




function test2(debugPrint, repeatable)

import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.test.solvers.gibbs.TrivialUniformBlockProposer;

table1 = zeros(2,2,2);
table1(:,:,1) = [2 5; 1 0];
table1(:,:,2) = [4 1; 3 7];

table2 = [1 2; 3 4];

fg = FactorGraph();
fg.Solver = 'SumProduct';
a = Bit;
b = Bit;
c = Bit;
d = Bit;

a.Input = 0.2;
fg.addFactor([0.7 0.3], b);   % For variety, do prior for b differently

fg.addFactor(table1, a, b, c);
fg.addFactor(table2, c, d);

% In this case, include a deterministic directed factor
e = ~d;
f = Bit;
fg.addFactor([1 3], f);
f.Input = 0.8;  % Both a factor and an input

fg.solve();
aB = a.Belief;
bB = b.Belief;
cB = c.Belief;
dB = d.Belief;
eB = e.Belief;
fB = f.Belief;


fg.Solver = 'Gibbs';
% Separately set the proposal this time
blockSampler1 = BlockMHSampler;
blockSampler1.setProposalKernel(TrivialUniformBlockProposer);
fg.Schedule = {a, {blockSampler1,b,c,d}, e, f};  % Block connects to deterministic directed factor

if repeatable
    fg.Solver.setSeed(2);
end

fg.Solver.setNumSamples(10000);
fg.Solver.saveAllSamples();
fg.solve();

assertElementsAlmostEqual(aB/a.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(bB/b.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(cB/c.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(dB/d.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(eB/e.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(fB/f.Belief, 1, 'absolute', 0.04);

as = a.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();
cs = c.Solver.getAllSampleIndices();

assertEqual(nnz(arrayfun(@(a,b,c)a==1 && b==1 && c==0, as,bs,cs)), 0);

end


function test3(debugPrint, repeatable)

import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.test.solvers.gibbs.TrivialNonuniformBlockProposer;

table1 = zeros(2,2,2);
table1(:,:,1) = [2 5; 1 0];
table1(:,:,2) = [4 1; 3 7];

table2 = [1 2; 3 4];

fg = FactorGraph();
fg.Solver = 'SumProduct';
a = Bit;
b = Bit;
c = Bit;
d = Bit;

a.Input = 0.2;
fg.addFactor([0.7 0.3], b);   % For variety, do prior for b differently

fg.addFactor(table1, a, b, c);
fg.addFactor(table2, c, d);

fg.solve();
aB = a.Belief;
bB = b.Belief;
cB = c.Belief;
dB = d.Belief;


fg.Solver = 'Gibbs';

% Use non-uniform proposal kernel with random weights
abcDomainSizes = [2, 2, 2];
abcWeights = rand(2,2,2) + 0.2;
abcWeightsFlat = abcWeights(:);
fg.Schedule = {{BlockMHSampler(TrivialNonuniformBlockProposer(abcWeightsFlat, abcDomainSizes)),a,b,c}, d};

if repeatable
    fg.Solver.setSeed(2);
end

fg.Solver.setNumSamples(10000);
fg.Solver.saveAllSamples();
fg.solve();

assertElementsAlmostEqual(aB/a.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(bB/b.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(cB/c.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(dB/d.Belief, 1, 'absolute', 0.04);

as = a.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();
cs = c.Solver.getAllSampleIndices();

assertEqual(nnz(arrayfun(@(a,b,c)a==1 && b==1 && c==0, as,bs,cs)), 0);

end



% Test with scheduler rather than custom schedule
function test4(debugPrint, repeatable)

import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.test.solvers.gibbs.TrivialUniformBlockProposer;

table1 = zeros(2,2,2);
table1(:,:,1) = [2 5; 1 0];
table1(:,:,2) = [4 1; 3 7];

table2 = [1 2; 3 4];

fg = FactorGraph();
fg.Solver = 'SumProduct';
a = Bit;
b = Bit;
c = Bit;
d = Bit;
x = Bit(1,4);

a.Input = 0.2;
fg.addFactor([0.7 0.3], b);   % For variety, do prior for b differently

fg.addFactor(table1, a, b, c);
fg.addFactor(table2, c, d);
fg.addFactor([1 2; 2 1], x(1), a);
fg.addFactor([1 2; 2 7], x(2), b);
fg.addFactor([1 2; 2 1], x(3), c);
fg.addFactor([1 2; 2 7], x(4), d);

fg.solve();
aB = a.Belief;
bB = b.Belief;
cB = c.Belief;
dB = d.Belief;
xB = x.Belief;


fg.Solver = 'Gibbs';

% Use uniform proposal kernel with default scheduler
fg.Scheduler.addBlockScheduleEntry(BlockMHSampler(TrivialUniformBlockProposer),x(2:3),c);
fg.Scheduler.addBlockScheduleEntry({BlockMHSampler(TrivialUniformBlockProposer),a,b});

if repeatable
    fg.Solver.setSeed(2);
end

fg.Solver.setNumSamples(10000);
fg.Solver.saveAllSamples();
fg.solve();

assertElementsAlmostEqual(aB/a.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(bB/b.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(cB/c.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(dB/d.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(xB/x.Belief, 1, 'absolute', 0.04);

as = a.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();
cs = c.Solver.getAllSampleIndices();

assertEqual(nnz(arrayfun(@(a,b,c)a==1 && b==1 && c==0, as,bs,cs)), 0);


% Use uniform proposal kernel with random scheduler
fg.Scheduler = 'GibbsRandomScanScheduler';
fg.Scheduler.addBlockScheduleEntries({BlockMHSampler(TrivialUniformBlockProposer),x(2:3),c}, ...
                                     {BlockMHSampler(TrivialUniformBlockProposer),a,b});

if repeatable
    fg.Solver.setSeed(2);
end

fg.Solver.setNumSamples(10000);
fg.Solver.saveAllSamples();
fg.solve();

assertElementsAlmostEqual(aB/a.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(bB/b.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(cB/c.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(dB/d.Belief, 1, 'absolute', 0.04);
assertElementsAlmostEqual(xB/x.Belief, 1, 'absolute', 0.04);

as = a.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();
cs = c.Solver.getAllSampleIndices();

assertEqual(nnz(arrayfun(@(a,b,c)a==1 && b==1 && c==0, as,bs,cs)), 0);

end
