function testPLsmallFactor()

    diffs = [1e-8 1e-3 1e-2];

    for B = 2:4

        rand('seed',1);

        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        % First test simply checks to see that the parameters are learned
        % correctly
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        fg = FactorGraph();

        dims = ones(1,B)*2;

        b = Bit(B,1);
        weights = rand(dims);
        weights = weights / sum(weights(:));
        fg.addFactor(weights,b);

        N = 1000;

        fg.Solver = 'gibbs';
        fg.Solver.setBurnInScans(10);
        fg.Solver.setNumSamples(N);
        fg.Solver.setScansPerSample(2);
        fg.Solver.saveAllSamples();
        fg.Solver.setSeed(1);
        fg.Solver.saveAllScores();
        fg.solve();

        samples = zeros(N,B);

        for i = 1:B
           samples(:,i) =  cell2mat(cell(b(i).Solver.getAllSamples()));
        end

        imperical_weights = zeros(dims);

        for i = 1:size(samples,1)
            inds = num2cell(samples(i,:)+1);
            ind = sub2ind(dims,inds{:});
            imperical_weights(ind) = imperical_weights(ind) + 1;
        end

        imperical_weights = imperical_weights / sum(imperical_weights(:));

        fg2 = FactorGraph();
        b2 = Bit(B,1);
        f = fg2.addFactor(rand(dims),b2);

        pl = PLLearner(fg2,{f.FactorTable},{b2});

        numSteps = 2000;
        scaleFactor = 0.05;
        pl.learn(numSteps,samples,scaleFactor);

        cdims = num2cell(dims);
        learned_weights = reshape(f.FactorTable.Weights,cdims{:});

        diff = learned_weights-imperical_weights;
        l2 = norm(diff(:));
        
        assertTrue(l2 < diffs(B-1));
    end
end
