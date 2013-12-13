function testDiscreteSamplers()

debugPrint = false;
repeatable = true;


dtrace(debugPrint, '++testDiscreteSamplers');

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testDiscreteSamplers');

end


function test1(debugPrint, repeatable)

samplingTest([], debugPrint, repeatable);
samplingTest('MHSampler', debugPrint, repeatable);
samplingTest('SuwaTodoSampler', debugPrint, repeatable);

end

function samplingTest(sampler, debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';
if (~isempty(sampler))
    fg.Solver.setDefaultDiscreteSampler(sampler);
else
    sampler = 'CDFSampler';
end

dDomainSize = 5;
d = Discrete(1:dDomainSize);
b = Bit;

di = rand(1,dDomainSize);
di = di/sum(di);
bi = rand;
d.Input = di;
b.Input = bi;

fg.addFactor(@(x)1, d);
fg.addFactor(@(x)1, b);

assert(strcmp(d.Solver.getSamplerName, sampler));
assert(strcmp(b.Solver.getSamplerName, sampler));

fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

numSamples = 10000;
fg.Solver.setNumSamples(numSamples);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

ds = d.Solver.getAllSampleIndices();
bs = b.Solver.getAllSampleIndices();

df = sum(bsxfun(@eq,ds,0:(dDomainSize-1)))/numSamples;
bf = sum(bs)/numSamples;

if (0)
    figure(1); hold off; plot(di,df,'o'); hold on; plot([0 1],[0 1],'g');
    figure(2); hold off; plot(bi,bf,'o'); hold on; plot([0 1],[0 1],'g');
end

assertElementsAlmostEqual(sum(abs(df-di)), 0, 'absolute', 0.02);
assertElementsAlmostEqual(abs(bf-bi), 0, 'absolute', 0.02);

end