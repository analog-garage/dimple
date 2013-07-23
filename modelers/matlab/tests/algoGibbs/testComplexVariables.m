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

function testComplexVariables()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testComplexVariables');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);

dtrace(debugPrint, '--testComplexVariables');

end

% Basic test
function test1(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Complex();
b = Complex();
c = Complex();
a.Name = 'a';
b.Name = 'b';
c.Name = 'c';

fg.addFactor('ComplexSum', c, a, b);

a.Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
b.Input = {FactorFunction('Normal',4,1), FactorFunction('Normal',6,2)};

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = a.Solver.getAllSamples;
bs = b.Solver.getAllSamples;
cs = c.Solver.getAllSamples;
ac = pairsToComplex(as);
bc = pairsToComplex(bs);
cc = pairsToComplex(cs);

assertElementsAlmostEqual(cc, ac + bc, 'absolute');
assertElementsAlmostEqual(mean(cs), [7,5], 'absolute', 0.05);
assert(a.Domain.NumElements == 2);
assert(b.Domain.NumElements == 2);
assert(c.Domain.NumElements == 2);

end


% Test complex operator overloading; all complex operators
function test2(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

x = Complex();
y = Complex();
x.Input = {FactorFunction('Normal',0,10), FactorFunction('Normal',0,10)};
y.Input = {FactorFunction('Normal',0,10), FactorFunction('Normal',0,10)};

a = x + y;
b = x - y;
c = x * y;
d = x / y;
e = -x;
f = x';
g = exp(x);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples();
bs = b.Solver.getAllSamples();
cs = c.Solver.getAllSamples();
ds = d.Solver.getAllSamples();
es = e.Solver.getAllSamples();
fs = f.Solver.getAllSamples();
gs = g.Solver.getAllSamples();
xs = x.Solver.getAllSamples();
ys = y.Solver.getAllSamples();

ac = pairsToComplex(as);
bc = pairsToComplex(bs);
cc = pairsToComplex(cs);
dc = pairsToComplex(ds);
ec = pairsToComplex(es);
fc = pairsToComplex(fs);
gc = pairsToComplex(gs);
xc = pairsToComplex(xs);
yc = pairsToComplex(ys);


assertElementsAlmostEqual(ac, xc + yc, 'absolute');
assertElementsAlmostEqual(bc, xc - yc, 'absolute');
assertElementsAlmostEqual(cc, xc .* yc, 'absolute');
assertElementsAlmostEqual(dc, xc ./ yc, 'absolute');
assertElementsAlmostEqual(ec, -xc, 'absolute');
assertElementsAlmostEqual(fc, (xc').', 'absolute');
assertElementsAlmostEqual(gc, exp(xc), 'absolute');

end



% Test vectorized complex operator overloading
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
a = Complex();
b = Complex(1,N);
c = Complex(1,N);
bt = Complex(N,1);

ar = Real();
br = Real(1,N);

d = a + 1+2i;           % Complex scalar and complex scalar constant
e = b + 1+2i;           % Complex vector and complex scalar constant
f = a - b;              % Complex scalar and complex vector
g = 1+2i + a;           % Complex scalar constant and complex scalar
h = 1+2i + b;           % Complex scalar constant and complex vector
i = b - a;              % Complex vector and complex scalar

j = a * b;              % Complex scalar times vector (non-pointwise operator)
k = b * a;              % Complex vector times scalar (non-pointwise operator)
l = a .* b;             % Complex scalar times vector (pointwise operator)
m = b .* a;             % Complex vector times scalar (pointwise operator)
n = b .* c;             % Complex vector times complex vector (pointwise operator)

o = b / a;              % Complex vector divided by scalar (non-pointwise operator)
p = a ./ b;             % Complex scalar divided by vector (pointwise operator)
q = b ./ a;             % Complex vector divided by scalar (pointwise operator)
r = b ./ c;             % Complex vector divided by complex vector (pointwise operator)

s = b';                 % Conjugate transpose
t = bt';                % Conjugate transpose

u = a - ar;             % Complex scalar and real scalar
v = ar + a;             % Real scalar and complex scalar
w = b - br;             % Complex vector and real vector
x = br + b;             % Real vector and complex vector

y = a * br;             % Complex scalar times real vector (non-pointwise operator)
z = b * ar;             % Complex vector times real scalar (non-pointwise operator)
aa = ar .* b;           % Real scalar times complex vector (pointwise operator)
bb = br .* a;           % Real vector times complex scalar (pointwise operator)

cc = b / ar;            % Complex vector divided by real scalar (non-pointwise operator)
dd = a ./ br;           % Complex scalar divided by real vector (pointwise operator)
ee = b ./ ar;           % Complex vector divided by real scalar (pointwise operator)
ff = b ./ br;           % Complex vector divided by real complex vector (pointwise operator)

gg = br / a;            % Real vector divided by complex scalar (non-pointwise operator)
hh = ar ./ b;           % Real scalar divided by complex vector (pointwise operator)
ii = br ./ a;           % Real vector divided by complex scalar (pointwise operator)
jj = br ./ b;           % Real vector divided by complex vector (pointwise operator)


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getAllSamples');
ars = ar.invokeSolverMethodWithReturnValue('getAllSamples');
bs = b.invokeSolverMethodWithReturnValue('getAllSamples');
bts = bt.invokeSolverMethodWithReturnValue('getAllSamples');
brs = br.invokeSolverMethodWithReturnValue('getAllSamples');
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
dds = dd.invokeSolverMethodWithReturnValue('getAllSamples');
ees = ee.invokeSolverMethodWithReturnValue('getAllSamples');
ffs = ff.invokeSolverMethodWithReturnValue('getAllSamples');
ggs = gg.invokeSolverMethodWithReturnValue('getAllSamples');
hhs = hh.invokeSolverMethodWithReturnValue('getAllSamples');
iis = ii.invokeSolverMethodWithReturnValue('getAllSamples');
jjs = jj.invokeSolverMethodWithReturnValue('getAllSamples');

ac = repmat(pairsToComplex(as), 1, N);
arc = repmat(ars, 1, N);
bc = cell2mat(cellfun(@(x)pairsToComplex(x), bs, 'UniformOutput', false));
btc = cell2mat(cellfun(@(x)pairsToComplex(x), bts, 'UniformOutput', false).').';
brc = cell2mat(brs);
cc = cell2mat(cellfun(@(x)pairsToComplex(x), cs, 'UniformOutput', false));
dc = repmat(pairsToComplex(ds), 1, N);
ec = cell2mat(cellfun(@(x)pairsToComplex(x), es, 'UniformOutput', false));
fc = cell2mat(cellfun(@(x)pairsToComplex(x), fs, 'UniformOutput', false));
gc = repmat(pairsToComplex(gs), 1, N);
hc = cell2mat(cellfun(@(x)pairsToComplex(x), hs, 'UniformOutput', false));
ic = cell2mat(cellfun(@(x)pairsToComplex(x), is, 'UniformOutput', false));
jc = cell2mat(cellfun(@(x)pairsToComplex(x), js, 'UniformOutput', false));
kc = cell2mat(cellfun(@(x)pairsToComplex(x), ks, 'UniformOutput', false));
lc = cell2mat(cellfun(@(x)pairsToComplex(x), ls, 'UniformOutput', false));
mc = cell2mat(cellfun(@(x)pairsToComplex(x), ms, 'UniformOutput', false));
nc = cell2mat(cellfun(@(x)pairsToComplex(x), ns, 'UniformOutput', false));
oc = cell2mat(cellfun(@(x)pairsToComplex(x), os, 'UniformOutput', false));
pc = cell2mat(cellfun(@(x)pairsToComplex(x), ps, 'UniformOutput', false));
qc = cell2mat(cellfun(@(x)pairsToComplex(x), qs, 'UniformOutput', false));
rc = cell2mat(cellfun(@(x)pairsToComplex(x), rs, 'UniformOutput', false));
sc = cell2mat(cellfun(@(x)pairsToComplex(x), ss, 'UniformOutput', false).').';
tc = cell2mat(cellfun(@(x)pairsToComplex(x), ts, 'UniformOutput', false));
uc = repmat(pairsToComplex(us), 1, N);
vc = repmat(pairsToComplex(vs), 1, N);
wc = cell2mat(cellfun(@(x)pairsToComplex(x), ws, 'UniformOutput', false));
xc = cell2mat(cellfun(@(x)pairsToComplex(x), xs, 'UniformOutput', false));
yc = cell2mat(cellfun(@(x)pairsToComplex(x), ys, 'UniformOutput', false));
zc = cell2mat(cellfun(@(x)pairsToComplex(x), zs, 'UniformOutput', false));
aac = cell2mat(cellfun(@(x)pairsToComplex(x), aas, 'UniformOutput', false));
bbc = cell2mat(cellfun(@(x)pairsToComplex(x), bbs, 'UniformOutput', false));
ccc = cell2mat(cellfun(@(x)pairsToComplex(x), ccs, 'UniformOutput', false));
ddc = cell2mat(cellfun(@(x)pairsToComplex(x), dds, 'UniformOutput', false));
eec = cell2mat(cellfun(@(x)pairsToComplex(x), ees, 'UniformOutput', false));
ffc = cell2mat(cellfun(@(x)pairsToComplex(x), ffs, 'UniformOutput', false));
ggc = cell2mat(cellfun(@(x)pairsToComplex(x), ggs, 'UniformOutput', false));
hhc = cell2mat(cellfun(@(x)pairsToComplex(x), hhs, 'UniformOutput', false));
iic = cell2mat(cellfun(@(x)pairsToComplex(x), iis, 'UniformOutput', false));
jjc = cell2mat(cellfun(@(x)pairsToComplex(x), jjs, 'UniformOutput', false));

% Compare results
assertElementsAlmostEqual(dc, ac + 1+2i, 'absolute');            
assertElementsAlmostEqual(ec, bc + 1+2i, 'absolute');            
assertElementsAlmostEqual(fc, ac - bc, 'absolute');            
assertElementsAlmostEqual(gc, 1+2i + ac, 'absolute');            
assertElementsAlmostEqual(hc, 1+2i + bc, 'absolute');            
assertElementsAlmostEqual(ic, bc - ac, 'absolute');            
assertElementsAlmostEqual(jc, ac .* bc, 'absolute');            
assertElementsAlmostEqual(kc, bc .* ac, 'absolute');            
assertElementsAlmostEqual(lc, ac .* bc, 'absolute');            
assertElementsAlmostEqual(mc, bc .* ac, 'absolute');            
assertElementsAlmostEqual(nc, bc .* cc, 'absolute');            
assertElementsAlmostEqual(oc, bc ./ ac, 'absolute');            
assertElementsAlmostEqual(pc, ac ./ bc, 'absolute');            
assertElementsAlmostEqual(qc, bc ./ ac, 'absolute');            
assertElementsAlmostEqual(rc, bc ./ cc, 'absolute');            
assertElementsAlmostEqual(sc, bc', 'absolute');            
assertElementsAlmostEqual(tc, btc', 'absolute');         
assertElementsAlmostEqual(uc, ac - arc, 'absolute');            
assertElementsAlmostEqual(vc, arc + ac, 'absolute');            
assertElementsAlmostEqual(wc, bc - brc, 'absolute');            
assertElementsAlmostEqual(xc, brc + bc, 'absolute');    
assertElementsAlmostEqual(yc, ac .* brc, 'absolute');            
assertElementsAlmostEqual(zc, bc .* arc, 'absolute');            
assertElementsAlmostEqual(aac, arc .* bc, 'absolute');            
assertElementsAlmostEqual(bbc, brc .* ac, 'absolute');
assertElementsAlmostEqual(ccc, bc ./ arc, 'absolute');            
assertElementsAlmostEqual(ddc, ac ./ brc, 'absolute');            
assertElementsAlmostEqual(eec, bc ./ arc, 'absolute');            
assertElementsAlmostEqual(ffc, bc ./ brc, 'absolute');            
assertElementsAlmostEqual(ggc, brc ./ ac, 'absolute');            
assertElementsAlmostEqual(hhc, arc ./ bc, 'absolute');            
assertElementsAlmostEqual(iic, brc ./ ac, 'absolute');            
assertElementsAlmostEqual(jjc, brc ./ bc, 'absolute');            

end


% Test multi-dimensional complex variables
function test4(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a22 = Complex(2);
b22 = Complex(2);
a23 = Complex(2,3);
b23 = Complex(2,3);
a234 = Complex(2,3,4);
b234 = Complex(2,3,4);

c22 = a22 + b22;
c23 = a23 + b23;
c234 = a234 + b234;

fg.addFactorVectorized('ComplexSum', c22, a22, b22);
fg.addFactorVectorized('ComplexSum', c23, a23, b23);
fg.addFactorVectorized('ComplexSum', c234, a234, b234);

for x=1:2
    for y=1:2
        a22(x,y).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
        b22(x,y).Input = {FactorFunction('Normal',2,5), FactorFunction('Normal',-2,1)};
        c22(x,y).Input = {FactorFunction('Normal',1,5), FactorFunction('Normal',-3,1)};
    end
end

for x=1:2
    for y=1:3
        a23(x,y).Input = {FactorFunction('Normal',4,5), FactorFunction('Normal',-2,1)};
        b23(x,y).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-3,1)};
        c23(x,y).Input = {FactorFunction('Normal',2,5), FactorFunction('Normal',-4,1)};
    end
end

for x=1:2
    for y=1:3
        for z = 1:4
            a234(x,y).Input = {FactorFunction('Normal',5,5), FactorFunction('Normal',-3,1)};
            b234(x,y).Input = {FactorFunction('Normal',4,5), FactorFunction('Normal',-4,1)};
            c234(x,y).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-5,1)};
        end
    end
end

        
fg.Solver.setNumSamples(20);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

a22s = a22.invokeSolverMethodWithReturnValue('getAllSamples');
b22s = b22.invokeSolverMethodWithReturnValue('getAllSamples');
c22s = c22.invokeSolverMethodWithReturnValue('getAllSamples');
a23s = a23.invokeSolverMethodWithReturnValue('getAllSamples');
b23s = b23.invokeSolverMethodWithReturnValue('getAllSamples');
c23s = c23.invokeSolverMethodWithReturnValue('getAllSamples');
a234s = a234.invokeSolverMethodWithReturnValue('getAllSamples');
b234s = b234.invokeSolverMethodWithReturnValue('getAllSamples');
c234s = c234.invokeSolverMethodWithReturnValue('getAllSamples');

a22c = cell2mat(cellfun(@(x)pairsToComplex(x), a22s, 'UniformOutput', false));
b22c = cell2mat(cellfun(@(x)pairsToComplex(x), b22s, 'UniformOutput', false));
c22c = cell2mat(cellfun(@(x)pairsToComplex(x), c22s, 'UniformOutput', false));
a23c = cell2mat(cellfun(@(x)pairsToComplex(x), a23s, 'UniformOutput', false));
b23c = cell2mat(cellfun(@(x)pairsToComplex(x), b23s, 'UniformOutput', false));
c23c = cell2mat(cellfun(@(x)pairsToComplex(x), c23s, 'UniformOutput', false));
a234c = cell2mat(cellfun(@(x)pairsToComplex(x), a234s, 'UniformOutput', false));
b234c = cell2mat(cellfun(@(x)pairsToComplex(x), b234s, 'UniformOutput', false));
c234c = cell2mat(cellfun(@(x)pairsToComplex(x), c234s, 'UniformOutput', false));

assertElementsAlmostEqual(c22c, a22c + b22c, 'absolute');            
assertElementsAlmostEqual(c23c, a23c + b23c, 'absolute');            
assertElementsAlmostEqual(c234c, a234c + b234c, 'absolute');            

end


% Utility function - convert column pairs to complex numbers
function c = pairsToComplex(s)
c = s(:,1) + 1i*s(:,2);
end




% Test bounded complex domains
function test5(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

minr = -1;
maxr = 1;
mini = -0.5;
maxi = 0.5;
dr = RealDomain(minr,maxr);
di = RealDomain(mini, maxi);
jd = RealJointDomain(dr,di);

a = Complex(jd);
b = Complex(jd);
a.Name = 'a';
b.Name = 'b';

c = a + b;

a.Input = {FactorFunction('Normal',.3,2), FactorFunction('Normal',-.1,2)};
b.Input = {FactorFunction('Normal',.4,2), FactorFunction('Normal',.6,2)};

fg.Solver.setNumSamples(1000);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = a.Solver.getAllSamples;
bs = b.Solver.getAllSamples;
cs = c.Solver.getAllSamples;
ac = pairsToComplex(as);
bc = pairsToComplex(bs);
cc = pairsToComplex(cs);

assertElementsAlmostEqual(cc, ac + bc, 'absolute');

% Check that samples of a and b don't exceed the bounds
assert(all(min(as) > [minr mini]));
assert(all(max(as) < [maxr maxi]));
assert(all(min(bs) > [minr mini]));
assert(all(max(bs) < [maxr maxi]));
assert(all(min(cs) > [2*minr 2*mini]));
assert(all(max(cs) < [2*maxr 2*maxi]));


end