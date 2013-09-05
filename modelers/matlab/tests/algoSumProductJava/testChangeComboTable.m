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

function testChangeComboTable()

    %Create a Factor Grap
    fg = FactorGraph();
    
    %Create 6 bits
    b1 = Bit(3,1);
    b2 = Bit(3,1);
    
    %Create two factors that are independent from one another.
    %We do this so that we can show that changing one factor's
    %combo table affects the other factor.
    f1 = fg.addFactor(@xorDelta,b1);
    f2 = fg.addFactor(@xorDelta,b2);
    
    fg.Solver.setNumIterations(1);
    fg.solve();

    %First, make sure we get 50% for all variables.
    assertElementsAlmostEqual([.5 .5 .5]',b1.Belief);
    assertElementsAlmostEqual([.5 .5 .5]',b2.Belief);

    %Now we change the values
    newvals = [1 2 3 4]';
    f1.FactorTable.Weights = newvals;

    %Solve
    fg.solve();

    %Now we want to check that the result is correct
    indices = f1.FactorTable.Indices;
    
    %Since we've left the input as 50%, we can use a trick
    %where we multiply the values against the indices for each variable
    %to get the expected belief
    p0s = double(~indices)'* newvals;
    p1s = double(indices)'* newvals;
    total = p0s + p1s;
    p1s_normalized = p1s ./ total;

    %compare
    assertElementsAlmostEqual(p1s_normalized,b1.Belief);
    assertElementsAlmostEqual(p1s_normalized,b2.Belief);

    %Now we try changing the indices.  This is basically an inverted
    %xor.
    f1.FactorTable.change(~indices, f1.FactorTable.Weights);
    fg.solve();

    %we expect the probabilities to be inverted.
    assertElementsAlmostEqual(1-p1s_normalized,b1.Belief);
    assertElementsAlmostEqual(1-p1s_normalized,b2.Belief);


    %Try changing the combo table completely.  here we turn it
    %into an equals gate.
    f1.FactorTable.change([0 0 0; 1 1 1],[1 1]);
    b1(1).Input = .8;
    b2(1).Input = .8;
    
    fg.solve();
    
    assertElementsAlmostEqual([.8 .8 .8]',b1.Belief);
    assertElementsAlmostEqual([.8 .8 .8]',b2.Belief);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Let's test our ability to catch errors
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    %First we try this when the user sets the value vector to a bad length
    thrown = 0;
    try
        f1.FactorTable.Values = [1 2 3];
    catch exception
        thrown = 1;
    end
    assertTrue(thrown==1);

    %Next we try setting the indices to an incorrect length
    thrown = 0;
    try
        f1.FactorTable.Indices = ones(3,3);
    catch exception
        thrown = 1;
    end
    assertTrue(thrown==1);

    %Set indices to values that are too large for the domain lengths
    thrown = 0;
    try
        f1.FactorTable.Indices = ones(2,3)*2;
    catch exception
        thrown = 1;
    end
    assertTrue(thrown==1);

end
