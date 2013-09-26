function testFlooding()

    N = 10;

    fg = FactorGraph();
    b = Bit(N,N);
    fg.addFactorVectorized(@(a,b) rand(), b(:,1:end-1),b(:,2:end));
    fg.addFactorVectorized(@(a,b) rand(), b(1:end-1,:),b(2:end,:));
    b.Input = rand(N,N);
    fg.NumIterations = 10;
    fg.solve();
    x = b.Belief;

    for i = 0:2
       fg.Solver.setNumThreads(8);
       fg.Solver.setMultiThreadMode(i);
       fg.solve();
       y = b.Belief;
       assertTrue(norm(x(:)-y(:)) == 0);
    end
end