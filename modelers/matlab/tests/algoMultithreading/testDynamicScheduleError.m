function testDynamicScheduleError()

    N = 2;
    b = Bit(N);
    fg = FactorGraph();
    fg.Scheduler='RandomWithReplacementScheduler';
    fg.addFactorVectorized(@(a,b) rand(), b(:,1:end-1),b(:,2:end));
    fg.addFactorVectorized(@(a,b) rand(), b(1:end-1,:),b(2:end,:));
    b.Input = rand(N,N);
    fg.NumIterations = 5;    
    numThreads = 16;
    fg.Solver.setNumThreads(numThreads);

    try
        fg.solve();
    catch e
       assertTrue(~isempty(findstr(e.message,'Cannot currently create dependency graph of Dynamic Schedule')));
    end
end