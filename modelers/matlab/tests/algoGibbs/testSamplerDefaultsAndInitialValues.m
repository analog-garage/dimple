function testSamplerDefaultsAndInitialValues()

debugPrint = false;
repeatable = true;


dtrace(debugPrint, '++testSamplerDefaultsAndInitialValues');

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);

dtrace(debugPrint, '--testSamplerDefaultsAndInitialValues');

end



% Test setting default sampler
function test1(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

d = Discrete(1:5);
b = Bit;
r = Real;

fg.addFactor(@(x)1, d);
fg.addFactor(@(x)1, b);
fg.addFactor({'LogNormal',0,1}, r);

assert(strcmp(d.Solver.getSamplerName, 'CDFSampler'));
assert(strcmp(b.Solver.getSamplerName, 'CDFSampler'));
assert(strcmp(r.Solver.getSamplerName, 'SliceSampler'));

%----

fg = FactorGraph;
fg.Solver = 'Gibbs';

fg.Solver.setDefaultDiscreteSampler('SuwaTodoSampler');
fg.Solver.setDefaultRealSampler('MHSampler');

d = Discrete(1:5);
b = Bit;
r = Real;

fg.addFactor(@(x)1, d);
fg.addFactor(@(x)1, b);
fg.addFactor({'LogNormal',0,1}, r);

assert(strcmp(d.Solver.getSamplerName, 'SuwaTodoSampler'));
assert(strcmp(b.Solver.getSamplerName, 'SuwaTodoSampler'));
assert(strcmp(r.Solver.getSamplerName, 'MHSampler'));

d.Solver.setSampler('CDFSampler');
assert(strcmp(d.Solver.getSamplerName, 'CDFSampler'));
assert(strcmp(b.Solver.getSamplerName, 'SuwaTodoSampler'));
assert(strcmp(r.Solver.getSamplerName, 'MHSampler'));

fg.Solver.setDefaultDiscreteSampler('MHSampler');
fg.Solver.setDefaultRealSampler('SliceSampler');
assert(strcmp(d.Solver.getSamplerName, 'CDFSampler'));
assert(strcmp(b.Solver.getSamplerName, 'MHSampler'));
assert(strcmp(r.Solver.getSamplerName, 'SliceSampler'));

r.Solver.setSampler('MHSampler');
fg.Solver.setDefaultRealSampler('SliceSampler');
assert(strcmp(d.Solver.getSamplerName, 'CDFSampler'));
assert(strcmp(b.Solver.getSamplerName, 'MHSampler'));
assert(strcmp(r.Solver.getSamplerName, 'MHSampler'));

end



% Test initial values for discrete and real variables
function test2(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

d = Discrete(1:1000);
b = Bit;
q = Real;
r = Real;
s = Real([-1 1]);

fg.addFactor(@(x)1, d);
fg.addFactor(@(x)1, b);
fg.addFactor({'LogNormal',0,1}, q);
fg.addFactor({'Normal',0,100}, r);
r.Input = {'Normal',0,1};
fg.addFactor({'Normal',0,1}, s);

fg.Solver.setSeed(1);					% Make this repeatable

fg.Solver.setBurnInScans(0);
fg.initialize();

runs = 20;
ds = zeros(1,runs);
bs = zeros(1,runs);
qs = zeros(1,runs);
rs = zeros(1,runs);
ss = zeros(1,runs);
for i=1:runs
    fg.Solver.burnIn();
    ds(i) = d.Solver.getCurrentSampleIndex;
    bs(i) = b.Solver.getCurrentSampleIndex;
    qs(i) = q.Solver.getCurrentSample;
    rs(i) = r.Solver.getCurrentSample;
    ss(i) = s.Solver.getCurrentSample;
end
assert(all(ds(1) ~= ds(2:end)));
assert(any(bs(1) ~= bs(2:end)));
assert(all(qs == 0));   % Won't randomize this since there's no input or bound
assert(all(rs(1) ~= rs(2:end)));
assert(all(ss(1) ~= ss(2:end)));

% Now set the initial values; should use those instead of randomizing
d.Solver.setInitialSampleIndex(37);
b.Solver.setInitialSampleIndex(1);
q.Solver.setInitialSampleValue(3.8372);
r.Solver.setInitialSampleValue(-482.1991);
s.Solver.setInitialSampleValue(pi/7);

ds = zeros(1,runs);
bs = zeros(1,runs);
qs = zeros(1,runs);
rs = zeros(1,runs);
ss = zeros(1,runs);
for i=1:runs
    fg.Solver.burnIn();
    ds(i) = d.Solver.getCurrentSampleIndex;
    bs(i) = b.Solver.getCurrentSampleIndex;
    qs(i) = q.Solver.getCurrentSample;
    rs(i) = r.Solver.getCurrentSample;
    ss(i) = s.Solver.getCurrentSample;
end
assert(all(ds == 37));
assert(all(bs == 1));
assert(all(qs == 3.8372));
assert(all(rs == -482.1991));
assert(all(ss == pi/7));


% Now, burn-in as if it were the second re-start; should re-randomize when possible
ds = zeros(1,runs);
bs = zeros(1,runs);
qs = zeros(1,runs);
rs = zeros(1,runs);
ss = zeros(1,runs);
for i=1:runs
    fg.Solver.burnIn(1);
    ds(i) = d.Solver.getCurrentSampleIndex;
    bs(i) = b.Solver.getCurrentSampleIndex;
    qs(i) = q.Solver.getCurrentSample;
    rs(i) = r.Solver.getCurrentSample;
    ss(i) = s.Solver.getCurrentSample;
end
assert(all(ds(1) ~= ds(2:end)));
assert(any(bs(1) ~= bs(2:end)));
assert(all(qs == 3.8372));   % Keeps last value since can't re-randomize
assert(all(rs(1) ~= rs(2:end)));
assert(all(ss(1) ~= ss(2:end)));


end
