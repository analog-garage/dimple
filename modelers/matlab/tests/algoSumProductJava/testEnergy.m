%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

function testEnergy()
    rand('seed',1);
    
    fg = FactorGraph();
    b = Discrete({0,1},2,1);
    f1 = fg.addFactor([1 2; 3 4],b(1),b(2));
    f2 = fg.addFactor([3 2; 5 4],b(1),b(2));
    f3 = fg.addFactor([6 2; 7 4],b(1),b(2));
    b(1).Input = [.1 .9];
    b(2).Input = [.2 .8];

    fg.solve();

    bfe = fg.BetheFreeEnergy;
    internalEnergy = fg.InternalEnergy;
    bfentropy = fg.BetheEntropy;

    f1Entropy = f1.BetheEntropy;
    f1Energy = f1.InternalEnergy;

    f2Entropy = f2.BetheEntropy;
    f2Energy = f2.InternalEnergy;

    f3Entropy = f3.BetheEntropy();
    f3Energy = f3.InternalEnergy();

    b1Entropy = b(1).BetheEntropy();
    b1Energy = b(1).InternalEnergy();

    b2Entropy = b(2).BetheEntropy();
    b2Energy = b(2).InternalEnergy();

    % Bethe Free Energy = Internal Energy - Bethe Entropy
    assertElementsAlmostEqual(bfe,internalEnergy-bfentropy);

    % InternalEnergy = sum of variable and factor internal entropy
    assertElementsAlmostEqual(internalEnergy,f1Energy+f2Energy+f3Energy+b1Energy+b2Energy);

    % Bethe Entropy = sum of factor enropy - sum of ( variable entropy * (degree -1) )
    assertElementsAlmostEqual(bfentropy, f1Entropy+f2Entropy+f3Entropy - b1Entropy * 2 - b2Entropy * 2);

    %Test Factor Energy -> sum (factor beliefs * (-log (factor weight)))
    f1b = f1.Belief();
    f1values = f1.FactorTable.Weights;
    assertElementsAlmostEqual(f1Energy,sum(f1b .* (-log(f1values))));

    %Test Variable Energy
    b1b = b(1).Belief;
    assertElementsAlmostEqual(b1Energy,sum(b1b .* (-log(b(1).Input))));

    %Test Factor Bethe Entropy
    f1b = f1.Solver.getBelief();
    assertElementsAlmostEqual(f1Entropy,-sum(f1b .* log(f1b)));

    %Test Variable Bethe Entropy
    b1b = b(1).Belief;
    assertElementsAlmostEqual(b1Entropy,-sum(b1b .* log(b1b)));

    %% The following tests confirm that the BetheFreeEnergy is equal 
    %  to the -log(Z) where Z is the partition function.  The partition
    %  function is the sum over all models P(Observation|Model)*P(Model)
    
    %First we try a really simple case
    fg = FactorGraph();
    x = Bit();
    y = Bit();
    fg.addFactor(@(x,y) x == y,x,y);
    x.Input = .8;
    y.Input = 1;
    fg.solve();
    actual = -log(1*.8 + 0*.2);
    assertElementsAlmostEqual(fg.BetheFreeEnergy,actual);


    %Same simple case with soft factor.
    fg = FactorGraph();
    x = Bit();
    y = Bit();
    fg.addFactor(@(x,y) x+y+2,x,y);
    x.Input = .7;
    y.Input = 1;
    fg.solve();
    actual = -log(.7 * 4 + .3 * 3);
    assertElementsAlmostEqual(fg.BetheFreeEnergy,actual);

    %2 Node HMM
    fg = FactorGraph();
    x = Bit(2,1);
    y = Bit(2,1);
    transitionMatrix = rand(2);
    emissionMatrix = rand(2);
    transition = FactorTable(transitionMatrix,x.Domain,x.Domain);
    emission = FactorTable(emissionMatrix,x.Domain,y.Domain);
    fg.addFactor(transition,x(1),x(2));
    fg.addFactor(emission,x(1),y(1));
    fg.addFactor(emission,x(2),y(2));
    x(1).Input = rand();
    x(2).Input = rand();
    y(1).Input = .99999999;
    y(2).Input = .99999999;
    fg.solve();
    estimate = -log(x(1).Input(1)*x(2).Input(1)*emissionMatrix(1,2)^2*transitionMatrix(1,1) + ...
         x(1).Input(1)*x(2).Input(2)*emissionMatrix(1,2)*emissionMatrix(2,2)*transitionMatrix(1,2) + ...
         x(1).Input(2)*x(2).Input(1)*emissionMatrix(1,2)*emissionMatrix(2,2)*transitionMatrix(2,1) + ...
         x(1).Input(2)*x(2).Input(2)*emissionMatrix(2,2)*emissionMatrix(2,2)*transitionMatrix(2,2));

    assertElementsAlmostEqual(fg.BetheFreeEnergy,estimate);

    %Bigger HMM with observations randomized.
    N = 5;
    fg = FactorGraph();
    x = Bit(N,1);
    y = Bit(N,1);
    transitionMatrix = rand(2);
    emissionMatrix = rand(2);
    transition = FactorTable(transitionMatrix,x.Domain,x.Domain);
    emission = FactorTable(emissionMatrix,x.Domain,y.Domain);
    for i = 2:N
       fg.addFactor(transition,x(i-1),x(i));
    end
    for i = 1:N
        fg.addFactor(emission,x(i),y(i));
    end
    x.Input = rand(N,1);
    yInputs = rand(N,1) > 0.5;
    y.Input = yInputs;
    fg.solve();

    %calculate partition function
    estimate = 0;
    %for every combination of hidden states
    for i = 0:(2^N-1)
        states = dec2bin(i,N)-'0';
        tmp = 1;
        %multiply priors
        for j = 1:N
           tmp = tmp*x(j).Input(states(j)+1); 
        end
        %multiply emission probabilities
        for j = 1:N
           tmp = tmp * emissionMatrix(states(j)+1,(yInputs(j)>.5) + 1); 
        end
        %multiply transition proabilities
        for j = 2:N
           tmp = tmp * transitionMatrix(states(j-1)+1,states(j)+1); 
        end
        estimate = estimate + tmp;
    end
    estimate = -log(estimate);
    assertElementsAlmostEqual(fg.BetheFreeEnergy,estimate);

end
