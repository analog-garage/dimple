function testParamEstimation()

    rand('seed',0);

    %Generate data
    N = 20;
    table = rand(2);
    xdata = rand(N,1) > 0.5;
    ydata = zeros(N,1);
    for i = 1:N
        dist = table(xdata(i)+1,:);
        ydata(i) = dist(1)/sum(dist) < rand();
    end

    x = Bit(N,1);
    y = Bit(N,1);

    ft = FactorTable(rand(2),x.Domain,y.Domain);
    fg = FactorGraph();
    fg.Solver.setSeed(0);
    delta = 1e-9;
    for i = 1:N
        fg.addFactor(ft,x(i),y(i));
        if xdata(i) == 0
            x(i).Input = delta;
        else
            x(i).Input = 1-delta;
        end

        if ydata(i) == 0
            y(i).Input = delta;
        else
            y(i).Input = 1-delta;
        end
    end

    numReEstimations = 20;
    numRestarts = 4;
    Epsilon = .1;

    fg.solve();

    fg.estimateParameters(ft,numRestarts,numReEstimations,Epsilon);

    golden = zeros(4,1);
    indices = ft.Indices;
    weights = ft.Weights;
    for i = 1:size(indices,1)
        golden(i) = table(indices(i,1)+1,indices(i,2)+1);
    end

    golden = golden/sum(golden);
    weights = weights/sum(weights);

    %golden
    %weights

    %golden-weights
    assertTrue(norm(golden-weights)<.2);
end

