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

function testOperatorOverloadingGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testOperatorOverloadingGibbs');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);
test6(debugPrint, repeatable);

dtrace(debugPrint, '--testOperatorOverloadingGibbs');

end


function test1(debugPrint, repeatable)

numSamples = 1000;
scansPerSample = 50;
burnInScans = 10;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);


a1 = Real();
b1 = Real();

a1.Input = FactorFunction('Normal',3,1/12^2);       % Use FactorFunction
b1.Input = {'Normal',7,1/17^2};                     % Use cell notation


z1 = a1 + b1;

if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end

graph1.Solver.saveAllSamples();
graph1.solve();


graph2 = FactorGraph();
graph2.Solver = 'Gibbs';
graph2.Solver.setNumSamples(numSamples);
graph2.Solver.setScansPerSample(scansPerSample);
graph2.Solver.setBurnInScans(burnInScans);

a2 = Real();
b2 = Real();
z2 = Real();

a2.Input = FactorFunction('Normal',3,1/12^2);       % Use FactorFunction
b2.Input = {'Normal',7,1/17^2};                     % Use cell notation

graph2.addFactor('Sum',z2,a2,b2);

if (repeatable)
    graph2.Solver.setSeed(1);					% Make this repeatable
end
graph2.Solver.saveAllSamples();
graph2.solve();

z1Samples = z1.Solver.getAllSamples();
z2Samples = z2.Solver.getAllSamples();

assertElementsAlmostEqual(z1Samples, z2Samples);

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
assertElementsAlmostEqual(a1Samples + b1Samples, z1Samples);

end




