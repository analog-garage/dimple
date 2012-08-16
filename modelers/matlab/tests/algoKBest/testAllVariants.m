function testAllVariants()

    %In this text we vary:
    %1) minsum vs. sumproduct
    %2) matlab factor function vs. java factor function 

    javaFF = com.analog.lyric.dimple.FactorFunctions.SumOfInputs();
    matlabFF = @(x) sum(x);
    factorFunctions = {javaFF,matlabFF};
    solvers = {'minsum','sumproduct'};

    %Set K to 3
    K = 3;
    %Domain size to 10
    D = 10;
    %Number of vars to 3
    NumVars = 2;


    for ffIndex = 1:length(factorFunctions)
        factorFunction = factorFunctions{ffIndex};

        for solverIndex = 1:length(solvers)
            solver = solvers{solverIndex};
            setSolver(solver);

            %First we run this with kbest
            kfg = FactorGraph();
            kvars = Discrete(1:D,NumVars,1);
            kf = kfg.addFactor(factorFunction,kvars);
            kf.Solver.setK(K);
            kvars.Input = fliplr(1:D);
            kfg.solve();

            %Now, we do the same thing without kbest and compare
            fg = FactorGraph();
            vars = Discrete(1:D,NumVars,1);
            f = fg.addFactor(factorFunction,vars);

            %If we set only the kbest inputs to their values and everything else to 0,
            %this should be equivalent to kbest.
            vars.Input = fliplr([zeros(1,D-K) (1:K)+D-K]);
            fg.solve();
            vars.Input = fliplr(1:D);

            diff = kvars.Belief-vars.Belief;
            assertTrue(norm(diff(:)) < 1e-13);

            %Now make sure they're different
            vars.Input = fliplr(1:D);
            fg.solve();

            diff = kvars.Belief-vars.Belief;
            assertTrue(norm(diff(:)) > .001);
        end
    end
end

