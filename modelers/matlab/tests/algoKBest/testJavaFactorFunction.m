%function testJavaFactorFunction()

    domainSize = 20;
    numVars = 3;
    k = 3;
    

    %Create some inputs
    inputs = rand(numVars,domainSize);

    %% First we solve with the kbestminsumsolver
    setSolver('minsum');

    %We retrieve the java vactor function
    ff = com.analog.lyric.dimple.FactorFunctions.FactorFunctionForTesting();

    %Create the factor graph
    fg = FactorGraph();
    vars = Discrete(1:domainSize,numVars,1);
    f = fg.addFactor(ff,vars);

    %Set the inputs
    vars.Input = inputs;
    f.Solver.setK(k);

    %Solve
    fg.solve();

    %Make sure 
    assertTrue(f.Solver.getFactorFunction().factorTableExists(f.Solver.getFactor().getDomains())==0);

    %Retrieve beliefs
    belief = vars.Belief;


    %% Now we run with the minsum solver to compare

    %Create graph
    fg2 = FactorGraph();
    vars2 = Discrete(1:domainSize,numVars,1);
    f2 = fg2.addFactor(ff,vars2);

    %zero out some of the inputs to match kbest
    inputs2 = inputs;

    if k < size(inputs,2)
        for v = 1:numVars
            input = inputs(v,:);
            sinput = fliplr(sort(input));
            kbest = sinput(k);
            inputs2(v,:) = input .* (input >= kbest);
        end
    end

    %Set inputs
    vars2.Input = inputs2;

    %solve
    fg2.solve();

    %Now we reset the inputs to the original values so beliefs 
    vars2.Input = inputs;
    belief2 = vars2.Belief;

    %TODO: why does minsum not normalize beliefs and kbestminsum does?
    belief2 = belief2 ./ repmat(sum(belief2,2),1,domainSize);

    %Compare the beliefs
    diff = belief2-belief;
    n = norm(diff(:));
    assertTrue(n<1e-13);
    
%end

