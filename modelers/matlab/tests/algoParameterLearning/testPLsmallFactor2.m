function testPLsmallFactor2()

    rand('seed',1);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % First test simply checks to see that the parameters are learned
    % correctly
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    fg = FactorGraph();
    b = Bit(2,1);
    weights = rand(2);
    weights = weights / sum(weights(:));
    fg.addFactor(weights,b);
    fg.Solver = 'gibbs';
    fg.Solver.setBurnInScans(10);
    fg.Solver.setNumSamples(1000);
    fg.Solver.setScansPerSample(2);
    fg.Solver.saveAllSamples();
    fg.Solver.setSeed(1);
    fg.Solver.saveAllScores();
    fg.solve();
    b1samples = cell2mat(cell(b(1).Solver.getAllSamples()));
    b2samples = cell2mat(cell(b(2).Solver.getAllSamples()));
    samples = [b1samples b2samples];
    imperical_weights = zeros(2);

    %plot(fg.Solver.getAllScores());

    for i = 1:size(samples,1)
        ind = sub2ind([2 2],samples(i,1)+1,samples(i,2)+1);
        imperical_weights(ind) = imperical_weights(ind) + 1;
    end

    imperical_weights = imperical_weights / sum(imperical_weights(:));

    fg2 = FactorGraph();
    b2 = Bit(2,1);
    f = fg2.addFactor(rand(2),b2);

    pl = PLLearner(fg2,{f.FactorTable},{b2});

    numSteps = 2000;
    scaleFactor = 0.05;
    pl.learn(numSteps,samples,scaleFactor);

    learned_weights = reshape(f.FactorTable.Weights,2,2);

    l2 = norm(learned_weights-imperical_weights);
    assertTrue(l2 < 1e-8);
end
