function testSpeed()

    N = 60;
    K = 2;

    fg = FactorGraph();
    vars = Variable(1:N,3,1);

    table = zeros(N^3,3);

    index = 1;
    for x = 1:N
        for y = 1:N
            for z = 1:N
                table(index,:) = [x y z]-1;
                index = index+1;
            end
        end
    end

    f = fg.addFactor(table,ones(size(table,1),1),vars);

    numIterations = 100;
    
    fg.Solver = 'MinSum';
    fg.NumIterations = numIterations;

    fg.solve();

    tic;
    fg.solve();
    minsumTime = toc;


    fg.Solver = 'minsum';
    fg.NumIterations = numIterations;

    f.Solver.setK(K);

    fg.solve();
    tic
    fg.solve();
    kbestTime = toc;

    kbestTime
    minsumTime
    assertTrue(kbestTime < minsumTime/2);
    %expectedRatio = K*K*N/N^3
    %actualRation = kbestTime/minsumTime
end
