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

function testTreeScheduler()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testTreeScheduler');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);

dtrace(debugPrint, '--testTreeScheduler');

end



% Non-nested graph
function test1(debugPrint, repeatable)

% Create model
fg = FactorGraph();
fg.Solver = 'SumProduct';

a = Bit;
b = Bit;
c = Bit;
d = Bit;
e = Bit;
f = Bit;
g = Bit;
h = Bit;
m = -a;
n = ~b;
o = Discrete(-1:3);
fg.addFactor('Sum', o, m, n, c, d);
p = -o;
q = p - e + (-(~f));
r = -q;
s = r * g + (~h);

if (debugPrint)
    fg.plot();
end

a.Input = rand;
b.Input = rand;
c.Input = rand;
d.Input = rand;
e.Input = rand;
f.Input = rand;
g.Input = rand;
h.Input = rand;


fg.Scheduler = 'TreeOrFloodingScheduler';
fg.NumIterations = 1;   % Make sure
fg.solve();

ba = a.Belief;
bb = b.Belief;
bc = c.Belief;
bd = d.Belief;
be = e.Belief;
bf = f.Belief;
bg = g.Belief;
bh = h.Belief;
bm = m.Belief;
bn = n.Belief;
bo = o.Belief;
bp = p.Belief;
bq = q.Belief;
br = r.Belief;
bs = s.Belief;

% Verify that the beliefs are the same as a flooding schedule with many iterations
fg.Scheduler = 'FloodingScheduler';
fg.NumIterations = 50;    % More than enough iterations for flooding schedule
fg.solve();

assertElementsAlmostEqual(ba, a.Belief);
assertElementsAlmostEqual(bb, b.Belief);
assertElementsAlmostEqual(bc, c.Belief);
assertElementsAlmostEqual(bd, d.Belief);
assertElementsAlmostEqual(be, e.Belief);
assertElementsAlmostEqual(bf, f.Belief);
assertElementsAlmostEqual(bg, g.Belief);
assertElementsAlmostEqual(bh, h.Belief);
assertElementsAlmostEqual(bm, m.Belief);
assertElementsAlmostEqual(bn, n.Belief);
assertElementsAlmostEqual(bo, o.Belief);
assertElementsAlmostEqual(bp, p.Belief);
assertElementsAlmostEqual(bq, q.Belief);
assertElementsAlmostEqual(br, r.Belief);
assertElementsAlmostEqual(bs, s.Belief);

end





% Nested graphs (doubly-nested)
function test2(debugPrint, repeatable)

% Create model
fg = FactorGraph();
fg.Solver = 'SumProduct';

sg = FactorGraph();

ssg = FactorGraph();

a = Bit;
b = Bit;
c = Bit;
d = Bit;
e = Bit;
f = Bit;
g = Bit;
h = Bit;
m = -a;
n = ~b;
o = Discrete(-1:3);
ssg.addFactor('Sum', o, m, n, c, d);
p = -o;
q = p - e + (-(~f));
r = -q;
s = r * g + (~h);

ssg.addBoundaryVariables(a, b, c, d, e, f, g, h, m, n, o, p, q, r, s);

sa1 = Bit;
sb1 = Bit;
sc1 = Bit;
sd1 = Bit;
se1 = Bit;
sf1 = Bit;
sg1 = Bit;
sh1 = Bit;
sm1 = Discrete(-1:0);
sn1 = Bit;
so1 = Discrete(-1:3);
sp1 = Discrete(-3:1);
sq1 = Discrete(-5:1);
sr1 = Discrete(-1:5);
ss1 = Discrete(-1:6);

sa2 = Bit;
sb2 = Bit;
sc2 = Bit;
sd2 = Bit;
se2 = Bit;
sf2 = Bit;
sg2 = Bit;
sh2 = Bit;
sm2 = Discrete(-1:0);
sn2 = Bit;
so2 = Discrete(-1:3);
sp2 = Discrete(-3:1);
sq2 = Discrete(-5:1);
sr2 = Discrete(-1:5);
ss2 = Discrete(-1:6);

% Add two copies of sub-sub-graph
sg.addFactor(ssg, sa1, sb1, sc1, sd1, se1, sf1, sg1, sh1, sm1, sn1, so1, sp1, sq1, sr1, ss1);
sg.addFactor(ssg, sa2, sb2, sc2, sd2, se2, sf2, sg2, sh2, sm2, sn2, so2, sp2, sq2, sr2, ss2);

sg.addBoundaryVariables(sa1, sb1, sc1, sd1, se1, sf1, sg1, sh1, sm1, sn1, so1, sp1, sq1, sr1, ss1);
sg.addBoundaryVariables(sa2, sb2, sc2, sd2, se2, sf2, sg2, sh2, sm2, sn2, so2, sp2, sq2, sr2, ss2);

fa1 = Bit;
fb1 = Bit;
fc1 = Bit;
fd1 = Bit;
fe1 = Bit;
ff1 = Bit;
fg1 = Bit;
fh1 = Bit;
fm1 = Discrete(-1:0);
fn1 = Bit;
fo1 = Discrete(-1:3);
fp1 = Discrete(-3:1);
fq1 = Discrete(-5:1);
fr1 = Discrete(-1:5);
fs1 = Discrete(-1:6);

