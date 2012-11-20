function testBaumWelch()
    
    rand('state',0');

    %Hidden states
    H = 2;
    %Observed states
    O = 2;
    %Transition Matrix
    TM = rand(H);
    TM = [0.1 0.9; ...
          0.9 0.1];
    %Emission Matrix
    E = rand(H,O);
    E = [0.1 0.9; ...
         0.9 0.1];

    %Initial distribtuion
    Pi = rand(H,1);
    %Num samples
    N = 50;

    %Generate data
    x = zeros(N,1);
    y = zeros(N,1);

    x(1) = Pi(1)/sum(Pi) < rand();
    for i = 2:N
       dist = TM(x(i-1)+1,:);
       x(i) = dist(1)/sum(dist) < rand();
    end
    for i = 1:N
       dist = E(x(i)+1,:);
       y(i) = dist(1)/sum(dist) < rand();
    end

    %create Factor Graph
    fg = FactorGraph();
    fg.Solver.setSeed(0);
    xv = Bit(N,1);
    yv = Bit(N,1);
    piFT = FactorTable([0; 1],[.5 .5]',xv(1).Domain);
    fg.addDirectedFactor(piFT,{xv(1)},{xv(1)});
    tFT = FactorTable(rand(2),xv.Domain,xv.Domain);
    tFT.normalize(1);

    fg.addFactorVectorized(tFT,xv(1:(end-1)),xv(2:end)).DirectedTo = xv(2:end);
    eFT = FactorTable(rand(H,O),xv.Domain,yv.Domain);
    eFT.normalize(1);
    fg.addFactorVectorized(eFT,xv,yv).DirectedTo = yv;
    

    %Set data
    delta = 1e-9;
    input = y-2*delta*y + delta;
    yv.Input = input;
    input2 = x-2*delta*x + delta;
    xv.Input = input2;

    %Estimate parameters
    numReEstimations = 20;
    numRestarts = 20;

    fg.solve();
    bfe1 = fg.BetheFreeEnergy;
    epsilon = 1e-6;
    weights = tFT.Weights;
    weights(1) = weights(1)+epsilon;
    tFT.Weights = weights;
    fg.solve();
    bfe2 = fg.BetheFreeEnergy;
    numeric = (bfe2-bfe1)/epsilon;
    derivative = fg.Solver.calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(tFT.ITable.getModelerObject(),0);

    fg.baumWelch({tFT,eFT,piFT},numRestarts,numReEstimations);

    
    compareStuff(tFT,TM);
    compareStuff(eFT,E);
   
end

function compareStuff(ft,t)
    
    golden = zeros(4,1);
    indices = ft.Indices;
    weights = ft.Weights;
    for i = 1:size(indices,1)
        golden(i) = t(indices(i,1)+1,indices(i,2)+1);
    end

    golden = golden/sum(golden);
    weights = weights/sum(weights);
    diff = golden-weights;
    assertTrue(norm(diff)<.1);
end
