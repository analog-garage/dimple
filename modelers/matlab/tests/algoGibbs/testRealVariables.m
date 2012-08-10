function testRealVariables()

debugPrint = false;

dtrace(debugPrint, '++testRealVariables');

% Test 1 - only real variables


setSolver(com.analog.lyric.dimple.solvers.gibbs.Solver());
graph = FactorGraph();


% Scalar variables

a = Real();
b = Real([-1,1]);
c = Real(com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1));
d = Real([-1.1,1.1], com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1));
e = Real([0,Inf]);

assert(a.Domain.LB == -Inf);
assert(a.Domain.UB == Inf);
assert(b.Domain.LB == -1);
assert(b.Domain.UB == 1);
assert(c.Domain.LB == -Inf);
assert(c.Domain.UB == Inf);
assert(d.Domain.LB == -1.1);
assert(d.Domain.UB == 1.1);
assert(e.Domain.LB == 0);
assert(e.Domain.UB == Inf);


dtrace(debugPrint, ['c.Input.eval(0): ' num2str(c.Input.eval(0))]);
assertElementsAlmostEqual(c.Input.eval(0), 1.0);

dtrace(debugPrint, ['d.Input.eval(1): ' num2str(d.Input.eval(1))]);
assertElementsAlmostEqual(d.Input.eval(1), exp(-1));

assert(isempty(a.Input));
a.Input = com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1);
dtrace(debugPrint, ['a.Input.eval(0): ' num2str(a.Input.eval(0))]);
assertElementsAlmostEqual(a.Input.eval(0), 1.0);


% Arrays

d14 = Real([-2,2], com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1),1,4);
d41 = Real([-3,3], com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1),4,1);
d45 = Real([-4,4], com.analog.lyric.dimple.FactorFunctions.SimpleNormal(0,1),4,5);

assert(d14.Domain.LB == -2);
assert(d14.Domain.UB == 2);
assert(d41.Domain.LB == -3);
assert(d41.Domain.UB == 3);
assert(d45.Domain.LB == -4);
assert(d45.Domain.UB == 4);

dtrace(debugPrint, ['d14.Input{1,4}.eval(1): ' num2str(d14.Input{1,4}.eval(1))]);
assertElementsAlmostEqual(d14.Input{1,4}.eval(1), exp(-1));
assertElementsAlmostEqual(d14.Input{4}.eval(1), exp(-1));
dtrace(debugPrint, ['d41.Input{4,1}.eval(1): ' num2str(d41.Input{4,1}.eval(1))]);
assertElementsAlmostEqual(d41.Input{4,1}.eval(1), exp(-1));
assertElementsAlmostEqual(d41.Input{4}.eval(1), exp(-1));
dtrace(debugPrint, ['d45.Input{4,5}.eval(1): ' num2str(d45.Input{4,5}.eval(1))]);
assertElementsAlmostEqual(d45.Input{4,5}.eval(1), exp(-1));


d45.Input = com.analog.lyric.dimple.FactorFunctions.SimpleNormal(10,1);
dtrace(debugPrint, ['d45.Input{4,5}.eval(10) (mean 10): ' num2str(d45.Input{4,5}.eval(10))]);
assertElementsAlmostEqual(d45.Input{4,5}.eval(10), 1);


dtrace(debugPrint, '--testRealVariables');

end


