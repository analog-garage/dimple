function testBetheFreeEnergyDerivative()
    %This function tests the derivative of the BetheFreeEnergy with respect
    %to a change in one of the Factor Tables.  The test numerically computes
    %the derivative and compares against Dimple.

    %Create an HMM FactorGraph

    rand('seed',0);

    SD = 2;
    OD = 2;
    N = 3;
    x = Discrete(1:SD,N,1);
    y = Discrete(1:OD,N,1);
    pi = FactorTable(((1:SD)-1)',rand(SD,1),x.Domain);
    t = FactorTable(rand(SD),x.Domain,x.Domain);
    e = FactorTable(rand(SD,OD),x.Domain,y.Domain);
    fg = FactorGraph();
    pif = fg.addFactor(pi,x(1));
    for i = 2:N
        tf = fg.addFactor(t,x(i-1),x(i));
    end
    for i = 1:N
        ef = fg.addFactor(e,x(i),y(i));
        y(i).Input = rand(OD,1);
        x(i).Input = rand(SD,1);
    end


    epsilon = 1e-9;

    %% Transition table

    %Go through all weights in the table
    for wn = 1:(SD^2)

        %Solve and calculate BetheFreeEnergy
        fg.solve();
        bfe1 = fg.BetheFreeEnergy;

        %Change the weight and recalculate BetheFree Energy
        weights = t.Weights;
        weights(wn) = weights(wn) + epsilon;
        t.Weights = weights;
        fg.solve();
        bfe2 = fg.BetheFreeEnergy;

        numericalDerivative = (bfe2-bfe1)/epsilon;
        derivative = fg.Solver.calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(t.ITable.getModelerObject(),wn-1);
        diff = abs(numericalDerivative-derivative);
        assertTrue(diff < 1e-5);
    end

    %% Emission table
    for wn = 1:(SD*OD)
        fg.solve();
        bfe1 = fg.BetheFreeEnergy;
        weights = e.Weights;
        weights(wn) = weights(wn) + epsilon;
        e.Weights = weights;
        %tf.FactorTable.Weights
        fg.solve();
        bfe2 = fg.BetheFreeEnergy;

        numericalDerivative = (bfe2-bfe1)/epsilon;
        derivative = fg.Solver.calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(e.ITable.getModelerObject(),wn-1);
        diff = abs(derivative-numericalDerivative);
        assertTrue(diff < 1e-5);
    end

    %% Pi table
    for wn = 1:(SD)
        fg.solve();
        bfe1 = fg.BetheFreeEnergy;
        weights = pi.Weights;
        weights(wn) = weights(wn) + epsilon;
        pi.Weights = weights;
        %tf.FactorTable.Weights
        fg.solve();
        bfe2 = fg.BetheFreeEnergy;

        numericalDerivative = (bfe2-bfe1)/epsilon;
        derivative = fg.Solver.calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(pi.ITable.getModelerObject(),wn-1);
        diff = abs(derivative-numericalDerivative);
        assertTrue(diff < 1e-5);
    end

end



