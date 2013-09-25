
a = Bit();
b = Bit();
ng = FactorGraph(a,b);
ng.addFactor(@(a,b) rand(),a,b);

fg = FactorGraph();
%fg.Scheduler = 'TreeOrFloodingScheduler';


b = Bit(3,1);
fg.addFactor(ng,b(1),b(2));
fg.addFactor(ng,b(2),b(3));
fg.addFactor(@(a,b) rand(), b(1),b(2));

fg.VectorObject.getModelerNode(0).getSchedule()


b.Input = rand(3,1);

fg.solve();
x = b.Belief

fg.Solver.setNumThreads(16);
fg.Solver.setMultiThreadMode(0);
fg.solve();
y = b.Belief

