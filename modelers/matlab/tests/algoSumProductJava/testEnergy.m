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

    %Test Factor Energy -> sum (factor beliefs * log (factor weight))
    f1b = f1.Belief();
    f1values = f1.FactorTable.Weights;
    assertElementsAlmostEqual(f1Energy,sum(f1b .* log(f1values)));

    %Test Variable Energy
    b1b = b(1).Belief;
    assertElementsAlmostEqual(b1Energy,sum(b1b .* log(b(1).Input)));

    %Test Factor Bethe Entropy
    f1b = f1.Solver.getBelief();
    assertElementsAlmostEqual(f1Entropy,-sum(f1b .* log(f1b)));

    %Test Variable Bethe Entropy
    b1b = b(1).Belief;
    assertElementsAlmostEqual(b1Entropy,-sum(b1b .* log(b1b)));
end

