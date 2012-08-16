%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

    %Let's create a simple graph
    fg = FactorGraph();

    %We make three variables
    a = Variable({0,1});
    b = Variable({0,1});
    c = Variable({0,1});

    %We create a factor that enforces equality and puts some weight on
    %the variables being 0
    t = [0 0; 1 1];
    v = [3 2];

    table = fg.createTable(t,v,DiscreteDomain({0,1}),DiscreteDomain({0,1}));
    f1 = fg.addFactor(table,a,b);
    f2 = fg.addFactor(table,b,c);

    a.Input = [2 4];
    b.Input = [2 4];
    c.Input = [3 1];

    %Before we solve the energy should be infinite since c will pick a 
    %value of 0 and b will pick a value of 1 and f2 enforces equality.
    assertEqual(fg.Energy,Inf);

    fg.solve();

    %Now guess the energy and compare
    f1Energy = -log(3/3);
    f2Energy = -log(3/3);
    aEnergy = -log(2/4);
    bEnergy = -log(2/4);
    cEnergy = -log(3/3);
    guessEnergy = f1Energy+f2Energy+aEnergy+bEnergy+cEnergy;
    firstGuessEnergy = guessEnergy;
    assertElementsAlmostEqual(guessEnergy,fg.Energy);
    assertElementsAlmostEqual(f1.Energy,f1Energy);
    assertElementsAlmostEqual(f2.Energy,f2Energy);
    assertElementsAlmostEqual(a.Energy,aEnergy);
    assertElementsAlmostEqual(b.Energy,bEnergy);
    assertElementsAlmostEqual(c.Energy,cEnergy);

    %Test we can get a vector of variables energy
    tmp = [a b];
    assertElementsAlmostEqual(tmp.Energy,aEnergy+bEnergy);

    %Now let's try setting guesses.

    %First we initialize and make sure it's back to infinite
    fg.initialize();
    assertEqual(fg.Energy,Inf);

    a.Guess = 0;
    b.Guess = 0;
    c.Guess = 0;
    assertElementsAlmostEqual(fg.Energy,guessEnergy);

    %Now we set a guess
    a.Guess = 1;
    b.Guess = 1;
    c.Guess = 1;

    aEnergy = -log(4/4);
    bEnergy = -log(4/4);
    cEnergy = -log(1/3);
    f1Energy = -log(2/3);
    f2Energy = -log(2/3);
    guessEnergy = aEnergy+bEnergy+cEnergy+f1Energy+f2Energy;
    assertElementsAlmostEqual(fg.Energy,guessEnergy);

    %Verify that the guesses hold their values
    fg.Solver.iterate();
    assertElementsAlmostEqual(fg.Energy,guessEnergy);

    %Make sure the guesses are cleared.
    fg.solve();
    assertElementsAlmostEqual(fg.Energy,firstGuessEnergy);
    
end
