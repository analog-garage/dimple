function testIncrementalUpdates()

debugPrint = false;
repeatable = true;


dtrace(debugPrint, '++testIncrementalUpdates');

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testIncrementalUpdates');

end


function test1(debugPrint, repeatable)

fg = FactorGraph;
fg.Solver = 'Gibbs';

v1 = Normal(0,10,[1,4]);
v2 = Normal(0,10,[1,4]);
p = v1 * v2;    % Deterministic inner product factor
x = Normal(p, 1);
x.FixedValue = 0;

fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();

fg.Solver.setNumSamples(50);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

scores = fg.Solver.getAllScores;
assert(all(isfinite(scores)));              % None of the scores should be infinite

end