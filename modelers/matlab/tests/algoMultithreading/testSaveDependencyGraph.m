function testSaveDependencyGraph()

    fg = FactorGraph();

    b = Bit(2,1);
    f = fg.addFactor(@xorDelta,b);
    b(1).Name = 'b1';
    b(2).Name = 'b2';
    f.Name = 'f';

    fg.Solver.getMultithreadingManager().getDependencyGraph().createDotFile('small.dot');
    assertTrue(compareFiles('small.dot','small.dot.golden'));

    fg = FactorGraph();
    fg.Scheduler = 'TreeOrSequentialScheduler';
    N = 5;
    b = Bit(N,N);
    for i = 1:N
        for j = 1:N
            b(i,j).Name = sprintf('B%d_%d',i,j);
        end
    end


    f1 = fg.addFactorVectorized(@(a,b) rand(),b(:,1:end-1),b(:,2:end));
    f2 = fg.addFactorVectorized(@(a,b) rand(),b(1:end-1,:),b(2:end,:));

    for i=1:size(f1,1)
        for j = 1:size(f1,2)
            f1(i,j).Name = sprintf('fh%d_%d',i,j);
        end
    end

    for i=1:size(f2,1)
        for j = 1:size(f2,2)
            f2(i,j).Name = sprintf('fv%d_%d',i,j);
        end
    end

    fg.Solver.getMultithreadingManager().getDependencyGraph().createDotFile('big.dot');
    assertTrue(compareFiles('big.dot','big.dot.golden'));
end
