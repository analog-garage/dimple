function testPLmrf()

    rand('seed',1);

    vertWeights = rand(2);
    horzWeights = rand(2);
    vertWeights = vertWeights / sum(vertWeights(:));
    horzWeights = horzWeights / sum(horzWeights(:));

    H = 2;
    W = 2;
    [fg,b,vertFT,horzFT] = buildMRF(H,W,vertWeights,horzWeights);

    BURN_INS = 10;
    NUM_SAMPLES = 1000;
    SCANS_PER_SAMPLE = 10;

    samples = collectSamples(fg,BURN_INS,NUM_SAMPLES,SCANS_PER_SAMPLE);

    %Learn the parameters
    [fg2,b2,vertFT2,horzFT2] = buildMRF(H,W,rand(2),rand(2));

    pl = PLLearner(fg2,{horzFT2,vertFT2},{b2});

    %Compare against numerical gradient
    pl.setData(samples);
    gradient = pl.calculateGradient();
    delta = 0.000001;

    num_gradient = zeros(2,4);
    for i = 1:2
        if i == 1
            ft = horzFT2;
        else
            ft = vertFT2;
        end
        for j = 1:4
            num_gradient(i,j) = pl.calculateNumericalGradient(ft, j,delta);
        end
    end
    l2 = norm(gradient-num_gradient);
    assertTrue(l2 < 1e-5);


    numSteps = 10000;
    scaleFactor = 0.03;
    
    pl.learn(numSteps,samples,scaleFactor);

    learnedHorzWeights = reshape(horzFT2.Weights,2,2);
    learnedHorzWeights = learnedHorzWeights / sum(learnedHorzWeights(:));
    learnedVertWeights = reshape(vertFT2.Weights,2,2);
    learnedHorzWeights = learnedHorzWeights / sum(learnedHorzWeights(:));


    assertTrue(norm(learnedHorzWeights-horzWeights) < 0.1);
    assertTrue(norm(vertWeights-learnedVertWeights) < 0.1);

    %Use gibbs sampling and expecation of random projections to see the
    %similarity
    N = 1000;
    samples1 = collectSamples(fg,BURN_INS,N,SCANS_PER_SAMPLE);
    samples2 = collectSamples(fg2,BURN_INS,N,SCANS_PER_SAMPLE);

    M = 5;
    A = rand(M,H*W);
    proj1 = sum(A * samples1',2) / N;
    proj2 = sum(A * samples2',2) / N;

    assertTrue(norm(proj1-proj2) < 0.05);
end