function test2(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

w1 = Real();
x1 = Real([-pi pi]);
y1 = Real();
z1 = Real([-.99 .99]);
cc = Complex();

w1.Input = FactorFunction('Gamma',1,1);
y1.Input = {'Normal',0,1/10^2};
cc.Input = {FactorFunction('Normal',0,1/10^2), FactorFunction('Normal',0,1/10^2)};

a1 = abs(z1);
b1 = exp(y1);
c1 = log(w1);
d1 = sqrt(w1);
e1 = sin(x1);
f1 = cos(x1);
g1 = tan(x1);
h1 = asin(z1);
i1 = acos(z1);
j1 = atan(z1);
k1 = sinh(y1);
l1 = cosh(y1);
m1 = tanh(y1);
o1 = abs(cc);
n1 = a1 + b1 + c1 + d1 + e1 + f1 + g1 + h1 + i1 + j1 + k1 + l1 + m1 + o1;


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
c1Samples = c1.Solver.getAllSamples();
d1Samples = d1.Solver.getAllSamples();
e1Samples = e1.Solver.getAllSamples();
f1Samples = f1.Solver.getAllSamples();
g1Samples = g1.Solver.getAllSamples();
h1Samples = h1.Solver.getAllSamples();
i1Samples = i1.Solver.getAllSamples();
j1Samples = j1.Solver.getAllSamples();
k1Samples = k1.Solver.getAllSamples();
l1Samples = l1.Solver.getAllSamples();
m1Samples = m1.Solver.getAllSamples();
n1Samples = n1.Solver.getAllSamples();
o1Samples = o1.Solver.getAllSamples();

w1Samples = w1.Solver.getAllSamples();
x1Samples = x1.Solver.getAllSamples();
y1Samples = y1.Solver.getAllSamples();
z1Samples = z1.Solver.getAllSamples();
ccSamples = cc.Solver.getAllSamples();
ccSamplesR = ccSamples(:,1);
ccSamplesI = ccSamples(:,2);

assert(all(w1Samples ~= 0));
assert(all(x1Samples ~= 0));
assert(all(y1Samples ~= 0));
assert(all(z1Samples ~= 0));
assert(all(ccSamplesR ~= 0));
assert(all(ccSamplesI ~= 0));

assertElementsAlmostEqual(a1Samples, abs(z1Samples), 'absolute');
assertElementsAlmostEqual(b1Samples, exp(y1Samples), 'absolute');
assertElementsAlmostEqual(c1Samples, log(w1Samples), 'absolute');
assertElementsAlmostEqual(d1Samples, sqrt(w1Samples), 'absolute');
assertElementsAlmostEqual(e1Samples, sin(x1Samples), 'absolute');
assertElementsAlmostEqual(f1Samples, cos(x1Samples), 'absolute');
assertElementsAlmostEqual(g1Samples, tan(x1Samples), 'absolute');
assertElementsAlmostEqual(h1Samples, asin(z1Samples), 'absolute');
assertElementsAlmostEqual(i1Samples, acos(z1Samples), 'absolute');
assertElementsAlmostEqual(j1Samples, atan(z1Samples), 'absolute');
assertElementsAlmostEqual(k1Samples, sinh(y1Samples), 'absolute');
assertElementsAlmostEqual(l1Samples, cosh(y1Samples), 'absolute');
assertElementsAlmostEqual(m1Samples, tanh(y1Samples), 'absolute');
assertElementsAlmostEqual(n1Samples, a1Samples + b1Samples + c1Samples + d1Samples + e1Samples + f1Samples + g1Samples + h1Samples + i1Samples + j1Samples + k1Samples + l1Samples + m1Samples + o1Samples, 'absolute');
assertElementsAlmostEqual(o1Samples, sqrt(ccSamplesR.^2 + ccSamplesI.^2), 'absolute');


end




function test3(debugPrint, repeatable)

numSamples = 20;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

N = 4;
a = Discrete(1:5);
b = Discrete(1:10, 1, N);
c = Real([0 1]);
d = Real([0 1], 1, N);
M = 3;
bt = Discrete(1:10, N, 1);
dt = Real([0 1], N, 1);
A = Discrete(1:5, M, N);
B = Real([0 1], M, N);
P = 5;
C = Discrete(1:5, N, P);
D = Real([0 1], N, P);
rj1 = RealJoint(N);
rj2 = RealJoint(N);
a.setNames('a');
b.setNames('b');
c.setNames('c');
d.setNames('d');

e = b + 1;              % Discrete vector and scalar constant
f = a * 3;              % Discrete scalar and scalar constant
g = b - a;              % Discrete vector and scalar

h = 1 + b;              % Discrete vector and scalar constant
i = 3 * a;              % Discrete scalar and scalar constant
j = a + -b;             % Discrete vector and scalar

k = d + 1;              % Real vector and scalar constant
l = c * 3;              % Real scalar and scalar constant
m = d - c;              % Real vector and scalar

n = 1 + d;              % Real vector and scalar constant
o = 3 * c;              % Real scalar and scalar constant
p = c + -d;             % Real vector and scalar

q = a + c;              % Discrete scalar and Real scalar
r = b .* c;             % Discrete vector and Real scalar
s = a + d;              % Discrete scalar and Real vector
t = b .* d;             % Discrete vector and Real vector

u = c + a;              % Discrete scalar and Real scalar
v = c .* b;             % Discrete vector and Real scalar
w = d + a;              % Discrete scalar and Real vector
x = d .* b;             % Discrete vector and Real vector

y = a * b;              % Discrete scalar times vector (non-pointwise operator)
z = d * c;              % Real scalar times vector (non-pointwise operator)
aa = b^a;               % Discrete vector to scalar power (non-pointwise operator)
bb = d^c;               % Real vector to scalar power (non-pointwise operator)
cc = c.^d;              % Real scalar to vector power (pointwise operator)
aa3 = b^3;              % Discrete vector to constant scalar power (non-pointwise operator)
bb3 = d^3;              % Real vector to scalar constant power (non-pointwise operator)
cc3 = d.^3;             % Real vector to constant vector power (pointwise operator)
aa2 = b^2;              % Discrete vector squared (non-pointwise operator)
bb2 = d^2;              % Real vector squared (non-pointwise operator)
cc2 = d.^2;             % Real vector squared (pointwise operator)
assert(cmpFactorName(aa3(1).Factors{1}, 'Power'));
assert(cmpFactorName(bb3(1).Factors{1}, 'Power'));
assert(cmpFactorName(cc3(1).Factors{1}, 'Power'));
assert(cmpFactorName(aa2(1).Factors{1}, 'Square'));
assert(cmpFactorName(bb2(1).Factors{1}, 'Square'));
assert(cmpFactorName(cc2(1).Factors{1}, 'Square'));

dd = A * bt;            % Discrete matrix times discrete column vector
ee = A * dt;            % Discrete matrix times real column vector
ff = B * bt;            % Real matrix times discrete column vector
gg = B * dt;            % Real matrix times real column vector

hh = b * A';            % Discrete row vector times discrete matrix
ii = d * A';            % Real row vector times discrete matrix
jj = b * B';            % Discrete row vector times real matrix
kk = d * B';            % Real row vector times real matrix

CA = [1 2 3 4; 5 6 7 8; 9 10 11 12];
cb = [9 8 7 6];
cbt = cb';
ll = CA * bt;           % Constant matrix times discrete vector
mm = CA * dt;           % Constant matrix times real vector
nn = A * cbt;           % Discrete variable matrix times constant vector
oo = B * cbt;           % Real variable matrix times constant vector
pp = b * CA';           % Discrete variable vector times constant matrix
qq = d * CA';           % Real variable vector times constant matrix
rr = cb * A';           % Constant vector times discrete matrix
tt = cb * B';           % Constant vector times real matrix

uu = a / 3;             % Discrete scalar and scalar constant
vv = 3 / a;             % Discrete scalar and scalar constant
ww = c / 3;             % Real scalar and scalar constant
xx = 3 / c;             % Real scalar and scalar constant
yy = b ./ c;            % Discrete vector and Real scalar
zz = b ./ d;            % Discrete vector and Real vector
aaa = c ./ b;           % Discrete vector and Real scalar
bbb = d ./ b;           % Discrete vector and Real vector
ccc = a ./ b;           % Discrete scalar and discrete vector
ddd = d ./ c;           % Real scalar and real vector

eee = rj1 * rj2;        % RealJoint vector times RealJoint vector
fff = rj1 * cb;         % RealJoint vector times constant vector
ggg = cb * rj2;         % Constant vector times RealJoint vector
hhh = rj1 * b;          % RealJoint vector times Discrete vector
iii = d * rj2;          % Real vector times RealJoint vector
jjj = b * bt;           % Discrete vector times discrete vector
kkk = b * d;            % Discrete vector times real vector
lll = bt * dt;          % Real vector times discrete vector
mmm = dt * b;           % Real vector times discrete vector
nnn = cb * d;           % Real constant vector times real vector
ooo = b * cbt;          % Discrete vector times real constant vector

CD = rand(N,P);
ppp = A * C;            % Discrete matrix times discrete matrix
qqq = A * D;            % Discrete matrix times real matrix
rrr = B * C;            % Real matrix times discrete matrix
ttt = B * D;            % Real matrix times real matrix
uuu = A * CD;           % Discrete matrix times constant matrix
vvv = B * CD;           % Real matrix times constant matrix
www = CA * C;           % Constant matrix times discrete matrix
xxx = CA * D;           % Constant matrix times real matrix

RJC = [1 2 3 4];
aaaa = rj1 + rj2;       % RealJoint plus RealJoint
bbbb = rj1 - rj2;       % RealJoint minus RealJoint
cccc = -rj1;            % RealJoint negation
dddd = rj1 + RJC;       % RealJoint plus vector constant
eeee = RJC + rj2;       % Vector constant plus RealJoint
ffff = rj1 - RJC;       % RealJoint minus vector constant
gggg = RJC - rj2;       % Vector constant minus RealJoint
hhhh = rj1 + 7;         % RealJoint plus scalar constant
iiii = 7 + rj2;         % Scalar constant plus RealJoint
jjjj = rj1 - 7;         % RealJoint minus scalar constant
kkkk = 7 - rj2;         % Scalar constant minums RealJoint

CDt = CD';
llll = CDt * rj1;       % Constant matrix times RealJoint
mmmm = rj1 * CD;        % RealJoint times constant matrix
nnnn = B * rj1;         % Real matrix times RealJoint
oooo = rj1 * D;         % RealJoint vector times Real matrix


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getAllSamples');
bs = b.invokeSolverMethodWithReturnValue('getAllSamples');
cs = c.invokeSolverMethodWithReturnValue('getAllSamples');
ds = d.invokeSolverMethodWithReturnValue('getAllSamples');
es = e.invokeSolverMethodWithReturnValue('getAllSamples');
fs = f.invokeSolverMethodWithReturnValue('getAllSamples');
gs = g.invokeSolverMethodWithReturnValue('getAllSamples');
hs = h.invokeSolverMethodWithReturnValue('getAllSamples');
is = i.invokeSolverMethodWithReturnValue('getAllSamples');
js = j.invokeSolverMethodWithReturnValue('getAllSamples');
ks = k.invokeSolverMethodWithReturnValue('getAllSamples');
ls = l.invokeSolverMethodWithReturnValue('getAllSamples');
ms = m.invokeSolverMethodWithReturnValue('getAllSamples');
ns = n.invokeSolverMethodWithReturnValue('getAllSamples');
os = o.invokeSolverMethodWithReturnValue('getAllSamples');
ps = p.invokeSolverMethodWithReturnValue('getAllSamples');
qs = q.invokeSolverMethodWithReturnValue('getAllSamples');
rs = r.invokeSolverMethodWithReturnValue('getAllSamples');
ss = s.invokeSolverMethodWithReturnValue('getAllSamples');
ts = t.invokeSolverMethodWithReturnValue('getAllSamples');
us = u.invokeSolverMethodWithReturnValue('getAllSamples');
vs = v.invokeSolverMethodWithReturnValue('getAllSamples');
ws = w.invokeSolverMethodWithReturnValue('getAllSamples');
xs = x.invokeSolverMethodWithReturnValue('getAllSamples');
ys = y.invokeSolverMethodWithReturnValue('getAllSamples');
zs = z.invokeSolverMethodWithReturnValue('getAllSamples');
aas = aa.invokeSolverMethodWithReturnValue('getAllSamples');
bbs = bb.invokeSolverMethodWithReturnValue('getAllSamples');
ccs = cc.invokeSolverMethodWithReturnValue('getAllSamples');
aa3s = aa3.invokeSolverMethodWithReturnValue('getAllSamples');
bb3s = bb3.invokeSolverMethodWithReturnValue('getAllSamples');
cc3s = cc3.invokeSolverMethodWithReturnValue('getAllSamples');
aa2s = aa2.invokeSolverMethodWithReturnValue('getAllSamples');
bb2s = bb2.invokeSolverMethodWithReturnValue('getAllSamples');
cc2s = cc2.invokeSolverMethodWithReturnValue('getAllSamples');
dds = dd.invokeSolverMethodWithReturnValue('getAllSamples');
ees = ee.invokeSolverMethodWithReturnValue('getAllSamples');
ffs = ff.invokeSolverMethodWithReturnValue('getAllSamples');
ggs = gg.invokeSolverMethodWithReturnValue('getAllSamples');
hhs = hh.invokeSolverMethodWithReturnValue('getAllSamples');
iis = ii.invokeSolverMethodWithReturnValue('getAllSamples');
jjs = jj.invokeSolverMethodWithReturnValue('getAllSamples');
kks = kk.invokeSolverMethodWithReturnValue('getAllSamples');
lls = ll.invokeSolverMethodWithReturnValue('getAllSamples');
mms = mm.invokeSolverMethodWithReturnValue('getAllSamples');
nns = nn.invokeSolverMethodWithReturnValue('getAllSamples');
oos = oo.invokeSolverMethodWithReturnValue('getAllSamples');
pps = pp.invokeSolverMethodWithReturnValue('getAllSamples');
qqs = qq.invokeSolverMethodWithReturnValue('getAllSamples');
rrs = rr.invokeSolverMethodWithReturnValue('getAllSamples');
tts = tt.invokeSolverMethodWithReturnValue('getAllSamples');
uus = uu.invokeSolverMethodWithReturnValue('getAllSamples');
vvs = vv.invokeSolverMethodWithReturnValue('getAllSamples');
wws = ww.invokeSolverMethodWithReturnValue('getAllSamples');
xxs = xx.invokeSolverMethodWithReturnValue('getAllSamples');
yys = yy.invokeSolverMethodWithReturnValue('getAllSamples');
zzs = zz.invokeSolverMethodWithReturnValue('getAllSamples');
aaas = aaa.invokeSolverMethodWithReturnValue('getAllSamples');
bbbs = bbb.invokeSolverMethodWithReturnValue('getAllSamples');
cccs = ccc.invokeSolverMethodWithReturnValue('getAllSamples');
ddds = ddd.invokeSolverMethodWithReturnValue('getAllSamples');
eees = eee.invokeSolverMethodWithReturnValue('getAllSamples');
fffs = fff.invokeSolverMethodWithReturnValue('getAllSamples');
gggs = ggg.invokeSolverMethodWithReturnValue('getAllSamples');
hhhs = hhh.invokeSolverMethodWithReturnValue('getAllSamples');
iiis = iii.invokeSolverMethodWithReturnValue('getAllSamples');
jjjs = jjj.invokeSolverMethodWithReturnValue('getAllSamples');
kkks = kkk.invokeSolverMethodWithReturnValue('getAllSamples');
llls = lll.invokeSolverMethodWithReturnValue('getAllSamples');
mmms = mmm.invokeSolverMethodWithReturnValue('getAllSamples');
nnns = nnn.invokeSolverMethodWithReturnValue('getAllSamples');
ooos = ooo.invokeSolverMethodWithReturnValue('getAllSamples');
ppps = ppp.invokeSolverMethodWithReturnValue('getAllSamples');
qqqs = qqq.invokeSolverMethodWithReturnValue('getAllSamples');
rrrs = rrr.invokeSolverMethodWithReturnValue('getAllSamples');
ttts = ttt.invokeSolverMethodWithReturnValue('getAllSamples');
uuus = uuu.invokeSolverMethodWithReturnValue('getAllSamples');
vvvs = vvv.invokeSolverMethodWithReturnValue('getAllSamples');
wwws = www.invokeSolverMethodWithReturnValue('getAllSamples');
xxxs = xxx.invokeSolverMethodWithReturnValue('getAllSamples');
aaaas = aaaa.Solver.getAllSamples;
bbbbs = bbbb.Solver.getAllSamples;
ccccs = cccc.Solver.getAllSamples;
dddds = dddd.Solver.getAllSamples;
eeees = eeee.Solver.getAllSamples;
ffffs = ffff.Solver.getAllSamples;
ggggs = gggg.Solver.getAllSamples;
hhhhs = hhhh.Solver.getAllSamples;
iiiis = iiii.Solver.getAllSamples;
jjjjs = jjjj.Solver.getAllSamples;
kkkks = kkkk.Solver.getAllSamples;
lllls = llll.Solver.getAllSamples;
mmmms = mmmm.Solver.getAllSamples;
nnnns = nnnn.Solver.getAllSamples;
oooos = oooo.Solver.getAllSamples;
bts = bt.invokeSolverMethodWithReturnValue('getAllSamples');
dts = dt.invokeSolverMethodWithReturnValue('getAllSamples');
As = A.invokeSolverMethodWithReturnValue('getAllSamples');
Bs = B.invokeSolverMethodWithReturnValue('getAllSamples');
Cs = C.invokeSolverMethodWithReturnValue('getAllSamples');
Ds = D.invokeSolverMethodWithReturnValue('getAllSamples');
rj1s = rj1.Solver.getAllSamples;
rj2s = rj2.Solver.getAllSamples;

% Convert to 2D double arrays (numSamples x N)
asx = repmat(arrayfun(@(x)x, as), 1, N);
bsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bs, 'UniformOutput', false));
csx = repmat(arrayfun(@(x)x, cs), 1, N);
dsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ds, 'UniformOutput', false));

