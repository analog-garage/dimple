%clc

b = Bit(2,2);
v = Variable(1);
g = FactorGraph();

g.addFactor(@xorDelta,b(:,1),v);
g.addFactor(@xorDelta,b(:,2),v);
g.addFactor(@xorDelta,b(1,:),v);
g.addFactor(@xorDelta,b(2,:),v);

b(1).Input = .8;

g.Solver.setNumIterations(20);
g.solve();

disp(b.Belief);
