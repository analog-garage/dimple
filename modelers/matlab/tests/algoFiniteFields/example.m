tic

setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
fg = FactorGraph();

primPoly = 2^7 + 2^1 + 2^0;
ff1 = FiniteFieldVariable(primPoly);
ff2 = FiniteFieldVariable(primPoly);
ff3 = FiniteFieldVariable(primPoly);

%TODO: do I want this to take a number as an argument as well?
fg.addFactor(@finiteFieldMult,1,ff2,ff3);
%fg.addFactor(@finiteFieldAdd,ff1,ff2,ff3);

%priors1 = zeros(128,1);
%priors1(2) = 1;

priors2 = zeros(128,1);
priors2(3) = 1;

%ff1.Input = priors1;
ff2.Input = priors2;

fg.Solver.setNumIterations(1);
fg.solve();
ff3.Value
toc