esx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), es, 'UniformOutput', false));
fsx = repmat(arrayfun(@(x)x, fs), 1, N);
gsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), gs, 'UniformOutput', false));

hsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hs, 'UniformOutput', false));
isx = repmat(arrayfun(@(x)x, is), 1, N);
jsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), js, 'UniformOutput', false));

ksx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ks, 'UniformOutput', false));
lsx = repmat(arrayfun(@(x)x, ls), 1, N);
msx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ms, 'UniformOutput', false));

nsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ns, 'UniformOutput', false));
osx = repmat(arrayfun(@(x)x, os), 1, N);
psx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ps, 'UniformOutput', false));

qsx = repmat(arrayfun(@(x)x, qs), 1, N);
rsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), rs, 'UniformOutput', false));
ssx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ss, 'UniformOutput', false));
tsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ts, 'UniformOutput', false));

usx = repmat(arrayfun(@(x)x, us), 1, N);
vsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), vs, 'UniformOutput', false));
wsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ws, 'UniformOutput', false));
xsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), xs, 'UniformOutput', false));

ysx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ys, 'UniformOutput', false));
zsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), zs, 'UniformOutput', false));
aasx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aas, 'UniformOutput', false));
bbsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bbs, 'UniformOutput', false));
ccsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ccs, 'UniformOutput', false));
aa3sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aa3s, 'UniformOutput', false));
bb3sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bb3s, 'UniformOutput', false));
cc3sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), cc3s, 'UniformOutput', false));
aa2sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aa2s, 'UniformOutput', false));
bb2sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bb2s, 'UniformOutput', false));
cc2sx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), cc2s, 'UniformOutput', false));

ddsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), dds', 'UniformOutput', false))';
eesx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ees', 'UniformOutput', false))';
ffsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ffs', 'UniformOutput', false))';
ggsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ggs', 'UniformOutput', false))';

hhsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hhs, 'UniformOutput', false));
iisx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), iis, 'UniformOutput', false));
jjsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), jjs, 'UniformOutput', false));
kksx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), kks, 'UniformOutput', false));

llsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), lls', 'UniformOutput', false))';
mmsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), mms', 'UniformOutput', false))';
nnsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), nns', 'UniformOutput', false))';
oosx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), oos', 'UniformOutput', false))';

ppsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), pps, 'UniformOutput', false));
qqsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), qqs, 'UniformOutput', false));
rrsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), rrs, 'UniformOutput', false));
ttsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), tts, 'UniformOutput', false));

