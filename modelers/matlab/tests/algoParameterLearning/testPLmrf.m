%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Test Pseudolikelihood parameter estimation using an MRF.
function testPLmrf()

    %Make things repeatable
    rand('seed',1);
    verbose = 0;

    %%%%%%%%%%%%%%%%%%%%%%%%
    % Generate samples
    %%%%%%%%%%%%%%%%%%%%%%%%
    
    %Generate the weights
    vertWeights = rand(2);
    horzWeights = rand(2);
    vertWeights = vertWeights / sum(vertWeights(:));
    horzWeights = horzWeights / sum(horzWeights(:));

    %Build the MRF
    H = 3;
    W = 3;
    [fg,b,vertFT,horzFT] = buildMRF(H,W,vertWeights,horzWeights);

    %Set the parameters for sampling
    BURN_INS = 10;
    NUM_SAMPLES = 1000;
    SCANS_PER_SAMPLE = 10;
    
    %Collect the samples
    if verbose
        disp('collecting samples...');
    end
    samples = collectSamples(fg,BURN_INS,NUM_SAMPLES,SCANS_PER_SAMPLE);

    %%%%%%%%%%%%%%%%%%%%%%%%
    % Create the new graph and the learner
    %%%%%%%%%%%%%%%%%%%%%%%%
    
    %Build the new graph and randomize parameters
    [fg2,b2,vertFT2,horzFT2] = buildMRF(H,W,rand(2),rand(2));

    %Create a learner
    pl = PLLearner(fg2,{horzFT2,vertFT2},{b2});

    %%%%%%%%%%%%%%%%%%%%%%%%
    %Compare against numerical gradient
    %%%%%%%%%%%%%%%%%%%%%%%%
    
    %Set the data
    pl.setData(samples);
    
    %Calculat the gradient
    gradient = pl.calculateGradient();
    
    %Calculate the numerical gradient for all weights
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
    
    %Compare   
    l2 = norm(gradient-num_gradient);
    if verbose
       fprintf('difference between gradient and numerical gradient: %f\n',l2); 
    end
    assertTrue(l2 < 1e-5);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Learn the parameters
    %%%%%%%%%%%%%%%%%%%%%%%%%%%

    numSteps = 1000;
    scaleFactor = 0.03;
    
    if verbose
        disp('learning params...')
    end
    args.numSteps = numSteps;
    args.scaleFactor = scaleFactor;
    
    pl.learn(samples,args);

    learnedHorzWeights = reshape(horzFT2.Weights,2,2);
    learnedHorzWeights = learnedHorzWeights / sum(learnedHorzWeights(:));
    learnedVertWeights = reshape(vertFT2.Weights,2,2);
    learnedHorzWeights = learnedHorzWeights / sum(learnedHorzWeights(:));

    %Compare
    horzL2 = norm(learnedHorzWeights-horzWeights);
    vertL2 = norm(vertWeights-learnedVertWeights);
    
    if verbose
        fprintf('horzL2: %f, vertL2: %f\n',horzL2,vertL2);
    end

    assertTrue(horzL2 < 0.1);
    assertTrue(vertL2 < 0.1);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Use gibbs sampling and expecation of random projections to see the
    %similarity
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    if verbose
        disp('collecting samples...');
    end
    N = 1000;
    samples1 = collectSamples(fg,BURN_INS,N,SCANS_PER_SAMPLE);
    samples2 = collectSamples(fg2,BURN_INS,N,SCANS_PER_SAMPLE);

    M = 5;
    A = rand(M,H*W);
    proj1 = sum(A * samples1',2) / N;
    proj2 = sum(A * samples2',2) / N;
    l2 = norm(proj1-proj2);
    if verbose
        fprintf('diff of random projection: %f\n',l2);
    end
    assertTrue(l2 < 0.05);
end