fa2 = Bit;
fb2 = Bit;
fc2 = Bit;
fd2 = Bit;
fe2 = Bit;
ff2 = Bit;
fg2 = Bit;
fh2 = Bit;
fm2 = Discrete(-1:0);
fn2 = Bit;
fo2 = Discrete(-1:3);
fp2 = Discrete(-3:1);
fq2 = Discrete(-5:1);
fr2 = Discrete(-1:5);
fs2 = Discrete(-1:6);

% One copy of sub-graph
fg.addFactor(sg, fa1, fb1, fc1, fd1, fe1, ff1, fg1, fh1, fm1, fn1, fo1, fp1, fq1, fr1, fs1, ...
                 fa2, fb2, fc2, fd2, fe2, ff2, fg2, fh2, fm2, fn2, fo2, fp2, fq2, fr2, fs2);

% Plus one additional factor in the outer-most graph
setFactorGraph(fg); % Change the default graph back to fg
x = fs1 + fs2;

if (debugPrint)
    fg.plot();
end


fa1.Input = rand;
fb1.Input = rand;
fc1.Input = rand;
fd1.Input = rand;
fe1.Input = rand;
ff1.Input = rand;
fg1.Input = rand;
fh1.Input = rand;

fa2.Input = rand;
fb2.Input = rand;
fc2.Input = rand;
fd2.Input = rand;
fe2.Input = rand;
ff2.Input = rand;
fg2.Input = rand;
fh2.Input = rand;



fg.Scheduler = 'TreeOrFloodingScheduler';
fg.NumIterations = 1;   % Make sure
fg.solve();

ba1 = fa1.Belief;
bb1 = fb1.Belief;
bc1 = fc1.Belief;
bd1 = fd1.Belief;
be1 = fe1.Belief;
bf1 = ff1.Belief;
bg1 = fg1.Belief;
bh1 = fh1.Belief;
bm1 = fm1.Belief;
bn1 = fn1.Belief;
bo1 = fo1.Belief;
bp1 = fp1.Belief;
bq1 = fq1.Belief;
br1 = fr1.Belief;
bs1 = fs1.Belief;

ba2 = fa2.Belief;
bb2 = fb2.Belief;
bc2 = fc2.Belief;
bd2 = fd2.Belief;
be2 = fe2.Belief;
bf2 = ff2.Belief;
bg2 = fg2.Belief;
bh2 = fh2.Belief;
bm2 = fm2.Belief;
bn2 = fn2.Belief;
bo2 = fo2.Belief;
bp2 = fp2.Belief;
bq2 = fq2.Belief;
br2 = fr2.Belief;
bs2 = fs2.Belief;

bx = x.Belief;

% Verify that the beliefs are the same as a flooding schedule with many iterations
fg.Scheduler = 'FloodingScheduler';
fg.NumIterations = 50;    % More than enough iterations for flooding schedule
fg.solve();

assertElementsAlmostEqual(ba1, fa1.Belief);
assertElementsAlmostEqual(bb1, fb1.Belief);
assertElementsAlmostEqual(bc1, fc1.Belief);
assertElementsAlmostEqual(bd1, fd1.Belief);
assertElementsAlmostEqual(be1, fe1.Belief);
assertElementsAlmostEqual(bf1, ff1.Belief);
assertElementsAlmostEqual(bg1, fg1.Belief);
assertElementsAlmostEqual(bh1, fh1.Belief);
assertElementsAlmostEqual(bm1, fm1.Belief);
assertElementsAlmostEqual(bn1, fn1.Belief);
assertElementsAlmostEqual(bo1, fo1.Belief);
assertElementsAlmostEqual(bp1, fp1.Belief);
assertElementsAlmostEqual(bq1, fq1.Belief);
assertElementsAlmostEqual(br1, fr1.Belief);
assertElementsAlmostEqual(bs1, fs1.Belief);

assertElementsAlmostEqual(ba2, fa2.Belief);
assertElementsAlmostEqual(bb2, fb2.Belief);
assertElementsAlmostEqual(bc2, fc2.Belief);
assertElementsAlmostEqual(bd2, fd2.Belief);
assertElementsAlmostEqual(be2, fe2.Belief);
assertElementsAlmostEqual(bf2, ff2.Belief);
assertElementsAlmostEqual(bg2, fg2.Belief);
assertElementsAlmostEqual(bh2, fh2.Belief);
assertElementsAlmostEqual(bm2, fm2.Belief);
assertElementsAlmostEqual(bn2, fn2.Belief);
assertElementsAlmostEqual(bo2, fo2.Belief);
assertElementsAlmostEqual(bp2, fp2.Belief);
assertElementsAlmostEqual(bq2, fq2.Belief);
assertElementsAlmostEqual(br2, fr2.Belief);
assertElementsAlmostEqual(bs2, fs2.Belief);

assertElementsAlmostEqual(bx, x.Belief);


end