uusx = repmat(arrayfun(@(x)x, uus), 1, N);
vvsx = repmat(arrayfun(@(x)x, vvs), 1, N);
wwsx = repmat(arrayfun(@(x)x, wws), 1, N);
xxsx = repmat(arrayfun(@(x)x, xxs), 1, N);

yysx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), yys, 'UniformOutput', false));
zzsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), zzs, 'UniformOutput', false));
aaasx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aaas, 'UniformOutput', false));
bbbsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bbbs, 'UniformOutput', false));
cccsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), cccs, 'UniformOutput', false));
dddsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ddds, 'UniformOutput', false));

btsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bts', 'UniformOutput', false))';
dtsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), dts', 'UniformOutput', false))';
Asx = cellfun(@(x)(arrayfun(@(y)y,x)), As, 'UniformOutput', false);
Bsx = cellfun(@(x)(arrayfun(@(y)y,x)), Bs, 'UniformOutput', false);
Asxx = zeros(M, N, numSamples);
Bsxx = zeros(M, N, numSamples);
for sample = 1:numSamples
    for ri = 1:M
        for ci = 1:N
            Asxx(ri, ci, sample) = Asx{ri, ci}(sample);
            Bsxx(ri, ci, sample) = Bsx{ri, ci}(sample);
        end
    end
end
Csx = cellfun(@(x)(arrayfun(@(y)y,x)), Cs, 'UniformOutput', false);
Dsx = cellfun(@(x)(arrayfun(@(y)y,x)), Ds, 'UniformOutput', false);
Csxx = zeros(N, P, numSamples);
Dsxx = zeros(N, P, numSamples);
for sample = 1:numSamples
    for ri = 1:N
        for ci = 1:P
            Csxx(ri, ci, sample) = Csx{ri, ci}(sample);
            Dsxx(ri, ci, sample) = Dsx{ri, ci}(sample);
        end
    end
end
pppsx = cellfun(@(x)(arrayfun(@(y)y,x)), ppps, 'UniformOutput', false);
qqqsx = cellfun(@(x)(arrayfun(@(y)y,x)), qqqs, 'UniformOutput', false);
rrrsx = cellfun(@(x)(arrayfun(@(y)y,x)), rrrs, 'UniformOutput', false);
tttsx = cellfun(@(x)(arrayfun(@(y)y,x)), ttts, 'UniformOutput', false);
uuusx = cellfun(@(x)(arrayfun(@(y)y,x)), uuus, 'UniformOutput', false);
vvvsx = cellfun(@(x)(arrayfun(@(y)y,x)), vvvs, 'UniformOutput', false);
wwwsx = cellfun(@(x)(arrayfun(@(y)y,x)), wwws, 'UniformOutput', false);
xxxsx = cellfun(@(x)(arrayfun(@(y)y,x)), xxxs, 'UniformOutput', false);
pppsxx = zeros(M, P, numSamples);
qqqsxx = zeros(M, P, numSamples);
rrrsxx = zeros(M, P, numSamples);
tttsxx = zeros(M, P, numSamples);
uuusxx = zeros(M, P, numSamples);
vvvsxx = zeros(M, P, numSamples);
wwwsxx = zeros(M, P, numSamples);
xxxsxx = zeros(M, P, numSamples);
for sample = 1:numSamples
    for ri = 1:M
        for ci = 1:P
            pppsxx(ri, ci, sample) = pppsx{ri, ci}(sample);
            qqqsxx(ri, ci, sample) = qqqsx{ri, ci}(sample);
            rrrsxx(ri, ci, sample) = rrrsx{ri, ci}(sample);
            tttsxx(ri, ci, sample) = tttsx{ri, ci}(sample);
            uuusxx(ri, ci, sample) = uuusx{ri, ci}(sample);
            vvvsxx(ri, ci, sample) = vvvsx{ri, ci}(sample);
            wwwsxx(ri, ci, sample) = wwwsx{ri, ci}(sample);
            xxxsxx(ri, ci, sample) = xxxsx{ri, ci}(sample);
        end
    end
end


% Compare results
assertElementsAlmostEqual(esx, bsx + 1, 'absolute');            
assertElementsAlmostEqual(fsx, asx * 3, 'absolute');            
assertElementsAlmostEqual(gsx, bsx - asx, 'absolute');            

assertElementsAlmostEqual(hsx, 1 + bsx, 'absolute');            
assertElementsAlmostEqual(isx, 3 * asx, 'absolute');            
assertElementsAlmostEqual(jsx, asx + -bsx, 'absolute');           

assertElementsAlmostEqual(ksx, dsx + 1, 'absolute');            
assertElementsAlmostEqual(lsx, csx * 3, 'absolute');            
assertElementsAlmostEqual(msx, dsx - csx, 'absolute');            

assertElementsAlmostEqual(nsx, 1 + dsx, 'absolute');            
assertElementsAlmostEqual(osx, 3 * csx, 'absolute');            
assertElementsAlmostEqual(psx, csx + -dsx, 'absolute');           

assertElementsAlmostEqual(qsx, asx + csx, 'absolute');            
assertElementsAlmostEqual(rsx, bsx .* csx, 'absolute');           
assertElementsAlmostEqual(ssx, asx + dsx, 'absolute');            
assertElementsAlmostEqual(tsx, bsx .* dsx, 'absolute');           

assertElementsAlmostEqual(usx, csx + asx, 'absolute');            
assertElementsAlmostEqual(vsx, csx .* bsx, 'absolute');           
assertElementsAlmostEqual(wsx, dsx + asx, 'absolute');           
assertElementsAlmostEqual(xsx, dsx .* bsx, 'absolute');          

assertElementsAlmostEqual(ysx, asx .* bsx, 'absolute');
assertElementsAlmostEqual(zsx, dsx .* csx, 'absolute');
assertElementsAlmostEqual(aasx, bsx .^ asx, 'absolute');
assertElementsAlmostEqual(bbsx, dsx .^ csx, 'absolute');
assertElementsAlmostEqual(ccsx, csx .^ dsx, 'absolute');
assertElementsAlmostEqual(aa3sx, bsx .^ 3, 'absolute');
assertElementsAlmostEqual(bb3sx, dsx .^ 3, 'absolute');
assertElementsAlmostEqual(cc3sx, dsx .^ 3, 'absolute');
assertElementsAlmostEqual(aa2sx, bsx .^ 2, 'absolute');
assertElementsAlmostEqual(bb2sx, dsx .^ 2, 'absolute');
assertElementsAlmostEqual(cc2sx, dsx .^ 2, 'absolute');

assertElementsAlmostEqual(uusx, asx / 3, 'absolute');
assertElementsAlmostEqual(vvsx, 3 ./ asx, 'absolute');
assertElementsAlmostEqual(wwsx, csx / 3, 'absolute');
assertElementsAlmostEqual(xxsx, 3 ./ csx, 'absolute');
assertElementsAlmostEqual(yysx, bsx ./ csx, 'absolute');
assertElementsAlmostEqual(zzsx, bsx ./ dsx, 'absolute');
assertElementsAlmostEqual(aaasx, csx ./ bsx, 'absolute');
assertElementsAlmostEqual(bbbsx, dsx ./ bsx, 'absolute');
assertElementsAlmostEqual(cccsx, asx ./ bsx, 'absolute');
assertElementsAlmostEqual(dddsx, dsx ./ csx, 'absolute');

