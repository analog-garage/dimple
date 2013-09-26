function testNested()

    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@(a,b) rand(),a,b);

    fg = FactorGraph();


    b = Bit(3,1);
    fg.addFactor(ng,b(1),b(2));
    fg.addFactor(ng,b(2),b(3));
    fg.addFactor(@(a,b) rand(), b(1),b(2));

    rand('seed',1);
    b.Input = rand(3,1);

    fg.solve();
    x = b.Belief;

    for i = 0:2
        fg.Solver.setNumThreads(16);
        fg.Solver.setMultiThreadMode(i);
        fg.solve();
        y = b.Belief;
        assertTrue(norm(x(:)-y(:))==0);
    end
end