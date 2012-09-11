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

function testScore()

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

    %Before we solve the Score should be infinite since c will pick a 
    %value of 0 and b will pick a value of 1 and f2 enforces equality.
    assertEqual(fg.Score,Inf);

    fg.solve();

    %Now guess the Score and compare
    f1Score = -log(3/3);
    f2Score = -log(3/3);
    aScore = -log(2/4);
    bScore = -log(2/4);
    cScore = -log(3/3);
    guessScore = f1Score+f2Score+aScore+bScore+cScore;
    firstGuessScore = guessScore;
    assertElementsAlmostEqual(guessScore,fg.Score);
    assertElementsAlmostEqual(f1.Score,f1Score);
    assertElementsAlmostEqual(f2.Score,f2Score);
    assertElementsAlmostEqual(a.Score,aScore);
    assertElementsAlmostEqual(b.Score,bScore);
    assertElementsAlmostEqual(c.Score,cScore);

    %Test we can get a vector of variables Score
    tmp = [a b];
    assertElementsAlmostEqual(tmp.Score,aScore+bScore);

    %Now let's try setting guesses.

    %First we initialize and make sure it's back to infinite
    fg.initialize();
    assertEqual(fg.Score,Inf);

    a.Guess = 0;
    b.Guess = 0;
    c.Guess = 0;
    assertElementsAlmostEqual(fg.Score,guessScore);

    %Now we set a guess
    a.Guess = 1;
    b.Guess = 1;
    c.Guess = 1;

    aScore = -log(4/4);
    bScore = -log(4/4);
    cScore = -log(1/3);
    f1Score = -log(2/3);
    f2Score = -log(2/3);
    guessScore = aScore+bScore+cScore+f1Score+f2Score;
    assertElementsAlmostEqual(fg.Score,guessScore);

    %Verify that the guesses hold their values
    fg.Solver.iterate();
    assertElementsAlmostEqual(fg.Score,guessScore);

    %Make sure the guesses are cleared.
    fg.solve();
    assertElementsAlmostEqual(fg.Score,firstGuessScore);
    
end