assertElementsAlmostEqual(eees, diag(rj1s * rj2s')); 
assertElementsAlmostEqual(fffs, rj1s * cb');
assertElementsAlmostEqual(gggs, rj2s * cb');
assertElementsAlmostEqual(hhhs, diag(rj1s * bsx'));
assertElementsAlmostEqual(iiis, diag(rj2s * dsx'));
assertElementsAlmostEqual(jjjs, diag(bsx * btsx));
assertElementsAlmostEqual(kkks, diag(bsx * dsx'));
assertElementsAlmostEqual(llls, diag(btsx' * dtsx));
assertElementsAlmostEqual(mmms, diag(bsx * dtsx));
assertElementsAlmostEqual(nnns, dsx * cb');
assertElementsAlmostEqual(ooos, bsx * cbt);

for sample=1:numSamples
    assertElementsAlmostEqual(ddsx(:,sample), Asxx(:,:,sample) * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(eesx(:,sample), Asxx(:,:,sample) * dtsx(:,sample), 'absolute');
    assertElementsAlmostEqual(ffsx(:,sample), Bsxx(:,:,sample) * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(ggsx(:,sample), Bsxx(:,:,sample) * dtsx(:,sample), 'absolute');
    
    assertElementsAlmostEqual(hhsx(sample,:), bsx(sample,:) * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(iisx(sample,:), dsx(sample,:) * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(jjsx(sample,:), bsx(sample,:) * Bsxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(kksx(sample,:), dsx(sample,:) * Bsxx(:,:,sample)', 'absolute');

    assertElementsAlmostEqual(llsx(:,sample), CA * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(mmsx(:,sample), CA * dtsx(:,sample), 'absolute');
    assertElementsAlmostEqual(nnsx(:,sample), Asxx(:,:,sample) * cbt, 'absolute');
    assertElementsAlmostEqual(oosx(:,sample), Bsxx(:,:,sample) * cbt, 'absolute');
    
    assertElementsAlmostEqual(ppsx(sample,:), bsx(sample,:) * CA', 'absolute');
    assertElementsAlmostEqual(qqsx(sample,:), dsx(sample,:) * CA', 'absolute');
    assertElementsAlmostEqual(rrsx(sample,:), cb * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(ttsx(sample,:), cb * Bsxx(:,:,sample)', 'absolute');
    
    assertElementsAlmostEqual(pppsxx(:,:,sample), Asxx(:,:,sample) * Csxx(:,:,sample), 'absolute');
    assertElementsAlmostEqual(qqqsxx(:,:,sample), Asxx(:,:,sample) * Dsxx(:,:,sample), 'absolute');
    assertElementsAlmostEqual(rrrsxx(:,:,sample), Bsxx(:,:,sample) * Csxx(:,:,sample), 'absolute');
    assertElementsAlmostEqual(tttsxx(:,:,sample), Bsxx(:,:,sample) * Dsxx(:,:,sample), 'absolute');
    assertElementsAlmostEqual(uuusxx(:,:,sample), Asxx(:,:,sample) * CD, 'absolute');
    assertElementsAlmostEqual(vvvsxx(:,:,sample), Bsxx(:,:,sample) * CD, 'absolute');
    assertElementsAlmostEqual(wwwsxx(:,:,sample), CA * Csxx(:,:,sample), 'absolute');
    assertElementsAlmostEqual(xxxsxx(:,:,sample), CA * Dsxx(:,:,sample), 'absolute');

    assertElementsAlmostEqual(lllls(sample,:)', CDt * rj1s(sample,:)');
    assertElementsAlmostEqual(mmmms(sample,:), rj1s(sample,:) * CD);
    assertElementsAlmostEqual(nnnns(sample,:)', Bsxx(:,:,sample) * rj1s(sample,:)');
    assertElementsAlmostEqual(oooos(sample,:), rj1s(sample,:) * Dsxx(:,:,sample));

end

RJCs = repmat(RJC,numSamples,1);
assertElementsAlmostEqual(aaaas, rj1s + rj2s);
assertElementsAlmostEqual(bbbbs, rj1s - rj2s);
assertElementsAlmostEqual(ccccs, -rj1s);
assertElementsAlmostEqual(dddds, rj1s + RJCs);
assertElementsAlmostEqual(eeees, RJCs + rj2s);
assertElementsAlmostEqual(ffffs, rj1s - RJCs);
assertElementsAlmostEqual(ggggs, RJCs - rj2s);
assertElementsAlmostEqual(hhhhs, rj1s + 7);
assertElementsAlmostEqual(iiiis, 7 + rj2s);
assertElementsAlmostEqual(jjjjs, rj1s - 7);
assertElementsAlmostEqual(kkkks, 7 - rj2s);

end


% Test a discrete MATLAB deterministic directed factor
function test4(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

inDomain = 1:10;
[inx,iny] = ndgrid(inDomain,inDomain);
outDomain = sort(unique(reshape(inx + iny,1,length(inDomain)^2)));
a = Discrete(inDomain);
b = Discrete(inDomain);
c = Discrete(outDomain);
fg.addFactor(@myPlusFactor, c, a, b).DirectedTo = c;


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples();
bs = b.Solver.getAllSamples();
cs = c.Solver.getAllSamples();

asx = arrayfun(@(x)x, as);
bsx = arrayfun(@(x)x, bs);
csx = arrayfun(@(x)x, cs);
assertElementsAlmostEqual(asx + bsx, csx, 'absolute');

end

function value = myPlusFactor(out,in1,in2)
    if (out == in1 + in2)
        value = 2;  % Verify that any constant will do
    else
        value = 0;
    end
end


% Test Equals and NotEquals
function test5(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

N = 8;
a = Discrete(1:4);
b = Discrete(1:4);
c = Bit();
d = Bit();
aa = Discrete(1:4,1,N);
ba = Discrete(1:4,1,N);
ca = Bit(1,N);
da = Bit(1,N);

e = Equals(a);
f = Equals(a,b);
g = Equals(a,b,c,d);
h = Equals(a,3);
ea = Equals(aa);
fa = Equals(aa,ba);
ga = Equals(aa,ba,ca,da);
ha = Equals(aa,3);
ia = Equals(a,ba);
ja = Equals(aa,ba,c);

en = NotEquals(a);
fn = NotEquals(a,b);
gn = NotEquals(a,b,c,d);
hn = NotEquals(a,3);
ean = NotEquals(aa);
fan = NotEquals(aa,ba);
gan = NotEquals(aa,ba,ca,da);
han = NotEquals(aa,3);
ian = NotEquals(a,ba);
jan = NotEquals(aa,ba,c);


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();


aS = a.Solver.getAllSamples();
bS = b.Solver.getAllSamples();
cS = c.Solver.getAllSamples();
dS = d.Solver.getAllSamples();
aaS = aa.invokeSolverMethodWithReturnValue('getAllSamples');
baS = ba.invokeSolverMethodWithReturnValue('getAllSamples');
caS = ca.invokeSolverMethodWithReturnValue('getAllSamples');
daS = da.invokeSolverMethodWithReturnValue('getAllSamples');

eS = e.Solver.getAllSamples();
fS = f.Solver.getAllSamples();
gS = g.Solver.getAllSamples();
hS = h.Solver.getAllSamples();
eaS = ea.invokeSolverMethodWithReturnValue('getAllSamples');
faS = fa.invokeSolverMethodWithReturnValue('getAllSamples');
gaS = ga.invokeSolverMethodWithReturnValue('getAllSamples');
haS = ha.invokeSolverMethodWithReturnValue('getAllSamples');
iaS = ia.invokeSolverMethodWithReturnValue('getAllSamples');
jaS = ja.invokeSolverMethodWithReturnValue('getAllSamples');

enS = en.Solver.getAllSamples();
fnS = fn.Solver.getAllSamples();
gnS = gn.Solver.getAllSamples();
hnS = hn.Solver.getAllSamples();
eanS = ean.invokeSolverMethodWithReturnValue('getAllSamples');
fanS = fan.invokeSolverMethodWithReturnValue('getAllSamples');
ganS = gan.invokeSolverMethodWithReturnValue('getAllSamples');
hanS = han.invokeSolverMethodWithReturnValue('getAllSamples');
ianS = ian.invokeSolverMethodWithReturnValue('getAllSamples');
janS = jan.invokeSolverMethodWithReturnValue('getAllSamples');


aSX = arrayfun(@(x)x, aS);
bSX = arrayfun(@(x)x, bS);
cSX = arrayfun(@(x)x, cS);
dSX = arrayfun(@(x)x, dS);
aSXN = repmat(aSX, 1, N);
bSXN = repmat(bSX, 1, N);
cSXN = repmat(cSX, 1, N);
dSXN = repmat(dSX, 1, N);
aaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aaS, 'UniformOutput', false));
baSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), baS, 'UniformOutput', false));
caSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), caS, 'UniformOutput', false));
daSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), daS, 'UniformOutput', false));

eSX = arrayfun(@(x)x, eS);
fSX = arrayfun(@(x)x, fS);
gSX = arrayfun(@(x)x, gS);
hSX = arrayfun(@(x)x, hS);
eaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), eaS, 'UniformOutput', false));
faSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), faS, 'UniformOutput', false));
gaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), gaS, 'UniformOutput', false));
haSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), haS, 'UniformOutput', false));
iaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), iaS, 'UniformOutput', false));
jaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), jaS, 'UniformOutput', false));

enSX = arrayfun(@(x)x, enS);
fnSX = arrayfun(@(x)x, fnS);
gnSX = arrayfun(@(x)x, gnS);
hnSX = arrayfun(@(x)x, hnS);
eanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), eanS, 'UniformOutput', false));
fanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), fanS, 'UniformOutput', false));
ganSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ganS, 'UniformOutput', false));
hanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hanS, 'UniformOutput', false));
ianSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ianS, 'UniformOutput', false));
janSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), janS, 'UniformOutput', false));


assert(all(eSX));            
assert(all(fSX == (aSX==bSX)));            
assert(all(gSX == (aSX==bSX & aSX==cSX & aSX==dSX)));            
assert(all(hSX == (aSX==3)));          

assert(all(eaSX(:)));
assert(all(faSX(:) == (aaSX(:)==baSX(:))));
assert(all(gaSX(:) == (aaSX(:)==baSX(:) & aaSX(:)==caSX(:) & aaSX(:)==daSX(:))));            
assert(all(haSX(:) == (aaSX(:)==3)));          
assert(all(iaSX(:) == (aSXN(:)==baSX(:))));          
assert(all(jaSX(:) == (aaSX(:)==baSX(:) & aaSX(:)==cSXN(:))));            

assert(all(~enSX));            
assert(all(fnSX ~= (aSX==bSX)));            
assert(all(gnSX ~= (aSX==bSX & aSX==cSX & aSX==dSX)));            
assert(all(hnSX ~= (aSX==3)));            

assert(all(~eanSX(:)));
assert(all(fanSX(:) ~= (aaSX(:)==baSX(:))));
assert(all(ganSX(:) ~= (aaSX(:)==baSX(:) & aaSX(:)==caSX(:) & aaSX(:)==daSX(:))));            
assert(all(hanSX(:) ~= (aaSX(:)==3)));          
assert(all(ianSX(:) ~= (aSXN(:)==baSX(:))));          
assert(all(janSX(:) ~= (aaSX(:)==baSX(:) & aaSX(:)==cSXN(:))));            


end



% Test * (mtimes) for all supported variations
function test6(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

N = 4;
M = 5;
L = 6;
domain = 1:10;
cr = rand;
crN = rand(1,N);
crM = rand(1,M);
crNM = rand(N,M);
crML = rand(M,L);
cc = rand + 1i*rand;
ccN = rand(1,N) + 1i*rand(1,N);
ccM = rand(1,M) + 1i*rand(1,M);
ccNM = rand(N,M) + 1i*rand(N,M);
ccML = rand(M,L) + 1i*rand(M,L);
D = Discrete(domain);
DN = Discrete(domain,1,N);
DM = Discrete(domain,1,M);
DNM = Discrete(domain,N,M);
DML = Discrete(domain,M,L);
R = Real();
RN = Real(1,N);
RM = Real(1,M);
RNM = Real(N,M);
RML = Real(M,L);
C = Complex();
CN = Complex(1,N);
CM = Complex(1,M);
CNM = Complex(N,M);
CML = Complex(M,L);
Jn = RealJoint(N);
Jm = RealJoint(M);

D2 = Discrete(domain);
DN2 = Discrete(domain,1,N);
DM2 = Discrete(domain,1,M);
DNM2 = Discrete(domain,N,M);
DML2 = Discrete(domain,M,L);
R2 = Real();
RN2 = Real(1,N);
RM2 = Real(1,M);
RNM2 = Real(N,M);
RML2 = Real(M,L);
C2 = Complex();
CN2 = Complex(1,N);
CM2 = Complex(1,M);
CNM2 = Complex(N,M);
CML2 = Complex(M,L);
Jn2 = RealJoint(N);
Jm2 = RealJoint(M);

% Create all supported combinations of (non-pointwise) products
aD1 = D * cr;
aD2 = cr * D;
bD1 = D * crN;
bD2 = crN * D;
cD1 = D * crNM;
cD2 = crNM * D;
dD1 = D * cc;
dD2 = cc * D;
eD1 = D * ccN;
eD2 = ccN * D;
fD1 = D * ccNM;
fD2 = ccNM * D;
gD1 = D * D2;
gD2 = D2 * D;

assert(cmp(aD1, 'Discrete', 'Product', [1 1]));
assert(cmp(aD2, 'Discrete', 'Product', [1 1]));
assert(cmp(bD1, 'Discrete', 'Product', [1 N]));
assert(cmp(bD2, 'Discrete', 'Product', [1 N]));
assert(cmp(cD1, 'Discrete', 'Product', [N M]));
assert(cmp(cD2, 'Discrete', 'Product', [N M]));
assert(cmp(dD1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(dD2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(eD1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(eD2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(fD1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(fD2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gD1, 'Discrete', 'Product', [1 1]));
assert(cmp(gD2, 'Discrete', 'Product', [1 1]));

aDN1 = DN * cr;
aDN2 = cr * DN;
bDN1 = DN * crN;
bDN2 = crN * DN;
cDN1 = DN * crNM;
cDN2 = crNM * DM;
dDN1 = DN * cc;
dDN2 = cc * DN;
% eDN1 = DN * ccN;      % Not currently supported
% eDN2 = ccN * DN;      % Not currently supported
% fDN1 = DN * ccNM;     % Not currently supported
% fDN2 = ccNM * DN;     % Not currently supported
gDN1 = DN * D;
gDN2 = D * DN;
hDN1 = DN * DN2;
hDN2 = DN2 * DN;

assert(cmp(aDN1, 'Discrete', 'Product', [1 N]));
assert(cmp(aDN2, 'Discrete', 'Product', [1 N]));
assert(cmp(bDN1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(bDN2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(cDN1, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(cDN2, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(dDN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(dDN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(gDN1, 'Discrete', 'Product', [1 N]));
assert(cmp(gDN2, 'Discrete', 'Product', [1 N]));
assert(cmp(hDN1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(hDN2, 'Real', 'VectorInnerProduct', [1 1]));

aDNM1 = DNM * cr;
aDNM2 = cr * DNM;
bDNM1 = DNM * crM;
bDNM2 = crN * DNM;
cDNM1 = DNM * crML;
cDNM2 = crNM * DML;
dDNM1 = DNM * cc;
dDNM2 = cc * DNM;
% eDNM1 = DNM * ccM;     % Not currently supported
% eDNM2 = ccN * DNM;     % Not currently supported
% fDNM1 = DNM * ccML;    % Not currently supported
% fDNM2 = ccNM * DML;    % Not currently supported
gDNM1 = DNM * D;
gDNM2 = D * DNM;
hDNM1 = DNM * DM;
hDNM2 = DN * DNM;
iDNM1 = DNM * DML2;
iDNM2 = DNM2 * DML;

assert(cmp(aDNM1, 'Discrete', 'Product', [N M]));
assert(cmp(aDNM2, 'Discrete', 'Product', [N M]));
assert(cmp(bDNM1, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(bDNM2, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(cDNM1, 'Real', 'MatrixProduct', [N L]));
assert(cmp(cDNM2, 'Real', 'MatrixProduct', [N L]));
assert(cmp(dDNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(dDNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gDNM1, 'Discrete', 'Product', [N M]));
assert(cmp(gDNM2, 'Discrete', 'Product', [N M]));
assert(cmp(hDNM1, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(hDNM2, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(iDNM1, 'Real', 'MatrixProduct', [N L]));
assert(cmp(iDNM2, 'Real', 'MatrixProduct', [N L]));

aR1 = R * cr;
aR2 = cr * R;
bR1 = R * crN;
bR2 = crN * R;
cR1 = R * crNM;
cR2 = crNM * R;
dR1 = R * cc;
dR2 = cc * R;
eR1 = R * ccN;
eR2 = ccN * R;
fR1 = R * ccNM;
fR2 = ccNM * R;
gR1 = R * D;
gR2 = D * R;
hR1 = R * DN;
hR2 = DN * R;
iR1 = R * DNM;
iR2 = DNM * R;
jR1 = R * R2;
jR2 = R2 * R;

assert(cmp(aR1, 'Real', 'Product', [1 1]));
assert(cmp(aR2, 'Real', 'Product', [1 1]));
assert(cmp(bR1, 'Real', 'Product', [1 N]));
assert(cmp(bR2, 'Real', 'Product', [1 N]));
assert(cmp(cR1, 'Real', 'Product', [N M]));
assert(cmp(cR2, 'Real', 'Product', [N M]));
assert(cmp(dR1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(dR2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(eR1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(eR2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(fR1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(fR2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gR1, 'Real', 'Product', [1 1]));
assert(cmp(gR2, 'Real', 'Product', [1 1]));
assert(cmp(hR1, 'Real', 'Product', [1 N]));
assert(cmp(hR2, 'Real', 'Product', [1 N]));
assert(cmp(iR1, 'Real', 'Product', [N M]));
assert(cmp(iR2, 'Real', 'Product', [N M]));
assert(cmp(jR1, 'Real', 'Product', [1 1]));
assert(cmp(jR2, 'Real', 'Product', [1 1]));

aRN1 = RN * cr;
aRN2 = cr * RN;
bRN1 = RN * crN;
bRN2 = crN * RN;
cRN1 = RN * crNM;
cRN2 = crNM * RM;
dRN1 = RN * cc;
dRN2 = cc * RN;
% eRN1 = RN * ccN;     % Not currently supported
% eRN2 = ccN * RN;     % Not currently supported
% fRN1 = RN * ccNM;    % Not currently supported
% fRN2 = ccNM * RN;    % Not currently supported
gRN1 = RN * D;
gRN2 = D * RN;
hRN1 = RN * DN;
hRN2 = DN * RN;
iRN1 = RN * DNM;
iRN2 = DNM * RM;
jRN1 = RN * R;
jRN2 = R * RN;
kRN1 = RN * RN2;
kRN2 = RN2 * RN;

assert(cmp(aRN1, 'Real', 'Product', [1 N]));
assert(cmp(aRN2, 'Real', 'Product', [1 N]));
assert(cmp(bRN1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(bRN2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(cRN1, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(cRN2, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(dRN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(dRN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(gRN1, 'Real', 'Product', [1 N]));
assert(cmp(gRN2, 'Real', 'Product', [1 N]));
assert(cmp(hRN1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(hRN2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(iRN1, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(iRN2, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(jRN1, 'Real', 'Product', [1 N]));
assert(cmp(jRN2, 'Real', 'Product', [1 N]));
assert(cmp(kRN1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(kRN2, 'Real', 'VectorInnerProduct', [1 1]));

aRNM1 = RNM * cr;
aRNM2 = cr * RNM;
bRNM1 = RNM * crM;
bRNM2 = crN * RNM;
cRNM1 = RNM * crML;
cRNM2 = crNM * RML;
dRNM1 = RNM * cc;
dRNM2 = cc * RNM;
% eRNM1 = RNM * ccN;     % Not currently supported
% eRNM2 = ccN * RNM;     % Not currently supported
% fRNM1 = RNM * ccNM;    % Not currently supported
% fRNM2 = ccNM * RNM;    % Not currently supported
gRNM1 = RNM * D;
gRNM2 = D * RNM;
hRNM1 = RNM * DM;
hRNM2 = DN * RNM;
iRNM1 = RNM * DML;
iRNM2 = DNM * RML;
jRNM1 = RNM * R;
jRNM2 = R * RNM;
kRNM1 = RNM * RM;
kRNM2 = RN * RNM;
lRNM1 = RNM * RML2;
lRNM2 = RNM2 * RML;

assert(cmp(aRNM1, 'Real', 'Product', [N M]));
assert(cmp(aRNM2, 'Real', 'Product', [N M]));
assert(cmp(bRNM1, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(bRNM2, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(cRNM1, 'Real', 'MatrixProduct', [N L]));
assert(cmp(cRNM2, 'Real', 'MatrixProduct', [N L]));
assert(cmp(dRNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(dRNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gRNM1, 'Real', 'Product', [N M]));
assert(cmp(gRNM2, 'Real', 'Product', [N M]));
assert(cmp(hRNM1, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(hRNM2, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(iRNM1, 'Real', 'MatrixProduct', [N L]));
assert(cmp(iRNM2, 'Real', 'MatrixProduct', [N L]));
assert(cmp(jRNM1, 'Real', 'Product', [N M]));
assert(cmp(jRNM2, 'Real', 'Product', [N M]));
assert(cmp(kRNM1, 'Real', 'MatrixVectorProduct', [N 1]));
assert(cmp(kRNM2, 'Real', 'MatrixVectorProduct', [1 M]));
assert(cmp(lRNM1, 'Real', 'MatrixProduct', [N L]));
assert(cmp(lRNM2, 'Real', 'MatrixProduct', [N L]));

aC1 = C * cr;
aC2 = cr * C;
bC1 = C * crN;
bC2 = crN * C;
cC1 = C * crNM;
cC2 = crNM * C;
dC1 = C * cc;
dC2 = cc * C;
eC1 = C * ccN;
eC2 = ccN * C;
fC1 = C * ccNM;
fC2 = ccNM * C;
gC1 = C * D;
gC2 = D * C;
hC1 = C * DN;
hC2 = DN * C;
iC1 = C * DNM;
iC2 = DNM * C;
jC1 = C * R;
jC2 = R * C;
kC1 = C * RN;
kC2 = RN * C;
lC1 = C * RNM;
lC2 = RNM * C;
mC1 = C * C2;
mC2 = C2 * C;

assert(cmp(aC1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(aC2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(bC1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(bC2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(cC1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(cC2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(dC1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(dC2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(eC1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(eC2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(fC1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(fC2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gC1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(gC2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(hC1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(hC2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(iC1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(iC2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(jC1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(jC2, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(kC1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(kC2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(lC1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(lC2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(mC1, 'Complex', 'ComplexProduct', [1 1]));
assert(cmp(mC2, 'Complex', 'ComplexProduct', [1 1]));

aCN1 = CN * cr;
aCN2 = cr * CN;
% bCN1 = CN * crN;     % Not currently supported
% bCN2 = crN * CN;     % Not currently supported
% cCN1 = CN * crNM;    % Not currently supported
% cCN2 = crNM * CN;    % Not currently supported
dCN1 = CN * cc;
dCN2 = cc * CN;
% eCN1 = CN * ccN;     % Not currently supported
% eCN2 = ccN * CN;     % Not currently supported
% fCN1 = CN * ccNM;    % Not currently supported
% fCN2 = ccNM * CN;    % Not currently supported
gCN1 = CN * D;
gCN2 = D * CN;
% hCN1 = CN * DN;      % Not currently supported
% hCN2 = DN * CN;      % Not currently supported
% iCN1 = CN * DNM;     % Not currently supported
% iCN2 = DNM * CN;     % Not currently supported
jCN1 = CN * R;
jCN2 = R * CN;
% kCN1 = CN * RN;      % Not currently supported
% kCN2 = RN * CN;      % Not currently supported
% lCN1 = CN * RNM;     % Not currently supported
% lCN2 = RNM * CN;     % Not currently supported
mCN1 = CN * C;
mCN2 = C * CN;
% nCN1 = CN * CN2;     % Not currently supported
% nCN2 = CN2 * CN;     % Not currently supported

assert(cmp(aCN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(aCN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(dCN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(dCN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(gCN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(gCN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(jCN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(jCN2, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(mCN1, 'Complex', 'ComplexProduct', [1 N]));
assert(cmp(mCN2, 'Complex', 'ComplexProduct', [1 N]));

aCNM1 = CNM * cr;
aCNM2 = cr * CNM;
% bCNM1 = CNM * crN;      % Not currently supported
% bCNM2 = crN * CNM;      % Not currently supported
% cCNM1 = CNM * crNM;     % Not currently supported
% cCNM2 = crNM * CNM;     % Not currently supported
dCNM1 = CNM * cc;
dCNM2 = cc * CNM;
% eCNM1 = CNM * ccN;      % Not currently supported
% eCNM2 = ccN * CNM;      % Not currently supported
% fCNM1 = CNM * ccNM;     % Not currently supported
% fCNM2 = ccNM * CNM;     % Not currently supported
gCNM1 = CNM * D;
gCNM2 = D * CNM;
% hCNM1 = CNM * DN;       % Not currently supported
% hCNM2 = DN * CNM;       % Not currently supported
% iCNM1 = CNM * DNM;      % Not currently supported
% iCNM2 = DNM * CNM;      % Not currently supported
jCNM1 = CNM * R;
jCNM2 = R * CNM;
% kCNM1 = CNM * RN;       % Not currently supported
% kCNM2 = RN * CNM;       % Not currently supported
% lCNM1 = CNM * RNM;      % Not currently supported
% lCNM2 = RNM * CNM;      % Not currently supported
mCNM1 = CNM * C;
mCNM2 = C * CNM;
% nCNM1 = CNM * CN;       % Not currently supported
% nCNM2 = CN * CNM;       % Not currently supported
% oCNM1 = CNM * CNM2;     % Not currently supported
% oCNM2 = CNM2 * CNM;     % Not currently supported

assert(cmp(aCNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(aCNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(dCNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(dCNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gCNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(gCNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(jCNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(jCNM2, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(mCNM1, 'Complex', 'ComplexProduct', [N M]));
assert(cmp(mCNM2, 'Complex', 'ComplexProduct', [N M]));

% aJ1 = Jn * cr;       % Not currently supported
% aJ2 = cr * Jn;       % Not currently supported
bJ1 = Jn * crN;
bJ2 = crN * Jn;
cJ1 = Jn * crNM;
cJ2 = crNM * Jm;
% dJ1 = Jn * cc;       % Not currently supported
% dJ2 = cc * Jn;       % Not currently supported
% eJ1 = Jn * ccN;      % Not currently supported
% eJ2 = ccN * Jn;      % Not currently supported
% fJ1 = Jn * ccNM;     % Not currently supported
% fJ2 = ccNM * Jm;     % Not currently supported
% gJ1 = Jn * D;        % Not currently supported
% gJ2 = D * Jn;        % Not currently supported
hJ1 = Jn* DN;
hJ2 = DN * Jn;
iJ1 = Jn * DNM;
iJ2 = DNM * Jm;
% jJ1 = J * R;         % Not currently supported
% jJ2 = R * J;         % Not currently supported
kJ1 = Jn * RN;
kJ2 = RN * Jn;
lJ1 = Jn * RNM;
lJ2 = RNM * Jm;
% mJ1 = Jn * C;        % Not currently supported
% mJ2 = C * Jn;        % Not currently supported
% nJ1 = Jn * CN;       % Not currently supported
% nJ2 = CN * Jn;       % Not currently supported
% oJ1 = Jn * CNM;      % Not currently supported
% oJ2 = CNM * Jm;      % Not currently supported
pJ1 = Jn * Jn2;
pJ2 = Jn2 * Jn;

assert(cmp(bJ1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(bJ2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(cJ1, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(cmp(cJ2, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(cJ1.Domain.NumElements == M);
assert(cJ2.Domain.NumElements == N);
assert(cmp(hJ1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(hJ2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(iJ1, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(cmp(iJ2, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(iJ1.Domain.NumElements == M);
assert(iJ2.Domain.NumElements == N);
assert(cmp(kJ1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(kJ2, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(lJ1, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(cmp(lJ2, 'RealJoint', 'MatrixRealJointVectorProduct', [1 1]));
assert(lJ1.Domain.NumElements == M);
assert(lJ2.Domain.NumElements == N);
assert(cmp(pJ1, 'Real', 'VectorInnerProduct', [1 1]));
assert(cmp(pJ2, 'Real', 'VectorInnerProduct', [1 1]));

end



% Helpers
function c = cmp(variable, expectedTypeName, expectedFactorName, expectedSize)
s = all(size(variable) == expectedSize);
v = cmpVarType(variable, expectedTypeName);
f = cmpFactorName(variable(1).Factors{1}, expectedFactorName); 
c = s && v && f;
end

function c = cmpVarType(variable, expectedName)
className = class(variable);
c = strcmp(className, expectedName);
end

function c = cmpFactorName(factor, expectedName)
factorFunctionName = class(factor.VectorObject.getFactorFunction.getContainedFactorFunction);
fqName = ['com.analog.lyric.dimple.factorfunctions.' expectedName];
c = strcmp(factorFunctionName, fqName);
end
